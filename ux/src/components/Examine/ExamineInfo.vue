<template>
  <div v-loading="loading"
       class="contract-flow-box">
    <flexbox direction="row-reverse"
             style="position:relative;">
      <el-popover v-model="showFlowPopover"
                  placement="bottom"
                  width="300"
                  trigger="click">
        <check-flow ref="checkFlow"
                    :id="recordId"
                    :examineType="examineType"
                    @close="showFlowPopover=false"></check-flow>
        <el-button slot="reference"
                   class="check-flow-button"
                   type="text">查看审批历史</el-button>
      </el-popover>
      <div style="min-height: 40px;">
        <el-button v-if="examineInfo.isRecheck==1"
                   @click="examineHandle(4)"
                   class="flow-button white">撤回审核</el-button>
        <el-button v-if="examineInfo.isCheck==1"
                   @click="examineHandle(2)"
                   class="flow-button red">拒绝</el-button>
        <el-button v-if="examineInfo.isCheck==1"
                   @click="examineHandle(1)"
                   class="flow-button blue">通过</el-button>
      </div>
    </flexbox>
    <flexbox v-if="examineInfo.examineType == 2"
             class="check-items">
      <flexbox class="check-item"
               v-for="(item, index) in examineInfo.steps"
               :key="index">
        <div>
          <flexbox class="check-item-user"
                   style="width:auto;">
            <div v-photo="item.examinUser"
                 v-lazy:background-image="$options.filters.filterUserLazyImg(item.examinUser.img)"
                 class="div-photo check-item-img"></div>
            <div class="check-item-name">{{item.examinUser.realname}}</div>
          </flexbox>
          <flexbox class="check-item-info">
            <img class="check-item-img"
                 :src="item.examineStatus|statusIcon">
            <div class="check-item-name">{{getStatusName(item.examineStatus)}}</div>
          </flexbox>
        </div>
        <i v-if="examineInfo.steps.length -1 != index"
           class="el-icon-arrow-right check-item-arrow"></i>
      </flexbox>
    </flexbox>
    <flexbox v-else-if="examineInfo.examineType == 1"
             class="check-items"
             wrap="wrap">
      <el-popover v-for="(item, index) in examineInfo.steps"
                  :key="index"
                  placement="bottom"
                  :disabled="!item.userList || item.userList.length==0"
                  trigger="hover">
        <div class="popover-detail">
          <flexbox v-for="(subItem, subIndex) in item.userList"
                   :key="subIndex"
                   align="stretch"
                   class="popover-detail-item">
            <img class="popover-detail-item-img"
                 :src="subItem.examineStatus|statusIcon">
            <div>
              <div class="popover-detail-item-time">{{subItem.examineTime}}</div>
              <flexbox class="popover-detail-item-examine">
                <div class="examine-name">{{subItem.realname}}</div>
                <div class="examine-info">{{getStatusName(subItem.examineStatus)}}此申请</div>
              </flexbox>
            </div>
          </flexbox>
        </div>
        <flexbox slot="reference"
                 class="fixed-examine-item">
          <div class="fixed-examine-info">
            <img src="@/assets/img/examine_head.png" />
            <div class="detail">{{item|detailName}}</div>
            <flexbox class="check-item-info">
              <img class="check-item-img"
                   :src="item.examineStatus|statusIcon">
              <div class="check-item-name">{{getStatusName(item.examineStatus)}}</div>
            </flexbox>
          </div>
          <i v-if="examineInfo.steps.length -1 != index"
             class="el-icon-arrow-right check-item-arrow"></i>
        </flexbox>
      </el-popover>
    </flexbox>
    <examine-handle :show="showExamineHandle"
                    @close="showExamineHandle = false"
                    @save="examineHandleClick"
                    :id="id"
                    :recordId="recordId"
                    :examineType="examineType"
                    :detail="examineInfo"
                    :status="examineHandleInfo.status"></examine-handle>
  </div>
</template>
<script type="text/javascript">
import { crmExamineFlowStepList } from '@/api/customermanagement/common' // 审批步骤
import { oaExamineFlowStepList } from '@/api/oamanagement/examine'

import Nzhcn from 'nzh/cn'
import ExamineHandle from './ExamineHandle' // 审批操作理由
import CheckFlow from './CheckFlow' // 审批流程

// 审核信息 config 1 固定 0 自选
export default {
  name: 'examine-info', // 合同审核操作
  components: {
    ExamineHandle,
    CheckFlow
  },
  computed: {},
  filters: {
    statusIcon: function(status) {
      // 0失败，1通过，2撤回，3创建，4待审核
      // JAVA 0 未审核 1 审核通过 2 审核拒绝 3 审核中 4 已撤回 5 创建
      if (status == 2) {
        return require('@/assets/img/check_fail.png')
      } else if (status == 1) {
        return require('@/assets/img/check_suc.png')
      } else if (status == 4) {
        return require('@/assets/img/check_revoke.png')
      } else if (status == 3) {
        return require('@/assets/img/check_create.png')
      } else if (status == 0) {
        return require('@/assets/img/check_wait.png')
      } else if (status == 5) {
        return require('@/assets/img/check_create.png')
      }
      return ''
    },
    detailName: function(data) {
      if (data.stepType == 2) {
        return data.userList.length + '人或签'
      } else if (data.stepType == 3) {
        return data.userList.length + '人会签'
      } else if (data.stepType == 1) {
        return '负责人主管'
      } else if (data.stepType == 4) {
        return '上一级审批人主管'
      } else if (data.stepType == 5) {
        return '创建人'
      }
    },
    stepName: function(index) {
      return '第' + Nzhcn.encodeS(index) + '级'
    }
  },
  watch: {
    recordId: {
      handler(val) {
        if (val) {
          this.examineInfo = {}
          this.getFlowStepList()
          if (this.$refs.checkFlow) {
            this.$refs.checkFlow.getDetail()
          }
        }
      },
      deep: true,
      immediate: true
    }
  },
  data() {
    return {
      loading: false,
      examineInfo: {}, //审核信息
      showFlowPopover: false,
      examineHandleInfo: { status: 1 }, // 1 审核通过 2 审核拒绝 4 已撤回
      showExamineHandle: false // 审核操作
    }
  },
  props: {
    examineType: {
      type: String,
      default: ''
    },
    // 详情信息id
    id: [String, Number],
    // 审批流id
    recordId: [String, Number],
    ownerUserId: [String, Number]
  },
  mounted() {},
  methods: {
    getFlowStepList() {
      if (!this.recordId || !this.id) {
        return
      }
      this.loading = true
      let request = {
        crm_contract: crmExamineFlowStepList,
        crm_receivables: crmExamineFlowStepList,
        oa_examine: oaExamineFlowStepList
      }[this.examineType]

      request({
        recordId: this.recordId,
        ownerUserId: this.ownerUserId
      })
        .then(res => {
          this.loading = false
          this.examineInfo = res.data
          this.$emit('value-change', {
            config: res.data.examineType, // 审批类型
            value: [] // 审批信息
          })
        })
        .catch(() => {
          this.loading = false
        })
    },
    // 撤回审核 通过 拒绝
    examineHandle(status) {
      this.examineHandleInfo.status = status
      this.showExamineHandle = true
    },
    // 获取状态名称
    getStatusName(status) {
      // 0拒绝，1通过，2撤回，3创建，4待审核
      // if (status == 0) {
      //   return '拒绝'
      // } else if (status == 1) {
      //   return '通过'
      // } else if (status == 2) {
      //   return '撤回'
      // } else if (status == 3) {
      //   return '创建'
      // } else if (status == 4) {
      //   return '待审核'
      // } else if (status == 5) {
      //   return '审核中'
      // }
      if (status == 0) {
        return '未审核'
      } else if (status == 1) {
        return '通过'
      } else if (status == 2) {
        return '拒绝'
      } else if (status == 3) {
        return '审核中'
      } else if (status == 4) {
        return '撤回'
      } else if (status == 5) {
        return '创建'
      }
      return ''
    },
    getContentFilters(array) {
      var content = ''
      for (let index = 0; index < array.length; index++) {
        const item = array[index]
        if (index == array.length - 1) {
          content =
            content + item.realname + '：' + this.getStatusName(item.checkType)
        } else {
          content =
            content +
            item.realname +
            '：' +
            this.getStatusName(item.checkType) +
            '、'
        }
      }
      return content
    },
    // 审批操作点击
    examineHandleClick(data) {
      this.getFlowStepList()
      if (this.$refs.checkFlow) {
        this.$refs.checkFlow.getDetail()
      }
      this.$emit('on-handle', data)
    }
  }
}
</script>
<style lang="scss" scoped>
.contract-flow-box {
  position: relative;
  min-height: 100px;
}
.flow-button {
  width: 75px;
  height: 30px;
  border-radius: 2px;
  margin: 5px;
  font-size: 13px;
}
.blue {
  background-color: #3e84e9;
  color: white;
  border: none;
}
.red {
  background-color: #ff6767;
  color: white;
  border: none;
}
.white {
  background-color: white;
  color: #777;
}
.check-flow-button {
  font-size: 13px;
  position: absolute;
  left: 0;
  top: 5px;
}

/** 审核流程 */
.check-items {
  overflow-x: auto;
  padding: 10px 0;
}

.check-item {
  width: auto;
  flex-shrink: 0;
  .check-item-user {
    width: auto;
    .check-item-img {
      display: block;
      width: 40px;
      height: 40px;
      border-radius: 20px;
      margin-right: 8px;
    }
    .check-item-name {
      color: #333;
      font-size: 15px;
      font-weight: 500;
    }
  }
  .check-item-info {
    width: auto;
    margin-top: 5px;
    .check-item-img {
      display: block;
      width: 16px;
      height: 16px;
      border-radius: 8px;
      margin-right: 8px;
    }
    .check-item-name {
      color: #aaa;
      font-size: 12px;
    }
  }
  .check-item-arrow {
    color: #ccc;
    margin: 0 16px;
    font-size: 20px;
    font-weight: 600;
  }
}

// 固定审批信息
.fixed-examine-item {
  width: auto;
  flex-shrink: 0;

  .fixed-examine-info {
    padding: 10px 20px;
    text-align: center;
    img {
      width: 40px;
      height: 40px;
    }
    .detail {
      color: #777777;
      font-size: 12px;
      padding: 2px 0;
      transform: scale(0.9, 0.9);
    }
    .check-item-info {
      width: auto;
      margin-top: 5px;
      .check-item-img {
        display: block;
        width: 16px;
        height: 16px;
        border-radius: 8px;
        margin-right: 8px;
      }
      .check-item-name {
        color: #aaa;
        font-size: 12px;
      }
    }
  }

  .check-item-arrow {
    color: #ccc;
    margin: 0 16px;
    font-size: 20px;
    font-weight: 600;
  }
}

.popover-detail {
  padding: 0 5px;
}
.popover-detail-item {
  padding: 5px 0;
  &-img {
    display: block;
    width: 16px;
    height: 16px;
    border-radius: 8px;
    margin-right: 10px;
  }
  &-time {
    color: #bababa;
    font-size: 12px;
  }
  &-examine {
    .examine-name {
      color: #333;
      margin-right: 10px;
    }
    .examine-info {
      color: #666;
    }
  }
}
</style>
