<template>
  <div>
    <c-r-m-list-head
      ref="listHead"
      :search.sync="search"
      :crm-type="crmType"
      title="回款管理"
      placeholder="请输入回款编号"
      main-title="新建回款"
      @on-handle="listHeadHandle"
      @on-search="crmSearch"/>
    <div
      v-empty="!crm.receivables.index"
      xs-empty-icon="nopermission"
      xs-empty-text="暂无权限"
      class="crm-container">
      <c-r-m-table-head
        ref="crmTableHead"
        :crm-type="crmType"
        @filter="handleFilter"
        @exportData="exportData"
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
  /** 客户管理 的 回款列表 */
  name: 'MoneyIndex',
  components: {
    CRMAllDetail
  },
  mixins: [table],
  data() {
    return {
      crmType: 'receivables'
    }
  },
  computed: {},
  mounted() {},
  methods: {
    /** 通过回调控制style */
    cellStyle({ row, column, rowIndex, columnIndex }) {
      if (
        column.property === 'number' ||
        column.property === 'customerName' ||
        column.property === 'contractNum'
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
</style>
