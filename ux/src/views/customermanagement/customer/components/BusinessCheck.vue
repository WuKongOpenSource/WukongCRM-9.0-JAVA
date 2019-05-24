<template>
  <div class="container"
       v-empty="!canShowIndex"
       xs-empty-icon="nopermission"
       xs-empty-text="暂无权限">
    <flexbox class="header">
      <div class="name">{{data.row.customerName}}</div>
      <div class="detail">商机（{{list.length}}）</div>
      <img @click="hidenView"
           class="close"
           src="@/assets/img/task_close.png" />
    </flexbox>
    <el-table v-loading="loading"
              :data="list"
              height="250"
              stripe
              :cell-style="cellStyle"
              :header-cell-style="headerCellStyle"
              @row-click="handleRowClick"
              style="margin-right:3px;"
              highlight-current-row>
      <el-table-column v-for="(item, index) in fieldList"
                       :key="index"
                       align="center"
                       header-align="center"
                       show-overflow-tooltip
                       :prop="item.prop"
                       :label="item.label">
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { crmCustomerQueryBusiness } from '@/api/customermanagement/customer'

export default {
  /** 客户管理 的 客户列表  相关商机列表*/
  name: 'business-check',
  components: {},
  computed: {
    ...mapGetters(['crm']),
    canShowIndex() {
      return this.crm.business && this.crm.business.index
    }
  },
  watch: {
    show: {
      handler(val) {
        if (
          this.canShowIndex &&
          val &&
          this.data.row &&
          this.data.row.businessCount > 0 &&
          this.list.length == 0
        ) {
          this.getDetail()
        }
      },
      deep: true,
      immediate: true
    }
  },
  props: {
    show: Boolean,
    data: {
      type: Object,
      default: () => {
        return {
          row: {
            name: ''
          }
        }
      }
    }
  },
  data() {
    return {
      loading: false,
      list: [],
      fieldList: []
    }
  },
  mounted() {
    this.fieldList.push({
      prop: 'businessName',
      width: '200',
      label: '商机名称'
    })
    this.fieldList.push({
      prop: 'money',
      width: '200',
      label: '商机金额'
    })
    this.fieldList.push({
      prop: 'customerName',
      width: '200',
      label: '客户名称'
    })
    this.fieldList.push({ prop: 'typeName', width: '200', label: '商机状态组' })
    this.fieldList.push({ prop: 'statusName', width: '200', label: '状态' })
  },
  methods: {
    getDetail() {
      this.loading = true
      crmCustomerQueryBusiness({
        pageType: 0,
        customerId: this.data.row.customerId
      })
        .then(res => {
          this.loading = false
          this.list = res.data
        })
        .catch(() => {
          this.loading = false
        })
    },
    hidenView() {
      document.querySelector('#app').click()
      this.$emit('close', this.$el, this.data)
    },
    //当某一行被点击时会触发该事件
    handleRowClick(row, column, event) {
      this.$emit('click', row)
    },
    /** 通过回调控制style */
    cellStyle({ row, column, rowIndex, columnIndex }) {
      return { fontSize: '12px', textAlign: 'center', cursor: 'pointer' }
    },
    headerCellStyle({ row, column, rowIndex, columnIndex }) {
      return { fontSize: '12px', textAlign: 'center' }
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  position: relative;
}

.header {
  height: 40px;
  padding: 0 10px;
  flex-shrink: 0;
  .name {
    font-size: 13px;
    padding: 0 10px;
    color: #333;
  }
  .detail {
    font-size: 12px;
    padding: 0 10px;
    color: #aaaaaa;
    border-left: 1px solid #aaaaaa;
  }
  .close {
    position: absolute;
    width: 40px;
    height: 40px;
    top: 0;
    right: 10px;
    padding: 10px;
  }
}
</style>
