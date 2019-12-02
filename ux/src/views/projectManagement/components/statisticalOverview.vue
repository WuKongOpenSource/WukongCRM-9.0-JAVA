<template>
  <create-sections title="任务总览">
    <flexbox class="content">
      <div class="content-progress">
        <radial-progress-bar
          :diameter="120"
          :completed-steps="parseFloat(data.completionRate) || 0"
          :total-steps="100"
          :stroke-width="7"
          inner-stroke-color="#E7F2FA"
          start-color="#0067E5"
          stop-color="#0067E5"
          class="progress">
          <p class="progress-title">完成率</p>
          <p class="progress-value">{{ data.completionRate || 0 }}<span>%</span></p>
        </radial-progress-bar>

        <radial-progress-bar
          :diameter="120"
          :completed-steps="parseFloat(data.overdueRate) || 0"
          :total-steps="100"
          :stroke-width="7"
          inner-stroke-color="#E8F2FA"
          start-color="#FF5D60"
          stop-color="#FF5D60"
          class="progress">
          <p class="progress-title">逾期率</p>
          <p class="progress-value">{{ data.overdueRate || 0 }}<span>%</span></p>
        </radial-progress-bar>
      </div>
      <div class="content-bar">
        <div id="barmain"/>
      </div>
      <div
        v-if="list && list.length > 0"
        class="content-user">
        <div class="content-user-items">
          <div
            v-for="(item, index) in showList"
            :key="index"
            class="main-user">
            <div
              v-photo="item"
              v-lazy:background-image="$options.filters.filterUserLazyImg(item.img)"
              :key="item.img"
              class="div-photo main-user-head"/>
            <div class="main-user-name">{{ item.realname }}</div>
          </div>
          <el-tooltip
            v-if="list.length > 3"
            placement="top"
            effect="light"
            popper-class="tooltip-change-border task-tooltip">
            <div
              slot="content"
              class="tooltip-content"
              style="margin: 10px 10px 10px 0;">
              <div
                v-for="(user, userIndex) in list"
                :key="userIndex"
                class="item-label"
                style="display: inline-block; margin-right: 10px;">
                <span
                  v-if="userIndex > 2"
                  class="k-name">{{ user.realname }}</span>
              </div>
            </div>
            <span class="main-user-more">
              <i>...</i>
            </span>
          </el-tooltip>
        </div>
        <div class="content-user-title">项目负责人</div>
      </div>
    </flexbox>
    <slot/>
  </create-sections>
</template>
<script type="text/javascript">
import CreateSections from '@/components/CreateSections'
import echarts from 'echarts'
import RadialProgressBar from 'vue-radial-progress'

export default {
  name: 'StatisticalOverview', // 任务总阅

  components: {
    CreateSections,
    RadialProgressBar
  },

  props: {
    data: {
      type: Object,
      default: () => {
        return {
          allCount: 0,
          archive: 0,
          completionRate: 0,
          overdueRate: 0,
          complete: 0,
          overdue: 0,
          unfinished: 0
        }
      }
    },
    list: Array
  },

  data() {
    return {
      barOption: null,
      barChart: null
    }
  },

  computed: {
    showList() {
      if (this.list && this.list.length > 3) {
        return this.list.slice(0, 3)
      }
      return this.list || []
    }
  },

  watch: {
    data() {
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
      this.barOption.series[0].data = [
        this.data.allCount || 0,
        this.data.unfinished || 0,
        this.data.complete || 0,
        this.data.overdue || 0,
        this.data.archive || 0
      ]
      this.barChart.setOption(this.barOption, true)
    },

    /**
     * 柱状图
     */
    initBar() {
      this.barChart = echarts.init(document.getElementById('barmain'))

      this.barOption = {
        tooltip: {
          show: false
        },
        legend: {
          show: false
        },
        grid: {
          top: '15px',
          left: 0,
          right: 0,
          bottom: '10px',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: ['总任务', '未完成', '已完成', '已逾期', '已归档'],
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
        },
        yAxis: {
          show: false
        },
        series: [
          {
            name: '成交客户数',
            type: 'bar',
            barWidth: 10,
            label: {
              show: true,
              position: 'top',
              color: '#333'
            },
            itemStyle: {
              barBorderRadius: [7.5, 7.5, 0, 0]
            },
            color: function(params) {
              return ['#0067E5', '#0067E5', '#0067E5', '#FF5D60', '#19DBC1'][
                params.dataIndex
              ]
            },
            data: []
          }
        ]
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.content {
  position: relative;
  padding: 10px 60px;
  .content-progress {
    flex: 1.4;
    text-align: center;
  }

  .content-bar {
    flex: 2;
    text-align: center;
    #barmain {
      display: inline-block;
      width: 400px;
      height: 150px;
    }
  }

  .content-user {
    text-align: center;
    flex: 1;
    .content-user-title {
      margin-top: 8px;
    }
  }
}

.main-user {
  display: inline-block;
  .main-user-head {
    width: 36px;
    height: 36px;
    border-radius: 18px;
    margin-bottom: 5px;
  }
  .main-user-name {
    font-size: 13px;
    color: #333;
  }

  .main-user-name + .main-user-name {
    margin-top: 5px;
  }
}

.main-user + .main-user {
  margin-left: 8px;
}

.progress {
  display: inline-block;
  .progress-title {
    font-size: 12px;
    color: #666;
    margin-bottom: 7px;
  }

  .progress-value {
    font-size: 27px;
    color: #333;
    font-weight: 700;
    span {
      font-size: 12px;
    }
  }
}

.progress:first-child {
  margin-right: 10%;
}

// 更多
.main-user-more {
  background: #eee;
  width: 35px;
  height: 20px;
  text-align: center;
  display: inline-block;
  font-weight: 700;
  border-radius: 3px;
  vertical-align: top;
  margin-top: 20px;
  margin-left: 8px;
  position: relative;
  i {
    position: absolute;
    left: 0;
    right: 0;
    top: -1px;
  }
}

.item-label {
  display: inline-block;
}
.k-name {
  display: inline-block;
  height: 20px;
  line-height: 20px;
  padding: 0 10px;
  border-radius: 3px;
  color: #fff;
  margin-right: 6px;
  font-size: 12px;
  background-color: $xr-color-primary;
}
</style>
