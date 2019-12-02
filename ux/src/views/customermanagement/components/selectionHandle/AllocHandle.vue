<template>
  <el-dialog
    v-loading="loading"
    :visible.sync="visible"
    :append-to-body="true"
    title="批量分配"
    width="400px"
    @close="handleCancel">
    <div class="handle-box">
      <flexbox
        class="handle-item"
        align="stretch">
        <div
          class="handle-item-name"
          style="margin-top: 8px;">请选择：</div>
        <xh-user-cell
          class="handle-item-content"
          placeholder="点击选择"
          @value-change="userChage"/>
      </flexbox>
    </div>
    <span
      slot="footer"
      class="dialog-footer">
      <el-button @click.native="handleCancel">取消</el-button>
      <el-button
        type="primary"
        @click.native="handleConfirm">保存</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { XhUserCell } from '@/components/CreateCom'
import { crmCustomerDistribute } from '@/api/customermanagement/customer'

export default {
  /** 客户管理 的 勾选后的 公海分配 操作*/
  name: 'AllocHandle',
  components: {
    XhUserCell
  },
  mixins: [],
  props: {
    dialogVisible: {
      type: Boolean,
      required: true,
      default: false
    },
    /** 没有值就是全部类型 有值就是当个类型 */
    crmType: {
      type: String,
      default: ''
    },
    /** 转移数据 */
    selectionList: {
      type: Array,
      default: () => {
        return []
      }
    }
  },
  data() {
    return {
      loading: true,
      visible: false,
      usersList: []
    }
  },
  computed: {},
  watch: {
    dialogVisible: {
      handler(val) {
        this.visible = val
      },
      deep: true,
      immediate: true
    }
  },
  mounted() {
    this.visible = this.dialogVisible
  },
  methods: {
    /**
     * 取消选择
     */
    handleCancel() {
      this.visible = false
      this.$emit('update:dialogVisible', false)
    },
    /** 负责人更改 */
    userChage(data) {
      this.usersList = data.value
    },
    handleConfirm() {
      // 移除操作不可移除客户负责人
      if (this.usersList.length === 0) {
        this.$message.error('请选择负责人')
      } else {
        var self = this
        var actionIds = this.selectionList.map(function(item, index, array) {
          return item[self.crmType + 'Id']
        })
        var params = {
          userId: this.usersList[0].userId
        }
        params.ids = actionIds.join(',')
        this.loading = true
        crmCustomerDistribute(params)
          .then(res => {
            this.$message({
              type: 'success',
              message: '操作成功'
            })
            this.loading = false
            this.$emit('handle', {
              type: 'alloc'
            })
            this.handleCancel()
          })
          .catch(() => {
            this.loading = false
          })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.handle-box {
  color: #333;
  font-size: 12px;
}
.handle-item {
  padding-bottom: 15px;
  .handle-item-name {
    flex-shrink: 0;
    width: 90px;
  }
  .handle-item-content {
    flex: 1;
  }
}
.handle-bar {
  position: relative;
  margin-top: 10px;
  height: 30px;
  button {
    float: right;
    margin-right: 10px;
  }
}
</style>
