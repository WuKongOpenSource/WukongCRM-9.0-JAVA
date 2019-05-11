<template>
  <el-input v-model="dataValue"
            type="textarea"
            resize="none"
            :rows="3"
            @input="valueChange"
            :disabled="disabled"></el-input>
</template>
<script type="text/javascript">
import stringMixin from './stringMixin'

export default {
  name: 'xh-textarea', //新建 textarea
  components: {},
  mixins: [stringMixin],
  computed: {},
  watch: {},
  data() {
    return {}
  },
  props: {},
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
