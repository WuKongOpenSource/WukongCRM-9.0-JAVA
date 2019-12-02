<template>
  <div
    v-loading="loading"
    class="main-container">
    <filtrate-handle-view
      :show-product-selct="true"
      class="filtrate-bar"
      module-type="product"
      @load="loading=true"
      @change="getDataList"/>
    <div class="content">
      <div class="axis-content">
        <div id="axismain"/>
      </div>
    </div>
  </div>
</template>

<script>
import base from '../mixins/base'
import echarts from 'echarts'
import { biProductCategoryAPI } from '@/api/businessIntelligence/product'

export default {
  /** 产品分类销量分析 */
  name: 'ProductCategoryStatistics',
  mixins: [base],
  data() {
    return {
      loading: false,
      axisOption: null,
      axisChart: null
    }
  },
  computed: {},
  mounted() {
    this.initAxis()
  },
  methods: {
    getDataList(params) {
      this.loading = true
      biProductCategoryAPI(params)
        .then(res => {
          this.loading = false

          const numCounts = []
          const legendData = []
          for (let index = 0; index < res.data.length; index++) {
            const element = res.data[index]
            numCounts.push({ name: element.productName, value: element.num })
            legendData.push(element.productName)
          }
          this.axisOption.legend.data = legendData
          this.axisOption.series[0].data = numCounts
          this.axisChart.setOption(this.axisOption, true)
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 柱状图 */
    initAxis() {
      var axisChart = echarts.init(document.getElementById('axismain'))

      var option = {
        title: {
          text: '产品分类销售',
          x: 'center',
          bottom: '10'
        },
        color: this.chartColors,
        tooltip: {
          trigger: 'item',
          formatter: '{b} : {c}'
        },
        legend: {
          orient: 'vertical',
          type: 'scroll',
          x: 'left',
          data: []
        },
        series: [
          {
            name: '',
            type: 'pie',
            radius: ['50%', '70%'],
            data: []
          }
        ]
      }

      axisChart.setOption(option, true)
      this.axisOption = option
      this.axisChart = axisChart
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import '../styles/detail.scss';
</style>
