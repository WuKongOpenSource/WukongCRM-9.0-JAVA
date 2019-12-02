<template>
  <div v-loading="loading">
    <div v-empty="list.length === 0">
      <div class="log-items">
        <examine-cell
          v-for="(item, index) in list"
          :key="index"
          :data="item"
          @on-handle="examineCellHandle"/>
        <div class="load">
          <el-button
            :loading="loadMoreLoading"
            type="text">{{ loadMoreLoading ? '加载更多' : '没有更多了' }}</el-button>
        </div>
      </div>
    </div>
    <examine-handle
      :show="showExamineHandle"
      :id="rowID"
      examine-type="oa_examine"
      status="2"
      @close="showExamineHandle = false"
      @save="refreshList"/>
    <examine-create-view
      v-if="isCreate"
      :category-id="createInfo.categoryId"
      :category-title="createInfo.title"
      :type="createInfo.type"
      :action="createAction"
      @save-success="refreshList"
      @hiden-view="isCreate=false"/>
    <c-r-m-full-screen-detail
      :visible.sync="showFullDetail"
      :crm-type="detailCRMType"
      :id="rowID"/>
  </div>
</template>

<script>
import ExamineCell from '@/views/OAManagement/examine/components/examineCell' // 跟进记录
import ExamineCreateView from '@/views/OAManagement/examine/components/examineCreateView'
import { crmQueryExamineRelation } from '@/api/customermanagement/common'
import { oaExamineDelete } from '@/api/oamanagement/examine'

export default {
  /** 审批 跟进记录*/
  name: 'ExamineLog',
  components: {
    ExamineCell,
    CRMFullScreenDetail: () =>
      import('@/views/customermanagement/components/CRMFullScreenDetail.vue'),
    ExamineHandle: () => import('@/components/Examine/ExamineHandle'),
    ExamineCreateView
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
  data() {
    return {
      loading: false,
      loadMoreLoading: true,
      isPost: false,
      page: 1,
      list: [], // 跟进记录列表
      rowID: '', // 行信息
      // 撤回操作
      showExamineHandle: false,
      isCreate: false, // 是编辑
      createAction: { type: 'save' },
      createInfo: {}, // 编辑所需要的id 标题名信息
      // 详情
      showFullDetail: false,
      detailCRMType: ''
    }
  },
  computed: {},
  watch: {
    id: function(val) {
      this.refreshList()
    }
  },
  mounted() {
    // 分批次加载
    const dom = document.getElementById('follow-log-content')
    dom.onscroll = () => {
      const scrollOff = dom.scrollTop + dom.clientHeight - dom.scrollHeight
      // 滚动条到底部的条件
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
      const params = { page: this.page, limit: 10 }
      params[this.crmType + 'Ids'] = this.id
      crmQueryExamineRelation(params)
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
    examineCellHandle(data) {
      // 编辑
      if (data.type == 'edit') {
        data.data.item.title = data.data.item.categoryName
        this.createInfo = data.data.item
        this.createAction = {
          type: 'update',
          id: data.data.item.examineId,
          data: data.data.item
        }
        this.isCreate = true
        // 删除
      } else if (data.type == 'delete') {
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            oaExamineDelete({
              examineId: data.data.item.examineId
            }).then(res => {
              this.refreshList()
              this.$message({
                type: 'success',
                message: '删除成功!'
              })
            })
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '已取消删除'
            })
          })
        // 撤回
      } else if (data.type == 'withdraw') {
        this.rowID = data.data.item.examineId
        this.showExamineHandle = true
        // 详情
      } else if (data.type == 'view') {
        this.detailCRMType = 'examine'
        this.rowID = data.data.item.examineId
        this.showFullDetail = true
      } else if (data.type == 'related-detail') {
        this.rowID = data.data.item[data.data.type + 'Id']
        this.detailCRMType = data.data.type
        this.showFullDetail = true
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.log-items {
  min-height: 400px;
  position: relative;
  padding: 10px 20px;
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
</style>
