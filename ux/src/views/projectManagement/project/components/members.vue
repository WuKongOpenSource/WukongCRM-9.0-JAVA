<template>
  <transition name="slide-fade">
    <el-card
      v-loading="loading"
      v-show="visible"
      :style="{ 'z-index': zIndex }"
      class="add-members-project-screening">
      <div class="header">
        <span class="label">成员<span class="color-a">（{{ userList.length }}）</span></span>
        <i
          class="rt el-icon-close"
          @click="close"/>
      </div>
      <div class="content">
        <members-dep
          v-if="canUpdateWork"
          :user-checked-data="userList"
          :close-dep="true"
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
        <div
          v-for="(item, index) in userList"
          :key="index"
          class="item-list">
          <div
            v-photo="item"
            v-lazy:background-image="$options.filters.filterUserLazyImg(item.img)"
            :key="item.img"
            class="div-photo"/>
          <span>{{ item.realname }}
          </span>
          <i
            v-if="canUpdateWork"
            class="el-icon-close"
            @click="deleteUser(item, index)"/>
        </div>
      </div>
    </el-card>
  </transition>
</template>

<script>
// API
import {
  workWorkOwnerDelAPI,
  workWorkSaveAPI
} from '@/api/projectManagement/project'
import MembersDep from '@/components/selectEmployee/membersDep'
import { getMaxIndex } from '@/utils/index'

export default {
  name: 'Members', // 项目成员
  components: {
    MembersDep
  },

  props: {
    visible: {
      type: Boolean,
      default: false
    },
    workId: [Number, String],
    list: Array,
    isOpen: [String, Number],
    permission: {
      type: Object,
      default: () => {
        return {
        }
      }
    }
  },

  data() {
    return {
      zIndex: getMaxIndex(),
      loading: false,
      userList: []
    }
  },

  computed: {
    /**
     * 可以编辑项目
     */
    canUpdateWork() {
      return this.isOpen != 1 && this.permission.work && this.permission.work.update
    }
  },

  watch: {
    visible(val) {
      if (val) {
        this.userList = this.list || []
      } else {
        this.$emit('close')
      }
    },

    userList() {
      this.$bus.$emit('members-update', this.userList)
    }
  },

  mounted() {
    document
      .getElementById('app')
      .addEventListener('click', this.taskShowHandle, false)
  },

  methods: {
    /**
     * 编辑成员
     */
    userSelectChange(members, dep) {
      this.loading = true
      workWorkSaveAPI({
        workId: this.workId,
        ownerUserId: members.map(item => {
          return item.userId
        }).join(',')
      })
        .then(res => {
          this.loading = false
          this.userList = res.data
          this.$emit('handle', 'member', res.data)
          this.$message.success('添加成功')
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * 删除员工
     */
    deleteUser(val, index) {
      this.$confirm('确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          workWorkOwnerDelAPI({
            workId: this.workId,
            ownerUserId: val.userId
          }).then(res => {
            this.userList.splice(index, 1)
            this.$message.success('删除成功')
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    },

    /**
     * 关闭
     */
    close() {
      this.$emit('update:visible', false)
      this.$emit('close')
    },

    /**
     * 点击空白处关闭详情
     */
    taskShowHandle(e) {
      if (this.visible && !this.$el.contains(e.target)) {
        this.close()
      }
    }
  }
}
</script>


<style scoped lang="scss">
.add-members-project-screening /deep/ .el-card__body {
  padding: 0;
}
.add-members-project-screening {
  position: fixed;
  top: 60px;
  bottom: 0;
  right: 0;
  width: 300px;
  overflow: auto;
  .header {
    border-bottom: 1px solid #e6e6e6;
    margin-bottom: 10px;
    padding: 16px;
    .label {
      font-size: 16px;
      margin-right: 10px;
    }
    .el-icon-close {
      color: #ccc;
      font-size: 18px;
      margin-right: 0;
      cursor: pointer;
    }
    .color-a {
      color: #aaa;
      margin-left: 5px;
      font-size: 13px;
    }
  }
  .content {
    font-size: 13px;
    padding: 16px;
    .item-list {
      position: relative;
      height: 36px;
      line-height: 36px;
      margin: 5px 0;
      .el-icon-close {
        font-size: 14px;
        color: #ccc;
        position: absolute;
        top: 11px;
        right: 8px;
        opacity: 0;
      }
      .div-photo {
        width: 24px;
        height: 24px;
        border-radius: 12px;
        vertical-align: middle;
        margin-right: 5px;
      }
    }
    .item-list:hover {
      background: #f7f8fa;
      cursor: pointer;
      .el-icon-close {
        opacity: 1;
      }
    }
  }
}

// 添加员工
.img-span {
  margin-bottom: 20px;
  cursor: pointer;
  img {
    width: 16px;
    vertical-align: middle;
  }
  .add-title {
    font-size: 13px;
    color: #3e84e9;
    vertical-align: middle;
  }
}
</style>
