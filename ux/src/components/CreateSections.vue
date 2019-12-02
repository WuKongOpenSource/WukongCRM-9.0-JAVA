<template>
  <div class="section">
    <div
      v-if="title && title.length > 0"
      class="section-header">
      <div
        :style="{ 'border-left-color': mColor }"
        class="section-mark"/>
      <div class="section-title">{{ title }}</div>
      <slot name="header"/>
    </div>
    <div class="content">
      <slot/>
    </div>
  </div>
</template>
<script type="text/javascript">
export default {
  name: 'CreateSections',
  components: {},
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
  data() {
    return {}
  },
  computed: {},
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
