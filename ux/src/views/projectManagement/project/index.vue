<template>
  <div
    class="project-list"
    direction="column">
    <div class="nav-box">
      <div class="title">
        <flexbox class="title-left lt">
          <i
            :style="{color : projectColor ? projectColor : '#4AB8B8'}"
            class="wukong wukong-subproject"/>
          <span>{{ projectName }}</span>
          <el-popover
            v-model="projectHandleShow"
            placement="bottom-start"
            popper-class="project-settings-182"
            width="182">
            <div class="project-list-popover-btn-list">
              <members-dep
                :user-checked-data="membersList"
                :close-dep="true"
                @popoverSubmit="userSelectChange">
                <p
                  v-if="canUpdateWork && projectData.isOpen != 1"
                  slot="membersDep"
                  @click="projectHandleShow = false">添加项目成员</p>
              </members-dep>

              <project-settings
                v-if="canUpdateWork"
                :work-id="workId"
                :title="projectName"
                :color="projectColor"
                :is-open="projectData.isOpen"
                :add-members-data="membersList"
                @close="projectHandleShow = false"
                @submite="setSubmite"
                @handle="projectSettingsHandle"
                @click="projectHandleShow = false"/>
              <p
                v-if="canUpdateWork"
                @click="archiveProject">归档项目</p>
              <p
                v-if="canUpdateWork"
                @click="deleteProject">删除项目</p>
              <p v-if="projectData.isOpen == 0" @click="exitProject">退出项目</p>
            </div>
            <img
              slot="reference"
              src="@/assets/img/project/t_set.png"
              class="img-right">
          </el-popover>
        </flexbox>
        <div class="title-right rt">
          <!-- 人员列表 -->
          <img
            src="@/assets/img/project/task_circle.png"
            alt=""
            @click="membersShow = true">
          <!-- 筛选 -->
          <img
            v-show="screeningButtonShow"
            src="@/assets/img/project/project_filtrate.png"
            alt=""
            @click="screeningShow = true">
        </div>
      </div>
      <div class="nav">
        <el-tabs
          v-model="activeName"
          @tab-click="tabClick">
          <el-tab-pane
            label="任务板"
            name="task-board"/>
          <el-tab-pane
            label="附件"
            name="attachment"/>
          <el-tab-pane
            label="任务统计"
            name="task-statistical"/>
          <el-tab-pane
            label="归档任务"
            name="archiving-task"/>
        </el-tabs>
      </div>
    </div>
    <div class="content">
      <keep-alive>
        <component
          :is="activeName"
          :work-id="workId"
          :permission="permission"/>
      </keep-alive>
    </div>

    <!-- 筛选 -->
    <task-screening
      v-if="screeningShow"
      :work-id="workId"
      @close="screeningShow = false"/>

    <!-- 人员列表 -->
    <members
      :work-id="workId"
      :list="membersList"
      :is-open="projectData.isOpen"
      :permission="permission"
      :visible.sync="membersShow"
      @handle="membersHandle"/>
  </div>
</template>

<script>
import TaskBoard from './components/taskBoard'
import Attachment from './components/attachment'
import TaskStatistical from './components/taskStatistical'
import ArchivingTask from './components/archivingTask'
import ProjectSettings from './components/projectSettings'
import TaskScreening from './components/taskScreening'
import Members from './components/members'
import MembersDep from '@/components/selectEmployee/membersDep'

import {
  workWorkReadAPI,
  workWorkDeleteAPI,
  workWorkLeaveAPI,
  workWorkOwnerListAPI,
  workWorkSaveAPI
} from '@/api/projectManagement/project'

export default {
  components: {
    TaskBoard,
    Attachment,
    TaskStatistical,
    ArchivingTask,
    ProjectSettings,
    TaskScreening,
    Members,
    MembersDep
  },

  data() {
    return {
      // 项目ID
      workId: '',
      projectName: '',
      projectColor: '',
      projectData: {
        isOpen: 0
      },

      activeName: 'task-board',
      // 项目设置
      projectHandleShow: false,

      // 人员列表
      membersShow: false,
      membersList: [],
      // 是否显示筛选
      screeningButtonShow: true,
      screeningShow: false,

      // 权限
      permission: {}
    }
  },

  computed: {
    /**
     * 可以编辑项目
     */
    canUpdateWork() {
      return this.permission.work && this.permission.work.update
    }
  },

  beforeRouteUpdate(to, from, next) {
    this.workId = to.params.id
    this.membersShow = false
    this.screeningShow = false
    this.getDetail()
    this.getMemberList()
    next()
  },

  created() {
    this.activeName = 'task-board'
    // 当页面刷新时重新获取路由信息
    this.workId = this.$route.params.id
    this.getDetail()
    this.getMemberList()
  },

  methods: {
    /**
     * 获取项目详情
     */
    getDetail() {
      workWorkReadAPI({
        workId: this.workId
      })
        .then(res => {
          const data = res.data
          this.projectData = data
          this.projectColor = data.color
          this.projectName = data.name

          this.permission = data.authList.work || {}
        })
        .catch(() => {})
    },

    tabClick(val) {
      this.screeningButtonShow = this.activeName == 'task-board'
    },

    /**
     * 获取列表
     */
    getMemberList() {
      workWorkOwnerListAPI({
        workId: this.workId
      })
        .then(res => {
          this.membersList = res.data || []
          this.$bus.$emit('members-update', this.membersList)
        })
        .catch(() => {})
    },

    /**
     * 编辑成员
     */
    userSelectChange(members, dep) {
      workWorkSaveAPI({
        workId: this.workId,
        ownerUserId: members
          .map(item => {
            return item.userId
          })
          .join(',')
      })
        .then(res => {
          this.membersList = res.data
          this.$bus.$emit('members-update', res.data)
          this.$message.success('添加成功')
        })
        .catch(() => {})
    },

    /**
     * 删除项目
     */
    deleteProject() {
      this.$confirm(
        '确定要删除项目吗？删除后此项目中的所有任务将一并彻底删除，无法恢复',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
        .then(() => {
          workWorkDeleteAPI({ workId: this.workId })
            .then(res => {
              this.$message({
                type: 'success',
                message: '删除成功!'
              })
              this.$bus.$emit('delete-project', this.workId)
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

    /**
     * 退出项目
     */
    exitProject() {
      this.$confirm('确认退出' + ' "' + this.projectName + '"', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          workWorkLeaveAPI({ workId: this.workId })
            .then(res => {
              this.$message({
                type: 'success',
                message: '退出成功!'
              })
              this.$bus.$emit('delete-project', this.workId)
            })
            .catch(() => {})
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '取消操作'
          })
        })
    },

    /**
     * 归档项目
     */
    archiveProject() {
      this.$confirm('确认归档项目' + ' "' + this.projectName + '"', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          workWorkSaveAPI({ workId: this.workId, status: 3 }) // 状态 1启用 2 删除 3归档
            .then(res => {
              this.$message({
                type: 'success',
                message: '归档成功'
              })
              this.$bus.$emit('delete-project', this.workId)
            })
            .catch(() => {})
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '取消操作'
          })
        })
    },

    /**
     * 项目设置更新数据
     */
    setSubmite(name, color, isOpen) {
      if (this.projectData.isOpen != isOpen) {
        this.getDetail()
        this.getMemberList()
      } else {
        this.projectColor = color
        this.projectName = name
        this.$bus.$emit('project-setting', name, this.workId)
      }
    },

    /**
     * 项目设置
     */
    projectSettingsHandle(type, data) {
      if (type == 'member') {
        this.membersList = data
      }
    },

    /**
     * 成员设置
     */
    membersHandle(type, data) {
      if (type == 'member') {
        this.$bus.$emit('members-update', data)
        this.membersList = data
      }
    }
  }
}
</script>

<style scoped lang="scss">
.project-list {
  height: 100%;
  overflow: hidden;
  .nav-box {
    margin-bottom: 15px;
    background: #fff;
    border-radius: 4px;
    .title {
      font-size: 16px;
      height: 50px;
      line-height: 50px;
      .title-left {
        width: auto;
        img {
          width: 17px;
          vertical-align: unset;
          margin-right: 5px;
        }
        .img-right {
          margin-left: 15px;
          width: 13px;
          cursor: pointer;
        }
      }
      .title-right {
        img {
          margin-right: 25px;
          cursor: pointer;
          vertical-align: middle;
        }
      }
    }
    .nav {
      margin-left: 64px;
      .el-tabs /deep/ .el-tabs__header {
        margin-bottom: 0;
        .el-tabs__nav-wrap::after {
          height: 0;
        }
      }
    }
  }
  .content {
    height: calc(100% - 105px);
    overflow-y: auto;
    position: relative;
  }
}
// 设置
.project-list-popover-btn-list {
  margin: 0 -12px;
  p {
    height: 34px;
    line-height: 34px;
    cursor: pointer;
    padding-left: 32px;
  }
  p:hover {
    background: #f7f8fa;
    color: #3e84e9;
  }
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

// 项目图
.wukong-subproject {
  font-size: 27px;
  margin-right: 8px;
}
</style>
