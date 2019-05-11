<template>
  <div v-loading="loading"
       class="main-container">
    <div class="handle-bar">
      <el-date-picker v-model="dateSelect"
                      type="daterange"
                      :clearable="false"
                      range-separator="-"
                      format="yyyy-MM-dd"
                      value-format="yyyy-MM-dd"
                      :picker-options="pickerOptions"
                      start-placeholder="开始日期"
                      end-placeholder="结束日期">
      </el-date-picker>
      <el-select v-model="structuresSelectValue"
                 @change="structuresValueChange"
                 placeholder="选择部门">
        <el-option v-for="item in deptList"
                   :key="item.id"
                   :label="item.name"
                   :value="item.id">
        </el-option>
      </el-select>
      <el-select v-model="userSelectValue"
                 :clearable="true"
                 placeholder="选择员工">
        <el-option v-for="item in userOptions"
                   :key="item.userId"
                   :label="item.realname"
                   :value="item.userId">
        </el-option>
      </el-select>
      <el-select v-model="businessStatusValue"
                 placeholder="商机组">
        <el-option v-for="item in businessOptions"
                   :key="item.typeId"
                   :label="item.name"
                   :value="item.typeId">
        </el-option>
      </el-select>
      <el-button @click.native="handleClick('search')"
                 type="primary">搜索</el-button>
    </div>
    <div class="content">
      <flexbox>
        <flexbox-item>
          <div id="funnelmain"
               class="funnel"></div>
        </flexbox-item>
        <flexbox-item>
          <el-table :data="list"
                    height="300"
                    stripe
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
        </flexbox-item>
      </flexbox>

    </div>

  </div>
</template>

<script>
import echarts from 'echarts'
import { adminStructuresSubIndex, usersList } from '@/api/common'
import { formatTimeToTimestamp } from '@/utils'
import { crmBusinessStatusList } from '@/api/customermanagement/business'
import { biBusinessFunnel } from '@/api/businessIntelligence/bi'

import moment from 'moment'

export default {
  /** 销售漏斗 */
  name: 'funnel-statistics',
  components: {},
  data() {
    return {
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now()
        }
      },
      loading: false,

      dateSelect: [], // 选择的date区间
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
      /** 商机状态 */
      businessOptions: [],
      businessStatusValue: '',

      list: [],
      fieldList: [
        { field: 'name', name: '阶段' },
        { field: 'totalPrice', name: '金额' },
        { field: 'businessNum', name: '商机数' }
      ],

      funnelChart: null, // 漏斗图
      funnelOption: null
    }
  },
  computed: {},
  mounted() {
    this.getDeptList()

    var endDate = moment(new Date()).format('YYYY-MM-DD')
    var times = endDate.split('-')
    var startDate = times[0] + '-' + times[1] + '-01'
    this.dateSelect = [startDate, endDate]

    this.list = []
    this.initFunnel()
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
            this.getUserList() // 更新员工列表
            this.getBusinessStatusList() // 获取商机状态信息 然后请求数据
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
      let params = { pageType: 0 }
      params.deptId = this.structuresSelectValue
      usersList(params)
        .then(res => {
          this.userOptions = res.data
        })
        .catch(() => {})
    },
    /** 商机阶段 */
    getBusinessStatusList() {
      this.loading = true
      crmBusinessStatusList({})
        .then(res => {
          this.loading = false
          this.businessOptions = res.data
          if (res.data.length > 0) {
            this.businessStatusValue = res.data[0].typeId
            this.getData()
          }
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 顶部操作 */
    handleClick(type) {
      if (type === 'search') {
        this.getData()
      }
    },
    getData() {
      this.loading = true
      biBusinessFunnel({
        startTime: this.dateSelect[0],
        endTime: this.dateSelect[1],
        deptId: this.structuresSelectValue,
        userId: this.userSelectValue,
        typeId: this.businessStatusValue
      })
        .then(res => {
          this.loading = false
          this.list = res.data
          var data = []
          let sumMoney = 0
          for (let index = 0; index < res.data.length; index++) {
            const element = res.data[index]
            data.push({
              name: (element.name || '') + '(' + element.businessNum + ')',
              value: parseFloat(element.totalPrice)
            })
            sumMoney += parseFloat(element.totalPrice)
          }

          this.funnelOption.series[0].data = data
          this.funnelOption.series[0].max =
            sumMoney < 1 ? 1 : sumMoney
          this.funnelChart.setOption(this.funnelOption, true)
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 销售漏斗 */
    initFunnel() {
      var funnelChart = echarts.init(
        document.getElementById('funnelmain')
      )
      var option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b} <br/> 预测金额: {c}元'
        },
        toolbox: {
          feature: {
            saveAsImage: {}
          }
        },
        calculable: true,
        grid: {
          left: 0,
          right: 0,
          bottom: 0,
          top: 0
        },
        series: [
          {
            name: '漏斗图',
            type: 'funnel',
            left: '20%',
            width: '56%',
            /** 数据排序 */
            sort: 'none',
            /** 数据图形间距。 */
            gap: 2,

            label: {
              normal: {
                show: true,
                position: 'right'
              },
              emphasis: {
                textStyle: {
                  fontSize: 20
                }
              }
            },
            labelLine: {
              normal: {
                length: 20,
                lineStyle: {
                  width: 1,
                  type: 'solid'
                }
              }
            },
            itemStyle: {
              /** 传入的是数据项 seriesIndex, dataIndex, data, value 等各个参数 */
              color: data => {
                return [
                  '#6CA2FF',
                  '#6AC9D7',
                  '#72DCA2',
                  '#DBB375',
                  '#E164F7',
                  '#FF7474',
                  '#FFB270',
                  '#FECD51'
                ][data.dataIndex % 8]
              }
            },
            data: []
          }
        ]
      }

      funnelChart.setOption(option, true)
      this.funnelOption = option
      this.funnelChart = funnelChart
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import './styles/detail.scss';

.handle-bar {
  padding: 15px 20px 5px 20px;
  .el-date-editor {
    padding: 0px 10px;
    width: 240px;
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

.content {
  padding: 20px 20px;
}
.content /deep/ .el-table {
  border: 1px solid #e6e6e6;
  width: 300px;
}
/** 漏斗 */
.funnel {
  width: 420px;
  height: 400px;
}
</style>
