<template>
  <div class="xh-files-cont"
       :class="[disabled ? 'is_disabled' : 'is_valid']">
    <flexbox class="f-header"
             :class="[disabled ? 'is_disabled' : 'is_valid']"
             @click.native="selectImage">
      <img class="f-logo"
           v-if="!disabled"
           src="@/assets/img/relevance_file.png" />
      <div class="f-name">附件</div>
      <input :id="'xhImageInput' + index||'0'"
             type="file"
             class="bar-iput"
             accept="*.*"
             multiple
             @change="xhUploadFile" />
    </flexbox>
    <div class="f-body">
      <flexbox class="f-item"
               v-for="(item, index) in dataValue"
               :key="index">
        <img class="f-img"
             src="@/assets/img/relevance_file.png" />
        <div class="f-name">{{item.name.length > 25 ? (item.name.substring(0, 25) + '...'): item.name+'('+item.size+')'}}</div>
        <div class="close-button"
             @click="deleteFile(item, index)">×</div>
      </flexbox>
    </div>
  </div>
</template>
<script type="text/javascript">
import arrayMixin from './arrayMixin'
import { crmFileSave, crmFileDelete } from '@/api/common'
import { fileSize } from '@/utils/index'

export default {
  name: 'xh-files', // 新建 file  以数组的形式上传
  components: {},
  mixins: [arrayMixin],
  computed: {},
  watch: {},
  data() {
    return {
      batchId: '' // 批次ID
    }
  },
  props: {},
  mounted() {},
  methods: {
    selectImage() {
      if (!this.disabled) {
        document.getElementById('xhImageInput' + this.index || '0').click()
      }
    },
    /** 图片选择出发 */
    xhUploadFile(event) {
      var files = event.target.files
      var self = this
      var firstFile = files[0]
      this.sendFileRequest(firstFile, () => {
        for (let index = 1; index < files.length; index++) {
          const file = files[index]
          this.sendFileRequest(file)
        }
        event.target.value = ''
      })
    },
    // 发送请求
    sendFileRequest(file, result) {
      var params = { file: file }
      if (this.batchId) {
        params.batchId = this.batchId
      }
      crmFileSave(params)
        .then(res => {
          if (this.batchId == '') {
            this.batchId = res.batchId
          }
          res.size = fileSize(res.size)
          this.dataValue.push(res)
          this.$emit('value-change', {
            index: this.index,
            value: this.dataValue
          })
          if (result) {
            result()
          }
        })
        .catch(() => {})
    },
    /** 删除图片 */
    deleteFile(item, index) {
      this.$confirm('您确定要删除该文件吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          crmFileDelete({
            id: item.fileId
          })
            .then(res => {
              this.dataValue.splice(index, 1)
              this.$emit('value-change', {
                index: this.index,
                value: this.dataValue
              })
              this.$message.success('操作成功')
            })
            .catch(() => {})
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消操作'
          })
        })
    }
  }
}
</script>
<style lang="scss" scoped>
/** 附件  */
.xh-files-cont {
  position: relative;
  display: inline-block;
  border-radius: 3px;
  width: 100%;
  border: 1px solid #ddd;
  padding: 3.5px 5px;
  margin: 3px;
  line-height: 15px;
}

.xh-files-cont.is_disabled {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  cursor: not-allowed;
  .f-name {
    background-color: #f0f4f8ea;
    color: #c0c4cc;
    cursor: not-allowed;
  }
}

.xh-files-cont.is_valid:hover {
  border-color: #c0c4cc;
}

.f-header {
  cursor: pointer;
  padding: 5px 0 5px;
  .f-logo {
    position: block;
    width: 15px;
    height: 15px;
    margin-right: 8px;
  }
  .f-name {
    color: #3e84e9;
    font-size: 12px;
  }
}
.f-header.is_disabled {
  cursor: not-allowed;
}

.f-body {
  .f-item {
    padding: 3px 0;
    height: 25px;
    .f-img {
      position: block;
      width: 15px;
      height: 15px;
      padding: 0 1px;
      margin-right: 8px;
    }
    .f-name {
      color: #666;
      font-size: 12px;
    }
    .close-button {
      cursor: pointer;
    }
  }
}

.bar-iput {
  position: absolute;
  top: 0;
  left: 0;
  height: 0;
  width: 0;
  opacity: 0;
}
</style>
