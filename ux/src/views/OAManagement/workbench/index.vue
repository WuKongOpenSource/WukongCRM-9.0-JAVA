<template>
  <div class="workbench">
    <div class="tabs-box">
      <el-tabs v-model="activeName"
               @tab-click="tabClick"
               v-loading="loading">
        <el-tab-pane :label="item.label"
                     :name="item.key"
                     v-for="(item, index) in tabsData"
                     :key="index">
          <tab-journal v-if="activeName == '1'"
                       :journalData="listData">
            <p class="load"
               slot="load">
              <el-button type="text"
                         :loading="loadMoreLoading">{{loadText}}</el-button>
            </p>
          </tab-journal>
          <tabs-content v-else
                        :listData="listData">
            <p class="load"
               slot="workbenchLoad">
              <el-button type="text"
                         :loading="loadMoreLoading">{{loadText}}</el-button>
            </p>
          </tabs-content>
        </el-tab-pane>
      </el-tabs>
    </div>
    <div class="right-content">
      <div class="task">
        <v-task>
        </v-task>
      </div>
      <div class="schedule">
        <v-schedule v-loading="scheduleLoading"
                    :calendarArr="calendarArr"
                    @changeMonth="changeMonth">
        </v-schedule>
      </div>
    </div>
  </div>
</template>

<script>
import tabsContent from './tabsContent'
import VTask from './task'
import VSchedule from './schedule'
import tabJournal from './tabsJournal'
// API
import { workbenchList, eventList } from '@/api/oamanagement/workbench'
import moment from 'moment'

export default {
  components: {
    tabsContent,
    VTask,
    VSchedule,
    tabJournal
  },
  data() {
    return {
      activeName: 0,
      tabsData: [
        { label: '全部', key: '0' },
        { label: '日志', key: '1' },
        { label: '审批', key: '5' },
        { label: '任务', key: '4' },
        { label: '日程', key: '2' },
        { label: '公告', key: '3' }
      ],
      // 列表数据
      listData: [],
      loading: true,
      // 页数
      loadMoreLoading: true,
      pageNum: 1,
      loadText: '加载更多',
      isPost: false,
      calendarArr: [],
      // 日程加载
      scheduleLoading: true
    }
  },
  mounted() {
    // 日程
    eventList({ month: moment(new Date()).format('YYYY-MM') })
      .then(res => {
        for (let item of res.data) {
          if (item.status == 1) {
            item.className = 'mark1'
          }
        }
        this.calendarArr = res.data
        this.scheduleLoading = false
      })
      .catch(err => {
        this.scheduleLoading = false
      })
    this.workbenchData(this.activeName, this.pageNum)
    // 分批次加载
    for (let dom of document.getElementsByClassName('el-tabs__content')) {
      dom.onscroll = () => {
        let scrollOff = dom.scrollTop + dom.clientHeight - dom.scrollHeight
        //滚动条到底部的条件
        if (Math.abs(scrollOff) < 10 && this.loadMoreLoading == true) {
          if (!this.isPost) {
            this.isPost = true
            this.pageNum++
            this.workbenchData(this.activeName, this.pageNum)
          } else {
            this.loadMoreLoading = false
          }
        }
      }
    }
  },
  methods: {
    // 获取数据
    workbenchData(type, page) {
      workbenchList({
        type: type,
        page: page,
        limit: 15
      })
        .then(res => {
          this.listData = this.listData.concat(res.data.list)
          if (res.data.list.length < 15) {
            this.loadText = '没有更多了'
            this.loadMoreLoading = false
          } else {
            this.loadText = '加载更多'
            this.loadMoreLoading = true
          }
          this.isPost = false
          this.loading = false
        })
        .catch(err => {
          this.loadText = ''
          this.loading = false
          this.isPost = false
        })
    },
    // tabs切换事件
    tabClick(val) {
      this.loading = true
      this.listData = []
      this.loadMoreLoading = true
      this.pageNum = 1
      this.workbenchData(this.activeName, this.pageNum)
    },
    // 切换上下月
    changeMonth(monthDate) {
      this.scheduleLoading = true
      eventList({ month: moment(monthDate).format('YYYY-MM') })
        .then(res => {
          for (let item of res.data) {
            if (item.status == 1) {
              item.className = 'mark1'
            }
          }
          this.calendarArr = res.data
          this.scheduleLoading = false
          // 切换月份去掉背景
          this.$nextTick(() => {
            for (let item of document.getElementsByClassName('wh_item_date')) {
              item.classList.remove('wh_chose_day')
            }
          })
        })
        .catch(err => {
          this.scheduleLoading = false
        })
    }
  }
}
</script>

<style scoped lang="scss">
@import '../styles/tabs.scss';

$colorF: #fff;
.workbench {
  // margin: 0 auto;
  height: 100%;
  min-height: 720px;
  // width: 100%;
  width: 1160px;
  display: flex;
  .tabs-box {
    width: 770px;
    // width: 66%;
    background: $colorF;
    margin-right: 20px;
    height: 100%;
    border: 1px solid rgba(230, 230, 230, 1);
    border-radius: 4px;
    // padding: 0 20px;
    .load {
      color: #999;
      font-size: 13px;
      margin: 0 auto 15px;
      text-align: center;
      .el-button,
      .el-button:focus {
        color: #ccc;
        cursor: auto;
      }
    }
  }
  .tabs-box /deep/ .el-tabs {
    display: flex;
    flex-direction: column;
    height: 100%;
    .el-tabs__content {
      flex: 1;
      overflow: auto;
      margin-bottom: 20px;
    }
  }
  .right-content {
    width: 370px;
    // width: 34%;
    .task {
      background: $colorF;
      height: 300px;
      margin-bottom: 20px;
      border: 1px solid rgba(230, 230, 230, 1);
      border-radius: 4px;
    }
    .schedule {
      background: $colorF;
      min-height: 400px;
      padding: 0 20px;
      border: 1px solid rgba(230, 230, 230, 1);
      border-radius: 4px;
    }
  }
}
</style>
