<template>
  <el-tabs v-model="tabActiveName"
           class="main-container">
    <el-tab-pane v-for="(item, index) in tabList"
                 :key="index"
                 :label="item.label"
                 :name="item.name">
      <cycle-view :type="item.name"
                  :show="item.name == tabActiveName"></cycle-view>
    </el-tab-pane>
  </el-tabs>
</template>

<script>
import CycleView from './components/CycleView'

export default {
  /** 成交周期分析 */
  name: 'customer-cycle-statistics',
  components: {
    CycleView
  },
  data() {
    return {
      tabActiveName: 'customer',
      tabList: [
        { label: '员工客户成交周期', name: 'customer' },
        { label: '地区成交周期', name: 'address' },
        { label: '产品成交周期', name: 'product' }
      ]
    }
  },
  computed: {},
  mounted() {},
  methods: {}
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import '../styles/detail.scss';

.main-container {
  height: 100%;
  position: relative;

  /deep/ .el-tabs__header {
    padding: 10px 20px 0;
    margin-bottom: 0;
  }

  /deep/ .el-tabs__content {
    overflow: hidden;
    height: 100%;

    .el-tab-pane {
      overflow: hidden;
      height: 100%;
    }
  }
}
</style>
