<template>
  <el-input v-model="dataValue"
            :type="type"
            :disabled="disabled"
            @input="valueChange"></el-input>
</template>
<script type="text/javascript">
import stringMixin from './stringMixin'

export default {
  name: 'xh-input', // 新建 input
  components: {},
  mixins: [stringMixin],
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
