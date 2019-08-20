<template>
  <div class="my-task-dialog">
    <el-dialog title="新建任务"
               :visible.sync="newDialogVisible"
               width="700px"
               :show-close="false"
               v-loading="newLoading"
               :close-on-click-modal="false"
               :before-close="handleClose">
      <img class="el-icon-close"
           src="@/assets/img/task_close.png"
           @click="handleClose"
           alt="">
      <el-form :model="formInline"
               ref="form"
               :rules="rules">
        <el-form-item :class="'el-form-item'+ '-' + item.field"
                      :label="item.label"
                      :prop="item.field"
                      v-for="(item, index) in formList"
                      :key="index">
          <el-input v-if="item.type == 'textarea'"
                    :autosize="{ minRows: 4}"
                    v-model="formInline[item.field]"
                    type="textarea"></el-input>
          <el-date-picker v-else-if="item.type == 'time'"
                          v-model="formInline[item.field]"
                          type="date"
                          value-format="yyyy-MM-dd"
                          placeholder="选择日期">
          </el-date-picker>
          <div v-else-if="item.type == 'priority'"
               class="priority-box">
            <el-radio-group v-model="formInline[item.field]"
                            fill="red"
                            text-color="#FFF">
              <el-radio :label="3">高</el-radio>
              <el-radio :label="2">中</el-radio>
              <el-radio :label="1">低</el-radio>
              <el-radio :label="0">无</el-radio>
            </el-radio-group>
          </div>
          <div v-else-if="item.type == 'popover'"
               class="type-popover">
            <el-popover placement="bottom-end"
                        width="280"
                        trigger="click">
              <xh-user ref="xhuser"
                       :radio="radio"
                       :selectedData="colleaguesList"
                       @changeCheckout="changeCheckout">
              </xh-user>
              <div class="select-box"
                   slot="reference">
                <span v-for="(item, index) in colleaguesList"
                      :key="index"
                      class="select-box-span">
                  {{item.realname}}
                  <span class="el-icon-close"
                        @click.stop="selectDelect(item, index)"> </span>
                </span>
                <span class="el-icon-plus"></span>
              </div>
            </el-popover>
          </div>
          <el-input v-else
                    v-model="formInline[item.field]"></el-input>
        </el-form-item>
        <related-business :allData="allData"
                          :marginLeft="'0'"
                          @checkInfos="checkInfos">
        </related-business>
      </el-form>
      <span slot="footer"
            class="dialog-footer">
        <el-button type="primary"
                   @click="dialogVisibleSubmit">保存</el-button>
        <el-button @click="handleClose">取消</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { usersList, depList } from '@/api/common'
// 关联业务 - 选中列表
import relatedBusiness from '@/components/relatedBusiness'
import XhUser from '@/components/CreateCom/XhUser'

export default {
  components: {
    relatedBusiness,
    XhUser
  },
  data() {
    return {
      formInline: {
        priority: 0
      },
      formList: [
        { label: '任务名称', field: 'name' },
        { label: '负责人', field: 'mainUserId', type: 'popover' },
        { label: '开始时间', field: 'startTime', type: 'time' },
        { label: '结束时间', field: 'stopTime', type: 'time' },
        { label: '优先级', field: 'priority', type: 'priority' },
        { label: '任务描述', field: 'description', type: 'textarea' }
      ],
      // 上传附件
      uploadFile: [],
      // 负责人弹出框
      colleaguesList: [],
      popoverModel: false,
      rules: {
        name: [
          { required: true, message: '任务名称不能为空', trigger: 'blur' },
          { max: 50, message: '任务名称长度最多为50个字符', trigger: 'blur' }
        ]
      },
      // 获取选择的数据id数组
      relevanceAll: {},
      allData: {},
      radio: true
    }
  },
  watch: {
    newDialogVisible(value) {
      if (!value) {
        this.formInline = {
          priority: 0
        }
      }
    }
  },
  props: {
    newDialogVisible: Boolean,
    newLoading: Boolean
  },
  created() {},
  methods: {
    handleClose() {
      this.$emit('handleClose')
    },
    dialogVisibleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          var formInlineCopy = Object.assign({}, this.formInline)
          formInlineCopy = {
            mainUserId:
              this.colleaguesList.length == 0
                ? ''
                : this.colleaguesList[0].userId,
            startTime: this.formInline.startTime,
            stopTime: this.formInline.stopTime,
            description: this.formInline.description,
            priority: this.formInline.priority,
            name: this.formInline.name,
            customerIds:
              this.relevanceAll.customerIds &&
              this.relevanceAll.customerIds.length
                ? ',' + this.relevanceAll.customerIds.join(',') + ','
                : '',
            contactsIds:
              this.relevanceAll.contactsIds &&
              this.relevanceAll.contactsIds.length
                ? ',' + this.relevanceAll.contactsIds.join(',') + ','
                : '',
            businessIds:
              this.relevanceAll.businessIds &&
              this.relevanceAll.businessIds.length
                ? ',' + this.relevanceAll.businessIds.join(',') + ','
                : '',
            contractIds:
              this.relevanceAll.contractIds &&
              this.relevanceAll.contractIds.length
                ? ',' + this.relevanceAll.contractIds.join(',') + ','
                : ''
          }
          this.$emit('dialogVisibleSubmit', formInlineCopy)
        } else {
          return false
        }
      })
    },
    checkInfos(val) {
      this.relevanceAll = val
    },
    changeCheckout(data) {
      this.colleaguesList = data.data
    },
    // 删除选择员工
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
      .el-form-item-mainUserId,
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
