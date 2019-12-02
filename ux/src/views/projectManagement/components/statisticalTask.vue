<template>
  <create-sections :title="title">
    <div
      :id="'barmain' + id "
      class="barmain"/>
  </create-sections>
</template>
<script type="text/javascript">
import CreateSections from '@/components/CreateSections'
import echarts from 'echarts'

export default {
  name: 'StatisticalTask', // 柱状图

  components: {
    CreateSections
  },

  props: {
    title: {
      type: String,
      default: ''
    },
    type: String, // task label
    list: {
      type: Array,
      default: () => {
        return []
      }
    }
  },

  data() {
    return {
      barOption: null,
      barChart: null,

      id: Math.ceil(Math.random() * 100)
    }
  },

  computed: {},

  watch: {
    list() {
      this.changeBarData()
    }
  },

  mounted() {
    this.initBar()
    this.changeBarData()
  },

  methods: {
    /**
     * 修改bar数据
     */
    changeBarData() {
      const xAxis = []
      const undoneList = []
      const doneList = []

      for (let index = 0; index < this.list.length; index++) {
        const item = this.list[index]

        if (this.type == 'task') {
          xAxis.push(item.className)
          undoneList.push(item.undone)
          doneList.push(item.complete)
        } else if (this.type == 'label') {
          xAxis.push(item.name)
          undoneList.push(item.undone)
          doneList.push(item.complete)
        }
      }
      this.barOption.xAxis[0].data = xAxis
      this.barOption.series[0].data = doneList
      this.barOption.series[1].data = undoneList
      this.barChart.setOption(this.barOption, true)
    },

    /**
     * 柱状图
     */
    initBar() {
      this.barChart = echarts.init(document.getElementById('barmain' + this.id))
      this.barOption = {
        color: ['#6ca2ff', '#ff7474'],
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            // 坐标轴指示器，坐标轴触发有效
            type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
          }
        },
        legend: {
          data: ['已完成', '未完成'],
          bottom: '0px',
          itemWidth: 14
        },
        grid: {
          top: '20px',
          left: '20px',
          right: '20px',
          bottom: '30px',
          containLabel: true
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
              color: '#666'
            },
            /** 坐标轴轴线相关设置 */
            axisLine: {
              lineStyle: { color: '#ECECEC' }
            },
            splitLine: {
              show: false
            }
          }
        ],
        yAxis: {
          splitNumber: 3,
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          },
          splitLine: {
            lineStyle: { color: '#ECECEC' }
          },
          axisLabel: {
            textStyle: {
              color: '#666'
            }
          }
        },
        series: [
          {
            name: '已完成',
            type: 'bar',
            stack: 'one',
            barWidth: '15%',
            data: []
          },
          {
            name: '未完成',
            type: 'bar',
            stack: 'one',
            barWidth: '15%',
            data: []
          }
        ]
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.barmain {
  height: 150px;
  margin-bottom: 10px;
}
</style>
