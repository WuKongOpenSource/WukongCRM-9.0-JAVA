<template>
  <el-date-picker
    v-model="dataValue"
    :disabled="disabled"
    style="width: 100%;"
    type="date"
    value-format="yyyy-MM-dd"
    placeholder="选择日期"
    @change="valueChange"/>
</template>
<script type="text/javascript">
import stringMixin from './stringMixin'

export default {
  name: 'XhInput', // 新建 date
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
</style>
