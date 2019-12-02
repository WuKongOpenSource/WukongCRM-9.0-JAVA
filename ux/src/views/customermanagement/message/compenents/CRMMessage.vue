<template>
  <div class="ec-container">
    <div class="title">{{ infoTitle }}
      <el-tooltip
        v-if="infoTips"
        :content="infoTips"
        effect="dark"
        placement="top">
        <i class="wukong wukong-help_tips"/>>
      </el-tooltip>
    </div>
    <div class="option-bar">
      <div v-if="selectionList.length == 0">
        <el-select
          v-if="showOptions"
          v-model="optionsType"
          placeholder="请选择"
          @change="refreshList">
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.name"
            :value="item.value"/>
        </el-select>
        <el-select
          v-if="showSubType"
          v-model="isSubType"
          :style="{'margin-left': showOptions ? '10px' : 0}"
          style="width: 120px;"
          placeholder="请选择"
          @change="refreshList">
          <el-option
            v-for="item in [{name: '我的', value: 1}, {name: '我下属的', value: 2}]"
            :key="item.value"
            :label="item.name"
            :value="item.value"/>
        </el-select>
        <div
          v-if="showFilterView"
          class="filtrate-button"
          @click="getFilterFieldInfo">
          <img
            class="filtrate-button-img"
            src="@/assets/img/c_filtrate.png" >
          <span class="filtrate-button-title">高级筛选</span>
        </div>
        <filter-form
          :field-list="filterFieldList"
          :dialog-visible.sync="showFilter"
          :obj="filterObj"
          :crm-type="crmType"
          :is-seas="true"
          @filter="handleFilter"/>
      </div>
      <flexbox
        v-else
        class="selection-bar">
        <div class="selected—title">已选中<span class="selected—count">{{ selectionList.length }}</span>项</div>
        <flexbox class="selection-items-box">
          <flexbox
            v-for="(item, index) in selectionButtonList"
            :key="index"
            class="selection-item"
            @click.native="selectionBarClick(item.type)">
            <img
              :src="item.icon"
              class="selection-item-icon" >
            <div class="selection-item-name">{{ item.name }}</div>
          </flexbox>
        </flexbox>
      </flexbox>
    </div>
    <filter-content
      v-if="filterObj.form && filterObj.form.length > 0"
      :obj="filterObj"
      @delete="handleDeleteField"/>
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
      @selection-change="handleSelectionChange">
      <el-table-column
        v-if="showSelection"
        show-overflow-tooltip
        type="selection"
        align="center"
        width="55"/>
      <el-table-column
        v-for="(item, index) in fieldList"
        :key="index"
        :prop="item.prop"
        :label="item.label"
        :width="item.width"
        :formatter="fieldFormatter"
        show-overflow-tooltip/>
      <el-table-column :resizable="false"/>
      <el-table-column
        v-if="showCheckStatus"
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

    <!-- 相关详情页面 -->
    <c-r-m-all-detail
      :visible.sync="showDview"
      :crm-type="rowType"
      :id="rowID"/>
  </div>
</template>

<script>
import crmTypeModel from '@/views/customermanagement/model/crmTypeModel'
import { filterIndexfields } from '@/api/customermanagement/common'
import { crmLeadsSetFollowAPI } from '@/api/customermanagement/clue'
import { crmCustomerSetFollowAPI } from '@/api/customermanagement/customer'
import message_table from '../mixins/message_table'
import filterForm from '@/views/customermanagement/components/filterForm'
import filterContent from '@/views/customermanagement/components/filterForm/filterContent'
import CRMAllDetail from '@/views/customermanagement/components/CRMAllDetail'

export default {
  /** 客户管理 的待审核系统 */
  name: 'CRMMessage',

  components: {
    filterForm,
    filterContent,
    CRMAllDetail
  },

  mixins: [message_table],

  props: {
    // crm类型
    crmType: {
      type: String,
      default: ''
    },
    // crm某个类型下的类型数据
    infoType: {
      type: String,
      default: ''
    },

    infoTitle: {
      type: String,
      default: ''
    },

    infoTips: {
      type: String,
      default: ''
    },

    // 展示的时候 才发请求
    show: Boolean
  },

  data() {
    return {
      optionsType: 0,
      isSubType: 1, // 是否是下属
      /** 高级筛选 */
      showFilter: false, // 控制筛选框
      filterFieldList: [],
      filterObj: { form: [] }, // 筛选确定数据
      /** 勾选数据操作 */
      selectionList: [], // 勾选的数据
      selectionButtonList: [
        {
          name: '已跟进',
          type: 'follow',
          icon: require('@/assets/img/selection_alloc.png')
        }
      ], // 操作按钮列表
      /** 控制详情展示 */
      rowID: '', // 行信息
      rowType: '', // 详情类型
      showDview: false
    }
  },

  computed: {
    // 展示勾选框
    showSelection() {
      if (this.infoType == 'followLeads' || this.infoType == 'followCustomer') {
        return true
      }

      return false
    },

    // 展示筛选
    showFilterView() {
      if (this.crmType == 'receivables_plan') {
        return false
      }
      return true
    },

    // 展示审核状态
    showCheckStatus() {
      if (this.crmType == 'contract' || this.crmType == 'receivables') {
        return true
      }
      return false
    },

    // 展示我的/下属筛选
    showSubType() {
      if (
        this.infoType == 'todayCustomer' ||
        this.infoType == 'putInPoolRemind'
      ) {
        return true
      }
      return false
    },

    // 下拉数据
    options() {
      if (this.infoType == 'todayCustomer') {
        return [
          { name: '今日需联系', value: 1 },
          { name: '已逾期', value: 2 },
          { name: '已联系', value: 3 }
        ]
      } else if (
        this.infoType == 'followLeads' ||
        this.infoType == 'followCustomer'
      ) {
        return [{ name: '待跟进', value: 1 }, { name: '已跟进', value: 2 }]
      } else if (
        this.infoType == 'checkContract' ||
        this.infoType == 'checkReceivables'
      ) {
        return [{ name: '待审核', value: 1 }, { name: '已审核', value: 2 }]
      } else if (this.infoType == 'remindReceivablesPlan') {
        return [
          { name: '待回款', value: 1 },
          { name: '已回款', value: 2 },
          { name: '已逾期', value: 3 }
        ]
      } else if (this.infoType == 'endContract') {
        return [{ name: '即将到期', value: 1 }, { name: '已到期', value: 2 }]
      }

      return []
    }
  },

  watch: {
    show() {
      this.initTableHead()
    }
  },

  mounted() {
    if (this.showOptions && this.options.length > 0) {
      this.optionsType = this.options[0].value
    }

    this.initTableHead()
  },

  methods: {
    /**
     * 初始化表头数据
     */
    initTableHead() {
      if (this.show && this.fieldList.length == 0) {
        this.getFieldList()
      }
    },

    /**
     * 刷新列表
     */
    refreshList() {
      this.currentPage = 1
      if (this.fieldList.length > 0) {
        this.getList()
      } else {
        this.getFieldList()
      }
    },

    /**
     * 勾选数据
     */
    handleSelectionChange(val) {
      this.selectionList = val // 勾选的行
    },

    /**
     * 勾选后的操作
     */
    selectionBarClick(type) {
      if (type == 'follow') {
        this.$confirm('您确定此操作吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            const request = {
              followLeads: crmLeadsSetFollowAPI,
              followCustomer: crmCustomerSetFollowAPI
            }[this.infoType]
            request({
              ids: this.selectionList
                .map(item => {
                  return item[this.crmType + 'Id']
                })
                .join(',')
            })
              .then(res => {
                this.$message.success('操作成功')
                this.refreshList()

                this.$emit('on-handle', {
                  type: 'follow',
                  value: this.selectionList.length,
                  infoType: this.infoType
                })
              })
              .catch(() => {})
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '已取消操作'
            })
          })
      }
    },

    /**
     * 获取高级筛选字段数据后展示
     */
    getFilterFieldInfo() {
      filterIndexfields({
        label: crmTypeModel[this.crmType]
      })
        .then(res => {
          this.filterFieldList = res.data
          this.showFilter = true
        })
        .catch(() => {})
    },

    /**
     * 选择筛选字段
     */
    handleFilter(form) {
      this.filterObj = form
      this.showFilter = false
      this.updateTableHeight()
      this.refreshList()
    },

    /**
     * 删除筛选字段
     */
    handleDeleteField(data) {
      this.filterObj = data.obj
      this.updateTableHeight()
      this.refreshList()
    },

    /**
     * 表单元可点击样式
     */
    cellStyle({ row, column, rowIndex, columnIndex }) {
      if (
        column.property === 'name' ||
        column.property === 'number' ||
        column.property === 'leadsName' ||
        column.property === 'customerName' ||
        column.property === 'businessName' ||
        column.property === 'contactsName' ||
        column.property === 'contractName' ||
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

<style rel="stylesheet/scss" lang="scss" scoped>
@import '../../styles/table.scss';
/** 场景和筛选 */
.filtrate-button {
  cursor: pointer;
  margin-left: 10px;
  display: inline-block;
  &-img {
    vertical-align: middle;
    margin: 0 5px;
    width: 12px;
  }
}
.filtrate-button:hover {
  color: $xr-color-primary;
}

.ec-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.title {
  padding: 0 20px;
  font-size: 16px;
  line-height: 60px;
  height: 60px;

  i {
    cursor: pointer;
    margin-left: 5px;
    color: #dcdfe6;
  }

  i:hover {
    color: $xr-color-primary;
  }
}
.option-bar {
  padding: 5px 20px;
}

/** 勾选操作 */
.selection-bar {
  font-size: 12px;
  height: 34px;
  color: #777;

  .selected—title {
    flex-shrink: 0;
    padding-right: 20px;
    border-right: 1px solid $--table-border-color;
    .selected—count {
      color: $xr-color-primary;
    }
  }
}

.selection-items-box {
  .selection-item {
    width: auto;
    padding: 15px;
    .selection-item-icon {
      display: block;
      margin-right: 5px;
      width: 15px;
      height: 15px;
    }
    .selection-item-name {
      cursor: pointer;
      color: #777;
    }
    .selection-item-name:hover {
      color: $xr-color-primary;
    }
  }
}
</style>
