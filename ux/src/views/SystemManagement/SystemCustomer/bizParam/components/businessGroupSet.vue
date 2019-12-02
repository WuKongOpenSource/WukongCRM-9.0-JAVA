<template>
  <div v-loading="loading">
    <div class="content-title">
      <span>商机组设置</span>
      <el-button
        type="primary"
        class="rt"
        size="medium"
        @click="addBusiness">添加商机组</el-button>
    </div>
    <div class="business-table">
      <el-table
        :data="businessData"
        :height="tableHeight"
        :header-cell-style="headerCellStyle"
        style="width: 100%"
        stripe>
        <el-table-column
          v-for="(item, index) in businessList"
          :key="index"
          :prop="item.field"
          :label="item.label"
          :formatter="fieldFormatter"
          show-overflow-tooltip/>
        <el-table-column
          fixed="right"
          label="操作"
          width="140">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="businessEdit(scope.row)">编 辑</el-button>
            <el-button
              type="text"
              size="small"
              @click="businessDelect(scope)">删 除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="p-contianer">
        <el-pagination
          :current-page="currentPage"
          :page-sizes="pageSizes"
          :page-size.sync="pageSize"
          :total="total"
          class="p-bar"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"/>
      </div>
    </div>
    <business-dialog
      :info-data="businessObj"
      :business-dialog-visible="businessDialogVisible"
      :business-title="businessTitle"
      @businessClose="businessClose"
      @businessSubmit="businessSubmit"/>
  </div>
</template>

<script>
import BusinessDialog from '@/views/SystemManagement/components/businessDialog'
import {
  businessGroupList,
  businessGroupAdd,
  businessGroupUpdate,
  businessGroupRead,
  businessGroupDelete
} from '@/api/systemManagement/SystemCustomer'

export default {
  name: 'BusinessGroupSet',

  components: {
    BusinessDialog
  },

  data() {
    return {
      loading: false, // 展示加载中效果
      tableHeight: document.documentElement.clientHeight - 320, // 表的高度

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
      currentPage: 1,
      pageSize: 10,
      pageSizes: [10, 20, 30, 40],
      total: 0
    }
  },
  created() {
    /** 控制table的高度 */
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 320
    }
    this.getBusinessGroupList()
  },
  methods: {
    /**
     * 商机组列表头样式
     */
    headerCellStyle(val, index) {
      return { background: '#F2F2F2' }
    },

    /**
     * 更改每页展示数量
     */
    handleSizeChange(val) {
      this.pageSize = val
      this.getBusinessGroupList()
    },

    /**
     * 更改当前页数
     */
    handleCurrentChange(val) {
      this.currentPage = val
      this.getBusinessGroupList()
    },

    /**
     * 商机组列表
     */
    getBusinessGroupList() {
      this.loading = true
      businessGroupList({
        page: this.currentPage,
        limit: this.pageSize
      })
        .then(res => {
          this.loading = false
          this.businessData = res.data.list
          this.total = res.data.totalRow
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * 商机列表格式化
     */
    fieldFormatter(row, column) {
      // 如果需要格式化
      if (column.property == 'deptName') {
        // 格式部门
        var info = row.deptIds
        var name = ''
        if (info) {
          for (let index = 0; index < info.length; index++) {
            name =
              name + info[index].name + (index === info.length - 1 ? '' : '、')
          }
        }
        return name || '全公司'
      }
      return row[column.property]
    },

    /**
     * 商机组编辑
     */
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

    /**
     * 商机组删除
     */
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

    /**
     * 商机组添加
     */
    addBusiness() {
      this.businessObj = { name: '', businessDep: [], settingList: '' }
      this.businessDialogVisible = true
      this.businessTitle = '添加商机组'
    },

    /**
     * 商机组添加 -- 关闭
     */
    businessClose() {
      this.businessDialogVisible = false
    },

    /**
     * 商机组添加 -- 确定按钮
     */
    businessSubmit(name, dep, list, title, typeId) {
      var businessHandleRequest = null
      var params = {
        crmBusinessType: {
          name: name,
          typeId: typeId || null
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

/* 商机组设置 */

.business-table {
  border: 1px solid #e6e6e6;
  margin: 30px;
  flex: 1;
  overflow: auto;
  box-sizing: border-box;
}

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
</style>
