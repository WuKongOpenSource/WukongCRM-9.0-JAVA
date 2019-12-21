<template>
  <div>
    <c-r-m-list-head
      ref="listHead"
      :search.sync="search"
      :crm-type="crmType"
      title="合同管理"
      placeholder="请输入合同名称"
      main-title="新建合同"
      @on-handle="listHeadHandle"
      @on-search="crmSearch"/>
    <div
      v-empty="!crm.contract.index"
      xs-empty-icon="nopermission"
      xs-empty-text="暂无权限"
      class="crm-container">
      <c-r-m-table-head
        ref="crmTableHead"
        :crm-type="crmType"
        @exportData="exportData"
        @filter="handleFilter"
        @handle="handleHandle"
        @scene="handleScene"/>
      <el-table
        v-loading="loading"
        id="crm-table"
        :data="list"
        :height="tableHeight"
        :cell-style="cellStyle"
        class="n-table--border"
        stripe
        border
        highlight-current-row
        style="width: 100%"
        @row-click="handleRowClick"
        @sort-change="sortChange"
        @header-dragend="handleHeaderDragend"
        @selection-change="handleSelectionChange">
        <el-table-column
          show-overflow-tooltip
          type="selection"
          align="center"
          width="55"/>
        <el-table-column
          v-for="(item, index) in fieldList"
          :key="index"
          :fixed="index==0"
          :prop="item.prop"
          :label="item.label"
          :width="item.width"
          :formatter="fieldFormatter"
          sortable="custom"
          show-overflow-tooltip>
          <template
            slot="header"
            slot-scope="scope">
            <div class="table-head-name">{{ scope.column.label }}</div>
          </template>
        </el-table-column>
        <el-table-column
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
              {{ getStatusName(scope.row.checkStatus) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column/>
        <el-table-column
          fixed="right"
          width="36">
          <template
            slot="header"
            slot-scope="slot">
            <img
              src="@/assets/img/t_set.png"
              class="table-set"
              @click="handleTableSet" >
          </template>
        </el-table-column>
      </el-table>
      <div class="p-contianer">
        <el-pagination
          :current-page="currentPage"
          :page-sizes="pageSizes"
          :page-size.sync="pageSize"
          :total="total"
          class="p-bar"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"/>
        <span class="money-bar">合同总金额：{{ moneyPageData.contractMoney || 0 }} / 已回款金额：{{ moneyPageData.receivedMoney || 0 }}</span>
      </div>
    </div>
    <!-- 相关详情页面 -->
    <c-r-m-all-detail
      :visible.sync="showDview"
      :crm-type="rowType"
      :id="rowID"
      class="d-view"
      @handle="handleHandle"/>
    <fields-set
      :crm-type="crmType"
      :dialog-visible.sync="showFieldSet"
      @set-success="setSave"/>
  </div>
</template>

<script>
import CRMAllDetail from '@/views/customermanagement/components/CRMAllDetail'
import table from '../mixins/table'

export default {
  /** 客户管理 的 合同列表 */
  name: 'ContractIndex',
  components: {
    CRMAllDetail
  },
  mixins: [table],
  data() {
    return {
      crmType: 'contract',
      moneyData: null // 合同列表金额
    }
  },
  computed: {
    moneyPageData() {
      // 未勾选展示合同总金额信息
      if (this.selectionList.length == 0 && this.moneyData) {
        return this.moneyData
      } else {
        let contractMoney = 0.0
        let receivedMoney = 0.0
        for (let index = 0; index < this.selectionList.length; index++) {
          const element = this.selectionList[index]
          // 2 审核通过的合同
          if (element.checkStatus == 1) {
            contractMoney += parseFloat(element.money)
            receivedMoney += parseFloat(element.receivedMoney)
          }
        }
        return {
          contractMoney: contractMoney.toFixed(2),
          receivedMoney: receivedMoney.toFixed(2)
        }
      }
    }
  },
  mounted() {},
  methods: {
    /** 通过回调控制style */
    cellStyle({ row, column, rowIndex, columnIndex }) {
      if (
        column.property === 'num' ||
        column.property === 'customerName' ||
        column.property === 'businessName' ||
        column.property === 'contactsName'
      ) {
        return { color: '#3E84E9', cursor: 'pointer' }
      } else {
        return ''
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../styles/table.scss';
.money-bar {
  color: #99a9bf;
  line-height: 44px !important;
  position: absolute;
  left: 20px;
  top: 0;
}
</style>
