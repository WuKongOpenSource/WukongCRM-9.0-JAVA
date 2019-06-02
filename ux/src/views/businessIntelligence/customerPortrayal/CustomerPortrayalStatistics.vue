<template>
  <div v-loading="loading"
       class="main-container">
    <filtrate-handle-view class="filtrate-bar"
                          moduleType="portrait"
                          @load="loading=true"
                          @change="getDataList">
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
                           :fixed="index==0?true: false"
                           :min-width="index==0?180: 100"
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
import { biAchievementPortraitAPI } from '@/api/businessIntelligence/customerPortrayal'

export default {
  /** 客户画像分析 */
  name: 'customer-portrayal-statistics',
  data() {
    return {
      loading: false,
      axisOption: null,
      axisChart: null,

      list: [],
      type: '', // 类型 source：客户来源；industry：客户行业；level：客户级别

      postParams: {},
      fieldList: []
    }
  },
  props: {},
  mixins: [base],
  computed: {},
  created() {
    this.type = this.$route.params.type
  },
  mounted() {
    this.initAxis()
  },
  beforeRouteUpdate(to, from, next) {
    this.type = to.params.type
    this.getDataList(this.postParams)
    next()
  },
  methods: {
    getDataList(params) {
      this.postParams = params
      this.loading = true
      params.type_analyse = this.type
      biAchievementPortraitAPI(params)
        .then(res => {
          this.loading = false

          // 图表信息
          let allData = []
          let dealData = []
          let legendData = []

          // 表信息
          let list = [
            {
              name: '所有客户（个）'
            },
            {
              name: '成交客户（个）'
            }
          ]
          let fieldList = [
            {
              field: 'name',
              name: {
                source: '客户来源',
                industry: '客户行业',
                level: '客户级别'
              }[this.type]
            }
          ]

          for (let eIndex = 0; eIndex < res.data.length; eIndex++) {
            const element = res.data[eIndex]
            // 图表信息
            allData.push({
              name: element[this.type],
              value: element.allCustomer
            })
            dealData.push({
              name: element[this.type],
              value: element.dealCustomer
            })
            legendData.push(element[this.type])

            // 处理表头逻辑
            let fieldKey = 'value' + eIndex
            if (fieldList.length <= res.data.length) {
              fieldList.push({
                field: fieldKey,
                name: element[this.type]
              })
            }
            let keys = ['allCustomer', 'dealCustomer']
            for (let keyIndex = 0; keyIndex < keys.length; keyIndex++) {
              const keyElement = keys[keyIndex]
              list[keyIndex][fieldKey] = element[keyElement]
            }
          }

          // 图表展示
          this.axisOption.legend.data = legendData

          this.axisOption.series[0].data = allData
          this.axisOption.series[1].data = dealData

          this.axisChart.setOption(this.axisOption, true)

          // 列表展示
          this.fieldList = fieldList
          this.list = list
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 柱状图 */
    initAxis() {
      this.axisChart = echarts.init(document.getElementById('axismain'))
      this.axisOption = this.getChartOptione()
      this.axisChart.setOption(this.axisOption, true)
    },
    getChartOptione() {
      return {
        title: [
          {
            text: '全部客户',
            x: '20%',
            bottom: '25'
          },
          {
            text: '成交客户',
            x: '70%',
            bottom: '25'
          }
        ],
        color: this.chartColors,
        tooltip: {
          trigger: 'item',
          formatter: '{b} : {c}'
        },
        legend: {
          x: 'center',
          y: 'bottom',
          type: 'scroll',
          data: []
        },
        series: [
          {
            name: '全部客户',
            type: 'pie',
            radius: ['35%', '50%'],
            center: ['25%', '50%'],
            data: []
          },
          {
            name: '成交客户',
            type: 'pie',
            radius: ['35%', '50%'],
            center: ['75%', '50%'],
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

.axis-content {
  #axismain {
    height: 420px;
  }
}
</style>
