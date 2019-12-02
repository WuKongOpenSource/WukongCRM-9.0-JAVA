<template>
  <div class="check-container">
    <div class="condition-content">
      <el-form
        label-position="top"
        class="condition-items">
        <el-form-item
          v-for="(item, index) in conditionList"
          :key="item.field"
          :style="{'padding-left': getPaddingLeft(item, index), 'padding-right': getPaddingRight(item, index)}"
          class="condition-item">
          <div
            slot="label"
            class="form-label">
            {{ item.name }}
            <el-tooltip
              class="item"
              effect="dark"
              placement="top-start">
              <div
                slot="content"
                style="line-height: 20px;"
                v-html="item.tips"/>
              <i
                v-if="item.showTips"
                class="wukong wukong-help_tips"/>
            </el-tooltip>
          </div>
          <el-input
            v-model="item.value"
            type="text"/>
        </el-form-item>
      </el-form>
      <el-button
        type="primary"
        class="condition-button"
        @click="getList">查询</el-button>
    </div>
    <div class="table-content">
      <el-table
        v-loading="loading"
        id="crm-table"
        :data="list"
        :cell-style="cellStyle"
        height="400"
        stripe
        border
        highlight-current-row
        style="width: 100%"
        @row-click="handleRowClick">
        <el-table-column
          v-for="(item, index) in fieldList"
          :key="index"
          :prop="item.field"
          :label="item.name"
          show-overflow-tooltip/>
      </el-table>
      <div class="p-contianer">
        <el-pagination
          :current-page.sync="currentPage"
          :total="total"
          layout="prev, pager, next"
          @current-change="getList"/>
      </div>
    </div>
    <!-- 相关详情页面 -->
    <c-r-m-all-detail
      :visible.sync="showDview"
      :crm-type="rowType"
      :id="rowID"
      :listener-ids="['duplicate-check-container']"/>
  </div>
</template>
<script type="text/javascript">
import { crmCustomerQueryListAPI } from '@/api/customermanagement/customer'
import { crmContactsQueryListAPI } from '@/api/customermanagement/contacts'
import { crmLeadsQueryListAPI } from '@/api/customermanagement/clue'

import { XhInput } from '@/components/CreateCom'
import CRMAllDetail from '@/views/customermanagement/components/CRMAllDetail'

export default {
  name: 'CheckContent', // 验重内容

  components: {
    XhInput,
    CRMAllDetail
  },

  props: {
    type: {
      required: true,
      type: String
    }
  },

  data() {
    return {
      // 条件数组
      conditionList: this.getConditionList(),
      currentPage: 1,
      total: 1,
      loading: false,
      list: [],

      /** 控制详情展示 */
      rowID: '', // 行信息
      rowType: '', // 详情类型
      showDview: false
    }
  },

  computed: {
    // 表头信息
    fieldList() {
      if (this.type == 'customer') {
        return [
          { name: '客户名称', field: 'customerName' },
          { name: '负责人', field: 'ownerUserName' }
        ]
      } else if (this.type == 'contacts') {
        return [
          { name: '姓名', field: 'name' },
          { name: '客户名称', field: 'customerName' },
          { name: '负责人', field: 'ownerUserName' }
        ]
      } else if (this.type == 'leads') {
        return [
          { name: '线索名称', field: 'leadsName' },
          { name: '负责人', field: 'ownerUserName' }
        ]
      }
      return []
    }
  },

  watch: {},

  mounted() {},
  destroyed() {},

  methods: {
    /** 获取列表数据 */
    getList() {
      this.loading = true
      const params = {
        page: this.currentPage,
        limit: 10,
        types: 'crm_' + this.type
      }
      let pass = false
      for (let index = 0; index < this.conditionList.length; index++) {
        const element = this.conditionList[index]
        params[element.field] = element.value
        if (element.value) {
          pass = true
        }
      }

      if (pass) {
        const request = {
          customer: crmCustomerQueryListAPI,
          contacts: crmContactsQueryListAPI,
          leads: crmLeadsQueryListAPI
        }[this.type]

        request(params)
          .then(res => {
            this.list = res.data.list
            this.total = res.data.totalRow

            this.loading = false
          })
          .catch(() => {
            this.loading = false
          })
      } else {
        this.$message.error('查询条件不能为空')
      }
    },

    // 获取左边padding
    getPaddingLeft(item, index) {
      return index % 2 == 0 ? '0' : '30px'
    },

    // 获取左边padding
    getPaddingRight(item, index) {
      return index % 2 == 0 ? '30px' : '0'
    },

    /**
     * 获取条件数组信息
     */
    getConditionList() {
      const customerTips = `为确保客户信息安全性，客户名称字段增加了防作弊机制，以<br />
                下关键词为禁搜关键词：<br />
                1.省、市名；<br />
                2.中国、公司、有限公司、有限责任公司、股份有限公司；`
      const leadsTips = `为确保客户信息安全性，线索名称字段增加了防作弊机制，以<br />
                下关键词为禁搜关键词：<br />
                1.省、市名；<br />
                2.中国、公司、有限公司、有限责任公司、股份有限公司；`
      if (this.type == 'customer') {
        return [
          {
            name: '客户名称',
            field: 'customerName',
            value: '',
            showTips: true,
            tips: customerTips
          },
          { name: '手机', field: 'mobile', value: '' },
          { name: '电话', field: 'telephone', value: '' }
        ]
      } else if (this.type == 'contacts') {
        return [
          { name: '联系人', field: 'name', value: '' },
          {
            name: '客户名称',
            field: 'customerName',
            value: '',
            showTips: true,
            tips: customerTips
          },
          { name: '手机', field: 'mobile', value: '' },
          { name: '电话', field: 'telephone', value: '' }
        ]
      } else if (this.type == 'leads') {
        return [
          {
            name: '线索名称',
            field: 'leadsName',
            value: '',
            showTips: true,
            tips: leadsTips
          },
          { name: '手机', field: 'mobile', value: '' },
          { name: '电话', field: 'telephone', value: '' }
        ]
      }
      return []
    },

    /**
     * 查看详情
     */
    handleRowClick(row, column, event) {
      let type = this.type
      if (column.property == 'customerName' && this.type == 'contacts') {
        type = 'customer'
      }
      this.rowID = row[this.type + 'Id']
      this.rowType = type
      this.showDview = true
    },

    /**
     * 通过回调控制style
     */
    cellStyle({ row, column, rowIndex, columnIndex }) {
      if (
        column.property === 'name' ||
        column.property === 'customerName' ||
        column.property === 'leadsName'
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
.check-container {
  height: 100%;
  overflow-y: auto;
  padding: 0 30px;
}

// 提示标志
.wukong-help_tips {
  color: #999;
  font-size: 12px;
}

.wukong-help_tips:hover {
  color: $xr-color-primary;
}

//  条件
.condition-content {
  .condition-items {
    display: flex;
    flex-wrap: wrap;
  }

  .condition-item {
    flex: 0 0 50%;
    flex-shrink: 0;
    /deep/ .el-form-item__label {
      padding: 10px 0 0 0 !important;
      line-height: 30px;
      font-size: 13px;
    }
  }
  .condition-button {
    margin-top: 15px;
  }
}

.el-form-item {
  margin-bottom: 0px;
}

// 表内容
.table-content {
  margin-top: 20px;
}
</style>
