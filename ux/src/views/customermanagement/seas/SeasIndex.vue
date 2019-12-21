<template>
  <div>
    <c-r-m-list-head
      ref="listHead"
      :search.sync="search"
      :is-seas="true"
      title="公海管理"
      placeholder="请输入客户名称/手机/电话号码"
      main-title="新建客户"
      crm-type="customer"
      @on-handle="listHeadHandle"
      @on-search="crmSearch"
      @on-export="exportInfos"/>
    <div
      v-empty="!crm.pool.index"
      xs-empty-icon="nopermission"
      xs-empty-text="暂无权限"
      class="crm-container">
      <c-r-m-table-head
        ref="crmTableHead"
        :is-seas="isSeas"
        crm-type="customer"
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
    <customer-detail
      v-if="showDview"
      :id="rowID"
      :is-seas="isSeas"
      class="d-view"
      @handle="handleHandle"
      @hide-view="showDview=false"/>
    <fields-set
      :crm-type="crmType"
      :is-seas="isSeas"
      :dialog-visible.sync="showFieldSet"
      @set-success="setSave"/>
  </div>
</template>

<script>
import CustomerDetail from '../customer/CustomerDetail'
import table from '../mixins/table'

export default {
  /** 客户管理 的 公海列表 */
  name: 'SeacIndex',
  components: {
    CustomerDetail
  },
  mixins: [table],
  data() {
    return {
      crmType: 'customer',
      isSeas: true // 是公海
    }
  },
  computed: {},
  mounted() {},
  methods: {
    /** 通过回调控制style */
    cellStyle({ row, column, rowIndex, columnIndex }) {
      if (
        column.property === 'customerName' ||
        column.property === 'businessCheck'
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
