<template>
  <div v-loading="loading"
       class="main-container">
    <filtrate-handle-view class="filtrate-bar"
                          moduleType="customer"
                          :showCustomSelect="true"
                          :customDefault="showType"
                          :customOptions="[{name:'折线图', value: 'line'}, {name:'饼状图', value: 'pie'},{name:'柱状图', value: 'bar'}]"
                          @load="loading=true"
                          @change="searchClick"
                          @typeChange="showTypeChange">
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
  biCustomerConversionInfoAPI,
  biCustomerConversionAPI
} from '@/api/businessIntelligence/customer'

export default {
  /** 客户转化率分析 */
  name: 'customer-conversion-statistics',
  data() {
    return {
      loading: false,
      showType: 'line',

      axisOption: null,
      pieOption: null,
      axisChart: null,

      postParams: {}, // 筛选参数
      list: [],
      axisList: [],
      fieldList: [
        { field: 'customerName', name: '客户名称' },
        { field: 'contractName', name: '合同名称' },
        { field: 'contractMoney', name: '合同金额（元）' },
        { field: 'receivablesMoney', name: '回款金额（元）' },
        { field: '客户行业', name: '客户行业' },
        { field: '客户来源', name: '客户来源' },
        { field: 'ownerUserName', name: '负责人' },
        { field: 'createUserName', name: '创建人' },
        { field: 'createTime', name: '创建时间' },
        { field: 'orderDate', name: '下单时间' }
      ]
    }
  },
  mixins: [base],
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
      biCustomerConversionAPI(this.postParams)
        .then(res => {
          this.loading = false
          let list = res.data || []
          this.axisList = list

          let pieData = []
          let axisData = []
          let legendData = []
          for (let index = 0; index < list.length; index++) {
            const element = list[index]
            pieData.push({ name: element.type, value: element.pro })
            axisData.push(element.pro)
            legendData.push(element.type)
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
    /**
     * 获取相关列表
     */
    getRecordList(dataIndex) {
      this.list = []

      let params = {}

      if (typeof dataIndex !== 'undefined') {
        let dataItem = this.axisList[dataIndex]
        params.userId = this.postParams.userId
        params.deptId = this.postParams.deptId
        params.startTime = dataItem.startTime
        params.endTime = dataItem.endTime
      } else {
        params = this.postParams
      }

      this.loading = true
      biCustomerConversionInfoAPI(params)
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
      this.axisChart = echarts.init(document.getElementById('axismain'))
      this.axisChart.on('click', params => {
        // seriesIndex	1：跟进客户数 2:跟进次数  dataIndex 具体的哪条数据
        this.getRecordList(params.dataIndex)
      })

      this.axisOption = {
        color: ['#6ca2ff'],
        tooltip: {
          trigger: 'axis',
          formatter: '{b} : {c}% ',
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
              formatter: '{value}%'
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
            type: this.showType,
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
          bottom: '0px',
          data: []
        },
        series: [
          {
            name: '',
            type: 'pie',
            radius: '55%',
            center: ['40%', '50%'],
            stillShowZeroSum: false,
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
