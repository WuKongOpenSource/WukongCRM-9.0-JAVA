<template>
  <div v-loading="loading"
       class="main-container">
    <div class="handle-bar">
      <el-date-picker v-model="dateSelect"
                      type="daterange"
                      :clearable="false"
                      range-separator="-"
                      format="yyyy-MM-dd"
                      value-format="yyyy-MM-dd"
                      :picker-options="pickerOptions"
                      start-placeholder="开始日期"
                      end-placeholder="结束日期">
      </el-date-picker>
      <el-select v-model="structuresSelectValue"
                 @change="structuresValueChange"
                 placeholder="选择部门">
        <el-option v-for="item in deptList"
                   :key="item.id"
                   :label="item.name"
                   :value="item.id">
        </el-option>
      </el-select>
      <el-select v-model="userSelectValue"
                 :clearable="true"
                 placeholder="选择员工">
        <el-option v-for="item in userOptions"
                   :key="item.userId"
                   :label="item.realname"
                   :value="item.userId">
        </el-option>
      </el-select>
      <el-button @click.native="handleClick('search')"
                 type="primary">搜索</el-button>
    </div>
    <div class="content">
      <div class="box">
        <el-table id="crm-table"
                  :data="newList"
                  :height="tableHeight"
                  :span-method="objectSpanMethod"
                  border
                  :cell-style="cellStyle"
                  @row-click="handleRowClick">
          <el-table-column v-for="(item, index) in headFieldList"
                           :key="index"
                           align="center"
                           header-align="center"
                           show-overflow-tooltip
                           :prop="item.field"
                           :label="item.name">
          </el-table-column>
        </el-table>
      </div>
    </div>
    <contract-detail v-if="showContractview"
                     :id="rowID"
                     @hide-view="showContractview=false"
                     class="d-view"></contract-detail>
    <customer-detail v-if="showCustomerView"
                     :id="rowID"
                     @hide-view="showCustomerView=false"
                     class="d-view"></customer-detail>
    <product-detail v-if="showProductview"
                    :id="rowID"
                    @hide-view="showProductview=false"
                    class="d-view"></product-detail>
  </div>
</template>

<script>
import { adminStructuresSubIndex, usersList } from '@/api/common'
import { biProductStatistics } from '@/api/businessIntelligence/bi'
import moment from 'moment'
import ContractDetail from '@/views/customermanagement/contract/ContractDetail'
import CustomerDetail from '@/views/customermanagement/customer/CustomerDetail'
import ProductDetail from '@/views/customermanagement/product/ProductDetail'

export default {
  /** 产品销售情况统计 */
  name: 'product-statistics',
  components: {
    ContractDetail,
    CustomerDetail,
    ProductDetail
  },
  data() {
    return {
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now()
        }
      },
      loading: false,
      tableHeight: document.documentElement.clientHeight - 170,

      dateSelect: [], // 选择的date区间
      /** 部门选择解析数据 */
      structuresProps: {
        children: 'children',
        label: 'label',
        value: 'id'
      },
      deptList: [], // 部门列表
      structuresSelectValue: '',
      /** 用户列表 */
      userOptions: [],
      userSelectValue: '',

      headFieldList: [
        { field: 'categoryName', name: '产品分类', width: '115px' },
        { field: 'productName', name: '产品名称', width: '115px' },
        { field: 'contracNum', name: '合同编号', width: '115px' },
        { field: 'ownerUserName', name: '负责人', width: '115px' },
        { field: 'customerName', name: '客户名称', width: '115px' },
        { field: 'productPrice', name: '销售单价', width: '115px' },
        { field: 'productNum', name: '数量', width: '115px' },
        { field: 'productSubtotal', name: '订单产品小计', width: '115px' }
      ],
      list: [],
      //
      spanList: [],
      newList: [],
      /** 控制详情展示 */
      showContractview: false,
      showCustomerView: false,
      showProductview: false,
      rowID: ''
    }
  },
  computed: {},
  mounted() {
    var self = this
    /** 控制table的高度 */
    window.onresize = function() {
      var offsetHei = document.documentElement.clientHeight
      self.tableHeight = offsetHei - 170
    }

    this.getDeptList()

    var endDate = moment(new Date()).format('YYYY-MM-DD')
    var times = endDate.split('-')
    var startDate = times[0] + '-' + times[1] + '-01'
    this.dateSelect = [startDate, endDate]
  },
  methods: {
    objectSpanMethod({ row, column, rowIndex, columnIndex }) {
      var item = this.spanList[rowIndex]
      if (columnIndex == 0) {
        if (item.rowspan == 0) {
          return {
            rowspan: 0,
            colspan: 0
          }
        } else {
          return {
            rowspan: item.rowspan,
            colspan: 1
          }
        }
      } else if (columnIndex == 1) {
        if (item.productRowspan == 0) {
          return {
            rowspan: 0,
            colspan: 0
          }
        } else {
          return {
            rowspan: item.productRowspan,
            colspan: 1
          }
        }
      }
    },
    /** 列表操作 */
    // 当某一行被点击时会触发该事件
    handleRowClick(row, column, event) {
      if (column.property === 'customerId') {
        if (this.showProductview) {
          this.showProductview = false
        }
        if (this.showContractview) {
          this.showContractview = false
        }
        this.rowID = row.customerId
        this.showCustomerView = true
      } else if (column.property === 'productName') {
        if (this.showCustomerView) {
          this.showCustomerView = false
        }
        if (this.showContractview) {
          this.showContractview = false
        }
        this.rowID = row.productId
        this.showProductview = true
      } else if (column.property === 'contractId') {
        if (this.showProductview) {
          this.showProductview = false
        }
        if (this.showCustomerView) {
          this.showCustomerView = false
        }
        this.rowID = row.contractId
        this.showContractview = true
      }
    },
    cellStyle({ row, column, rowIndex, columnIndex }) {
      var item = this.spanList[rowIndex]
      if (item.isSum == true) {
        return { backgroundColor: '#FFF9F2' }
      } else if (item.isAllSum == true) {
        return { backgroundColor: '#FFF3E8' }
      } else if (columnIndex === 1 || columnIndex === 2 || columnIndex === 4) {
        return { color: '#3E84E9', cursor: 'pointer' }
      }
    },
    /**
     * 获取部门列表
     */
    getDeptList() {
      this.loading = true
      adminStructuresSubIndex({ type: 'tree' })
        .then(res => {
          this.deptList = res.data
          this.loading = false
          if (res.data.length > 0) {
            this.structuresSelectValue = res.data[0].id
            this.getUserList() // 更新员工列表
            this.getProductDatalist()
          }
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 部门更改 */
    structuresValueChange(data) {
      this.userSelectValue = ''
      this.userOptions = []
      this.getUserList() // 更新员工列表
    },
    /** 部门下员工 */
    getUserList() {
      let params = { pageType: 0 }
      params.deptId = this.structuresSelectValue
      usersList(params)
        .then(res => {
          this.userOptions = res.data
        })
        .catch(() => {})
    },
    /** 顶部操作 */
    handleClick(type) {
      if (type === 'search') {
        this.getProductDatalist()
      }
    },
    /** 获取部门业绩完成信息 */
    getProductDatalist() {
      this.loading = true
      biProductStatistics({
        startTime: this.dateSelect[0],
        endTime: this.dateSelect[1],
        deptId: this.structuresSelectValue,
        userId: this.userSelectValue
      })
        .then(res => {
          this.list = res.data
          this.handleShowInfo()
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 处理展示数据 */
    handleShowInfo() {
      // 记录分类合并
      // 产品合并
      // 小产品
      /**
       * rowspan 数量
       *
       */

      var newList = []
      var spanList = []
      var seriesIndex = 0 // 操控span中元素
      var productIndex = 0

      var subCount = 0 // 产品
      var subMoney = 0
      var allCount = 0 // 系列
      var allMoney = 0

      var count
      for (let index = 0; index < this.list.length; index++) {
        const element = this.list[index]

        if (spanList.length == 0) {
          seriesIndex = 0 //一个新系列的开始
          productIndex = 0 //一个新产品的开始
          subCount = parseFloat(element.productNum) // 产品
          subMoney = parseFloat(element.productSubtotal)
          allCount = parseFloat(element.productNum) // 系列
          allMoney = parseFloat(element.productSubtotal)

          spanList.push({ rowspan: 1, productRowspan: 1 })
          newList.push(element) // 真实数据
        } else if (element.categoryId != this.list[index - 1].categoryId) {
          // 系列改变时候的逻辑
          /** 上一个最后产品的处理 */
          var preItem = spanList[seriesIndex]
          preItem.rowspan += 1
          newList.push({ productNum: subCount, productSubtotal: subMoney }) // 产品小计数据
          spanList.push({ rowspan: 0, productRowspan: 1, isSum: true }) // 产品小计style

          newList.push({ productNum: allCount, productSubtotal: allMoney }) // 系列小计数据
          spanList.push({ rowspan: 1, productRowspan: 1, isAllSum: true }) // 系列小计style

          /*** 新系列开始 */
          spanList.push({ rowspan: 1, productRowspan: 1 }) // 新系列 新产品的 展示数据开始 style
          subCount = parseFloat(element.productNum) // 新产品的值 所以取消了重置为0
          subMoney = parseFloat(element.productSubtotal)
          allCount = parseFloat(element.productNum) // 系列
          allMoney = parseFloat(element.productSubtotal)
          newList.push(element) // 真实数据
          seriesIndex = spanList.length - 1 //一个新系列的开始
          productIndex = spanList.length - 1 //一个新产品的开始
        } else {
          var preItem = spanList[seriesIndex]
          preItem.rowspan += 1
          /*** 相同产品 */
          if (element.productId == this.list[index - 1].productId) {
            var preProItem = spanList[productIndex]
            preProItem.productRowspan += 1
            spanList.push({ rowspan: 0, productRowspan: 0 }) // 产品 非第一条数据的style
            subCount += parseFloat(element.productNum) // 产品
            subMoney += parseFloat(element.productSubtotal)
            allCount += parseFloat(element.productNum) // 系列
            allMoney += parseFloat(element.productSubtotal)
            newList.push(element) // 真实数据
          } else {
            /*** 不相同产品 */
            // 需要添加一个小计
            preItem.rowspan += 1

            newList.push({ productNum: subCount, productSubtotal: subMoney }) // 产品小计数据
            spanList.push({ rowspan: 0, productRowspan: 1, isSum: true }) // 产品小计Style

            spanList.push({ rowspan: 0, productRowspan: 1 }) // 新产品 第一条数据style
            productIndex = spanList.length - 1 //一个新产品的开始=
            subCount = parseFloat(element.productNum)
            subMoney = parseFloat(element.productSubtotal) //开始了一个新的产品  所以没有 清空数据
            allCount += parseFloat(element.productNum) // 系列 继续 叠加
            allMoney += parseFloat(element.productSubtotal)
            newList.push(element) // 真实数据
          }
        }

        if (this.list.length - 1 == index) {
          // 最后一个产品的处理
          var preItem = spanList[seriesIndex]
          preItem.rowspan += 1
          newList.push({ productNum: subCount, productSubtotal: subMoney }) // 产品小计数据
          subCount = 0
          subMoney = 0 // 完成一个产品统计 清空数据
          spanList.push({ rowspan: 0, productRowspan: 1, isSum: true }) // 产品小计style

          newList.push({ productNum: allCount, productSubtotal: allMoney }) // 系列小计数据
          allCount = 0
          allMoney = 0 // 完成一个系列统计 清空数据
          spanList.push({ rowspan: 1, productRowspan: 1, isAllSum: true }) // 系列小计style
        }
      }

      this.spanList = spanList
      this.newList = newList
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import './styles/detail.scss';
@import '@/views/customermanagement/styles/detailview.scss';

.handle-bar {
  padding: 15px 20px 5px 20px;
  .el-date-editor {
    padding: 0px 10px;
    width: 240px;
    margin-right: 15px;
  }
  .el-cascader {
    width: 130px;
    margin-right: 15px;
  }
  .el-select {
    width: 120px;
    margin-right: 15px;
  }
}

.content {
  padding: 10px 20px;
}

.table-header {
  background-color: #f2f2f2;
  .header-item {
    text-align: center;
    height: 40px;
    line-height: 40px;
    border-left: 1px solid #e6e6e6;
  }
  .header-item:first-child {
    border-left: none;
  }
}
/** cell 信息 */
.table-cell {
  div {
    text-align: center;
  }
}
.series-info {
  .series-name {
    width: 115px;
    height: 100%;
  }
  .series-body {
    border-left: 1px solid #e6e6e6;
    flex: 1;
  }
}
.product-info {
  .product-name {
    width: 115px;
  }
  .product-body {
    border-left: 1px solid #e6e6e6;
    flex: 1;
  }
}

.money-cells {
  .money-cell {
    border-left: 1px solid #e6e6e6;
    width: 115px;
    height: 40px;
    line-height: 40px;
  }
  .money-cell:first-child {
    border-left: none;
  }
}
</style>
