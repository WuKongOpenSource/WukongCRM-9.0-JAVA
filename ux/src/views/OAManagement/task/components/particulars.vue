<template>
  <!-- <div class="particulars-common"> -->
  <transition name="slide-fade">
    <el-card
      v-loading="loading"
      :style="{ 'z-index': zIndex }"
      class="particulars-common">
      <!-- 头部信息 -->
      <div
        v-if="taskData"
        slot="header"
        class="clear-fix">
        <div class="img-box-grop">
          <div>
            <el-popover
              v-model="priorityVisible"
              placement="bottom-start"
              width="200"
              trigger="click">
              <div class="particulars-priority-box">
                <p
                  v-for="(item, index) in particularsList"
                  :key="index"
                  @click="priorityBtn(item, taskData.priority)">
                  <span :style="{'border-color': item.color, 'background': item.id == taskData.priority ? item.color: 'transparent', 'color': item.id == taskData.priority ? '#fff': item.color}">{{ item.label }}</span>
                </p>
              </div>
              <span slot="reference">
                <i class="wukong wukong-lightning"/>
                <span>优先级</span>
              </span>
            </el-popover>
          </div>
          <div>
            <tag-index
              :placement="'bottom'"
              :task-data="taskData">
              <div slot="editIndex">
                <i class="wukong wukong-label"/>
                <span>标签</span>
              </div>
            </tag-index>
          </div>
          <div
            v-if="taskData.pid == 0"
            @click="addSubtasksBtn">
            <i class="wukong wukong-sub-task"/>
            <span>子任务</span>
          </div>
          <div>
            <el-upload
              :http-request="httpRequest"
              class="upload-demo"
              action="https://jsonplaceholder.typicode.com/posts/"
              multiple
              list-type="picture">
              <i class="wukong wukong-file"/>
              <span>附件</span>
            </el-upload>
          </div>
          <div>
            <el-popover
              placement="bottom"
              width="80"
              trigger="click">
              <div class="more-btn-group">
                <p @click="moreDelete">删 除</p>
              </div>
              <span slot="reference">
                <img src="@/assets/img/task_ellipsis.png">
              </span>
            </el-popover>
          </div>
          <div class="task-close-img">
            <img
              src="@/assets/img/task_close.png"
              @click="closeBtn">
          </div>
        </div>
      </div>
      <div
        v-if="taskData"
        class="body-content">
        <slot name="extraContent"/>
        <div class="card-content-box">
          <el-checkbox
            v-model="taskData.checked"
            @change="titleCheckbox"/>
          <div class="card-content">
            <div class="card-content-row margin-bottom-15">
              <div class="priority-data-name">
                <div
                  v-show="!nameVinput"
                  :style="{'text-decoration': taskData.checked ? 'line-through' : 'none'}"
                  class="title-text">
                  {{ taskData.name }}
                  <span
                    class="el-icon-edit-outline"
                    @click="nameVinput = true, taskDataName = taskData.name"/>
                </div>
                <div
                  v-show="nameVinput"
                  class="show-input">
                  <el-input
                    v-model="taskDataName"
                    :maxlength="50"
                    size="medium"/>
                  <div class="btn-box">
                    <el-button
                      type="primary"
                      size="mini"
                      @click="nameVShow(taskDataName)">保 存</el-button>
                    <el-button
                      size="mini"
                      @click="nameVinput = false">取 消</el-button>
                  </div>
                </div>
              </div>
              <div class="card-row-right">
                <flexbox class="text-right">
                  <div class="color-label user-name-label"> 负责人： </div>
                  <div
                    v-if="taskData.mainUser"
                    class="bg-position">
                    <el-tooltip
                      placement="bottom"
                      effect="light"
                      popper-class="tooltip-change-border">
                      <div slot="content">
                        <span>{{ taskData.mainUser.realname }}</span>
                      </div>
                      <div
                        v-photo="taskData.mainUser"
                        v-lazy:background-image="$options.filters.filterUserLazyImg(taskData.mainUser.img)"
                        class="div-photo main-user-name"/>
                    </el-tooltip>
                    <img
                      src="@/assets/img/delete_task.png"
                      class="el-icon-close"
                      alt=""
                      @click="editMainUser('')">
                  </div>
                  <template v-else>
                    <el-popover
                      placement="bottom"
                      width="280"
                      trigger="click">
                      <xh-user
                        ref="xhuser"
                        radio
                        @changeCheckout="editMainUser"/>
                      <i
                        slot="reference"
                        class="wukong wukong-addition-task"/>
                    </el-popover>
                  </template>
                </flexbox>
              </div>
            </div>
            <div
              v-if="taskData.labelList"
              class="card-content-row  margin-bottom-25">
              <div class="particulars-priority-copy">
                <template v-show="taskData.labelList.length != 0">
                  <span
                    v-for="(item, index) in taskData.labelList"
                    :style="{'background': item.color ? item.color : '#ccc'}"
                    :key="index"
                    class="item-color">
                    {{ item.labelName }}
                  </span>
                </template>
                <div class="add-tag">
                  <tag-index
                    :placement="'right'"
                    :task-data="taskData">
                    <div slot="editIndex">
                      <span class="el-icon-plus"/>
                      <span class="label">标签</span>
                    </div>
                  </tag-index>
                </div>
              </div>
              <div class="card-row-right">
                <div class="text-right">
                  <span class="color-label"> 截止日期： </span>
                  <div class="time-top">
                    <el-date-picker
                      ref="endTime"
                      :class="{ 'no-time-top': !taskData.stopTime }"
                      v-model="taskData.stopTime"
                      :clearable="false"
                      type="date"
                      value-format="yyyy-MM-dd"
                      placeholder=""
                      @change="endTimeChange"/>
                    <img
                      v-if="taskData.stopTime"
                      src="@/assets/img/delete_task.png"
                      class="el-icon-close time-top-close"
                      alt=""
                      @click.stop="deleteTimeTop(taskData.stopTime)">
                  </div>
                </div>
              </div>
            </div>
            <div class="add-description">
              <div v-show="!addDescriptionShow">
                <div
                  v-if="taskData.description"
                  @click="addDescriptionShow = true; addDescriptionTextarea = taskData.description">{{ taskData.description }}</div>
                <div
                  v-else
                  class="no-description">
                  <span class="color-label">暂无描述</span>
                  <el-button
                    type="text"
                    icon="el-icon-plus"
                    @click="addDescriptionShow = true">添加描述</el-button>
                </div>
              </div>
              <div v-show="addDescriptionShow">
                <el-input
                  :autosize="{ minRows: 2}"
                  v-model="addDescriptionTextarea"
                  type="textarea"
                  placeholder="请输入内容"/>
                <div class="btn-box">
                  <el-button
                    type="primary"
                    size="mini"
                    @click="addDescriptionSubmit">保 存</el-button>
                  <el-button
                    type="text"
                    size="mini"
                    @click="addDescriptionShow = false">取 消</el-button>
                </div>
              </div>
            </div>
            <div class="card-content-row card-content-row-column">
              <flexbox>
                <div class="color-label participant">
                  <i class="wukong wukong-user"/>
                  <span>参与人： </span>
                </div>
                <div
                  v-if="taskData.ownerUserList"
                  class="participant-class">
                  <span
                    v-for="(item, index) in taskData.ownerUserList"
                    :key="index"
                    class="owner-list">
                    <el-tooltip
                      placement="bottom"
                      effect="light"
                      popper-class="tooltip-change-border">
                      <div slot="content">
                        <span>{{ item.realname }}</span>
                      </div>
                      <div
                        v-photo="item"
                        v-lazy:background-image="$options.filters.filterUserLazyImg(item.img)"
                        :key="item.img"
                        class="div-photo item-img"/>
                    </el-tooltip>
                    <img
                      src="@/assets/img/delete_task.png"
                      class="el-icon-close"
                      @click="deleteOwnerList(item, index)">
                  </span>
                </div>
                <members-dep
                  :close-dep="true"
                  :content-block="false"
                  :user-checked-data="taskData.ownerUserList"
                  @popoverSubmit="editOwnerList">
                  <i
                    slot="membersDep"
                    class="wukong wukong-addition-task"/>
                </members-dep>
              </flexbox>
            </div>
            <div class="card-content-row card-content-row-column margin-bottom-30">
              <span class="color-label participant">
                <i class="wukong wukong-relevance"/>
                <span>关联业务</span>
              </span>
              <!-- 关联业务 -->
              <related-business
                :margin-left="'0'"
                :is-task="true"
                :all-data="allData"
                :task-id="taskData.taskId"
                @checkRelatedDetail="checkRelatedDetail"
                @checkInfos="checkInfos"/>
            </div>
            <div class="card-content-row card-content-row-column">
              <div class="display-flex sub-task margin-bottom-7">
                <span class="color-label participant">
                  <i class="wukong wukong-sub-task"/>
                  <span>子任务</span>
                </span>
                <template v-if="taskData.childTask.length != 0">
                  <span class="color-label sub-task-progress"> ({{ subTaskProgress }}/{{ taskData.childTask.length }}) </span>
                  <el-progress
                    :percentage="subTaskProgress/taskData.childTask.length*100"
                    :stroke-width="10"/>
                </template>
                <template v-else>
                  <span class="color-label sub-task-progress"> ({{ subTaskProgress }}/{{ taskData.childTask.length }}) </span>
                  <el-progress :percentage="0"/>
                </template>
              </div>
              <template v-if="taskData.childTask.length != 0">
                <div
                  v-for="(item, index) in taskData.childTask"
                  :key="index"
                  class="card-related-matters subtasks-box">
                  <div
                    v-if="!item.showEdit"
                    class="show-edit">
                    <div
                      style="display: inline-block;"
                      @click.stop>
                      <el-checkbox
                        v-model="item.checked"
                        @change="subtasksCheckbox(item, $event)"/>
                    </div>
                    <span
                      :style="{'text-decoration': item.checked ? 'line-through' : 'none'}"
                      class="item-name">{{ item.name }}</span>
                    <!-- 编辑和删除 -->
                    <div class="edit-del-box">
                      <i
                        class="wukong wukong-edit-task"
                        @click="editSubTask(item)"/>
                      <i
                        class="wukong wukong-delete-task"
                        @click="deleteSubTask(item)"/>
                    </div>
                    <div class="rt">
                      <flexbox class="rt-box">
                        <div
                          v-if="item.stopTime"
                          class="bg-color task-bg-color">{{ item.stopTime | moment("MM-DD") }} 截止</div>
                        <div
                          v-photo="item.mainUser"
                          v-lazy:background-image="$options.filters.filterUserLazyImg(item.mainUser.img)"
                          v-if="item.mainUser"
                          :key="item.mainUser.img"
                          class="div-photo"/>
                      </flexbox>
                    </div>
                  </div>
                  <sub-task
                    v-else
                    :sub-task-com="'edit'"
                    :time="item.stopTime"
                    :sub-data="item"
                    :text="item.name"
                    :task-id="subTaskID"
                    :checkbox-data="item.checked"
                    :task-data="taskData"
                    @on-handle="handleSubTasksBlock($event, item)"/>
                </div>
              </template>
              <div
                v-if="addSubtasks"
                class="add-subtasks"
                @click="addSubtasks = false">
                <span class="el-icon-plus"/>
                <span>添加子任务</span>
              </div>
              <sub-task
                v-else
                :sub-task-com="'new'"
                :task-data="taskData"
                @on-handle="handleSubTasksBlock"/>
            </div>
            <div
              v-if="fileList.length != 0"
              class="card-content-row card-content-row-column">
              <span class="color-label participant class-file">
                <i class="wukong wukong-file"/>
                <span>附件</span>
              </span>
              <div class="accessory-box">
                <file-cell
                  v-for="(file, fileIndex) in fileList"
                  :key="fileIndex"
                  :data="file"
                  :cell-index="fileIndex"
                  :module_id="id"
                  :show-delete="true"
                  @delete="accessoryDeleteFun"/>
              </div>
            </div>
          </div>
        </div>
        <div class="border"/>
        <!-- 底部内容 -->
        <div class="card-footers">
          <div class="footer-title">
            <span @click="footerTitle(0)">
              <i
                v-if="isComment"
                class="wukong wukong-comment-task"
                style="color: #3E84E9;"/>
              <i
                v-else
                class="wukong wukong-comment-task"/>
              <span
                :style="{'color': isComment ? '#3E84E9' : '#666'}"
                class="cursor-pointer">评论</span>
            </span>
            <span class="title-border"/>
            <span @click="footerTitle(1)">
              <i
                v-if="isComment"
                class="wukong wukong-activity-task"
                style="font-size: 18px;"/>
              <i
                v-else
                class="wukong wukong-activity-task"
                style="color: #3E84E9; font-size: 18px;"/>
              <span
                :style="{'color': isComment ? '#666' : '#3E84E9'}"
                class="cursor-pointer"
                @click="footerTitle(isComment)">活动</span>
            </span>
          </div>
          <div
            v-loading="commentsLoading"
            class="footer-content-box">
            <div class="footer-content">
              <div
                v-if="commentsActivities"
                class="add-comments">
                <div class="footer-img">
                  <div
                    v-photo="userInfo"
                    v-lazy:background-image="$options.filters.filterUserLazyImg(userInfo.img)"
                    :key="userInfo.img"
                    class="div-photo"/>
                </div>
                <div class="comments-con">
                  <div
                    v-clickoutside="commentsSubmit"
                    v-if="addComments"
                    class="comments-box">
                    <el-input
                      ref="commentsTextareaRef"
                      :rows="4"
                      v-model="commentsTextarea"
                      type="textarea"
                      placeholder="添加评论"
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
                      <el-button
                        type="primary"
                        class="rt"
                        @click="commentsSub">发布</el-button>
                    </div>
                  </div>
                  <div
                    v-else
                    class="footer-bg"
                    @click="addComments = true">添加评论</div>

                  <!-- 评论 -->
                  <div
                    v-if="replyList && replyList.length != 0"
                    class="discuss">
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
                         v-html="emoji(discussItem.reply_content)"></p> -->

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
                            <emoji @select="selectEmojiChild"/>
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
                    </div>
                  </div>
                </div>
              </div>
              <div
                v-else
                class="activity-box">
                <template v-if="activityList.length != 0">
                  <div
                    v-for="(item, index) in activityList"
                    :key="index"
                    class="activity-list">
                    <div
                      v-photo="item"
                      v-lazy:background-image="$options.filters.filterUserLazyImg(item.img)"
                      :key="item.img"
                      class="div-photo"/>
                    <span class="activity-name">{{ item.realname }}</span>
                    <span>{{ item.content }}</span>
                    <span class="activity-time">{{ item.createTime }}</span>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </div>
        <slot/>
      </div>
      <div
        v-if="taskData && taskData.createUser"
        class="tip">
        <span>{{ taskData.createUser.realname }} 创建于 {{ taskData.createTime }}</span>
      </div>

      <c-r-m-full-screen-detail
        :visible.sync="showRelatedDetail"
        :crm-type="relatedCRMType"
        :id="relatedID"/>
    </el-card>
  </transition>
  <!-- </div> -->
</template>

<script type="text/javascript">
import {
  editTask,
  editTaskRelation,
  deleteTask,
  detailsTask,
  readLoglist
} from '@/api/oamanagement/task'
import {
  setCommentAPI,
  deleteCommentAPI,
  queryCommentListAPI
} from '@/api/oamanagement/common'
import { crmFileSave } from '@/api/common'

import membersDep from '@/components/selectEmployee/membersDep'
import tagIndex from './tag/tagIndex'
import subTask from './subTask'
// emoji
import xss from 'xss'
import emoji from '@/components/emoji'
// 关联业务 - 选中列表
import relatedBusiness from '@/components/relatedBusiness'
import XhUser from '@/components/CreateCom/XhUser'
import FileCell from '@/views/OAManagement/components/fileCell'
import CRMFullScreenDetail from '@/views/customermanagement/components/CRMFullScreenDetail'
import { mapGetters } from 'vuex'
import { getMaxIndex } from '@/utils'

export default {
  components: {
    membersDep,
    emoji,
    relatedBusiness,
    XhUser,
    tagIndex,
    CRMFullScreenDetail,
    subTask,
    FileCell
  },
  props: {
    id: [String, Number],
    detailIndex: Number,
    appendToBody: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      zIndex: getMaxIndex(),
      // 评论 - 活动
      // 评论 - 活动显示的内容
      commentsActivities: true,
      // 评论框
      commentsTextarea: '',
      // 回复
      childCommentsTextarea: '',
      // 评论 - 是否弹出表情
      // 回复的一行用户数据
      replyChildComment: null, // 被评论对象
      replyChildIndex: -1, // -1 是主评论 0以上为子评论
      contentLoading: false, // 回复按钮loading
      commentsPopover: false,
      // 回复弹出框
      childCommentsPopover: false,
      // 紧急弹出框
      priorityVisible: false,
      // 优先级列表
      particularsList: [
        { id: 3, label: '高', color: '#ED6363' },
        { id: 2, label: '中', color: '#FF9668' },
        { id: 1, label: '低', color: '#8bb5f0' },
        { id: 0, label: '无', color: '#ccc' }
      ],
      // 是否显示子任务
      addSubtasks: true,
      // 任务名称和编辑切换
      nameVinput: false,
      // 任务名
      taskDataName: '',
      // 是否显示描述
      addDescriptionShow: false,
      // 描述内容
      addDescriptionTextarea: '',
      // 子任务进度
      subTaskProgress: 0,
      blurIndex: 0,
      // 是否显示评论框
      addComments: false,
      allData: {},
      isComment: true,
      commentsLoading: false,
      // 相关详情的查看
      relatedID: '',
      relatedCRMType: '',
      showRelatedDetail: false,
      // 子任务ID
      subTaskID: null,
      // 任务详情
      loading: false,
      taskData: null,
      activityList: [],
      fileList: [],
      // 评论列表
      replyList: []
    }
  },
  computed: {
    ...mapGetters(['userInfo'])
  },
  watch: {
    id: function(val) {
      this.initInfo()
      this.getDetail()
      this.getActivityList()
      this.getCommentList()
    }
  },
  mounted() {
    if (this.appendToBody) {
      document.body.appendChild(this.$el)
    }
    if (this.id) {
      this.getDetail()
      this.getActivityList()
      this.getCommentList()
    }
  },

  beforeDestroy() {
    if (this.appendToBody && this.$el && this.$el.parentNode) {
      this.$el.parentNode.removeChild(this.$el)
    }
  },
  methods: {
    initInfo() {
      this.taskData = null
      this.subTaskProgress = 0
      // 设置关联项列表
      this.allData = {
        business: [],
        contacts: [],
        contract: [],
        customer: []
      }

      this.taskData = null
      this.activityList = []
      this.fileList = []
    },
    // 基础详情
    getDetail() {
      this.loading = true
      detailsTask({ taskId: this.id })
        .then(res => {
          const taskData = res.data
          taskData.checked = taskData.status == 5

          if (taskData.childTask) {
            for (const item of taskData.childTask) {
              if (item.status == 5) {
                item.checked = true
                this.subTaskProgress++
              } else {
                item.checked = false
              }
            }
          }
          this.fileList = res.data.file
          this.allData = {
            business: taskData.businessList || [],
            contacts: taskData.contactsList || [],
            contract: taskData.contractList || [],
            customer: taskData.customerList || []
          }
          this.taskData = taskData
          this.loading = false
        })
        .catch(() => {
          this.loading = false
          this.closeBtn()
        })
    },
    // 获取活动信息
    getActivityList() {
      readLoglist({
        taskId: this.id
      })
        .then(res => {
          this.activityList = res.data
        })
        .catch(() => {})
    },

    // 获取评论信息
    getCommentList() {
      queryCommentListAPI({
        typeId: this.id,
        type: 1
      })
        .then(res => {
          this.replyList = res.data
        })
        .catch(() => {})
    },

    // 主题勾选
    titleCheckbox(val) {
      this.taskData.checked = val
      editTask({
        taskId: this.id,
        status: this.taskData.checked ? 5 : 1
      })
        .then(res => {
          this.$emit('on-handle', {
            type: 'title-check',
            value: val,
            index: this.detailIndex
          })
          this.$store.dispatch('GetOAMessageNum', 'task')
        })
        .catch(() => {
          this.$emit('on-handle', {
            type: 'title-check',
            value: !this.taskData.checked,
            index: this.detailIndex
          })
          this.taskData.checked = !this.taskData.checked
        })
    },
    closeBtn() {
      this.$emit('close')
    },
    // 评论 - 活动 切换功能
    footerTitle(key) {
      switch (key) {
        case 0:
          this.commentsActivities = true
          this.isComment = true
          break
        case 1:
          this.commentsActivities = false
          this.isComment = false
          break
      }
    },
    // 紧急按钮
    priorityBtn(value, def) {
      this.taskData.priority = value.id
      editTask({
        taskId: this.id,
        priority: value.id
      })
        .then(res => {
          this.$emit('on-handle', {
            type: 'change-priority',
            value: value,
            index: this.detailIndex
          })
          this.priorityVisible = false
        })
        .catch(() => {
          this.$message.error('编辑失败')
          this.taskData.priority = def
        })
    },
    // 更多 ———— 删除和规定按钮
    moreDelete() {
      this.$confirm('此操作将永久删除该任务, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'is-particulars'
      })
        .then(() => {
          deleteTask({
            taskId: this.id
          })
            .then(res => {
              this.$message.success('删除成功')
              this.$emit('on-handle', {
                type: 'delete',
                index: this.detailIndex
              })
              this.$emit('close')
            })
            .catch(() => {})
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    },
    // 附件 -- 上传
    httpRequest(val) {
      crmFileSave({
        file: val.file,
        batchId: this.taskData.batchId
      })
        .then(res => {
          this.fileList.push(res)
          // this.$emit('httpRequest', this.taskData)
          this.$message.success('上传成功')
        })
        .catch(() => {})
    },
    // 附件删除
    accessoryDeleteFun(index, item) {
      this.fileList.splice(index, 1)
    },
    // 子任务添加
    addSubtasksBtn() {
      this.addSubtasks = !this.addSubtasks
    },
    // 子任务 -- 勾选
    subtasksCheckbox(val, e) {
      if (e) {
        this.$set(val, 'checked', true)
        this.subTaskProgress++
      } else {
        this.$set(val, 'checked', false)
        this.subTaskProgress--
      }
      this.$emit('on-handle', {
        type: 'change-sub-task',
        value: {
          subdonecount: this.subTaskProgress,
          allcount: this.taskData.childTask.length
        },
        index: this.detailIndex
      })
      editTask({
        taskId: val.taskId,
        status: e ? 5 : 1
      })
        .then(res => {})
        .catch(() => {
          this.$message.error('子任务标记失败')
          if (e) {
            this.$set(val, 'checked', false)
            this.subTaskProgress--
          } else {
            this.$set(val, 'checked', true)
            this.subTaskProgress++
          }
          this.$emit('on-handle', {
            type: 'change-sub-task',
            value: {
              subdonecount: this.subTaskProgress,
              allcount: this.taskData.childTask.length
            },
            index: this.detailIndex
          })
          // this.$emit('', val, e)
        })
    },
    /**
     * 参与人操作
     */
    // 提交按钮
    editOwnerList(users, dep) {
      this.taskData.ownerUserList = []
      editTask({
        taskId: this.id,
        ownerUserId: users
          .map(item => {
            return item.userId
          })
          .join(',')
      })
        .then(res => {
          this.taskData.ownerUserList = users
        })
        .catch(() => {})
    },
    // 参与人删除按钮
    deleteOwnerList(item, index) {
      editTask({
        taskId: this.id,
        ownerUserId: this.taskData.ownerUserList
          .filter(userItem => {
            return userItem.userId != item.userId
          })
          .map(item => {
            return item.userId
          })
          .join(',')
      })
        .then(res => {
          this.taskData.ownerUserList.splice(index, 1)
        })
        .catch(() => {})
    },
    // 编辑负责人
    editMainUser(val) {
      editTask({
        taskId: this.id,
        mainUserId: val ? val.data[0].userId : ''
      })
        .then(res => {
          if (val) {
            this.$set(this.taskData, 'mainUser', val.data[0])
          } else {
            this.$set(this.taskData, 'mainUser', null)
          }
        })
        .catch(() => {})
    },
    // 编辑任务名
    nameVShow(val) {
      editTask({
        name: val,
        taskId: this.id
      })
        .then(res => {
          this.nameVinput = false
          this.$emit('on-handle', {
            type: 'change-name',
            value: val,
            index: this.detailIndex
          })
          this.taskData.name = val
        })
        .catch(() => {})
    },
    // 截至日期API
    endTimeChange(val) {
      editTask({
        stopTime: val,
        taskId: this.id
      })
        .then(res => {
          // val.substring(5)
          this.$emit('on-handle', {
            type: 'change-stop-time',
            value: val,
            index: this.detailIndex
          })
        })
        .catch(() => {})
    },
    // 描述提交按钮
    addDescriptionSubmit() {
      editTask({
        taskId: this.id,
        description: this.addDescriptionTextarea
      })
        .then(res => {
          this.addDescriptionShow = false
          this.$set(this.taskData, 'description', this.addDescriptionTextarea)
        })
        .catch(() => {})
    },
    // 评论选中功能
    selectEmoji(val) {
      const list = this.commentsTextarea.split('')
      list.splice(this.blurIndex, 0, val)
      this.commentsTextarea = list.join('')
      this.commentsPopover = false
    },
    // 评论回复 -- 选中功能
    selectEmojiChild(val) {
      const list = this.childCommentsTextarea.split('')
      list.splice(this.blurIndex, 0, val)
      this.childCommentsTextarea = list.join('')
      this.childCommentsPopover = false
    },
    // 评论发布
    commentsSub() {
      if (this.commentsTextarea) {
        this.commentsLoading = true
        setCommentAPI({
          typeId: this.id,
          type: 1,
          content: xss(this.commentsTextarea)
        })
          .then(res => {
            res.data.childCommentList = []
            res.data.show = false
            res.data.user = this.userInfo
            this.replyList.push(res.data)
            this.commentsTextarea = ''
            // this.$emit('commentsSet', 'add')
            this.$emit('on-handle', {
              type: 'change-comments',
              value: 'add',
              index: this.detailIndex
            })
            this.commentsLoading = false
          })
          .catch(() => {
            this.commentsLoading = false
          })
      }
    },
    // 回复评论
    childCommentSubmit() {
      if (this.replyChildComment && this.childCommentsTextarea) {
        var item =
          this.replyChildIndex == -1
            ? this.replyChildComment
            : this.replyChildComment.childCommentList[this.replyChildIndex]
        setCommentAPI({
          pid: item.userId,
          typeId: item.typeId,
          mainId: item.mainId == 0 ? item.commentId : item.mainId,
          type: 1,
          content: xss(this.childCommentsTextarea)
        })
          .then(res => {
            this.childCommentsPopover = false
            res.data.user = this.userInfo
            res.data.replyUser = item.user
            this.replyChildComment.childCommentList.push(res.data)
            this.replyChildComment.show = false
            this.replyChildComment = null
            this.childCommentsTextarea = ''
            this.$emit('on-handle', {
              type: 'change-comments',
              value: 'add',
              index: this.detailIndex
            })
          })
          .catch(() => {})
      }
    },
    // 删除评论
    discussDelete(val, items, index) {
      this.$confirm('确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'is-particulars'
      })
        .then(() => {
          deleteCommentAPI({
            commentId: val.commentId
          })
            .then(res => {
              items.splice(index, 1)
              this.$emit('on-handle', {
                type: 'change-comments',
                value: 'delete',
                index: this.detailIndex
              })
              // this.$emit('commentsSet', 'delete')
            })
            .catch(() => {})
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    },
    // 点击回复按钮
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
    blurFun(eve) {
      this.blurIndex = eve.target.selectionEnd
    },
    // 关联业务提交按钮
    checkInfos(val) {
      editTaskRelation({
        taskId: this.id,
        customerIds:
          val.customerIds && val.customerIds.length
            ? ',' + val.customerIds.join(',') + ','
            : '',
        contactsIds:
          val.contactsIds && val.contactsIds.length
            ? ',' + val.contactsIds.join(',') + ','
            : '',
        businessIds:
          val.businessIds && val.businessIds.length
            ? ',' + val.businessIds.join(',') + ','
            : '',
        contractIds:
          val.contractIds && val.contractIds.length
            ? ',' + val.contractIds.join(',') + ','
            : ''
      })
        .then(res => {
          this.$message.success('关联成功')
        })
        .catch(() => {})
    },
    commentsSubmit(event) {
      this.addComments = false
    },
    checkRelatedDetail(crmType, item) {
      this.relatedID = item.key
      this.relatedCRMType = crmType
      this.showRelatedDetail = true
    },
    deleteSubTask(val) {
      this.$confirm('此操作将永久删除该任务, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'is-particulars'
      })
        .then(() => {
          deleteTask({
            taskId: val.taskId
          })
            .then(res => {
              const subData = this.taskData.childTask
              for (const i in subData) {
                if (subData[i].taskId == val.taskId) {
                  subData.splice(i, 1)
                  break
                }
              }
              if (val.checked) {
                this.subTaskProgress--
                this.$emit('on-handle', {
                  type: 'change-sub-task',
                  value: {
                    subdonecount: this.subTaskProgress,
                    allcount: this.taskData.childTask.length
                  },
                  index: this.detailIndex
                })
              }
              this.$message.success('子任务删除成功')
            })
            .catch(() => {
              this.$message.error('子任务删除失败')
            })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    },
    editSubTask(val) {
      this.subTaskID = val.taskId
      const dataList = this.taskData.childTask
      for (const i in dataList) {
        if (dataList[i].taskId == val.taskId) {
          this.$set(dataList[i], 'showEdit', true)
        } else {
          this.$set(dataList[i], 'showEdit', false)
        }
      }
    },
    handleSubTasksBlock(data, item) {
      if (data.type == 'edit') {
        this.$set(item, 'showEdit', false)
      } else if (data.type == 'add') {
        this.addSubtasks = true
        if (data.result == 'success') {
          this.$emit('on-handle', {
            type: 'change-sub-task',
            value: {
              subdonecount: this.subTaskProgress,
              allcount: this.taskData.childTask.length + 1
            },
            index: this.detailIndex
          })
        } else {
          this.$emit('on-handle', {
            type: 'change-sub-task',
            value: {
              subdonecount: this.subTaskProgress,
              allcount: this.taskData.childTask.length - 1
            },
            index: this.detailIndex
          })
        }
      } else if (data.type == 'cancel') {
        if (item) {
          this.$set(item, 'showEdit', false)
        } else {
          this.addSubtasks = true
        }
      }
    },
    // 删除截止时间
    deleteTimeTop() {
      editTask({
        taskId: this.id,
        stopTime: ''
      })
        .then(res => {
          this.$set(this.taskData, 'stopTime', '')
        })
        .catch(() => {})
    }
  }
}
</script>
<style lang="scss" scoped>
.wukong {
  vertical-align: middle;
  color: #666;
}
.slide-fade-enter-active,
.slide-fade-leave-active {
  will-change: transform;
  transition: all 0.35s ease;
}
.slide-fade-enter,
.slide-fade-leave-to {
  transform: translateX(100%);
}
.display-flex {
  display: flex;
}
// 小手抓
.cursor-pointer {
  cursor: pointer;
}
// 时间背景
.bg-color {
  background: #f5f5f5;
  padding: 4px 8px;
  border-radius: 14px;
}
.task-bg-color {
  background: #e6e6e6;
}
.border {
  border-top: 1px solid #e6e6e6;
  margin: 0 -20px;
}

.particulars-priority-box {
  display: flex;
  p {
    flex: 1;
    cursor: pointer;
    display: inline-block;
    text-align: center;
    span {
      color: #aaa;
      font-size: 12px;
      padding: 7px 10px;
      text-align: center;
      display: inline-block;
      border-radius: 50%;
      border: 1px solid transparent;
    }
  }
}
// 更多按钮
.more-btn-group {
  line-height: 34px;
  font-size: 14px;
  margin: 0 -12px;
  p {
    padding-left: 22px;
    cursor: pointer;
  }
  p:hover {
    background: #f7f8fa;
    color: #3e84e9;
  }
}
// 负责人
.particulars-colleagues-list {
  margin: 0 -12px;
  p {
    height: 34px;
    line-height: 34px;
    padding-left: 22px;
    cursor: pointer;
  }
  p:hover {
    -webkit-box-shadow: 0 0 8px 2px #eee;
    box-shadow: 0 0 8px 2px #eee;
    background: 0 0;
  }
}
.particulars-common /deep/ .el-card__body {
  flex: 1;
  overflow: auto;
  display: flex;
  flex-direction: column;
  padding-bottom: 90px;
}
.tip {
  position: fixed;
  right: 0px;
  width: 926px;
  bottom: 0;
  overflow: hidden;
  margin-top: 50px;
  height: 40px;
  line-height: 40px;
  border-top: 1px solid #e6e6e6;
  background: #fff;
  color: #999;
  z-index: 9;
  span {
    margin-left: 55px;
  }
}
.particulars-common {
  position: fixed;
  top: 60px;
  right: 0px;
  width: 926px;
  bottom: 0;
  display: flex;
  flex-direction: column;
  .clear-fix {
    overflow: auto;
  }
  .img-box-grop {
    float: right;
    color: #999;
    .task-close-img {
      padding-left: 30px;
      img {
        width: 18px;
      }
    }

    .wukong {
      color: #999;
    }
  }
  .img-box-grop > div {
    display: inline-block;
    margin-right: 20px;
    cursor: pointer;
    img {
      vertical-align: middle;
    }
    .upload-demo /deep/ .el-upload-list--picture {
      display: none;
    }
  }
  .card-content-box {
    display: flex;
    img {
      vertical-align: middle;
    }
    .el-checkbox {
      height: 20px;
      margin-right: 0;
    }
    // 解决多选框样式
    .el-checkbox /deep/ .el-checkbox__inner {
      width: 20px;
      height: 20px;
    }
    .el-checkbox /deep/ .el-checkbox__inner::after {
      border-width: 2px;
      height: 12px;
      width: 5px;
      left: 5px;
    }
    .card-content {
      flex: 1;
      padding: 0 15px;
      .card-content-row {
        margin-bottom: 35px;
        display: flex;
        .priority-data-name {
          .title-text {
            font-size: 17px;
            color: #333;
            .el-icon-edit-outline {
              margin-left: 20px;
              color: #3e84e9;
              opacity: 0;
              cursor: pointer;
            }
          }
          .title-text:hover {
            .el-icon-edit-outline {
              opacity: 1;
            }
          }
          .show-input {
            width: 90%;
            .btn-box {
              margin-top: 10px;
            }
          }
        }
        .el-icon-circle-plus-outline {
          width: 30px;
          height: 30px;
          color: #ccc;
        }
        .card-row-right {
          width: 50%;
          .text-right {
            width: 60%;
            float: right;
            min-width: 200px;
            min-height: 28px;
            position: relative;
            .el-icon-close {
              opacity: 0;
              margin-left: 10px;
              color: #ccc;
              cursor: pointer;
            }
            .el-popover__reference {
              width: 20px;
            }
            .main-user-name {
              width: 25px;
              height: 25px;
              border-radius: 12.5px;
            }
            .user-name-label {
              width: 73px;
              display: inline-block;
            }

            .bg-position {
              position: relative;
              display: inline-block;
              .el-icon-close {
                opacity: 0;
                border-radius: 50%;
                cursor: pointer;
                position: absolute;
                z-index: 3;
                top: -7px;
                right: -5px;
              }
            }
            .wukong-addition-task {
              cursor: pointer;
            }

            .time-top {
              position: relative;
              display: inline-block;
              .time-top-close {
                opacity: 0;
                border-radius: 50%;
                cursor: pointer;
                position: absolute;
                z-index: 3;
                top: -7px;
                right: -5px;
              }
            }

            .time-top /deep/ .el-date-editor {
              width: 115px;
              input {
                background-color: #f5f5f5;
                border: none;
                border-radius: 15px;
                line-height: 30px;
                height: 30px;
                padding-right: 0;
              }
            }
            .time-top /deep/ .no-time-top.el-date-editor {
              width: 30px;
              .el-input__prefix {
                left: 2px;
              }
            }
          }

          .text-right:hover {
            .el-icon-close {
              opacity: 1;
            }
          }
          .color-label {
            margin-right: 10px;
            vertical-align: middle;
          }
        }
        .card-row-right:hover {
          .el-icon-close {
            opacity: 1;
          }
        }
        .particulars-priority-copy {
          .item-color {
            padding: 0 10px;
            color: #fff;
            font-size: 12px;
            margin-right: 5px;
            border-radius: 3px;
            display: inline-block;
            height: 22px;
            line-height: 22px;
            margin-bottom: 5px;
          }
          .el-icon-plus,
          .add-tag {
            color: #999;
          }
          .el-icon-plus {
            font-size: 13px;
            padding-left: 6px;
          }
          .add-tag {
            display: inline-block;
            width: 83px;
            height: 24px;
            line-height: 24px;
            background: rgba(245, 245, 245, 1);
            border-radius: 3px;
            cursor: pointer;
            text-align: center;
            font-size: 12px;
          }
          .label {
            padding-right: 10px;
          }
        }
        .class-file {
          img {
            margin: 0 5px;
          }
        }
      }
      .card-content-row-column {
        flex-direction: column;
        margin-right: 20px;
        .accessory-box {
          margin-top: 10px;
        }
        .participant {
          color: #666;
          img,
          span {
            vertical-align: middle;
          }
        }
        .participant-class {
          margin-left: 10px;
          min-height: 28px;
          .owner-list {
            position: relative;
            margin-right: 10px;
            display: inline-block;
            .item-img {
              width: 25px;
              height: 25px;
              border-radius: 12.5px;
            }
            .el-icon-close {
              opacity: 0;
              color: #fff;
              background: #ccc;
              border-radius: 50%;
              cursor: pointer;
              position: absolute;
              z-index: 3;
              top: -7px;
              right: -5px;
            }
          }
          .owner-list:hover {
            .el-icon-close {
              opacity: 1;
            }
          }
        }
        .task-add-png {
          display: inline-block;
        }
        .show-edit {
          background: #f5f7fa;
          height: 40px;
          line-height: 40px;
          // color: #3E84E9;
          padding: 0 10px;
          font-size: 13px;
          .card-related-matters-right {
            float: right;
            margin-top: 8px;
          }
          .rt {
            margin-right: 0;
            .bg-color {
              font-size: 12px;
              padding: 0 8px !important;
              vertical-align: middle;
              height: 30px;
              line-height: 30px;
            }
            .div-photo {
              margin-top: 0 !important;
            }
            .rt-box {
              height: 41.5px;
            }
          }
        }
        .display-flex {
          margin-bottom: 10px;
          span {
            margin-right: 10px;
          }
        }
        .el-progress {
          flex: 1;
          margin-left: 10px;
        }
        .el-progress /deep/ .el-progress-bar {
          padding-right: 0;
          .el-progress-bar__outer {
            background: #f5f5f5;
          }
        }
        .el-progress /deep/ .el-progress__text {
          display: none;
        }
        .subtasks-box {
          color: #333;
          border-radius: 3px;
          margin: 3px 0;
          height: 40px;
          // line-height: 40px;
          // cursor: pointer;
          // 解决多选框样式
          .el-checkbox /deep/ .el-checkbox__inner {
            width: 16px;
            height: 16px;
          }
          .el-checkbox /deep/ .el-checkbox__inner::after {
            border-width: 2px;
            height: 10px;
            width: 4px;
            left: 4px;
            top: 0;
          }
          .rt {
            .div-photo {
              margin-top: 7px;
              width: 25px;
              height: 25px;
              border-radius: 12.5px;
              margin-left: 10px;
            }
          }
          .edit-del-box {
            display: inline-block;
            padding-left: 10px;
            opacity: 0;
            .wukong {
              padding-left: 10px;
              cursor: pointer;
            }
            .wukong:hover {
              color: #3e84e9;
            }
          }
          .item-name {
            padding-left: 5px;
          }
        }
        .subtasks-box:hover {
          box-shadow: 0px 1px 4px 0px rgba(223, 227, 234, 1);
          .edit-del-box {
            opacity: 1;
          }
        }
      }
      .card-content-row > div {
        flex: 1;
      }
      .card-content-row /deep/ .related-business {
        margin: 10px 0;
        .list {
          margin: 0;
          .item-list {
            margin-bottom: 5px;
            background: #f5f7fa;
            border: 0;
          }
        }
        .add-file {
          display: inline-block;
          background: #f5f7fa;
          width: 100%;
          height: 40px;
          line-height: 40px;
          padding-left: 8px;
          margin: 0;
        }
      }
      .add-subtasks {
        color: #3e84e9;
        cursor: pointer;
        background: #f5f7fa;
        height: 40px;
        line-height: 40px;
        margin-top: 10px;
        padding-left: 12px;
        font-size: 13px;
      }
      .add-description /deep/ {
        margin-bottom: 20px;
        position: relative;
        cursor: pointer;
        white-space: pre-wrap;
        word-wrap: break-word;
        .no-description {
          color: #3e84e9;
          .color-label {
            color: #999;
          }
          button {
            margin-left: 5px;
          }
        }
        .btn-box {
          margin-top: 10px;
        }
        .el-textarea /deep/.el-textarea__inner {
          resize: none;
        }
      }
      .margin-bottom-15 {
        margin-bottom: 15px;
      }
      .margin-bottom-25 {
        margin-bottom: 25px;
      }
      .margin-bottom-30 {
        margin-bottom: 30px;
      }
      .margin-bottom-7 {
        margin-bottom: 7px;
      }
    }
    .color-label {
      color: #666;
    }
    .sub-task {
      .sub-task-progress {
        line-height: 22px;
      }
      .el-progress {
        line-height: 22px;
      }
    }
  }
  .card-footers {
    margin-top: 20px;
    .footer-title {
      margin-bottom: 40px;
      margin-left: 35px;
      img,
      span {
        vertical-align: middle;
        cursor: pointer;
      }
      .title-border {
        border-right: 2px solid #ccc;
        margin: 0 20px;
      }
    }
    .footer-content-box {
      display: flex;
      .footer-content {
        flex: 1;
        .activity-box {
          margin-left: 35px;
          margin-right: 35px;
          .activity-list {
            margin-bottom: 15px;
            color: #333;
            .activity-time {
              float: right;
              color: #999;
              font-size: 12px;
            }
            .activity-name {
              color: #666;
              margin: 0 5px;
            }
            .div-photo {
              width: 25px;
              height: 25px;
              border-radius: 12.5px;
              margin-right: 10px;
              vertical-align: middle;
            }
          }
        }
        .comments-con > .comments-box {
          margin-bottom: 10px;
        }
        .comments-box {
          border: 1px solid #e6e6e6;
          .el-textarea /deep/ .el-textarea__inner {
            border: 0;
            resize: none;
          }
          .btn-group {
            overflow: hidden;
            margin: 10px;
            .smiling-img {
              margin-top: 7px;
            }
            .rt {
              margin-right: 10px;
            }
          }
        }

        .add-comments {
          display: flex;
          .footer-img {
            margin-left: 35px;
            margin-right: 20px;
            .div-photo {
              width: 42px;
              height: 42px;
              border-radius: 21px;
              margin-top: 4px;
            }
          }
          .comments-con {
            flex: 1;
            margin-right: 35px;
            .footer-bg {
              background: #f5f5f5;
              height: 50px;
              line-height: 50px;
              padding-left: 24px;
              color: #666;
              cursor: pointer;
              margin-bottom: 5px;
            }
          }
        }
      }
    }
  }
  .title {
    color: #333333;
    font-size: 18px;
  }
  // 回到父任务
  .parent-task {
    margin-bottom: 15px;
    .p-name {
      margin-left: 10px;
    }
    .back {
      margin-left: 35px;
      color: #3e84e9;
      cursor: pointer;
    }
  }
}

// 评论列表
.discuss {
  padding: 10px 0;
  .discuss-list {
    .name,
    .time,
    .head-img {
      vertical-align: middle;
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
}

// 评论框
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
</style>

