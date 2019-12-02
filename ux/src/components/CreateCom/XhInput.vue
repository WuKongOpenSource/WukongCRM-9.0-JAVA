<template>
  <el-input
    v-model="dataValue"
    :type="type"
    :disabled="disabled"
    @input="valueChange"/>
</template>
<script type="text/javascript">
import stringMixin from './stringMixin'

export default {
  name: 'XhInput', // 新建 input
  components: {},
  mixins: [stringMixin],
  props: {},
  data() {
    return {}
  },
  computed: {
    type() {
      if (this.item && this.item.form_type == 'password') {
        return this.item.form_type
      } else {
        return 'text'
      }
    }
  },
  watch: {},
  mounted() {},
  methods: {}
}
</script>
<style lang="scss" scoped>
</style>
