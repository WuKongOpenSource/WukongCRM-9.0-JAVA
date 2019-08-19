<template>
  <div class="task-calendars"
       v-loading="loading">
    <div class="add-btn">
      <el-button type="primary"
                 @click="createTask">
        创建任务
      </el-button>
    </div>
    <div ref="hoverDialog"
         class="hover-dialog"
         :style="{'border-color': getPriorityColor(hoverDialogList.priority)}">
      <flexbox class="title"
               align="stretch">
        <el-checkbox></el-checkbox>
        <div>{{hoverDialogList.name}}</div>
      </flexbox>
      <div class="img-content">
        <span>{{hoverDialogList.startTime + ' - ' + hoverDialogList.stopTime}}</span>
      </div>
    </div>
    <!-- 新建任务弹出框 newDialog-->
    <new-dialog :visible="taskCreateShow"
                :action="createActionInfo"
                @handleClose="handleClose"
                @submit="refetchCalendar">
    </new-dialog>
    <!-- 详情 -->
    <particulars v-if="taskDetailShow"
                 ref="particulars"
                 :id="taskID"
                 :detailIndex="detailIndex"
                 @on-handle="refetchCalendar"
                 @close="closeBtn">
    </particulars>
    <!-- 日历 -->
    <div id='calendar'></div>
  </div>
</template>


<script>
import $ from 'jquery'
import fullcalendar from 'fullcalendar'
import 'fullcalendar/dist/locale/zh-cn.js'

import newDialog from '../components/newDialog'
import particulars from '../components/particulars'
// API
import { workTaskDateListAPI } from '@/api/projectManagement/calendars'

export default {
  components: {
    newDialog,
    particulars
  },

  data() {
    return {
      fcEvents: [],
      // 新建任务
      createActionInfo: {
        type: 'create'
      },
      taskCreateShow: false,
      // 详情
      taskDetailShow: false,
      hoverDialogList: {},
      loading: true,
      // 详情数据
      taskID: '',
      detailIndex: -1,
      taskDetailShow: false
    }
  },

  created() {
    $(() => {
      this.listFun()
    })
  },

  mounted() {
    document
      .getElementById('project-main-container')
      .addEventListener('click', this.taskShowHandle, false)
  },

  methods: {
    /**
     * 初始化日历任务
     */
    listFun() {
      $('#calendar').fullCalendar({
        height: document.documentElement.clientHeight - 101,
        nextDayThreshold: '00:00:00',

        dayClick: (date, jsEvent, view) => {
          this.createActionInfo = {
            type: 'create',
            data: {
              startTime: date.format('YYYY-MM-DD'),
              stopTime: date.format('YYYY-MM-DD')
            }
          }
          this.taskCreateShow = true
        },

        eventClick: (val, key) => {
          this.showDetailView(val)
        },

        header: {
          left: 'today,   agendaDay,agendaWeek,month', //上一页、下一页、今天
          center: 'prevYear,prev, title, next,nextYear', //居中：时间范围区间标题
          right: ''
        },

        eventMouseover: (event, jsEvent, view) => {
          this.$refs.hoverDialog.style.display = 'block'
          this.$refs.hoverDialog.style.width =
            document.getElementsByClassName('fc-day')[0].offsetWidth - 10 + 'px'
          this.$refs.hoverDialog.style.left =
            jsEvent.currentTarget.offsetLeft + 'px'
          let styleTop = jsEvent.clientY - jsEvent.offsetY - 60
          this.$refs.hoverDialog.style.top = styleTop + 'px'
          this.hoverDialogList = {
            startTime: event.startTime || '无',
            stopTime: event.stopTime || '无',
            name: event.name,
            color: event.color,
            priority: event.priority
          }

          this.$nextTick(() => {
            let hoverDialogRect = this.$refs.hoverDialog.getBoundingClientRect()
            if (
              hoverDialogRect.top + hoverDialogRect.height >
              document.body.clientHeight
            ) {
              this.$refs.hoverDialog.style.top =
                styleTop - hoverDialogRect.height - 18 + 'px'
            }
          })
        },

        eventMouseout: (event, jsEvent, view) => {
          this.$refs.hoverDialog.style.display = 'none'
        },

        events: (start, end, timezone, callback) => {
          this.loading = true
          workTaskDateListAPI({
            startTime: start.format('YYYY-MM-DD'),
            endTime: end.format('YYYY-MM-DD')
          })
            .then(res => {
              let taskData = res.data.map(item => {
                if (item.startTime && item.stopTime) {
                  item.start = item.startTime
                  item.end = item.stopTime
                } else if (!item.startTime && item.stopTime) {
                  item.start = item.stopTime
                  item.end = item.stopTime
                } else if (item.startTime && !item.stopTime) {
                  item.start = item.startTime
                  item.end = item.startTime
                } else if (!item.startTime && !item.stopTime) {
                  item.start = item.updateTime
                  item.end = item.updateTime
                }

                item.color = this.getPriorityColor(item.priority)
                item.title = item.name
                return item
              })
              callback(taskData)
              this.loading = false
            })
            .catch(err => {
              this.loading = false
            })
        }
      })
    },

    /**
     * 获取优先级颜色
     */
    getPriorityColor(priority) {
      if (priority == 1) {
        return '#8bb5f0'
      } else if (priority == 2) {
        return '#FF9668'
      } else if (priority == 3) {
        return '#ED6363'
      } else {
        return '#ccc'
      }
    },

    /**
     * 展示创建任务
     */
    createTask() {
      this.taskCreateShow = true
    },

    /**
     * 关闭创建任务
     */
    handleClose() {
      this.taskCreateShow = false
    },

    /**
     * 点击显示详情
     */
    showDetailView(val) {
      this.taskID = val.taskId
      this.taskDetailShow = true
    },

    /**
     * 刷新日历
     */
    refetchCalendar() {
      $('#calendar').fullCalendar('refetchEvents')
    },

    /**
     * 关闭详情页
     */
    closeBtn() {
      this.taskDetailShow = false
    },

    /**
     * 点击空白处关闭详情
     */
    taskShowHandle(e) {
      if (
        this.$refs.particulars &&
        !this.$refs.particulars.$el.contains(e.target)
      ) {
        let hidden = true
        let items = document.getElementsByClassName('fc-event-container')
        for (let index = 0; index < items.length; index++) {
          const element = items[index]
          if (element.contains(e.target)) {
            hidden = false
            break
          }
        }
        this.taskDetailShow = !hidden
      }
    }
  }
}
</script>

<style>
@import 'fullcalendar/dist/fullcalendar.css';
</style>

<style lang="scss" scoped>
@import '@/styles/calendars.scss';

.task-calendars {
  position: relative;
  border: 1px solid #e6e6e6;
  border-radius: 4px;
  height: 100%;
  .hover-dialog {
    display: none;
    padding: 15px;
    z-index: 99;
    position: absolute;
    background: #fff;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    border-left: 2px solid transparent;

    .el-checkbox {
      margin-right: 5px;
    }

    .title {
      overflow: hidden;
      overflow-wrap: break-word;
      word-wrap: break-word;
      line-height: 18px;
    }

    .img-content {
      margin-top: 10px;
      margin-left: 20px;
      margin-right: 10px;
      color: #999999;
      font-size: 12px;
      .el-icon-time {
        color: #ddd;
        font-size: 14px;
      }
      img {
        vertical-align: middle;
      }
    }
  }

  .add-btn {
    position: absolute;
    top: 14px;
    right: 40px;
  }
}
</style>

