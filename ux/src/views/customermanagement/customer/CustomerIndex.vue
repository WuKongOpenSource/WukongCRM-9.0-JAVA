<template>
  <div>
    <c-r-m-list-head
      ref="listHead"
      :search.sync="search"
      :crm-type="crmType"
      title="客户管理"
      placeholder="请输入客户名称/手机/电话"
      main-title="新建客户"
      @on-handle="listHeadHandle"
      @on-search="crmSearch"
      @on-export="exportInfos"/>
    <div
      v-empty="!crm.customer.index"
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
          :resizable="false"
          prop="businessCheck"
          fixed
          label=""
          width="38">
          <template
            slot="header"
            slot-scope="slot">
            <i
              :style="{ 'color': '#2486E4'}"
              class="wukong wukong-business"
              style="cursor: not-allowed; opacity: 0.5;"/>
          </template>
          <template slot-scope="scope">
            <el-popover
              :disabled="scope.row.businessCount == 0"
              :offset="250"
              placement="right"
              popper-class="no-padding-popover"
              width="500"
              trigger="click">
              <business-check
                :data="scope"
                :show="scope.row.show"
                @click="relativeBusinessClick"
                @close="businessClose($event, scope)"/>
              <i
                slot="reference"
                :style="{'opacity' :scope.row.businessCount > 0 ? 1 : 0}"
                class="wukong wukong-business"
                style="color: '#2486E4'"
                @click="businessCheckClick($event, scope)"/>
            </el-popover>
          </template>
        </el-table-column>
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
        <!--        <el-table-column v-if="CRMConfig.customerConfig == 1"-->
        <!--                         prop="poolDay"-->
        <!--                         show-overflow-tooltip-->
        <!--                         :resizable='false'-->
        <!--                         label="距进入公海天数"-->
        <!--                         width="120">-->
        <!--          <template slot-scope="scope">-->
        <!--            <div v-if="scope.row.isLock == 0">{{scope.row.poolDay}}</div>-->
        <!--            <i v-else-->
        <!--               class="wukong wukong-lock customer-lock"></i>-->
        <!--          </template>-->
        <!--        </el-table-column>-->
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
import { mapGetters } from 'vuex'
import CRMAllDetail from '@/views/customermanagement/components/CRMAllDetail'
import BusinessCheck from './components/BusinessCheck' // 相关商机
import table from '../mixins/table'

export default {
  /** 客户管理 的 客户列表 */
  name: 'CustomerIndex',
  components: {
    CRMAllDetail,
    BusinessCheck
  },
  mixins: [table],
  data() {
    return {
      crmType: 'customer'
    }
  },
  computed: {
    ...mapGetters(['CRMConfig'])
  },
  mounted() {},
  methods: {
    relativeBusinessClick(data) {
      this.rowID = data.businessId
      this.rowType = 'business'
      this.showDview = true
    },
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
    },
    // 商机信息查看
    businessCheckClick(e, scope) {
      if (scope.row.businessCount == 0) {
        return
      }
      this.$set(scope.row, 'show', !scope.row.show)
      const popoverEl = e.target.parentNode
      popoverEl.__vue__.showPopper = !scope.row.show
    },
    businessClose(e, scope) {
      const popoverEl = e.parentNode
      popoverEl.__vue__.showPopper = false
      this.$set(scope.row, 'show', false)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../styles/table.scss';
.customer-lock {
  color: #f15e64;
}

.el-table /deep/ tbody tr td:nth-child(2) {
  border-right-width: 0;
}

.el-table /deep/ tbody tr td:nth-child(3) {
  border-right: 1px solid #e6e6e6;
}
</style>
