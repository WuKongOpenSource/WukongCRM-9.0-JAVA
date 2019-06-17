<template>
  <div>
    <div class="se-table-header">
      <el-button class="se-table-header-button"
                 @click="addExamine"
                 type="primary">添加审批流程</el-button>
    </div>
    <el-table class="main-table"
              id="examine-table"
              v-loading="loading"
              :data="list"
              :height="tableHeight"
              stripe
              highlight-current-row
              style="width: 100%"
              @row-click="handleRowClick">
      <el-table-column v-for="(item, index) in fieldList"
                       :key="index"
                       show-overflow-tooltip
                       :formatter="fieldFormatter"
                       :prop="item.prop"
                       :width="item.width"
                       :label="item.label">
        <template slot="header"
                  slot-scope="scope">
          <div class="table-head-name">{{scope.column.label}}</div>
        </template>
      </el-table-column>
      <el-table-column>
      </el-table-column>
      <el-table-column fixed="right"
                       label="操作"
                       width="250">
        <template slot-scope="scope">
          <el-button @click="handleClick('edit-table', scope)"
                     type="text"
                     size="small">编辑表单</el-button>
          <el-button @click="handleClick('edit', scope)"
                     type="text"
                     size="small">编辑</el-button>
          <el-button :disabled="scope.row.isSys === 1"
                     @click="handleClick('delete', scope)"
                     type="text"
                     size="small">删除</el-button>
          <el-button @click="handleClick('change', scope)"
                     type="text"
                     size="small">{{scope.row['status'] === 0 ? '启用' : '停用'}}</el-button>
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
    <create-examine-category v-if="showHandleView"
                             :handle="createHandleInfo"
                             @save="saveSuccess"
                             @hiden-view="showHandleView=false"></create-examine-category>
    <examine-category-detail v-if="showDetail"
                             :data="detailData"
                             @refresh="getList"
                             @hide-view="showDetail=false">

    </examine-category-detail>
  </div>
</template>

<script>
import {
  oaExamineCategory,
  oaExamineCategoryEnables,
  oaExamineCategoryDelete
} from '@/api/systemManagement/workbench'
import CreateExamineCategory from './CreateExamineCategory'
import ExamineCategoryDetail from './examineCategoryDetail'
import { timestampToFormatTime } from '@/utils'

export default {
  name: 'examine-manager',
  components: {
    CreateExamineCategory,
    ExamineCategoryDetail
  },
  data() {
    return {
      loading: false, // 加载动画
      tableHeight: document.documentElement.clientHeight - 240, // 表的高度
      list: [],
      fieldList: [
        {
          prop: 'title',
          label: '审批名称',
          width: 150
        },
        {
          prop: 'examineType',
          label: '流程类型',
          width: 150
        },
        {
          prop: 'userIds',
          label: '可见范围',
          width: 150
        },
        {
          prop: 'remarks',
          label: '审批说明',
          width: 150
        },
        {
          prop: 'updateTime',
          label: '最后修改时间',
          width: 150
        },
        {
          prop: 'status',
          label: '状态',
          width: 150
        }
      ],
      currentPage: 1,
      pageSize: 10,
      pageSizes: [10, 20, 30, 40],
      total: 0,
      showHandleView: false, // 审批类型操作
      // 创建的相关信息
      createHandleInfo: { action: 'save', type: 'examine', id: '' },
      // 详情展示
      showDetail: false,
      detailData: {}
    }
  },
  watch: {},
  mounted() {
    var self = this
    /** 控制table的高度 */
    window.onresize = function() {
      self.tableHeight = document.documentElement.clientHeight - 240
    }

    this.getList()
  },
  methods: {
    /** 新建成功 */
    saveSuccess() {
      this.currentPage = 1
      this.getList()
    },
    /** 获取列表数据 */
    /** 获取列表数据 */
    getList() {
      this.loading = true
      oaExamineCategory({
        page: this.currentPage,
        limit: this.pageSize
      })
        .then(res => {
          this.list = res.data.list

          this.total = res.data.totalRow

          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 格式化字段 */
    fieldFormatter(row, column) {
      // 如果需要格式化
      if (column.property === 'examineType') {
        if (row[column.property] === 1) {
          return '固定审批流'
        } else if (row[column.property] === 2) {
          return '授权审批人'
        } else {
          return ''
        }
      } else if (column.property === 'userIds') {
        let structures = row['deptIds'] || []
        let strName = structures
          .map(item => {
            return item.name
          })
          .join('、')

        if (strName) {
          strName += '、'
        }

        let users = row['userIds'] || []
        let userName = users
          .map(item => {
            return item.realname
          })
          .join('、')

        let name = strName + userName
        return name ? name : '全公司'
        return name ? name : '全公司'
      } else if (column.property === 'status') {
        if (row[column.property] === 0) {
          return '停用'
        }
        return '启用'
      }
      return row[column.property]
    },
    /**
     *  添加审批流
     */
    addExamine() {
      this.createHandleInfo = { action: 'save', type: 'examine', id: '' }
      this.showHandleView = true
    },
    /** 列表操作 */
    // 当某一行被点击时会触发该事件
    handleRowClick(row, column, event) {
      if (column.property) {
        this.detailData = row
        this.showDetail = true
      }
    },
    // 更改每页展示数量
    handleSizeChange(val) {
      this.pageSize = val
      this.getList()
    },
    // 更改当前页数
    handleCurrentChange(val) {
      this.currentPage = val
      this.getList()
    },
    handleClick(type, scope) {
      if (type === 'edit-table') {
        this.$router.push({
          name: 'handlefield',
          params: {
            type: 'oa_examine',
            label: '10',
            id: scope.row.categoryId
          }
        })
      } else if (type === 'edit') {
        this.createHandleInfo.action = 'update'
        this.createHandleInfo.id = scope.row.categoryId
        this.createHandleInfo.data = scope.row
        this.showHandleView = true
      } else if (type === 'delete') {
        // 启用停用
        this.$confirm('您确定要删除该审批流?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            this.loading = true
            oaExamineCategoryDelete({
              id: scope.row['categoryId']
            })
              .then(res => {
                this.list.splice(scope.$index, 1)
                this.getList()

                this.$message({
                  type: 'success',
                  message: '操作成功'
                })
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
      } else if (type === 'change') {
        // 启用停用
        this.$confirm(
          '您确定要' +
            (scope.row['status'] === 0 ? '启用' : '停用') +
            '该审批流?',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
          .then(() => {
            oaExamineCategoryEnables({
              id: scope.row['categoryId'],
              status: scope.row['status'] === 0 ? 1 : 0
            })
              .then(res => {
                scope.row['status'] = scope.row['status'] === 0 ? 1 : 0
                this.$message({
                  type: 'success',
                  message: '操作成功'
                })
              })
              .catch(() => {})
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '已取消删除'
            })
          })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.se-table-header {
  height: 50px;
  background-color: white;
  position: relative;
  .se-table-header-button {
    float: right;
    margin-right: 20px;
    margin-top: 10px;
  }
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

@import '../../styles/table.scss';
</style>
