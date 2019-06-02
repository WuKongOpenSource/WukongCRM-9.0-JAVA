<template>
  <div v-loading="loading"
       class="main-container">
    <filtrate-handle-view class="filtrate-bar"
                          moduleType="business"
                          @load="loading=true"
                          @change="searchClick">
    </filtrate-handle-view>
    <div class="content">
      <div class="axis-content">
        <div id="axismain"></div>
      </div>
      <div class="table-content">
        <el-table :data="list"
                  height="400"
                  stripe
                  border
                  highlight-current-row>
          <el-table-column v-for="(item, index) in fieldList"
                           :key="index"
                           align="center"
                           header-align="center"
                           show-overflow-tooltip
                           :prop="item.field"
                           :label="item.name">
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import base from '../mixins/base'
import echarts from 'echarts'
import {
  biBusinessWinAPI,
  biBusinessTrendListAPI
} from '@/api/businessIntelligence/business'

export default {
  /** 赢单机会转化率趋势分析 */
  name: 'business-win-statistics',
  data() {
    return {
      loading: false,
      axisOption: null,
      axisChart: null,

      list: [],

      postParams: {}, // 筛选参数
      axisList: [],
      fieldList: [
        { field: 'businessName', name: '商机名称' },
        { field: 'customerName', name: '客户名称' },
        { field: 'typeName', name: '商机状态组' },
        { field: 'statusName', name: '商机阶段' },
        { field: 'money', name: '商机金额' },
        { field: 'dealDate', name: '预计成交日期' },
        { field: 'ownerUserName', name: '负责人' },
        { field: 'createTime', name: '创建时间' }
      ]
    }
  },
  mixins: [base],
  computed: {},
  mounted() {
    this.initAxis()
  },
  methods: {
    /**
     * 搜索点击
     */
    searchClick(params) {
      this.postParams = params
      this.getDataList()
      this.getRecordList()
    },
    /**
     * 图表数据
     */
    getDataList() {
      this.loading = true
      biBusinessWinAPI(this.postParams)
        .then(res => {
          this.loading = false
          this.axisList = res.data || []

          let endCounts = []
          let numCounts = []
          let proportionCounts = []
          let xAxis = []
          for (let index = 0; index < this.axisList.length; index++) {
            const element = this.axisList[index]
            endCounts.push(element.businessEnd)
            numCounts.push(element.businessNum)
            proportionCounts.push(element.proportion)
            xAxis.push(element.type)
          }
          this.axisOption.xAxis[0].data = xAxis

          this.axisOption.series[0].data = proportionCounts
          this.axisOption.series[1].data = numCounts
          this.axisOption.series[2].data = endCounts
          this.axisChart.setOption(this.axisOption, true)
        })
        .catch(() => {
          this.loading = false
        })
    },
    /**
     * 获取相关列表
     */
    getRecordList(dataIndex) {
      this.list = []

      let params = {}

      if (typeof dataIndex !== 'undefined') {
        let dataItem = this.axisList[dataIndex]
        params.user_id = this.postParams.user_id
        params.structure_id = this.postParams.structure_id
        params.startTime = dataItem.start_time
        params.endTime = dataItem.end_time
      } else {
        params = this.postParams
      }

      this.loading = true
      biBusinessTrendListAPI(params)
        .then(res => {
          this.loading = false
          this.list = res.data
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 柱状图 */
    initAxis() {
      var axisChart = echarts.init(document.getElementById('axismain'))

      var option = {
        color: ['#6ca2ff', '#6ac9d7', '#ff7474'],
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            // 坐标轴指示器，坐标轴触发有效
            type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
          }
        },
        legend: {
          data: ['赢单转化率', '商机总数', '赢单商机数'],
          bottom: '0px',
          itemWidth: 14
        },
        grid: {
          top: '40px',
          left: '40px',
          right: '40px',
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
            name: '赢单转化率',
            axisTick: {
              alignWithLabel: true,
              lineStyle: { width: 0 }
            },
            axisLabel: {
              color: '#BDBDBD',
              formatter: '{value}%'
            },
            /** 坐标轴轴线相关设置 */
            axisLine: {
              lineStyle: { color: '#BDBDBD' }
            },
            splitLine: {
              show: false
            }
          },
          {
            type: 'value',
            name: '商机数',
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
            name: '赢单转化率',
            type: 'line',
            yAxisIndex: 0,
            data: []
          },
          {
            name: '商机总数',
            type: 'bar',
            yAxisIndex: 1,
            barWidth: 15,
            data: []
          },
          {
            name: '赢单商机数',
            type: 'bar',
            yAxisIndex: 1,
            barWidth: 15,
            data: []
          }
        ]
      }

      axisChart.setOption(option, true)
      axisChart.on('click', params => {
        // seriesIndex	1：赢单转化率 2:商机总数  dataIndex 具体的哪条数据
        this.getRecordList(params.dataIndex)
      })
      this.axisOption = option
      this.axisChart = axisChart
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import '../styles/detail.scss';
</style>
