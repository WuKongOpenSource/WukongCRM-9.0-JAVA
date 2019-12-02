<template>
  <div class="project-tag">
    <div class="header">
      <span>{{ labelName }}</span>
      <el-popover
        v-model="labelSetShow"
        placement="bottom-start"
        width="300"
        trigger="click">
        <div class="new-tag-dialog">
          <div class="title">
            <span>标签设置</span>
            <span
              class="el-icon-close rt"
              @click="labelSetShow = false"/>
          </div>
          <div class="search">
            <el-input
              v-model="editLabelName"
              size="mini"
              placeholder="输入标签名"/>
            <span
              :style="{'background': editLabelColor}"
              class="checked-color"/>
          </div>
          <div class="color-box">
            <span
              v-for="(item, index) in colorList"
              :key="index"
              :style="{'background': item}"
              @click="editLabelColor = item"/>
          </div>
          <div class="footer">
            <el-button
              type="primary"
              size="medium"
              @click="labelSetSave">保存</el-button>
            <el-button
              size="medium"
              @click="labelSetShow = false">取消</el-button>
          </div>
        </div>
        <img
          slot="reference"
          src="@/assets/img/project/t_set.png"
          class="img-set"
          @click="labelSetClick">
      </el-popover>
    </div>
    <div
      v-loading="loading"
      class="content">
      <div
        v-empty="taskList.length == 0 && loading == false"
        class="content-body-items">
        <el-collapse v-model="collapseNames">
          <el-collapse-item
            v-for="(item, index) in taskList"
            :name="item.workId"
            :key="index"
            class="list-box">
            <i
              slot="title"
              :style="{color : item.color ? item.color : '#4AB8B8'}"
              class="wukong wukong-subproject"/>
            <span
              slot="title"
              class="title">{{ item.name }}</span>
            <task-cell
              v-for="(taskItem, taskIndex) in item.list"
              :key="taskIndex"
              :data="taskItem"
              :data-section="index"
              :data-index="taskIndex"
              class="item-list"
              @on-handle="taskCellHandle"/>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
    <!-- 详情 -->
    <particulars
      v-if="taskDetailShow"
      ref="particulars"
      :id="taskID"
      :detail-index="detailIndex"
      :detail-section="detailSection"
      @on-handle="detailHandle"
      @close="closeBtn"/>
  </div>
</template>

<script>
// API
import {
  workTasklableReadAPI,
  workTasklableGetWokListAPI,
  workTasklableSaveAPI
} from '@/api/projectManagement/tag'
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
      labelID: '',
      taskList: [],
      colorList: [
        '#53D397',
        '#20C1BD',
        '#58DADA',
        '#0FC9E7',
        '#3498DB',
        '#4586FF',
        '#8983F3',
        '#AEA1EA',
        '#FF6699',
        '#F24D70',
        '#FF6F6F'
      ],
      labelName: '',
      labelColor: '#53D397',
      // 展开的面板
      collapseNames: [],
      // 编辑
      editLabelName: '',
      editLabelColor: '',
      labelSetShow: false,
      // 详情数据
      taskID: '',
      detailIndex: -1,
      detailSection: -1,
      taskDetailShow: false
    }
  },

  beforeRouteUpdate(to, from, next) {
    this.labelID = to.params.id
    this.taskList = []
    this.collapseNames = []
    this.getDetail({ labelId: to.params.id })
    this.getList()
    next()
  },

  created() {
    this.labelID = this.$route.params.id
    this.getDetail({ labelId: this.$route.params.id })
    this.getList()
  },

  mounted() {
    document
      .getElementById('project-main-container')
      .addEventListener('click', this.taskShowHandle, false)
  },

  methods: {
    /**
     * 获取详情
     */
    getDetail(params) {
      workTasklableReadAPI(params)
        .then(res => {
          this.labelName = res.data.name
          this.labelColor = res.data.color
        })
        .catch(() => {})
    },

    /**
     * 页面数据
     */
    getList() {
      this.loading = true
      workTasklableGetWokListAPI({
        labelId: this.labelID
      })
        .then(res => {
          this.taskList = res.data || []
          for (const item of this.taskList) {
            for (const i of item.list) {
              if (i.status == 5) {
                i.checked = true
              }
            }
          }
          this.collapseNames = res.data.map(item => {
            return item.workId
          })
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * 标签设置保存
     */
    labelSetSave() {
      workTasklableSaveAPI({
        name: this.editLabelName,
        labelId: this.labelID,
        color: this.editLabelColor
      }).then(res => {
        this.labelName = this.editLabelName
        this.labelColor = this.editLabelColor
        this.labelSetShow = false
      })
    },

    /**
     * 标签设置
     */
    labelSetClick() {
      this.editLabelName = this.labelName
      this.editLabelColor = this.labelColor
    },

    /**
     * 列表操作
     */
    taskCellHandle(data) {
      if (data.type == 'view') {
        const dataCell = data.data
        this.taskID = dataCell.item.taskId
        this.detailIndex = dataCell.index
        this.detailSection = dataCell.section
        this.taskDetailShow = true
      }
    },

    /**
     * 关闭详情页
     */
    closeBtn() {
      this.taskDetailShow = false
    },

    /**
     * 详情操作
     */
    detailHandle(data) {
      if (data.index == 0 || data.index) {
        // 是否完成勾选
        if (data.type == 'title-check') {
          this.$set(
            this.taskList[data.section].list[data.index],
            'checked',
            data.value
          )
        } else if (
          data.type == 'delete' ||
          data.type == 'activate-task' ||
          data.type == 'recover-task' ||
          data.type == 'thorough-delete-task'
        ) {
          this.taskList.splice(data.index, 1)
        } else if (data.type == 'change-stop-time') {
          const stopTime = new Date(data.value).getTime() / 1000 + 86399
          if (stopTime > new Date(new Date()).getTime() / 1000) {
            this.taskList[data.section].list[data.index].isEnd = false
          } else {
            this.taskList[data.section].list[data.index].isEnd = true
          }
          this.taskList[data.section].list[data.index].stopTime = data.value
        } else if (data.type == 'change-priority') {
          this.taskList[data.section].list[data.index].priority = data.value.id
        } else if (data.type == 'change-name') {
          this.taskList[data.section].list[data.index].name = data.value
        } else if (data.type == 'change-comments') {
          const commentCount = this.taskList[data.section].list[data.index]
            .commentCount
          if (data.value == 'add') {
            this.taskList[data.section].list[data.index].commentCount =
              commentCount + 1
          } else {
            this.taskList[data.section].list[data.index].commentCount =
              commentCount - 1
          }
        } else if (data.type == 'change-sub-task') {
          this.taskList[data.section].list[data.index].childWCCount =
            data.value.subdonecount
          this.taskList[data.section].list[data.index].childAllCount =
            data.value.allcount
        } else if (data.type == 'change-main-user') {
          this.taskList[data.section].list[data.index].mainUser = data.value
        } else if (data.type == 'change-label') {
          this.taskList[data.section].list[data.index].labelList = data.value
        }
      }
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
        const items = document.getElementsByClassName('board-column')
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
.project-tag {
  height: 100%;
  overflow: hidden;
  position: relative;
  .header {
    height: 60px;
    line-height: 60px;
    position: relative;
    z-index: 100;
    padding: 0 20px;
    font-size: 18px;
    span {
      margin-right: 5px;
    }
    .img-set {
      width: 15px;
      vertical-align: middle;
      margin-right: 5px;
      cursor: pointer;
    }
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

    .content-body-items {
      height: 100%;
    }

    .el-collapse {
      border: none;
    }

    .el-collapse /deep/ .el-collapse-item__wrap {
      border: 0;
    }
    .el-collapse /deep/ .el-collapse-item__header {
      padding-left: 20px;
      border: 0;
      .el-icon-arrow-right {
        float: none;
        margin: 0;
      }
      .wukong-subproject {
        font-size: 20px;
        margin-right: 5px;
      }
      .title {
        float: left;
        margin-right: 20px;
      }
    }
    .list-box {
      font-size: 14px;
      color: #333;
    }
  }
}
.new-tag-dialog {
  .title {
    padding: 10px;
    border-bottom: 1px solid#E6E6E6;
    margin-bottom: 20px;
    .el-icon-close {
      margin-right: 0;
      cursor: pointer;
    }
    .el-icon-arrow-left {
      cursor: pointer;
    }
  }
  .search {
    position: relative;
    .el-input {
      width: 90%;
      margin: 0 5%;
    }
    .el-input /deep/ .el-input__inner {
      padding-left: 40px;
    }
    .checked-color {
      position: absolute;
      left: 5%;
      top: 0;
      display: inline-block;
      width: 15px;
      height: 15px;
      border-radius: 50%;
      margin: 6px 10px;
    }
  }
  .footer {
    text-align: right;
    margin: 20px;
  }
  .color-box {
    margin: 10px 5%;
    span {
      display: inline-block;
      width: 15px;
      height: 15px;
      margin-right: 8px;
      border-radius: 50%;
      cursor: pointer;
    }
    span:last-child {
      margin: 0;
    }
  }
}
</style>

