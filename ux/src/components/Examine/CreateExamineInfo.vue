<template>
  <div>
    <el-form
      v-if="examineInfo.examineType == 2"
      ref="form"
      :model="form"
      :rules="rules"
      label-position="top"
      class="crm-create-box">
      <el-form-item
        prop="name"
        class="crm-create-item">
        <div
          slot="label"
          style="display: inline-block;">
          <div style="margin:5px 0;font-size:12px;word-wrap:break-word;word-break:break-all;">
            审核人
            <span style="color:#999;"/>
          </div>
        </div>
        <xh-user-cell
          :info-type="types"
          :value="draftUser ? [draftUser] : []"
          @value-change="fieldValueChange"/>
      </el-form-item>
    </el-form>
    <flexbox
      v-else-if="examineInfo.examineType == 1"
      class="fixed-examine"
      wrap="wrap">
      <el-popover
        v-for="(item, index) in examineInfo.examineSteps"
        :key="index"
        :disabled="item.userList.length==0"
        :content="item.userList|contentFilters"
        placement="bottom"
        trigger="hover">
        <div
          slot="reference"
          class="fixed-examine-item">
          <img src="@/assets/img/examine_head.png" >
          <div class="detail">{{ item|detail }}</div>
          <div class="step">{{ (index+1)|step }}</div>
        </div>
      </el-popover>
    </flexbox>

  </div>
</template>
<script type="text/javascript">
import { crmCreateExamineFlow } from '@/api/customermanagement/common'
import { oaCreateExamineFlow } from '@/api/oamanagement/examine'

import { XhUserCell } from '@/components/CreateCom'
import Nzhcn from 'nzh/cn'

export default {
  name: 'CreateExamineInfo',
  components: {
    XhUserCell
  },
  filters: {
    detail: function(data) {
      if (data.stepType == 2) {
        return data.userList.length + '人或签'
      } else if (data.stepType == 3) {
        return data.userList.length + '人会签'
      } else if (data.stepType == 1) {
        return '负责人主管'
      } else if (data.stepType == 4) {
        return '上一级审批人主管'
      }
    },
    step: function(index) {
      return '第' + Nzhcn.encodeS(index) + '级'
    },
    contentFilters: function(array) {
      return array
        .map(item => {
          return item.realname
        })
        .join('、')
    }
  },
  props: {
    // CRM类型
    types: {
      type: String,
      default: ''
    },
    // 办公审批 传ID
    typesId: {
      type: [String, Number],
      default: ''
    }
  },
  data() {
    return {
      form: {
        name: ''
      },
      rules: {
        name: [{ required: true, message: '审批人不能为空', trigger: 'blur' }]
      },
      // 审核信息 examineType 1 固定审批 2 授权审批
      examineInfo: {},
      draftUser: null
    }
  },
  computed: {},
  mounted() {
    this.getDetail()
  },
  methods: {
    getDetail() {
      const reqeust = {
        oa_examine: oaCreateExamineFlow,
        crm_contract: crmCreateExamineFlow,
        crm_receivables: crmCreateExamineFlow
      }[this.types]

      const params = {}
      if (this.types == 'oa_examine') {
        params.categoryId = this.typesId
      } else {
        params.id = this.typesId
        params.categoryType = this.types == 'crm_contract' ? 1 : 2 // 1 合同 2 回款
      }
      reqeust(params)
        .then(res => {
          this.examineInfo = res.data
          if (res.data.examineType == 2 && res.data.examineUser) {
            this.draftUser = {
              realname: res.data.examineUserName,
              userId: res.data.examineUser
            }
            this.form.name = res.data.examineUserName
            this.$emit('value-change', {
              examineType: res.data.examineType, // 审批类型
              value: [this.draftUser] // 审批信息
            })
          } else {
            this.form.name = ''
            this.draftUser = null
            this.$emit('value-change', {
              examineType: res.data.examineType, // 审批类型
              value: [] // 审批信息
            })
          }
        })
        .catch(() => {})
    },
    validateField(result) {
      if (this.examineInfo.examineType == 2) {
        // 授权审批人 需要验证关联人
        this.$refs.form.validate(valid => {
          if (valid) {
            result() // 成功回调
          } else {
            return false
          }
        })
      } else {
        result() // 成功回调
      }
    },
    // 字段的值更新
    fieldValueChange(data) {
      if (data.value.length) {
        this.draftUser = data.value[0]
        this.form.name = this.draftUser.userId
      } else {
        this.draftUser = null
        this.form.name = ''
      }
      this.$emit('value-change', {
        examineType: this.examineInfo.examineType, // 审批类型
        value: data.value // 审批信息
      })
      this.$refs.form.validateField('name')
    }
  }
}
</script>
<style lang="scss" scoped>
/** 将其改变为flex布局 */
.crm-create-box {
  display: flex;
  flex-wrap: wrap;
  padding: 0 10px;
}

.crm-create-item {
  flex: 0 0 50%;
  flex-shrink: 0;
  // overflow: hidden;
  padding-bottom: 10px;
}

.el-form-item /deep/ .el-form-item__label {
  line-height: normal;
  font-size: 13px;
  color: #333333;
  position: relative;
  padding-left: 8px;
  padding-bottom: 0;
}

.el-form /deep/ .el-form-item {
  margin-bottom: 0px;
}

.el-form /deep/ .el-form-item.is-required .el-form-item__label:before {
  content: '*';
  color: #f56c6c;
  margin-right: 4px;
  position: absolute;
  left: 0;
  top: 5px;
}

// 固定审批信息
.fixed-examine {
  padding: 0px 10px;
  text-align: center;
  .fixed-examine-item {
    padding: 10px 20px;
    img {
      width: 40px;
      height: 40px;
    }
    .detail {
      color: #777777;
      font-size: 12px;
      padding: 2px 0;
      transform: scale(0.8, 0.8);
    }
    .step {
      color: #333333;
      font-size: 12px;
    }
  }
}
</style>
