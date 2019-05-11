<template>
  <div class="cell">
    <div class="div-flex">
      <div style="flex:1;">
        <div class="cell-title">{{title}}</div>
        <div class="cell-detail">{{detail}}</div>
      </div>
      <div style="flex-shrink:0;">
        <slot name="value"></slot>
        <div class="cell-time">{{time}}</div>
      </div>
    </div>
  </div>
</template>

<script type="text/javascript">
export default {
  name: 'details-cell', //标题  详情  时间 常用布局
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
    detail: {
      type: String,
      default: ''
    },
    time: {
      type: String,
      default: ''
    }
  },
  mounted() {},
  methods: {}
}
</script>
<style lang="scss" scoped>
.cell {
  padding: 10px 15px;
  position: relative;
  background-color: white;
}
.cell:before {
  content: ' ';
  position: absolute;
  top: 0;
  right: 0;
  height: 1px;
  border-top: 1px solid #e5e5e5;
  color: #e5e5e5;
  -webkit-transform-origin: 0 0;
  transform-origin: 0 0;
  -webkit-transform: scaleY(0.5);
  transform: scaleY(0.5);
  left: 15px;
  z-index: 2;
}
.cell:first-child:before {
  display: none;
}
.div-flex {
  display: -webkit-box;
  display: -webkit-flex;
  display: flex;
}
.cell-title {
  color: #333333;
  font-size: 13px;
}
.cell-detail {
  color: #999999;
  font-size: 12px;
  margin-top: 8px;
}
.cell-time {
  color: #333333;
  font-size: 13px;
}
</style>
