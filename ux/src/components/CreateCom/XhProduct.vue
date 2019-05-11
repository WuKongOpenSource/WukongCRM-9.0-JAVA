<template>
  <div>
    <div class="handel-header">
      <el-popover v-model="showPopover"
                  placement="bottom"
                  width="700"
                  style="padding: 0 !important;"
                  trigger="click">
        <crm-relative ref="crmrelative"
                      crm-type="product"
                      v-if="showSelectView"
                      :radio="false"
                      @close="showPopover=false"
                      @changeCheckout="selectInfos"></crm-relative>
        <el-button slot="reference"
                   type="primary"
                   @click="showSelectView=true">添加产品</el-button>
      </el-popover>
    </div>
    <el-table :data="productList"
              style="width: 620px;">
      <el-table-column prop="name"
                       label="产品名称">
      </el-table-column>
      <el-table-column prop="categoryName"
                       label="产品类别">
      </el-table-column>
      <el-table-column prop="unit"
                       label="单位">
      </el-table-column>
      <el-table-column prop="price"
                       label="标准价格">
      </el-table-column>
      <el-table-column label="售价">
        <template slot-scope="scope">
          <el-input v-model="scope.row.salesPrice"
                    @input="salesPriceChange(scope)"
                    placeholder="请输入"></el-input>
        </template>
      </el-table-column>
      <el-table-column label="数量">
        <template slot-scope="scope">
          <el-input v-model="scope.row.num"
                    @input="numChange(scope)"
                    placeholder="请输入"></el-input>
        </template>
      </el-table-column>
      <el-table-column label="折扣（%）">
        <template slot-scope="scope">
          <el-input v-model="scope.row.discount"
                    @input="discountChange(scope)"
                    placeholder="请输入"></el-input>
        </template>
      </el-table-column>
      <el-table-column prop="subtotal"
                       label="合计">
      </el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button @click="removeItem(scope.$index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <flexbox class="handle-footer">
      <div class="discount-title">整单折扣（%）：</div>
      <el-input style="width: 80px"
                v-model="discountRate"
                @input="rateChange"
                placeholder="请输入"></el-input>
      <div class="total-info">已选中产品：<span class="info-yellow">{{productList.length}}</span>&nbsp;种&nbsp;&nbsp;总金额：<span class="info-yellow">{{totalPrice}}</span>&nbsp;元</div>
    </flexbox>
  </div>
</template>
<script type="text/javascript">
import objMixin from './objMixin'
import CrmRelative from '@/components/CreateCom/CrmRelative'

export default {
  name: 'xh-product', // 关联产品
  components: {
    CrmRelative
  },
  mixins: [objMixin],
  computed: {},
  watch: {
    dataValue: function(value) {
      this.selectInfos({ data: value.product })
    }
  },
  data() {
    return {
      showPopover: false, // 展示产品框
      showSelectView: false, // 内容
      productList: [],
      totalPrice: 0,
      discountRate: 0
    }
  },
  props: {},
  mounted() {
    if (this.dataValue.product) {
      this.productList = this.dataValue.product
      this.totalPrice = this.dataValue.totalPrice
        ? this.dataValue.totalPrice
        : 0
      this.discountRate = this.dataValue.discountRate
        ? this.dataValue.discountRate
        : 0
    }
  },
  methods: {
    /** 选中 */
    selectInfos(data) {
      if (data.data) {
        var self = this
        data.data.forEach(function(element) {
          let obj = self.productList.find(item => {
            return item.productId == element.productId
          })
          if (!obj) {
            self.productList.push(self.getShowItem(element))
          }
        })
      }
    },
    getShowItem(data) {
      var item = {}
      item.name = data.name
      item.categoryName = data.categoryName
      item.unit = data.unit
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
      var item = data.row
      if (item.salesPrice === '') {
        item.salesPrice = 0.0
      }

      var discount = ((item.price - item.salesPrice) / item.price) * 100.0
      discount = discount.toFixed(2)
      if (item.discount !== discount) {
        item.discount = discount
      }
      this.calculateSubTotal(item)
      this.calculateToal()
    },
    // 数量
    numChange(data) {
      var item = data.row
      if (item.count === '') {
        item.count = 1
      }
      this.calculateSubTotal(item)
      this.calculateToal()
    },
    // 折扣
    discountChange(data) {
      var item = data.row
      var salesPrice = (item.price * (100.0 - item.discount)) / 100.0
      salesPrice = salesPrice.toFixed(2)
      if (item.salesPrice !== salesPrice) {
        item.salesPrice = salesPrice
      }
      this.calculateSubTotal(item)
      this.calculateToal()
    },
    // 计算单价
    calculateSubTotal(item) {
      item.subtotal = (item.salesPrice * item.num).toFixed(2)
    },
    // 计算总价
    calculateToal() {
      var totalPrice = 0.0
      for (var i = 0; i < this.productList.length; i++) {
        var item = this.productList[i]
        totalPrice = parseFloat(totalPrice) + parseFloat(item.subtotal)
      }
      totalPrice = (totalPrice * (100.0 - this.discountRate)) / 100.0
      this.totalPrice = totalPrice.toFixed(2)
      this.updateValue() // 传递数据给父组件
    },
    // 总折扣
    rateChange() {
      this.calculateToal()
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
