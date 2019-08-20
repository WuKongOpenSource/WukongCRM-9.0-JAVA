<template>
  <div v-loading="loading">
    <div v-empty="list.length === 0">
      <div class="log-items">
        <journal-cell v-for="(item, index) in list"
                      :key="index"
                      class="list-cell"
                      :data="item"
                      @on-handle="jourecallCellHandle"></journal-cell>
        <div class="load">
          <el-button type="text"
                     :loading="loadMoreLoading">{{loadMoreLoading ? '加载更多' : '没有更多了'}}</el-button>
        </div>
      </div>
    </div>
    <c-r-m-full-screen-detail :visible.sync="showFullDetail"
                              :crmType="detailCRMType"
                              :id="rowID"></c-r-m-full-screen-detail>
    <new-dialog v-if="showNewDialog"
                :formData="formData"
                :dialogTitle="dialogTitle"
                :imgFileList="imgFileList"
                :accessoryFileList="accessoryFileList"
                :newLoading="newLoading"
                @close="showNewDialog=false"
                @submitBtn="submitBtn">
    </new-dialog>
  </div>
</template>

<script>
// API
import { journalDelete, journalEdit } from '@/api/oamanagement/journal'
import JournalCell from '@/views/OAManagement/journal/journalCell' // 办公日志
import { crmQueryLogRelation } from '@/api/customermanagement/common'
import { formatTimeToTimestamp } from '@/utils'

export default {
  /** 日志 跟进记录*/
  name: 'journal-log',
  components: {
    JournalCell,
    CRMFullScreenDetail: () =>
      import('@/views/customermanagement/components/CRMFullScreenDetail.vue'),
    NewDialog: () => import('@/views/OAManagement/journal/newDialog')
  },
  props: {
    /** 模块ID */
    id: [String, Number],
    /** 没有值就是全部类型 有值就是当个类型 */
    crmType: {
      type: String,
      default: ''
    }
  },
  watch: {
    id: function(val) {
      this.refreshList()
    }
  },
  data() {
    return {
      loading: false,
      loadMoreLoading: true,
      isPost: false,
      page: 1,
      list: [], // 跟进记录列表
      rowID: '', // 行信息
      // 详情
      showFullDetail: false,
      detailCRMType: '',
      // 编辑
      // 显示新建页面
      showNewDialog: false,
      // 新建数据
      formData: {},
      // 弹出框标题
      dialogTitle: '',
      // 图片数组
      imgFileList: [],
      // 附件数组
      accessoryFileList: [],
      newLoading: false
    }
  },
  computed: {},
  mounted() {
    // 分批次加载
    let dom = document.getElementById('follow-log-content')
    dom.onscroll = () => {
      let scrollOff = dom.scrollTop + dom.clientHeight - dom.scrollHeight
      //滚动条到底部的条件
      if (Math.abs(scrollOff) < 10 && this.loadMoreLoading == true) {
        if (!this.isPost) {
          this.isPost = true
          this.page++
          this.getList()
        } else {
          this.loadMoreLoading = false
        }
      }
    }

    this.getList()
  },
  activated: function() {},
  deactivated: function() {},
  methods: {
    getList() {
      this.loading = true
      let params = { page: this.page, limit: 10 }
      params[this.crmType + 'Ids'] = this.id
      crmQueryLogRelation(params)
        .then(res => {
          this.list = this.list.concat(res.data.list)
          if (res.data.list.length < 10) {
            this.loadMoreLoading = false
          } else {
            this.loadMoreLoading = true
          }
          this.loading = false
          this.isPost = false
        })
        .catch(() => {
          this.isPost = false
          this.loading = false
        })
    },
    refreshList() {
      this.page = 1
      this.list = []
      this.getList()
    },
    jourecallCellHandle(data) {
      // 编辑按钮
      if (data.type == 'edit') {
        let val = data.data.item
        this.showNewDialog = true
        this.dialogTitle = '编辑日志'
        this.formData = val
        this.imgFileList = []
        if (val.imgList) {
          for (let item of val.imgList) {
            item.url = item.filePath
            this.imgFileList.push(item)
          }
        }
        // 附件
        this.accessoryFileList = []
        if (val.fileList) {
          for (let item of val.fileList) {
            item.url = item.filePath
            this.accessoryFileList.push(item)
          }
        }
        // 员工部门赋值
        this.formData.depData = val.sendStructList ? val.sendStructList : []
        this.formData.sentWhoList = val.sendUserList ? val.sendUserList : []
        // 删除按钮
      } else if (data.type == 'delete') {
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            journalDelete({ logId: data.data.item.logId }).then(res => {
              this.$message({
                type: 'success',
                message: '删除成功!'
              })
              for (let i in this.list) {
                if (this.list[i].logId == data.data.item.logId) {
                  this.list.splice(i, 1)
                  break
                }
              }
            })
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '已取消删除'
            })
          })
        // 相关详情
      } else if (data.type == 'related-detail') {
        this.rowID = data.data.item.key
        this.detailCRMType = data.data.type
        this.showFullDetail = true
      }
    },
    // 新建提交
    submitBtn(key, file, img, relevanceAll) {
      this.newLoading = true
      let imgList = []
      let fileList = []
      // 获取部门
      let dep = []
      if (this.formData.depData) {
        for (let j of this.formData.depData) {
          dep.push(j.id)
        }
      }
      // 获取员工
      let staff = []
      if (this.formData.sentWhoList) {
        for (let h of this.formData.sentWhoList) {
          staff.push(h.id)
        }
      }
      for (let item of this.imgFileList) {
        imgList.push(item.fileId)
      }
      for (let item of this.accessoryFileList) {
        fileList.push(item.fileId)
      }
      let pramas = {
        id: this.formData.logId,
        categoryId: key,
        content: this.formData.content,
        tomorrow: this.formData.tomorrow,
        question: this.formData.question,
        file: fileList.concat(imgList),
        sendUserIds: staff,
        sendStructureIds: dep,
        customerIds: relevanceAll.customerIds,
        contactsIds: relevanceAll.contactsIds,
        businessIds: relevanceAll.businessIds,
        contractIds: relevanceAll.contractIds
      }
      journalEdit(pramas)
        .then(res => {
          this.refreshList()
          this.showNewDialog = false
          this.$message.success('编辑成功')
          this.newLoading = false
        })
        .catch(err => {
          this.newLoading = false
          this.$message.error('编辑失败')
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.log-items {
  min-height: 400px;
  position: relative;
}

.load {
  color: #999;
  font-size: 13px;
  margin: 0 auto 15px;
  text-align: center;
  .el-button,
  .el-button:focus {
    color: #ccc;
    cursor: auto;
  }
}

.list-cell {
  margin-bottom: 20px;
  border-radius: 4px;
}
</style>
