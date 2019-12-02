<template>
  <div style="margin-bottom:20px;min-width: 800px;">
    <flexbox
      :gutter="0"
      wrap="wrap">
      <flexbox-item
        v-loading="jianbaoLoading"
        :span="1/2"
        style="padding-right:10px;padding-bottom:20px;">
        <div
          class="card"
          style="height: 350px;">
          <flexbox class="mark-header"><img
            class="img-mark"
            src="@/assets/img/jianbao.png" >销售简报</flexbox>
          <flexbox
            :gutter="0"
            wrap="wrap">
            <flexbox-item
              v-for="(item, index) in jianbaoItems"
              :span="1/2"
              :key="index"
              class="jianbao-icon-container"
              @click.native="reportClick(item)">
              <flexbox
                :style="{cursor: item.type ? 'pointer':'auto'}"
                class="jianbao-icon-content">
                <img
                  :src="item.icon"
                  class="jianbao-icon" >
                <div class="jianbao-title">{{ item.title }}</div>
                <div class="jianbao-value">{{ item.value }}</div>
              </flexbox>
            </flexbox-item>
          </flexbox>

        </div>
      </flexbox-item>
      <flexbox-item
        v-loading="gaugeLoading"
        :span="1/2"
        style="padding-left:10px;padding-bottom:20px;">
        <div
          class="card"
          style="height: 350px;">
          <flexbox class="mark-header"><img
            class="img-mark"
            src="@/assets/img/zhibiao.png" >业绩指标</flexbox>
          <flexbox class="gaugeSelect">
            <el-select
              v-model="gaugeSelectValue"
              style="display: block;width: 100px;"
              placeholder="请选择"
              @change="getCrmIndexAchievementData">
              <el-option
                v-for="item in gaugeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"/>
            </el-select>
          </flexbox>
          <div
            id="gaugemain"
            class="gauge"/>
          <flexbox
            justify="center"
            class="target-items">
            <div class="target-item">
              <div class="name">目标</div>
              <div class="value">{{ gaugeData.achievementMoneys }}元</div>
            </div>
            <div class="target-item">
              <div class="name">合同金额</div>
              <div class="value">{{ gaugeData.contractMoneys }}元</div>
            </div>
            <div class="target-item">
              <div class="name">回款金额</div>
              <div class="value">{{ gaugeData.receivablesMoneys }}元</div>
            </div>
          </flexbox>
        </div>
      </flexbox-item>
      <flexbox-item
        v-loading="funnelLoading"
        :span="1/2"
        style="padding-right:10px;padding-bottom:20px;">
        <div
          class="card"
          style="height: 400px;">
          <flexbox class="mark-header"><img
            class="img-mark"
            src="@/assets/img/loudou.png" >销售漏斗
          </flexbox>
          <flexbox class="funnelSelect">
            <el-select
              v-model="businessStatusValue"
              placeholder="商机组"
              @change="getBusinessStatustatistics">
              <el-option
                v-for="item in businessOptions"
                :key="item.typeId"
                :label="item.name"
                :value="item.typeId"/>
            </el-select>
          </flexbox>
          <div
            id="funnelmain"
            class="funnel"/>
          <div class="funnel-label">
            <div>赢单：{{ funnelData.winSingle }}元</div>
            <div>输单：{{ funnelData.loseSingle }}元</div>
          </div>
        </div>
      </flexbox-item>
      <flexbox-item
        v-loading="trendLoading"
        :span="1/2"
        style="padding-left:10px;padding-bottom:20px;">
        <div
          class="card"
          style="height: 400px;">
          <flexbox class="mark-header"><img
            class="img-mark"
            src="@/assets/img/qushi.png" >销售趋势</flexbox>
          <flexbox style="position: relative;">
            <div class="trend-target-item">
              <div class="name">合同金额</div>
              <div class="value">{{ trendData.totlaContractMoney }}元</div>
            </div>
            <div class="trend-target-item">
              <div class="name">回款金额</div>
              <div class="value">{{ trendData.totlaReceivablesMoney }}元</div>
            </div>
          </flexbox>
          <div class="trend-label">
            <flexbox class="label-item">
              <div
                class="label-item-mark"
                style="background-color: #6ca2ff"/>
              <div class="label-item-name">合同金额</div>
            </flexbox>
            <flexbox
              class="label-item"
              style="margin-top:5px;">
              <div
                class="label-item-mark"
                style="background-color: #ff7474"/>
              <div class="label-item-name">回款金额</div>
            </flexbox>
          </div>
          <div class="axismain-content">
            <div id="axismain"/>
          </div>
        </div>
      </flexbox-item>
    </flexbox>

    <!-- 销售简报列表 -->
    <report-list
      v-if="showReportList"
      :title="reportData.title"
      :placeholder="reportData.placeholder"
      :crm-type="reportData.crmType"
      :request="reportData.request"
      :params="reportData.params"
      :field-list="fieldReportList"
      @hide="showReportList = false"/>
  </div>
</template>

<script>
import echarts from 'echarts'
import {
  crmIndexIndex,
  crmIndexAchievementData,
  crmIndexFunnel,
  crmIndexSaletrend,
  crmIndexIndexListAPI,
  crmQueryRecordConuntAPI
} from '@/api/customermanagement/workbench'
import { crmBusinessStatusList } from '@/api/customermanagement/business'
import ReportList from './reportList'

export default {
  /** 客户管理下的工作台-仪表盘 */
  name: 'CustomerDash',
  components: {
    ReportList
  },
  props: {
    // 获取数据的员工 部门条件
    data: Object
  },
  data() {
    return {
      /** 销售简报 */
      jianbaoLoading: false,
      jianbaoItems: [
        {
          title: '新增客户',
          icon: require('@/assets/img/c_curomer.png'),
          field: 'customerCount',
          type: 'customer',
          label: 2, // 0为变化的商机 2为客户 3为联系人 5为商机 6为合同 7为回款
          value: 0
        },
        {
          title: '新增联系人',
          icon: require('@/assets/img/c_contact.png'),
          field: 'contactsCount',
          type: 'contacts',
          label: 3,
          value: 0
        },
        {
          title: '新增商机',
          icon: require('@/assets/img/c_business.png'),
          field: 'businessCount',
          type: 'business',
          label: 5,
          value: 0
        },
        {
          title: '阶段变化的商机',
          icon: require('@/assets/img/jd_business.png'),
          field: 'recordStatusCount',
          type: 'business_status',
          label: 0,
          value: 0
        },
        {
          title: '新增合同',
          icon: require('@/assets/img/c_contract.png'),
          field: 'contractCount',
          type: 'contract',
          label: 6,
          value: 0
        },
        {
          title: '新增跟进记录',
          icon: require('@/assets/img/c_log.png'),
          field: 'recordCount',
          type: 'record',
          value: 0
        },
        {
          title: '新增回款',
          icon: require('@/assets/img/c_receivables.png'),
          field: 'receivablesCount',
          type: 'receivables',
          label: 7,
          value: 0
        }
      ],
      showReportList: false,
      fieldReportList: null,
      reportData: {
        title: '',
        placeholder: '',
        crmType: '',
        request: null,
        params: null
      },
      /** 业绩指标 */
      gaugeLoading: false,
      gaugeSelectValue: 2,
      gaugeData: { contractMoney: 0, receivablesMoney: 0, achievementMoney: 0 },
      /** 销售漏斗 */
      /** 商机状态 */
      funnelLoading: false,
      businessOptions: [],
      businessStatusValue: '',
      funnelData: { winSingle: 0, loseSingle: 0 },
      /** 销售趋势 */
      trendLoading: false,
      trendData: { contractTotal: 0, receivablesTotal: 0 },
      gaugeChart: null, // 指标图
      gaugeOption: null,
      funnelChart: null, // 漏斗图
      funnelOption: null,
      axisChart: null, // 柱状图
      axisOption: null
    }
  },
  computed: {
    /** 简报信息 */
    gaugeOptions() {
      return [{ label: '回款金额', value: 2 }, { label: '合同金额', value: 1 }]
    }
  },
  watch: {
    data: function(val) {
      this.getCrmIndexIndex()
      this.getBusinessStatusList()
      this.getCrmIndexAchievementData()
      this.getCrmIndexSaletrend()
    }
  },
  mounted() {
    this.initGauge()
    this.initFunnel()
    this.initAxis()

    if (this.data.users.length > 0 || this.data.users.strucs > 0) {
      this.getCrmIndexIndex()
      this.getBusinessStatusList()
      this.getCrmIndexAchievementData()
      this.getCrmIndexSaletrend()
    }
  },
  methods: {
    // 销售简报
    getCrmIndexIndex() {
      this.jianbaoLoading = true
      var params = this.getBaseParams()
      crmIndexIndex(params)
        .then(res => {
          for (let index = 0; index < this.jianbaoItems.length; index++) {
            const element = this.jianbaoItems[index]
            element.value = res.data[element.field]
              ? res.data[element.field]
              : 0
          }
          this.jianbaoLoading = false
        })
        .catch(() => {
          this.jianbaoLoading = false
        })
    },

    /**
     * 销售简报查看
     */
    reportClick(item) {
      if (item.type) {
        this.reportData.title = `销售简报-${item.title}`
        this.reportData.placeholder = {
          customer: '请输入客户名称/手机/电话',
          contacts: '请输入联系人姓名/手机/电话',
          business: '请输入商机名称',
          business_status: '请输入商机名称',
          contract: '请输入合同名称',
          receivables: '请输入回款编号',
          record: ''
        }[item.type]
        if (item.type == 'record') {
          this.fieldReportList = [
            {
              label: '模块',
              prop: 'crmType',
              width: 300
            },
            {
              label: '新增跟进记录数',
              prop: 'count'
            }
          ]
          this.reportData.request = crmQueryRecordConuntAPI
        } else {
          this.fieldReportList = null
          this.reportData.request = crmIndexIndexListAPI
        }
        this.reportData.crmType = item.type
        this.reportData.params = this.getBaseParams()
        this.reportData.params.label = item.label
        this.showReportList = true
      }
    },

    getBaseParams() {
      const params = {
        userIds: this.data.users
          .map(function(item, index, array) {
            return item.userId
          })
          .join(','),
        deptIds: this.data.strucs
          .map(function(item, index, array) {
            return item.id
          })
          .join(',')
      }

      if (this.data.timeTypeValue.type) {
        if (this.data.timeTypeValue.type == 'custom') {
          params.startTime = this.data.timeTypeValue.startTime
          params.endTime = this.data.timeTypeValue.endTime
        } else {
          params.type = this.data.timeTypeValue.value || ''
        }
      }
      return params
    },
    /** 指标图 */
    // 销售简报
    getCrmIndexAchievementData() {
      this.gaugeLoading = true
      var params = this.getBaseParams()
      params.status = this.gaugeSelectValue
      crmIndexAchievementData(params)
        .then(res => {
          if (res.data) {
            this.gaugeData = res.data
            this.gaugeOption.series[0].data[0].value =
              res.data.proportion || '0'
            this.gaugeChart.setOption(this.gaugeOption, true)
          }
          this.gaugeLoading = false
        })
        .catch(() => {
          this.gaugeLoading = false
        })
    },
    initGauge() {
      var gaugeChart = echarts.init(document.getElementById('gaugemain'))
      // 指定图表的配置项和数据
      var option = {
        tooltip: {
          formatter: '{a} <br/>{b} : {c}%'
        },
        series: [
          {
            type: 'gauge',
            detail: {
              formatter: '{value}%',
              fontSize: 20,
              fontWeight: 'bold',
              color: '#5F879D'
            },
            data: [{ value: 0, name: '完成率' }],
            axisLine: {
              lineStyle: {
                width: 15,
                color: [[0.2, '#FF7474'], [0.8, '#FECD51'], [1, '#48E78D']]
              }
            },
            splitLine: {
              length: 13,
              lineStyle: { width: 0 }
            },
            /** 刻度样式 */
            axisTick: { show: false },
            /** 刻度标签 */
            axisLabel: { fontSize: 9 },
            /** 仪表盘指针 */
            pointer: {
              length: '70%',
              width: 4
            },
            /** 仪表盘指针颜色 */
            itemStyle: { color: '#5F879D' },
            title: { fontSize: 13, color: '#666' }
          }
        ]
      }
      // 使用刚指定的配置项和数据显示图表。
      gaugeChart.setOption(option, true)
      this.gaugeOption = option
      this.gaugeChart = gaugeChart
    },
    /** 销售漏斗 */
    /** 商机阶段 */
    getBusinessStatusList() {
      this.funnelLoading = true
      crmBusinessStatusList({})
        .then(res => {
          this.funnelLoading = false
          this.businessOptions = res.data
          if (res.data.length > 0) {
            this.businessStatusValue = res.data[0].typeId
            if (this.businessStatusValue) {
              this.getBusinessStatustatistics()
            }
          }
        })
        .catch(() => {
          this.funnelLoading = false
        })
    },
    getBusinessStatustatistics() {
      this.funnelLoading = true
      var params = this.getBaseParams()
      params.typeId = this.businessStatusValue
      crmIndexFunnel(params)
        .then(res => {
          this.funnelLoading = false
          var data = []
          let sumMoney = 0
          for (let index = 0; index < res.data.list.length; index++) {
            const element = res.data.list[index]
            data.push({
              name: (element.name || '') + '(' + element.count + ')',
              value: element.money
            })
            sumMoney += parseFloat(element.money || 0)
          }
          this.funnelData = {
            winSingle: res.data.sumYing,
            loseSingle: res.data.sumShu
          }
          this.funnelOption.series[0].data = data
          this.funnelOption.series[0].max = sumMoney < 1 ? 1 : sumMoney
          this.funnelChart.setOption(this.funnelOption, true)
        })
        .catch(() => {
          this.funnelLoading = false
        })
    },
    initFunnel() {
      var funnelChart = echarts.init(document.getElementById('funnelmain'))
      var option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b} <br/> 预测金额: {c}元'
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
            type: 'funnel',
            left: '35%',
            width: '45%',
            /** 数据排序 */
            sort: 'none',
            /** 数据图形间距。 */
            gap: 2,
            min: 0,
            max: 100,
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
    },
    /** 柱状图 */
    getCrmIndexSaletrend() {
      this.trendLoading = true
      var params = this.getBaseParams()
      crmIndexSaletrend(params)
        .then(res => {
          this.trendData = {
            totlaContractMoney: res.data.totlaContractMoney,
            totlaReceivablesMoney: res.data.totlaReceivablesMoney
          }
          const list = res.data.list || []
          const contractList = []
          const receivablesList = []
          const xAxisData = []
          for (let index = 0; index < list.length; index++) {
            const element = list[index]
            contractList.push(element.contractMoneys)
            receivablesList.push(element.receivablesMoneys)
            xAxisData.push(element.type)
          }

          this.axisOption.xAxis[0].data = xAxisData
          this.axisOption.series[0].data = contractList
          this.axisOption.series[1].data = receivablesList

          this.axisChart.setOption(this.axisOption, true)
          this.trendLoading = false
        })
        .catch(() => {
          this.trendLoading = false
        })
    },
    getTrendAxisInfo() {
      const contractList = []
      const receivablesList = []
      if (this.trendSelectValue == '月') {
        const keys = [
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
        ]
        this.axisOption.xAxis[0].data = keys
        for (const key in keys) {
          const element = this.trendData[keys[key]]
          contractList.push(element.contractMoneys)
          receivablesList.push(element.receivablesMoneys)
        }
      } else {
        const keys = ['一季度', '二季度', '三季度', '四季度']
        this.axisOption.xAxis[0].data = keys
        for (const key in keys) {
          const element = this.trendData[keys[key]]
          contractList.push(element.contractMoneys)
          receivablesList.push(element.receivablesMoneys)
        }
      }
      this.axisOption.series[0].data = contractList
      this.axisOption.series[1].data = receivablesList
      this.axisChart.setOption(this.axisOption, true)
    },
    initAxis() {
      var axisChart = echarts.init(document.getElementById('axismain'))

      var option = {
        color: ['#6ca2ff', '#ff7474'],
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            // 坐标轴指示器，坐标轴触发有效
            type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
          }
        },
        grid: {
          top: '5px',
          left: '20px',
          right: '20px',
          bottom: '20px',
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
        series: [
          {
            name: '合同金额',
            type: 'bar',
            stack: 'one',
            barWidth: 10,
            data: []
          },
          {
            name: '回款金额',
            type: 'bar',
            stack: 'one',
            barWidth: 10,
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

<style lang="scss" scoped>
.card {
  position: relative;
  border: 1px solid #e6e6e6;
  border-radius: 2px;
  padding: 20px;
  background-color: white;
}
.img-mark {
  display: block;
  width: 15px;
  margin-right: 5px;
}

.mark-header {
  margin-bottom: 20px;
  font-size: 13px;
}
/** 简报 */

.jianbao-icon-container {
  padding: 6px;
}

.jianbao-icon-content {
  padding: 8px 8px;
  background-color: #f2f2f5;
  border-radius: 3px;
  .jianbao-icon {
    display: block;
    width: 23px;
    margin-right: 10px;
  }

  .jianbao-title {
    color: #333;
    font-size: 12px;
    margin-right: 10px;
  }
  .jianbao-value {
    color: #333;
    text-align: right;
    font-size: 13px;
    flex: 1;
  }
}

.jianbaoSelect {
  position: absolute;
  top: 15px;
  right: 20px;
  width: 80px;
}

/** 指标 */
.gauge {
  margin: 0 auto;
  width: 220px;
  height: 220px;
}
.gaugeSelect {
  position: absolute;
  top: 15px;
  right: 20px;
  width: auto;
}

.target-items {
  position: absolute;
  left: 0;
  bottom: 0;
}

.target-item {
  padding: 20px 25px;
  min-width: 100px;
  .name {
    font-size: 12px;
    color: #666666;
  }
  .value {
    margin-top: 15px;
    font-size: 14px;
    color: #333;
  }
}

/** 销售漏斗 */
.funnelSelect {
  position: absolute;
  top: 15px;
  right: 20px;
  width: auto;
  .el-date-editor {
    padding: 0px 10px;
    width: 240px;
    margin-right: 15px;
  }
  .el-select {
    width: 120px;
  }
}

.funnel {
  position: absolute;
  top: 40px;
  left: 20px;
  width: 480px;
  height: 350px;
  margin: 0 auto;
  z-index: 0;
}
.funnel-label {
  position: absolute;
  top: 180px;
  left: 70px;
  font-size: 13px;
  div:nth-child(1) {
    color: #6ca2ff;
  }
  div:nth-child(2) {
    margin-top: 10px;
    color: #ff7474;
  }
}
/** 销售趋势 */
.trend-target-item {
  padding: 10px 25px 25px 25px;
  .name {
    font-size: 12px;
    color: #666666;
  }
  .value {
    margin-top: 10px;
    font-size: 18px;
    color: #333;
  }
}

.trend-label {
  position: absolute;
  z-index: 1;
  right: 30px;
  top: 80px;
  font-size: 12px;
  .label-item {
    .label-item-mark {
      width: 10px;
      height: 10px;
      margin-right: 10px;
    }
  }
}

.axismain-content {
  padding: 0 10px;
  #axismain {
    width: 100%;
    height: 250px;
  }
}
</style>

