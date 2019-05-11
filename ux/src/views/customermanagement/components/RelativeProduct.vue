<template>
  <div class="rc-cont"
       v-empty="nopermission"
       xs-empty-icon="nopermission"
       xs-empty-text="暂无权限">
    <flexbox class="rc-head"
             direction="row-reverse">
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
    <flexbox class="handle-footer">
      <div class="discount-title">整单折扣（%）：<span class="discount-title-value">{{totalInfo.discountRate}}</span></div>
      <div class="total-info">已选中产品：<span class="info-yellow">{{list.length}}</span>&nbsp;种&nbsp;&nbsp;总金额：<span class="info-yellow">{{totalInfo.money}}</span>&nbsp;元</div>
    </flexbox>
    <c-r-m-full-screen-detail :visible.sync="showFullDetail"
                              crmType="product"
                              :id="productId">
    </c-r-m-full-screen-detail>
  </div>
</template>

<script type="text/javascript">
import loading from '../mixins/loading'
import { crmBusinessProduct } from '@/api/customermanagement/business'
import { crmContractProduct } from '@/api/customermanagement/contract'

export default {
  name: 'relative-product', //相关产品  可能再很多地方展示 放到客户管理目录下
  components: {
    CRMFullScreenDetail: () => import('./CRMFullScreenDetail.vue')
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
      productId: '', // 查看全屏产品详情的 ID
      totalInfo: { money: '0.00', discountRate: '0.00' }
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
    /** 联系人人下 新建商机 需要联系人里的客户信息  合同下需要客户和商机信息 */
    detail: {
      type: Object,
      default: () => {
        return {}
      }
    },
    /** 没有值就是全部类型 有值就是当个类型 */
    crmType: {
      type: String,
      default: ''
    },
    /** 是公海 默认是客户 */
    isSeas: {
      type: Boolean,
      default: false
    }
  },
  mounted() {
    this.getDetail()
  },
  activated: function() {},
  deactivated: function() {},
  methods: {
    getFieldList() {
      this.fieldList.push({
        prop: 'productName',
        width: '200',
        label: '产品名称'
      })
      this.fieldList.push({
        prop: 'categoryName',
        width: '200',
        label: '产品类别'
      })
      this.fieldList.push({ prop: 'unit', width: '200', label: '单位' })
      this.fieldList.push({ prop: 'price', width: '200', label: '标准价格' })
      this.fieldList.push({ prop: 'salesPrice', width: '200', label: '售价' })
      this.fieldList.push({ prop: 'num', width: '200', label: '数量' })
      this.fieldList.push({
        prop: 'discount',
        width: '200',
        label: '折扣（%）'
      })
      this.fieldList.push({ prop: 'subtotal', width: '200', label: '合计' })
    },
    getDetail() {
      this.loading = true
      this.getRequest()(this.getParams())
        .then(res => {
          if (this.fieldList.length == 0) {
            this.getFieldList()
          }
          this.nopermission = false
          this.loading = false
          this.list = res.data.list
          this.totalInfo.money = res.data.money
          this.totalInfo.discountRate = res.data.discountRate
        })
        .catch(data => {
          if (data.code == 102) {
            this.nopermission = true
          }
          this.loading = false
        })
    },
    getRequest() {
      if (this.crmType == 'business') {
        return crmBusinessProduct
      } else if (this.crmType == 'contract') {
        return crmContractProduct
      }
    },
    getParams() {
      if (this.crmType == 'business') {
        return {
          businessId: this.id,
          pageType: 0
        }
      } else if (this.crmType == 'contract') {
        return {
          contractId: this.id,
          pageType: 0
        }
      }
    },
    //当某一行被点击时会触发该事件
    handleRowClick(row, column, event) {
      this.productId = row.productId
      this.showFullDetail = true
    },
    /** 通过回调控制表头style */
    headerRowStyle({ row, column, rowIndex, columnIndex }) {
      return { textAlign: 'center' }
    },
    /** 通过回调控制style */
    cellStyle({ row, column, rowIndex, columnIndex }) {
      return { textAlign: 'center' }
    }
  }
}
</script>
<style lang="scss" scoped>
@import '../styles/relativecrm.scss';

.handle-footer {
  position: relative;
  font-size: 12px;
  padding: 8px 5px;
  .discount-title {
    color: #666;
    .discount-title-value {
      color: #333;
    }
  }
  .total-info {
    position: absolute;
    right: 20px;
    top: 5px;
    .info-yellow {
      color: #fd715a;
    }
  }
}
</style>
