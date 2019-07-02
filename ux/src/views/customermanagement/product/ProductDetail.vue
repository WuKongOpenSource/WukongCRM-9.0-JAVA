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
      <c-r-m-detail-head crmType="product"
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
      <div class="t-loading-content">
        <keep-alive>
          <component v-bind:is="tabName"
                     crmType="product"
                     :detail="detailData"
                     :id="id"></component>
        </keep-alive>
      </div>
    </flexbox>
    <c-r-m-create-view v-if="isCreate"
                       crm-type="product"
                       :action="{type: 'update', id: this.id, batchId: detailData.batchId}"
                       @save-success="editSaveSuccess"
                       @hiden-view="isCreate=false"></c-r-m-create-view>
  </slide-view>
</template>

<script>
import { crmProductRead } from '@/api/customermanagement/product'

import SlideView from '@/components/SlideView'
import CRMDetailHead from '../components/CRMDetailHead'
import CRMBaseInfo from '../components/CRMBaseInfo' // 产品基本信息
import RelativeFiles from '../components/RelativeFiles' //相关附件
import RelativeHandle from '../components/RelativeHandle' //相关操作

import CRMCreateView from '../components/CRMCreateView' // 新建页面
import detail from '../mixins/detail'

export default {
  /** 客户管理 的 客户详情 */
  name: 'product-detail',
  components: {
    SlideView,
    CRMDetailHead,
    CRMBaseInfo,
    RelativeFiles,
    RelativeHandle,
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
      crmType: 'product',
      detailData: {}, // read 详情
      headDetails: [
        { title: '产品类别', value: '' },
        { title: '产品单位', value: '' },
        { title: '产品价格', value: '' },
        { title: '产品编码', value: '' }
      ],
      tabnames: [
        { label: '基本信息', name: 'basicinfo' },
        { label: '附件', name: 'file' },
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
      } else if (this.tabCurrentName == 'file') {
        return 'relative-files'
      } else if (this.tabCurrentName == 'operationlog') {
        return 'relative-handle'
      }
      return ''
    }
  },
  mounted() {},
  methods: {
    getDetial() {
      this.loading = true
      crmProductRead({
        productId: this.id
      })
        .then(res => {
          this.loading = false
          this.detailData = res.data

          this.headDetails[0].value = res.data.categoryName
          this.headDetails[1].value = res.data.单位
          this.headDetails[2].value = res.data.price
          this.headDetails[3].value = res.data.num
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
