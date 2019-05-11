<template>
  <div class="system-customer">
    <div class="title">客户管理</div>
    <div class="customer-content">
      <!-- 客户管理导航 -->
      <div class="system-view-nav">
        <el-menu default-active="1"
                 @select="menuSelect">
          <el-menu-item v-for="(item, index) in menuList"
                        :key="index"
                        :index="item.key">
            <span slot="title">{{item.label}}</span>
          </el-menu-item>
        </el-menu>
      </div>
      <!-- 右侧显示内容 ———— 自定义字段 -->
      <div class="system-view-table"
           v-if="menuIndex == '1'">
        <div class="content-title">
          <span>自定义字段设置</span>
        </div>
        <div class="table-box">
          <div v-for="(item, index) in tableList"
               :key="index"
               class="table-list">
            <img class="table-item-icon"
                 :src="getCustomFieldIcon(item.types)" />
            <div class="table-item-label">{{item.name}}</div>
            <div class="table-item-time">{{item.updateTime == 0 ? '暂无' : item.updateTime}}更新</div>
            <div class="table-right-btn">
              <el-button style="font-weight: 400;"
                         @click="handleCustomField('edit', item, index)"
                         type="text">编 辑</el-button>
              <el-button style="font-weight: 400;"
                         @click="handleCustomField('preview', item, index)"
                         type="text">预 览</el-button>
            </div>
          </div>
        </div>
      </div>
      <!-- 客户保护规则设置 -->
      <div v-else-if="menuIndex == '2'"
           class="system-view-table">
        <div class="content-title">
          <span>客户公海规则设置</span>
          <el-button type="primary"
                     class="rt"
                     size="medium"
                     @click="customerSettingSave">保存</el-button>
        </div>
        <div class="customer-setting">
          <label>客户公海规则设置</label>
          <div class="customer-radio">
            <el-radio-group v-model="customerData.type">
              <el-radio :label="0">不启用</el-radio>
              <el-radio :label="1">启用</el-radio>
            </el-radio-group>
            <br />
            <el-input-number v-model="customerData.followupDay"
                             controls-position="right"
                             :min="0"
                             size="small"></el-input-number>
            <span>&nbsp;天不跟进或&nbsp;</span>
            <el-input-number v-model="customerData.dealDay"
                             controls-position="right"
                             :min="0"
                             size="small"></el-input-number>
            <span>&nbsp;天未成交</span>
          </div>
        </div>
      </div>
      <!-- 商机组设置 -->
      <div v-else-if="menuIndex == '3'"
           class="system-view-table">
        <div class="content-title">
          <span>商机组设置</span>
          <el-button type="primary"
                     class="rt"
                     size="medium"
                     @click="addBusiness">添加商机组</el-button>
        </div>
        <div class="business-table">
          <el-table :data="businessData"
                    style="width: 100%"
                    stripe
                    :header-cell-style="headerCellStyle">
            <el-table-column v-for="(item, index) in businessList"
                             :key="index"
                             show-overflow-tooltip
                             :prop="item.field"
                             :formatter="fieldFormatter"
                             :label="item.label">

            </el-table-column>
            <el-table-column fixed="right"
                             label="操作"
                             width="100">
              <template slot-scope="scope">
                <el-button @click="businessEdit(scope.row)"
                           type="text"
                           size="small">编 辑</el-button>
                <el-button type="text"
                           size="small"
                           @click="businessDelect(scope)">删 除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <!-- 产品类别设置 -->
      <div v-else-if="menuIndex == '4'"
           class="system-view-table">
        <div class="content-title">
          <span>产品类别设置</span>
        </div>
        <div class="product-setting">
          <div>
            <span class="el-icon-plus"
                  style="color: #409EFF;"></span>
            <el-button @click.native="handleTreeSetDrop({type:'create-one'})"
                       type="text">新增一级分类</el-button>
          </div>
          <div class="tree-box">
            <el-tree :data="treeData"
                     default-expand-all
                     :props="defaultProps">
              <flexbox slot-scope="{ node, data }"
                       class="node-data">
                <img class="node-img"
                     v-if="node.expanded"
                     src="@/assets/img/fold.png">
                <img class="node-img"
                     v-if="!node.expanded"
                     src="@/assets/img/unfold.png">
                <div class="node-label">{{ node.label }}</div>
                <el-dropdown trigger="click"
                             @command="handleTreeSetDrop">
                  <div @click.stop="getChild(node)"
                       class="node-label-set">设置</div>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item v-for="(item, index) in treeSetTypes"
                                      :key="index"
                                      :command="item">{{item.name}}</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </flexbox>
            </el-tree>
          </div>
        </div>
      </div>
      <!-- 业绩目标设置 -->
      <div v-else-if="menuIndex == '5'"
           class="system-view-table">
        <task-set-statistics></task-set-statistics>
      </div>
    </div>
    <business-dialog @businessClose="businessClose"
                     @businessSubmit="businessSubmit"
                     :infoData="businessObj"
                     :businessDialogVisible="businessDialogVisible"
                     :businessTitle="businessTitle">
    </business-dialog>
    <el-dialog title="提示"
               :visible.sync="productHandleDialog"
               width="400px">
      <el-form :model="productForm">
        <el-form-item label="类别名称"
                      label-width="80">
          <el-input v-model="productForm.name"
                    autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button @click="productHandleDialog = false">取 消</el-button>
        <el-button type="primary"
                   @click="handleProduct">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 表单预览 -->
    <preview-field-view v-if="showTablePreview"
                        :types="tablePreviewData.types"
                        :typesId="tablePreviewData.typesId"
                        :label="tablePreviewData.label"
                        @hiden-view="showTablePreview=false"></preview-field-view>
  </div>
</template>

<script>
import BusinessDialog from '../components/businessDialog'
import TaskSetStatistics from './components/TaskSetStatistics' // 业绩目标设置
import PreviewFieldView from '@/views/SystemManagement/components/previewFieldView'
import {
  businessGroupList,
  businessGroupAdd,
  businessGroupUpdate,
  businessGroupRead,
  businessGroupDelete,
  customFieldIndex,
  productCategoryIndex,
  productCategorySave,
  productCategoryDelete,
  crmSettingConfig, // 客户保护规则
  crmSettingConfigData // 客户详情
} from '@/api/systemManagement/SystemCustomer'
import { Loading } from 'element-ui'
import { getDateFromTimestamp } from '@/utils'
import moment from 'moment'

export default {
  components: {
    BusinessDialog,
    TaskSetStatistics,
    PreviewFieldView
  },
  watch: {
    loading: function(val) {
      if (val) {
        this.loadingView = Loading.service({
          target: document.querySelector('.system-view-table')
        })
      } else {
        if (this.loadingView) {
          this.loadingView.close()
        }
      }
    }
  },
  data() {
    return {
      menuList: [
        { label: '自定义字段设置', key: '1' },
        { label: '客户保护规则设置', key: '2' },
        { label: '商机组设置', key: '3' },
        { label: '产品类别设置', key: '4' },
        { label: '业绩目标设置', key: '5' }
      ],
      loadingView: null, // 加载效果
      loading: false, // 展示加载中效果
      // 自定义字段设置
      tableList: [],
      // 展示表单预览
      tablePreviewData: { types: '', typesId: '' },
      showTablePreview: false,
      // 导航显示不同的页面
      menuIndex: '1',
      // 客户掉报规则设置
      customerData: {
        type: 0,
        followupDay: 0,
        dealDay: 0
      },
      // 商机组设置
      /** 商机组每行的信息 */
      businessObj: { name: '', businessDep: [], settingList: '' },
      businessData: [],
      businessList: [
        { label: '商机组名称', field: 'name' },
        { label: '应用部门', field: 'deptName' },
        { label: '创建时间', field: 'createTime' },
        { label: '创建人', field: 'createName' }
      ],
      // 添加商机组
      businessDialogVisible: false,
      businessTitle: '添加商机组',
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
  methods: {
    // 导航栏点击事件
    menuSelect(i, key) {
      this.menuIndex = i
      if (i == '4') {
        this.getProductCategoryIndex()
      } else if (i == '2') {
        this.customerSettingData()
      }
    },
    /** 客户保护规则 */
    customerSettingData() {
      this.loading = true
      crmSettingConfigData()
        .then(res => {
          this.loading = false
          res.data.type = parseInt(res.data.type)
          this.customerData = res.data
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 设置保存 */
    customerSettingSave() {
      this.loading = true
      crmSettingConfig(this.customerData)
        .then(res => {
          this.loading = false
          this.$message.success('操作成功')
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 商机组列表 */
    getBusinessGroupList() {
      this.loading = true
      businessGroupList({
        page: 1,
        limit: 100,
        search: ''
      })
        .then(res => {
          this.loading = false
          this.businessData = res.data.list
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 格式化字段 */
    fieldFormatter(row, column) {
      // 如果需要格式化
      if (column.property == 'deptName') {
        //格式部门
        var info = row.deptIds
        var name = ''
        if (info) {
          for (let index = 0; index < info.length; index++) {
            name =
              name + info[index].name + (index === info.length - 1 ? '' : '、')
          }
        }
        return name ? name : '全公司'
      }
      return row[column.property]
    },
    // 商机组编辑
    businessEdit(data) {
      businessGroupRead({
        id: data.typeId
      })
        .then(res => {
          var settingList = res.data.statusList || []
          this.businessObj = {
            typeId: res.data.typeId,
            name: res.data.name,
            businessDep: res.data.deptIds || [],
            settingList: settingList
          }
          this.businessDialogVisible = true
          this.businessTitle = '编辑商机组'
        })
        .catch(() => {})
    },
    // 商机删除
    businessDelect(scope) {
      this.$confirm('确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          businessGroupDelete({
            id: scope.row.typeId
          })
            .then(res => {
              this.businessData.splice(scope.$index, 1)
              this.$message.success('删除成功')
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
    // 商机组添加
    addBusiness() {
      this.businessObj = { name: '', businessDep: [], settingList: '' }
      this.businessDialogVisible = true
      this.businessTitle = '添加商机组'
    },
    // 商机组添加 -- 关闭
    businessClose() {
      this.businessDialogVisible = false
    },
    // 商机组添加 -- 确定按钮
    businessSubmit(name, dep, list, title, typeId) {
      var businessHandleRequest = null
      var params = {
        crmBusinessType: {
          name: name,
          typeId: typeId ? typeId : null
        },
        deptIds: dep,
        crmBusinessStatus: list
      }
      if (title == '添加商机组') {
        businessHandleRequest = businessGroupAdd
      } else {
        businessHandleRequest = businessGroupUpdate
      }
      businessHandleRequest(params)
        .then(res => {
          this.$message.success('操作成功')
          this.getBusinessGroupList()
          this.businessClose()
        })
        .catch(() => {})
    },
    // 产品类别设置
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
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            this.loading = true
            productCategoryDelete({
              id: command.node.data.categoryId
            })
              .then(res => {
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
            this.loading = false
          })
          .catch(() => {
            this.loading = false
          })
      }
    },
    /** 获取产品分类数据 */
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
    },
    // 设置列表头样式
    headerCellStyle(val, index) {
      return { background: '#F2F2F2' }
    },
    // 进入添加自定义字段
    handleCustomField(type, item, index) {
      if (type == 'edit') {
        this.$router.push({
          name: 'handlefield',
          params: {
            type: item.types,
            id: 'none',
            label: item.label
          }
        })
      } else if (type == 'preview') {
        this.tablePreviewData = item
        this.showTablePreview = true
      }
    },
    // 根据自定义字段types 获取展示icon
    getCustomFieldIcon(type) {
      if (type == 'crm_leads') {
        return require('@/assets/img/field_leads_manager.png')
      } else if (type == 'crm_customer') {
        return require('@/assets/img/field_customer_manager.png')
      } else if (type == 'crm_contacts') {
        return require('@/assets/img/field_contacts_manager.png')
      } else if (type == 'crm_business') {
        return require('@/assets/img/field_business_manager.png')
      } else if (type == 'crm_contract') {
        return require('@/assets/img/field_contract_manager.png')
      } else if (type == 'crm_product') {
        return require('@/assets/img/field_product_manager.png')
      } else if (type == 'crm_receivables') {
        return require('@/assets/img/field_receivables_manager.png')
      }
      return require('@/assets/img/field_other_manager.png')
    }
  },
  created() {
    this.getBusinessGroupList()

    // 获取自定义字段的更新时间
    customFieldIndex({})
      .then(res => {
        this.tableList = res.data
      })
      .catch(() => {})
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.system-customer {
  /* padding: 0 20px 20px; */
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}
.title {
  font-size: 18px;
  height: 40px;
  padding: 0 20px;
  line-height: 40px;
  margin: 10px 0;
  color: #333;
}
.customer-content {
  flex: 1;
  position: relative;
  display: flex;
}
.system-view-nav {
  min-width: 200px;
  background: #fff;
  margin-right: 10px;
  padding-top: 20px;
  border: 1px solid #e6e6e6;
}
.system-view-table {
  flex: 1;
  border: 1px solid #e6e6e6;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow-x: auto;
}
.system-view-nav .is-active {
  border-right: 2px solid #46cdcf;
  background-color: #ecf5ff;
}
.system-view-nav /deep/ .el-menu {
  border-right: none;
}
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
.table-list {
  border-bottom: 1px solid #e6e6e6;
  height: 60px;
  overflow: hidden;
  align-items: center;
  font-size: 13px;
  display: flex;
}
.table-item-label {
  text-align: left;
  flex: 1;
}
.table-item-icon {
  display: block;
  width: 35px;
  height: 35px;
  margin-right: 15px;
}
.table-box {
  margin: 30px;
  overflow: auto;
}
.table-right-btn {
  text-align: right;
  flex: 1;
  padding-right: 40px;
}
.table-item-time {
  flex: 1;
  color: #999;
}
.customer-setting {
  padding: 30px;
}
.customer-radio {
  display: inline-block;
  vertical-align: top;
}
.customer-setting > label {
  padding-right: 30px;
}
.customer-radio > .el-radio-group {
  margin-bottom: 30px;
}
.business-table {
  border: 1px solid #e6e6e6;
  margin: 30px;
  flex: 1;
  overflow: auto;
  box-sizing: border-box;
}
/* 产品类别设置 */
.product-setting {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin: 30px;
}
.tree-box {
  flex: 1;
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
</style>
