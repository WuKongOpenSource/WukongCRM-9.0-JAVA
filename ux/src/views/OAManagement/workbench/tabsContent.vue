<template>
  <div class="tabs-content">
    <div
      v-for="(item, index) in listData"
      :key="index"
      class="list">
      <template v-if="item.type == 1">
        <tab-journal
          :margin-defaults="true"
          :journal-data="[item]"/>
      </template>
      <template v-else>
        <div
          v-photo="item.createUser"
          v-lazy:background-image="$options.filters.filterUserLazyImg(item.createUser.img)"
          class="div-photo"/>
        <div class="img-text">
          <div class="name-time">
            <p class="name-behavior">
              <span
                v-if="item.createUser.realname"
                class="name">{{ item.createUser.realname }}</span>
              <span class="behavior">{{ item.actionContent }}</span>
            </p>
            <p class="time">{{ item.createTime }}</p>
          </div>
          <div class="log-title">
            <img
              v-if="item.type == 1"
              src="@/assets/img/work_log.png">
            <img
              v-else-if="item.type == 2"
              src="@/assets/img/work_schedule.png">
            <img
              v-else-if="item.type == 3"
              src="@/assets/img/work_notice.png">
            <img
              v-else-if="item.type == 4"
              src="@/assets/img/work_task.png">
            <img
              v-else-if="item.type == 5"
              class="img-5"
              src="@/assets/img/work_examine.png">
            <span class="type-name">{{ item.typeName }}</span>
          </div>
          <div
            v-if="item.title"
            class="title">
            <span
              ref="taskRow"
              @click="rowFun(item)">{{ item.title }}</span>
          </div>
        </div>
      </template>
    </div>
    <slot name="workbenchLoad"/>
    <!-- 公告详情 -->
    <v-details
      v-if="dialog"
      :btn-show="false"
      :title-list="titleList"
      @close="close"/>
    <!-- 日程详情 -->
    <schedule-details
      v-if="showScheduleDetails"
      :btn-show="false"
      :dialog-visible="dialogVisible"
      :list-data="scheduleData"
      @handleClose="scheduleClose"/>
    <!-- 任务详情 -->
    <particulars
      v-if="taskDetailShow"
      ref="particulars"
      :id="taskID"
      @close="taskDetailShow = false"/>
    <!-- 审批详情 -->
    <examine-detail
      v-if="showExamine"
      :id="examineData.id"
      :no-listener-class="['tabs-content']"
      @hide-view="showExamine=false"/>
  </div>
</template>

<script>
import VDetails from '../notice/details'
import ScheduleDetails from '../schedule/components/details'
import ExamineDetail from '../examine/components/examineDetail'
// 任务详情
import particulars from '../task/components/particulars'

import tabJournal from './tabsJournal'
export default {
  components: {
    VDetails,
    ScheduleDetails,
    particulars,
    tabJournal,
    ExamineDetail
  },
  props: {
    listData: Array
  },
  data() {
    return {
      // 公告详情
      dialog: false,
      titleList: {},
      // 日程详情
      dialogVisible: false,
      scheduleData: {},
      // 任务 -- 详情数据
      taskID: '',
      taskDetailShow: false,
      // 是否显示日程详情
      showScheduleDetails: false,
      // 是否显示审批
      examineData: {},
      showExamine: false
    }
  },
  mounted() {
    document
      .getElementById('workbench-main-container')
      .addEventListener('click', this.taskShowHandle, false)
  },
  methods: {
    rowFun(val) {
      switch (val.type) {
        // 日程
        case 2:
          {
            this.dialogVisible = true
            this.scheduleData = val
            const list = []
            list.push(val.startTime, val.endTime)
            this.scheduleData.time = list
            this.showScheduleDetails = true
          }
          break
        // 公告
        case 3:
          this.dialog = true
          this.titleList = {
            title: val.title,
            createTime: val.createTime,
            content: val.annContent
          }
          break
        // 任务
        case 4:
          this.taskID = val.actionId
          this.taskDetailShow = true
          break
        // 审批
        case 5:
          this.examineData = { id: val.actionId }
          this.showExamine = true
          break
      }
    },
    // 点击空白处关闭详情
    taskShowHandle(e) {
      if (
        this.$refs.particulars &&
        !this.$refs.particulars.$el.contains(e.target)
      ) {
        let hidden = true
        const items = document.getElementsByClassName('tabs-content')
        for (let index = 0; index < items.length; index++) {
          const element = items[index]
          if (element.contains(e.target)) {
            hidden = false
            break
          }
        }
        this.taskDetailShow = !hidden
      }
    },
    // 公告详情关闭
    close() {
      this.dialog = false
    },
    // 日程详情关闭
    scheduleClose() {
      this.dialogVisible = false
    }
  }
}
</script>

<style scoped lang="scss">
.tabs-content {
  margin: 0 30px;
  .list {
    font-size: 13px;
    padding: 20px 0;
    border-bottom: 1px solid #f1f1f1;
    .div-photo {
      width: 35px;
      height: 35px;
      border-radius: 17.5px;
      float: left;
    }
    .img-text {
      margin-left: 50px;
      .name-time {
        display: inline-block;
        .name-behavior {
          margin-bottom: 6px;
          .name {
            margin-right: 10px;
            color: #333;
            font-size: 15px;
          }
          .behavior {
            color: #666;
          }
        }
        .time {
          color: #999;
          font-size: 12px;
        }
      }
      .log-title {
        float: right;
        margin-top: 6px;
        img,
        .type-name {
          vertical-align: middle;
        }
        .img-5 {
          margin-bottom: 3px;
        }
      }
      .title {
        margin: 20px 0 0;
        color: #3e84e9;
        white-space: pre-wrap;
        word-wrap: break-word;
        letter-spacing: 0.5px;
        line-height: 18px;
        background-color: #f4f7fd;
        padding: 10px;
        border-radius: 2px;
        span {
          cursor: pointer;
        }
      }
      .title :hover {
        text-decoration: underline;
      }
    }
  }
  .list:last-child .title {
    border: 0;
  }
  .list:first-child {
    padding-top: 5px;
  }
}
</style>
