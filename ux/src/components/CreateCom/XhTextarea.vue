<template>
  <el-input
    v-model="dataValue"
    :rows="3"
    :maxlength="200"
    :disabled="disabled"
    type="textarea"
    resize="none"
    show-word-limit
    @input="valueChange"/>
</template>
<script type="text/javascript">
import stringMixin from './stringMixin'

export default {
  name: 'XhTextarea', // 新建 textarea
  components: {},
  mixins: [stringMixin],
  props: {},
  data() {
    return {}
  },
  computed: {},
  watch: {},
  mounted() {},
  methods: {}
}
</script>
<style lang="scss" scoped>
/** 调整textarea输入 */
.el-textarea /deep/ .el-textarea__inner {
  padding: 5px 8px;
  border-radius: 3px;
  border: 1px solid #ddd;
  color: #333333;
}
.el-textarea /deep/ .el-textarea__inner:focus {
  outline: 0;
  border-color: #ddd;
}
</style>
