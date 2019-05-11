<template>
  <slide-view class="d-view"
              :listenerIDs="['manager-main-container']"
              :noListenerIDs="['depTable']"
              @side-close="hideView"
              :body-style="{padding: '10px 30px', height: '100%'}">
    <flexbox orient="vertical"
             style="height: 100%;">
      <flexbox class="detail-header">
        <div class="header-name"></div>
        <img @click="hideView"
             class="header-close"
             src="@/assets/img/task_close.png" />
      </flexbox>
      <div class="detail-body">
        <div class="dialog-top">
          <img :src="data.img"
               alt="">
          <span>{{data.realname}}</span>
          <div class="dialog-btn-group">
            <el-button type="primary"
                       size="medium"
                       @click="editBtn"> 编 辑 </el-button>
            <el-dropdown trigger="click"
                         @command="handleCommand">
              <el-button size="medium">
                更 多<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="reset">重置密码</el-dropdown-item>
                <el-dropdown-item command="status">{{data.status === 0 ? '激 活' : '禁 用'}}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
          <div class="dialog-remark">
            <p>账号状态：{{{'0':'禁用','1':'激活','2':'未激活' }[data.status]}}</p>
            <p>创建时间：{{data.createTime}}</p>
          </div>
        </div>
        <div class="dialog-content">
          <flexbox v-for="(item, index) in detailList"
                   :key="index"
                   class="content-items"
                   align="stretch">
            <div class="content-items-name">{{item.value}}</div>
            <div class="content-items-value">{{data|formatedInfo(item.field)}}</div>
          </flexbox>
        </div>
      </div>
    </flexbox>
  </slide-view>
</template>

<script>
import SlideView from '@/components/SlideView'

export default {
  /** 审批详情 */
  name: 'employee-detail',
  components: {
    SlideView
  },
  props: {
    // 详情信息
    data: Object
  },
  filters: {
    formatedInfo(data, field) {
      if (field == 'sex') {
        return { 1: '男', 2: '女' }[data.sex]
      }
      return data[field]
    }
  },
  watch: {},
  data() {
    return {
      detailList: [
        { field: 'username', value: '手机号（登录名）' },
        { field: 'realname', value: '姓名' },
        { field: 'sex', value: '性别', type: 'select' },
        { field: 'email', value: '邮箱' },
        { field: 'deptName', value: '部门', type: 'select' },
        { field: 'post', value: '岗位' },
        { field: 'parentName', value: '直属上级', type: 'select' },
        { field: 'roleName', value: '角色', type: 'selectCheckout' }
      ]
    }
  },
  computed: {},
  mounted() {},
  methods: {
    editBtn() {
      this.$emit('edit')
    },
    handleCommand(command) {
      this.$emit('command', command)
    },
    //** 点击关闭按钮隐藏视图 */
    hideView() {
      this.$emit('hide-view')
    }
  }
}
</script>

<style lang="scss" scoped>
.detail-header {
  position: relative;
  min-height: 60px;
  .header-name {
    font-size: 14px;
    color: #333333;
    flex: 1;
  }
  .header-close {
    display: block;
    width: 40px;
    height: 40px;
    margin-left: 20px;
    padding: 10px;
    cursor: pointer;
  }
}

.dialog-top > img {
  vertical-align: middle;
  margin-right: 10px;
  height: 36px;
}
.dialog-btn-group {
  float: right;
}
.dialog-remark {
  font-size: 14px;
  color: #999;
  margin-top: 10px;
  p + p {
    margin-top: 5px;
  }
}
.dialog-content {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e6e6e6;
  .content-items {
    padding: 10px 0;
    .content-items-name {
      width: 132px;
      color: #777;
      flex-shrink: 0;
    }
    .content-items-value {
      flex: 1;
    }
  }
}

.detail-body {
  flex: 1;
  overflow-y: auto;
  width: 100%;
}

.d-view {
  position: fixed;
  width: 500px;
  top: 60px;
  bottom: 0px;
  right: 0px;
}
</style>

