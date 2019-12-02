<template>
  <div class="task-sub">
    <div
      v-clickoutside="subtasksSubmit"
      class="subtasks-content">
      <div class="checkbox-box subtasks-box">
        <el-checkbox
          v-model="checkboxData"
          disabled/>
      </div>
      <div class="subtasks-content-box">
        <el-input
          :maxlength="50"
          v-model="subtasksTextarea"
          type="textarea"
          autosize
          placeholder="请输入内容"
          @keyup.enter.native="subtasksSubmit"/>
      </div>
      <flexbox class="subtasks-content-png">
        <div class="time">
          <el-date-picker
            ref="subtasksDate"
            v-model="subtasksDate"
            :style="{'width': subtasksDataText ? '54px': '28px'}"
            type="date"
            format="yyyy 年 MM 月 dd 日"
            value-format="yyyy-MM-dd"
            placeholder="选择日期"
            @change="subtasksDateClick(subtasksDate)"/>
          <span
            v-if="subtasksDataText && subtasksDataText != 0"
            class="bg-color">{{ subtasksDataText | moment("MM-DD") }}</span>
          <i
            v-else
            class="wukong wukong-time-task"
            @click="subtasksDateFun"/>
        </div>
        <!-- 选择负责人 -->
        <el-popover
          placement="bottom-end"
          width="280"
          trigger="click">
          <xh-user
            ref="xhuser"
            :selected-data="xhUserData"
            radio
            @changeCheckout="xhUserCheckout"/>
          <div
            slot="reference"
            class="select-box">
            <template v-if="subtaskChange">
              <span
                v-for="(item, index) in xhUserData"
                :key="index">
                <el-tooltip
                  placement="bottom"
                  effect="light"
                  popper-class="tooltip-change-border">
                  <div slot="content">
                    <span>{{ item.realname }}</span>
                  </div>
                  <div
                    v-photo="item"
                    v-lazy:background-image="$options.filters.filterUserLazyImg(item.img)"
                    class="div-photo"/>
                </el-tooltip>
              </span>
            </template>
            <i
              v-else
              class="wukong wukong-user"/>
          </div>
        </el-popover>
      </flexbox>
    </div>
  </div>
</template>

<script>
import { addTask, editTask } from '@/api/oamanagement/task'
import XhUser from '@/components/CreateCom/XhUser'

export default {
  components: {
    XhUser
  },
  props: {
    taskData: {
      type: Object,
      default: () => {
        return {}
      }
    },
    // 子任务输入内容
    text: String,
    time: [String, Number],
    subData: Object,
    subTaskCom: {
      type: String,
      default: ''
    },
    taskId: [Number, String],
    checkboxData: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      // 子任务选择时间
      subtasksDate: '',
      // 子任务数据
      xhUserData: [],
      subtasksDataText: null,
      // 子任务人员
      subtaskChange: false,
      num: 0,
      subtasksTextarea: '',
      isNum: 0
    }
  },
  watch: {},
  created() {
    if (this.subTaskCom == 'edit') {
      this.subtasksTextarea = this.text ? this.text : ''
      this.subtasksDataText = this.time
      if (JSON.stringify(this.subData) !== '{}') {
        if (this.subData.mainUser) {
          this.subtaskChange = true
          this.xhUserData = [this.subData.mainUser]
        }
      } else {
        this.xhUserData = []
        this.subtaskChange = false
      }
    }
  },
  methods: {
    // 子任务 -- 选中时间
    subtasksDateClick(val) {
      this.subtasksDataText = val.substring(5)
    },
    subtasksSubmit(event) {
      this.subtasksTextarea = this.subtasksTextarea.split(/[\n]/).join('')
      if (this.subtasksTextarea && this.num == 0) {
        this.num = 1
        if (this.subTaskCom == 'new') {
          this.$emit('on-handle', { type: 'add', result: 'success' })
          addTask({
            pid: this.taskData.taskId,
            name: this.subtasksTextarea,
            stopTime: this.subtasksDate,
            mainUserId:
              this.xhUserData.length != 0 ? this.xhUserData[0].userId : ''
          })
            .then(res => {
              this.taskData.childTask.push({
                name: this.subtasksTextarea,
                stopTime: this.subtasksDate,
                taskId: res.data.taskId,
                mainUser: this.xhUserData.length > 0 ? this.xhUserData[0] : null
              })
              this.$message.success('子任务创建成功')
              // 创建成功 -- 清除选择
              this.subtasksTextarea = ''
              this.subtasksDataText = null
              this.subtaskChange = false
              this.num = 0
            })
            .catch(() => {
              this.$emit('on-handle', { type: 'add', result: 'error' })
              this.$message.error('子任务创建失败')
              this.num = 0
            })
        } else if (this.subTaskCom == 'edit') {
          if (
            this.isNum == 1 ||
            this.text != this.subtasksTextarea ||
            this.subtasksDataText != this.time
          ) {
            this.$emit('on-handle', { type: 'edit', result: 'success' })
            editTask({
              taskId: this.taskId,
              stopTime: this.subtasksDate,
              mainUserId:
                this.xhUserData.length > 0 ? this.xhUserData[0].id : '',
              name: this.subtasksTextarea
            })
              .then(res => {
                const dataList = this.taskData.childTask
                for (const i in dataList) {
                  if (dataList[i].taskId == this.taskId) {
                    const list = dataList[i]
                    list.name = this.subtasksTextarea
                    list.stopTime = this.subtasksDate
                    list.mainUser =
                      this.xhUserData.length > 0 ? this.xhUserData[0] : null
                    dataList.splice(i, 1, list)
                    break
                  }
                }
                this.num = 0
                this.$message.success('子任务编辑成功')
              })
              .catch(() => {
                this.$emit('on-handle', { type: 'edit', result: 'error' })
                this.$message.error('子任务编辑失败')
                this.num = 0
              })
          } else {
            this.$emit('on-handle', { type: 'cancel' })
          }
        }
      } else {
        this.$emit('on-handle', { type: 'cancel' })
      }
    },
    xhUserCheckout(data) {
      if (data.data.length != 0) {
        this.subtaskChange = true
        if (this.xhUserData.length != 0) {
          this.$set(this.xhUserData[0], 'img', data.data[0].img)
          this.$set(this.xhUserData[0], 'realname', data.data[0].realname)
        } else {
          this.xhUserData = data.data
        }
        this.isNum = 1
      } else {
        this.isNum = 0
        this.subtaskChange = false
      }
    },
    // 子任务 -- 时间弹框
    subtasksDateFun() {
      this.$refs.subtasksDate.change()
    }
  }
}
</script>

<style scoped lang="scss">
.wukong {
  color: #666;
}
.subtasks-content {
  border: 1px solid #3e84e9;
  border-radius: 3px;
  display: flex;
  flex-direction: row;
  align-items: center;
  // margin-top: 10px;
  height: 40px;
  .subtasks-box {
    line-height: 40px;
  }
  .subtasks-content-box {
    flex: 1;
    .el-textarea /deep/ .el-textarea__inner {
      border-width: 0;
      resize: none;
    }
  }
  .subtasks-content-png {
    width: auto;
    img {
      margin-right: 10px;
      cursor: pointer;
      vertical-align: middle;
    }
    .time {
      display: inline-block;
      position: relative;
      vertical-align: middle;
      .el-date-editor {
        position: absolute;
        right: 0;
        width: 28px;
        opacity: 0;
        bottom: 0;
        top: 0;
      }
      .el-date-editor /deep/ .el-input__inner {
        padding: 0;
        height: 100%;
      }
      .el-date-editor /deep/ .el-input__suffix {
        display: none;
      }
      .el-date-editor /deep/ .el-input__prefix {
        left: 0;
        right: 0;
      }
      .bg-color {
        padding: 2px 10px;
      }
      .wukong-time-task {
        margin-right: 10px;
        cursor: pointer;
        vertical-align: middle;
      }
    }
    .select-box {
      display: inline-block;
      .div-photo {
        width: 24px;
        height: 24px;
        border-radius: 12px;
      }
      .wukong-user {
        margin-right: 10px;
        cursor: pointer;
        vertical-align: middle;
      }
    }
  }
  .checkbox-box {
    margin-left: 10px;
  }
  .subtask-popover-span {
    padding: 0 10px 0 5px;
  }
}
</style>
