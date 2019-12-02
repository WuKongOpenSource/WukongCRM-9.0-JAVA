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
        @click.native="createClick">新建联系人</el-button>
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
          :selected-data="{ 'contacts': list }"
          crm-type="contacts"
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
        show-overflow-tooltip/>
    </el-table>
    <c-r-m-full-screen-detail
      :visible.sync="showFullDetail"
      :id="contactsId"
      crm-type="contacts"/>
    <c-r-m-create-view
      v-if="isCreate"
      :action="createActionInfo"
      crm-type="contacts"
      @save-success="createSaveSuccess"
      @hiden-view="isCreate=false"/>
  </div>
</template>

<script type="text/javascript">
import loading from '../mixins/loading'
import CRMCreateView from './CRMCreateView'
import { crmCustomerQueryContacts } from '@/api/customermanagement/customer'
import {
  crmBusinessQueryContactsAPI,
  crmBusinessRelateContactsAPI,
  crmBusinessUnrelateContactsAPI
} from '@/api/customermanagement/business'
import CrmRelative from '@/components/CreateCom/CrmRelative'

export default {
  name: 'RelativeContacts', // 相关联系人列表  可能再很多地方展示 放到客户管理目录下
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
      contactsId: '', // 查看全屏联系人详情的 ID
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
      return this.crmType == 'business'
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
        const params = { businessId: this.id }
        params.contactsIds = data.data
          .map(item => {
            return item.contactsId
          })
          .join(',')
        crmBusinessRelateContactsAPI(params)
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
            const params = { businessId: this.id }
            params.contactsIds = this.selectionList
              .map(item => {
                return item.contactsId
              })
              .join(',')
            crmBusinessUnrelateContactsAPI(params)
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
     * 表头数据
     */
    getFieldList() {
      this.fieldList.push({
        prop: 'name',
        width: '200',
        label: '姓名'
      })
      this.fieldList.push({ prop: 'mobile', width: '200', label: '手机' })
      this.fieldList.push({ prop: 'post', width: '200', label: '职务' })
    },

    /**
     * 获取数据
     */
    getDetail() {
      this.loading = true
      const request = {
        customer: crmCustomerQueryContacts,
        business: crmBusinessQueryContactsAPI
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
      this.contactsId = row.contactsId
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
      /** 客户 下新建联系人 */
      if (this.crmType == 'customer') {
        this.createActionInfo.data['customer'] = this.detail
      } else if (this.crmType == 'business') {
        this.createActionInfo.data['customer'] = this.detail
        this.createActionInfo.relativeData = {
          businessId: this.detail.businessId
        }
      }
      this.isCreate = true
    },
    createSaveSuccess() {
      if (this.canRelation) {
        this.$refs.crmrelative.refreshList()
      } else {
        this.getDetail()
      }
    }
  }
}
</script>
<style lang="scss" scoped>
@import '../styles/relativecrm.scss';
</style>
