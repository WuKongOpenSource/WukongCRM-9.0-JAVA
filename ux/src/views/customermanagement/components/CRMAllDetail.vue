<template>
  <component
    v-if="id&&visible"
    :is="tabName"
    :crm-type="crmType"
    :id="id"
    :listener-ids="listenerIDs"
    :no-listener-ids="noListenerIDs"
    class="d-view"
    @handle="detailHandle"
    @hide-view="hiddenView"/>
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

export default {
  name: 'CRMAllDetail', // 详情
  components: {
    ClueDetail,
    CustomerDetail,
    ContactsDetail,
    BusinessDetail,
    ContractDetail,
    ProductDetail,
    MoneyDetail
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
    },
    // 监听的dom 进行隐藏详情
    listenerIDs: {
      type: Array,
      default: () => {
        return ['crm-main-container']
      }
    },
    // 不监听
    noListenerIDs: {
      type: Array,
      default: () => {
        return []
      }
    },
    noListenerClass: {
      type: Array,
      default: () => {
        return ['el-table__body']
      }
    }
  },
  data() {
    return {
      tabName: '' // 组件名
    }
  },
  computed: {},
  watch: {
    crmType: function(type) {
      if (this.crmType == 'leads') {
        this.tabName = 'clue-detail'
      } else if (this.crmType == 'customer') {
        this.tabName = 'customer-detail'
      } else if (this.crmType == 'contacts') {
        this.tabName = 'contacts-detail'
      } else if (this.crmType == 'business') {
        this.tabName = 'business-detail'
      } else if (this.crmType == 'contract') {
        this.tabName = 'contract-detail'
      } else if (this.crmType == 'product') {
        this.tabName = 'product-detail'
      } else if (this.crmType == 'receivables') {
        this.tabName = 'money-detail'
      }
    }
  },
  mounted() {
    if (this.visible) {
      document.body.appendChild(this.$el)
      this.$el.style.zIndex = getMaxIndex()
    }
  },
  destroyed() {
    // remove DOM node after destroy
    if (this.$el && this.$el.parentNode) {
      this.$el.parentNode.removeChild(this.$el)
    }
  },
  methods: {
    hiddenView() {
      this.$emit('update:visible', false)
    },

    /**
     * 详情操作
     */
    detailHandle(data) {
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
