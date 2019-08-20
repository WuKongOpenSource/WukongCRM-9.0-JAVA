<template>
  <div v-loading="loading">
    <div v-empty="list.length === 0">
      <div class="log-items">
        <follow-schedule-cell v-for="(item, index) in list"
                              :key="index"
                              :data="item"
                              @on-handle="examineCellHandle"></follow-schedule-cell>
        <div class="load">
          <el-button type="text"
                     :loading="loadMoreLoading">{{loadMoreLoading ? '加载更多' : '没有更多了'}}</el-button>
        </div>
      </div>
    </div>
    <c-r-m-full-screen-detail :visible.sync="showFullDetail"
                              :crmType="detailCRMType"
                              :id="rowID"></c-r-m-full-screen-detail>
    <!-- 编辑日程 -->
    <create-schedule v-if="showDialog"
                     :text="newText"
                     :formData="formData"
                     :appendToBody="true"
                     @onSubmit="onSubmit"
                     @closeDialog="showDialog=false">
    </create-schedule>
  </div>
</template>

<script>
import { crmQueryEventRelation } from '@/api/customermanagement/common'
import FollowScheduleCell from './components/FollowScheduleCell'
import { scheduleDelete } from '@/api/oamanagement/schedule'
import CreateSchedule from '@/views/OAManagement/schedule/components/createSchedule'
import { formatTimeToTimestamp } from '@/utils'

export default {
  /** 日程 跟进记录*/
  name: 'schedule-log',
  components: {
    FollowScheduleCell,
    CRMFullScreenDetail: () =>
      import('@/views/customermanagement/components/CRMFullScreenDetail.vue'),
    CreateSchedule
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
      // 详情
      showFullDetail: false,
      detailCRMType: '',
      rowID: '', // 行信息
      showDialog: false,
      formData: {
        checkList: []
      },
      newtext: ''
    }
  },
  computed: {},
  mounted() {
    // 分批次加载
    let self = this
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
      crmQueryEventRelation(params)
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
        this.newText = '编辑日程'
        let val = data.data.item
        val.startTime = val.startTime
        val.endTime = val.endTime
        val.ownerUserIds = []
        for (let k of val.ownerList) {
          val.ownerUserIds.push(k.userId)
        }
        this.formData = val
        this.showDialog = true
      } else if (data.type == 'delete') {
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            scheduleDelete({ eventId: data.data.item.eventId }).then(res => {
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
      } else if (data.type == 'related-detail') {
        this.rowID = data.data.item[data.data.type + 'Id']
        this.detailCRMType = data.data.type
        this.showFullDetail = true
      }
    },
    // 提交
    onSubmit(data, file) {
      this.refreshList()
      if (this.newText == '创建日程') {
        this.$message.success('新建成功')
        this.showDialog = false
      } else {
        this.$message.success('编辑成功')
        this.showDialog = false
      }
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
</style>
