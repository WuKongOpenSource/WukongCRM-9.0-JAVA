<template>
  <div v-loading="loading"
       class="main-container">
    <flexbox class="content">
      <flexbox-item>
        <div class="axis-content">
          <div class="axismain"
               id="allChart"></div>
        </div>
      </flexbox-item>
      <flexbox-item>
        <div class="axis-content">
          <div class="axismain"
               id="dealChart"></div>
        </div>
      </flexbox-item>
    </flexbox>
  </div>
</template>

<script>
import echarts from 'echarts'
import 'echarts/map/js/china.js'
import axios from 'axios'
import { biAchievementAnalysisAPI } from '@/api/businessIntelligence/customerPortrayal'

export default {
  /** 城市分布分析 */
  name: 'customer-address-statistics',
  data() {
    return {
      loading: false,
      allOption: null,
      dealOption: null,
      allChart: null,
      dealChart: null,

      list: []
    }
  },
  computed: {},
  mounted() {
    this.initAxis()
    this.getDataList()
  },
  methods: {
    getDataList() {
      this.loading = true
      biAchievementAnalysisAPI()
        .then(res => {
          this.loading = false
          // this.axisList = res.data || []

          let allData = []
          let dealData = []
          let xAxis = []
          for (let index = 0; index < res.data.length; index++) {
            const element = res.data[index]
            if (element.allCustomer) {
              allData.push({
                name: element.address,
                value: element.allCustomer
              })
            }

            if (element.dealCustomer) {
              dealData.push({
                name: element.address,
                value: element.dealCustomer
              })
            }
          }

          this.allOption.series[0].data = allData
          this.dealOption.series[0].data = dealData
          this.allChart.setOption(this.allOption, true)
          this.dealChart.setOption(this.dealOption, true)
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 柱状图 */
    initAxis() {
      this.allChart = echarts.init(document.getElementById('allChart'))
      this.dealChart = echarts.init(document.getElementById('dealChart'))
      this.allOption = this.getChartOptione('全部客户')
      this.dealOption = this.getChartOptione('成交客户')
      this.allChart.setOption(this.allOption, true)
      this.dealChart.setOption(this.dealOption, true)
    },
    getChartOptione(title) {
      return {
        title: {
          text: title,
          left: 'center',
          bottom: 0
        },
        tooltip: {
          trigger: 'item',
          formatter: function(data) {
            return data.name + '<br/>' + (data.value || '-') + '（个）'
          }
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: ['客户数']
        },
        visualMap: {
          min: 0,
          max: 50,
          left: 'left',
          top: 'bottom',
          text: ['多', '少'], // 文本，默认为数值文本
          calculable: true,
          inRange: {
            color: ['lightskyblue', 'yellow', 'orangered']
          }
        },
        toolbox: {
          show: true,
          orient: 'vertical',
          left: 'right',
          top: 'center',
          feature: {
            mark: { show: true },
            dataView: { show: true, readOnly: false },
            restore: { show: true },
            saveAsImage: { show: true }
          }
        },
        series: [
          {
            name: '',
            type: 'map',
            mapType: 'china',
            showLegendSymbol: false,
            itemStyle: {
              normal: { label: { show: true }, borderColor: '#ccc' },
              emphasis: { label: { show: true } }
            },
            data: []
          }
        ]
      }
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import '../styles/detail.scss';
</style>
