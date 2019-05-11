<template>
  <el-dialog :title="title"
             v-loading="loading"
             :visible.sync="visible"
             @close="handleCancel"
             :append-to-body="true"
             width="400px">
    <div class="handle-box">
      <flexbox class="handle-item"
               align="stretch">
        <div class="handle-item-name"
             style="margin-top: 8px;">选择团队成员：</div>
        <xh-user-cell class="handle-item-content"
                      :radio="false"
                      placeholder="点击选择（多选）"
                      @value-change="userChage"></xh-user-cell>
        <div v-if="title!='添加团队成员'"
             class="tips">此操作不可移除数据负责人</div>
      </flexbox>
      <flexbox v-if="title=='添加团队成员'"
               class="handle-item">
        <div class="handle-item-name">权限：</div>
        <el-radio-group v-model="handleType">
          <el-radio :label="1">只读</el-radio>
          <el-radio :label="2">读写</el-radio>
        </el-radio-group>
      </flexbox>
      <flexbox v-if="title=='添加团队成员' && crmType === 'customer'"
               class="handle-item">
        <div class="handle-item-name">同时添加至：</div>
        <el-checkbox-group v-model="addsTypes">
          <!-- <el-checkbox label="1">联系人</el-checkbox> -->
          <el-checkbox label="2">商机</el-checkbox>
          <el-checkbox label="3">合同</el-checkbox>
        </el-checkbox-group>
      </flexbox>
    </div>
    <span slot="footer"
          class="dialog-footer">
      <el-button @click.native="handleCancel">取消</el-button>
      <el-button type="primary"
                 @click.native="handleConfirm">保存</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { XhUserCell } from '@/components/CreateCom'
import {
  crmCustomerSettingTeamSave,
  crmCustomerSettingTeamDelete
} from '@/api/customermanagement/customer'
import {
  crmContractSettingTeamSave,
  crmContractSettingTeamDelete
} from '@/api/customermanagement/contract'

import {
  crmBusinessSettingTeamSave,
  crmBusinessSettingTeamDelete
} from '@/api/customermanagement/business'

export default {
  /** 客户管理 的 勾选后的 团队成员 操作 移除操作不可移除客户负责人*/
  name: 'teams-handle',
  components: {
    XhUserCell
  },
  mixins: [],
  watch: {
    dialogVisible: {
      handler(val) {
        this.visible = val
        if (val) {
        }
      },
      deep: true,
      immediate: true
    }
  },
  props: {
    dialogVisible: {
      type: Boolean,
      required: true,
      default: false
    },
    title: {
      type: String,
      default: ''
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
      loading: false, // 加载动画
      visible: false,

      usersList: [], // 变更负责人
      handleType: 1, // 操作类型
      addsTypes: [] // 添加至
    }
  },
  computed: {},
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
        this.$message.error('请选择团队成员')
      } else {
        var self = this
        var actionIds = this.selectionList.map(function(item, index, array) {
          return item[self.crmType + 'Id']
        })
        var userIds = this.usersList.map(function(item, index, array) {
          return item.userId
        })
        var params = {
          ids: actionIds.join(','),
          memberIds: userIds.join(',')
        }
        if (this.crmType === 'customer' && this.title == '添加团队成员') {
          // 只有客户下面同时添加到
          params.changeType = this.addsTypes.join(',')
        }

        let request
        if (this.title == '添加团队成员') {
          // 1只读，2读写
          params.power = this.handleType
          request = {
            customer: crmCustomerSettingTeamSave,
            contract: crmContractSettingTeamSave,
            business: crmBusinessSettingTeamSave
          }[this.crmType]
        } else {
          request = {
            customer: crmCustomerSettingTeamDelete,
            contract: crmContractSettingTeamDelete,
            business: crmBusinessSettingTeamDelete
          }[this.crmType]
        }

        this.loading = true

        request(params)
          .then(res => {
            this.$message({
              type: 'success',
              message: '操作成功'
            })
            this.loading = false
            this.handleCancel()
            this.$emit('handle', {
              type: this.title == '添加团队成员' ? 'add_user' : 'delete_user'
            })
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
.tips {
  color: #777;
  font-size: 12px;
  position: absolute;
  bottom: -2px;
  right: 125px;
}
.handle-box {
  color: #333;
  font-size: 12px;
}
.handle-item {
  padding-bottom: 15px;
  position: relative;
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
