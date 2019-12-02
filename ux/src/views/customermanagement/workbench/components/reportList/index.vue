<template>
  <transition name="opacity-fade">
    <div
      :style="{'padding': padding+' 0', 'z-index': zIndex }"
      class="container">
      <div class="content">
        <div
          class="header"
          @click="showDview = false">
          <span class="title">{{ title }}</span>
          <el-input
            v-if="placeholder"
            :placeholder="placeholder"
            v-model="inputContent"
            class="search-input"
            @keyup.enter.native="searchInput">
            <el-button
              slot="append"
              icon="el-icon-search"
              @click.native="searchInput"/>
          </el-input>
          <img
            class="close"
            src="@/assets/img/task_close.png"
            @click="hideView" >
        </div>
        <div class="list-body">
          <el-table
            v-loading="loading"
            id="crm-table"
            :data="list"
            :height="tableHeight"
            :cell-style="cellStyle"
            stripe
            border
            highlight-current-row
            style="width: 100%"
            @row-click="handleRowClick"
            @sort-change="sortChange">
            <el-table-column
              v-for="(item, index) in showFieldList"
              :key="index"
              :sortable="sortable"
              :fixed="index==0"
              :prop="item.prop"
              :label="item.label"
              :width="item.width"
              :formatter="fieldFormatter"
              show-overflow-tooltip>
              <template
                slot="header"
                slot-scope="scope">
                <div class="table-head-name">{{ scope.column.label }}</div>
              </template>
            </el-table-column>
            <el-table-column
              v-if="showPoolDayField"
              :resizable="false"
              prop="poolDay"
              show-overflow-tooltip
              label="距进入公海天数"
              width="120">
              <template slot-scope="scope">
                <div v-if="scope.row.isLock == 0">{{ scope.row.poolDay }}</div>
                <i
                  v-else
                  class="wukong wukong-lock customer-lock"/>
              </template>
            </el-table-column>
            <el-table-column
              v-if="showExamineStatus"
              :resizable="false"
              show-overflow-tooltip
              prop="checkStatus"
              label="状态"
              width="100"
              align="center"
              fixed="right">
              <template
                slot="header"
                slot-scope="scope">
                <div class="table-head-name">{{ scope.column.label }}</div>
              </template>
              <template slot-scope="scope">
                <div
                  :style="getStatusStyle(scope.row.checkStatus)"
                  class="status_button">
                  {{ scope.row.checkStatus }}
                </div>
              </template>
            </el-table-column>
            <el-table-column v-if="showFillColumn"/>
          </el-table>
          <div
            v-if="showPagination"
            class="p-contianer">
            <el-pagination
              :current-page="currentPage"
              :page-sizes="pageSizes"
              :page-size.sync="pageSize"
              :total="total"
              class="p-bar"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"/>
          </div>
        </div>
      </div>
      <!-- 相关详情页面 -->
      <c-r-m-all-detail
        :visible.sync="showDview"
        :crm-type="rowType"
        :id="rowID"
        class="d-view"
        @handle="handleHandle"/>

      <record-list
        v-if="recordShow"
        :crm-type="rowType"
        :params="recordParams"
        @handle="getList"
        @hide="recordShow = false"/>
    </div>
  </transition>
</template>
<script type="text/javascript">
import { filedGetTableField } from '@/api/customermanagement/common'
import crmTypeModel from '@/views/customermanagement/model/crmTypeModel'
import { getMaxIndex } from '@/utils/index'
import { mapGetters } from 'vuex'
import Lockr from 'lockr'
import CRMAllDetail from '@/views/customermanagement/components/CRMAllDetail'
import RecordList from './components/recordList'

export default {
  name: 'ReportList', // 简报列表
  components: {
    CRMAllDetail,
    RecordList
  },
  props: {
    /** 展示内容的上下padding */
    padding: {
      type: String,
      default: '0'
    },
    title: String,
    placeholder: {
      type: String,
      default: '请输入搜索内容'
    },
    crmType: String,
    fieldList: Array,
    request: {
      type: Function,
      required: true
    },
    params: Object
  },
  data() {
    return {
      zIndex: getMaxIndex(),
      inputContent: '',

      loading: false, // 加载动画
      tableHeight: document.documentElement.clientHeight - 115, // 表的高度
      list: [],
      showFieldList: [],
      sortData: {}, // 字段排序
      currentPage: 1,
      pageSize: Lockr.get('crmPageSizes') || 15,
      pageSizes: [15, 30, 60, 100],
      total: 0,

      /** 控制详情展示 */
      rowID: '', // 行信息
      rowType: '', // 详情类型
      showDview: false,
      /** 格式化规则 */
      formatterRules: {},

      recordParams: {},
      recordShow: false
    }
  },
  computed: {
    ...mapGetters(['crm', 'CRMConfig']),
    showPoolDayField() {
      if (this.crmType == 'customer' && this.CRMConfig.customerConfig == 1) {
        return true
      }
      return false
    },
    showExamineStatus() {
      if (this.crmType == 'contract' && this.crmType == 'receivables') {
        return true
      }
      return false
    },
    sortable() {
      if (this.fieldList && this.fieldList.length) {
        return false
      }
      return 'custom'
    },
    showFillColumn() {
      if (this.fieldList && this.fieldList.length) {
        return false
      }
      return true
    },
    showPagination() {
      if (this.fieldList && this.fieldList.length) {
        return false
      }
      return true
    }
  },
  watch: {},
  mounted() {
    document.body.appendChild(this.$el)

    window.onresize = () => {
      self.tableHeight = document.documentElement.clientHeight - 115
    }
    if (this.fieldList) {
      this.showFieldList = this.fieldList
      this.getList()
    } else {
      this.getFieldList()
    }
  },

  destroyed() {
    // remove DOM node after destroy
    if (this.$el && this.$el.parentNode) {
      this.$el.parentNode.removeChild(this.$el)
    }
  },
  methods: {
    /**
     * 搜索
     */
    searchInput() {
      this.currentPage = 1
      this.getList()
    },

    /**
     * 获取列表数据
     */
    getList() {
      this.loading = true
      var params = {
        page: this.currentPage,
        limit: this.pageSize,
        search: this.inputContent,
        type: crmTypeModel[this.crmType]
      }

      if (this.sortData.order) {
        params.sortField = this.sortData.prop
        params.order = this.sortData.order == 'ascending' ? 2 : 1
      }

      this.request({ ...params, ...this.params })
        .then(res => {
          if (res.data.list) {
            this.list = res.data.list
            this.total = res.data.totalRow
          } else {
            this.list = res.data
          }
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * 获取表头字段
     */
    getFieldList() {
      if (this.showFieldList.length == 0) {
        this.loading = true
        const crmType =
          this.crmType == 'business_status' ? 'business' : this.crmType
        var params = {
          label: crmTypeModel[crmType]
        }

        filedGetTableField(params)
          .then(res => {
            for (let index = 0; index < res.data.length; index++) {
              const element = res.data[index]

              var width = 0
              if (!element.width) {
                if (element.name && element.name.length <= 6) {
                  width = element.name.length * 15 + 45
                } else {
                  width = 140
                }
              } else {
                width = element.width
              }

              this.showFieldList.push({
                prop: element.fieldName,
                label: element.name,
                width: width
              })
            }

            // 获取好字段开始请求数据
            this.getList()
          })
          .catch(() => {
            this.loading = false
          })
      } else {
        // 获取好字段开始请求数据
        this.getList()
      }
    },

    /**
     * 格式化字段
     */
    fieldFormatter(row, column) {
      // 如果需要格式化
      if (this.fieldList && this.fieldList.length) {
        if (column.property == 'crmType') {
          return {
            crm_leads: '线索',
            crm_customer: '客户',
            crm_contacts: '联系人',
            crm_business: '商机',
            crm_contract: '合同'
          }[row[column.property]]
        }
      }
      return row[column.property] || '--'
    },

    /**
     * 字段排序
     */
    sortChange(column, prop, order) {
      this.sortData = column
      this.getList()
    },

    /**
     * 更改每页展示数量
     */
    handleSizeChange(val) {
      Lockr.set('crmPageSizes', val)
      this.pageSize = val
      this.getList()
    },

    /**
     * 更改当前页数
     */
    handleCurrentChange(val) {
      this.currentPage = val
      this.getList()
    },

    /**
     * 0待审核、1审核中、2审核通过、3审核未通过
     */
    getStatusStyle(status) {
      if (status == 0) {
        return {
          'border-color': '#E6A23C',
          'background-color': '#FDF6EC',
          color: '#E6A23C'
        }
      } else if (status == 1) {
        return {
          'border-color': '#409EFF',
          'background-color': '#ECF5FF',
          color: '#409EFF'
        }
      } else if (status == 2) {
        return {
          'border-color': '#67C23A',
          'background-color': '#F0F9EB',
          color: '#67C23A'
        }
      } else if (status == 3) {
        return {
          'border-color': '#F56C6B',
          'background-color': '#FEF0F0',
          color: '#F56C6B'
        }
      } else if (status == 4) {
        return {
          'background-color': '#FFFFFF'
        }
      }
    },

    /**
     * 查看详情
     */
    handleRowClick(row, column, event) {
      if (this.crmType === 'leads') {
        if (column.property === 'leadsName') {
          this.rowID = row.leadsId
          this.showDview = true
        } else {
          this.showDview = false
        }
      } else if (this.crmType === 'customer') {
        if (column.property === 'customerName') {
          this.rowID = row.customerId
          this.rowType = 'customer'
          this.showDview = true
        } else {
          this.showDview = false
        }
      } else if (this.crmType === 'contacts') {
        if (column.property === 'customerName') {
          this.rowID = row.customerId
          this.rowType = 'customer'
          this.showDview = true
        } else if (column.property === 'name') {
          this.rowID = row.contactsId
          this.rowType = 'contacts'
          this.showDview = true
        } else {
          this.showDview = false
        }
      } else if (
        this.crmType === 'business' ||
        this.crmType === 'business_status'
      ) {
        if (column.property === 'customerName') {
          this.rowID = row.customerId
          this.rowType = 'customer'
          this.showDview = true
        } else if (column.property === 'businessName') {
          this.rowID = row.businessId
          this.rowType = 'business'
          this.showDview = true
        } else {
          this.showDview = false
        }
      } else if (this.crmType === 'contract') {
        if (column.property === 'customerName') {
          this.rowID = row.customerId
          this.rowType = 'customer'
          this.showDview = true
        } else if (column.property === 'businessName') {
          this.rowID = row.businessId
          this.rowType = 'business'
          this.showDview = true
        } else if (column.property === 'contactsName') {
          this.rowID = row.contactsId
          this.rowType = 'contacts'
          this.showDview = true
        } else if (column.property === 'num') {
          this.rowID = row.contractId
          this.rowType = 'contract'
          this.showDview = true
        } else {
          this.showDview = false
        }
      } else if (this.crmType === 'product') {
        if (column.property === 'name') {
          this.rowID = row.productId
          this.showDview = true
        } else {
          this.showDview = false
        }
      } else if (this.crmType === 'receivables') {
        if (column.property === 'customerName') {
          this.rowID = row.customerId
          this.rowType = 'customer'
          this.showDview = true
        } else if (column.property === 'contractNum') {
          this.rowID = row.contractId
          this.rowType = 'contract'
          this.showDview = true
        } else if (column.property === 'number') {
          this.rowID = row.receivablesId
          this.rowType = 'receivables'
          this.showDview = true
        } else {
          this.showDview = false
        }
      } else if (this.crmType === 'record') {
        if (column.property === 'count' && row.count) {
          this.rowType = row.crmType
          this.recordParams = { crmType: row.crmType, ...this.params }
          this.recordShow = true
        } else {
          this.recordShow = false
        }
      }
    },

    /**
     * 详情操作
     */
    handleHandle(data) {
      if (
        data.type === 'alloc' ||
        data.type === 'get' ||
        data.type === 'transfer' ||
        data.type === 'transform' ||
        data.type === 'delete' ||
        data.type === 'put_seas'
      ) {
        this.showDview = false
      }

      if (data.type !== 'edit') {
        this.getList()
      }
    },

    /**
     * 通过回调控制style
     */
    cellStyle({ row, column, rowIndex, columnIndex }) {
      if (
        (column.property === 'customerName' ||
          column.property === 'businessName' ||
          column.property === 'name' ||
          column.property === 'contactsName' ||
          column.property === 'num' ||
          column.property === 'contractNum' ||
          column.property === 'number' ||
          (this.crmType === 'record' && column.property === 'count')) &&
        row[column.property]
      ) {
        return { color: '#3E84E9', cursor: 'pointer' }
      } else {
        return ''
      }
    },

    /**
     * 隐藏视图
     */
    hideView() {
      this.$emit('hide')
    }
  }
}
</script>
<style lang="scss" scoped>
.opacity-fade-enter-active,
.opacity-fade-leave-active {
  transition: all 0.2s;
}
.opacity-fade-enter,
.opacity-fade-leave-to {
  opacity: 0;
}
/** 容器布局 */
.container {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  right: 0;
  background-color: white;
}

.content {
  position: relative;
  height: 100%;

  .header {
    position: relative;
    .title {
      padding: 0 20px;
      font-size: 18px;
      line-height: 60px;
    }

    .search-input {
      width: 300px;
      margin: -18px 0 0 -150px;
      position: absolute;
      left: 50%;
      top: 50%;
    }

    .close {
      display: block;
      float: right;
      width: 40px;
      height: 40px;
      margin-left: 20px;
      margin-top: 10px;
      margin-right: 10px;
      padding: 10px;
      cursor: pointer;
    }
  }
}

.p-contianer {
  position: relative;
  background-color: white;
  height: 44px;

  .p-bar {
    float: right;
    margin: 5px 100px 0 0;
    font-size: 14px !important;
  }
}

.customer-lock {
  color: #f15e64;
}

.status_button {
  padding: 1px 6px;
  border: 1px solid #e6e6e6;
  border-radius: 4px;
  display: inline-block;
  font-size: 12px;
  margin: 0 auto;
}

.table-head-name {
  font-size: 14px;
  color: #475059;
  line-height: 23px;
  padding: 0;
}

.el-table /deep/ thead th {
  background-color: #fafdff;
  font-weight: 400;
  border-color: #e6e6e6;
  color: #475059;
}

.el-table /deep/ thead .cell {
  height: 23px;
}

.el-table /deep/ .sort-caret.ascending {
  top: -6px;
}
.el-table /deep/ .caret-wrapper {
  height: 24px;
}
</style>
