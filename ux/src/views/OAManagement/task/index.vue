<template>
  <div class="task oa-bgcolor">
    <div class="header">
      <el-button type="primary"
                 class="new-btn"
                 @click="newBtn">新建任务</el-button>
      <el-tabs v-model="activeName"
               @tab-click="handleClick">
        <el-tab-pane :label="item.label"
                     :name="item.key"
                     v-for="(item, index) in tabData"
                     :key="index">
          <my-task v-loading="loading"
                   :list="listData"
                   :listType="item.key"
                   :ref="item.key"
                   :subUserListData="subUserListData"
                   :loadMoreLoading="loadMoreLoading"
                   @selectChange="selectChange">
            <div class="search-input"
                 slot="searchInput">
              <el-input size="medium"
                        placeholder="搜索任务名称"
                        suffix-icon="el-icon-search"
                        v-model="searchValue"
                        @input="searchChange">
              </el-input>
            </div>
          </my-task>
        </el-tab-pane>
      </el-tabs>
    </div>
    <!-- 新增任务弹出框 newDialog-->
    <new-dialog :newDialogVisible="dialogVisible"
                :newLoading="newLoading"
                @handleClose="handleClose"
                @dialogVisibleSubmit="dialogVisibleSubmit">
    </new-dialog>
  </div>
</template>

<script>
import myTask from './components/myTask'
import newDialog from './components/newDialog'
import { taskListAPI, addTask } from '@/api/oamanagement/task'
import { usersList } from '@/api/common'

export default {
  components: {
    myTask,
    newDialog
  },
  data() {
    return {
      activeName: 'mytask',
      activePramas: { priority: '' },
      tabData: [
        { label: '我的任务', key: 'mytask' },
        { label: '我下属的任务', key: 'subtask' }
      ],
      listData: [],
      // 新建
      dialogVisible: false,
      newLoading: false,
      loading: true,
      // 输入框搜索
      searchValue: '',
      // 负责人
      subUserListData: [],
      // 页数
      pageNum: 1,
      loadText: '加载更多',
      loadMoreLoading: true,
      // 判断是否在请求数据
      isPost: false
    }
  },
  watch: {
    $route(to, from) {
      this.$router.go(0)
    }
  },
  beforeRouteUpdate(to, from, next) {
    if (to.query.routerKey == 1) {
      this.newBtn()
    }
    next()
  },
  created() {
    if (this.$route.query.routerKey == 1) {
      this.newBtn()
    }
    // 负责人
    this.subUserFun()
  },
  mounted() {
    // 分批次加载
    for (let dom of document.getElementsByClassName('list-box-container')) {
      dom.onscroll = () => {
        let scrollOff = dom.scrollTop + dom.clientHeight - dom.scrollHeight
        //滚动条到底部的条件
        if (Math.abs(scrollOff) < 10 && this.loadMoreLoading == true) {
          if (!this.isPost) {
            this.isPost = true
            this.pageNum++
            this.getList()
          } else {
            this.loadMoreLoading = false
          }
        }
      }
    }
    this.getList()
  },
  methods: {
    // 负责人
    subUserFun() {
      usersList({ pageType: 0 })
        .then(res => {
          this.subUserListData = res.data
          this.subUserListData.unshift({
            userId: '',
            realname: ' 全部'
          })
        })
        .catch(() => {})
    },
    handleClick() {
      this.activePramas = this.$refs[this.activeName][0].fromData
      this.refreshList()
    },
    newBtn() {
      this.dialogVisible = true
    },
    // 新建关闭
    handleClose(tip) {
      if (tip == 'shortcut') {
        this.$router.push('task')
      } else {
        if (this.$route.query.routerKey == 1) {
          this.$router.go(-1)
        } else {
          this.dialogVisible = false
        }
      }
    },
    // 新建提交
    dialogVisibleSubmit(val) {
      this.newLoading = true
      addTask(val)
        .then(res => {
          this.refreshList()
          this.handleClose('shortcut')
          this.$message.success('新建成功')
          this.newLoading = false
          this.dialogVisible = false
        })
        .catch(err => {
          this.newLoading = false
          this.dialogVisible = false
        })
    },
    selectChange(data) {
      let params = {
        status: data.data.status,
        type: data.data.type,
        priority: data.data.priority,
        date: data.data.date
      }
      if (data.type == 'subtask') {
        params.userId = data.data.subUser
      } else {
        params.userId = ''
      }
      this.activePramas = params

      this.refreshList()
    },
    refreshList() {
      this.pageNum = 1
      this.listData = []
      this.loadMoreLoading = true
      this.getList()
    },
    searchChange() {
      this.refreshList()
    },
    getList() {
      this.loading = true
      var params = this.activePramas
      params.limit = 15
      params.page = this.pageNum
      params.search = this.searchValue
      if (this.activeName == 'subtask') {
        params.mold = 1
      }
      taskListAPI(params)
        .then(res => {
          for (let item of res.data.list) {
            if (item.status == 5) {
              item.checked = true
            }
          }
          this.listData = this.listData.concat(res.data.list)

          if (res.data.list.length < 15) {
            this.loadText = '没有更多了'
            this.loadMoreLoading = false
          } else {
            this.loadText = '加载更多'
            this.loadMoreLoading = true
          }
          this.isPost = false
          this.loading = false
        })
        .catch(err => {
          this.isPost = false
          this.loading = false
        })
    }
  }
}
</script>

<style scoped lang="scss">
@import '../styles/tabs.scss';

.task {
  .header {
    position: relative;
    height: 100%;
    .new-btn {
      position: absolute;
      top: 10px;
      right: 40px;
      z-index: 9;
    }
    .el-tabs {
      height: 100%;
      display: flex;
      flex-direction: column;
    }
    .el-tabs /deep/ .el-tabs__content {
      padding: 0 30px;
      flex: 1;
      margin-bottom: 20px;
      .el-tab-pane {
        height: 100%;
        display: flex;
        flex-direction: column;
        overflow-y: hidden;
      }
    }
    .search-input {
      .el-input {
        width: 328px;
      }
    }
  }
}
</style>
