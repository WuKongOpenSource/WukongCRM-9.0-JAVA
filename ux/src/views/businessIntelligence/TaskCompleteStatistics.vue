<template>
  <div
    v-loading="loading"
    class="main-container">
    <div class="handle-bar">
      <el-date-picker
        v-model="dateSelect"
        :clearable="false"
        :picker-options="pickerOptions"
        type="year"
        value-format="yyyy"
        placeholder="选择年"/>
      <el-select
        v-model="typeSelect"
        placeholder="请选择">
        <el-option
          v-for="item in [{label:'合同金额', value:1},{label:'回款金额', value:2}]"
          :key="item.value"
          :label="item.label"
          :value="item.value"/>
      </el-select>
      <el-select
        v-model="structuresSelectValue"
        placeholder="选择部门"
        @change="structuresValueChange">
        <el-option
          v-for="item in deptList"
          :key="item.id"
          :label="item.name"
          :value="item.id"/>
      </el-select>
      <el-select
        v-model="userSelectValue"
        :clearable="true"
        placeholder="选择员工">
        <el-option
          v-for="item in userOptions"
          :key="item.userId"
          :label="item.realname"
          :value="item.userId"/>
      </el-select>
      <el-button
        type="primary"
        @click.native="handleClick('search')">搜索</el-button>
    </div>
    <div class="content">
      <div class="axis-content">
        <div id="axismain"/>
      </div>
      <div class="table-content">
        <el-table
          :data="list"
          stripe
          border
          height="400"
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
import echarts from 'echarts'
import { adminStructuresSubIndex, usersList } from '@/api/common'
import { biAchievementStatistics } from '@/api/businessIntelligence/bi'
import moment from 'moment'

export default {
  /** 业绩目标完成情况 */
  name: 'TaskCompleteStatistics',
  components: {},
  data() {
    return {
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now()
        }
      },
      loading: false,

      dateSelect: '', // 选择的date
      typeSelect: 1, // 类型选择 1销售（目标）2回款（目标）
      /** 部门选择解析数据 */
      structuresProps: {
        children: 'children',
        label: 'label',
        value: 'id'
      },
      deptList: [], // 部门列表
      structuresSelectValue: '',
      /** 用户列表 */
      userOptions: [],
      userSelectValue: '',

      list: [],
      fieldList: [
        { field: 'month', name: '时间' },
        { field: 'receivables', name: '合同金额(元)' },
        { field: 'achievement', name: '目标(元)' },
        { field: 'rate', name: '完成率(%)' }
      ],

      axisChart: null, // 柱状图
      axisOption: null
    }
  },
  computed: {},
  mounted() {
    this.dateSelect = moment(new Date())
      .year()
      .toString()
    this.getDeptList()
    this.initAxis()
  },
  methods: {
    /**
     * 获取部门列表
     */
    getDeptList() {
      this.loading = true
      adminStructuresSubIndex()
        .then(res => {
          this.deptList = res.data
          this.loading = false
          if (res.data.length > 0) {
            this.structuresSelectValue = res.data[0].id
            this.getUserList()
            this.getAhievementDatalist()
          }
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 部门更改 */
    structuresValueChange(data) {
      this.userSelectValue = ''
      this.userOptions = []
      this.getUserList() // 更新员工列表
    },
    /** 部门下员工 */
    getUserList() {
      const params = { pageType: 0 }
      params.deptId = this.structuresSelectValue
      usersList(params)
        .then(res => {
          this.userOptions = res.data
        })
        .catch(() => {})
    },
    /** 获取部门业绩完成信息 */
    getAhievementDatalist() {
      this.loading = true
      biAchievementStatistics({
        year: this.dateSelect,
        type: this.typeSelect,
        deptId: this.structuresSelectValue,
        userId: this.userSelectValue
      })
        .then(res => {
          var receivabless = []
          var achiements = []
          var rates = []
          this.list = []
          for (let index = 0; index < 12; index++) {
            const element = res.data[index]
            receivabless.push(element.receivables)
            achiements.push(element.achievement)
            rates.push(element.rate)
            this.list.push(element)
          }
          this.axisOption.series[0].data = receivabless
          this.axisOption.series[1].data = achiements
          this.axisOption.series[2].data = rates
          this.axisChart.setOption(this.axisOption, true)
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 顶部操作 */
    handleClick(type) {
      if (type === 'search') {
        this.refreshTableHeadAndChartInfo()
        this.getAhievementDatalist()
      }
    },
    /**
     * 刷新表头和图标关键字
     */
    refreshTableHeadAndChartInfo() {
      this.fieldList[1].name =
        this.typeSelect == 1 ? '合同金额(元)' : '回款金额(元)'
      this.axisOption.legend.data[0] =
        this.typeSelect == 1 ? '合同金额' : '回款金额'
      this.axisOption.series[0].name =
        this.typeSelect == 1 ? '合同金额' : '回款金额'
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
          data: ['合同金额', '目标', '完成率'],
          bottom: '0px',
          itemWidth: 14
        },
        grid: {
          top: '5px',
          left: '20px',
          right: '20px',
          bottom: '40px',
          containLabel: true,
          borderColor: '#fff'
        },
        xAxis: [
          {
            type: 'category',
            data: [
              '1月',
              '2月',
              '3月',
              '4月',
              '5月',
              '6月',
              '7月',
              '8月',
              '9月',
              '10月',
              '11月',
              '12月'
            ],
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
            name: '合同金额',
            axisTick: {
              alignWithLabel: true,
              lineStyle: { width: 0 }
            },
            axisLabel: {
              color: '#BDBDBD',
              formatter: '{value} 元'
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
            name: '完成率',
            axisTick: {
              alignWithLabel: true,
              lineStyle: { width: 0 }
            },
            position: 'right',
            axisLabel: {
              color: '#BDBDBD',
              formatter: '{value} %'
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
            name: '合同金额',
            type: 'bar',
            yAxisIndex: 0,
            barWidth: 15,
            data: []
          },
          {
            name: '目标',
            type: 'bar',
            yAxisIndex: 0,
            barWidth: 15,
            data: []
          },
          {
            name: '完成率',
            type: 'line',
            yAxisIndex: 1,
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
@import './styles/detail.scss';
.handle-bar {
  background-color: white;
  padding: 15px 20px 5px 20px;
  .el-date-editor {
    width: 130px;
    margin-right: 15px;
  }
  .el-cascader {
    width: 130px;
    margin-right: 15px;
  }
  .el-select {
    width: 120px;
    margin-right: 15px;
  }
}
</style>

