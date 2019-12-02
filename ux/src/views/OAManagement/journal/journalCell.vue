<template>
  <div
    :id="'journal-cell' + logIndex"
    class="list">
    <div class="list-content">
      <div class="header">
        <div
          v-photo="data.createUser"
          v-lazy:background-image="$options.filters.filterUserLazyImg(data.createUser.img)"
          :key="data.createUser.img"
          class="div-photo head-img header-circle"/>
        <div class="row">
          <p class="row-title">
            <span class="name">{{ data.createUser.realname }}</span>
            <span
              v-if="showWorkbench"
              class="item-content">{{ data.actionContent }}</span>
            <span
              v-else
              :style="{'color': data.isRead == 0 ? '#3E84E9' : '#ccc'}"
              class="read">{{ data.isRead == 0 ? '未读' : '已读' }}</span>
          </p>
          <span class="time">{{ data.createTime | moment("YYYY-MM-DD HH:mm") }}</span>
          <el-tooltip
            :disabled="!(data.sendUserList.length > 0 || data.sendDeptList.length > 0)"
            placement="bottom"
            effect="light"
            popper-class="tooltip-change-border">
            <div slot="content">
              <div class="members-dep-title">
                <!-- hover员工 -->
                <span v-if="data.sendUserList">
                  <!-- 如果没有部门而且员工最后一个 - 不显示逗号 -->
                  <span
                    v-for="(k, i) in data.sendUserList"
                    :key="i">
                    {{ data.sendDeptList.length == 0 && i == data.sendUserList.length-1 ? k.realname : k.realname + "，" }}
                  </span>
                </span>
                <!-- hover部门 -->
                <span
                  v-for="(dep, depIndex) in data.sendDeptList"
                  v-show="data.sendDeptList.legnth != 0"
                  :key="depIndex">
                  {{ depIndex == data.sendDeptList.length-1 ? dep.name : dep.name+"，" }}
                </span>
              </div>
            </div>
            <p
              class="row-title"
              style="display: inline-block;">
              <span v-if="data.sendDeptList">{{ data.sendDeptList.length }} 个部门，</span>
              <span v-if="data.sendUserList">{{ data.sendUserList.length }}个同事</span>
            </p>
          </el-tooltip>
        </div>
        <div
          v-if="!showWorkbench && (data.permission && (data.permission.isUpdate || data.permission.isDelete))"
          class="rt-setting">
          <el-dropdown
            trigger="click"
            @command="handleCommand">
            <i
              style="color:#CDCDCD; cursor: pointer;"
              class="el-icon-arrow-down el-icon-more"/>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item
                v-if="data.permission.isUpdate"
                command="edit">编辑</el-dropdown-item>
              <el-dropdown-item
                v-if="data.permission.isDelete"
                command="delete">删除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
      <div class="text">
        <p
          v-if="data.content"
          class="row">
        <span class="title">{{ data.categoryId == 1 ? "今日工作内容" : data.categoryId == 2 ? "本周工作内容" : "本月工作内容" }}：</span>{{ data.content }}</p>
        <p
          v-if="data.tomorrow"
          class="row">
        <span class="title">{{ data.categoryId == 1 ? "明日工作内容" : data.categoryId == 2 ? "下周工作内容" : "下月工作内容" }}：</span>{{ data.tomorrow }}</p>
        <p
          v-if="data.question"
          class="row">
        <span class="title">遇到的问题：</span>{{ data.question }}</p>
      </div>
      <div class="accessory">
        <div
          v-if="data.img.length != 0"
          class="upload-img-box">
          <div
            v-for="(imgItem, k) in data.img"
            :key="k"
            class="img-list"
            @click="imgZoom(data.img, k)">
            <img
              v-lazy="imgItem.filePath"
              :key="imgItem.filePath">
          </div>
        </div>
        <div
          v-if="data.file.length != 0"
          class="accessory-box">
          <file-cell
            v-for="(file, fileIndex) in data.file"
            :key="fileIndex"
            :data="file"
            :cell-index="fileIndex"/>
        </div>
      </div>
      <!-- 关联业务 -->
      <related-business
        v-if="allDataShow"
        :margin-left="'0'"
        :alterable="false"
        :all-data="allData"
        @checkRelatedDetail="checkRelatedDetail"/>
      <!-- 评论 -->
      <div
        v-if="replyList && replyList.length != 0"
        class="discuss">
        <div class="border"/>
        <div
          v-for="(discussItem, k) in replyList"
          :key="k"
          class="discuss-list">
          <div
            v-photo="discussItem.user"
            v-lazy:background-image="$options.filters.filterUserLazyImg(discussItem.user.img)"
            :key="discussItem.user.img"
            class="div-photo head-img header-circle"/>
          <span class="name">{{ discussItem.user.realname }}</span>
          <span class="time">{{ discussItem.createTime }}</span>
          <div class="rt">
            <span @click="discussDelete(discussItem, replyList, k)">删除</span>
            <span @click="discussBtn(discussItem, -1)">回复</span>
          </div>

          <p class="reply-title">
            <span v-html="emoji(discussItem.content)"/>
          </p>

          <!-- <p class="discuss-content"
             v-html="emoji(discussItem.replyContent)"></p> -->

          <div
            v-if="discussItem.childCommentList && discussItem.childCommentList.length > 0"
            class="children-reply">
            <div
              v-for="(childDiscussItem, k) in discussItem.childCommentList"
              :key="k"
              class="discuss-list">
              <div
                v-photo="childDiscussItem.user"
                v-lazy:background-image="$options.filters.filterUserLazyImg(childDiscussItem.user.img)"
                :key="childDiscussItem.user.img"
                class="div-photo head-img header-circle"/>
              <span class="name">{{ childDiscussItem.user.realname }}</span>
              <span class="time">{{ childDiscussItem.createTime }}</span>
              <div class="rt">
                <span @click="discussDelete(childDiscussItem, discussItem.childCommentList, k)">删除</span>
                <span @click="discussBtn(discussItem, k)">回复</span>
              </div>
              <p class="reply-title">
                <template>
                  <span>回复</span>
                  <span
                    v-if="childDiscussItem.replyUser"
                    class="reply">@{{ childDiscussItem.replyUser.realname }}：</span>
                </template>
                <span v-html="emoji(childDiscussItem.content)"/>
              </p>
            </div>
          </div>

          <!-- 评论 -- 回复  -->
          <div
            v-if="discussItem.show"
            class="comment-box">
            <el-input
              :rows="2"
              v-model="childCommentsTextarea"
              type="textarea"
              placeholder="请输入内容"
              @blur="blurFun"/>
            <div class="btn-group">
              <el-popover
                v-model="childCommentsPopover"
                placement="top"
                width="400"
                trigger="click">
                <!-- 表情 -->
                <emoji @select="childSelectEmoji"/>
                <img
                  slot="reference"
                  src="@/assets/img/smiling_face.png"
                  class="smiling-img">
              </el-popover>
              <div class="btn-box">
                <el-button
                  :loading="contentLoading"
                  type="primary"
                  @click="childCommentSubmit()">回复</el-button>
                <el-button @click="discussItem.show= false">取消</el-button>
              </div>
            </div>
          </div>
          <div class="border"/>
        </div>
      </div>
    </div>
    <div class="footer">
      <el-button
        type="primary"
        icon="el-icon-chat-line-round"
        @click="commentBtn(data)">回复</el-button>
        <!-- <img @click="commentBtn(item)" class="comment" src="@/assets/img/journal_comment.png"> -->
    </div>
    <!-- 底部评论 -->
    <div
      v-if="data.showComment"
      class="comment-box">
      <el-input
        v-model="commentsTextarea"
        :rows="3"
        type="textarea"
        placeholder="请输入内容"
        @blur="blurFun"/>
      <div class="btn-group">
        <el-popover
          v-model="commentsPopover"
          placement="top"
          width="400"
          trigger="click">
          <!-- 表情 -->
          <emoji @select="selectEmoji"/>
          <img
            slot="reference"
            src="@/assets/img/smiling_face.png"
            class="smiling-img">
        </el-popover>
        <div class="btn-box">
          <el-button
            :loading="contentLoading"
            type="primary"
            @click="commentSubmit()">回复</el-button>
          <el-button @click="data.showComment = false">取消</el-button>
        </div>
      </div>
    </div>
  </div>
</template>
<script type="text/javascript">
import xss from 'xss'
import emoji from '@/components/emoji'
// API
import { journalSetread } from '@/api/oamanagement/journal'

import {
  setCommentAPI,
  deleteCommentAPI,
  queryCommentListAPI
} from '@/api/oamanagement/common'

// 关联业务 - 选中列表
import relatedBusiness from '@/components/relatedBusiness'

import { mapGetters } from 'vuex'
import FileCell from '@/views/OAManagement/components/fileCell'

export default {
  name: 'JournalCell', // 日志cell
  components: {
    emoji,
    relatedBusiness,
    FileCell
  },
  mixins: [],
  props: {
    data: Object,
    logIndex: {
      type: Number,
      default: 0
    },
    // 工作台操作动态展示
    showWorkbench: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      // 评论
      commentsTextarea: '',
      // 回复数据
      childCommentsTextarea: '',
      // 评论 -- 表情
      commentsPopover: false,
      replyChildComment: null, // 被评论对象
      replyChildIndex: -1, // -1 是主评论 0以上为子评论
      childCommentsPopover: false,
      blurIndex: 0,
      contentLoading: false,
      // 父元素
      parentTarget: null,
      awaitMoment: false, // 等客户浏览
      // 评论列表
      replyList: []
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    allData() {
      const allData = {}
      allData.business = this.data.businessList
      allData.contacts = this.data.contactsList
      allData.contract = this.data.contractList
      allData.customer = this.data.customerList
      return allData
    },
    allDataShow() {
      if (
        this.data.businessList.length != 0 ||
        this.data.contactsList.length != 0 ||
        this.data.contractList.length != 0 ||
        this.data.customerList.length != 0
      ) {
        return true
      } else {
        return false
      }
    }
  },
  watch: {},
  mounted() {
    if (this.data.isRead == 0 && !this.showWorkbench) {
      this.$bus.on('journal-list-box-scroll', target => {
        this.observePreview(target)
      })
      this.observePreview(
        document.getElementById('journal-cell' + this.logIndex).parentNode
      )
    }

    this.replyList = this.data.replyList
  },
  beforeDestroy() {
    this.$bus.off('journal-list-box-scroll')
  },
  methods: {
    /**
     * 观察预览
     */
    observePreview(target) {
      if (this.data.isRead == 0) {
        if (target) {
          this.parentTarget = target
        }
        const ispreview = this.whetherPreview()
        if (!this.awaitMoment && ispreview) {
          this.awaitMoment = true
          setTimeout(() => {
            this.awaitMoment = false
            const ispreview = this.whetherPreview()
            if (ispreview) {
              this.submiteIsRead()
            }
          }, 3000)
        }
      }
    },

    /**
     * 是否预览
     */
    whetherPreview() {
      const dom = this.parentTarget.children[this.logIndex]
      if (this.parentTarget.getBoundingClientRect()) {
        const offsetTop =
          this.parentTarget.getBoundingClientRect().top -
          dom.getBoundingClientRect().top
        let ispreview = false
        if (
          offsetTop <= 0 &&
          Math.abs(offsetTop) < this.parentTarget.clientHeight
        ) {
          ispreview = true
        } else if (offsetTop > 0 && offsetTop < dom.clientHeight) {
          ispreview = true
        }
        return ispreview
      } else {
        return false
      }
    },
    submiteIsRead() {
      journalSetread({
        logId: this.showWorkbench ? this.data.actionId : this.data.logId
      })
        .then(res => {
          this.$store.dispatch('GetOAMessageNum', 'log')
          this.data.isRead = 1
        })
        .catch(() => {})
    },

    // 获取评论信息
    getCommentList() {
      queryCommentListAPI({
        typeId: this.data.logId,
        type: 1
      })
        .then(res => {
          this.replyList = res.data
        })
        .catch(() => {})
    },

    verifyAwaitInfo() {},
    // 编辑 删除
    handleCommand(command) {
      this.$emit('on-handle', { type: command, data: { item: this.data }})
    },
    checkRelatedDetail(type, data) {
      this.$emit('on-handle', {
        type: 'related-detail',
        data: { type: type, item: data }
      })
    },
    // 评论删除
    discussDelete(val, items, index) {
      this.$confirm('确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          deleteCommentAPI({
            commentId: val.commentId
          }).then(res => {
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            items.splice(index, 1)
            this.$emit('on-handle', {
              type: 'discuss-delete',
              data: { item: this.data }
            })
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    },
    // 回复 -- 显示输入框
    discussBtn(item, index) {
      if (item.show) {
        this.$set(item, 'show', false)
        this.replyChildComment = null
      } else {
        this.$set(item, 'show', true)
        this.$set(item, 'showComment', false)
        this.replyChildComment = item
        this.replyChildIndex = index
      }
    },
    // 子评论 回复 -- 提交
    childCommentSubmit() {
      if (this.replyChildComment && this.childCommentsTextarea) {
        var item =
          this.replyChildIndex == -1
            ? this.replyChildComment
            : this.replyChildComment.childCommentList[this.replyChildIndex]
        this.contentLoading = true
        setCommentAPI({
          pid: item.userId,
          typeId: item.typeId,
          mainId: item.mainId == 0 ? item.commentId : item.mainId,
          type: 2,
          content: xss(this.childCommentsTextarea)
        })
          .then(res => {
            this.childCommentsPopover = false

            res.data.user = this.userInfo
            res.data.replyUser = item.user

            this.replyChildComment.childCommentList.push(res.data)
            this.$message.success('回复成功')
            this.replyChildComment.show = false
            this.replyChildComment = null

            this.contentLoading = false
            this.childCommentsTextarea = ''
            this.$emit('on-handle', {
              type: 'discuss-submit',
              data: { item: this.data }
            })
          })
          .catch(() => {
            this.$message.error('回复失败')
            this.contentLoading = false
          })
      }
    },
    // 评论 -- 提交
    commentSubmit() {
      if (this.commentsTextarea) {
        this.contentLoading = true
        setCommentAPI({
          typeId: this.showWorkbench ? this.data.actionId : this.data.logId,
          type: 2, // 1 任务 2 日志
          content: xss(this.commentsTextarea)
        })
          .then(res => {
            // 插入一条数据
            this.$set(this.data, 'showComment', false)
            res.data.childCommentList = []
            res.data.show = false
            res.data.user = this.userInfo

            this.replyList.push(res.data)
            this.commentsTextarea = ''
            this.$message.success('回复成功')
            this.contentLoading = false
          })
          .catch(() => {
            this.$message.error('回复失败')
            this.contentLoading = false
          })
      }
    },
    // 评论
    commentBtn(item) {
      if (item.showComment) {
        this.$set(item, 'showComment', false)
      } else {
        if (this.replyChildComment) {
          this.replyChildComment.show = false
        }
        this.$set(item, 'showComment', true)
      }
    },
    // 评论选中功能
    selectEmoji(val) {
      const list = this.commentsTextarea.split('')
      list.splice(this.blurIndex, 0, val)
      this.commentsTextarea = list.join('')
      this.commentsPopover = false
    },
    // 回复选中功能
    childSelectEmoji(val) {
      const list = this.childCommentsTextarea.split('')
      list.splice(this.blurIndex, 0, val)
      this.childCommentsTextarea = list.join('')
      this.childCommentsPopover = false
    },
    blurFun(eve) {
      this.blurIndex = eve.target.selectionEnd
    },
    // 放大图片
    imgZoom(val, k) {
      this.$bus.emit('preview-image-bus', {
        index: k,
        data: val.map(function(item, index, array) {
          return {
            url: item.filePath,
            name: item.name
          }
        })
      })
    }
  }
}
</script>
<style lang="scss" scoped>
@import '../styles/content.scss';

.list {
  .list-content {
    padding: 20px;
    .header {
      margin-bottom: 15px;
      @include color9;
      font-size: 12px;
      .row {
        display: inline-block;
        vertical-align: middle;
        margin-left: 10px;
        .row-title {
          margin-bottom: 7px;
          @include v-align;
        }
        .el-tooltip {
          margin-bottom: 0;
        }
        .el-popover__reference {
          margin-bottom: 0;
          cursor: pointer;
          display: inline-block;
        }
      }
      .head-img {
        display: inline-block;
        width: 35px;
        height: 35px;
        border-radius: 17.5px;
        @include v-align;
      }
      .name,
      .time,
      .read,
      .head-img {
        @include v-align;
      }
      .read,
      .time {
        display: inline-block;
        margin-right: 10px;
      }
      .name {
        font-size: 15px;
        margin: 0 10px 0 0;
        color: #333333;
      }
      .rt-setting {
        float: right;
        line-height: 30px;
        .dep {
          color: #333333;
          margin-right: 20px;
        }
        img {
          width: 16px;
          @include cursor;
          @include v-align;
        }
      }
    }
    .text {
      .row {
        margin-bottom: 7px;
        line-height: 22px;
        font-size: 13px;
        white-space: pre-wrap;
        word-wrap: break-word;
        .title {
          width: 95px;
          text-align: left;
          display: inline-block;
          @include color9;
          font-size: 13px;
        }
      }
    }
    .accessory {
      .upload-img-box {
        margin: 10px 0;
        .img-list {
          display: inline-block;
          position: relative;
          margin-right: 10px;
          width: 80px;
          height: 60px;
          line-height: 60px;
          cursor: pointer;
          img {
            max-width: 80px;
            max-height: 60px;
          }
        }
      }
    }
    .discuss {
      margin-top: 20px;
      .border {
        height: 1px;
        background: #e6e6e6;
        margin: 15px -20px;
      }
      .discuss-list {
        .name,
        .time,
        .head-img {
          @include v-align;
        }
        .head-img {
          width: 25px;
          height: 25px;
          display: inline-block;
          border-radius: 12.5px;
        }
        .name {
          margin: 0 10px;
          font-size: 15px;
        }
        .time {
          color: #999;
          font-size: 12px;
          display: inline-block;
          margin-top: 3px;
        }

        .reply-title {
          color: #333;
          font-size: 13px;
          margin: 10px 0 10px 40px;
          padding: 10px 10px 10px 40px;
          white-space: pre-wrap;
          word-wrap: break-word;
          span {
            letter-spacing: 0.5px;
            line-height: 18px;
          }
          .reply {
            color: #3e84e9;
          }
        }
        .reply-title /deep/ img {
          vertical-align: text-bottom;
          margin: 0 2px;
        }

        .children-reply {
          margin: 10px 0 10px 40px;
        }

        .rt {
          margin-top: 4px;
          color: #999;
          margin-right: 0;
          span {
            margin-left: 10px;
            cursor: pointer;
          }
        }
        .discuss-content {
          background: #f5f7fa;
          color: #777;
          line-height: 36px;
          margin-left: 40px;
          padding-left: 15px;
        }
        .discuss-content /deep/ img {
          vertical-align: middle;
          margin: 0 3px;
        }
        .border {
          margin: 15px 0 15px 40px;
        }
        .comment-box {
          margin-left: 40px;
          padding: 0;
          background: transparent;
          margin-top: 15px;
        }
      }
      .discuss-list:last-child {
        .border {
          display: none;
        }
      }
    }
  }
  .footer {
    background: #f4f7fd;
    height: 40px;
    line-height: 40px;
    color: #ccc;
    text-align: right;
    padding-right: 20px;
    img {
      @include v-align;
      @include cursor;
    }
    .comment {
      margin-right: 15px;
      color: #6c7a95;
      font-size: 13px;
      cursor: pointer;
    }
  }
  .comment-box {
    margin: 20px;
    border: 1px solid #e6e6e6;
    .btn-group {
      padding: 0 20px 10px 10px;
      overflow: hidden;
      .btn-box {
        float: right;
      }
    }
    .btn-group /deep/ img {
      cursor: pointer;
    }
    .el-textarea /deep/ .el-textarea__inner {
      resize: none;
      border: 0;
    }
  }
}

.wukong {
  cursor: pointer;
}
</style>
