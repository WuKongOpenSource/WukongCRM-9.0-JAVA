<template>
  <div class="content"
       v-loading="loading">
    <div class="select-box">
      <div class="select-group">
        <label>审核状态</label>
        <el-select v-model="checkStatus"
                   size="small"
                   placeholder="请选择"
                   @change="searchBtn">
          <el-option v-for="item in statusOptions"
                     :key="item.key"
                     :label="item.label"
                     :value="item.key">
          </el-option>
        </el-select>
      </div>
      <div class="select-group">
        <label>发起时间</label>
        <el-date-picker v-model="betweenTime"
                        type="daterange"
                        style="padding: 0px 10px;width: 250px;"
                        range-separator="-"
                        value-format="yyyy-MM-dd"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        @change="searchBtn">
        </el-date-picker>
      </div>
    </div>
    <div class="list-box"
         :id="'examine-list-box' + this.by">
      <examine-cell v-for="(item, index) in list"
                    :key="index"
                    :data="item"
                    @on-handle="examineCellHandle"></examine-cell>
      <p class="load">
        <el-button type="text"
                   :loading="loadMoreLoading">{{loadMoreLoading ? '加载更多' : '没有更多了'}}</el-button>
      </p>
    </div>
    <examine-detail v-if="showDview"
                    :id="rowID"
                    class="d-view"
                    @on-examine-handle="searchBtn"
                    @hide-view="showDview=false">
    </examine-detail>
    <c-r-m-all-detail :visible.sync="showRelatedDetail"
                      :crmType="relatedCRMType"
                      :listenerIDs="['workbench-main-container']"
                      :noListenerIDs="['examine-list-box']"
                      :id="relatedID"></c-r-m-all-detail>
    <examine-handle :show="showExamineHandle"
                    @close="showExamineHandle = false"
                    @save="searchBtn"
                    :id="rowID"
                    :recordId="rowData.examineRecordId"
                    :detail="rowData"
                    examineType="oa_examine"
                    status="4"></examine-handle>
  </div>
</template>

<script>
import {
  oaExamineMyCreateIndex,
  oaExamineMyExamineIndex,
  oaExamineDelete
} from '@/api/oamanagement/examine'
import { formatTimeToTimestamp } from '@/utils'
import ExamineCell from './examineCell'
import ExamineDetail from './examineDetail'
import CRMAllDetail from '@/views/customermanagement/components/CRMAllDetail'
import ExamineHandle from '@/components/Examine/ExamineHandle' // 审批操作理由

export default {
  components: {
    ExamineCell,
    ExamineDetail,
    CRMAllDetail,
    ExamineHandle
  },
  data() {
    return {
      loading: false,
      loadMoreLoading: true,
      checkStatus: this.by == 'examine' ? '1' : '',
      betweenTime: [],
      list: [],
      // 判断是否发请求
      isPost: false,
      page: 1,
      /** 控制详情展示 */
      rowID: '', // 行信息
      rowData: {}, // 行全部信息
      showDview: false,
      // 相关详情的查看
      relatedID: '',
      relatedCRMType: '',
      showRelatedDetail: false,
      // 撤回操作
      showExamineHandle: false
    }
  },
  watch: {
    categoryId: function(params) {
      this.page = 1
      this.list = []
      this.getList()
    }
  },
  props: {
    // 类型 my我发起的,examine我审批的
    by: String,
    // 审批类型ID
    categoryId: [String, Number]
  },
  computed: {
    statusOptions() {
      if (this.by == 'examine') {
        return [
          { label: '待我审批的', key: '1' },
          { label: '我已审批的', key: '2' }
        ] // 1待我审批的、2我已审批的
      } else {
        return [
          { label: '全部', key: '' },
          { label: '待审', key: '0' },
          { label: '审批中', key: '3' },
          { label: '通过', key: '1' },
          { label: '失败', key: '2' },
          { label: '撤回', key: '4' }
        ]
      }
    }
  },
  mounted() {
    // 分批次加载
    let self = this
    let item = document.getElementById('examine-list-box' + this.by)
    item.onscroll = function() {
      let scrollTop = item.scrollTop
      let windowHeight = item.clientHeight
      let scrollHeight = item.scrollHeight //滚动条到底部的条件

      if (
        scrollTop + windowHeight == scrollHeight &&
        self.loadMoreLoading == true
      ) {
        if (!self.isPost) {
          self.isPost = true
          self.page++
          self.getList()
        } else {
          self.loadMoreLoading = false
        }
      }
    }

    this.getList()
  },
  methods: {
    /** 获取列表数据 */
    getList() {
      this.loading = true
      let params = {
        page: this.page,
        limit: 15,
        categoryId: this.categoryId
      }
      if (this.by == 'examine') {
        params.status = this.checkStatus
      } else {
        params.checkStatus = this.checkStatus
      }

      if (this.betweenTime && this.betweenTime.length > 0) {
        params.startTime = this.betweenTime[0]
        params.endTime = this.betweenTime[1]
      }

      let request = {
        my: oaExamineMyCreateIndex,
        examine: oaExamineMyExamineIndex
      }[this.by]
      request(params)
        .then(res => {
          this.list = this.list.concat(res.data.list)
          if (res.data.list.length < 15) {
            this.loadMoreLoading = false
          } else {
            this.loadMoreLoading = true
          }
          this.isPost = false
          this.loading = false
        })
        .catch(() => {
          this.isPost = false
          this.loading = false
        })
    },
    // 搜索
    searchBtn() {
      this.list = []
      this.page = 1
      this.getList()
    },
    // 重置
    resetBtn() {
      this.checkStatus = 'all'
      this.betweenTime = []
      this.$emit('reset')
    },
    examineCellHandle(data) {
      // 编辑
      if (data.type == 'edit') {
        this.$emit('edit', data.data.item)
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
              this.searchBtn()
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
        this.rowData = data.data.item
        this.showExamineHandle = true
        // 详情
      } else if (data.type == 'view') {
        this.showRelatedDetail = false
        this.rowID = data.data.item.examineId
        this.showDview = true
      } else if (data.type == 'related-detail') {
        this.showDview = false
        this.relatedID = data.data.item[data.data.type + 'Id']
        this.relatedCRMType = data.data.type
        this.showRelatedDetail = true
      }
    }
  }
}
</script>

<style scoped lang="scss">
@import '../../styles/content.scss';
.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  .select-box {
    margin: 10px 0 20px;
    .select-group {
      margin-right: 20px;
      display: inline-block;
      label {
        @include color9;
        margin-right: 10px;
      }
      .el-select {
        width: 116px;
        height: 30px;
      }
    }
    .btn-box {
      float: right;
      margin-right: 10px;
    }
  }
  .list-box {
    overflow: auto;
    padding-right: 30px;
  }
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

.d-view {
  position: fixed;
  width: 926px;
  top: 60px;
  bottom: 0px;
  right: 0px;
}
</style>
