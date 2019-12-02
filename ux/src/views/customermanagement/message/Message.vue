<template>
  <div style="height:100%;">
    <div class="message-header">
      待办事项
    </div>
    <div class="message-body">
      <div class="message-content">
        <div class="message-body-side">
          <div
            v-for="(item, index) in leftSides"
            v-if="!item.hidden"
            :key="index"
            :class="leftType==item.infoType? 'side-item-select' : 'side-item-default'"
            class="side-item"
            @click="sideClick(item)">
            {{ item.name }}
            <el-badge
              v-if="item.num > 0"
              :max="99"
              :value="item.num"/>
          </div>
        </div>
        <div class="message-body-content">
          <c-r-m-message
            v-for="(item, index) in leftSides"
            v-show="leftType==item.infoType"
            :key="index"
            :crm-type="item.crmType"
            :info-type="item.infoType"
            :info-title="item.name"
            :info-tips="item.tips"
            :show="leftType==item.infoType"
            @on-handle="messageHandle"/>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import CRMMessage from './compenents/CRMMessage' // 系统消息
import { mapGetters } from 'vuex'
import { objDeepCopy } from '@/utils'

export default {
  /** 客户管理 的 消息列表 */
  name: 'Message',

  components: {
    CRMMessage
  },

  data() {
    return {
      leftType: 'todayCustomer',
      leftSides: [
        {
          name: '今日需联系客户',
          crmType: 'customer',
          infoType: 'todayCustomer',
          num: 0,
          tips: '下次跟进时间为今日的客户',
          hidden: false
        },
        {
          name: '分配给我的线索',
          crmType: 'leads',
          infoType: 'followLeads',
          num: 0,
          tips: '转移之后未跟进的线索',
          hidden: false
        },
        {
          name: '分配给我的客户',
          crmType: 'customer',
          infoType: 'followCustomer',
          num: 0,
          tips: '转移、领取、分配之后未跟进的客户，默认显示自己负责的客户',
          hidden: false
        },
        {
          name: '待进入公海的客户',
          crmType: 'customer',
          infoType: 'putInPoolRemind',
          num: 0,
          tips: '',
          hidden: false
        },
        {
          name: '待审核合同',
          crmType: 'contract',
          infoType: 'checkContract',
          num: 0,
          tips: '',
          hidden: false
        },
        {
          name: '待审核回款',
          crmType: 'receivables',
          infoType: 'checkReceivables',
          num: 0,
          tips: '',
          hidden: false
        },
        {
          name: '待回款提醒',
          crmType: 'receivables_plan',
          infoType: 'remindReceivablesPlan',
          num: 0,
          tips: '',
          hidden: false
        },
        {
          name: '即将到期的合同',
          crmType: 'contract',
          infoType: 'endContract',
          num: 0,
          tips: '根据“合同到期时间”及设置的“提前提醒天数”提醒',
          hidden: false
        }
      ]
    }
  },

  computed: {
    ...mapGetters(['messageNum'])
  },

  watch: {
    /** 变化就刷新数据 */
    messageNum() {
      this.refreshNum()
    }
  },

  mounted() {
    this.refreshNum()
    /** 控制table的高度 */
    window.onresize = () => {
      var offsetHei = document.documentElement.clientHeight
      this.$bus.emit('message-scroll', offsetHei - 300)
    }
  },

  methods: {
    /**
     * 刷新消息数据
     */
    refreshNum() {
      for (let index = 0; index < this.leftSides.length; index++) {
        const element = this.leftSides[index]
        if (this.messageNum.hasOwnProperty(element.infoType)) {
          element.num = this.messageNum[element.infoType] || 0
          element.hidden = false
        } else {
          element.hidden = true
        }
      }
    },

    /**
     * 消息页面点击操作
     */
    messageHandle(data) {
      if (data.type == 'follow') {
        const copyNum = objDeepCopy(this.messageNum)
        const num = parseInt(copyNum[data.infoType]) - data.value
        copyNum[data.infoType] = num > 0 ? num : 0
        this.$store.commit('SET_MESSAGENUM', copyNum)
      }
    },

    /**
     * 侧边点击
     */
    sideClick(item) {
      this.leftType = item.infoType
    }
  }
}
</script>

<style lang="scss" scoped>
.message-header {
  height: 60px;
  font-size: 18px;
  padding: 0 20px;
  line-height: 60px;
}
.message-body {
  position: relative;
  height: calc(100% - 60px);
}
.message-content {
  position: relative;
  height: 100%;
}

.message-body-side {
  width: 200px;
  font-size: 14px;
  background-color: white;
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 1;
  border: 1px solid #e6e6e6;
  border-radius: 2px;
  overflow-y: auto;
  .side-item {
    position: relative;
    height: 50px;
    line-height: 50px;
    padding: 0 20px;
    font-size: 13px;
    cursor: pointer;
    i {
      color: #999;
    }
  }
}
.message-body-content {
  margin-left: 210px;
  height: 100%;
  overflow: hidden;
  background-color: white;
  border: 1px solid #e6e6e6;
  border-radius: 2px;
}

.side-item-default {
  color: #333;
  border-right: 2px solid transparent;
}

.side-item-select {
  color: #409eff;
  border-right: 2px solid #46cdcf;
  background-color: #ecf5ff;
}

.block {
  padding: 10px;
}

.el-badge /deep/ .el-badge__content {
  border: none;
  top: 0;
}

.el-badge {
  position: absolute;
  right: 15px;
  top: 0;
}
</style>
