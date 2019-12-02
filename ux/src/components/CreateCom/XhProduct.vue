<template>
  <div>
    <div class="handel-header">
      <el-popover
        v-model="showPopover"
        placement="bottom"
        width="700"
        style="padding: 0 !important;"
        trigger="click">
        <crm-relative
          v-if="showSelectView"
          ref="crmrelative"
          :radio="false"
          :show="showPopover"
          :selected-data="selectedData"
          crm-type="product"
          @close="showPopover=false"
          @changeCheckout="selectInfos"/>
        <el-button
          slot="reference"
          type="primary"
          @click="showSelectView=true">添加产品</el-button>
      </el-popover>
    </div>
    <el-table
      :data="productList"
      style="width: 620px;">
      <el-table-column
        prop="name"
        label="产品名称"/>
      <el-table-column
        prop="categoryName"
        label="产品类别"/>
      <el-table-column
        prop="unit"
        label="单位"/>
      <el-table-column
        prop="price"
        label="标准价格"/>
      <el-table-column label="售价">
        <template slot-scope="scope">
          <el-input
            v-model="scope.row.salesPrice"
            placeholder="请输入"
            @input="salesPriceChange(scope)"/>
        </template>
      </el-table-column>
      <el-table-column label="数量">
        <template slot-scope="scope">
          <el-input
            v-model="scope.row.num"
            placeholder="请输入"
            @input="numChange(scope)"/>
        </template>
      </el-table-column>
      <el-table-column label="折扣（%）">
        <template slot-scope="scope">
          <el-input
            v-model="scope.row.discount"
            placeholder="请输入"
            @input="discountChange(scope)"/>
        </template>
      </el-table-column>
      <el-table-column
        prop="subtotal"
        label="合计"/>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button @click="removeItem(scope.$index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <flexbox class="handle-footer">
      <div class="discount-title">整单折扣（%）：</div>
      <el-input
        v-model="discountRate"
        style="width: 80px"
        placeholder="请输入"
        @input="rateChange"/>
      <div class="total-info">已选中产品：
        <span class="info-yellow">{{ productList.length }}</span>&nbsp;种&nbsp;&nbsp;总金额：
        <el-input
          v-model="totalPrice"
          style="width: 80px"
          placeholder="请输入"
          @input="totalPriceChange"
          @blur="totalPrice || (totalPrice = 0)"/>&nbsp;元
      </div>
    </flexbox>
  </div>
</template>
<script type="text/javascript">
import objMixin from './objMixin'
import CrmRelative from '@/components/CreateCom/CrmRelative'

export default {
  name: 'XhProduct', // 关联产品
  components: {
    CrmRelative
  },
  mixins: [objMixin],
  props: {},
  data() {
    return {
      showPopover: false, // 展示产品框
      showSelectView: false, // 内容
      productList: [],
      totalPrice: 0,
      discountRate: 0,
      selectedData: { product: [] }
    }
  },
  computed: {},
  watch: {
    dataValue: function(value) {
      this.refreshProductList()
    },
    productList() {
      this.selectedData = { product: this.productList || [] }
    }
  },
  mounted() {
    this.refreshProductList()
  },
  methods: {
    /**
     * 刷新数据
     */
    refreshProductList() {
      this.productList = this.dataValue.product || []
      this.totalPrice = this.dataValue.totalPrice || 0
      this.discountRate = this.dataValue.discountRate || 0
    },
    /** 选中 */
    selectInfos(data) {
      if (data.data) {
        const newSelects = []
        data.data.forEach(element => {
          const obj = this.productList.find(item => {
            return item.productId == element.productId
          })
          if (obj) {
            newSelects.push(obj)
          } else {
            newSelects.push(this.getShowItem(element))
          }
        })
        this.productList = newSelects
        this.calculateToal()
      }
    },
    getShowItem(data) {
      const item = {}
      item.name = data.name
      item.categoryName = data.categoryName
      item.unit = data.单位
      item.price = data.price
      item.salesPrice = data.price
      item.num = 0
      item.discount = 0
      item.subtotal = 0
      item.productId = data.productId
      return item
    },
    // 单价
    salesPriceChange(data) {
      this.verifyNumberValue(data, 'salesPrice')
      const item = data.row

      let discount = ((item.price - item.salesPrice || 0) / item.price) * 100.0
      discount = discount.toFixed(2)
      if (item.discount !== discount) {
        item.discount = discount
      }
      this.calculateSubTotal(item)
      this.calculateToal()
    },
    // 数量
    numChange(data) {
      this.verifyNumberValue(data, 'num')
      const item = data.row
      this.calculateSubTotal(item)
      this.calculateToal()
    },
    // 折扣
    discountChange(data) {
      this.verifyNumberValue(data, 'discount')
      const item = data.row
      let salesPrice =
        (item.price * (100.0 - parseFloat(item.discount || 0))) / 100.0
      salesPrice = salesPrice.toFixed(2)
      if (item.salesPrice !== salesPrice) {
        item.salesPrice = salesPrice
      }
      this.calculateSubTotal(item)
      this.calculateToal()
    },
    // 计算单价
    calculateSubTotal(item) {
      item.subtotal = (item.salesPrice * parseFloat(item.num || 0)).toFixed(2)
    },
    // 计算总价
    calculateToal() {
      let totalPrice = this.getProductTotal()
      totalPrice =
        (totalPrice * (100.0 - parseFloat(this.discountRate || 0))) / 100.0
      this.totalPrice = totalPrice.toFixed(2)
      this.updateValue() // 传递数据给父组件
    },
    /**
     * 获取产品总价(未折扣)
     */
    getProductTotal() {
      let totalPrice = 0.0
      for (let i = 0; i < this.productList.length; i++) {
        const item = this.productList[i]
        totalPrice += parseFloat(item.subtotal)
      }
      return totalPrice
    },
    // 总折扣
    rateChange() {
      if (/^\d+\.?\d{0,2}$/.test(this.discountRate)) {
        this.discountRate = this.discountRate
      } else {
        this.discountRate = this.discountRate.substring(
          0,
          this.discountRate.length - 1
        )
      }
      this.calculateToal()
    },
    /**
     * 总价更改 折扣更改
     */
    totalPriceChange() {
      if (/^\d+\.?\d{0,2}$/.test(this.totalPrice)) {
        this.totalPrice = this.totalPrice || 0
      } else {
        this.totalPrice =
          this.totalPrice.substring(0, this.totalPrice.length - 1) || 0
      }
      const totalPrice = this.getProductTotal()
      if (totalPrice) {
        this.discountRate = (
          100.0 -
          (parseFloat(this.totalPrice) / totalPrice) * 100
        ).toFixed(2)
      }
      this.updateValue()
    },
    // 删除操作
    removeItem(index) {
      this.productList.splice(index, 1)
      this.calculateToal()
    },
    updateValue() {
      this.valueChange({
        product: this.productList,
        totalPrice: this.totalPrice,
        discountRate: this.discountRate
      })
    },
    /**
     * 验证数据数值是否符合
     */
    verifyNumberValue(data, field) {
      if (/^\d+\.?\d{0,2}$/.test(data.row[field])) {
        data.row[field] = data.row[field]
      } else {
        data.row[field] = data.row[field].substring(
          0,
          data.row[field].length - 1
        )
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.handel-header {
  button {
    float: right;
    margin-bottom: 10px;
  }
}

.el-table /deep/ thead th {
  font-size: 12px;
}

.el-table /deep/ tbody tr td {
  font-size: 12px;
}

.handle-footer {
  position: relative;
  font-size: 12px;
  padding: 5px;
  .discount-title {
    color: #666;
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
