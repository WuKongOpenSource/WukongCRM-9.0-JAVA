<template>
  <div class="project-settings">
    <el-popover
      v-model="projectSetShow"
      placement="bottom-start"
      width="400"
      popper-class="project-settings-list-top"
      trigger="click">
      <div
        v-loading="loading"
        class="project-settings-box">
        <p class="project-settings-title-top">
          <span>项目设置</span>
          <span
            class="el-icon-close rt"
            @click="close"/>
        </p>
        <div class="content">
          <p class="title-checked">
            <span
              :style="{color: tabType == 'base' ? '#3E84E9': '#333333'}"
              class="span-item"
              @click="tabType = 'base'">基础设置</span>
            <span
              v-if="isOpen == 0"
              :style="{color: tabType == 'member' ? '#3E84E9': '#333333'}"
              class="span-item"
              @click="tabType = 'member'">成员管理</span>
          </p>
          <!-- 基础设置 -->
          <div
            v-show="tabType == 'base'"
            class="infrastructure">
            <div class="row">
              <span class="label name">项目名称</span>
              <div class="color-dynamic">
                <el-input
                  v-model="setTitle"
                  size="mini"/>
                <span
                  :style="{background: setColor}"
                  class="dynamic-span"/>
              </div>
            </div>
            <div class="row">
              <span class="label">图标颜色</span>
              <div class="color-box">
                <span
                  v-for="(item, index) in colorList"
                  :key="index"
                  :style="{background: item}"
                  @click="setColor = item"/>
              </div>
            </div>
            <div class="row">
              <span class="label name">可见范围</span>
              <div class="color-dynamic">
                <el-select
                  v-model="setIsOpen"
                  placeholder="请选择">
                  <el-option
                    v-for="item in openOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"/>
                </el-select>
              </div>
            </div>
          </div>
          <div
            v-show="tabType == 'member'"
            class="add-members">
            <members-dep
              :user-checked-data="membersList"
              :close-dep="true"
              :content-block="false"
              @popoverSubmit="userSelectChange">
              <div
                slot="membersDep"
                class="img-span">
                <img
                  src="@/assets/img/project/project_add.png"
                  alt="">
                <span class="add-title">添加成员</span>
              </div>
            </members-dep>

            <div class="member-section">
              <div
                v-for="(item, index) in membersList"
                :key="index"
                class="member-row">
                <div
                  v-photo="item"
                  v-lazy:background-image="$options.filters.filterUserLazyImg(item.img)"
                  :key="item.img"
                  class="div-photo"/>
                <span class="member-row-name">{{ item.realname }}</span>
                <div class="rt">
                  <el-select
                    v-model="item.roleId"
                    placeholder="请选择"
                    size="mini">
                    <el-option
                      v-for="val in optionList"
                      :key="val.roleId"
                      :label="val.roleName"
                      :value="val.roleId"/>
                  </el-select>
                  <span
                    class="el-icon-close"
                    @click="deleteMember(item, index)"/>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="footer">
          <el-button
            type="primary"
            @click="submite">确定</el-button>
          <el-button @click="close">取消</el-button>
        </div>
      </div>
      <p
        slot="reference"
        class="title"
        @click="projectSetting">项目设置</p>
    </el-popover>
  </div>
</template>

<script>
import {
  workWorkSaveAPI,
  workWorkOwnerDelAPI,
  workWorkAddUserGroupAPI,
  workWorkGroupListAPI
} from '@/api/projectManagement/project'
import MembersDep from '@/components/selectEmployee/membersDep'

export default {
  components: {
    MembersDep
  },

  props: {
    workId: [Number, String],
    title: String,
    color: String,
    isOpen: [Number, String],
    // 人员数据
    addMembersData: Array,
    name: {
      type: Array,
      default: () => {
        return []
      }
    }
  },

  data() {
    return {
      loading: false,
      // 是否显示弹出框
      projectSetShow: false,
      colorList: [
        '#53D397',
        '#20C1BD',
        '#58DADA',
        '#0FC9E7',
        '#3498DB',
        '#4586FF',
        '#8983F3',
        '#AEA1EA',
        '#FF6699',
        '#F24D70',
        '#FF6F6F'
      ],
      tabType: 'base', // base 基础设置 member 成员设置
      // 动态背景
      setColor: '',
      setTitle: '',
      setIsOpen: 0,
      openOptions: [
        {
          value: 0,
          label: '私有：只有加入的成员才能看见此项目'
        },
        {
          value: 1,
          label: '公开：企业所有成员都可以看见此项目'
        }
      ],
      membersList: [],
      // 角色权限
      optionList: []
    }
  },

  watch: {
    addMembersData() {
      this.membersList = this.addMembersData || []
    },
    tabType(val) {
      if (val == 'member' && this.optionList.length == 0) {
        this.getGroupList()
      }
    }
  },

  created() {
    this.membersList = this.addMembersData || []
  },

  beforeDestroy() {},

  methods: {
    /**
     * 获取项目角色列表
     */
    getGroupList() {
      workWorkGroupListAPI()
        .then(res => {
          this.optionList = res.data || []
        })
        .catch(() => {})
    },

    /**
     * 点击项目设置
     */
    projectSetting() {
      this.setColor = this.color
      this.setTitle = this.title
      this.setIsOpen = this.isOpen
      this.tabType = 'base'
      this.$emit('click')
    },

    /**
     * 设置提交 base 基础设置
     */
    submite() {
      if (this.tabType == 'base') {
        this.loading = true
        workWorkSaveAPI({
          name: this.setTitle,
          color: this.setColor,
          isOpen: this.setIsOpen,
          workId: this.workId
        })
          .then(res => {
            this.loading = false
            this.$message.success('操作成功')
            this.$emit('submite', this.setTitle, this.setColor, this.setIsOpen)
            this.close()
          })
          .catch(() => {
            this.loading = false
          })
      } else {
        this.loading = true
        workWorkAddUserGroupAPI({
          list: this.membersList,
          workId: this.workId
        })
          .then(res => {
            this.loading = false
            this.$message.success('操作成功')
            this.close()
          })
          .catch(() => {
            this.loading = false
          })
      }
    },

    /**
     * 关闭弹窗
     */
    close() {
      this.projectSetShow = false
      this.$emit('close')
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
          this.$emit('handle', 'member', res.data)
          this.$message.success('添加成功')
        })
        .catch(() => {})
    },

    /**
     * 删除一个成员
     */
    deleteMember(data, index) {
      workWorkOwnerDelAPI({
        workId: this.workId,
        ownerUserId: data.userId
      }).then(res => {
        this.membersList.splice(index, 1)
        this.$message.success('删除成功')
      }).catch(() => {})
    }
  }
}
</script>

<style scoped lang="scss">
.project-settings {
  .title {
    height: 34px;
    line-height: 34px;
    cursor: pointer;
    padding-left: 32px;
  }
  .title:hover {
    background: #f7f8fa;
    color: #3e84e9;
  }
}
.project-settings-box {
  .project-settings-title-top {
    border-bottom: 1px solid #e6e6e6;
    padding-bottom: 10px;
    font-size: 16px;
    .el-icon-close {
      color: #ccc;
      margin-right: 0;
    }
  }
  .content {
    padding-top: 10px;
    .title-checked {
      text-align: center;
      margin: 5px 0;
      span {
        padding: 0 10px;
        font-size: 13px;
        cursor: pointer;
      }
      .span-item + .span-item {
        border-left: 1px solid #e6e6e6;
      }
    }
    .infrastructure {
      .row {
        display: flex;
        flex-direction: row;
        margin-top: 10px;
        .el-textarea {
          flex: 1;
        }
        .label {
          margin-right: 10px;
          font-size: 13px;
        }
        .color-box {
          span {
            display: inline-block;
            width: 15px;
            height: 15px;
            border-radius: 50%;
            margin-right: 7px;
            cursor: pointer;
          }
        }
        .name {
          line-height: 28px;
        }
        .color-dynamic {
          flex: 1;
          position: relative;
          .dynamic-span {
            position: absolute;
            left: 10px;
            top: 6px;
            display: inline-block;
            width: 15px;
            height: 15px;
            border-radius: 50%;
          }
          .el-input /deep/ .el-input__inner {
            padding-left: 30px;
          }

          .el-select {
            width: 100%;
          }
        }
      }
    }
    .add-members {
      .rt {
        margin-right: 0;
        .el-icon-close {
          margin-left: 10px;
          color: #cccccc;
          font-size: 16px;
        }
      }
      .img-span {
        margin-bottom: 15px;
        margin-left: 10px;
        display: inline-block;
        cursor: pointer;
        img {
          width: 16px;
          vertical-align: middle;
        }
        .add-title {
          color: #3e84e9;
          vertical-align: middle;
        }
      }

      .member-section {
        max-height: 300px;
        overflow-y: auto;
        .member-row {
          position: relative;
          margin-bottom: 10px;
          padding: 0 10px;
          .member-row-name {
            display: inline-block;
            width: 70px;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
            vertical-align: middle;
          }
          .div-photo {
            width: 24px;
            height: 24px;
            border-radius: 12px;
            vertical-align: middle;
            margin-right: 5px;
          }
          .el-icon-close {
            cursor: pointer;
          }
        }
      }
    }
  }
  .footer {
    margin-top: 10px;
    text-align: right;
  }
}
</style>

