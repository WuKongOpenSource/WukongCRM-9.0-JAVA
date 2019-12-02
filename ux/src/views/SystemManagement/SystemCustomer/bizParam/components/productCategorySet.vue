<template>
  <div v-loading="loading">
    <div class="content-title">
      <span>产品类别设置</span>
    </div>
    <div class="product-setting">
      <div class="product-setting-con">
        <div>
          <span
            class="el-icon-plus"
            style="color: #409EFF;"/>
          <el-button
            type="text"
            @click.native="handleTreeSetDrop({type:'create-one'})">新增一级分类</el-button>
        </div>
        <div class="tree-box">
          <el-tree
            :data="treeData"
            :props="defaultProps"
            default-expand-all>
            <flexbox
              slot-scope="{ node, data }"
              class="node-data">
              <img
                v-if="node.expanded"
                class="node-img"
                src="@/assets/img/fold.png">
              <img
                v-if="!node.expanded"
                class="node-img"
                src="@/assets/img/unfold.png">
              <div class="node-label">{{ node.label }}</div>
              <el-dropdown
                trigger="click"
                @command="handleTreeSetDrop">
                <div
                  class="node-label-set"
                  @click.stop="getChild(node)">设置</div>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item
                    v-for="(item, index) in treeSetTypes"
                    :key="index"
                    :command="item">{{ item.name }}</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </flexbox>
          </el-tree>
        </div>
      </div>
    </div>
    <el-dialog
      :visible.sync="productHandleDialog"
      title="提示"
      width="400px">
      <el-form :model="productForm">
        <el-form-item
          label="类别名称"
          label-width="80">
          <el-input
            v-model="productForm.name"
            autocomplete="off"/>
        </el-form-item>
      </el-form>
      <div
        slot="footer"
        class="dialog-footer">
        <el-button @click="productHandleDialog = false">取 消</el-button>
        <el-button
          type="primary"
          @click="handleProduct">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  productCategoryIndex,
  productCategorySave,
  productCategoryDelete
} from '@/api/systemManagement/SystemCustomer'

export default {
  name: 'ProductCategorySet',

  components: {},

  data() {
    return {
      loading: false, // 展示加载中效果

      // 产品类别设置
      treeData: [],
      /** 更多操作 */
      treeSetTypes: [],
      // 编辑产品弹窗
      productHandleDialog: false,
      productForm: { name: '', type: '', pid: '', categoryId: '' },
      defaultProps: {
        children: 'children',
        label: 'label'
      }
    }
  },

  created() {
    this.getProductCategoryIndex()
  },

  methods: {
    /**
     * 产品类别设置
     */
    getChild(node) {
      var temps = [
        { type: 'create-child', name: '新建子分类' },
        { type: 'create-brother', name: '新建平级分类' },
        { type: 'edit', name: '编辑分类' },
        { type: 'delete', name: '删除分类' }
      ]
      this.treeSetTypes = temps.map(function(item, index, array) {
        item['node'] = node
        return item
      })
    },

    /**
     * 产品操作
     */
    handleTreeSetDrop(command) {
      if (command.type == 'create-one') {
        this.productForm.type = command.type
        this.productForm.name = ''
        this.productHandleDialog = true
      }
      if (command.type == 'create-child') {
        this.productForm.type = command.type
        this.productForm.pid = command.node.data.categoryId
        this.productForm.name = ''
        this.productHandleDialog = true
      } else if (command.type == 'create-brother') {
        this.productForm.type = command.type
        this.productForm.pid = command.node.data.pid
        this.productForm.name = ''
        this.productHandleDialog = true
      } else if (command.type == 'edit') {
        this.productForm.type = command.type
        this.productForm.name = command.node.data.name
        this.productForm.categoryId = command.node.data.categoryId
        this.productForm.pid = command.node.data.pid
        this.productHandleDialog = true
      } else if (command.type == 'delete') {
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            this.loading = true
            productCategoryDelete({
              id: command.node.data.categoryId
            })
              .then(res => {
                this.$message({
                  type: 'success',
                  message: '删除成功!'
                })
                this.getProductCategoryIndex()
                this.loading = false
              })
              .catch(() => {
                this.loading = false
              })
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '已取消删除'
            })
          })
      }
    },

    /**
     * 产品类别操作
     */
    handleProduct() {
      if (this.productForm.name.length == 0) {
        this.$message({
          message: '请填写名称',
          type: 'warning'
        })
        return
      }
      this.productHandleDialog = false
      if (this.productForm.type == 'create-one') {
        this.loading = true
        productCategorySave({
          name: this.productForm.name
        })
          .then(res => {
            this.getProductCategoryIndex()
            this.$message.success('新增成功')
            this.loading = false
          })
          .catch(() => {
            this.loading = false
          })
      }
      if (this.productForm.type == 'create-child') {
        this.loading = true
        productCategorySave({
          pid: this.productForm.pid,
          name: this.productForm.name
        })
          .then(res => {
            this.getProductCategoryIndex()
            this.$message.success('新建成功')
            this.loading = false
          })
          .catch(() => {
            this.loading = false
          })
      } else if (this.productForm.type == 'create-brother') {
        this.loading = true
        productCategorySave({
          pid: this.productForm.pid,
          name: this.productForm.name
        })
          .then(res => {
            this.getProductCategoryIndex()
            this.$message.success('新建成功')
            this.loading = false
          })
          .catch(() => {
            this.loading = false
          })
      } else if (this.productForm.type == 'edit') {
        this.loading = true
        productCategorySave({
          categoryId: this.productForm.categoryId,
          pid: this.productForm.pid,
          name: this.productForm.name
        })
          .then(res => {
            this.getProductCategoryIndex()
            this.$message.success('编辑成功')
            this.loading = false
          })
          .catch(() => {
            this.loading = false
          })
      }
    },

    /**
     * 获取产品分类数据
     */
    getProductCategoryIndex() {
      this.loading = true
      productCategoryIndex({
        type: 'tree'
      })
        .then(res => {
          this.loading = false
          this.treeData = res.data
        })
        .catch(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.content-title {
  padding: 10px;
  border-bottom: 1px solid #e6e6e6;
}

.content-title > span {
  display: inline-block;
  height: 36px;
  line-height: 36px;
  margin-left: 20px;
}

/* 产品类别设置 */
.product-setting {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin: 30px;
  position: relative;
}
.tree-box {
  overflow: auto;
}
.tree-box /deep/ .el-tree-node__expand-icon {
  display: none;
}
.tree-box /deep/ .el-tree-node__content {
  margin-bottom: 10px;
}
.node-data {
  font-size: 13px;
  .node-img {
    width: 15px;
    height: 15px;
    display: block;
    margin-right: 8px;
  }
  .node-label:hover {
    background-color: #ededed;
  }
  .node-label-set {
    margin-left: 8px;
    font-size: 12px;
    color: $xr-color-primary;
    display: none;
  }
}

.node-data:hover .node-label-set {
  display: block;
}

.product-setting-con {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  overflow: auto;
}
</style>
