<template>
  <flexbox style="height:100%;"
           direction="column"
           align="stretch">
    <div class="header">
      系统配置
    </div>
    <div class="body"
         v-loading="loading">
      <el-button class="save-button"
                 @click="save"
                 type="primary">保存</el-button>

      <div class="section">
        <div class="name">企业名称</div>
        <el-input v-model="systemName"></el-input>
      </div>
      <div class="section">
        <div class="name">企业logo</div>
        <el-upload v-if="systemImage == ''"
                   drag
                   class="upload"
                   :show-file-list="false"
                   action="http"
                   accept="image/png, image/jpeg, image/gif, image/jpg"
                   :http-request="fileUpload">
          <i class="el-icon-plus uploader-icon"></i>
        </el-upload>
        <div v-else
             class="upload-show">
          <img :src="systemImage">
          <i @click="deleteSystemImage"
             class="el-icon-remove icon-delete"></i>
        </div>
      </div>
    </div>
    <edit-image title="编辑企业logo"
                :fixedNumber="[15, 4]"
                previewWidth="150px"
                previewHeight="40px"
                previewRadius="0"
                width="550px"
                :show="showEditImage"
                :image="editImage"
                :file="editFile"
                save-button-title="确定"
                @save="submiteImage"
                @close="showEditImage=false"></edit-image>
  </flexbox>
</template>

<script>
import { adminSystemSave } from '@/api/systemManagement/SystemConfig'
import EditImage from '@/components/EditImage'

export default {
  name: 'system-config',
  components: {
    EditImage
  },
  data() {
    return {
      loading: false,
      showEditImage: false,
      editImage: null,
      editFile: null,
      systemName: '',
      systemImage: '',
      editedImage: null // 编辑后的图片
    }
  },
  mounted() {
    this.getDetail()
  },
  methods: {
    /** 附件上传 */
    fileUpload(content) {
      const reader = new FileReader()
      var self = this
      reader.onload = function(e) {
        let result
        if (typeof e.target.result === 'object') {
          // 把Array Buffer转化为blob 如果是base64不需要
          result = window.URL.createObjectURL(new Blob([e.target.result]))
        } else {
          result = e.target.result
        }
        self.editImage = result
        self.editFile = content.file
        self.showEditImage = true
      }
      reader.readAsDataURL(content.file)
    },
    deleteSystemImage() {
      this.systemImage = ''
      this.editedImage = null
    },
    getDetail() {
      this.loading = true
      this.$store
        .dispatch('SystemLogoAndName')
        .then(res => {
          this.loading = false
          this.systemName = res.data.name ? res.data.name : ''
          this.systemImage = res.data.logo
        })
        .catch(() => {
          this.loading = false
        })
    },
    submiteImage(data) {
      this.editedImage = data
      this.systemImage = data.image
    },
    save() {
      if (!this.systemName) {
        this.$message.error('企业名称不能为空')
      } else {
        this.loading = true
        var param = new FormData()
        param.append('name', this.systemName)
        if (this.editedImage) {
          param.append(
            'file',
            this.editedImage.blob,
            this.editedImage.file.name
          )
        }
        adminSystemSave(param)
          .then(res => {
            this.loading = false
            this.$message.success('操作成功')
            this.getDetail()
          })
          .catch(() => {
            this.loading = false
          })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.header {
  height: 60px;
  line-height: 60px;
  position: relative;
  z-index: 100;
  padding: 0 20px;
  font-size: 18px;
}

.body {
  flex: 1;
  overflow-y: auto;
  padding: 40px 30px 20px 30px;
  background-color: white;
  border: 1px solid #e6e6e6;
  border-radius: 2px;
  position: relative;
}

.save-button {
  position: absolute;
  top: 8px;
  right: 30px;
}

.section {
  margin-bottom: 30px;
  .name {
    color: #777777;
    font-size: 12px;
    margin-bottom: 8px;
  }

  .el-input {
    width: 300px;
  }
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 300px;
  height: 80px;
  line-height: 80px;
  text-align: center;
}
.upload /deep/ .el-upload-dragger {
  width: 300px;
  height: 80px;
}
.upload-show {
  position: relative;
  display: block;
  width: 300px;
  height: 80px;
  img {
    width: 100%;
    height: 100%;
  }

  .icon-delete {
    position: absolute;
    top: -10px;
    right: -8px;
    color: red;
    font-size: 20px;
    display: none;
  }
}
.upload-show:hover {
  .icon-delete {
    display: block;
  }
}
</style>

