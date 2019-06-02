<template>
  <div class="cycle-content"
       v-loading="loading">
    <filtrate-handle-view class="filtrate-bar"
                          moduleType="customer"
                          @load="loading=true"
                          @change="getDataList">
    </filtrate-handle-view>
    <div class="content">
      <div class="axis-content">
        <div class="content-title">{{title}}</div>
        <div class="axismain"
             :id="'axismain' + type"></div>
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
<script type="text/javascript">
import base from '../../mixins/base'
import echarts from 'echarts'
import {
  biCustomerUserCycleAPI,
  biCustomerAddressCycleAPI,
  biCustomerProductCycleAPI,
  employeeCycleInfo
} from '@/api/businessIntelligence/customer'

export default {
  name: 'cycle-view',
  components: {},
  computed: {
    title() {
      return {
        customer: '员工客户成交周期（根据合同下单时间和客户创建时间计算）',
        address: '地区成交周期（根据合同下单时间和客户创建时间计算）',
        product: '产品成交周期（根据合同下单时间和客户创建时间计算）'
      }[this.type]
    }
  },
  data() {
    return {
      loading: false,
      axisOption: null,
      axisChart: null,

      postParams: {},
      list: [],
      fieldList: [],
      initView: false // 默认没有初始化
    }
  },
  watch: {
    show: function(value) {
      if (value && !this.initView) {
        this.initView = true
        this.$nextTick(() => {
          this.initAxis()
          this.getDataList(this.postParams)
        })
      }
    }
  },
  mixins: [base],
  props: {
    type: {
      required: true,
      type: String
    }, // 'customer' 'product' 'address'
    show: {
      // 是否展示
      required: true,
      type: Boolean
    }
  },
  mounted() {
    this.fieldList = this.getFieldList()
    if (this.show) {
      this.initView = true
      this.initAxis()
    }
  },
  methods: {
    getDataList(params) {
      this.postParams = params
      if (!this.show) {
        return
      }
      this.loading = true
      let request = {
        customer: biCustomerUserCycleAPI,
        product: biCustomerProductCycleAPI,
        address: biCustomerAddressCycleAPI
      }[this.type]
      request(params)
        .then(res => {
          this.loading = false
          if(this.type != 'customer'){
            this.list = res.data
          }
          let axisList = res.data
          let cycleData = []
          let dealData = []
          let xAxis = []
          for (let index = 0; index < axisList.length; index++) {
            const element = axisList[index]
            cycleData.push(element.cycle)
            dealData.push(element.customerNum)
            if (this.type == 'customer') {
              xAxis.push(element.type)
            } else if (this.type == 'address') {
              xAxis.push(element.type)
            } else if (this.type == 'product') {
              xAxis.push(element.productName)
            }
          }
          this.axisOption.xAxis[0].data = xAxis
          this.axisOption.series[0].data = cycleData
          this.axisOption.series[1].data = dealData
          this.axisChart.setOption(this.axisOption, true)
        })
        .catch(() => {
          this.loading = false
        })
      if(this.type == 'customer'){
        employeeCycleInfo(params)
          .then(res => {
            this.loading = false

            this.list = res.data
          })
          .catch(() => {
            this.loading = false
          })
      }


    },
    /** 柱状图 */
    initAxis() {
      let axisChart = echarts.init(
        document.getElementById('axismain' + this.type)
      )

      let option = {
        color: ['#6ca2ff', '#ff7474'],
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            // 坐标轴指示器，坐标轴触发有效
            type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
          }
        },
        legend: {
          data: ['成交周期', '成交客户数'],
          bottom: '0px',
          itemWidth: 14
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
            name: '成交周期',
            axisTick: {
              alignWithLabel: true,
              lineStyle: { width: 0 }
            },
            axisLabel: {
              color: '#BDBDBD',
              formatter: '{value}天'
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
            name: '成交客户数',
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
            name: '成交周期',
            type: 'bar',
            yAxisIndex: 0,
            barWidth: 15,
            data: []
          },
          {
            name: '成交客户数',
            type: 'bar',
            yAxisIndex: 1,
            barWidth: 15,
            data: []
          }
        ]
      }

      axisChart.setOption(option, true)
      this.axisOption = option
      this.axisChart = axisChart
    },
    getFieldList() {
      return {
        customer: [
          { field: 'realname', name: '姓名' },
          { field: 'cycle', name: '成交周期（天）' },
          { field: 'customerNum', name: '成交客户数' }
        ],
        product: [
          { field: 'productName', name: '产品名称' },
          { field: 'cycle', name: '成交周期（天）' },
          { field: 'customerNum', name: '成交客户数' }
        ],
        address: [
          { field: 'type', name: '地区' },
          { field: 'cycle', name: '成交周期（天）' },
          { field: 'customerNum', name: '成交客户数' }
        ]
      }[this.type]
    }
  }
}
</script>
<style lang="scss" scoped>
@import '../../styles/detail.scss';
.filtrate-bar {
  position: absolute;
  z-index: 2;
  left: 0;
  right: 0;
  top: 0;
}

.content {
  padding-top: 54px;
  height: 100%;
}

.cycle-content {
  height: 100%;
  overflow-y: auto;
}
</style>
