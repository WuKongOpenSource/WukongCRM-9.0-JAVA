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
      <c-r-m-detail-head crmType="receivables"
                         @handle="detailHeadHandle"
                         @close="hideView"
                         :detail="detailData"
                         :headDetails="headDetails"
                         :id="id">
      </c-r-m-detail-head>
      <div class="examine-info">
        <examine-info :id="id"
                      class="examine-info-border"
                      examineType="crm_receivables"
                      :recordId="detailData.examineRecordId"
                      :ownerUserId="detailData.ownerUserId">
        </examine-info>
      </div>
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
      <div class="t-loading-content">
        <keep-alive>
          <component v-bind:is="tabName"
                     crmType="receivables"
                     :detail="detailData"
                     :id="id"></component>
        </keep-alive>
      </div>
    </flexbox>
    <c-r-m-create-view v-if="isCreate"
                       crm-type="receivables"
                       :action="{type: 'update', id: this.id, batchId: detailData.batchId}"
                       @save-success="editSaveSuccess"
                       @hiden-view="isCreate=false"></c-r-m-create-view>
  </slide-view>
</template>

<script>
import { crmReceivablesRead } from '@/api/customermanagement/money'

import SlideView from '@/components/SlideView'
import CRMDetailHead from '../components/CRMDetailHead'
import CRMBaseInfo from '../components/CRMBaseInfo' // 基本信息
import RelativeHandle from '../components/RelativeHandle' //相关操作
import CRMCreateView from '../components/CRMCreateView' // 新建页面
import ExamineInfo from '@/components/Examine/ExamineInfo'
import { timestampToFormatTime } from '@/utils'
import detail from '../mixins/detail'

export default {
  /** 客户管理 的 回款详情 */
  name: 'money-detail',
  components: {
    SlideView,
    CRMDetailHead,
    CRMBaseInfo,
    RelativeHandle,
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
      crmType: 'receivables',
      name: '', // 名称
      detailData: {}, // read 详情
      headDetails: [
        { title: '客户名称', value: '' },
        { title: '合同金额', value: '' },
        { title: '合同名称', value: '' },
        { title: '回款日期', value: '' },
        { title: '回款金额', value: '' },
        { title: '负责人', value: '' }
      ],
      tabnames: [
        { label: '基本信息', name: 'basicinfo' },
        { label: '操作记录', name: 'operationlog' }
      ],
      tabCurrentName: 'basicinfo',
      isCreate: false // 编辑操作
    }
  },
  computed: {
    tabName() {
      if (this.tabCurrentName == 'basicinfo') {
        return 'c-r-m-base-info'
      }
      if (this.tabCurrentName == 'operationlog') {
        return 'relative-handle'
      }
      return ''
    }
  },
  mounted() {},
  methods: {
    getDetial() {
      this.loading = true
      crmReceivablesRead({
        receivablesId: this.id
      })
        .then(res => {
          this.loading = false
          this.name = res.data.number
          this.detailData = res.data
          //   // 负责人
          this.headDetails[0].value = res.data.customerName
          this.headDetails[1].value = res.data.contractMoney
          this.headDetails[2].value = res.data.contractName
          this.headDetails[3].value = res.data.returnTime
          this.headDetails[4].value = res.data.money
          this.headDetails[5].value = res.data.ownerUserName
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
.busi-line {
  position: absolute;
  bottom: 0;
  left: 17px;
  right: 17px;
  height: 1px;
  background-color: #e6e6e6;
}
</style>
