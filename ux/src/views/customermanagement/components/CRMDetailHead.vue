<template>
  <div style="position: relative;">
    <flexbox class="t-section">
      <img class="t-img"
           :src="crmIcon" />
      <div class="t-name">{{name}}</div>
      <el-button v-if="showTransfer"
                 class="head-handle-button"
                 @click.native="handleTypeClick('transfer')"
                 type="primary">转移</el-button>
      <el-button v-if="showEdit"
                 class="head-handle-button"
                 @click.native="handleTypeClick('edit')"
                 type="primary">编辑</el-button>
      <el-dropdown trigger="click"
                   @command="handleTypeClick">
        <flexbox class="t-more">
          <div>更多</div>
          <i class="el-icon-arrow-down el-icon--right"
             style="color:#ccc;"></i>
        </flexbox>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item v-for="(item, index) in moreTypes"
                            :key="index"
                            v-if="whetherTypeShowByPermision(item.type)"
                            :command="item.type">{{item.name}}</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <img @click="hideView"
           class="t-close"
           src="@/assets/img/task_close.png" />
    </flexbox>
    <flexbox class="h-section"
             align="stretch">
      <flexbox-item class="h-item"
                    span="240"
                    v-for="(item, index) in headDetails"
                    :key="index">
        <div class="h-title">{{item.title}}</div>
        <div class="h-value">{{item.value}}</div>
      </flexbox-item>
    </flexbox>
    <slot></slot>
    <transfer-handle :crmType="crmType"
                     :selectionList="[detail]"
                     @handle="handleCallBack"
                     :dialogVisible.sync="transferDialogShow"></transfer-handle>
    <alloc-handle :crmType="crmType"
                  :selectionList="[detail]"
                  @handle="handleCallBack"
                  :dialogVisible.sync="allocDialogShow"></alloc-handle>
  </div>
</template>
<script type="text/javascript">
import { mapGetters } from 'vuex'
import {
  crmLeadsTransform,
  crmLeadsDelete
} from '@/api/customermanagement/clue'
import {
  crmCustomerLock,
  crmCustomerPutInPool,
  crmCustomerDelete,
  crmCustomerReceive
} from '@/api/customermanagement/customer'
import { crmContactsDelete } from '@/api/customermanagement/contacts'
import { crmBusinessDelete } from '@/api/customermanagement/business'
import { crmContractDelete } from '@/api/customermanagement/contract'
import { crmReceivablesDelete } from '@/api/customermanagement/money'
import { crmProductStatus } from '@/api/customermanagement/product'
import TransferHandle from './selectionHandle/TransferHandle' // 转移
import AllocHandle from './selectionHandle/AllocHandle' // 公海分配操作

export default {
  name: 'c-r-m-detail-head',
  components: {
    TransferHandle,
    AllocHandle
  },
  computed: {
    ...mapGetters(['crm', 'CRMConfig']),
    crmIcon() {
      if (this.crmType === 'customer') {
        return require('@/assets/img/customer_detail.png')
      } else if (this.crmType === 'leads') {
        return require('@/assets/img/clue_detail.png')
      } else if (this.crmType === 'business') {
        return require('@/assets/img/business_detail.png')
      } else if (this.crmType === 'contacts') {
        return require('@/assets/img/contacts_detail.png')
      } else if (this.crmType === 'contract') {
        return require('@/assets/img/contract_detail.png')
      } else if (this.crmType === 'receivables') {
        return require('@/assets/img/money_detail.png')
      } else if (this.crmType === 'product') {
        return require('@/assets/img/product_detail.png')
      }
      return ''
    },
    name() {
      if (this.crmType === 'receivables') {
        return this.detail.number
      } else if (this.crmType === 'customer') {
        return this.detail.customerName
      } else if (this.crmType === 'business') {
        return this.detail.businessName
      }
      return this.detail.name
    },
    // 展示转移
    showTransfer() {
      if (
        this.crmType === 'receivables' ||
        this.crmType === 'product' ||
        this.isSeas
      ) {
        return false
      }
      return this.crm[this.crmType].transfer
    },
    showEdit() {
      return this.isSeas ? false : this.crm[this.crmType].update
    }
  },
  data() {
    return {
      moreTypes: [], // 更多操作
      transferDialogShow: false, // 转移操作
      allocDialogShow: false // 公海分配操作提示框
    }
  },
  props: {
    /** 模块ID */
    id: [String, Number],
    /** 没有值就是全部类型 有值就是当个类型 */
    crmType: {
      type: String,
      default: ''
    },
    // 辅助 使用
    isSeas: {
      type: Boolean,
      default: false
    },
    /** 联系人人下 新建商机 需要联系人里的客户信息  合同下需要客户和商机信息 */
    detail: {
      type: Object,
      default: () => {
        return {}
      }
    },
    headDetails: {
      type: Array,
      default: () => {
        return []
      }
    }
  },
  mounted() {
    this.moreTypes = this.getSelectionHandleItemsInfo()
  },
  methods: {
    /** 更多操作 */
    handleTypeClick(type) {
      if (type == 'edit') {
        // 编辑
        this.$emit('handle', { type: 'edit' })
      } else if (type == 'transfer') {
        // 转移
        this.transferDialogShow = true
      } else if (
        type == 'transform' ||
        type == 'put_seas' ||
        type == 'delete' ||
        type == 'lock' ||
        type == 'unlock' ||
        type == 'start' ||
        type == 'disable' ||
        type == 'get'
      ) {
        var message = ''
        if (type == 'transform') {
          message = '确定将这些线索转换为客户吗?'
        } else if (type == 'put_seas') {
          message = '确定转移到公海吗?'
        } else if (type == 'delete') {
          message = '确定要删除这些数据吗?'
        } else if (type == 'lock') {
          message = '确定要锁定这些客户吗？锁定后将不会掉入公海。'
        } else if (type == 'unlock') {
          message = '确定要解锁这些客户吗？'
        } else if (type == 'start') {
          message = '确定要上架这些产品吗?'
        } else if (type == 'disable') {
          message = '确定要下架这些产品吗?'
        } else if (type == 'get') {
          message = '确定要领取该客户吗?'
        }
        this.$confirm(message, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            this.confirmHandle(type)
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '已取消操作'
            })
          })
      } else if (type == 'alloc') {
        // 公海分配操作
        this.allocDialogShow = true
      }
    },
    confirmHandle(type) {
      if (type === 'lock' || type === 'unlock') {
        crmCustomerLock({
          isLock: type === 'lock' ? '1' : '0', // 1锁0不锁
          ids: this.id
        })
          .then(res => {
            this.$message({
              type: 'success',
              message: '操作成功'
            })
            this.$emit('handle', { type: type })
          })
          .catch(() => {})
      } else if (type === 'put_seas') {
        crmCustomerPutInPool({
          ids: this.id
        })
          .then(res => {
            this.$message({
              type: 'success',
              message: '操作成功'
            })
            this.$emit('handle', { type: type })
          })
          .catch(() => {})
      } else if (type === 'transform') {
        crmLeadsTransform({
          leadsIds: this.id
        })
          .then(res => {
            this.$message({
              type: 'success',
              message: '转化成功'
            })
            this.$emit('handle', { type: type })
          })
          .catch(() => {})
      } else if (type === 'start' || type === 'disable') {
        crmProductStatus({
          ids: this.id,
          status: type === 'start' ? '1' : '0'
        })
          .then(res => {
            this.$message({
              type: 'success',
              message: '操作成功'
            })
            this.$emit('handle', { type: type })
          })
          .catch(() => {})
      } else if (type === 'delete') {
        let request = {
          leads: crmLeadsDelete,
          customer: crmCustomerDelete,
          contacts: crmContactsDelete,
          business: crmBusinessDelete,
          contract: crmContractDelete,
          receivables: crmReceivablesDelete
        }[this.crmType]
        request({
          [this.crmType + 'Ids']: this.id
        })
          .then(res => {
            this.$message({
              type: 'success',
              message: '删除成功'
            })
            this.$emit('handle', { type: type })
          })
          .catch(() => {})
      } else if (type === 'get') {
        // 领取
        crmCustomerReceive({
          ids: this.id
        })
          .then(res => {
            this.$message({
              type: 'success',
              message: '操作成功'
            })
            this.$emit('handle', { type: type })
          })
          .catch(() => {})
      }
    },
    hideView() {
      this.$emit('close')
    },
    // 子组件 回调的 结果
    handleCallBack(data) {
      this.$emit('handle', { type: data.type })
    },

    /** 更多操作 */
    /** 获取展示items */
    getSelectionHandleItemsInfo() {
      let handleInfos = {
        transfer: {
          name: '转移',
          type: 'transfer',
          icon: require('@/assets/img/selection_transfer.png')
        },
        transform: {
          name: '转化为客户',
          type: 'transform',
          icon: require('@/assets/img/selection_convert_customer.png')
        },
        delete: {
          name: '删除',
          type: 'delete',
          icon: require('@/assets/img/selection_delete.png')
        },
        put_seas: {
          name: '放入公海',
          type: 'put_seas',
          icon: require('@/assets/img/selection_putseas.png')
        },
        lock: {
          name: '锁定',
          type: 'lock',
          icon: require('@/assets/img/selection_lock.png')
        },
        unlock: {
          name: '解锁',
          type: 'unlock',
          icon: require('@/assets/img/selection_unlock.png')
        },
        alloc: {
          name: '分配',
          type: 'alloc',
          icon: require('@/assets/img/selection_alloc.png')
        },
        get: {
          name: '领取',
          type: 'get',
          icon: require('@/assets/img/selection_get.png')
        },
        start: {
          name: '上架',
          type: 'start',
          icon: require('@/assets/img/selection_start.png')
        },
        disable: {
          name: '下架',
          type: 'disable',
          icon: require('@/assets/img/selection_disable.png')
        }
      }
      if (this.crmType == 'leads') {
        return this.forSelectionHandleItems(handleInfos, [
          'transform',
          'delete'
        ])
      } else if (this.crmType == 'customer') {
        if (this.isSeas) {
          return this.forSelectionHandleItems(handleInfos, [
            'alloc',
            'get',
            'delete'
          ])
        } else {
          return this.forSelectionHandleItems(handleInfos, [
            'put_seas',
            'lock',
            'unlock',
            'delete'
          ])
        }
      } else if (this.crmType == 'contacts') {
        return this.forSelectionHandleItems(handleInfos, ['delete'])
      } else if (this.crmType == 'business') {
        return this.forSelectionHandleItems(handleInfos, ['delete'])
      } else if (this.crmType == 'contract') {
        return this.forSelectionHandleItems(handleInfos, ['transfer', 'delete'])
      } else if (this.crmType == 'receivables') {
        return this.forSelectionHandleItems(handleInfos, ['delete'])
      } else if (this.crmType == 'product') {
        return this.forSelectionHandleItems(handleInfos, ['start', 'disable'])
      }
    },
    forSelectionHandleItems(handleInfos, array) {
      var tempsHandles = []
      for (let index = 0; index < array.length; index++) {
        tempsHandles.push(handleInfos[array[index]])
      }
      return tempsHandles
    },
    // 判断是否展示
    whetherTypeShowByPermision: function(type) {
      if (type == 'transfer') {
        return this.crm[this.crmType].transfer
      } else if (type == 'transform') {
        return this.crm[this.crmType].transform
      } else if (type == 'export') {
        if (this.isSeas) {
          return this.crm.pool.excelexport
        }
        return this.crm[this.crmType].excelexport
      } else if (type == 'delete') {
        return this.crm[this.crmType].delete
      } else if (type == 'put_seas') {
        // 放入公海(客户)
        return this.crm[this.crmType].putinpool
      } else if (type == 'lock' || type == 'unlock') {
        // 锁定解锁(客户)
        return this.crm[this.crmType].lock && this.CRMConfig.customerConfig == 1
      } else if (type == 'add_user' || type == 'delete_user') {
        // 添加 移除团队成员
        return this.crm[this.crmType].teamsave
      } else if (type == 'alloc') {
        // 分配(公海)
        return this.crm.pool.distribute
      } else if (type == 'get') {
        // 领取(公海)
        return this.crm.pool.receive
      } else if (type == 'start' || type == 'disable') {
        // 上架 下架(产品)
        return this.crm[this.crmType].status
      }

      return true
    }
  }
}
</script>
<style lang="scss" scoped>
.t-section {
  position: relative;
  padding: 10px 17px;
  min-height: 60px;
  .t-img {
    display: block;
    width: 35px;
    height: 35px;
    margin-right: 10px;
  }
  .t-name {
    font-size: 14px;
    color: #333333;
    flex: 1;
  }
  .t-more {
    border: 1px solid #dcdfe6;
    font-size: 12px;
    color: #606266;
    padding: 0 10px 0 12px;
    border-radius: 2px;
    font-weight: 500;
    height: 25px;
    cursor: pointer;
  }
  .t-close {
    display: block;
    width: 40px;
    height: 40px;
    margin-left: 20px;
    padding: 10px;
    cursor: pointer;
  }
}

.h-section {
  position: relative;
  padding: 8px 17px 15px 17px;
  min-height: 58px;
  .h-item {
    .h-title {
      font-size: 12px;
      color: #777;
    }
    .h-value {
      min-height: 14px;
      margin-top: 8px;
      font-size: 13px;
      color: #333333;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }
}
.head-handle-button {
  padding: 5px 15px;
  margin-right: 10px;
}
</style>
