<template>
  <div class="section">
    <div class="section-header"
         v-if="title && title.length > 0">
      <div class="section-mark"
           :style="{ 'border-left-color': mColor }"></div>
      <div class="section-title">{{title}}</div>
      <slot name="header"></slot>
    </div>
    <div class="content">
      <slot></slot>
    </div>
  </div>
</template>
<script type="text/javascript">
export default {
  name: 'create-sections',
  components: {},
  computed: {},
  data() {
    return {}
  },
  props: {
    title: {
      type: String,
      default: ''
    },
    mColor: {
      type: String,
      default: '#46CDCF'
    }
  },
  mounted() {},
  methods: {}
}
</script>
<style lang="scss" scoped>
.section {
  position: relative;
  background-color: white;
  margin-top: 8px;
}
.section:first-child {
  margin-top: 0;
}

.section-mark {
  border-left-width: 2px;
  border-left-style: solid;
  height: 10px;
}

.section-header {
  display: flex;
  align-items: center;
  padding: 5px 15px;
}
.section-title {
  font-size: 13px;
  color: #333;
  margin-left: 8px;
  flex-shrink: 0;
}

.content {
  padding: 0 10px;
}
</style>
