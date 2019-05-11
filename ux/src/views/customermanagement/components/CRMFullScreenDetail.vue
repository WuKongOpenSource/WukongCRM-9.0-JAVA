<template>
  <div v-show="visible"
       class="full-container">
    <component v-if="id&&showDetail"
               class="d-view"
               v-bind:is="tabName"
               :crmType="crmType"
               :id="id"
               @hide-view="hiddenView">
    </component>
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
  name: 'c-r-m-full-screen-detail', //客户管理下 重要提醒 回款计划提醒
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
  data() {
    return {
      showDetail: false
    }
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
  mounted() {
    if (this.visible) {
      document.body.appendChild(this.$el)
      this.$el.addEventListener('click', this.handleDocumentClick, false)
      this.$el.style.zIndex = getMaxIndex()
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
    }
  },

  beforeDestroy() {
    // remove DOM node after destroy
    if (this.$el && this.$el.parentNode) {
      this.$el.parentNode.removeChild(this.$el)
      this.$el.removeEventListener('click', this.handleDocumentClick, false)
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
