<template>
  <slide-view
    v-empty="!canShowDetail"
    :listener-ids="listenerIDs"
    :no-listener-ids="noListenerIDs"
    :no-listener-class="noListenerClass"
    :body-style="{padding: 0, height: '100%'}"
    xs-empty-icon="nopermission"
    xs-empty-text="暂无权限"
    @side-close="hideView">
    <flexbox
      v-loading="loading"
      v-if="canShowDetail"
      direction="column"
      align="stretch"
      class="d-container">
      <c-r-m-detail-head
        :detail="detailData"
        :head-details="headDetails"
        :id="id"
        crm-type="contract"
        @handle="detailHeadHandle"
        @close="hideView"/>
      <div class="examine-info">
        <examine-info
          :id="id"
          :record-id="detailData.examineRecordId"
          :owner-user-id="detailData.ownerUserId"
          class="examine-info-border"
          examine-type="crm_contract"/>
      </div>
      <div class="tabs">
        <el-tabs
          v-model="tabCurrentName"
          @tab-click="handleClick">
          <el-tab-pane
            v-for="(item, index) in tabnames"
            :key="index"
            :label="item.label"
            :name="item.name"/>
        </el-tabs>
      </div>
      <div
        id="follow-log-content"
        class="t-loading-content">
        <keep-alive>
          <component
            :is="tabName"
            :detail="detailData"
            :id="id"
            crm-type="contract"/>
        </keep-alive>
      </div>
    </flexbox>
    <c-r-m-create-view
      v-if="isCreate"
      :action="{type: 'update', id: id, batchId: detailData.batchId}"
      crm-type="contract"
      @save-success="editSaveSuccess"
      @hiden-view="isCreate=false"/>
  </slide-view>
</template>

<script>
import { crmContractRead } from '@/api/customermanagement/contract'

import SlideView from '@/components/SlideView'
import CRMDetailHead from '../components/CRMDetailHead'
import ContractFollow from './components/ContractFollow' // 跟进记录
import CRMBaseInfo from '../components/CRMBaseInfo' // 商机基本信息
import RelativeHandle from '../components/RelativeHandle' // 相关操作
import RelativeTeam from '../components/RelativeTeam' // 相关团队
import RelativeProduct from '../components/RelativeProduct' // 相关团队
import RelativeReturnMoney from '../components/RelativeReturnMoney' // 相关回款
import RelativeFiles from '../components/RelativeFiles' // 相关附件
import ExamineInfo from '@/components/Examine/ExamineInfo'

import CRMCreateView from '../components/CRMCreateView' // 新建页面

import detail from '../mixins/detail'

export default {
  /** 客户管理 的 合同详情 */
  name: 'ContractDetail',
  components: {
    SlideView,
    CRMDetailHead,
    ContractFollow,
    CRMBaseInfo,
    RelativeHandle,
    RelativeTeam,
    RelativeProduct,
    RelativeReturnMoney,
    RelativeFiles,
    ExamineInfo,
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
      crmType: 'contract',
      detailData: {}, // read 详情
      headDetails: [
        { title: '合同编号', value: '' },
        { title: '客户名称', value: '' },
        { title: '合同金额（元）', value: '' },
        { title: '签约时间', value: '' },
        { title: '回款金额（元）', value: '' },
        { title: '负责人', value: '' }
      ],
      tabCurrentName: 'followlog',
      isCreate: false // 编辑操作
    }
  },
  computed: {
    tabName() {
      if (this.tabCurrentName == 'followlog') {
        return 'contract-follow'
      } else if (this.tabCurrentName == 'basicinfo') {
        return 'c-r-m-base-info'
      } else if (this.tabCurrentName == 'team') {
        return 'relative-team'
      } else if (this.tabCurrentName == 'contract') {
        return 'relative-contract'
      } else if (this.tabCurrentName == 'operationlog') {
        return 'relative-handle'
      } else if (this.tabCurrentName == 'product') {
        return 'relative-product'
      } else if (this.tabCurrentName == 'returnedmoney') {
        return 'relative-return-money'
      } else if (this.tabCurrentName == 'file') {
        return 'relative-files'
      }
      return ''
    },
    tabnames() {
      var tempsTabs = []
      tempsTabs.push({ label: '跟进记录', name: 'followlog' })
      if (this.crm.contract && this.crm.contract.read) {
        tempsTabs.push({ label: '基本信息', name: 'basicinfo' })
      }
      if (this.crm.product && this.crm.product.index) {
        tempsTabs.push({ label: '产品', name: 'product' })
      }
      if (this.crm.receivables && this.crm.receivables.index) {
        tempsTabs.push({ label: '回款信息', name: 'returnedmoney' })
      }
      tempsTabs.push({ label: '相关团队', name: 'team' })
      tempsTabs.push({ label: '附件', name: 'file' })
      tempsTabs.push({ label: '操作记录', name: 'operationlog' })
      return tempsTabs
    }
  },
  mounted() {},
  methods: {
    getDetial() {
      this.loading = true
      crmContractRead({
        contractId: this.id
      })
        .then(res => {
          this.loading = false
          this.detailData = res.data // 创建回款计划的时候使用

          this.headDetails[0].value = res.data.num
          this.headDetails[1].value = res.data.customerName
          this.headDetails[2].value = this.moneyFormat(res.data.money)
          this.headDetails[3].value = res.data.orderDate
          this.headDetails[4].value = this.moneyFormat(res.data.receivablesMoney)
          this.headDetails[5].value = res.data.ownerUserName
        })
        .catch(() => {
          this.loading = false
        })
    },
    //* * 点击关闭按钮隐藏视图 */
    hideView() {
      this.$emit('hide-view')
    },
    //* * tab标签点击 */
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
