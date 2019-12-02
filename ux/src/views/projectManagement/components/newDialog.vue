<template>
  <div class="my-task-dialog">
    <el-dialog
      v-loading="loading"
      :visible.sync="visible"
      :show-close="false"
      :close-on-click-modal="false"
      :before-close="handleClose"
      title="新建任务"
      width="700px">
      <img
        class="el-icon-close"
        src="@/assets/img/project/task_close.png"
        alt=""
        @click="handleClose">
      <el-form
        ref="form"
        :model="formData"
        :rules="rules">
        <el-form-item
          v-for="(item, index) in formList"
          :class="'el-form-item'+ '-' + item.field"
          :label="item.label"
          :prop="item.field"
          :key="index">
          <el-input
            v-if="item.type == 'textarea'"
            :autosize="{ minRows: 4}"
            v-model="formData[item.field]"
            type="textarea"/>
          <el-date-picker
            v-else-if="item.type == 'time'"
            v-model="formData[item.field]"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="选择日期"/>
          <div
            v-else-if="item.type == 'priority'"
            class="priority-box">
            <el-radio-group
              v-model="formData[item.field]"
              fill="red"
              text-color="#FFF">
              <el-radio :label="3">高</el-radio>
              <el-radio :label="2">中</el-radio>
              <el-radio :label="1">低</el-radio>
              <el-radio :label="0">无</el-radio>
            </el-radio-group>
          </div>
          <div
            v-else-if="item.type == 'popover'"
            class="type-popover">
            <el-popover
              placement="bottom-end"
              width="280"
              trigger="click">
              <xh-user
                ref="xhuser"
                :radio="radio"
                :selected-data="colleaguesList"
                @changeCheckout="changeCheckout"/>
              <div
                slot="reference"
                class="select-box">
                <span
                  v-for="(item, index) in colleaguesList"
                  :key="index"
                  class="select-box-span">
                  {{ item.realname }}
                  <span
                    class="el-icon-close"
                    @click.stop="selectDelect(item, index)"/>
                </span>
                <span class="el-icon-plus"/>
              </div>
            </el-popover>
          </div>
          <el-input
            v-else
            v-model="formData[item.field]"/>
        </el-form-item>
        <related-business
          :all-data="allData"
          :margin-left="'0'"
          @checkInfos="checkInfos"/>
      </el-form>
      <span
        slot="footer"
        class="dialog-footer">
        <el-button
          type="primary"
          @click="dialogVisibleSubmit">保存</el-button>
        <el-button @click="handleClose">取消</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
// 关联业务 - 选中列表
import relatedBusiness from '@/components/relatedBusiness'
import XhUser from '@/components/CreateCom/XhUser'
import { workTaskSaveAPI } from '@/api/projectManagement/task'
import { formatTimeToTimestamp } from '@/utils/index'

export default {
  components: {
    relatedBusiness,
    XhUser
  },

  props: {
    visible: Boolean,
    params: Object, // 附加参数
    action: {
      type: Object,
      default: () => {
        return {
          type: 'create'
        }
      }
    }
  },

  data() {
    var validateTime = (rule, value, callback) => {
      if (this.formData.startTime && this.formData.stopTime) {
        if (
          formatTimeToTimestamp(this.formData.startTime) >
          formatTimeToTimestamp(this.formData.stopTime)
        ) {
          callback(new Error('开始时间必须小于结束时间'))
        }
      }
      callback()
    }
    return {
      loading: false,
      formData: {
        priority: 0
      },
      formList: [
        { label: '任务名称', field: 'name' },
        { label: '负责人', field: 'mainUserName', type: 'popover' },
        { label: '开始时间', field: 'startTime', type: 'time' },
        { label: '结束时间', field: 'stopTime', type: 'time' },
        { label: '优先级', field: 'priority', type: 'priority' },
        { label: '任务描述', field: 'description', type: 'textarea' }
      ],
      // 负责人弹出框
      colleaguesList: [],
      popoverModel: false,
      rules: {
        name: [
          { required: true, message: '任务名称不能为空', trigger: 'blur' },
          { max: 50, message: '任务名称长度最多为50个字符', trigger: 'blur' }
        ],
        startTime: [{ validator: validateTime, trigger: 'blur' }],
        stopTime: [{ validator: validateTime, trigger: 'blur' }]
      },
      // 获取选择的数据id数组
      relevanceAll: {},
      allData: {},
      radio: true
    }
  },

  watch: {
    visible(value) {
      if (value) {
        if (this.action.type == 'create') {
          if (this.action.data) {
            this.formData = Object.assign(
              {
                priority: 0
              },
              this.action.data
            )
          } else {
            this.formData = {
              priority: 0
            }
            this.colleaguesList = []
            this.relevanceAll = {}
            this.allData = {}
          }
        }
      }
    }
  },

  methods: {
    /**
     * 关闭窗口
     */
    handleClose() {
      this.formData = {
        priority: 0
      }
      this.$emit('handleClose')
    },

    /**
     * 保存
     */
    dialogVisibleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          var formDataCopy = Object.assign({}, this.formData)
          formDataCopy = {
            mainUserId:
              this.colleaguesList.length == 0
                ? ''
                : this.colleaguesList[0].userId,
            startTime: this.formData.startTime || '',
            stopTime: this.formData.stopTime || '',
            description: this.formData.description,
            priority: this.formData.priority,
            name: this.formData.name,
            customerIds: this.relevanceAll.customerIds
              ? ',' + this.relevanceAll.customerIds.join(',') + ','
              : '',
            contactsIds: this.relevanceAll.contactsIds
              ? ',' + this.relevanceAll.contactsIds.join(',') + ','
              : '',
            businessIds: this.relevanceAll.businessIds
              ? ',' + this.relevanceAll.businessIds.join(',') + ','
              : '',
            contractIds: this.relevanceAll.contractIds
              ? ',' + this.relevanceAll.contractIds.join(',') + ','
              : ''
          }

          if (this.params) {
            Object.assign(formDataCopy, this.params)
          }

          this.loading = true
          workTaskSaveAPI(formDataCopy)
            .then(res => {
              this.$message.success('创建成功')
              this.loading = false
              this.handleClose()
              this.$emit('submit')
            })
            .catch(() => {
              this.loading = false
            })
        } else {
          return false
        }
      })
    },

    /**
     * 选择相关
     */
    checkInfos(val) {
      this.relevanceAll = val
    },

    /**
     * 选择人员
     */
    changeCheckout(data) {
      this.colleaguesList = data.data
    },

    /**
     * 删除人员
     */
    selectDelect(value, index) {
      if (this.radio) {
        // 如果单选告知删除
        this.$refs.xhuser[0].changeCheckout([])
      }
      this.colleaguesList.splice(index, 1)
    }
  }
}
</script>
<style scoped lang="scss">
.my-task-dialog /deep/ .el-dialog {
  padding: 20px;
  .el-dialog__header {
    padding: 0 0 0 10px;
    .el-dialog__title {
      display: inline-block;
      height: 40px;
      line-height: 40px;
      font-size: 17px;
    }
    .el-dialog__headerbtn {
      display: none;
    }
  }
  .el-dialog__body {
    img.el-icon-close {
      position: absolute;
      top: 20px;
      right: 20px;
      width: 40px;
      height: 40px;
      padding: 10px;
    }
    .el-form {
      overflow: auto;
      flex: 1;
      padding: 20px;
      .el-date-editor {
        vertical-align: bottom;
      }
      .el-radio-group {
        vertical-align: super;
      }
      .el-textarea {
        margin-top: 7px;
      }
      .el-form-item-name,
      .el-form-item-startTime {
        padding-right: 25px;
      }
      .el-form-item-mainUserName,
      .el-form-item-stopTime {
        padding-left: 25px;
      }
      .el-form-item-priority,
      .el-form-item-description,
      .el-form-item-time {
        width: 100%;
      }
    }
  }
}
</style>

<style lang="scss" scoped>
.my-task-dialog /deep/ .el-dialog__wrapper {
  background: #f5f6f9;
  padding: 40px 0;
  overflow: hidden;
}
.my-task-dialog /deep/ .el-dialog {
  height: 100%;
  margin-top: 0 !important;
  /* margin-bottom: 40px; */
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.my-task-dialog /deep/ .el-dialog .el-dialog__body {
  padding: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.my-task-dialog /deep/ .el-dialog .el-dialog__footer {
  margin-right: 20px;
}
.colleagues-list p {
  height: 34px;
  line-height: 34px;
  padding-left: 20px;
  cursor: pointer;
  margin: 0 -12px;
}
.colleagues-list p:hover {
  -webkit-box-shadow: 0 0 8px 2px #eee;
  box-shadow: 0 0 8px 2px #eee;
  background: 0 0;
}
.el-form /deep/ .el-form-item {
  margin-bottom: 10px;
  padding-bottom: 6px;
  width: 50%;
  display: inline-block;
}
.my-task-dialog /deep/ .el-form-item__content > .el-date-editor {
  width: 100%;
}
.my-task-dialog /deep/ .el-form-item__content .el-input .el-input__inner {
  vertical-align: middle;
}
.my-task-dialog /deep/ .el-form-item__content .el-select {
  width: 100%;
}
.my-task-dialog /deep/ .el-form-item__content .el-textarea .el-textarea__inner {
  resize: none;
}
.select-box {
  border: 1px solid #e6e6e6;
  height: 34px;
  line-height: 34px;
  border-radius: 3px;
  cursor: pointer;
  display: inline-block;
  width: 100%;
  vertical-align: middle;
}

.select-box > .el-icon-plus {
  float: right;
  line-height: 34px;
  font-size: 16px;
  font-weight: 600;
  color: #aaa;
  padding-right: 10px;
}
.select-box > .select-box-span {
  background: #eff3fc;
  margin: 0 5px;
  height: 27px;
  line-height: 28px;
  font-size: 12px;
  color: #333;
  padding: 0 5px;
  display: inline-block;
}
.my-task-dialog /deep/ .el-form-item__label {
  display: block;
  width: 100%;
  text-align: left;
  font-size: 12px;
}
.my-task-dialog /deep/ .el-range-separator {
  width: auto;
}
.priority-box /deep/ .el-radio {
  margin-left: 0;
  width: 34px;
  height: 34px;
  line-height: 32px;
  margin-right: 14px;
  text-align: center;
}
.priority-box /deep/ .el-radio .el-radio__input {
  display: none;
}
.priority-box /deep/ .el-radio .el-radio__label {
  padding: 0;
  width: 100%;
  height: 100%;
  display: inline-block;
  text-align: center;
  border-radius: 50%;
  font-size: 12px;
  cursor: pointer;
  border: 1px solid #ccc;
  color: #cccccc;
}
.priority-box /deep/ .el-radio:nth-child(2) .el-radio__label {
  border: 1px solid #ff9668;
  color: #ff9668;
}
.priority-box /deep/ .el-radio:nth-child(3) .el-radio__label {
  border: 1px solid #8bb5f0;
  color: #8bb5f0;
}
.priority-box /deep/ .el-radio:nth-child(1) .el-radio__label {
  border: 1px solid #ed6363;
  color: #ed6363;
}
.priority-box
  /deep/
  .el-radio:nth-child(4)
  .el-radio__input.is-checked
  + .el-radio__label {
  color: #fff;
  background: #cccccc;
  border-color: #cccccc;
}
.priority-box
  /deep/
  .el-radio:nth-child(2)
  .el-radio__input.is-checked
  + .el-radio__label {
  color: #fff;
  background: #ff9668;
  border-color: #ff9668;
}
.priority-box
  /deep/
  .el-radio:nth-child(3)
  .el-radio__input.is-checked
  + .el-radio__label {
  color: #fff;
  background: #8bb5f0;
  border-color: #8bb5f0;
}
.priority-box
  /deep/
  .el-radio:nth-child(1)
  .el-radio__input.is-checked
  + .el-radio__label {
  color: #fff;
  background: #ed6363;
  border-color: #ed6363;
}
.upload-demo {
  display: inline-block;
}
.img-text-color {
  margin-top: 20px;
  color: #3e84e9;
}
.img-center {
  vertical-align: middle;
}
</style>
