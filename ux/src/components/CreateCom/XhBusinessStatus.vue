<template>
  <el-select style="width: 100%;"
             v-model="dataValue"
             @change="valueChange"
             placeholder="请选择"
             :disabled="disabled">
    <el-option v-for="item in options"
               :key="item.typeId"
               :label="item.name"
               :value="item.typeId">
    </el-option>
  </el-select>
</template>
<script type="text/javascript">
import stringMixin from './stringMixin'
import { crmBusinessStatusList } from '@/api/customermanagement/business'

export default {
  name: 'xh-business-status', // 商机状态
  components: {},
  mixins: [stringMixin],
  computed: {},
  data() {
    return {
      options: []
    }
  },
  props: {},
  mounted() {
    this.getBusinessStatusList()
  },
  methods: {
    /** 获取商机状态组 */
    getBusinessStatusList() {
      crmBusinessStatusList({})
        .then(res => {
          this.options = res.data
          if (this.dataValue) {
            for (let item of this.options) {
              if (item.typeId == this.dataValue) {
                this.$emit('value-change', {
                  index: this.index,
                  value: this.dataValue,
                  data: this.options,
                  type: 'init' // 初始化下不更改阶段值
                })
              }
            }
          }
        })
        .catch(() => {})
    },
    // 输入的值
    valueChange(val) {
      /** 商机组顺便回调筛选数据 */
      this.$emit('value-change', {
        index: this.index,
        value: val,
        data: this.options
      })
    }
  }
}
</script>
<style lang="scss" scoped>
</style>
