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
    <examine-section class="list-box"
                     :id="'examine-list-box' + this.by"
                     :list="list"
                     @handle="searchBtn">
      <p slot="load"
         class="load">
        <el-button type="text"
                   :loading="loadMoreLoading">{{loadMoreLoading ? '加载更多' : '没有更多了'}}</el-button>
      </p>
    </examine-section>
  </div>
</template>

<script>
import {
  oaExamineMyCreateIndex,
  oaExamineMyExamineIndex,
  oaExamineDelete
} from '@/api/oamanagement/examine'
import { formatTimeToTimestamp } from '@/utils'
import ExamineSection from './examineSection'

export default {
  components: {
    ExamineSection
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
      page: 1
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
    let dom = document.getElementById('examine-list-box' + this.by)
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
</style>
