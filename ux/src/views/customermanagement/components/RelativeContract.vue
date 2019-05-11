<template>
  <div class="rc-cont"
       v-empty="nopermission"
       xs-empty-icon="nopermission"
       xs-empty-text="暂无权限">
    <flexbox v-if="!isSeas"
             class="rc-head"
             direction="row-reverse">
      <el-button class="rc-head-item"
                 @click.native="createClick"
                 type="primary">新建合同
      </el-button>
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
                              crmType="contract"
                              :id="contractId">
    </c-r-m-full-screen-detail>
    <c-r-m-create-view v-if="isCreate"
                       crm-type="contract"
                       :action="createActionInfo"
                       @save-success="createSaveSuccess"
                       @hiden-view="isCreate=false"></c-r-m-create-view>
  </div>
</template>

<script type="text/javascript">
import loading from '../mixins/loading'
import CRMCreateView from './CRMCreateView'
import { crmCustomerQueryContract } from '@/api/customermanagement/customer'
import { crmBusinessQueryContract } from '@/api/customermanagement/business'

export default {
  name: 'relative-contract', //相关联系人  可能再很多地方展示 放到客户管理目录下
  components: {
    CRMFullScreenDetail: () => import('./CRMFullScreenDetail.vue'),
    CRMCreateView
  },
  computed: {},
  mixins: [loading],
  data() {
    return {
      nopermission: false,
      list: [],
      fieldList: [],
      tableHeight: '400px',
      showFullDetail: false,
      isCreate: false, // 控制新建
      contractId: '', // 查看全屏联系人详情的 ID
      // 创建的相关信息
      createActionInfo: { type: 'relative', crmType: this.crmType, data: {} }
    }
  },
  watch: {
    id: function(val) {
      this.list = []
      this.getDetail()
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
    /** 联系人人下 新建商机 需要联系人里的客户信息  合同下需要客户和商机信息 */
    detail: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  mounted() {
    this.getDetail()
  },
  activated: function() {},
  deactivated: function() {},
  methods: {
    getFieldList() {
      this.fieldList.push({ prop: 'num', width: '200', label: '合同编号' })
      this.fieldList.push({
        prop: 'contractName',
        width: '200',
        label: '合同名称'
      })
      this.fieldList.push({
        prop: 'customerName',
        width: '200',
        label: '客户名称'
      })
      this.fieldList.push({ prop: 'money', width: '200', label: '合同金额' })
      this.fieldList.push({
        prop: 'startTime',
        width: '200',
        label: '开始日期'
      })

      this.fieldList.push({ prop: 'endTime', width: '200', label: '结束日期' })
    },
    getDetail() {
      this.loading = true
      let request = {
        customer: crmCustomerQueryContract,
        business: crmBusinessQueryContract
      }[this.crmType]
      let params = { pageType: 0 }
      params[this.crmType + 'Id'] = this.id
      request(params)
        .then(res => {
          if (this.fieldList.length == 0) {
            this.getFieldList()
          }
          this.nopermission = false
          this.loading = false
          this.list = res.data
        })
        .catch(data => {
          if (data.code == 102) {
            this.nopermission = true
          }
          this.loading = false
        })
    },
    //当某一行被点击时会触发该事件
    handleRowClick(row, column, event) {
      this.contractId = row.contractId
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
      /** 客户 和 商机 下新建合同 */
      if (this.crmType == 'business') {
        this.createActionInfo.data['customer'] = this.detail
        this.createActionInfo.data['business'] = this.detail
      } else if (this.crmType == 'customer') {
        this.createActionInfo.data['customer'] = this.detail
      }
      this.isCreate = true
    },
    createSaveSuccess() {
      this.getDetail()
    }
  }
}
</script>
<style lang="scss" scoped>
@import '../styles/relativecrm.scss';
</style>
