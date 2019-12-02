<template>
  <div id="app">
    <router-view class="router-view" />
    <vue-picture-viewer
      v-if="showPreviewImg"
      :img-data="previewImgs"
      :select-index="previewIndex"
      @close-viewer="showPreviewImg=false"/>
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
      this.showPreviewImg = false // 切换页面隐藏图片预览
      if (to.meta.menuIndex) {
        this.$store.commit('SET_ACTIVEINDEX', to.meta.menuIndex)
      } else {
        this.$store.commit('SET_ACTIVEINDEX', to.path)
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
