<template>
  <slide-view v-empty="!canShowDetail"
              xs-empty-icon="nopermission"
              xs-empty-text="暂无权限"
              :listenerIDs="listenerIDs"
              :noListenerIDs="noListenerIDs"
              :noListenerClass="noListenerClass"
              @side-close="hideView"
              :body-style="{padding: 0, height: '100%'}">
    <flexbox v-if="canShowDetail"
             v-loading="loading"
             direction="column"
             align="stretch"
             class="d-container">
      <c-r-m-detail-head crmType="customer"
                         :isSeas="isSeasDetail"
                         @handle="detailHeadHandle"
                         @close="hideView"
                         :detail="detailData"
                         :headDetails="headDetails"
                         :id="id">
      </c-r-m-detail-head>
      <div class="tabs">
        <el-tabs v-model="tabCurrentName"
                 @tab-click="handleClick">
          <el-tab-pane v-for="(item, index) in tabnames"
                       :key="index"
                       :label="item.label"
                       :name="item.name">
          </el-tab-pane>
        </el-tabs>
      </div>
      <div class="t-loading-content"
           id="follow-log-content">
        <keep-alive>
          <component v-bind:is="tabName"
                     crmType="customer"
                     :detail="detailData"
                     :id="id"
                     :isSeas="isSeasDetail"></component>
        </keep-alive>
      </div>
    </flexbox>
    <c-r-m-create-view v-if="isCreate"
                       crm-type="customer"
                       :action="{type: 'update', id: this.id, batchId: detailData.batchId}"
                       @save-success="editSaveSuccess"
                       @hiden-view="isCreate=false"></c-r-m-create-view>
  </slide-view>
</template>

<script>
import { crmCustomerRead } from '@/api/customermanagement/customer'

import SlideView from '@/components/SlideView'
import CRMDetailHead from '../components/CRMDetailHead'
import CustomerFollow from './components/CustomerFollow' // 跟进记录
import CRMBaseInfo from '../components/CRMBaseInfo' // 基本信息
import RelativeContacts from '../components/RelativeContacts' //相关联系人
import RelativeBusiness from '../components/RelativeBusiness' //相关商机
import RelativeContract from '../components/RelativeContract' //相关合同
import RelativeReturnMoney from '../components/RelativeReturnMoney' //相关回款
import RelativeFiles from '../components/RelativeFiles' //相关附件
import RelativeHandle from '../components/RelativeHandle' //相关操作
import RelativeTeam from '../components/RelativeTeam' //相关团队

import CRMCreateView from '../components/CRMCreateView' // 新建页面

import { getDateFromTimestamp } from '@/utils'
import moment from 'moment'
import detail from '../mixins/detail'

export default {
  /** 客户管理 的 客户详情 */
  name: 'customer-detail',
  components: {
    SlideView,
    CustomerFollow,
    CRMDetailHead,
    CRMBaseInfo,
    RelativeContacts,
    RelativeBusiness,
    RelativeContract,
    RelativeReturnMoney,
    RelativeFiles,
    RelativeHandle,
    RelativeTeam,
    CRMCreateView
  },
  mixins: [detail],
  props: {
    // 详情信息id
    id: [String, Number],
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
      loading: false, // 展示加载loading
      crmType: 'customer',
      detailData: {}, // read 详情
      headDetails: [
        { title: '客户级别', value: '' },
        { title: '成交状态', value: '' },
        { title: '负责人', value: '' },
        { title: '更新时间', value: '' }
      ],
      tabCurrentName: 'followlog',
      isCreate: false // 编辑操作
    }
  },
  computed: {
    tabName() {
      if (this.tabCurrentName == 'followlog') {
        return 'customer-follow'
      } else if (this.tabCurrentName == 'basicinfo') {
        return 'c-r-m-base-info'
      } else if (this.tabCurrentName == 'contacts') {
        return 'relative-contacts'
      } else if (this.tabCurrentName == 'team') {
        return 'relative-team'
      } else if (this.tabCurrentName == 'business') {
        return 'relative-business'
      } else if (this.tabCurrentName == 'contract') {
        return 'relative-contract'
      } else if (this.tabCurrentName == 'returnedmoney') {
        return 'relative-return-money'
      } else if (this.tabCurrentName == 'file') {
        return 'relative-files'
      } else if (this.tabCurrentName == 'operationlog') {
        return 'relative-handle'
      }
      return ''
    },
    tabnames() {
      var tempsTabs = []
      tempsTabs.push({ label: '跟进记录', name: 'followlog' })
      if (this.crm.customer && this.crm.customer.read) {
        tempsTabs.push({ label: '基本信息', name: 'basicinfo' })
      }
      if (this.crm.contacts && this.crm.contacts.index) {
        tempsTabs.push({ label: '联系人', name: 'contacts' })
      }
      tempsTabs.push({ label: '相关团队', name: 'team' })
      if (this.crm.business && this.crm.business.index) {
        tempsTabs.push({ label: '商机', name: 'business' })
      }
      if (this.crm.contract && this.crm.contract.index) {
        tempsTabs.push({ label: '合同', name: 'contract' })
      }
      if (this.crm.receivables && this.crm.receivables.index) {
        tempsTabs.push({ label: '回款信息', name: 'returnedmoney' })
      }
      tempsTabs.push({ label: '附件', name: 'file' })
      tempsTabs.push({ label: '操作记录', name: 'operationlog' })
      return tempsTabs
    },
    /**
     * isSeas 是从公海模块传入的 配合详情is_pool字段确定
     */
    isSeasDetail() {
      if (this.detailData && this.detailData.hasOwnProperty('isPool')) {
        return this.detailData.isPool == 1
      }
      return this.isSeas
    }
  },
  mounted() {},
  methods: {
    getDetial() {
      this.loading = true
      crmCustomerRead({
        customerId: this.id
      })
        .then(res => {
          this.loading = false
          this.detailData = res.data
          // 负责人
          this.headDetails[0].value = res.data.客户级别
          this.headDetails[1].value = res.data.dealStatus
          this.headDetails[2].value = res.data.ownerUserName
          this.headDetails[3].value = res.data.updateTime
        })
        .catch(() => {
          this.loading = false
        })
    },
    //** 点击关闭按钮隐藏视图 */
    hideView() {
      this.$emit('hide-view')
    },
    //** tab标签点击 */
    handleClick(tab, event) {},
    editSaveSuccess() {
      this.$emit('handle', { type: 'save-success' })
      this.getDetial()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../styles/crmdetail.scss';
</style>
