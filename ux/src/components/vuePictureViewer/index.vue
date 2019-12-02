<template>
  <div
    id="vue-picture-viewer"
    :style="maskContainer">
    <!-- 头部 -->
    <flexbox class="perview-header">
      <div class="left">{{ imgIndex + 1 }} / {{ imgLength }}</div>
      <div class="center">{{ bigImgName.slice(0,bigImgName.indexOf('.')) }}</div>
      <img
        class="close"
        src="./img/pre_close.png"
        @click="closeViewer" >
    </flexbox>
    <!-- 图片容器 -->
    <div
      ref="imgContainer"
      :style="imgContainer"
      class="imgContainer">
      <img
        v-if="bigShowType.isImage"
        ref="bigImg"
        :src="bigImgUrl"
        :style="bigImgStyle"
        alt="">
      <flexbox
        v-if="!bigShowType.isImage"
        class="file-show">
        <div class="file-icon">
          <img :src="bigShowType.icon" >
        </div>
        <div class="file-handle">
          <!-- <el-button type="primary"
                     @click.native="fileHandle('online')">在线预览</el-button> -->
          <el-button
            type="primary"
            plain
            @click.native="fileHandle('download')">下载</el-button>
        </div>
      </flexbox>
      <!-- tips -->
      <transition name="fade">
        <div
          v-show="showTips"
          class="tips">{{ tipsText }}</div>
      </transition>
    </div>
    <div class="fixedHandle">
      <!-- 操作按钮 -->
      <flexbox
        v-if="bigShowType.isImage"
        class="handleContainer">
        <img
          src="./img/pre_max.png"
          @click="enlarge" >
        <img
          src="./img/pre_min.png"
          @click="reduce" >
        <img
          style="padding: 4.5px;"
          src="./img/pre_rotate.png"
          @click="rotate" >
        <img
          src="./img/pre_down.png"
          @click="downloadImg(bigImgUrl, bigImgName)" >
      </flexbox>
      <!-- 缩略图容器 -->
      <div
        v-if="imgLength > 1"
        class="thumbnailContainer">
        <ul>
          <li
            v-for="(item, index) in imgData"
            ref="thumbnailItem"
            :key="index"
            @click="switchImgUrl(index, $event)">
            <img
              v-if="isShowImage(item.url)"
              :src="item.url"
              alt="">
            <img
              v-if="!isShowImage(item.url)"
              :src="getFileTypeIconWithSuffix(item.url)"
              alt="">
          </li>
        </ul>
      </div>
    </div>
    <!-- 左边箭头 -->
    <div
      class="leftArrowCon"
      @click="handlePrev"
      @mouseenter="enterLeft"
      @mouseout="outLeft">
      <img
        v-show="leftArrowShow"
        class="leftArrow"
        src="./img/pre_left.png"
        @click="enlarge" >
    </div>
    <!-- 右边箭头 -->
    <div
      class="rightArrowCon"
      @click="handleNext"
      @mouseenter="enterRight"
      @mouseout="outRight">
      <img
        v-show="rightArrowShow"
        class="rightArrow"
        src="./img/pre_right.png" >
    </div>
  </div>
</template>

<script>
import { getMaxIndex, downloadImage } from '@/utils'

export default {
  name: 'VuePictureViewer',
  props: {
    imgData: {
      type: Array,
      default: () => {
        return []
      }
    },
    background: {
      type: String,
      default: 'rgba(0,0,0,0.4)'
    },
    // 选择的索引
    selectIndex: {
      type: Number,
      default: -1
    }
  },
  data() {
    return {
      // 默认不显示左右切换箭头
      leftArrowShow: false,
      rightArrowShow: false,
      // 图片容器数据
      rotateDeg: 0,
      bigImgUrl: '',
      bigShowType: { isImage: true, icon: '' }, // 不是图片的时候 展示 icon
      bigImgName: '',
      imgLength: 0,
      imgIndex: 0,
      showTips: false,
      tipsText: '',
      bigImgConWidth: '',
      bigImgConHeight: '',
      maskContainer: {
        width: '100%',
        height: '100%',
        background: this.background,
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0
      },
      imgContainer: {
        width: 'auto',
        height: 'auto',
        position: 'absolute',
        top: '50%',
        left: '50%',
        'z-index': 100,
        transform: 'translate(-50%, -50%)'
      },
      bigImgStyle: {
        display: 'block',
        width: '80px',
        height: '80px',
        position: 'absolute',
        top: 50 + '%',
        left: 50 + '%',
        marginLeft: '',
        marginTop: '',
        userSelect: 'none'
      }
    }
  },
  mounted() {
    document
      .getElementById('vue-picture-viewer')
      .addEventListener('click', e => {
        e.stopPropagation()
      })

    this.imgLength = this.imgData.length
    this.imgIndex = this.selectIndex
    this.$nextTick(() => {
      this.bigImgUrl = this.imgData[this.imgIndex].url
      this.getShowTypeInfo(this.bigImgUrl)
      this.bigImgName = this.imgData[this.imgIndex].name
      if (this.imgLength > 1) {
        // 大于1的时候才会展示缩略图
        var item = this.$refs.thumbnailItem
        item[this.imgIndex].className = 'borderActive'
      }
    })
    var self = this
    this.$refs.bigImg.onload = () => {
      self.init()
    }

    this.maskContainer['z-index'] = getMaxIndex()
  },
  beforeDestroy() {
    if (document.getElementById('vue-picture-viewer')) {
      document
        .getElementById('vue-picture-viewer')
        .removeEventListener('click', e => {
          e.stopPropagation()
        })
    }
  },
  methods: {
    // init
    init() {
      const screenW =
        document.documentElement.offsetWidth || document.body.offsetWidth
      const screenH =
        document.documentElement.scrollHeight || document.body.scrollHeight
      this.$nextTick(function() {
        const ratio = [0.1, 0.2, 0.3, 0.4, 0.5, 0.7, 0.8, 0.9]
        for (const item of ratio) {
          if (
            this.$refs.bigImg.naturalWidth * item < screenW &&
            this.$refs.bigImg.naturalHeight * item < screenH - 200
          ) {
            this.bigImgConWidth = this.$refs.bigImg.naturalWidth * item
            this.bigImgConHeight = this.$refs.bigImg.naturalHeight * item
            this.imgContainer.width = this.bigImgConWidth + 'px'
            this.imgContainer.height = this.bigImgConHeight + 'px'
            this.bigImgStyle.width = this.bigImgConWidth + 'px'
            this.bigImgStyle.height = this.bigImgConHeight + 'px'
            this.bigImgStyle.marginLeft = -(this.bigImgConWidth / 2) + 'px'
            this.bigImgStyle.marginTop = -(this.bigImgConHeight / 2) + 'px'
          }
        }
      })
    },
    // rotate init
    rotateInit() {
      const screenH =
        document.documentElement.scrollHeight || document.body.scrollHeight
      const ratio = [0.1, 0.2, 0.3, 0.4, 0.5, 0.7, 0.8, 0.9]
      for (const item of ratio) {
        if (this.$refs.bigImg.naturalWidth * item < screenH - 160) {
          this.bigImgConWidth = this.$refs.bigImg.naturalWidth * item
          this.bigImgConHeight = this.$refs.bigImg.naturalHeight * item
          this.imgContainer.width = this.bigImgConWidth + 'px'
          this.imgContainer.height = this.bigImgConHeight + 'px'
          this.bigImgStyle.width = this.bigImgConWidth + 'px'
          this.bigImgStyle.height = this.bigImgConHeight + 'px'
          this.bigImgStyle.marginLeft = -(this.bigImgConWidth / 2) + 'px'
          this.bigImgStyle.marginTop = -(this.bigImgConHeight / 2) + 'px'
        }
      }
    },
    // 放大
    enlarge() {
      this.$nextTick(function() {
        const screenW =
          document.documentElement.offsetWidth || document.body.offsetWidth
        const screenH =
          document.documentElement.scrollHeight || document.body.scrollHeight
        if (
          (this.$refs.bigImg.offsetWidth >= this.$refs.bigImg.offsetHeight &&
            this.$refs.bigImg.offsetHeight * 2 < screenH * 2) ||
          (this.$refs.bigImg.offsetHeight >= this.$refs.bigImg.offsetWidth &&
            this.$refs.bigImg.offsetWidth * 2 < screenW * 2)
        ) {
          this.$refs.bigImg.style.width =
            this.$refs.bigImg.offsetWidth * 1.3 + 'px'
          this.$refs.bigImg.style.height =
            this.$refs.bigImg.offsetHeight * 1.3 + 'px'
          this.$refs.bigImg.style.left = '50%'
          this.$refs.bigImg.style.top = '50%'
          this.bigImgStyle.marginLeft =
            -this.$refs.bigImg.offsetWidth / 2 + 'px'
          this.bigImgStyle.marginTop =
            -this.$refs.bigImg.offsetHeight / 2 + 'px'
        }
      })
    },
    // 缩小
    reduce() {
      if (this.$refs.bigImg.offsetWidth > 80) {
        /**
           * clientWidth = width + padding
             offsetWidth = width + padding + border  */
        this.$refs.bigImg.style.width =
          this.$refs.bigImg.offsetWidth * 0.7 + 'px'
        this.$refs.bigImg.style.height =
          this.$refs.bigImg.offsetHeight * 0.7 + 'px'
        this.$refs.bigImg.style.left = '50%'
        this.$refs.bigImg.style.top = '50%'
        this.bigImgStyle.marginLeft = -this.$refs.bigImg.offsetWidth / 2 + 'px'
        this.bigImgStyle.marginTop = -this.$refs.bigImg.offsetHeight / 2 + 'px'
      }
    },
    // 旋转
    rotate() {
      if (this.rotateDeg === 0) {
        this.$refs.bigImg.style.transform = 'rotate(90deg)'
        this.rotateInit()
        this.rotateDeg++
      } else if (this.rotateDeg === 1) {
        this.$refs.bigImg.style.transform = 'rotate(180deg)'
        this.init()
        this.rotateDeg++
      } else if (this.rotateDeg === 2) {
        this.$refs.bigImg.style.transform = 'rotate(270deg)'
        this.rotateInit()
        this.rotateDeg++
      } else if (this.rotateDeg === 3) {
        this.$refs.bigImg.style.transform = 'rotate(360deg)'
        this.init()
        this.rotateDeg = 0
      }
    },
    // 点击缩略图切换图片
    switchImgUrl(num, e) {
      var item = this.$refs.thumbnailItem
      item.forEach(function(i) {
        i.className = ''
      })
      this.imgIndex = num
      this.bigImgUrl = this.imgData[num].url
      this.getShowTypeInfo(this.bigImgUrl)
      this.bigImgName = this.imgData[num].name
      e.currentTarget.className = 'borderActive'
      if (this.bigShowType.isImage) {
        this.init()
      }
    },
    // 切换到上一张
    handlePrev() {
      if (this.imgIndex <= 0) {
        this.tips('已经是第一张了!')
        this.imgIndex = 0
      } else {
        if (this.$refs.bigImg) {
          this.$refs.bigImg.style.transform = 'rotate(0deg)'
          this.rotateDeg = 0
        }

        this.imgIndex--
        this.bigImgUrl = this.imgData[this.imgIndex].url
        this.getShowTypeInfo(this.bigImgUrl)
        this.bigImgName = this.imgData[this.imgIndex].name
        var item = this.$refs.thumbnailItem
        item.forEach(function(i) {
          i.className = ''
        })
        item[this.imgIndex].className = 'borderActive'
        if (this.bigShowType.isImage) {
          this.init()
        }
      }
    },
    // 切换到下一张
    handleNext() {
      if (this.imgIndex + 1 >= this.imgData.length) {
        this.tips('已经是最后一张了!')
      } else {
        if (this.$refs.bigImg) {
          this.$refs.bigImg.style.transform = 'rotate(0deg)'
          this.rotateDeg = 0
        }

        this.imgIndex++
        this.bigImgUrl = this.imgData[this.imgIndex].url
        this.getShowTypeInfo(this.bigImgUrl)
        this.bigImgName = this.imgData[this.imgIndex].name

        var item = this.$refs.thumbnailItem
        item.forEach(function(i) {
          i.className = ''
        })
        item[this.imgIndex].className = 'borderActive'
        if (this.bigShowType.isImage) {
          this.init()
        }
      }
    },
    // 提示框
    tips(msg) {
      this.showTips = true
      this.tipsText = msg
      const _this = this
      setTimeout(function() {
        _this.showTips = false
      }, 10000)
    },
    // 下载图片
    downloadImg(data, filename) {
      downloadImage(data, filename)
    },
    // 鼠标左移
    enterLeft() {
      this.leftArrowShow = true
    },
    outLeft() {
      this.leftArrowShow = false
    },
    // 鼠标右移
    enterRight() {
      this.rightArrowShow = true
    },
    outRight() {
      this.rightArrowShow = false
    },
    // 关闭查看器
    closeViewer() {
      this.$emit('close-viewer')
    },
    /** 附件逻辑 */
    fileHandle(type) {
      var a = document.createElement('a')
      a.href = this.bigImgUrl
      a.download = this.bigImgName ? this.bigImgName : '文件'
      a.target = '_black'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
    },
    getShowTypeInfo(url) {
      const temps = url ? url.split('.') : []
      var ext = ''
      if (temps.length > 0) {
        ext = temps[temps.length - 1]
      } else {
        ext = ''
      }
      var icon = ''
      var isImage = true
      if (this.arrayContain(['jpg', 'png', 'gif', 'jpeg'], ext)) {
        isImage = true
        icon = require('@/assets/img/file_img.png')
      } else if (this.arrayContain(['mp4', 'mp3', 'avi'], ext)) {
        isImage = false
        icon = require('@/assets/img/file_excle.png')
      } else if (this.arrayContain(['xlsx', 'xls', 'XLSX', 'XLS'], ext)) {
        isImage = false
        icon = require('@/assets/img/file_excle.png')
      } else if (this.arrayContain(['doc', 'docx', 'DOC', 'DOCX'], ext)) {
        isImage = false
        icon = require('@/assets/img/file_word.png')
      } else if (this.arrayContain(['rar', 'zip'], ext)) {
        isImage = false
        icon = require('@/assets/img/file_zip.png')
      } else if (ext === 'pdf') {
        isImage = false
        icon = require('@/assets/img/file_pdf.png')
      } else if (ext === 'ppt' || ext === 'pptx') {
        isImage = false
        icon = require('@/assets/img/file_ppt.png')
      } else if (this.arrayContain(['txt', 'text'], ext)) {
        isImage = false
        icon = require('@/assets/img/file_txt.png')
      } else {
        isImage = false
        icon = require('@/assets/img/file_unknown.png')
      }
      this.bigShowType = { isImage: isImage, icon: icon }
    },
    getFileTypeIconWithSuffix(url) {
      const temps = url ? url.split('.') : []
      var ext = ''
      if (temps.length > 0) {
        ext = temps[temps.length - 1]
      } else {
        ext = ''
      }
      if (this.arrayContain(['jpg', 'png', 'gif', 'jpeg'], ext)) {
        return require('@/assets/img/file_img.png')
      } else if (this.arrayContain(['mp4', 'mp3', 'avi'], ext)) {
        return require('@/assets/img/file_excle.png')
      } else if (this.arrayContain(['xlsx', 'xls', 'XLSX', 'XLS'], ext)) {
        return require('@/assets/img/file_excle.png')
      } else if (this.arrayContain(['doc', 'docx', 'DOC', 'DOCX'], ext)) {
        return require('@/assets/img/file_word.png')
      } else if (this.arrayContain(['rar', 'zip'], ext)) {
        return require('@/assets/img/file_zip.png')
      } else if (ext === 'pdf') {
        return require('@/assets/img/file_pdf.png')
      } else if (ext === 'ppt' || ext === 'pptx') {
        return require('@/assets/img/file_ppt.png')
      } else if (this.arrayContain(['txt', 'text'], ext)) {
        return require('@/assets/img/file_txt.png')
      }
      return require('@/assets/img/file_unknown.png')
    },
    isShowImage(url) {
      const temps = url ? url.split('.') : []
      var ext = ''
      if (temps.length > 0) {
        ext = temps[temps.length - 1]
      } else {
        ext = ''
      }
      if (this.arrayContain(['jpg', 'png', 'gif', 'jpeg'], ext)) {
        return true
      }
      return false
    },
    arrayContain(array, string) {
      return array.some(item => {
        return item === string
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.perview-header {
  width: 100%;
  height: 40px;
  background: rgba(0, 0, 0, 0.6);
  color: rgba(255, 255, 255, 0.8);
  line-height: 40px;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 101;
  padding: 10px;
  .left {
    flex-shrink: 0;
    font-size: 14px;
  }
  .center {
    text-align: center;
    flex: 1;
    padding: 0 20px;
  }
  .close {
    display: block;
    padding: 8px;
    width: 40px;
    height: 40px;
    cursor: pointer;
  }
}

.leftArrowCon {
  width: 30%;
  height: calc(100% - 40px);
  background: transparent;
  position: absolute;
  top: 40px;
  left: 0;
  z-index: 98;
  cursor: pointer;
}
.rightArrowCon {
  width: 30%;
  height: calc(100% - 40px);
  background: transparent;
  position: absolute;
  top: 40px;
  right: 0;
  z-index: 99;
  cursor: pointer;
}
.leftArrow {
  position: absolute;
  top: 50%;
  left: 30px;
  margin-top: -60px;
  transition: all 0.5s;
  width: 50px;
  height: 50px;
  pointer-events: none;
}
.rightArrow {
  position: absolute;
  top: 50%;
  right: 30px;
  margin-top: -60px;
  width: 50px;
  height: 50px;
  transition: all 0.5s;
  pointer-events: none;
}
.imgContainer .tips {
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  text-align: center;
  line-height: 40px;
  position: absolute;
  left: 50%;
  top: 50%;
  min-width: 150px;
  margin-left: -60px;
  margin-top: -20px;
  border-radius: 6px;
  padding: 4px 4px;
  font-size: 14px;
}
.fixedHandle {
  width: 800px;
  height: 140px;
  position: fixed;
  left: 50%;
  bottom: 0;
  margin-left: -400px;
  overflow: hidden;
  z-index: 100;
}
.handleContainer {
  width: 210px;
  height: 40px;
  background: rgba(0, 0, 0, 0.6);
  line-height: 40px;
  border-radius: 20px;
  position: absolute;
  left: 50%;
  bottom: 100px;
  padding: 0 14px;
  margin-left: -100px;

  img {
    display: block;
    width: 40px;
    height: 40px;
    padding: 8px;
    margin: 0 2px;
    cursor: pointer;
  }
}
.handleItem {
  width: 28px;
  height: 28px;
  color: white;
}
ul {
  padding: 0;
  margin: 0;
}
ul li {
  list-style: none;
  display: inline-block;
  width: 30px;
  height: 30px;
  margin-left: 20px;
  cursor: pointer;
}

.thumbnailContainer {
  max-width: 800px;
  background: rgba(255, 255, 255, 0.7);
  position: absolute;
  left: 50%;
  bottom: 0;
  border-top-left-radius: 5px;
  border-top-right-radius: 5px;
  transform: translate(-50%, 0%);
  overflow-x: auto;
  overflow-y: hidden;
}

.thumbnailContainer ul {
  padding-top: 10px;
  padding-bottom: 10px;
  text-align: center;
  white-space: nowrap;
}
.thumbnailContainer ul li {
  display: inline-block;
  width: 38px;
  height: 38px;
  box-sizing: content-box;
  margin-left: 10px;
  user-select: none;
}
.thumbnailContainer ul li:last-child {
  margin-right: 10px;
}
.thumbnailContainer ul li img {
  display: inline-block;
  width: 38px;
  height: 38px;
  border-radius: 3px;
  box-sizing: content-box;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 1s;
}
.fade-enter,
.fade-leave-to {
  opacity: 0;
}

/* 添加border */
.borderActive {
  box-shadow: 0px 4px 7px 0px rgb(241, 140, 112);
}
/* 修改原生的滚动条 */
::-webkit-scrollbar {
  /* 血槽宽度 */
  width: 8px;
  height: 8px;
}
::-webkit-scrollbar-thumb {
  /* 拖动条 */
  background: rgba(0, 0, 0, 0.3);
  border-radius: 6px;
}
::-webkit-scrollbar-track {
  /* 背景槽 */
  background: #ddd;
  border-radius: 6px;
}
/** 文件展示*/
.file-show {
  position: absolute;
  top: 60%;
  left: 50%;
  width: 450px;
  height: 260px;
  margin-top: -150px;
  margin-left: -225px;
  background-color: white;
  border-radius: 3px;
  padding: 0 80px;
  .file-icon {
    flex: 1;
    img {
      display: block;
      width: 100px;
    }
  }

  .file-handle {
    button {
      display: block;
      width: 120px;
      margin-left: 0;
      margin-right: 0;
      height: 34px;
    }
    button:first-child {
      margin-bottom: 20px;
    }
  }
}
</style>
