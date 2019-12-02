<template>
  <div class="mix-container">
    <div class="i-cont">
      <el-input
        ref="textarea"
        v-model="content"
        :autosize="inputAutosize"
        type="textarea"
        clearable
        resize="none"
        placeholder="请输入内容"
        @focus="inputFocus"/>
    </div>
    <section
      v-if="imgFiles.length > 0"
      class="img-cont">
      <flexbox wrap="wrap">
        <div
          v-for="(item, index) in imgFiles"
          :key="index"
          :style="{ 'background-image': 'url('+item.url+')' }"
          class="img-item"
          @mouseover="mouseImgOver(item, index)"
          @mouseleave="mouseImgLeave(item, index)">
          <div
            v-if="item.showDelete"
            class="img-delete"
            @click="deleteImgOrFile('image', item, index)">×</div>
        </div>
        <div class="img-item-add">
          <input
            type="file"
            class="img-item-iput"
            accept="image/*"
            multiple
            @change="uploadFile">
        </div>
      </flexbox>
      <div
        class="img-bar"
        @click="deleteAllImg">全部删除</div>
    </section>
    <section
      v-if="files.length > 0"
      class="file-cont">
      <flexbox class="f-header">
        <img
          class="f-logo"
          src="@/assets/img/send_file.png">
        <div class="f-name">附件</div>
      </flexbox>
      <div class="f-body">
        <flexbox
          v-for="(item, index) in files"
          :key="index"
          class="f-item">
          <img
            :src="item.icon"
            class="f-img">
          <div class="f-name">{{ item.name+'('+item.size+')' }}</div>
          <div
            class="close-button"
            @click="deleteImgOrFile('file', item, index)">×</div>
        </flexbox>
      </div>
      <div
        class="img-bar"
        @click="files=[]">全部删除</div>
    </section>
    <section
      v-if="business.length > 0"
      class="c-cont">
      <flexbox class="c-header">
        <img
          class="c-logo"
          src="@/assets/img/send_business.png">
        <div class="c-name">商机</div>
      </flexbox>
      <div class="c-body">
        <flexbox wrap="wrap">
          <flexbox
            v-for="(item, index) in business"
            :key="index"
            class="c-item">
            <div class="c-item-name">{{ item.businessName }}</div>
            <div
              class="c-item-close"
              @click="business.splice(index, 1)">×</div>
          </flexbox>
        </flexbox>
      </div>
    </section>
    <section
      v-if="contacts.length > 0"
      class="c-cont">
      <flexbox class="c-header">
        <img
          class="c-logo"
          src="@/assets/img/send_contacts.png">
        <div class="c-name">联系人</div>
      </flexbox>
      <div class="c-body">
        <flexbox wrap="wrap">
          <flexbox
            v-for="(item, index) in contacts"
            :key="index"
            class="c-item">
            <div class="c-item-name">{{ item.name }}</div>
            <div
              class="c-item-close"
              @click="contacts.splice(index, 1)">×</div>
          </flexbox>
        </flexbox>
      </div>
    </section>
    <flexbox class="bar-cont">
      <template v-for="(item, index) in barItems">
        <flexbox
          v-if="item.type=='img'||item.type=='file'"
          :key="index"
          class="bar-item"
          @click.native="barClick(item)">
          <input
            :accept="item.data"
            type="file"
            class="bar-input"
            multiple
            @change="uploadFile">
          <img
            :src="item.img"
            class="bar-img">
          <div class="bar-title">{{ item.title }}</div>
        </flexbox>
        <el-popover
          v-else
          :key="index"
          v-model="item.show"
          placement="bottom"
          width="700"
          popper-class="no-padding-popover"
          trigger="click">
          <crm-relative
            ref="crmrelative"
            :show="item.show"
            :radio="false"
            :action="{ type: 'condition', data: { moduleType: crmType, customerId: id } }"
            :selected-data="item.type == 'business' ? { 'business': business } : { 'contacts': contacts }"
            :crm-type="item.type"
            @close="item.show=false"
            @changeCheckout="checkRelativeInfos"/>
          <flexbox
            slot="reference"
            class="bar-item"
            @click.native="barClick(item)">
            <img
              :src="item.img"
              class="bar-img">
            <div class="bar-title">{{ item.title }}</div>
          </flexbox>
        </el-popover>
      </template>
    </flexbox>
  </div>
</template>

<script>
import { fileSize, getFileTypeIcon } from '@/utils/index'
import { crmFileSave, crmFileDelete } from '@/api/common'
import CrmRelative from '@/components/CreateCom/CrmRelative'

export default {
  /** 跟进记录 下的 添加 有添加框的都需要*/
  name: 'MixAdd',
  components: {
    CrmRelative
  },
  props: {
    /** 展示相关商机关联 */
    showRelativeBusiness: {
      type: Boolean,
      default: false
    },
    /** 展示相关商机关联 */
    showRelativeContacts: {
      type: Boolean,
      default: false
    },
    /** 模块ID */
    id: [String, Number],
    /** 没有值就是全部类型 有值就是当个类型 */
    crmType: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      /** 输入法 */
      content: '',
      inputAutosize: { minRows: 1, maxRows: 6 }, // 默认1 聚焦是 变成3
      /** 快捷添加 */
      barItems: [
        {
          img: require('@/assets/img/send_img.png'),
          title: '图片',
          type: 'img',
          data: 'image/*'
        },
        {
          img: require('@/assets/img/send_file.png'),
          title: '附件',
          type: 'file',
          data: '*.*'
        }
      ],
      /** 图片信息 */
      imgFiles: [],
      /** 图片信息 */
      files: [],
      /** 关联商机信息 */
      business: [],
      /** 关联联系人信息 */
      contacts: [],
      /** 展示关联弹窗 */
      showRelativeType: '',
      batchId: '' // 批次ID
    }
  },
  computed: {},
  mounted() {
    /** 控制展示关联商机 和 联系人 */
    if (this.showRelativeBusiness) {
      this.barItems.push({
        img: require('@/assets/img/send_business.png'),
        title: '关联商机',
        type: 'business',
        show: false
      })
    }

    if (this.showRelativeContacts) {
      this.barItems.push({
        img: require('@/assets/img/send_contacts.png'),
        title: '关联联系人',
        type: 'contacts',
        show: false
      })
    }
    /** 父组件通知子组件提交数据 */
    /** 将拼接好的数据回调父组件 this.$refs.child.$emit('submit-info');  调用*/
    this.$on('submit-info', function() {
      this.$emit('mixadd-info', {
        content: this.content,
        files: this.files,
        images: this.imgFiles,
        business: this.business,
        contacts: this.contacts,
        batchId: this.batchId
      })
    })
  },

  beforeDestroy() {
    this.$off('submit-info')
  },
  methods: {
    resetInfo() {
      /** 输入法 */
      this.content = ''
      /** 图片信息 */
      this.imgFiles = []
      /** 图片信息 */
      this.files = []
      /** 关联商机信息 */
      this.business = []
      /** 关联联系人信息 */
      this.contacts = []
      /** 展示关联弹窗 */
      this.showRelativeType = ''
      this.batchId = ''
    },
    /** 快捷添加按钮 */
    checkRelativeInfos(data) {
      if (this.showRelativeType == 'business') {
        this.business = data.data
      } else if (this.showRelativeType == 'contacts') {
        this.contacts = data.data
      }
    },
    barClick(item) {
      this.showRelativeType = item.type
      if (item.type == 'business') {
        item.show = true
      } else if (item.type == 'contacts') {
        item.show = true
      }
    },
    /** 图片选择出发 */
    uploadFile(event) {
      var files = event.target.files
      if (files.length) {
        for (let index = 0; index < files.length; index++) {
          const file = files[index]
          if (
            file.type.indexOf('image') == -1 &&
            this.showRelativeType == 'img'
          ) {
            this.$message.error('请上传正确的文件类型')
            return
          }
        }

        var type = event.target.accept == 'image/*' ? 'img' : 'file'
        var firstFile = files[0]
        this.sendFileRequest(firstFile, type, () => {
          for (let index = 1; index < files.length; index++) {
            const file = files[index]
            this.sendFileRequest(file, type)
          }
          event.target.value = ''
        })
      }
    },
    // 发送请求
    sendFileRequest(file, type, result) {
      var params = { file: file, type: type }
      if (this.batchId) {
        params.batchId = this.batchId
      }
      crmFileSave(params)
        .then(res => {
          if (this.batchId == '') {
            this.batchId = res.batchId
          }
          res.size = fileSize(file.size)
          if (type == 'img') {
            this.imgFiles.push(res)
          } else {
            res['icon'] = getFileTypeIcon(file)
            this.files.push(res)
          }
          if (result) {
            result()
          }
        })
        .catch(() => {})
    },
    /** 删除全部图片 */
    deleteAllImg() {
      this.imgFiles = []
    },
    deleteImgOrFile(type, item, index) {
      crmFileDelete({
        id: item.fileId
      })
        .then(res => {
          if (type == 'image') {
            this.imgFiles.splice(index, 1)
          } else {
            this.files.splice(index, 1)
          }
          this.$message.success('操作成功')
        })
        .catch(() => {})
    },
    /** 鼠标移入和移除 图片区域 */
    mouseImgOver(item, index) {
      item.showDelete = true
      this.$set(this.imgFiles, index, item)
    },
    mouseImgLeave(item, index) {
      item.showDelete = false
      this.$set(this.imgFiles, index, item)
    },
    inputFocus() {
      this.inputAutosize = { minRows: 3, maxRows: 6 }
      this.$nextTick(() => {
        this.$refs.textarea.resizeTextarea()
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.mix-container {
  position: relative;
  border: 1px solid #e6e6e6;
  border-radius: 3px;
  .i-cont {
    padding: 8px 10px;
  }
}

.i-cont /deep/ .el-textarea__inner {
  border: none;
  padding: 0;
}
/** 图片  */
.img-cont {
  padding: 0 10px;
  margin-bottom: 15px;
  .img-item {
    width: 98px;
    height: 98px;
    border: 1px solid #ccc;
    display: inline-block;
    margin: 0 4px 4px 0;
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
    position: relative;
    .img-delete {
      position: absolute;
      cursor: pointer;
      top: 0;
      right: 0;
      width: 20px;
      height: 20px;
      line-height: 20px;
      text-align: center;
      font-size: 17px;
      background-color: #666;
      color: white;
    }
  }
  .img-item-add {
    width: 98px;
    height: 98px;
    line-height: 98px;
    font-size: 60px;
    color: #ccc;
    text-align: center;
    margin: 0 4px 4px 0;
    cursor: pointer;
    display: inline-block;
    border-width: 1px;
    border-style: dashed;
    border-color: #ddd;
    position: relative;
    font-weight: 100;
    .img-item-iput {
      position: absolute;
      top: 0;
      right: 0;
      height: 98px;
      width: 98px;
      opacity: 0;
      cursor: pointer;
    }
  }
  .img-item-add:before {
    width: 2px;
    height: 39.5px;
    content: ' ';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background-color: #2888e4;
  }
  .img-item-add:after {
    width: 39.5px;
    height: 2px;
    content: ' ';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background-color: #2888e4;
  }
  .img-bar {
    color: #5a8ae2;
    font-size: 12px;
    padding: 5px 0;
    cursor: pointer;
  }
}
/** 附件  */
.file-cont {
  padding: 0 10px;
  margin: 0 10px 15px;
  border: 1px dashed #dfdfdf;
  .f-header {
    padding: 8px 0 15px;
    .f-logo {
      position: block;
      width: 15px;
      height: 15px;
      margin-right: 8px;
    }
    .f-name {
      color: #777;
      font-size: 12px;
    }
  }

  .f-body {
    .f-item {
      padding: 3px 0;
      height: 25px;
      .f-img {
        position: block;
        width: 15px;
        height: 15px;
        margin-right: 8px;
      }
      .f-name {
        color: #666;
        font-size: 12px;
      }
    }
  }

  .img-bar {
    color: #5a8ae2;
    font-size: 12px;
    padding: 5px 0;
  }
}
/** CRM  */
.c-cont {
  padding: 0 10px;
  margin: 0 10px 15px;
  border: 1px dashed #dfdfdf;
  .c-header {
    padding: 8px 0 15px;
    .c-logo {
      position: block;
      width: 15px;
      height: 15px;
      margin-right: 8px;
    }
    .c-name {
      color: #777;
      font-size: 12px;
    }
  }

  .c-body {
    margin-bottom: 10px;
    .c-item {
      height: 24px;
      border-radius: 12px;
      padding: 0 8px;
      margin: 0 5px 5px 0;
      background-color: #3487e2;
      color: white;
      width: auto;
      .c-item-name {
        font-size: 12px;
      }
      .c-item-close {
        padding-left: 5px;
        font-size: 17px;
      }
    }
  }
}
/** 底部bar  */
.bar-cont {
  background-color: #f9f9f9;
  padding: 8px 15px;
  .bar-item {
    width: auto;
    padding-right: 20px;
    position: relative;
    cursor: pointer;
    .bar-input {
      position: absolute;
      top: 0;
      right: 0;
      height: 15px;
      width: 68px;
      opacity: 0;
      font-size: 0;
      cursor: pointer;
    }
    .bar-img {
      display: block;
      width: 15px;
      height: 15px;
      margin-right: 8px;
    }
    .bar-title {
      color: #777;
      font-size: 12px;
    }
  }
}
/** 关闭按钮  */
.close-button {
  width: 30px;
  line-height: 16px;
  cursor: pointer;
  color: #ccc;
  height: 16px;
  font-size: 17px;
  text-align: center;
}
</style>
