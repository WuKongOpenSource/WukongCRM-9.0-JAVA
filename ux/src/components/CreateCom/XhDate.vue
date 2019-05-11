<template>
  <el-date-picker style="width: 100%;"
                  v-model="dataValue"
                  type="date"
                  value-format="yyyy-MM-dd"
                  @change="valueChange"
                  placeholder="选择日期"
                  :disabled="disabled">
  </el-date-picker>
</template>
<script type="text/javascript">
import stringMixin from './stringMixin'

export default {
  name: 'xh-input', // 新建 date
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
</style>
