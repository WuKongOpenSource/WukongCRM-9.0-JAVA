<template>
  <div class="notice oa-bgcolor"
       v-loading="loading">
    <el-button type="primary"
               class="new-btn"
               v-if="newStatus"
               @click="newBtn">新建公告</el-button>
    <el-tabs v-model="activeName">
      <el-tab-pane label="公告"
                   name="first">
        <div class="text-top">
          <label class="text">公告状态</label>
          <el-select v-model="optionsValue"
                     @change="selectChange"
                     placeholder="请选择"
                     size="small">
            <el-option v-for="item in options"
                       :key="item.value"
                       :label="item.label"
                       :value="item.value">
            </el-option>
          </el-select>
        </div>
        <div class="content" id="notice-list-box">
          <div class="list-box">
            <notice-cell v-for="(item, index) in listData"
                         :key="index"
                         :data="item"
                         :cellIndex="index"
                         @handle="noticeHandle">
            </notice-cell>
            <p class="load">
              <el-button type="text"
                         :loading="loadMoreLoading">{{loadText}}</el-button>
            </p>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
    <!-- 详情 -->
    <v-details v-if="dialog"
               :titleList="titleList"
               @editSubmit="editSubmit"
               @deleteFun="deleteFun"
               @close="close">
    </v-details>
    <!-- 新建 -->
    <new-dialog v-if="showNewDialog"
                @onSubmit="onSubmit"
                @close="newClose">
    </new-dialog>
  </div>
</template>

<script>
import VDetails from './details'
import newDialog from './newDialog'
import NoticeCell from './noticeCell'
// API
import { noticeList } from '@/api/oamanagement/notice'

export default {
  components: {
    VDetails,
    newDialog,
    NoticeCell
  },
  data() {
    return {
      activeName: 'first',
      // 公示下拉框
      options: [
        { value: '1', label: '公示中' },
        { value: '2', label: '已结束' }
      ],
      optionsValue: '1',
      // 公告列表
      listData: [],
      // 详情
      dialog: false,
      titleList: {},
      // 新建
      showNewDialog: false,
      loading: true,
      // 页数
      pageNum: 1,
      loadText: '加载更多',
      loadMoreLoading: true,
      // 判断是否还有数据
      isPost: true,
      newStatus: false
    }
  },
  watch: {
    $route(to, from) {
      this.$router.go(0)
    }
  },
  mounted() {
    this.noticeDataFun(1, this.pageNum)
    // 分批次加载
    document.getElementsByClassName('content')[0].onscroll = () => {
      let dom = document.getElementsByClassName('content')[0]
      let scrollOff = dom.scrollTop + dom.clientHeight - dom.scrollHeight
      //滚动条到底部的条件
      if (Math.abs(scrollOff) < 10 && this.loadMoreLoading == true) {
        if (!this.isPost) {
          this.isPost = true
          this.pageNum++
          this.noticeDataFun(this.optionsValue, this.pageNum)
        } else {
          this.loadMoreLoading = false
        }
      }
    }
  },
  methods: {
    noticeDataFun(type, num) {
      let _this = this
      noticeList({
        type: type,
        page: num,
        limit: 15
      })
        .then(res => {
          res.data.isSave == 1
            ? (this.newStatus = true)
            : (this.newStatus = false)
          for (let item of res.data.list) {
            item.contentSub = item.content.substring(0, 150)
          }
          this.listData = this.listData.concat(res.data.list)
          if (res.data.list.length == 0 || res.data.list.length != 15) {
            this.loadText = '没有更多了'
            this.loadMoreLoading = false
          } else {
            this.loadText = '加载更多'
            this.loadMoreLoading = true
          }
          this.loading = false
          this.isPost = false
        })
        .catch(err => {
          this.loadText = ''
          this.loading = false
          this.isPost = false
        })
    },
    // 点击显示详情
    noticeHandle(data) {
      if (data.type == 'detail') {
        this.titleList = data.value
        this.dialog = true
      }
    },
    close() {
      this.dialog = false
    },
    // 删除
    deleteFun() {
      for (let i in this.listData) {
        if (this.listData[i].announcementId == this.titleList.announcementId) {
          this.listData.splice(i, 1)
        }
      }
      this.close()
    },
    // 新建
    newBtn() {
      this.showNewDialog = true
    },
    // 新建关闭
    newClose() {
      this.showNewDialog = false
    },
    // 新建提交按钮
    onSubmit(data) {
      this.selectChange(this.optionsValue)
      this.newClose()
    },
    // 编辑确定
    editSubmit(val) {
      this.selectChange(this.optionsValue)
      this.close()
    },
    // 筛选状态
    selectChange(type) {
      this.loading = true
      this.listData = []
      this.pageNum = 1
      this.noticeDataFun(type, this.pageNum)
    }
  }
}
</script>

<style scoped lang="scss">
@import '../styles/tabs.scss';
.notice {
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  .new-btn {
    position: absolute;
    top: 10px;
    right: 40px;
    z-index: 999;
  }
  .text-top {
    padding: 5px 0 15px;
    .text {
      margin-right: 10px;
      color: #999;
    }
  }
  .content {
    .list-box {
      margin-top: 20px;
      padding-right: 20px;
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
    }
  }
}
.notice /deep/ .el-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  .el-tab-pane,
  .el-tabs__content {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
  .el-tab-pane {
    min-height: 0;
  }
  .el-tabs__content {
    padding: 0 30px;
    margin-bottom: 20px;
    .content {
      flex: 1;
      overflow: auto;
      padding-right: 30px;
      margin-right: -30px;
    }
  }
}
</style>
