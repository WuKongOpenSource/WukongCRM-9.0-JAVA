<template>
  <el-dialog
    v-loading="loading"
    :title="diaTitle"
    :visible="show"
    :modal-append-to-body="true"
    :append-to-body="true"
    :close-on-click-modal="false"
    class="create-dialog"
    width="700px"
    @close="closeView">
    <div class="label-input">
      <label class="label-title">权限名称</label>
      <el-input
        v-model="roleName"
        placeholder="请输入权限名称"/>
    </div>
    <div class="label-input">
      <label class="label-title">权限描述</label>
      <el-input
        v-model="remark"
        :rows="2"
        type="textarea"
        placeholder="请输入权限描述"/>
    </div>
    <label class="label-title">权限配置</label>
    <div class="jurisdiction-content-checkbox">
      <el-tree
        ref="tree"
        :data="showTreeData"
        :indent="0"
        :expand-on-click-node="false"
        :props="defaultProps"
        show-checkbox
        node-key="menuId"
        style="height: 0;"
        empty-text=""
        default-expand-all/>
    </div>
    <span
      slot="footer"
      class="dialog-footer">
      <el-button
        type="primary"
        @click="sureClick">确 定</el-button>
      <el-button @click="closeView">取 消</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { systemMenuGetWorkMenuListAPI, systemRoleSetWorkRoleAPI } from '@/api/systemManagement/project'

export default {
  name: 'JurisdictionCreate', // 文件导入
  components: {},

  props: {
    show: {
      type: Boolean,
      default: false
    },

    action: {
      type: Object,
      default: () => {
        return {
          type: 'save'
        }
      }
    }
  },
  data() {
    return {
      loading: false,
      roleName: '',
      remark: '',
      showTreeData: [],
      defaultProps: {
        children: 'childMenu',
        label: 'menuName'
      }
    }
  },

  computed: {
    diaTitle() {
      if (this.action.type == 'save') {
        return '新建'
      }

      return '编辑'
    }
  },

  watch: {
    show(value) {
      if (value) {
        this.initInfo()
      }
    }
  },

  mounted() {},

  methods: {
    /**
     * 初始化数据
     */
    initInfo() {
      if (this.action.type == 'update') {
        this.roleName = this.action.data.roleName
        this.remark = this.action.data.remark
      } else {
        this.roleName = ''
        this.remark = ''
        if (this.$refs.tree) {
          this.$refs.tree.setCheckedKeys([])
        }
      }

      if (this.showTreeData.length == 0) {
        this.getRulesList()
      } else {
        this.checkTreeByUpdateInfo()
      }
    },

    /**
     * 获取规则
     */
    getRulesList() {
      this.loading = true
      systemMenuGetWorkMenuListAPI()
        .then(res => {
          this.showTreeData = [res.data]
          this.checkTreeByUpdateInfo()
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * 标记数据
     */
    checkTreeByUpdateInfo() {
      if (this.action.type == 'update') {
        this.$nextTick(() => {
          if (this.$refs.tree) {
            this.$refs.tree.setCheckedKeys(
              this.getUserModuleRules(this.action.data.rules)
            )
          }
        })
      }
    },

    /**
     * 确定
     */
    sureClick() {
      if (!this.roleName) {
        this.$message.error('请填写权限名称')
      } else {
        this.loading = true
        const rules = this.$refs.tree.getCheckedKeys()
        const params = {
          roleName: this.roleName,
          remark: this.remark,
          rules: rules
        }

        if (this.action.type == 'update') {
          params.roleId = this.action.data.roleId
        }
        systemRoleSetWorkRoleAPI(params)
          .then(res => {
            this.loading = false
            this.$emit('submite')
            this.closeView()
          })
          .catch(() => {
            this.loading = false
          })
      }
    },

    closeView() {
      this.$emit('update:show', false)
    },

    /**
     * 获得check的实际数据
     */
    getUserModuleRules(array) {
      if (!array) {
        array = []
      }
      var firstTree = this.showTreeData[0]
      var hasRemove = false
      var copyArray = this.copyItem(array)
      for (
        let firstIndex = 0;
        firstIndex < firstTree.childMenu.length;
        firstIndex++
      ) {
        const firstItem = firstTree.childMenu[firstIndex]
        if (!firstItem.childMenu) {
          firstItem.childMenu = []
        }
        for (let index = 0; index < array.length; index++) {
          const element = array[index]
          var temps = []
          for (
            let secondIndex = 0;
            secondIndex < firstItem.childMenu.length;
            secondIndex++
          ) {
            const secondItem = firstItem.childMenu[secondIndex]
            if (secondItem.menuId == element) {
              temps.push(secondItem)
            }
          }
          if (temps.length != firstItem.childMenu.length) {
            hasRemove = true
            this.removeItem(copyArray, firstItem.menuId)
          }
        }
      }

      if (hasRemove) {
        this.removeItem(copyArray, firstTree.menuId)
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
        }
      }
      return false
    }
  }
}
</script>

<style scoped lang="scss">
.create-dialog /deep/ .el-dialog__body {
  padding-top: 0 !important;
}

.label-title {
  display: block;
  margin: 20px 0 10px 0;
  color: #666;
}

.label-input {
  position: relative;
}

.jurisdiction-content-checkbox {
  border: 1px solid #e6e6e6;
  border-radius: 2px;
  height: 200px;
  overflow-y: auto;
  padding: 10px 0;
}
.jurisdiction-content-checkbox
  .el-tree
  /deep/
  .el-tree-node
  > .el-tree-node__content {
  margin-bottom: 5px;
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
</style>
