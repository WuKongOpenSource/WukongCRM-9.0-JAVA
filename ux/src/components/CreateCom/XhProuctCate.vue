<template>
  <el-cascader ref="elCascader"
               style="width: 100%;"
               :options="options"
               change-on-select
               :show-all-levels="false"
               :props="defaultProps"
               @change="valueChange"
               v-model="dataValue"
               :disabled="disabled">
  </el-cascader>
</template>
<script type="text/javascript">
import arrayMixin from './arrayMixin'
import { productCategoryIndex } from '@/api/systemManagement/SystemCustomer'

export default {
  name: 'xh-produc-cate', // 新建 产品分类
  components: {},
  mixins: [arrayMixin],
  computed: {},
  watch: {},
  data() {
    return {
      options: [],
      defaultProps: {
        children: 'children',
        label: 'label',
        value: 'categoryId'
      }
    }
  },
  props: {},
  mounted() {
    this.getProductCategoryIndex()
  },
  methods: {
    /** 获取产品分类数据 */
    getProductCategoryIndex() {
      productCategoryIndex({
        type: 'tree'
      })
        .then(res => {
          this.options = res.data
        })
        .catch(() => {})
    },

    valueChange(val) {
      this.$emit('value-change', {
        index: this.index,
        item: this.item,
        value: val,
        valueContent: this.$refs.elCascader.currentLabels.join(',')
      })
    }
  }
}
</script>
<style lang="scss" scoped>
</style>
