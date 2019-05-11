<template>
  <el-select style="width: 100%;"
             v-model="dataValue"
             @change="valueChange"
             multiple
             placeholder="请选择"
             :disabled="disabled">
    <el-option v-for="item in option"
               :key="item.value"
               :label="item.value"
               :value="item.value">
    </el-option>
  </el-select>
</template>
<script type="text/javascript">
import arrayMixin from './arrayMixin'

export default {
  name: 'xh-multiple-select', // 新建 multiple select
  components: {},
  mixins: [arrayMixin],
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
