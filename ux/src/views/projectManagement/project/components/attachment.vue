<template>
  <div
    v-loading="loading"
    class="attachment">
    <div class="attachment-body">
      <el-table
        :data="list"
        :height="tableHeight"
        :header-cell-style="headerRowStyle"
        :cell-style="cellStyle"
        align="center"
        header-align="center"
        stripe
        style="width: 100%;border: 1px solid #E6E6E6;">
        <el-table-column
          v-for="(item, index) in fieldList"
          :key="index"
          :prop="item.prop"
          :label="item.label"
          show-overflow-tooltip/>
        <el-table-column
          label="操作"
          width="150">
          <template slot-scope="scope">
            <flexbox justify="center">
              <el-button
                type="text"
                @click.native="handleFile('preview', scope)">预览</el-button>
              <el-button
                type="text"
                @click.native="handleFile('edit', scope)">重命名</el-button>
              <el-button
                type="text"
                @click.native="handleFile('delete', scope)">删除</el-button>
            </flexbox>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog
      :append-to-body="true"
      :visible.sync="editDialog"
      title="编辑"
      width="30%">
      <el-form :model="editForm">
        <el-form-item
          label="新名称"
          label-width="100">
          <el-input
            v-model="editForm.name"
            autocomplete="off"/>
        </el-form-item>
      </el-form>
      <div
        slot="footer"
        class="dialog-footer">
        <el-button @click="editDialog = false">取 消</el-button>
        <el-button
          type="primary"
          @click="confirmEdit">确 定</el-button>
      </div>
    </el-dialog>
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
</template>

<script>
import { crmFileDelete, crmFileUpdate } from '@/api/common'

import { workWorkFileListAPI } from '@/api/projectManagement/project'

export default {

  props: {
    workId: [Number, String]
  },
  data() {
    return {
      firstRequst: true,
      list: [],
      loading: false,
      // 分页
      currentPage: 1,
      pageSize: 15,
      pageSizes: [15, 30, 45, 60],
      total: 0,

      fieldList: [],
      tableHeight: document.documentElement.clientHeight - 240,
      /** 重命名 弹窗 */
      editDialog: false,
      /** 编辑信息 */
      editForm: { name: '', data: {}}
    }
  },

  watch: {
    workId: function() {
      this.currentPage = 1
      this.list = []
      this.getList(true)
    }
  },

  mounted() {
    if (this.firstRequst) {
      this.firstRequst = false
      this.getList(true)
    } else {
      this.getList(false)
    }
  },

  created() {
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 240
    }

    this.fieldList.push({ prop: 'name', width: '200', label: '附件名称' })
    this.fieldList.push({ prop: 'size', width: '200', label: '附件大小' })
    this.fieldList.push({
      prop: 'createUserName',
      width: '200',
      label: '上传人'
    })
    this.fieldList.push({
      prop: 'createTime',
      width: '200',
      label: '上传时间'
    })
  },

  methods: {
    /**
     * 更改每页展示数量
     */
    handleSizeChange(val) {
      this.pageSize = val
      this.getList(false)
    },

    /**
     * 更改当前页数
     */
    handleCurrentChange(val) {
      this.currentPage = val
      this.getList(false)
    },

    /**
     * 获取附件
     */
    getList(loading) {
      this.loading = loading
      workWorkFileListAPI({
        page: this.currentPage,
        limit: this.pageSize,
        work_id: this.workId
      })
        .then(res => {
          this.list = res.data.list
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * 通过回调控制表头style
     */
    headerRowStyle({ row, column, rowIndex, columnIndex }) {
      return { textAlign: 'center' }
    },

    /**
     * 通过回调控制style
     */
    cellStyle({ row, column, rowIndex, columnIndex }) {
      return { textAlign: 'center' }
    },

    /**
     * 编辑
     */
    confirmEdit() {
      if (this.editForm.name) {
        crmFileUpdate({
          fileId: this.editForm.data.row.fileId,
          name: this.editForm.name
        })
          .then(res => {
            this.$message.success('编辑成功')
            this.editDialog = false
            var item = this.list[this.editForm.data.$index]
            item.name = this.editForm.name
          })
          .catch(() => {})
      }
    },

    /**
     * 操作
     */
    handleFile(type, item) {
      if (type === 'preview') {
        var previewList = this.list.map(element => {
          element.url = element.filePath
          return element
        })
        this.$bus.emit('preview-image-bus', {
          index: item.$index,
          data: previewList
        })
      } else if (type === 'delete') {
        this.$confirm('您确定要删除该文件吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            crmFileDelete({
              id: item.row.fileId
            })
              .then(res => {
                this.list.splice(item.$index, 1)
                this.$message.success('操作成功')
              })
              .catch(() => {})
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '已取消操作'
            })
          })
      } else {
        this.editForm.data = item
        this.editForm.name = item.row.name
        this.editDialog = true
      }
    }
  }
}
</script>

<style scoped lang="scss">
.attachment {
  background: #fff;
  .attachment-body {
    position: relative;
    overflow: hidden;
  }
}

.el-table /deep/ thead th {
  background-color: #f5f5f5;
  font-weight: 400;
  font-size: 12px;
}

.p-contianer {
  position: relative;
  background-color: white;
  height: 44px;
  border: 1px solid #e6e6e6;
  border-top: none;

  .p-bar {
    float: right;
    margin: 5px 100px 0 0;
    font-size: 14px !important;
  }
}

.el-table::before {
  display: none;
}
</style>
