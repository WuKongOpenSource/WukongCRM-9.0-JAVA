<template>
  <div class="archive-project">
    <div class="header">
      归档项目统计
    </div>
    <div v-loading="loading"
         class="content-body">
      <el-table class="n-table--border"
                id="crm-table"
                v-loading="loading"
                :data="list"
                :height="tableHeight"
                stripe
                highlight-current-row
                style="width: 100%">
        <el-table-column prop="businessCheck"
                         :resizable='false'
                         label=""
                         width="38">
          <template slot="header"
                    slot-scope="slot">
            <i style="color:#999"
               class="wukong wukong-subproject"></i>
          </template>
          <template slot-scope="scope">
            <i :style="{'color':scope.row.color}"
               class="wukong wukong-subproject"></i>
          </template>
        </el-table-column>
        <el-table-column show-overflow-tooltip
                         prop="name"
                         label="项目名称">
        </el-table-column>
        <el-table-column show-overflow-tooltip
                         width="200"
                         prop="archiveTime"
                         label="归档时间">
        </el-table-column>
        <el-table-column label="操作"
                         width="100">
          <template slot-scope="scope">
            <el-button type="text"
                       @click.native="recoverProject(scope.row, scope.$index)">恢复项目</el-button>
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
  </div>
</template>

<script>
import particulars from '../components/particulars'
import { workWorkArchiveListAPI } from '@/api/projectManagement/archive'
import { workWorkSaveAPI } from '@/api/projectManagement/project'

export default {
  components: {
    particulars
  },
  data() {
    return {
      loading: false,
      tableHeight: document.documentElement.clientHeight - 205,
      list: [],

      // 分页
      currentPage: 1,
      pageSize: 15,
      pageSizes: [15, 30, 45, 60],
      total: 0
    }
  },
  created() {
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 205
    }

    this.getList()
  },
  methods: {
    /**
     * 更改每页展示数量
     */
    handleSizeChange(val) {
      this.pageSize = val
      this.getList()
    },

    /**
     * 更改当前页数
     */
    handleCurrentChange(val) {
      this.currentPage = val
      this.getList()
    },

    /**
     * 获取列表
     */
    getList() {
      this.loading = true
      workWorkArchiveListAPI({
        page: this.currentPage,
        limit: this.pageSize
      })
        .then(res => {
          this.list = res.data.list
          this.loading = false
        })
        .catch(err => {
          this.loading = false
        })
    },

    /**
     * 恢复项目
     */
    recoverProject(val, index) {
      this.$confirm('确定恢复?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.loading = true
          workWorkSaveAPI({
            workId: val.workId,
            status: 1 // 状态 1启用 2 删除 3归档
          })
            .then(res => {
              this.list.splice(index, 1)
              this.$message.success('恢复成功')
              this.$bus.$emit('recover-project', val.name, val.workId)
              this.loading = false
            })
            .catch(err => {
              this.loading = false
            })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消'
          })
        })
    }
  }
}
</script>

<style scoped lang="scss">
.archive-project {
  height: 100%;
  overflow: hidden;
  position: relative;
  .header {
    height: 60px;
    line-height: 60px;
    position: relative;
    padding: 0 20px;
    font-size: 18px;
  }

  .content-body {
    position: absolute;
    top: 60px;
    right: 0;
    bottom: 0;
    left: 0;
    border-radius: 3px;
    overflow-y: auto;
  }
}

.el-table /deep/ thead th {
  background-color: #f5f5f5;
  font-weight: 400;
}

.el-table {
  border: 1px solid #e6e6e6;
  border-bottom: none;
}

.p-contianer {
  position: relative;
  background-color: white;
  height: 44px;
  border: 1px solid #e6e6e6;

  .p-bar {
    float: right;
    margin: 5px 100px 0 0;
    font-size: 14px !important;
  }
}

.el-table::before {
  display: none;
}

.wukong-subproject {
  font-size: 22px;
  display: block;
  margin-right: 5px;
}
</style>
