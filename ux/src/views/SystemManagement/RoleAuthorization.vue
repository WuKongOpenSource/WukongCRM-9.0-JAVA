<template>
  <div class="role-authorization">
    <p class="title"> 角色权限控制 </p>
    <div class="role-box">
      <!-- 左边导航 -->
      <div class="nav"
           v-loading="navLoading">
        <div class="nav-new-btn">
          <el-button size="medium"
                     @click="newRoleBtn"> 新建角色 </el-button>
        </div>
        <!-- 角色列表 -->
        <div class="role-nav-box">
          <div v-for="(item, index) in roleList"
               :key="index"
               class="role-list">
            <div class="item-label">
              {{item.name}}
            </div>
            <div class="item-list"
                 :class="{'item-list-hover' : value.id == roleActive.id}"
                 v-for="(value, i) in item.list"
                 v-if="item.list"
                 :key="i"
                 @click="roleListClick(value)">
              {{value.title}}
              <div class="icon-close"
                   v-if="item.pid != 1">
                <el-dropdown trigger="click"
                             @command="roleHandleClick">
                  <i class="el-icon-arrow-down"
                     @click="roleDropdownClick(value)"></i>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="copy">复制</el-dropdown-item>
                    <el-dropdown-item command="edit">编辑</el-dropdown-item>
                    <el-dropdown-item command="delete">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>
      </div>
      <el-dialog :title="roleTitle"
                 :visible.sync="newRoleVisible"
                 width="30%"
                 :before-close="newRoleClose">
        <label class="label-title">角色名称</label>
        <el-input v-model="role.title"
                  class="input-role"></el-input>
        <label class="label-title">角色类型</label>
        <el-select v-model="role.pid"
                   class="input-role"
                   placeholder="请选择">
          <el-option v-for="item in roleList"
                     v-if="item.pid != 1"
                     :key="item.pid"
                     :label="item.name"
                     :value="item.pid">
          </el-option>
        </el-select>
        <span slot="footer"
              class="dialog-footer">
          <el-button type="primary"
                     @click="newRoleSubmit">确 定</el-button>
          <el-button @click="newRoleClose">取 消</el-button>
        </span>
      </el-dialog>
      <!-- 右边内容 -->
      <div class="content-box">
        <el-menu :default-active="activeIndex"
                 class="el-menu-demo"
                 mode="horizontal"
                 @select="handleSelect">
          <el-menu-item index="1">角色员工</el-menu-item>
          <el-menu-item index="2"
                        v-if="roleActive && (roleActive.pid != 1 || roleActive.pid == 0)">角色权限</el-menu-item>
        </el-menu>
        <div class="content-table"
             v-loading="menuLoading"
             v-show="activeIndex == '1'">
          <flexbox class="content-table-header">
            <div class="content-table-header-reminder">
              <reminder v-if="this.roleActive && this.roleActive.pid == 1"
                        :content="managerGroupReminderContent(this.roleActive)">
              </reminder>
            </div>
            <el-button size="medium"
                       type="primary"
                       @click="addEmployees"> 关联员工 </el-button>
          </flexbox>
          <el-table :data="tableData"
                    :height="tableHeight">
            <el-table-column :prop="item.field"
                             show-overflow-tooltip
                             :label="item.label"
                             v-for="(item, index) in tableList"
                             :key="index">
              <template slot="header"
                        slot-scope="scope">
                <div class="table-head-name">{{scope.column.label}}</div>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template slot-scope="scope">
                <!-- <span class="el-icon-edit content-table-span"
                      @click="editBtn(scope.row)"></span> -->
                <span class="el-icon-delete content-table-span"
                      @click="delectBtn(scope.row)"></span>
              </template>
            </el-table-column>
          </el-table>
          <div class="p-contianer">
            <el-pagination class="p-bar"
                           @size-change="handleSizeChange"
                           @current-change="handleCurrentChange"
                           :current-page="currentPage"
                           :page-sizes="pageSizes"
                           :page-size.sync="pageSize"
                           layout="total, sizes, prev, pager, next, jumper"
                           :total="total">
            </el-pagination>
          </div>

        </div>
        <!-- 权限管理 -->
        <div class="jurisdiction-box"
             v-loading="jurisdictionLoading"
             v-show="activeIndex == '2'">
          <div style="position: relative;">
            <el-menu :default-active="jurisdictionIndex"
                     class="el-menu-demo"
                     mode="horizontal"
                     @select="jurisdictionSelect">
              <el-menu-item v-for="(item, index) in muneList"
                            :key="item.realm"
                            :index="item.realm">{{item.label}}</el-menu-item>
            </el-menu>
            <el-button size="medium"
                       type="primary"
                       class="jurisdiction-edit"
                       @click="jurisdictionSubmit"> 保 存 </el-button>
          </div>
          <div class="jurisdiction-content">
            <div class="content-left">
              <p class="jurisdiction-title">功能权限管理</p>
              <div class="jurisdiction-content-checkbox">
                <el-tree :data="showTreeData"
                         show-checkbox
                         node-key="menuId"
                         style="height: 0;"
                         ref="tree"
                         :indent="0"
                         empty-text=""
                         default-expand-all
                         :expand-on-click-node="false"
                         :props="defaultProps">
                </el-tree>
              </div>
            </div>
            <div class="content-right">
              <p class="jurisdiction-title">数据权限</p>
              <div class="data-radio">
                <el-radio-group v-model="radioModel">
                  <el-radio :label="1">本人</el-radio>
                  <el-radio :label="2">本人及下属</el-radio>
                  <el-radio :label="3">本部门</el-radio>
                  <el-radio :label="4">本部门及下属部门</el-radio>
                  <el-radio :label="5">全部</el-radio>
                </el-radio-group>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 添加员工 -->
    <new-dialog :dialogVisible="newEmployeeVisible"
                :roleList="roleList"
                :selectRoleList="newDialogSelectRoles"
                :selectUserList="newDialogSelectUsers"
                :dialogTitle="dialogTitle"
                @handleClose="newEmployeeVisible = false"
                @save="employeesSave">
    </new-dialog>
  </div>
</template>

<script>
import NewDialog from './components/newDialog'
import { usersList } from '@/api/common'
import Reminder from '@/components/reminder'
import {
  roleListFun,
  rulesList,
  roleAdd,
  roleDelete,
  roleCopy,
  updateRoleMenu,
  usersDelete,
  roleUpdate
} from '@/api/systemManagement/RoleAuthorization'

export default {
  components: {
    NewDialog,
    Reminder
  },
  data() {
    return {
      activeIndex: '1', // 角色员工  角色权限 切换 默认左边
      tableData: [], // 与角色关联的员工
      tableHeight: document.documentElement.clientHeight - 319,
      currentPage: 1,
      pageSize: 15,
      pageSizes: [15, 30, 45, 60],
      total: 0,
      tableList: [
        { label: '姓名', field: 'realname' },
        { label: '部门', field: 'deptName' },
        { label: '职位', field: 'post' },
        { label: '角色', field: 'roleName' }
      ], //员工列表 头部 数据
      // 新建角色
      newRoleVisible: false,
      role: {}, // 操作角色的框 关联的信息
      roleList: [], // 角色列表 list属性 是信息
      // 权限管理
      jurisdictionIndex: 'crm', // 默认模块 工作台
      muneList: [],
      treeData: [], // 角色权限数据
      showTreeData: [],
      defaultProps: {
        children: 'childMenu',
        label: 'menuName'
      },
      radioModel: 2,
      // 编辑或添加员工
      dialogTitle: '',
      newEmployeeVisible: false,
      // 选择员工
      newDialogSelectUsers: [],
      /**选择的角色 */
      newDialogSelectRoles: [],
      // 选中的角色
      roleActive: null,
      roleRulesEdit: [], // 编辑时用到的信息
      dropdownHandleRole: null, // 下拉操作编辑角色
      // 新建编辑角色title
      roleTitle: '',
      //   加载
      navLoading: false,
      // 权限加载中
      jurisdictionLoading: false,
      // 列表加载中
      menuLoading: false
    }
  },
  computed: {},
  mounted() {
    /** 控制table的高度 */
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 319
    }
    /** 获取权限信息 */
    this.getRulesList()
  },
  methods: {
    // 获取权限规则信息
    getRulesList() {
      this.navLoading = true
      rulesList({ type: 'tree' })
        .then(res => {
          this.navLoading = false
          var arr = []
          var map = {}
          for (var i = 0; i < res.data.length; i++) {
            arr.push({
              label: res.data[i].menuName,
              index: i,
              realm: res.data[i].realm
            })
            map[res.data[i].realm] = res.data[i]
          }
          this.treeData = map
          this.muneList = arr
          this.showTreeData = [this.treeData[this.jurisdictionIndex]]
          this.getRoleList()
        })
        .catch(() => {
          this.navLoading = false
        })
    },
    // 获取角色列表
    getRoleList() {
      this.navLoading = true
      roleListFun()
        .then(res => {
          this.roleList = res.data
          /** 判断数据是否存在 */
          let hasActive = false
          if (this.roleActive) {
            for (let index = 0; index < this.roleList.length; index++) {
              const element = this.roleList[index]
              for (
                let subIndex = 0;
                subIndex < element.list.length;
                subIndex++
              ) {
                const item = element.list[subIndex]
                if (item.id === this.roleActive.id) {
                  this.roleActive = item
                  // 点击角色 复制权限 用于编辑操作
                  this.getRoleRulesInfo(item)
                  hasActive = true
                  break
                }
              }
            }
          }
          if (!hasActive) {
            for (let item of this.roleList) {
              if (item.list.length != 0) {
                this.roleActive = item.list[0]
                // 点击角色 复制权限 用于编辑操作
                this.getRoleRulesInfo(this.roleActive)
                break
              }
            }
          }

          this.getUserListWithRole(this.roleActive)
          this.getUserRulesWithRole(this.roleActive)
          this.navLoading = false
        })
        .catch(err => {
          this.navLoading = false
        })
    },
    // 右侧角色和权限切换
    handleSelect(key, keyPath) {
      this.activeIndex = key
    },
    // 添加员工
    addEmployees() {
      this.newDialogSelectUsers = []
      this.newDialogSelectRoles = [this.roleActive.id]
      this.dialogTitle = '添加员工'
      this.newEmployeeVisible = true
    },
    // 添加员工确定
    employeesSave(val) {
      this.newEmployeeVisible = false
      this.getUserListWithRole(this.roleActive)
      this.getUserRulesWithRole(this.roleActive)
    },
    // 删除
    delectBtn(val) {
      this.$confirm('此操作将永久删除是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.menuLoading = true
          usersDelete({ userId: val.userId, roleId: this.roleActive.id }).then(
            res => {
              this.menuLoading = true
              this.getUserListWithRole(this.roleActive)
              this.$message.success('删除成功')
            }
          )
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    },
    editBtn(val) {
      this.newDialogSelectUsers = [val]
      this.newDialogSelectRoles = val.roleId.split(',').map(function(data) {
        return +data
      })
      this.dialogTitle = '编辑员工'
      this.newEmployeeVisible = true
    },
    // 不同模块的权限切换
    jurisdictionSelect(key) {
      this.roleRulesEdit[
        this.jurisdictionIndex
      ] = this.$refs.tree.getCheckedKeys()
      this.roleRulesEdit[
        this.jurisdictionIndex + '_upload'
      ] = this.$refs.tree
        .getCheckedKeys()
        .concat(this.$refs.tree.getHalfCheckedKeys())

      this.jurisdictionIndex = key
      this.showTreeData = [this.treeData[this.jurisdictionIndex]]

      this.$nextTick(() => {
        if (this.$refs.tree) {
          this.$refs.tree.setCheckedKeys(
            this.roleRulesEdit[this.jurisdictionIndex]
          )
        }
      })
    },
    // 管理角色说明
    managerGroupReminderContent(item) {
      if (item.label == 1) {
        return '项目管理员拥有“项目管理”模块所有权限，能看到并维护所有项目信息'
      } else if (item.id == 1) {
        return '超级管理员不可被任何管理员删除，默认系统所有权限，也可添加其他超级管理员'
      } else if (item.id == 2) {
        return '系统设置管理员拥有整个系统的“系统设置”权限'
      } else if (item.id == 3) {
        return '员工与角色权限管理的管理权限，可管理公司的组织结构和员工账号的增加、停用，可创建角色并为员工分配角色授权'
      } else if (item.id == 4) {
        return '审批管理员可配置、管理所有审批流程'
      } else if (item.id == 5) {
        return '办公管理员可以对“办公”的所有设置进行管理'
      } else if (item.id == 6) {
        return '客户管理管理员可以对“客户管理”的所有设置进行管理'
      } else if (item.id == 7) {
        return '公告管理员有新建、删除、结束公告的操作'
      }
      return ''
    },
    // 新建角色
    newRoleClose() {
      this.newRoleVisible = false
    },
    newRoleBtn() {
      this.roleTitle = '新建角色'
      this.newRoleVisible = true
      this.role = {}
    },
    roleDropdownClick(value) {
      this.dropdownHandleRole = value
    },
    roleHandleClick(command) {
      if (command == 'edit') {
        this.roleEditBtn(this.dropdownHandleRole)
      } else if (command == 'copy') {
        this.ticketsBtn(this.dropdownHandleRole)
      } else if (command == 'delete') {
        this.roleDelect(this.dropdownHandleRole)
      }
    },
    // 角色编辑
    roleEditBtn(val) {
      this.roleTitle = '编辑角色'
      this.role = {
        title: val.title,
        pid: val.pid,
        id: val.id
      }
      this.newRoleVisible = true
    },
    // 复制
    ticketsBtn(val) {
      this.$confirm('确定此操作?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          roleCopy({ roleId: val.id }).then(res => {
            this.$message.success('复制成功')
            this.getRoleList()
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    },
    // 删除
    roleDelect(val) {
      this.$confirm('此操作将永久删除是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          roleDelete({ roleId: val.id })
            .then(res => {
              this.getRoleList()
              this.$message.success('删除成功')
              if (
                this.roleList.length > 0 &&
                this.roleList[0].list.length > 0
              ) {
                this.roleActive = this.roleList[0].list[0]
                // 点击角色 复制权限 用于编辑操作
                this.getRoleRulesInfo(this.roleActive)
              }
            })
            .catch(() => {})
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    },
    newRoleSubmit() {
      if (!this.role.title) {
        this.$message.error('角色名称不能为空')
      } else if (!this.role.pid && this.role.pid != 0) {
        this.$message.error('角色类型不能为空')
      } else {
        if (this.roleTitle == '新建角色') {
          roleAdd({
            roleName: this.role.title,
            roleType: this.role.pid
          }).then(res => {
            this.getRoleList()
            this.$message.success('添加成功')
            this.newRoleClose()
          })
        } else {
          roleUpdate({
            roleName: this.role.title,
            roleType: this.role.pid,
            roleId: this.role.id
          }).then(res => {
            this.getRoleList()
            this.$message.success('编辑成功')
            this.newRoleClose()
          })
        }
      }
    },
    // 角色列表点击事件
    roleListClick(val) {
      if (val.pid == 1) {
        this.activeIndex = '1' // 切换会角色员工
      }
      this.roleActive = val
      // 点击角色 复制权限 用于编辑操作
      this.getRoleRulesInfo(val)
      this.getUserListWithRole(this.roleActive)
      this.getUserRulesWithRole(this.roleActive)
    },
    getRoleRulesInfo(role) {
      this.roleRulesEdit['crm'] = this.getUserModuleRules(
        role.rules['crm'],
        'crm'
      )
      this.roleRulesEdit['crm_upload'] = role.rules['crm']
        ? role.rules['crm']
        : []
      this.roleRulesEdit['bi'] = this.getUserModuleRules(role.rules['bi'], 'bi')
      this.roleRulesEdit['bi_upload'] = role.rules['bi'] ? role.rules['bi'] : []
    },
    // 获取角色下员工列表
    getUserListWithRole(role, noReset) {
      if (!noReset) {
        this.currentPage = 1
      }
      this.menuLoading = true
      usersList({
        page: this.currentPage,
        limit: this.pageSize,
        roleId: role.id
      })
        .then(res => {
          this.tableData = res.data.list
          this.total = res.data.totalRow
          this.menuLoading = false
        })
        .catch(err => {
          this.menuLoading = false
        })
    },
    /**
     * 更改每页展示数量
     */
    handleSizeChange(val) {
      this.pageSize = val
      this.getUserListWithRole(this.roleActive, true)
    },
    /**
     * 更改当前页数
     */
    handleCurrentChange(val) {
      this.currentPage = val
      this.getUserListWithRole(this.roleActive, true)
    },
    // 员工权限标记
    getUserRulesWithRole(role) {
      // 默认勾选第一个
      this.$nextTick(() => {
        if (this.$refs.tree) {
          this.$refs.tree.setCheckedKeys(
            this.roleRulesEdit[this.jurisdictionIndex]
          )
        }
      })
      // 默认勾选第一个数据权限
      if (role.type) {
        this.radioModel = role.type
      } else {
        this.radioModel = ''
      }
    },
    // 获得check的实际数据
    getUserModuleRules(array, type) {
      if (!array) {
        array = []
      }
      var firstTree = this.treeData[type]
      var hasRemove = false
      var copyArray = this.copyItem(array)
      for (
        let firstIndex = 0;
        firstIndex < firstTree.childMenu.length;
        firstIndex++
      ) {
        const firstItem = firstTree.childMenu[firstIndex]
        for (let index = 0; index < array.length; index++) {
          const element = array[index]
          var temps = []
          for (
            let secondIndex = 0;
            secondIndex < firstItem.childMenu.length;
            secondIndex++
          ) {
            const secondItem = firstItem.childMenu[secondIndex]
            if (secondItem.id == element) {
              temps.push(secondItem)
            }
          }
          if (temps.length != firstItem.childMenu.length) {
            hasRemove = true
            this.removeItem(copyArray, firstItem.id)
          }
        }
      }

      if (hasRemove) {
        this.removeItem(copyArray, firstTree.id)
      }

      var checkedKey = []
      for (let index = 0; index < copyArray.length; index++) {
        const element = copyArray[index]
        if (element) {
          checkedKey.push(parseInt(element))
        }
      }
      return checkedKey
    },
    copyItem(array) {
      var temps = []
      for (let index = 0; index < array.length; index++) {
        temps.push(array[index])
      }
      return temps
    },
    removeItem(array, item) {
      var removeIndex = -1
      for (let index = 0; index < array.length; index++) {
        if (item == array[index]) {
          removeIndex = index
          break
        }
      }
      if (removeIndex > 0) {
        array.splice(removeIndex, 1)
      }
    },
    containItem(array, item) {
      for (let index = 0; index < array.length; index++) {
        if (item == array[index]) {
          return true
          break
        }
      }
      return false
    },
    // 权限提交
    jurisdictionSubmit() {
      this.roleRulesEdit[
        this.jurisdictionIndex
      ] = this.$refs.tree.getCheckedKeys()
      this.roleRulesEdit[
        this.jurisdictionIndex + '_upload'
      ] = this.$refs.tree
        .getCheckedKeys()
        .concat(this.$refs.tree.getHalfCheckedKeys())

      this.jurisdictionLoading = true
      updateRoleMenu({
        rules: this.roleRulesEdit['crm'].concat(this.roleRulesEdit['bi']),
        type: this.radioModel,
        id: this.roleActive.id,
        title: this.roleActive.title
      })
        .then(res => {
          this.getRoleList()
          this.$message.success('编辑成功')
          this.jurisdictionLoading = false
        })
        .catch(() => {
          this.jurisdictionLoading = false
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.role-authorization {
  height: 100%;
  box-sizing: border-box;
  overflow: hidden;
}
.title {
  font-size: 18px;
  height: 40px;
  padding: 0 20px;
  line-height: 40px;
  margin: 10px 0;
  color: #333;
}
.role-box {
  height: calc(100% - 60px);
  overflow: hidden;
  position: relative;
}
.nav {
  width: 200px;
  background: #fff;
  border: 1px solid #e6e6e6;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
}
.nav-new-btn {
  text-align: center;
  padding-top: 15px;
  padding-bottom: 15px;
}
.nav-new-btn .el-button {
  background: #4ab8b8;
  border-color: #4ab8b8;
  color: #fff;
  width: 140px;
  border-radius: 2px;
}
.content-box {
  background: #fff;
  border: 1px solid #e6e6e6;
  margin-left: 215px;
  height: 100%;
  overflow: hidden;
}
.content-table {
  padding-bottom: 15px;
  height: calc(100% - 61px);
  overflow: hidden;
}
.content-table > .el-button {
  float: right;
  margin-bottom: 15px;
  margin-right: 30px;
}
.content-box .content-table-span {
  color: #3e84e9;
  margin-left: 5px;
  cursor: pointer;
}

.content-table-header {
  padding: 15px;
  .content-table-header-reminder {
    flex: 1;
  }
}

/* 权限管理 */
.jurisdiction-content {
  height: calc(100% - 61px);
  position: relative;
  overflow: hidden;
}
.content-left {
  height: 100%;
  margin-right: 250px;
  overflow: hidden;
}
.content-right {
  width: 250px;
  position: absolute;
  top: 0;
  right: 0;
  height: 100%;
}
.jurisdiction-box {
  padding-bottom: 15px;
  height: calc(100% - 61px);
  overflow: hidden;
}
.jurisdiction-title {
  border-bottom: 1px dashed #e6e6e6;
  padding: 15px 25px;
}
.jurisdiction-content-checkbox {
  border-right: 1px dashed #e6e6e6;
  height: calc(100% - 47px);
  overflow-y: scroll;
  padding: 20px;
}
.jurisdiction-content-checkbox
  .el-tree
  /deep/
  .el-tree-node
  > .el-tree-node__content {
  margin-bottom: 20px;
  width: 150px;
}
.jurisdiction-content-checkbox /deep/ .el-tree .el-tree-node {
  white-space: inherit;
  margin-bottom: 5px;
}
.jurisdiction-content-checkbox
  /deep/
  .el-tree
  > .el-tree-node
  > .el-tree-node__children
  > .is-expanded
  > .el-tree-node__children
  > .is-expanded {
  display: inline-block;
}
.role-authorization /deep/ .el-tree-node__expand-icon {
  display: none;
}
.data-radio {
  padding: 5px 30px;
}
.data-radio .el-radio {
  display: block;
  margin: 20px 0;
}
/* 新建角色 */
.input-role {
  padding: 10px 0 20px;
  width: 100%;
}
.role-nav-box {
  line-height: 30px;
  overflow-y: auto;
  padding: 5px 0 20px 0;
  height: calc(100% - 65px);
}
.role-nav-box .item-list {
  color: #333;
  font-size: 13px;
  padding-left: 30px;
  height: 40px;
  line-height: 40px;
  border-right: 2px solid transparent;
  cursor: pointer;
  position: relative;
  .icon-close {
    position: absolute;
    top: 0;
    right: 8px;
    z-index: 1;
    display: none;
  }
}
.role-nav-box .item-label {
  color: #000;
  font-size: 14px;
  padding-left: 15px;
  margin-bottom: 5px;
}
.role-nav-box .item-list:hover {
  background: #ebf3ff;
  border-right-color: #46cdcf;
  .icon-close {
    display: block;
  }
}
.item-list-hover {
  background: #ebf3ff;
  border-right: 2px solid #46cdcf;
}

.role-nav-box .item-list:hover .icon-close {
  display: block;
  float: right;
}
.jurisdiction-edit {
  text-align: right;
  padding: 10px 30px;
  position: absolute;
  top: 10px;
  right: 30px;
}

/** 分页布局 */
.p-contianer {
  position: relative;
  background-color: white;
  height: 44px;
  .p-bar {
    float: right;
    margin: 5px 100px 0 0;
    font-size: 14px !important;
  }
}
@import './styles/table.scss';
</style>
