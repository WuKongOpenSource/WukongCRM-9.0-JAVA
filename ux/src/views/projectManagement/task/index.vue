<template>
  <div class="my-task">
    <div class="my-task-header">
      我的任务
    </div>
    <div class="my-task-body">
      <div class="content-box"
           v-loading="loading"
           v-scrollx="{ ignoreClass :['ignoreClass']}">
        <div class="board-column"
             v-for="(item, index) in taskList"
             :key="index">
          <flexbox orient="vertical"
                   align="stretch"
                   class="board-column-wrapper ignoreClass">
            <div class="board-column-header">
              <div>
                <span class="text"> {{ item.title }} </span>
                <span class="text-num">{{item.checkedNum}} / {{item.list.length}}</span>
              </div>
              <el-progress v-if="item.checkedNum == 0"
                           :percentage="0"></el-progress>
              <el-progress v-else
                           :percentage="item.checkedNum / item.list.length * 100"></el-progress>
            </div>
            <draggable :list="item.list"
                       :options="{ group: 'mission', forceFallback: false, dragClass: 'sortable-drag' }"
                       @end="moveEndTask"
                       :id="index"
                       class="board-column-content">
              <div v-for="(element, i) in item.list"
                   :key="i"
                   :class="element.checked ? 'board-item board-item-active' : 'board-item'"
                   @click="showDetailView(element, index, i)"
                   ref="taskRow"
                   :style="{'border-color': element.priority == 1 ? '#8bb5f0' : element.priority == 2 ? '#FF9668' : element.priority == 3 ? '#ED6363' : ''}">
                <flexbox align="stretch">
                  <div @click.stop>
                    <el-checkbox v-model="element.checked"
                                 @change="checkboxChange(element, item)"></el-checkbox>
                  </div>
                  <div class="element-label">{{element.name}}<span v-if="element.workName">（{{element.workName}}）</span></div>
                  <div v-if="element.mainUser"
                       v-photo="element.mainUser"
                       v-lazy:background-image="$options.filters.filterUserLazyImg(element.mainUser.img)"
                       :key="element.mainUser.img"
                       class="head-png div-photo"></div>
                </flexbox>
                <div class="img-group">
                  <div class="img-box"
                       v-if="element.stopTime">
                    <i class="wukong wukong-time-task"
                       :style="{'color': element.isEnd == 1 && !element.checked ? 'red': '#999'}"></i>
                    <span :style="{'color': element.isEnd == 1 && !element.checked ? 'red': '#999'}">{{new Date(element.stopTime).getTime() | filterTimestampToFormatTime('MM-DD')}} 截止</span>
                  </div>
                  <div class="img-box"
                       v-if="element.childAllCount > 0">
                    <i class="wukong wukong-sub-task"></i>
                    <span>{{element.childWCCount}}/{{element.childAllCount}}</span>
                  </div>
                  <div class="img-box"
                       v-if="element.fileCount">
                    <i class="wukong wukong-file"></i>
                    <span>{{element.fileCount}}</span>
                  </div>
                  <div class="img-box"
                       v-if="element.commentCount">
                    <i class="wukong wukong-comment-task"></i>
                    <span>{{element.commentCount}}</span>
                  </div>

                  <template v-if="element.labelList.length <= 2">
                    <div v-for="(k, j) in element.labelList"
                         :key="j"
                         class="item-label"
                         :style="{'background': k.color}">
                      {{k.labelName}}
                    </div>
                  </template>
                  <template v-else>
                    <div class="item-label"
                         :style="{'background': element.labelList[0].color}">{{element.labelList[0].labelName}}</div>
                    <div class="item-label"
                         :style="{'background': element.labelList[1].color}">{{element.labelList[1].labelName}}</div>
                    <el-tooltip placement="top"
                                effect="light"
                                popper-class="tooltip-change-border task-tooltip">
                      <div slot="content"
                           style="margin: 10px 10px 10px 0;">
                        <div v-for="(k, j) in element.labelList"
                             :key="j"
                             style="display: inline-block; margin-right: 10px;">
                          <span v-if="j >= 2"
                                class="k-name"
                                :style="{'background': k.color ? k.color: '#ccc'}"
                                style="border-radius: 3px; color: #FFF; padding: 3px 10px;">{{k.labelName}}</span>
                        </div>
                      </div>
                      <div class="color-label-more">
                        <i>...</i>
                      </div>
                    </el-tooltip>

                  </template>
                </div>
              </div>
            </draggable>
            <div class="new-task"
                 @click="createTaskByTop(item.isTop)">
              <span class="el-icon-plus"></span>
              <span>新建任务</span>
            </div>
          </flexbox>
        </div>
      </div>
    </div>
    <!-- 新建任务弹出框 newDialog-->
    <new-dialog :visible="taskCreateShow"
                :params="{isTop: topId}"
                @handleClose="handleClose"
                @submit="getList">
    </new-dialog>
    <!-- 详情 -->
    <particulars v-if="taskDetailShow"
                 ref="particulars"
                 :id="taskID"
                 :detailIndex="detailIndex"
                 :detailSection="detailSection"
                 @on-handle="detailHandle"
                 @close="closeBtn">
    </particulars>
  </div>
</template>
<script>
import newDialog from '../components/newDialog'
import particulars from '../components/particulars'
import draggable from 'vuedraggable'
import scrollx from '@/directives/scrollx'
import {
  workTaskMyTaskAPI,
  workTaskUpdateTopAPI,
  workTaskSaveAPI
} from '@/api/projectManagement/task'

export default {
  components: {
    draggable,
    newDialog,
    particulars
  },

  directives: {
    scrollx
  },

  data() {
    return {
      taskList: [],
      // 新建任务弹出框
      taskCreateShow: false,
      // 加载中
      loading: true,
      // 拖拽的任务列表id
      topId: '',
      // 详情数据
      taskID: '',
      detailIndex: -1,
      detailSection: -1,
      taskDetailShow: false
    }
  },

  created() {
    this.getList()
  },

  mounted() {
    //为了防止火狐浏览器拖拽的时候以新标签打开，此代码真实有效
    document.body.ondrop = function(event) {
      event.preventDefault()
      event.stopPropagation()
    }

    document
      .getElementById('project-main-container')
      .addEventListener('click', this.taskShowHandle, false)
  },

  methods: {
    /**
     * 获取数据列表
     */
    getList() {
      this.loading = true
      workTaskMyTaskAPI()
        .then(res => {
          for (let item of res.data) {
            item.checkedNum = 0
            for (let i of item.list) {
              if (i.status == 5) {
                i.checked = true
                item.checkedNum += 1
              } else {
                i.checked = false
              }
            }
          }
          this.taskList = res.data
          this.loading = false
        })
        .catch(err => {
          this.loading = false
        })
    },

    /**
     * 移动任务
     */
    moveEndTask(evt) {
      if (evt) {
        let fromTop = evt.from.id
        let toTop = evt.to.id

        // 如果没有进行移动 不做处理
        if (fromTop == toTop && evt.oldIndex == evt.newIndex) {
          return
        }

        let fromTask = this.taskList[fromTop]
        let fromList = fromTask.list
        this.updateTaskListCheckNum(fromTask)

        let toTask = this.taskList[toTop]
        let toList = toTask.list
        this.updateTaskListCheckNum(toTask)

        let params = {}
        if (fromTop == toTop) {
          params = {
            toList: toList.map(item => {
              return item.taskId
            }),
            toTopId: toTop
          }
        } else {
          params = {
            fromList: fromList.map(item => {
              return item.taskId
            }),
            fromTopId: fromTop,
            toList: toList.map(item => {
              return item.taskId
            }),
            toTopId: toTop
          }
        }
        workTaskUpdateTopAPI(params)
          .then(res => {})
          .catch(err => {})
      }
    },

    
    /**
     * 更新勾选数字
     */
    updateTaskListCheckNum(task) {
      task.checkedNum = task.list.filter(item => {
        return item.checked
      }).length
    },

    /**
     * 勾选完成任务
     */
    checkboxChange(element, value) {
      if (element.checked) {
        value.checkedNum++
      } else {
        value.checkedNum--
      }
      workTaskSaveAPI({
        taskId: element.taskId,
        status: element.checked ? 5 : 1
      })
        .then(res => {})
        .catch(err => {
          if (element.checked) {
            value.checkedNum--
          } else {
            value.checkedNum++
          }
          element.checked = !element.checked
        })
    },

    /**
     * 新增按钮
     */
    createTaskByTop(isTop) {
      this.topId = isTop
      this.taskCreateShow = true
    },

    /**
     * 关闭新建窗口
     */
    handleClose() {
      this.taskCreateShow = false
    },

    /**
     * 点击显示详情
     */
    showDetailView(val, seciton, index) {
      this.topId = val.isTop
      this.taskID = val.taskId
      this.detailIndex = index
      this.detailSection = seciton
      this.taskDetailShow = true
    },

    /**
     * 详情操作
     */
    detailHandle(data) {
      if (data.index == 0 || data.index) {
        // 是否完成勾选
        if (data.type == 'title-check') {
          let sectionItem = this.taskList[data.section]
          this.$set(sectionItem.list[data.index], 'checked', data.value)
          if (data.value) {
            sectionItem.checkedNum++
          } else {
            sectionItem.checkedNum--
          }
          this.$set(sectionItem, 'checkedNum', sectionItem.checkedNum)
        } else if (data.type == 'delete') {
          this.taskList[data.section].list.splice(data.index, 1)
        } else if (data.type == 'change-stop-time') {
          let stopTime = parseInt(data.value) + 86399
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
          let commentCount = this.taskList[data.section].list[data.index]
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
        let items = document.getElementsByClassName('board-item')
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
.my-task {
  height: 100%;
  overflow: hidden;
  .my-task-header {
    height: 60px;
    line-height: 60px;
    position: relative;
    z-index: 100;
    padding: 0 20px;
    font-size: 18px;
  }
}

.my-task-body {
  padding-left: 15px;
  height: calc(100% - 60px);
  position: relative;
  overflow-y: hidden;
  overflow-x: auto;
  white-space: nowrap;
}

.content-box {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  overflow-y: hidden;
  overflow-x: auto;
}

.content-box > div:last-child {
  margin-right: 0;
}
.board-column {
  width: 280px;
  height: 100%;
  display: inline-block;
  margin: 0 7px;
  overflow: hidden;

  .board-column-wrapper {
    max-height: 100%;
    padding: 10px;
    vertical-align: top;
    border-radius: 4px;
    background: #fff;
    margin-right: 14px;
    position: relative;
    .board-column-header {
      padding: 10px 3px 17px 3px;
      color: #333;
      .text {
        font-size: 15px;
        display: inline-block;
        padding-bottom: 5px;
      }
      .el-progress /deep/ .el-progress-bar {
        padding-right: 0;
      }
      .el-progress /deep/ .el-progress__text {
        display: none;
      }
      .text-num {
        margin-left: 10px;
        color: #999;
      }
    }
    .board-column-content {
      min-height: 20px;
      margin-right: -10px;
      padding-right: 14px;
      padding-left: 7px;
      max-height: calc(100% - 90px);
      overflow: auto;
      .board-item {
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        padding: 10px 35px 0 10px;
        margin-bottom: 10px;
        margin-top: 1px;
        margin-left: 1px;
        border-radius: 3px;
        border-left: 2px solid transparent;
        cursor: pointer;
        overflow: hidden;
        position: relative;
        .img-group {
          padding: 10px 0 0 20px;
          white-space: normal;
          word-wrap: break-word;
          .img-box {
            display: inline-block;
            margin-bottom: 7px;
            margin-right: 5px;
            font-size: 12px;
            color: #999;
            font-size: 12px;
            img {
              width: 15px;
              vertical-align: middle;
            }
          }
        }
        .head-png {
          vertical-align: middle;
          position: absolute;
          top: 8px;
          right: 5px;
          width: 24px;
          height: 24px;
          border-radius: 50%;
        }
        .element-label {
          width: 180px;
          padding-left: 8px;
          white-space: pre-wrap;
          word-wrap: break-word;
          line-height: 18px;
          cursor: pointer;
          span {
            color: #999;
            font-size: 12px;
          }
          img {
            vertical-align: middle;
          }
        }
      }
      .board-item-active {
        border: 0;
        color: #8f8f8f;
        background: #f3f3f3;
        .element-label {
          text-decoration: line-through;
        }
      }
    }
    .new-task {
      cursor: pointer;
      color: #999999;
      padding-top: 10px;
      font-size: 13px;
      padding-left: 4px;
      .el-icon-plus {
        color: #3e84e9;
        font-weight: 700;
      }
    }
  }
}

.sortable-drag {
  background-color: white;
}

.wukong {
  font-size: 14px;
}

.item-label {
  display: inline-block;
  display: inline-block;
  height: 20px;
  line-height: 20px;
  padding: 0 10px;
  border-radius: 3px;
  color: #fff;
  margin-right: 6px;
  font-size: 12px;
  margin-bottom: 6px;
}

.color-label-more {
  background: #eee;
  width: 34px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  display: inline-block;
  font-size: inherit;
  font-weight: 700;
  border-radius: 3px;
  vertical-align: middle;
  position: relative;
  margin-bottom: 6px;
  i {
    position: absolute;
    left: 50%;
    line-height: 36px;
    top: 0%;
    height: 20px;
    transform: translate(-50%, -50%);
  }
}
</style>

