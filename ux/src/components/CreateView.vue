<template>
  <transition name="opacity-fade">
    <div class="c-view"
         :style="{ 'background-color': backgroundColor, 'padding': padding+' 0', 'z-index': zIndex }">
      <el-card v-loading="loading"
               class="crm-create-card-container"
               :style="{ 'width': width}"
               :body-style="bodyStyle">
        <slot name="header"></slot>
        <slot></slot>
      </el-card>
    </div>
  </transition>
</template>
<script type="text/javascript">
import { getMaxIndex } from '@/utils/index'

export default {
  name: 'create-view', // 所有新建效果的view
  components: {},
  computed: {},
  watch: {},
  data() {
    return {
      zIndex: getMaxIndex(),
      loadingObj: null
    }
  },
  props: {
    bodyStyle: {
      type: Object,
      default: () => {
        return {}
      }
    },
    loading: {
      type: Boolean,
      default: false
    },
    //更改背景颜色颜色
    backgroundColor: {
      type: String,
      default: '#F5F6F9' // rgba(0, 0, 0, 0.6) 黑色半透明
    },
    /** 展示内容的宽 */
    width: {
      type: String,
      default: '700px'
    },
    /** 展示内容的上下padding */
    padding: {
      type: String,
      default: '40px'
    }
  },
  mounted() {},
  methods: {}
}
</script>
<style lang="scss" scoped>
.opacity-fade-enter-active,
.opacity-fade-leave-active {
  transition: all 0.2s;
}
.opacity-fade-enter,
.opacity-fade-leave-to {
  opacity: 0;
}
/** 容器布局 */
.c-view {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  right: 0;
}
.crm-create-card-container {
  margin: 0 auto;
  height: 100%;
}
</style>
