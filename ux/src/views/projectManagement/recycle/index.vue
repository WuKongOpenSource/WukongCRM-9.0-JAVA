<template>
  <div class="recycle">
    <div class="recycle-header">
      回收站
    </div>
    <div
      v-loading="loading"
      class="content">
      <task-cell
        v-for="(item, index) in list"
        :key="index"
        :data="item"
        :data-index="index"
        class="item-list"
        @on-handle="taskCellHandle"/>
    </div>

    <!-- 详情 -->
    <particulars
      v-if="taskDetailShow"
      ref="particulars"
      :id="taskID"
      :detail-index="detailIndex"
      @on-handle="detailHandle"
      @close="closeBtn"/>
  </div>
</template>

<script>
import { workTrashIndexAPI } from '@/api/projectManagement/recycle'
import TaskCell from '@/views/projectManagement/components/taskCell'
import particulars from '../components/particulars'

export default {
  components: {
    particulars,
    TaskCell
  },

  data() {
    return {
      loading: true,
      list: [],
      // 详情数据
      taskID: '',
      detailIndex: -1,
      taskDetailShow: false
    }
  },

  created() {
    this.getList()
  },

  mounted() {
    document
      .getElementById('project-main-container')
      .addEventListener('click', this.taskShowHandle, false)
  },

  methods: {
    /**
     * 获取数据
     */
    getList() {
      workTrashIndexAPI()
        .then(res => {
          this.list = res.data
          for (const i of this.list) {
            i.isDeleted = true
            if (i.status == 5) {
              i.checked = true
            }
          }
          this.loading = false
        })
        .catch(() => {
          this.loading = true
        })
    },

    /**
     * 列表操作
     */
    taskCellHandle(data) {
      if (data.type == 'view') {
        const dataCell = data.data
        this.taskID = dataCell.item.taskId
        this.detailIndex = dataCell.index
        this.taskDetailShow = true
      }
    },

    /**
     * 详情操作
     */
    detailHandle(data) {
      if (data.index == 0 || data.index) {
        // 是否完成勾选
        if (data.type == 'title-check') {
          this.$set(this.list[data.index], 'checked', data.value)
        } else if (
          data.type == 'delete' ||
          data.type == 'activate-task' ||
          data.type == 'recover-task' ||
          data.type == 'thorough-delete-task'
        ) {
          this.list.splice(data.index, 1)
        } else if (data.type == 'change-stop-time') {
          const stopTime = new Date(data.value).getTime() / 1000 + 86399
          if (stopTime > new Date(new Date()).getTime() / 1000) {
            this.list[data.index].isEnd = false
          } else {
            this.list[data.index].isEnd = true
          }
          this.list[data.index].stopTime = data.value
        } else if (data.type == 'change-priority') {
          this.list[data.index].priority = data.value.id
        } else if (data.type == 'change-name') {
          this.list[data.index].name = data.value
        } else if (data.type == 'change-comments') {
          const commentCount = this.list[data.index].commentCount
          if (data.value == 'add') {
            this.list[data.index].commentCount = commentCount + 1
          } else {
            this.list[data.index].commentCount = commentCount - 1
          }
        } else if (data.type == 'change-sub-task') {
          this.list[data.index].childWCCount = data.value.subdonecount
          this.list[data.index].childAllCount =
            data.value.allcount
        } else if (data.type == 'change-main-user') {
          this.list[data.index].mainUser = data.value
        } else if (data.type == 'change-label') {
          this.list[data.index].labelList = data.value
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
     * 点击空白处关闭详情
     */
    taskShowHandle(e) {
      if (
        this.$refs.particulars &&
        !this.$refs.particulars.$el.contains(e.target)
      ) {
        let hidden = true
        const items = document.getElementsByClassName('item-list')
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
.recycle {
  height: 100%;
  overflow: hidden;
  position: relative;
  .recycle-header {
    height: 60px;
    line-height: 60px;
    position: relative;
    z-index: 100;
    padding: 0 20px;
    font-size: 18px;
  }
  .content {
    background-color: white;
    position: absolute;
    top: 60px;
    right: 0;
    bottom: 0;
    left: 0;
    border-radius: 3px;
    overflow-y: auto;
    border: 1px solid #e6e6e6;
  }
}
</style>
