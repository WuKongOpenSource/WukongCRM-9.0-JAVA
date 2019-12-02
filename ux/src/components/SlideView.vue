<template>
  <transition
    name="slide-fade"
    @after-enter="afterEnter">
    <el-card
      id="slide"
      ref="slide"
      :style="{ 'z-index': zIndex }"
      :body-style="bodyStyle"
      class="slide-detail-card-container">
      <slot/>
    </el-card>
  </transition>
</template>
<script type="text/javascript">
import { getMaxIndex } from '@/utils/index'

export default {
  name: 'SlideView', // 客户管理详情 滑动view
  components: {},
  props: {
    bodyStyle: {
      type: Object,
      default: () => {
        return { padding: 0 }
      }
    },
    /** 监听点击事件 隐藏视图 */
    listenerIDs: {
      type: Array,
      default: () => {
        return []
      }
    },
    /** 阻挡点击事件 隐藏视图 */
    noListenerIDs: {
      type: Array,
      default: () => {
        return []
      }
    },
    noListenerClass: {
      type: Array,
      default: () => {
        return []
      }
    },
    appendToBody: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      zIndex: getMaxIndex()
    }
  },
  computed: {},
  watch: {},
  mounted() {
    if (this.appendToBody) {
      document.body.appendChild(this.$el)
    }
    this.listenerIDs.forEach(element => {
      if (document.getElementById(element)) {
        document
          .getElementById(element)
          .addEventListener('click', this.handleDocumentClick, false)
      }
    })
  },

  beforeDestroy() {
    if (this.appendToBody && this.$el && this.$el.parentNode) {
      this.$el.parentNode.removeChild(this.$el)
    }
  },
  methods: {
    handleDocumentClick(e) {
      var hidden = true
      this.noListenerIDs.forEach(element => {
        if (
          document.getElementById(element) &&
          document.getElementById(element).contains(e.target)
        ) {
          hidden = false
        }
      })

      this.noListenerClass.forEach(element => {
        var items = document.getElementsByClassName(element)
        if (items && hidden) {
          for (let index = 0; index < items.length; index++) {
            const element = items[index]
            if (element.contains(e.target)) {
              hidden = false
              break
            }
          }
        }
      })

      if (
        document.getElementById('slide') &&
        document.getElementById('slide').contains(e.target)
      ) {
        hidden = false
      }
      if (hidden) {
        this.$emit('side-close')
      }
    },
    afterEnter() {
      this.$emit('afterEnter')
    }
  }
}
</script>
<style lang="scss" scoped>
.slide-fade-enter-active,
.slide-fade-leave-active {
  will-change: transform;
  transition: all 0.35s ease;
}
.slide-fade-enter,
.slide-fade-leave-to {
  transform: translateX(100%);
}
</style>
