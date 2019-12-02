<template>
  <div
    v-loading="loading"
    class="main-container">
    <filtrate-handle-view
      :show-custom-select="true"
      :custom-default="showType"
      :custom-options="[{name:'饼状图', value: 'pie'},{name:'柱状图', value: 'bar'}]"
      class="filtrate-bar"
      module-type="customer"
      @load="loading=true"
      @change="getDataList"
      @typeChange="showTypeChange"/>
    <div class="content">
      <div class="axis-content">
        <div id="axismain"/>
      </div>
      <div class="table-content">
        <el-table
          :data="list"
          height="400"
          stripe
          border
          highlight-current-row>
          <el-table-column
            v-for="(item, index) in fieldList"
            :key="index"
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
import base from '../mixins/base'
import echarts from 'echarts'
import { biCustomerRecordModeAPI } from '@/api/businessIntelligence/customer'

export default {
  /** 客户跟进方式分析 */
  name: 'CustomerRecordModeStatistics',
  mixins: [base],
  data() {
    return {
      loading: false,
      showType: 'pie',

      axisOption: null,
      axisChart: null,

      pieOption: null,

      list: [],
      fieldList: [
        { field: 'category', name: '跟进方式' },
        { field: 'recordNum', name: '个数' },
        { field: 'proportion', name: '占比（%）' }
      ]
    }
  },
  computed: {},
  mounted() {
    this.initPie()
    this.initAxis()
  },
  methods: {
    showTypeChange(type) {
      this.showType = type
      this.refreshChartInfo()
    },
    refreshChartInfo() {
      if (this.showType != 'pie') {
        this.axisOption.series[0].type = this.showType
        this.axisChart.setOption(this.axisOption, true)
      } else {
        this.axisChart.setOption(this.pieOption, true)
      }
    },
    getDataList(params) {
      biCustomerRecordModeAPI(params)
        .then(res => {
          this.loading = false
          this.list = res.data || []

          const pieData = []
          const axisData = []
          const legendData = []
          for (let index = 0; index < this.list.length; index++) {
            const element = this.list[index]
            pieData.push({ name: element.category, value: element.proportion })
            axisData.push(element.recordNum)
            legendData.push(element.category)
          }

          this.pieOption.legend.data = legendData
          this.pieOption.series[0].data = pieData

          this.axisOption.xAxis[0].data = legendData
          this.axisOption.series[0].data = axisData

          this.refreshChartInfo()
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 柱状图 */
    initAxis() {
      this.axisChart = echarts.init(document.getElementById('axismain'))

      this.axisOption = {
        color: ['#6ca2ff'],
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            // 坐标轴指示器，坐标轴触发有效
            type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
          }
        },
        grid: {
          top: '40px',
          left: '30px',
          right: '30px',
          bottom: '40px',
          containLabel: true,
          borderColor: '#fff'
        },
        xAxis: [
          {
            type: 'category',
            data: [],
            axisTick: {
              alignWithLabel: true,
              lineStyle: { width: 0 }
            },
            axisLabel: {
              color: '#BDBDBD'
            },
            /** 坐标轴轴线相关设置 */
            axisLine: {
              lineStyle: { color: '#BDBDBD' }
            },
            splitLine: {
              show: false
            }
          }
        ],
        yAxis: [
          {
            type: 'value',
            name: '',
            axisTick: {
              alignWithLabel: true,
              lineStyle: { width: 0 }
            },
            axisLabel: {
              color: '#BDBDBD',
              formatter: '{value}个'
            },
            /** 坐标轴轴线相关设置 */
            axisLine: {
              lineStyle: { color: '#BDBDBD' }
            },
            splitLine: {
              show: false
            }
          }
        ],
        series: [
          {
            name: '',
            type: 'bar',
            barWidth: 15,
            data: []
          }
        ]
      }
    },
    /** 饼状图 */
    initPie() {
      this.pieOption = {
        color: this.chartColors,
        tooltip: {
          trigger: 'item',
          formatter: '{b} : {c}% '
        },
        legend: {
          type: 'scroll',
          bottom: '0',
          x: 'center',
          data: []
        },
        series: [
          {
            name: '',
            type: 'pie',
            radius: '55%',
            stillShowZeroSum: false,
            center: ['40%', '50%'],
            data: [],
            itemStyle: {
              emphasis: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
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
