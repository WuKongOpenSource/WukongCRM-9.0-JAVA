<template>
  <el-dialog
    v-loading="loading"
    :title="title"
    :append-to-body="true"
    :visible.sync="showDialog"
    width="400px"
    @close="hiddenView">
    <div
      v-if="status == 1 && detail.examineType == 2"
      class="handle-type">
      <flexbox class="handle-item">
        <el-radio
          v-model="handleType"
          :label="1"><span/></el-radio>
        <div
          style="font-size: 12px;"
          @click.native="handleType=1">审核结束</div>
      </flexbox>
      <flexbox
        id="selectUser"
        class="handle-item">
        <el-radio
          v-model="handleType"
          :label="2"><span/></el-radio>
        <xh-user-cell
          class="select-user"
          placeholder="选择下一审批人"
          @focus="selectUserFocus"
          @value-change="selectExamineUser"/>
      </flexbox>
    </div>
    <div
      v-if="status == 1 && detail.examineType == 2"
      class="title">意见</div>
    <el-input
      v-model="content"
      :rows="5"
      :maxlength="200"
      :placeholder="placeholder"
      type="textarea"
      resize="none"
      show-word-limit/>
    <div
      slot="footer"
      class="dialog-footer">
      <el-button @click="handleClick('cancel')">取 消</el-button>
      <el-button
        type="primary"
        @click="handleClick('confirm')">确 定</el-button>
    </div>
  </el-dialog>
</template>
<script type="text/javascript">
import { crmExamineFlowAuditExamine } from '@/api/customermanagement/common'
import { oaExamineFlowAuditExamine } from '@/api/oamanagement/examine'

import { XhUserCell } from '@/components/CreateCom'

export default {
  name: 'ExamineHandle', // 合同审核操作
  components: {
    XhUserCell
  },
  props: {
    show: {
      type: Boolean,
      default: false
    }, // 审核状态 1 审核通过 2 审核拒绝 4 已撤回
    /** 操作类型  1通过0拒绝2撤回*/ status: {
      type: [String, Number],
      default: 1
    },
    // 详情信息id
    id: [String, Number],
    recordId: [String, Number],
    // 审核信息 config 1 固定 0 自选
    detail: {
      type: Object,
      default: () => {
        return {}
      }
    },
    // crm_contract crm_receivables oa_examine
    examineType: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      loading: false,
      showDialog: false,
      handleType: 1,
      selectUsers: [],
      content: '' // 输入内容
    }
  },
  computed: {
    title() {
      if (this.status == 1) {
        return '审批通过'
      } else if (this.status == 2) {
        return '审批拒绝'
      } else if (this.status == 4) {
        return '撤回审批'
      }
      return ''
    },
    placeholder() {
      // 1 审核通过 2 审核拒绝 4 已撤回
      if (this.status == 1) {
        return '请输入审批意见（选填）'
      } else if (this.status == 2) {
        return '请输入审批意见（必填）'
      } else if (this.status == 4) {
        return '请输入撤回理由（必填）'
      }
      return ''
    }
  },
  watch: {
    show: {
      handler(val) {
        this.showDialog = val
      },
      deep: true,
      immediate: true
    }
  },
  mounted() {},
  methods: {
    submitInfo() {
      if ((this.status == 2 || this.status == 4) && !this.content) {
        this.$message.error(this.placeholder)
      } else {
        if (this.status == 2 || this.status == 1) {
          // 1通过0拒绝2撤回
          this.handlePassAndReject()
        } else if (this.status == 4) {
          this.handleRevoke()
        }
      }
    },
    // 撤回操作
    handleRevoke() {
      this.loading = true
      this.getRequest()({
        id: this.id,
        recordId: this.recordId,
        status: this.status,
        remarks: this.content
      })
        .then(res => {
          this.loading = false
          this.$message.success('操作成功')
          this.$emit('save')
          this.$bus.emit('examine-handle-bus')
          this.$store.dispatch('GetMessageNum')
          this.hiddenView()
        })
        .catch(() => {
          this.loading = false
        })
    },
    getRequest() {
      return {
        crm_contract: crmExamineFlowAuditExamine,
        crm_receivables: crmExamineFlowAuditExamine,
        oa_examine: oaExamineFlowAuditExamine
      }[this.examineType]
    },
    // 通过 拒绝操作
    handlePassAndReject() {
      this.loading = true
      var params = {
        id: this.id,
        recordId: this.recordId,
        status: this.status,
        remarks: this.content
      }
      if (this.status == 1 && this.detail.examineType == 2) {
        if (this.handleType != 1) {
          params['nextUserId'] = this.selectUsers[0].userId
        }
      }
      this.getRequest()(params)
        .then(res => {
          this.loading = false
          this.$message.success('操作成功')
          this.$emit('save', { type: this.status })
          this.hiddenView()
          this.$bus.emit('examine-handle-bus')
          this.$store.dispatch('GetMessageNum')
        })
        .catch(() => {
          this.loading = false
        })
    },
    handleClick(type) {
      if (type == 'cancel') {
        this.hiddenView()
      } else if (type == 'confirm') {
        this.submitInfo()
      }
    },
    /** 选择了下一审批人 */
    selectUserFocus() {
      this.handleType = 2
    },
    selectExamineUser(data) {
      this.selectUsers = data.value
    },
    hiddenView() {
      this.$emit('close')
    }
  }
}
</script>
<style lang="scss" scoped>
.handle-type {
  padding-bottom: 8px;
  .handle-item {
    padding: 8px 0;
    cursor: pointer;
  }
}

.el-dialog__wrapper /deep/ .el-dialog__body {
  padding: 10px 25px 20px;
}

.el-radio-group /deep/ .el-radio + .el-radio {
  margin-left: 0px;
}

.select-user {
  flex: 1;
}

.title {
  font-size: 12px;
  padding-bottom: 8px;
  color: #666;
}
</style>
