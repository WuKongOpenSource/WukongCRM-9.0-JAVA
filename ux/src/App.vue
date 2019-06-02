<template>
  <div id="app">
    <router-view class="router-view" />
    <vue-picture-viewer :imgData="previewImgs"
                        :select-index="previewIndex"
                        v-if="showPreviewImg"
                        @close-viewer="showPreviewImg=false"></vue-picture-viewer>
  </div>
</template>

<script>
/** 常用图片预览创建组件 */
import VuePictureViewer from '@/components/vuePictureViewer/index'
import { mapGetters } from 'vuex'

export default {
  name: 'App',
  components: {
    VuePictureViewer
  },
  data() {
    return {
      showPreviewImg: false,
      previewIndex: 0,
      previewImgs: []
    }
  },
  computed: {
    ...mapGetters(['activeIndex'])
  },
  watch: {
    $route(to, from) {
      this.showPreviewImg = false //切换页面隐藏图片预览
      // 如果没有存 获取一下
      let paths = to.path.split('/')
      if (paths.length >= 3) {
        paths.splice(0, 2)
        this.$store.commit('SET_ACTIVEINDEX', paths.length == 1 ? paths[0] : paths.join('/'))
      }
    }
  },
  mounted() {
    this.addBus()
  },
  methods: {
    addBus() {
      var self = this
      this.$bus.on('preview-image-bus', function(data) {
        self.previewIndex = data.index
        self.previewImgs = data.data
        self.showPreviewImg = true
      })
    }
  }
}
</script>

<style>
#app {
  width: 100%;
  position: relative;
  height: 100%;
}
</style>
