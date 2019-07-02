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
      <c-r-m-detail-head crmType="contacts"
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
                       :name="item.name"></el-tab-pane>
        </el-tabs>
      </div>
      <div class="t-loading-content"
           id="follow-log-content">
        <keep-alive>
          <component v-bind:is="tabName"
                     crmType="contacts"
                     :detail="detailData"
                     :id="id"></component>
        </keep-alive>
      </div>
    </flexbox>
    <c-r-m-create-view v-if="isCreate"
                       crm-type="contacts"
                       :action="{type: 'update', id: this.id, batchId: detailData.batchId}"
                       @save-success="editSaveSuccess"
                       @hiden-view="isCreate=false"></c-r-m-create-view>
  </slide-view>
</template>

<script>
import { crmContactsRead } from '@/api/customermanagement/contacts'

import SlideView from '@/components/SlideView'
import CRMDetailHead from '../components/CRMDetailHead'
import ContactsFollow from './components/ContactsFollow' // 跟进记录
import CRMBaseInfo from '../components/CRMBaseInfo' // 联系人基本信息

import RelativeBusiness from '../components/RelativeBusiness' //相关商机
import RelativeHandle from '../components/RelativeHandle' //相关操作
import RelativeFiles from '../components/RelativeFiles' //相关附件
import CRMCreateView from '../components/CRMCreateView' // 新建页面

import { getDateFromTimestamp } from '@/utils'
import moment from 'moment'
import detail from '../mixins/detail'

export default {
  /** 联系人管理 的 联系人详情 */
  name: 'contacts-detail',
  components: {
    SlideView,
    CRMDetailHead,
    ContactsFollow,
    CRMBaseInfo,
    RelativeBusiness,
    RelativeHandle,
    RelativeFiles,
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
      crmType: 'contacts',
      detailData: {}, // 详情的完整信息
      headDetails: [
        { title: '客户名称', value: '' },
        { title: '职务', value: '' },
        { title: '手机', value: '' },
        { title: '创建时间', value: '' }
      ],
      tabCurrentName: 'followlog',
      isCreate: false // 编辑操作
    }
  },
  computed: {
    tabName() {
      if (this.tabCurrentName == 'followlog') {
        return 'contacts-follow'
      } else if (this.tabCurrentName == 'basicinfo') {
        return 'c-r-m-base-info'
      } else if (this.tabCurrentName == 'business') {
        return 'relative-business'
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
      if (this.crm.contacts && this.crm.contacts.read) {
        tempsTabs.push({ label: '基本信息', name: 'basicinfo' })
      }
      if (this.crm.business && this.crm.business.index) {
        tempsTabs.push({ label: '商机', name: 'business' })
      }
      tempsTabs.push({ label: '附件', name: 'file' })
      tempsTabs.push({ label: '操作记录', name: 'operationlog' })
      return tempsTabs
    }
  },
  mounted() {},
  methods: {
    getDetial() {
      this.loading = true
      crmContactsRead({
        contactsId: this.id
      })
        .then(res => {
          this.loading = false
          this.detailData = res.data
          // 负责人
          this.headDetails[0].value = res.data.customerName
          this.headDetails[1].value = res.data.post
          this.headDetails[2].value = res.data.mobile
          this.headDetails[3].value = res.data.createTime
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
