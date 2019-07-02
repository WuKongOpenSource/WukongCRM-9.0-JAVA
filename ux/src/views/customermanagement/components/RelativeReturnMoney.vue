<template>
  <div class="rc-cont">
    <flexbox v-if="!isSeas"
             class="rc-head"
             direction="row-reverse">
      <el-button class="rc-head-item"
                 @click.native="createClick('plan')"
                 type="primary">新建回款计划</el-button>
    </flexbox>
    <el-table :data="palnList"
              :height="tableHeight"
              stripe
              style="width: 100%;border: 1px solid #E6E6E6;"
              :header-cell-style="headerRowStyle"
              :cell-style="cellStyle">
      <el-table-column v-for="(item, index) in planFieldList"
                       :key="index"
                       show-overflow-tooltip
                       :prop="item.prop"
                       :label="item.label">
      </el-table-column>
      <el-table-column label="操作"
                       width="100">
        <template slot-scope="scope">
          <flexbox justify="center">
            <el-button type="text"
                       @click.native="handleFile('edit', scope)">编辑</el-button>
            <el-button type="text"
                       @click.native="handleFile('delete', scope)">删除</el-button>
          </flexbox>
        </template>
      </el-table-column>
    </el-table>

    <flexbox class="rc-head"
             direction="row-reverse"
             style="margin-top: 15px;">
      <el-button v-if="!isSeas"
                 class="rc-head-item"
                 @click.native="createClick('money')"
                 type="primary">新建回款</el-button>
    </flexbox>
    <el-table :data="list"
              :height="tableHeight"
              stripe
              style="width: 100%;border: 1px solid #E6E6E6;"
              :header-cell-style="headerRowStyle"
              :cell-style="cellStyle"
              @row-click="handleRowClick">
      <el-table-column v-for="(item, index) in fieldList"
                       :key="index"
                       show-overflow-tooltip
                       :prop="item.prop"
                       :label="item.label">
      </el-table-column>
    </el-table>
    <c-r-m-full-screen-detail :visible.sync="showFullDetail"
                              :crmType="showFullCrmType"
                              :id="showFullId">
    </c-r-m-full-screen-detail>
    <c-r-m-create-view v-if="isCreate"
                       :crm-type="createCrmType"
                       :action="createActionInfo"
                       @save-success="saveSuccess"
                       @hiden-view="isCreate=false"></c-r-m-create-view>
  </div>
</template>

<script type="text/javascript">
import loading from '../mixins/loading'
import CRMCreateView from './CRMCreateView'
import {
  crmCustomerQueryReceivables,
  crmCustomerQueryReceivablesPlan
} from '@/api/customermanagement/customer'
import {
  crmContractQueryReceivables,
  crmContractQueryReceivablesPlan
} from '@/api/customermanagement/contract'
import {
  crmReceivablesPlanDeleteAPI
} from '@/api/customermanagement/money'
/** 注意  需要删除接口 */
import { timestampToFormatTime, objDeepCopy } from '@/utils'

export default {
  name: 'relative-return-money', //相关回款  可能再很多地方展示 放到客户管理目录下

  components: {
    CRMCreateView,
    CRMFullScreenDetail: () => import('./CRMFullScreenDetail.vue')
  },

  computed: {},

  mixins: [loading],

  data() {
    return {
      list: [],
      fieldList: [],
      tableHeight: '250px',
      showFullDetail: false,
      showFullCrmType: 'receivables',
      showFullId: '', // 查看全屏详情的 ID
      createCrmType: 'receivables_plan', // 创建回款计划
      isCreate: false, // 新建回款回款
      palnList: [],
      planFieldList: [],
      createActionInfo: {} // 新建回款计划的时候 在客户 合同下导入关联信息
    }
  },

  watch: {
    id: function(val) {
      this.list = []
      this.palnList = []
      this.getList()
      this.getPlanList()
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
    /** 是公海 默认是客户 */
    isSeas: {
      type: Boolean,
      default: false
    },
    /** 客户和 合同下 可新建 回款计划 */
    detail: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },

  mounted() {
    this.planFieldList = [
      { prop: 'num', width: '200', label: '期数' },
      { prop: 'customerName', width: '200', label: '客户名称' },
      { prop: 'contractNum', width: '200', label: '合同编号' },
      { prop: 'money', width: '200', label: '计划回款金额' },
      { prop: 'returnDate', width: '200', label: '计划回款日期' },
      { prop: 'returnType', width: '200', label: '计划回款方式' },
      { prop: 'remind', width: '200', label: '提前几日提醒' },
      { prop: 'remark', width: '200', label: '备注' }
    ]

    this.getPlanList()

    this.fieldList = [
      { prop: 'receivablesNum', width: '200', label: '回款编号' },
      { prop: 'contractName', width: '200', label: '合同名称' },
      { prop: 'contractMoney', width: '200', label: '合同金额' },
      { prop: 'receivablesMoney', width: '200', label: '回款金额' },
      { prop: 'num', width: '200', label: '期数' },
      { prop: 'ownerUserName', width: '200', label: '负责人' },
      { prop: 'checkStatus', width: '200', label: '状态' },
      { prop: 'returnTime', width: '200', label: '回款日期' }
    ]
    this.getList()
  },

  methods: {
    /**
     * 回款计划列表
     */
    getPlanList() {
      this.loading = true
      let request = {
        customer: crmCustomerQueryReceivablesPlan,
        contract: crmContractQueryReceivablesPlan
      }[this.crmType]
      request(this.getParams())
        .then(res => {
          this.loading = false
          this.palnList = res.data
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * 回款列表
     */
    getList() {
      this.loading = true
      let request = {
        customer: crmCustomerQueryReceivables,
        contract: crmContractQueryReceivables
      }[this.crmType]
      request(this.getParams())
        .then(res => {
          this.loading = false
          this.list = res.data
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * 获取上传参数
     */
    getParams() {
      if (this.crmType === 'customer') {
        return { customerId: this.id, pageType: 0 }
      } else if (this.crmType === 'contract') {
        return { contractId: this.id, pageType: 0 }
      }
      return {}
    },

    /**
     * 当某一行被点击时会触发该事件
     */
    handleRowClick(row, column, event) {
      this.showFullId = row.receivablesId
      this.showFullCrmType = 'receivables'
      this.showFullDetail = true
    },

    /**
     * 通过回调控制style
     */
    cellStyle({ row, column, rowIndex, columnIndex }) {
      if (columnIndex == 1) {
        return { color: '#3E84E9' }
      } else {
        return ''
      }
    },

    /**
     * 新建回款和回款计划
     */
    createClick(type) {
      this.createActionInfo = {
        type: 'relative',
        crmType: this.crmType,
        data: {}
      }
      if (type == 'money') {
        if (this.crmType === 'contract') {
          this.createActionInfo.data['customer'] = objDeepCopy(this.detail)
          this.createActionInfo.data['contract'] = objDeepCopy(this.detail)
        } else if (this.crmType === 'customer') {
          this.createActionInfo.data['customer'] = this.detail
        }
        this.createCrmType = 'receivables'
        this.isCreate = true
      } else if (type == 'plan') {
        if (this.crmType === 'contract') {
          this.createActionInfo.data['customer'] = objDeepCopy(this.detail)
          this.createActionInfo.data['contract'] = objDeepCopy(this.detail)
        } else if (this.crmType === 'customer') {
          this.createActionInfo.data['customer'] = this.detail
        }
        this.createCrmType = 'receivables_plan'
        this.isCreate = true
      }
    },

    /**
     * 新建成功
     */
    saveSuccess() {
      if (this.createCrmType == 'receivables') {
        this.getList()
      } else {
        this.getPlanList()
      }
    },

    /**
     * 编辑操作
     */
    handleFile(type, item) {
      if (type == 'edit') {
        this.createActionInfo = { type: 'update', id: item.row.planId }
        this.createCrmType = 'receivables_plan'
        this.isCreate = true
      } else if (type == 'delete') {
        this.$confirm('您确定要删除吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            crmReceivablesPlanDeleteAPI({
              planIds: item.row.planId
            })
              .then(res => {
                this.palnList.splice(item.$index, 1)
                this.$message.success('删除成功')
              })
              .catch(() => {})
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '已取消操作'
            })
          })
      }
    },

    /**
     * 通过回调控制表头style
     */
    headerRowStyle({ row, column, rowIndex, columnIndex }) {
      return { textAlign: 'center' }
    },

    /**
     * 通过回调控制style
     */
    cellStyle({ row, column, rowIndex, columnIndex }) {
      return { textAlign: 'center' }
    }
  }
}
</script>
<style lang="scss" scoped>
@import '../styles/relativecrm.scss';
</style>
