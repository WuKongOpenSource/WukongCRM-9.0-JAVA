<template>
  <div class="new-dialog">
    <el-dialog :title="dialogTitle"
               :visible.sync="dialogVisible"
               width="700px"
               :close-on-click-modal="false"
               :before-close="handleClose">
      <div class="label-input">
        <label class="label-title">选择员工</label>
        <xh-user-cell :radio="false"
                      :value="selectUsers"
                      @value-change="changeCheckout"></xh-user-cell>
      </div>
      <label class="label-title">员工角色配置</label>
      <div class="role-allocation">
        <div class="role-label-checkout"
             v-for="(item, index) in roleList"
             :key="index">
          <label>{{item.name}}</label>
          <el-checkbox-group v-model="selectRoles">
            <el-checkbox :label="subRole.id"
                         v-for="(subRole, i) in item.list"
                         :key="i">
              {{subRole.title}}
            </el-checkbox>
          </el-checkbox-group>
        </div>
      </div>
      <span slot="footer"
            class="dialog-footer">
        <el-button type="primary"
                   @click="employeesDetermine">确 定</el-button>
        <el-button @click="handleClose">取 消</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { usersEdit } from '@/api/systemManagement/RoleAuthorization'
import { XhUserCell } from '@/components/CreateCom'
export default {
  components: {
    XhUserCell
  },
  watch: {
    selectRoleList: function(value) {
      this.selectRoles = value
    },
    selectUserList: function(value) {
      this.selectUsers = value
    },
    dialogVisible: function(value) {
      if (value && this.role) {
        this.selectRoles = [this.role.id]
      }
    }
  },
  data() {
    return {
      selectUsers: [],
      selectRoles: []
    }
  },
  props: {
    dialogTitle: String,
    dialogVisible: Boolean,
    roleList: Array,
    // 选择的员工 和 角色
    selectRoleList: Array,
    selectUserList: Array
  },
  mounted() {},
  methods: {
    handleClose() {
      this.$emit('handleClose')
    },
    employeesDetermine() {
      if (this.selectUsers.length == 0) {
        this.$message.error('请选择员工')
      } else {
      }
      usersEdit({
        userIds: this.selectUsers.map(function(data) {
          return data.userId
        }).join(','),
        roleIds: this.selectRoles.join(',')
      })
        .then(res => {
          this.$message.success('操作成功')
          this.$emit('save')
        })
        .catch(err => {})
    },
    /** 员工选择操作 */
    changeCheckout(data) {
      this.selectUsers = data.value
    },
    // 删除选择员工
    selectDelect(value, index) {
      this.$refs.xhuser.cancelCheckItem(value)
      this.selectUsers.splice(index, 1)
    }
  }
}
</script>

<style scoped>
.new-dialog >>> .el-dialog__header {
  border-bottom: 1px solid #e6e6e6;
  padding-bottom: 15px;
}
.new-dialog >>> .el-dialog__body {
  padding-top: 0;
  overflow: auto;
  flex: 1;
}
.new-dialog >>> .el-dialog__close {
  font-size: 20px;
  color: #ccc;
}
.new-dialog >>> .el-dialog__wrapper {
  background: #f5f6f9;
  overflow: hidden;
  padding: 40px 0;
}
.new-dialog >>> .el-dialog {
  margin-top: 0 !important;
  margin-bottom: 0 !important;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.label-title {
  display: block;
  margin: 20px 0 10px 0;
  color: #666;
}
.role-allocation {
  border: 1px solid #e6e6e6;
  padding: 20px 10px;
  height: 355px;
  overflow-y: auto;
}
.role-label-checkout {
  margin-bottom: 10px;
}
.role-label-checkout > label {
  color: #333;
}
.role-label-checkout > div {
  color: #666;
  padding-top: 10px;
}
.role-label-checkout:last-child {
  margin-bottom: 0;
}
.label-input {
  position: relative;
}
.select-input > .el-input {
  margin: 10px 0;
}
.select-input >>> .el-tabs .is-active {
  background: #f0f4fc;
}
.select-input >>> .el-tabs .el-tabs__active-bar {
  display: none;
}
.select-input >>> .el-tabs__nav-wrap::after {
  height: 0;
}
.select-input >>> .el-tabs .el-tabs__content {
  height: 300px;
  overflow: auto;
}
.select-input >>> .el-tabs .el-tabs__content .el-checkbox {
  margin-left: 0;
  margin-right: 7px;
}
.select-input >>> .el-tabs .el-tabs__content .el-checkbox__label {
  flex: 1;
  display: flex;
}
.select-input
  >>> .el-tabs
  .el-tabs__content
  .el-checkbox__label
  span:first-child {
  flex: 1;
}
.select-input >>> .el-breadcrumb {
  margin-bottom: 15px;
}
.select-input >>> .checkout-box {
  display: flex;
  margin-bottom: 10px;
}
.select-input >>> .checkout-box > div {
  /* float: right; */
  flex: 1;
  /* margin-top: 9px; */
}
.select-input >>> .checkout-box > div .el-icon-arrow-right {
  float: right;
}
.select-input >>> .checkout-box span {
  cursor: pointer;
}
/* 选择员工 */
.search-img {
  width: 20px;
  vertical-align: middle;
  margin-right: 7px;
}
.search-list {
  padding: 5px 0;
}
.search-box {
  padding: 5px 0;
}
.select-box {
  border: 1px solid #e6e6e6;
  height: 36px;
  line-height: 36px;
  border-radius: 3px;
  cursor: pointer;
}
.select-box > .el-icon-plus {
  float: right;
  line-height: 36px;
  font-size: 16px;
  font-weight: 600;
  color: #aaa;
  padding-right: 10px;
}
.select-box > .select-box-span {
  background: #eff3fc;
  margin: 0 5px;
  height: 27px;
  line-height: 28px;
  font-size: 12px;
  color: #333;
  padding: 0 5px;
  display: inline-block;
}

.role-label-checkout /deep/ .el-checkbox + .el-checkbox {
  margin-left: 0px;
}

.role-label-checkout /deep/ .el-checkbox {
  padding-right: 30px;
  padding-bottom: 8px;
  width: 150px;
  overflow-x: hidden;
}
.role-label-checkout /deep/ .el-checkbox__label {
  color: #666;
}
</style>

