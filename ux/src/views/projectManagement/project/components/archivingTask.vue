<template>
  <div class="archiving-task"
       v-loading="loading">
    <div class="archiving-task-body"
         v-empty="taskList">
      <!-- 任务cell -->
      <task-cell v-for="(item, index) in taskList"
                 :key="index"
                 :data="item"
                 :dataIndex="index"
                 @on-handle="taskCellHandle">
      </task-cell>
    </div>

    <!-- 任务详情 -->
    <particulars v-if="taskDetailShow"
                 ref="particulars"
                 :id="taskID"
                 :detailIndex="detailIndex"
                 @on-handle="detailHandle"
                 @close="closeBtn">
    </particulars>

  </div>
</template>

<script>
import TaskCell from '@/views/projectManagement/components/taskCell'
import axios from 'axios'
import { workTaskArchListAPI } from '@/api/projectManagement/project'
import { workTaskSaveAPI } from '@/api/projectManagement/task'
import particulars from '@/views/projectManagement/components/particulars'

export default {
  components: {
    particulars,
    TaskCell
  },

  data() {
    return {
      loading: false,
      taskList: [],
      firstRequst: true,
      // 详情数据
      taskID: '',
      detailIndex: -1,
      taskDetailShow: false
    }
  },

  watch: {
    workId() {
      this.taskList = []
      this.getList(true)
    }
  },

  props: {
    workId: [Number, String]
  },

  activated() {
    if (this.firstRequst) {
      this.firstRequst = false
      this.getList(true) 
    } else {
      this.getList(false)
    }
  },

  mounted() {
    document
      .getElementById('project-main-container')
      .addEventListener('click', this.taskShowHandle, false)
  },

  methods: {
    /**
     * 获取列表
     */
    getList(loading) {
      this.loading = loading
      workTaskArchListAPI({ workId: this.workId })
        .then(res => {
          this.loading = false
          // 完成状态 1正在进行2延期3归档 5结束
          for (let item of res.data) {
            item.checked = item.status == 5
          }
          this.taskList = res.data
        })
        .catch(err => {
          this.loading = false
        })
    },

    /**
     * 页面勾选删除
     */
    changeCheckbox(val) {
      workTaskSaveAPI({
        taskId: val.taskId,
        status: val.checked ? 5 : 1
      })
        .then(res => {})
        .catch(err => {
          val.checked = false
        })
    },

    /**
     * 点击显示详情
     */
    showDetailView(val, index) {
      this.taskID = val.taskId
      this.detailIndex = index
      this.taskDetailShow = true
    },

    /**
     * 详情操作
     */
    detailHandle(data) {
      if (data.index == 0 || data.index) {
        // 是否完成勾选
        if (data.type == 'title-check') {
          this.$set(this.taskList[data.index], 'checked', data.value)
        } else if (data.type == 'delete' || data.type == 'activate-task') {
          this.taskList.splice(data.index, 1)
        } else if (data.type == 'change-stop-time') {
          let stopTime = new Date(data.value).getTime() / 1000 + 86399
          if (stopTime > new Date(new Date()).getTime() / 1000) {
            this.taskList[data.index].isEnd = false
          } else {
            this.taskList[data.index].isEnd = true
          }
          this.taskList[data.index].stopTime = data.value
        } else if (data.type == 'change-priority') {
          this.taskList[data.index].priority = data.value.id
        } else if (data.type == 'change-name') {
          this.taskList[data.index].name = data.value
        } else if (data.type == 'change-comments') {
          let commentCount = this.taskList[data.index].commentCount
          if (data.value == 'add') {
            this.taskList[data.index].commentCount = commentCount + 1
          } else {
            this.taskList[data.index].commentCount = commentCount - 1
          }
        } else if (data.type == 'change-sub-task') {
          this.taskList[data.index].childWCCount = data.value.subdonecount
          this.taskList[data.index].childAllCount =
            data.value.allcount
        }
      }
    },

    /**
     * 关闭详情页
     */
    closeBtn() {
      this.taskDetailShow = false
    },

    /**
     * 详情页点击勾选
     */
    titleCheckbox(checked) {
      for (let item of this.taskList) {
        if (item.taskId == this.indexObjData.taskId) {
          this.$set(item, 'checked', checked)
        }
      }
      // this.moreDelete()
    },

    /**
     * 查看任务详情
     */
    taskCellHandle(data) {
      if (data.type == 'view') {
        this.showDetailView(data.data.item, data.data.index)
      }
    },

    // 点击空白处关闭详情
    taskShowHandle(e) {
      if (
        this.$refs.particulars &&
        !this.$refs.particulars.$el.contains(e.target)
      ) {
        let hidden = true
        let items = document.getElementsByClassName('list-box')
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

<style scoped lang="scss">
.archiving-task {
  border: 1px solid #e6e6e6;
  height: 100%;
  background-color: white;
  &-body {
    height: 100%;
  }
}
</style>

