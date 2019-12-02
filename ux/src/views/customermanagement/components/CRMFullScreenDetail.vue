<template>
  <div
    v-show="visible"
    class="full-container">
    <component
      v-if="id&&showDetail"
      :is="tabName"
      :crm-type="crmType"
      :id="id"
      class="d-view"
      @handle="detailHandle"
      @hide-view="hiddenView"/>
  </div>
</template>

<script type="text/javascript">
import { getMaxIndex } from '@/utils/index'
import ClueDetail from '../clue/ClueDetail'
import CustomerDetail from '../customer/CustomerDetail'
import ContactsDetail from '../contacts/ContactsDetail'
import BusinessDetail from '../business/BusinessDetail'
import ContractDetail from '../contract/ContractDetail'
import ProductDetail from '../product/ProductDetail'
import MoneyDetail from '../money/MoneyDetail'
import ExamineDetail from '@/views/OAManagement/examine/components/examineDetail'

export default {
  name: 'CRMFullScreenDetail', // 客户管理下 重要提醒 回款计划提醒
  components: {
    ClueDetail,
    CustomerDetail,
    ContactsDetail,
    BusinessDetail,
    ContractDetail,
    ProductDetail,
    MoneyDetail,
    ExamineDetail
  },
  props: {
    /** 模块ID */
    id: [String, Number],
    /** 没有值就是全部类型 有值就是当个类型 */
    crmType: {
      type: String,
      default: ''
    },
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      showDetail: false
    }
  },
  computed: {
    tabName() {
      if (this.crmType == 'leads') {
        return 'clue-detail'
      } else if (this.crmType == 'customer') {
        return 'customer-detail'
      } else if (this.crmType == 'contacts') {
        return 'contacts-detail'
      } else if (this.crmType == 'business') {
        return 'business-detail'
      } else if (this.crmType == 'contract') {
        return 'contract-detail'
      } else if (this.crmType == 'product') {
        return 'product-detail'
      } else if (this.crmType == 'receivables') {
        return 'money-detail'
      } else if (this.crmType == 'examine') {
        return 'examine-detail'
      }
      return ''
    }
  },
  watch: {
    visible(val) {
      this.showDetail = val
      if (val) {
        document.body.appendChild(this.$el)
        this.$el.addEventListener('click', this.handleDocumentClick, false)
        this.$el.style.zIndex = getMaxIndex()
      }
    },
    showDetail(val) {
      if (!val) {
        setTimeout(() => {
          this.$emit('update:visible', false)
        }, 350)
      }
    }
  },
  mounted() {
    if (this.visible) {
      document.body.appendChild(this.$el)
      this.$el.addEventListener('click', this.handleDocumentClick, false)
      this.$el.style.zIndex = getMaxIndex()
    }
  },

  beforeDestroy() {
    // remove DOM node after destroy
    if (this.$el && this.$el.parentNode) {
      this.$el.parentNode.removeChild(this.$el)
      this.$el.removeEventListener('click', this.handleDocumentClick, false)
    }
  },
  methods: {
    hiddenView() {
      this.showDetail = false
    },
    handleDocumentClick(e) {
      e.stopPropagation()
      if (this.$el == e.target) {
        this.showDetail = false
      }
    },

    /**
     * 详情操作
     */
    detailHandle(data) {
      if (data.type === 'alloc' || data.type === 'get' || data.type === 'transfer' || data.type === 'transform' || data.type === 'delete' || data.type === 'put_seas') {
        this.showDetail = false
      }
      this.$emit('handle', data)
    }
  }
}
</script>
<style lang="scss" scoped>
.full-container {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  overflow: auto;
  margin: 0;
  background-color: rgba(0, 0, 0, 0.3);
}

.d-view {
  position: fixed;
  width: 950px;
  top: 0px;
  bottom: 0px;
  right: 0px;
}
</style>
