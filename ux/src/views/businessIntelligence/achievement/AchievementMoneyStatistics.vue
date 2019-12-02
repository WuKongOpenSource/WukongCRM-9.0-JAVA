<template>
  <div
    v-loading="loading"
    class="main-container">
    <filtrate-handle-view
      :show-year-select="true"
      class="filtrate-bar"
      module-type="contract"
      @load="loading=true"
      @change="getDataList"/>
    <div class="content">
      <div class="axis-content">
        <div id="axismain"/>
      </div>
      <div class="table-content">
        <el-table
          :data="list"
          height="300"
          stripe
          border
          highlight-current-row>
          <el-table-column
            v-for="(item, index) in fieldList"
            :key="index"
            :fixed="index==0?true: false"
            :min-width="index==0?180: 100"
            :prop="item.field"
            :label="item.name"
            align="center"
            header-align="center"
            show-overflow-tooltip/>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import achievementMixins from '../mixins/achievement'

export default {
  /** 合同金额分析 */
  name: 'AchievementMoneyStatistics',
  mixins: [achievementMixins],
  data() {
    return {}
  },
  computed: {},
  created() {
    this.type = 'money'
  },
  methods: {}
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import '../styles/detail.scss';
</style>
