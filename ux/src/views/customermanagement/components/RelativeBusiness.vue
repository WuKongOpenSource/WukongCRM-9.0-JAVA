<template>
  <div
    v-empty="nopermission"
    class="rc-cont"
    xs-empty-icon="nopermission"
    xs-empty-text="暂无权限">
    <flexbox
      v-if="!isSeas"
      class="rc-head"
      direction="row-reverse">
      <el-button
        class="rc-head-item"
        type="primary"
        @click.native="createClick">新建商机</el-button>
      <el-button
        v-if="canRelation"
        class="rc-head-item"
        type="primary"
        @click.native="unRelevanceHandleClick">解除关联</el-button>
      <el-popover
        v-if="canRelation"
        v-model="showRelativeView"
        placement="bottom"
        width="700"
        popper-class="no-padding-popover"
        trigger="click"
        style="margin-right: 20px;">
        <crm-relative
          ref="crmrelative"
          v-model="showRelativeView"
          :show="showRelativeView"
          :radio="false"
          :action="{ type: 'condition', data: { moduleType: 'customer', customerId: customerId } }"
          :selected-data="{ 'business': list }"
          crm-type="business"
          @close="showRelativeView = false"
          @changeCheckout="checkRelativeInfos"/>
        <el-button
          slot="reference"
          class="rc-head-item"
          style="margin-right: 0;"
          type="primary"
          @click.native="showRelativeView = true">关联</el-button>
      </el-popover>

    </flexbox>
    <el-table
      :data="list"
      :height="tableHeight"
      :header-cell-style="headerRowStyle"
      :cell-style="cellStyle"
      stripe
      style="width: 100%;border: 1px solid #E6E6E6;"
      @row-click="handleRowClick"
      @selection-change="selectionList = $event">
      <el-table-column
        v-if="canRelation && fieldList.length > 0"
        show-overflow-tooltip
        type="selection"
        align="center"
        width="55"/>
      <el-table-column
        v-for="(item, index) in fieldList"
        :key="index"
        :prop="item.prop"
        :label="item.label"
        :formatter="fieldFormatter"
        show-overflow-tooltip/>
    </el-table>

    <c-r-m-full-screen-detail
      :visible.sync="showFullDetail"
      :id="businessId"
      crm-type="business"/>
    <c-r-m-create-view
      v-if="isCreate"
      :action="createActionInfo"
      crm-type="business"
      @save-success="createSaveSuccess"
      @hiden-view="isCreate=false"/>
  </div>
</template>

<script type="text/javascript">
import loading from '../mixins/loading'
import CRMCreateView from './CRMCreateView'
import { crmCustomerQueryBusiness } from '@/api/customermanagement/customer'
import {
  crmContactsQueryBusiness,
  crmContactsRelateBusinessAPI,
  crmContactsUnrelateBusinessAPI
} from '@/api/customermanagement/contacts'
import CrmRelative from '@/components/CreateCom/CrmRelative'

import { moneyFormat } from '@/utils'

export default {
  name: 'RelativeBusiness', // 相关联系人商机  可能再很多地方展示 放到客户管理目录下（新建时仅和客户进行关联）
  components: {
    CRMFullScreenDetail: () => import('./CRMFullScreenDetail.vue'),
    CRMCreateView,
    CrmRelative
  },
  mixins: [loading],
  props: {
    /** 模块ID */
    id: [String, Number],
    /** 没有值就是全部类型 有值就是当个类型 */
    crmType: {
      type: String,
      default: ''
    },
    /** 是公海 默认是客户 */
    isSeas: {
      type: Boolean,
      default: false
    },
    /** 联系人人下 新建商机 需要联系人里的客户信息  合同下需要客户和商机信息 */
    detail: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    return {
      nopermission: false,
      list: [],
      fieldList: [],
      tableHeight: '400px',
      showFullDetail: false,
      isCreate: false, // 控制新建
      businessId: '', // 查看全屏联系人详情的 ID
      // 创建的相关信息
      createActionInfo: { type: 'relative', crmType: this.crmType, data: {}},
      /**
       * 关联的逻辑
       */
      showRelativeView: false, // 控制关联信息视图
      selectionList: [] // 取消关联勾选的数据
    }
  },
  computed: {
    // 联系人下客户id获取关联商机
    customerId() {
      return this.detail.customerId
    },
    // 是否能关联
    canRelation() {
      return this.crmType == 'contacts'
    }
  },
  watch: {
    id: function(val) {
      this.list = []
      this.getDetail()
    }
  },
  mounted() {
    this.getDetail()
  },
  activated: function() {},
  deactivated: function() {},
  methods: {
    /**
     * 关联的数据
     */
    checkRelativeInfos(data) {
      if (data.data.length > 0) {
        const params = { contactsId: this.id }
        params.businessIds = data.data
          .map(item => {
            return item.businessId
          })
          .join(',')
        crmContactsRelateBusinessAPI(params)
          .then(res => {
            this.getDetail()
            this.$message.success('操作成功')
          })
          .catch(() => {})
      }
    },

    /**
     * 取消关联
     */
    unRelevanceHandleClick() {
      if (this.selectionList.length == 0) {
        this.$message.error('请先勾选数据')
      } else {
        this.$confirm('确认取消关联?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            const params = { contactsId: this.id }
            params.businessIds = this.selectionList
              .map(item => {
                return item.businessId
              })
              .join(',')
            crmContactsUnrelateBusinessAPI(params)
              .then(res => {
                this.getDetail()
                this.$message.success('操作成功')
              })
              .catch(() => {})
          })
          .catch(() => {
            this.$message.info('已取消操作')
          })
      }
    },

    /**
     * 获取字段信息
     */
    getFieldList() {
      this.fieldList.push({
        prop: 'businessName',
        width: '200',
        label: '商机名称'
      })
      this.fieldList.push({
        prop: 'money',
        width: '200',
        label: '商机金额'
      })
      this.fieldList.push({
        prop: 'customerName',
        width: '200',
        label: '客户名称'
      })
      this.fieldList.push({
        prop: 'typeName',
        width: '200',
        label: '商机状态组'
      })
      this.fieldList.push({ prop: 'statusName', width: '200', label: '状态' })
    },

    getDetail() {
      this.loading = true
      const request = {
        contacts: crmContactsQueryBusiness,
        customer: crmCustomerQueryBusiness
      }[this.crmType]
      const params = {}
      params[this.crmType + 'Id'] = this.id
      request(params)
        .then(res => {
          if (this.fieldList.length == 0) {
            this.getFieldList()
          }
          this.nopermission = false
          this.loading = false
          this.list = res.data.list
        })
        .catch(data => {
          if (data.code == 102) {
            this.nopermission = true
          }
          this.loading = false
        })
    },
    // 当某一行被点击时会触发该事件
    handleRowClick(row, column, event) {
      this.businessId = row.businessId
      this.showFullDetail = true
    },
    /** 通过回调控制表头style */
    headerRowStyle({ row, column, rowIndex, columnIndex }) {
      return { textAlign: 'center' }
    },
    /** 通过回调控制style */
    cellStyle({ row, column, rowIndex, columnIndex }) {
      return { textAlign: 'center' }
    },
    /** 新建 */
    createClick() {
      /** 客户 和 联系人 下可新建商机  */
      if (this.crmType == 'contacts') {
        this.createActionInfo.data['customer'] = this.detail
        this.createActionInfo.relativeData = {
          contactsId: this.detail.contactsId
        }
      } else if (this.crmType == 'customer') {
        this.createActionInfo.data['customer'] = this.detail
      }
      this.isCreate = true
    },

    /**
     * 创建成功刷新相关信息
     */
    createSaveSuccess() {
      if (this.canRelation) {
        this.$refs.crmrelative.refreshList()
      } else {
        this.getDetail()
      }
    },
    /**
     * 格式化表格
     */
    fieldFormatter(row, column) {
      if (column.property == 'money') {
        return moneyFormat(row[column.property])
      }
      return row[column.property] || '--'
    }
  }
}
</script>
<style lang="scss" scoped>
@import '../styles/relativecrm.scss';
</style>
