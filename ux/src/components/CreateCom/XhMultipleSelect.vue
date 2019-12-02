<template>
  <el-select
    v-model="dataValue"
    :disabled="disabled"
    style="width: 100%;"
    multiple
    placeholder="请选择"
    @change="valueChange">
    <el-option
      v-for="item in option"
      :key="item.value"
      :label="item.value"
      :value="item.value"/>
  </el-select>
</template>
<script type="text/javascript">
import arrayMixin from './arrayMixin'

export default {
  name: 'XhMultipleSelect', // 新建 multiple select
  components: {},
  mixins: [arrayMixin],
  props: {},
  data() {
    return {}
  },
  computed: {
    option() {
      var array = []
      if (this.item && this.item.data.setting) {
        for (let index = 0; index < this.item.data.setting.length; index++) {
          const element = this.item.data.setting[index]
          array.push({ value: element })
        }
      }
      return array
    }
  },
  watch: {},
  mounted() {},
  methods: {}
}
</script>
<style lang="scss" scoped>
</style>
