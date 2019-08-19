<template>
  <transition name="slide-fade">
    <el-card v-loading="loading"
             class="particulars-common"
             :style="{ 'z-index': zIndex }">
      <!-- 头部信息 -->
      <div class="clear-fix"
           slot="header"
           v-if="taskData">
        <div class="work-name"
             v-if="taskData && taskData.workName">{{taskData.workName}}</div>
        <div class="img-box-grop">
          <div>
            <el-popover placement="bottom-start"
                        width="200"
                        v-model="priorityVisible"
                        trigger="click">
              <div class="particulars-priority-box">
                <p v-for="(item, index) in particularsList"
                   :key="index"
                   @click="priorityBtn(item, taskData.priority)">
                  <span :style="{'border-color': item.color, 'background': item.id == taskData.priority ? item.color: 'transparent', 'color': item.id == taskData.priority ? '#fff': item.color}">{{item.label}}</span>
                </p>
              </div>
              <span slot="reference">
                <i class="wukong wukong-lightning"></i>
                <span>优先级</span>
              </span>
            </el-popover>
          </div>
          <div>
            <tag-index :placement="'bottom'"
                       :taskData="taskData">
              <div slot="editIndex">
                <i class="wukong wukong-label"></i>
                <span>标签</span>
              </div>
            </tag-index>
          </div>
          <div @click="addSubtasksBtn"
               v-if="taskData.pid == 0">
            <i class="wukong wukong-sub-task"></i>
            <span>子任务</span>
          </div>
          <div>
            <el-upload class="upload-demo"
                       action="https://jsonplaceholder.typicode.com/posts/"
                       multiple
                       :http-request="httpRequest"
                       list-type="picture">
              <i class="wukong wukong-file"></i>
              <span>附件</span>
            </el-upload>
          </div>
          <div v-if="taskData.ishidden != 1">
            <el-popover placement="bottom"
                        width="80"
                        trigger="click">
              <div class="more-btn-group">
                <p v-if="taskData.isArchive != 1 && taskData.ishidden != 1"
                   @click="moreArchive">归 档</p>
                <p v-if="taskData.ishidden != 1"
                   @click="moreDelete">删 除</p>
              </div>
              <span slot="reference">
                <img src="@/assets/img/task_ellipsis.png">
              </span>
            </el-popover>
          </div>
          <div class="task-close-img">
            <img src="@/assets/img/task_close.png"
                 @click="closeBtn">
          </div>
        </div>
      </div>
      <div class="body-content"
           v-if="taskData">
        <!-- 归档 -->
        <div class="particulars-extraContent"
             v-if="taskData.isArchive == 1 && taskData.ishidden != 1">
          <span class="text">该任务已于 {{taskData.archiveTime}} 被归档。</span>
          <el-button type="primary"
                     class="border-radius"
                     size="mini"
                     @click="activateTask">激活</el-button>
        </div>
        <!-- 回收站 -->
        <div class="particulars-extraContent"
             v-if="taskData.ishidden == 1">
          <span class="text">该任务已于 {{taskData.hiddenTime}} 被放入回收站。</span>
          <el-button type="primary"
                     class="border-radius"
                     size="mini"
                     @click="recoverTask">恢 复</el-button>
          <el-button type="text"
                     @click="thoroughDeleteTask">彻底删除</el-button>
        </div>
        <div class="card-content-box">
          <div class="card-content">
            <div class="card-content-row margin-bottom-15">
              <el-checkbox v-model="taskData.checked"
                           @change="titleCheckbox"></el-checkbox>
              <div class="priority-data-name">
                <div class="title-text"
                     :style="{'text-decoration': taskData.checked ? 'line-through' : 'none'}"
                     v-show="!nameVinput">{{taskData.name}}<span class="el-icon-edit-outline"
                        @click="nameVinput = true, taskDataName = taskData.name"></span></div>
                <div v-show="nameVinput"
                     class="show-input">
                  <el-input v-model="taskDataName"
                            :maxlength="50"
                            size="medium"></el-input>
                  <div class="btn-box">
                    <el-button type="primary"
                               size="mini"
                               @click="nameVShow(taskDataName)">保 存</el-button>
                    <el-button size="mini"
                               @click="nameVinput = false">取 消</el-button>
                  </div>
                </div>
              </div>
              <div class="card-row-right">
                <flexbox class="text-right">
                  <div class="color-label user-name-label"> 负责人： </div>
                  <div v-if="taskData.mainUser"
                       class="bg-position">
                    <el-tooltip placement="bottom"
                                effect="light"
                                popper-class="tooltip-change-border">
                      <div slot="content">
                        <span>{{taskData.mainUser.realname}}</span>
                      </div>
                      <div v-photo="taskData.mainUser"
                           v-lazy:background-image="$options.filters.filterUserLazyImg(taskData.mainUser.img)"
                           :key="taskData.main_user_img"
                           class="div-photo main-user-name"></div>
                    </el-tooltip>
                    <img src="@/assets/img/delete_task.png"
                         class="el-icon-close"
                         @click="editMainUser('')"
                         alt="">
                  </div>
                  <template v-else>
                    <el-popover placement="bottom"
                                width="280"
                                trigger="click">
                      <xh-user ref="xhuser"
                               radio
                               :infoRequest="ownerListRequest"
                               :infoParams="{ workId: workId }"
                               @changeCheckout="editMainUser">
                      </xh-user>
                      <i class="wukong wukong-addition-task"
                         slot="reference"></i>
                    </el-popover>
                  </template>
                </flexbox>
              </div>
            </div>
            <div class="card-content-row  margin-bottom-25"
                 v-if="taskData.labelList">
              <div class="particulars-priority-copy">
                <template v-show="taskData.labelList.length != 0">
                  <span class="item-color"
                        :style="{'background': item.color ? item.color : '#ccc'}"
                        v-for="(item, index) in taskData.labelList"
                        :key="index">
                    {{item.labelName}}
                  </span>
                </template>
                <div class="add-tag">
                  <tag-index :placement="'right'"
                             :taskData="taskData">
                    <div slot="editIndex">
                      <span class="el-icon-plus"></span>
                      <span class="label">添加标签</span>
                    </div>
                  </tag-index>
                </div>
              </div>
              <div class="card-row-right">
                <div class="text-right">
                  <span class="color-label"> 截止日期： </span>
                  <div class="time-top">
                    <el-date-picker :class="{ 'no-time-top': !taskData.stopTime }"
                                    v-model="taskData.stopTime"
                                    type="date"
                                    ref="endTime"
                                    :clearable="false"
                                    @change="endTimeChange"
                                    format="yyyy-MM-dd"
                                    value-format="yyyy-MM-dd"
                                    placeholder="">
                    </el-date-picker>
                    <img src="@/assets/img/delete_task.png"
                         class="el-icon-close time-top-close"
                         v-if="taskData.stopTime"
                         @click.stop="deleteTimeTop(taskData.stopTime)"
                         alt="">
                  </div>
                </div>
              </div>
            </div>
            <div class="add-description">
              <div v-show="!addDescriptionShow">
                <div v-if="taskData.description"
                     class="description-content"
                     @click="addDescriptionShow = true; addDescriptionTextarea = taskData.description">{{taskData.description}}</div>
                <div v-else
                     class="no-description">
                  <span class="color-label">暂无描述</span>
                  <el-button type="text"
                             icon="el-icon-plus"
                             @click="addDescriptionShow = true">添加描述</el-button>
                </div>
              </div>
              <div v-show="addDescriptionShow">
                <el-input type="textarea"
                          :autosize="{ minRows: 2}"
                          placeholder="请输入内容"
                          v-model="addDescriptionTextarea">
                </el-input>
                <div class="btn-box">
                  <el-button type="primary"
                             size="mini"
                             @click="addDescriptionSubmit">保 存</el-button>
                  <el-button type="text"
                             size="mini"
                             @click="addDescriptionShow = false">取 消</el-button>
                </div>
              </div>
            </div>
            <div class="card-content-row card-content-row-column">
              <flexbox>
                <div class="color-label participant">
                  <i class="wukong wukong-user"></i>
                  <span>参与人： </span>
                </div>
                <div class="participant-class"
                     v-if="taskData.ownerUserList">
                  <span v-for="(item, index) in taskData.ownerUserList"
                        :key="index"
                        class="owner-list">
                    <el-tooltip placement="bottom"
                                effect="light"
                                popper-class="tooltip-change-border">
                      <div slot="content">
                        <span>{{item.realname}}</span>
                      </div>
                      <div v-photo="item"
                           v-lazy:background-image="$options.filters.filterUserLazyImg(item.img)"
                           :key="item.img"
                           class="div-photo item-img"></div>
                    </el-tooltip>
                    <img src="@/assets/img/delete_task.png"
                         class="el-icon-close"
                         @click="deleteOwnerList(item, index)">
                  </span>
                </div>
                <members-dep :closeDep="true"
                             :contentBlock="false"
                             :userCheckedData="taskData.ownerUserList"
                             :userRequest="ownerListRequest"
                             :userParams="{ workId: workId }"
                             @popoverSubmit="editOwnerList">
                  <i slot="membersDep"
                     class="wukong wukong-addition-task"></i>
                </members-dep>
              </flexbox>
            </div>
            <div class="card-content-row card-content-row-column margin-bottom-30">
              <span class="color-label participant">
                <i class="wukong wukong-relevance"></i>
                <span>关联业务</span>
              </span>
              <!-- 关联业务 -->
              <related-business :marginLeft="'0'"
                                :isTask="true"
                                :allData="allData"
                                :taskID="taskData.taskId"
                                @checkRelatedDetail="checkRelatedDetail"
                                @checkInfos="checkInfos">
              </related-business>
            </div>
            <div class="card-content-row card-content-row-column"
                 v-if="taskData.pid == 0">
              <div class="display-flex sub-task margin-bottom-7">
                <span class="color-label participant">
                  <i class="wukong wukong-sub-task"></i>
                  <span>子任务</span>
                </span>
                <template v-if="taskData.childTask.length != 0">
                  <span class="color-label sub-task-progress"> ({{subTaskProgress}}/{{taskData.childTask.length}}) </span>
                  <el-progress :percentage="subTaskProgress/taskData.childTask.length*100"
                               :stroke-width="10"></el-progress>
                </template>
                <template v-else>
                  <span class="color-label sub-task-progress"> ({{subTaskProgress}}/{{taskData.childTask.length}}) </span>
                  <el-progress :percentage="0"></el-progress>
                </template>
              </div>
              <template v-if="taskData.childTask.length != 0">
                <div class="card-related-matters subtasks-box"
                     v-for="(item, index) in taskData.childTask"
                     :key="index">
                  <div v-if="!item.showEdit"
                       class="show-edit">
                    <div @click.stop
                         style="display: inline-block;">
                      <el-checkbox v-model="item.checked"
                                   @change="subtasksCheckbox(item, $event)"></el-checkbox>
                    </div>
                    <span :style="{'text-decoration': item.checked ? 'line-through' : 'none'}"
                          class="item-name">{{item.name}}</span>
                    <!-- 编辑和删除 -->
                    <div class="edit-del-box">
                      <i class="wukong wukong-edit-task"
                         @click="editSubTask(item)"></i>
                      <i class="wukong wukong-delete-task"
                         @click="deleteSubTask(item)"></i>
                    </div>
                    <div class="rt">
                      <flexbox class="rt-box">
                        <div class="bg-color task-bg-color"
                             v-if="item.stopTime">{{item.stopTime | moment("MM-DD")}} 截止</div>
                        <div v-if="item.mainUser"
                             v-photo="item.mainUser"
                             :key="item.mainUser.img"
                             v-lazy:background-image="$options.filters.filterUserLazyImg(item.mainUser.img)"
                             class="div-photo"></div>
                      </flexbox>
                    </div>
                  </div>
                  <sub-task v-else
                            :subTaskCom="'edit'"
                            :time="item.stopTime"
                            :subData="item"
                            :text="item.name"
                            :taskId="subTaskID"
                            :checkboxData="item.checked"
                            :taskData="taskData"
                            @on-handle="handleSubTasksBlock($event, item)">
                  </sub-task>
                </div>
              </template>
              <div class="add-subtasks"
                   v-if="addSubtasks"
                   @click="addSubtasks = false">
                <span class="el-icon-plus"></span>
                <span>添加子任务</span>
              </div>
              <sub-task v-else
                        :subTaskCom="'new'"
                        :taskData="taskData"
                        @on-handle="handleSubTasksBlock">
              </sub-task>
            </div>
            <div class="card-content-row card-content-row-column"
                 v-if="fileList.length != 0">
              <span class="color-label participant class-file">
                <i class="wukong wukong-file"></i>
                <span>附件</span>
              </span>
              <div class="accessory-box">
                <file-cell v-for="(file, fileIndex) in fileList"
                           :key="fileIndex"
                           :data="file"
                           :cellIndex="fileIndex"
                           :moduleId="id"
                           :showDelete="true"
                           @delete="accessoryDeleteFun"></file-cell>
              </div>
            </div>
          </div>
        </div>
        <div class="border"></div>
        <!-- 底部内容 -->
        <div class="card-footers">
          <div class="footer-title">
            <span @click="footerTitle(0)">
              <i class="wukong wukong-comment-task"
                 style="color: #3E84E9;"
                 v-if="isComment"></i>
              <i class="wukong wukong-comment-task"
                 v-else></i>
              <span class="cursor-pointer"
                    :style="{'color': isComment ? '#3E84E9' : '#666'}">评论</span>
            </span>
            <span class="title-border"></span>
            <span @click="footerTitle(1)">
              <i class="wukong wukong-activity-task"
                 style="font-size: 18px;"
                 v-if="isComment"></i>
              <i class="wukong wukong-activity-task"
                 style="color: #3E84E9; font-size: 18px;"
                 v-else></i>
              <span class="cursor-pointer"
                    :style="{'color': isComment ? '#666' : '#3E84E9'}"
                    @click="footerTitle(isComment)">活动</span>
            </span>
          </div>
          <div class="footer-content-box"
               v-loading="commentsLoading">
            <div class="footer-content">
              <div v-if="commentsActivities"
                   class="add-comments">
                <div class="footer-img">
                  <div v-photo="userInfo"
                       v-lazy:background-image="$options.filters.filterUserLazyImg(userInfo.img)"
                       :key="userInfo.img"
                       class="div-photo"></div>
                </div>
                <div class="comments-con">
                  <div class="comments-box"
                       v-clickoutside="commentsSubmit"
                       v-if="addComments">
                    <el-input type="textarea"
                              @blur="blurFun"
                              :rows="4"
                              ref="commentsTextareaRef"
                              placeholder="添加评论"
                              v-model="commentsTextarea"> </el-input>
                    <div class="btn-group">
                      <el-popover placement="top"
                                  width="400"
                                  v-model="commentsPopover"
                                  trigger="click">
                        <!-- 表情 -->
                        <emoji @select="selectEmoji">
                        </emoji>
                        <img src="@/assets/img/smiling_face.png"
                             class="smiling-img"
                             slot="reference">
                      </el-popover>
                      <el-button type="primary"
                                 class="rt"
                                 @click="commentsSub">发布</el-button>
                    </div>
                  </div>
                  <div class="footer-bg"
                       v-else
                       @click="addCommentsClick">添加评论</div>

                  <!-- 评论 -->
                  <div class="discuss"
                       v-if="replyList && replyList.length != 0">
                    <div class="discuss-list"
                         v-for="(discussItem, k) in replyList"
                         :key="k">
                      <div v-photo="discussItem.user"
                           v-lazy:background-image="$options.filters.filterUserLazyImg(discussItem.user.img)"
                           :key="discussItem.user.img"
                           class="div-photo head-img header-circle"></div>
                      <span class="name">{{discussItem.user.realname}}</span>
                      <span class="time">{{discussItem.createTime}}</span>
                      <div class="rt">
                        <span @click="discussDelete(discussItem, replyList, k)">删除</span>
                        <span @click="discussBtn(discussItem, -1)">回复</span>
                      </div>

                      <p class="reply-title">
                        <span v-html="emoji(discussItem.content)"></span>
                      </p>

                      <!-- <p class="discuss-content"
                         v-html="emoji(discussItem.reply_content)"></p> -->

                      <div class="children-reply"
                           v-if="discussItem.childCommentList && discussItem.childCommentList.length > 0">
                        <div class="discuss-list"
                             v-for="(childDiscussItem, k) in discussItem.childCommentList"
                             :key="k">
                          <div v-photo="childDiscussItem.user"
                               v-lazy:background-image="$options.filters.filterUserLazyImg(childDiscussItem.user.img)"
                               :key="childDiscussItem.user.img"
                               class="div-photo head-img header-circle"></div>
                          <span class="name">{{childDiscussItem.user.realname}}</span>
                          <span class="time">{{childDiscussItem.createTime}}</span>
                          <div class="rt">
                            <span @click="discussDelete(childDiscussItem, discussItem.childCommentList, k)">删除</span>
                            <span @click="discussBtn(discussItem, k)">回复</span>
                          </div>
                          <p class="reply-title">
                            <template>
                              <span>回复</span>
                              <span class="reply"
                                    v-if="childDiscussItem.replyUser">@{{childDiscussItem.replyUser.realname}}：</span>
                            </template>
                            <span v-html="emoji(childDiscussItem.content)"></span>
                          </p>
                        </div>
                      </div>

                      <!-- 评论 -- 回复  -->
                      <div class="comment-box"
                           v-if="discussItem.show">
                        <el-input type="textarea"
                                  @blur="blurFun"
                                  :rows="2"
                                  ref="childCommentsTextareaRef"
                                  placeholder="请输入内容"
                                  v-model="childCommentsTextarea"></el-input>
                        <div class="btn-group">
                          <el-popover placement="top"
                                      width="400"
                                      v-model="childCommentsPopover"
                                      trigger="click">
                            <!-- 表情 -->
                            <emoji @select="selectEmojiChild">
                            </emoji>
                            <img src="@/assets/img/smiling_face.png"
                                 class="smiling-img"
                                 slot="reference">
                          </el-popover>
                          <div class="btn-box">
                            <el-button type="primary"
                                       @click="childCommentSubmit()"
                                       :loading="contentLoading">回复</el-button>
                            <el-button @click="discussItem.show= false">取消</el-button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else
                   class="activity-box">
                <template v-if="activityList.length != 0">
                  <flexbox v-for="(item, index) in activityList"
                           :key="index"
                           align="stretch"
                           class="activity-list">
                    <div v-photo="item"
                         :key="item.img"
                         v-lazy:background-image="$options.filters.filterUserLazyImg(item.img)"
                         class="div-photo"></div>
                    <div class="activity-name">{{item.realname}}</div>
                    <div class="activity-content">{{item.content}}</div>
                    <div class="activity-time">{{item.createTime}}</div>
                  </flexbox>
                </template>
              </div>
            </div>
          </div>
        </div>
        <slot></slot>
      </div>
      <div class="tip"
           v-if="taskData && taskData.createUser">
        <span>{{taskData.createUser.realname}} 创建于 {{taskData.createTime}}</span>
      </div>

      <c-r-m-full-screen-detail :visible.sync="showRelatedDetail"
                                :crmType="relatedCRMType"
                                :id="relatedID">
      </c-r-m-full-screen-detail>
    </el-card>
  </transition>
</template>

<script type="text/javascript">
import xss from 'xss'
import {
  workTaskSaveAPI,
  taskSvaeTaskRelationAPI,
  workTaskDeleteAPI,
  workTaskArchiveAPI,
  queryCommentListAPI,
  workTaskcommentSaveAPI,
  workTaskcommentDeleteAPI,
  workTaskReadAPI,
  workTaskReadLoglistAPI,
  workTaskRecoverAPI // 激活任务
} from '@/api/projectManagement/task'
import { workWorkOwnerListAPI } from '@/api/projectManagement/project'
import { crmFileSave } from '@/api/common'
import {
  workTrashRecoverAPI,
  workTrashDeleteAPI
} from '@/api/projectManagement/recycle'

import membersDep from '@/components/selectEmployee/membersDep'
import tagIndex from './tag/tagIndex'
import subTask from './subTask'
// emoji
import emoji from '@/components/emoji'
// 关联业务 - 选中列表
import relatedBusiness from '@/components/relatedBusiness'
import XhUser from '@/components/CreateCom/XhUser'
import FileCell from '@/views/OAManagement/components/fileCell'
import CRMFullScreenDetail from '@/views/customermanagement/components/CRMFullScreenDetail'
import { mapGetters } from 'vuex'
import { getMaxIndex, timestampToFormatTime } from '@/utils'

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
  props: {
    id: [String, Number],
    detailIndex: Number,
    detailSection: Number,
    appendToBody: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    ownerListRequest() {
      return workWorkOwnerListAPI
    },
    workId() {
      return this.taskData.workId
    }
  },
  watch: {
    id: function(val) {
      this.initInfo()
      this.getDetail()
      this.getCommentList()
      this.getActivityList()
    }
  },
  mounted() {
    if (this.appendToBody) {
      document.body.appendChild(this.$el)
    }
    if (this.id) {
      this.getDetail()
      this.getCommentList()
      this.getActivityList()
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
      workTaskReadAPI({ taskId: this.id })
        .then(res => {
          let taskData = res.data
          taskData.checked = taskData.status == 5 ? true : false

          if (taskData.childTask) {
            for (let item of taskData.childTask) {
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
        .catch(err => {
          this.loading = false
          this.closeBtn()
        })
    },
    // 获取活动信息
    getActivityList() {
      workTaskReadLoglistAPI({
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
      workTaskSaveAPI({
        taskId: this.id,
        status: this.taskData.checked ? 5 : 1
      })
        .then(res => {
          this.$emit('on-handle', {
            type: 'title-check',
            value: val,
            index: this.detailIndex,
            section: this.detailSection
          })
          this.$store.dispatch('GetOAMessageNum', 'task')
        })
        .catch(err => {
          this.$emit('on-handle', {
            type: 'title-check',
            value: !this.taskData.checked,
            index: this.detailIndex,
            section: this.detailSection
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
      workTaskSaveAPI({
        taskId: this.id,
        priority: value.id
      })
        .then(res => {
          this.$emit('on-handle', {
            type: 'change-priority',
            value: value,
            index: this.detailIndex,
            section: this.detailSection
          })
          this.priorityVisible = false
        })
        .catch(err => {
          this.$message.error('编辑失败')
          this.taskData.priority = def
        })
    },
    // 更多 ———— 删除和规定按钮
    moreDelete() {
      this.$confirm('确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'is-particulars'
      })
        .then(() => {
          workTaskDeleteAPI({
            taskId: this.id
          })
            .then(res => {
              this.$message.success('删除成功')
              this.$emit('on-handle', {
                type: 'delete',
                index: this.detailIndex,
                section: this.detailSection
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
    moreArchive() {
      this.$confirm('此操作将归档该任务, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'is-particulars'
      })
        .then(() => {
          workTaskArchiveAPI({
            taskId: this.id
          })
            .then(res => {
              this.$message.success('操作成功')
              this.$emit('on-handle', {
                type: 'delete',
                index: this.detailIndex,
                section: this.detailSection
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
          res.filePath = res.url
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
      this.addSubtasks = this.addSubtasks ? false : true
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
        index: this.detailIndex,
        section: this.detailSection
      })
      workTaskSaveAPI({
        taskId: val.taskId,
        status: e ? 5 : 1
      })
        .then(res => {})
        .catch(err => {
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
            index: this.detailIndex,
            section: this.detailSection
          })
          // this.$emit('', val, e)
        })
    },
    /**
     * 参与人操作
     */
    // 提交按钮
    editOwnerList(users, dep) {
      workTaskSaveAPI({
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
      workTaskSaveAPI({
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
      workTaskSaveAPI({
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
      workTaskSaveAPI({
        name: val,
        taskId: this.id
      })
        .then(res => {
          this.nameVinput = false
          // this.$emit('saveName', val)
          this.$emit('on-handle', {
            type: 'change-name',
            value: val,
            index: this.detailIndex,
            section: this.detailSection
          })
          this.taskData.name = val
        })
        .catch(() => {})
    },
    // 截至日期API
    endTimeChange(val) {
      workTaskSaveAPI({
        stopTime: val,
        taskId: this.id
      })
        .then(res => {
          this.$emit('on-handle', {
            type: 'change-stop-time',
            value: val,
            index: this.detailIndex,
            section: this.detailSection
          })
        })
        .catch(() => {})
    },
    // 描述提交按钮
    addDescriptionSubmit() {
      workTaskSaveAPI({
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
      let list = this.commentsTextarea.split('')
      list.splice(this.blurIndex, 0, val)
      this.commentsTextarea = list.join('')
      this.commentsPopover = false
    },
    // 评论回复 -- 选中功能
    selectEmojiChild(val) {
      let list = this.childCommentsTextarea.split('')
      list.splice(this.blurIndex, 0, val)
      this.childCommentsTextarea = list.join('')
      this.childCommentsPopover = false
    },

    /**
     * 点击添加回复
     */
    addCommentsClick() {
      this.addComments = true
      this.$nextTick(() => {
        this.$refs.commentsTextareaRef.focus()
      })
    },

    // 评论发布
    commentsSub() {
      if (this.commentsTextarea) {
        this.commentsLoading = true
        workTaskcommentSaveAPI({
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
            this.$emit('on-handle', {
              type: 'change-comments',
              value: 'add',
              index: this.detailIndex,
              section: this.detailSection
            })
            this.commentsLoading = false
          })
          .catch(err => {
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
        workTaskcommentSaveAPI({
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
              index: this.detailIndex,
              section: this.detailSection
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
          workTaskcommentDeleteAPI({
            commentId: val.commentId
          })
            .then(res => {
              items.splice(index, 1)
              this.$emit('on-handle', {
                type: 'change-comments',
                value: 'delete',
                index: this.detailIndex,
                section: this.detailSection
              })
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
        this.replyChildComment = item
        this.replyChildIndex = index
        this.$nextTick(() => {
          this.$refs.childCommentsTextareaRef[0].focus()
        })
      }
    },
    blurFun(eve) {
      this.blurIndex = eve.target.selectionEnd
    },
    // 关联业务提交按钮
    checkInfos(val) {
      taskSvaeTaskRelationAPI({
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
      this.$confirm('此操作将删除该任务, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'is-particulars'
      })
        .then(() => {
          workTaskDeleteAPI({
            taskId: val.taskId
          })
            .then(res => {
              let subData = this.taskData.childTask
              for (let i in subData) {
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
                  index: this.detailIndex,
                  section: this.detailSection
                })
              }
              this.$message.success('子任务删除成功')
            })
            .catch(err => {
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
      let dataList = this.taskData.childTask
      for (let i in dataList) {
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
            index: this.detailIndex,
            section: this.detailSection
          })
        } else {
          this.$emit('on-handle', {
            type: 'change-sub-task',
            value: {
              subdonecount: this.subTaskProgress,
              allcount: this.taskData.childTask.length - 1
            },
            index: this.detailIndex,
            section: this.detailSection
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
      workTaskSaveAPI({
        taskId: this.id,
        stopTime: ''
      })
        .then(res => {
          this.$set(this.taskData, 'stopTime', '')
        })
        .catch(() => {})
    },

    /**
     * 激活任务
     */
    activateTask() {
      this.loading = true
      workTaskRecoverAPI({
        taskId: this.taskData.taskId
      })
        .then(res => {
          this.loading = false
          this.$emit('on-handle', {
            type: 'activate-task',
            index: this.detailIndex,
            section: this.detailSection
          })
          this.closeBtn()
        })
        .catch(err => {
          this.loading = false
        })
    },

    /**
     * 激活任务
     */
    recoverTask() {
      this.loading = true
      workTrashRecoverAPI({
        taskId: this.taskData.taskId
      })
        .then(res => {
          this.loading = false
          this.$message.success('恢复成功')
          this.$emit('on-handle', {
            type: 'recover-task',
            index: this.detailIndex,
            section: this.detailSection
          })
          this.closeBtn()
        })
        .catch(err => {
          this.loading = false
        })
    },

    /**
     * 激活任务
     */
    thoroughDeleteTask() {
      this.$confirm('确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.loading = true
          workTrashDeleteAPI({
            taskId: this.taskData.taskId
          })
            .then(res => {
              this.loading = false
              this.$emit('on-handle', {
                type: 'thorough-delete-task',
                index: this.detailIndex,
                section: this.detailSection
              })
              this.closeBtn()
            })
            .catch(() => {
              this.loading = false
            })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    }
  },

  beforeDestroy() {
    if (this.appendToBody && this.$el && this.$el.parentNode) {
      this.$el.parentNode.removeChild(this.$el)
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

  .work-name {
    position: absolute;
    top: 17px;
    left: 20px;
    font-size: 18px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    width: 300px;
    color: #666;
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
    img {
      vertical-align: middle;
    }
    .el-checkbox {
      height: 20px;
      margin-right: 8px;
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
      padding: 0 15px;
      .card-content-row {
        margin-bottom: 35px;
        display: flex;
        .priority-data-name {
          flex: 1;
          .title-text {
            white-space: pre-wrap;
            word-wrap: break-word;
            line-height: 22px;
            font-size: 17px;
            color: #333;
            .el-icon-edit-outline {
              margin: 0 10px;
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
            flex: 1;
            padding-right: 8px;
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
          width: 200px;
          .text-right {
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
          flex: 1;
          white-space: normal;
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
        .description-content {
          white-space: pre-wrap;
          word-wrap: break-word;
          line-height: 18px;
        }
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
    margin: 20px 0 60px 0;
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
      .footer-content {
        .activity-box {
          margin-left: 35px;
          margin-right: 35px;
          .activity-list {
            margin-bottom: 15px;
            color: #333;
            .activity-time {
              color: #999;
              font-size: 12px;
            }
            .activity-name {
              color: #666;
              margin: 0 8px;
            }
            .activity-content {
              color: #333;
              flex: 1;
              margin: 0 8px;
              white-space: pre-wrap;
              word-wrap: break-word;
              line-height: 18px;
            }
            .div-photo {
              width: 25px;
              height: 25px;
              border-radius: 12.5px;
              margin-right: 10px;
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
      word-wrap: break-word;
      white-space: pre-wrap;
      line-height: 18px;
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

// 归档
.particulars-extraContent {
  margin: -20px -20px 20px -20px;
  background: #eee;
  height: 40px;
  line-height: 40px;
  text-align: center;
  .border-radius {
    border-radius: 11px;
    padding: 5px 15px;
  }
}
</style>

