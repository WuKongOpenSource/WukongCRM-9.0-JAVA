<template>
  <div v-loading="loading">
    <div v-empty="list.length === 0">
      <div class="log-items">
        <task-cell
          v-for="(item, index) in list"
          :data="item"
          :data-index="index"
          :key="index"
          @on-handle="taskCellHandle"/>
        <div class="load">
          <el-button
            :loading="loadMoreLoading"
            type="text">{{ loadMoreLoading ? '加载更多' : '没有更多了' }}</el-button>
        </div>
      </div>
    </div>
    <!-- 详情 -->
    <div
      v-if="taskDetailShow"
      ref="taskShade"
      class="full-container">
      <particulars
        :id="taskID"
        :detail-index="detailIndex"
        class="d-view"
        @on-handle="detailHandle"
        @close="closeBtn"/>
    </div>
  </div>
</template>

<script>
import listTaskDetail from '@/views/OAManagement/task/mixins/listTaskDetail.js'
import TaskCell from '@/views/OAManagement/task/components/taskCell' // 任务

import { crmQueryTaskRelation } from '@/api/customermanagement/common'
import { getMaxIndex } from '@/utils'

export default {
  /** 任务 跟进记录*/
  name: 'TaskLog',
  components: {
    TaskCell,
    Particulars: () =>
      import('@/views/OAManagement/task/components/particulars')
  },
  mixins: [listTaskDetail],
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
      list: [] // 跟进记录列表
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
      crmQueryTaskRelation(params)
        .then(res => {
          res.data.list.forEach(item => {
            if (item.status == 5) {
              item.checked = true
            }
          })
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
    taskCellHandle(data) {
      if (data.type == 'view') {
        this.showDetailView(data.data.item, data.data.index, () => {
          this.$nextTick(() => {
            document.body.appendChild(this.$refs.taskShade)
            this.$refs.taskShade.style.zIndex = getMaxIndex()
            this.$refs.taskShade.addEventListener(
              'click',
              this.handleDocumentClick,
              false
            )
          })
        })
      }
    },
    // 点击显示详情
    handleDocumentClick(e) {
      e.stopPropagation()
      if (this.$refs.taskShade == e.target) {
        this.taskDetailShow = false
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

.d-view {
  position: fixed;
  width: 950px !important;
  top: 0px !important;
  bottom: 0px;
  left: auto !important;
  right: 0px !important;
}

.full-container {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  overflow: auto;
  margin: 0;
  background-color: rgba(0, 0, 0, 0.3);
}
</style>
