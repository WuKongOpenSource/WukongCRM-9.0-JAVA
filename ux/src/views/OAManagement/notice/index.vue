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
        <div class="content">
          <div class="list-box">
            <div class="list"
                 v-for="(item, index) in listData"
                 :key="index">
              <div class="header">
                <div v-photo="item"
                     v-lazy:background-image="$options.filters.filterUserLazyImg(item.img)"
                     class="div-photo"></div>
                <div class="name-time">
                  <p class="name">{{item.realname}}</p>
                  <p class="time">{{item.createTime | moment("YYYY-MM-DD HH:mm")}}</p>
                </div>
              </div>
              <div class="title"
                   @click="rowFun(item)">{{item.title}}</div>
              <div class="item-content"
                   v-if="item.preShow">{{item.content}}</div>
              <div class="item-content"
                   v-else>{{item.contentSub}}</div>
              <div v-if="item.contentSub.length < item.content.length"
                   class="load-more">
                <span v-if="!item.loadMore"
                      @click="loadMoreBtn(item)">展开全文</span>
                <span v-else
                      @click="item.loadMore = false, item.preShow = false">收起全文</span>
              </div>
            </div>
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
// API
import { noticeList } from '@/api/oamanagement/notice'

export default {
  components: {
    VDetails,
    newDialog
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
    let _this = this
    document.getElementsByClassName('content')[0].onscroll = function() {
      let doms = document.getElementsByClassName('content')[0]
      var scrollTop = doms.scrollTop
      var windowHeight = doms.clientHeight
      var scrollHeight = doms.scrollHeight //滚动条到底部的条件
      if (scrollTop + windowHeight == scrollHeight) {
        _this.loadMoreLoading = true
        if (_this.isPost) {
          _this.pageNum++
          _this.noticeDataFun(_this.optionsValue, _this.pageNum)
        } else {
          _this.loadMoreLoading = false
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
            this.isPost = false
          } else {
            this.loadText = '加载更多'
            this.isPost = true
          }
          this.loading = false
          this.loadMoreLoading = false
        })
        .catch(err => {
          this.loading = false
          this.loadMoreLoading = false
        })
    },
    // 点击显示详情
    rowFun(val) {
      this.titleList = val
      this.dialog = true
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
    },
    loadMoreBtn(val) {
      this.$set(val, 'preShow', true)
      this.$set(val, 'loadMore', true)
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
      .list {
        margin-bottom: 30px;
        padding-bottom: 30px;
        border-bottom: 1px solid #e6e6e6;
        .header {
          margin-bottom: 15px;
          .div-photo {
            width: 35px;
            height: 35px;
            border-radius: 17.5px;
            margin-right: 10px;
          }
          .name-time {
            display: inline-block;
            .time {
              color: #999;
              margin-top: 5px;
              font-size: 12px;
            }
          }
        }
        .title {
          cursor: pointer;
          display: inline-block;
        }
        .item-content {
          margin-top: 10px;
          color: #999;
          font-size: 12px;
          line-height: 18px;
          white-space: pre-wrap;
          word-wrap: break-word;
          background-color: #f0f7ff;
          padding: 15px;
          border-radius: 3px;
          color: #333;
          letter-spacing: 0.5px;
        }
        .load-more {
          text-align: left;
          margin-top: 15px;
          span {
            cursor: pointer;
            font-size: 13px;
            color: #8ab7f5;
          }
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
