<template>
  <el-select style="width: 100%;"
             v-model="dataValue"
             :disabled="disabled"
             @change="valueChange"
             placeholder="请选择">
    <el-option v-for="(item, index) in option"
               :key="index"
               :label="item.num"
               :value="item.planId">
    </el-option>
  </el-select>
</template>
<script type="text/javascript">
import stringMixin from './stringMixin'
import {
  crmContractQueryReceivablesPlan
} from '@/api/customermanagement/contract'

export default {
  name: 'xh-receivables-plan', // 回款 下的 回款计划
  components: {},
  mixins: [stringMixin],
  watch: {
    relation: function(val) {
      if (val.moduleType) {
        this.getPlanList()
      } else {
        this.option = []
      }
    }
  },
  computed: {},
  data() {
    return {
      option: []
    }
  },
  props: {
    relation: {
      // 相关ID
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  mounted() {
    if (this.relation.moduleType) {
      this.getPlanList()
    }
  },
  methods: {
    getPlanList() {
      this.loading = true
      crmContractQueryReceivablesPlan({ contractId: this.relation.contractId, pageType: 0})
        .then(res => {
          this.loading = false
          this.option = res.data
        })
        .catch(() => {
          this.loading = false
        })
    }
  }
}
</script>
<style lang="scss" scoped>
</style>
