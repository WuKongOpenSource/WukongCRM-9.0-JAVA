<template>
  <el-popover ref="popover"
              :placement="placement"
              :width="popoverWidth"
              v-model="popoverVisible"
              popper-class="project-com-popover"
              trigger="click">
    <div v-if="popoverContentShow">
      <div class="title-icon">
        <span>{{title}}</span>
        <span class="el-icon-close rt"
              @click="popoverVisible = false"></span>
      </div>
      <div class="popover-content-box">
        <div class="select-input">
          <!-- 搜索员工列表 -->
          <el-tabs v-model="activeTabName"
                   :stretch="true"
                   @tab-click="tabClick">
            <el-tab-pane label="员工"
                         name="user"
                         v-if="!closeUser"
                         v-loading="userLoading">
              <el-input placeholder="搜索员工"
                        size="mini"
                        prefix-icon="el-icon-search"
                        v-model="searchUserInput"
                        @input="userSearchChange">
              </el-input>
              <div class="search-list">
                <div v-for="(user, index) in userList"
                     :key="index"
                     class="colleagues-list"
                     v-if="!user.hidden">
                  <el-checkbox v-model="user.isCheck"
                               @change="userCheckboxChange(user, index)">
                    <div v-photo="user"
                         v-lazy:background-image="$options.filters.filterUserLazyImg(user.img)"
                         class="div-photo search-img header-circle"></div>
                    <span>{{user.realname}}</span>
                  </el-checkbox>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="部门"
                         name="dep"
                         v-if="!closeDep"
                         v-loading="depLoading">
              <el-input placeholder="搜索部门"
                        size="mini"
                        style="margin-bottom:10px;"
                        prefix-icon="el-icon-search"
                        v-model="searchDepInput"
                        @input="depSearchChange">
              </el-input>
              <div class="search-list">
                <el-breadcrumb separator-class="el-icon-arrow-right">
                  <el-breadcrumb-item v-for="(item, index) in breadcrumbList"
                                      :key="index">
                    <a href="javascript:;"
                       @click="breadcrumbBtn(item, index)">{{item.label}}</a>
                  </el-breadcrumb-item>
                </el-breadcrumb>
                <div v-for="(depItem, index) in depShowList"
                     :key="index"
                     v-if="!depItem.hidden"
                     class="checkout-box">
                  <el-checkbox v-model="depItem.isCheck"
                               @change="depCheckboxChange(depItem, index)"></el-checkbox>
                  <div @click="enterDepChildren(depItem)">
                    <span>{{depItem.name}}</span>
                    <span class="el-icon-arrow-right"
                          v-if="depItem.children"></span>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
        <div class="checked-content">
          <div class="checked-top">
            <span class="title">已选择</span>
            <span v-if="!closeUser"
                  class="title">员工 ({{userSelectCount}})</span>
            <span v-if="!closeDep"
                  class="title">部门 ({{depSelectCount}})</span>
            <el-button type="text"
                       class="rt"
                       @click="emptyClick">清空</el-button>
          </div>
          <div class="border-content">
            <div v-for="(item, index) in checkedUserDepList"
                 :key="index"
                 class="checked-list">
              <div v-if="item.type == 'user'"
                   v-photo="item"
                   v-lazy:background-image="$options.filters.filterUserLazyImg(item.img)"
                   class="div-photo"></div>
              <span v-if="item.type == 'user'"> {{item.realname}} </span>
              <span v-else> {{item.name}} </span>
              <span class="rt el-icon-close"
                    @click="selectDelect(item, index)"></span>
            </div>
          </div>
        </div>
      </div>
      <div class="popover-footer">
        <el-button type="primary"
                   @click="popoverSubmit">确 定</el-button>
        <el-button @click="popoverVisible = false">取 消</el-button>
      </div>
    </div>
    <div :style="{'display':contentBlock ? 'block' : 'inline-block'}"
         slot="reference"
         @click="showContentClick">
      <slot name="membersDep"></slot>
    </div>
  </el-popover>

</template>
<script>
import { usersList, depList } from '@/api/common'
export default {
  data() {
    return {
      activeTabName: 'user',
      // 筛选
      searchUserInput: '',
      searchDepInput: '',
      popoverVisible: false,
      // 内容展示
      popoverContentShow: false,
      // 加载动画
      userLoading: false,
      depLoading: false,
      // 所有用户
      userList: [],
      // 展示客户
      depShowList: [],
      // 面包屑数据
      breadcrumbList: [],
      // 选中的数据 -- 员工和部门一个数组用于页面展示
      checkedUserDepList: []
    }
  },
  watch: {
    userCheckedData: function() {
      this.updateCheckInfoByWatch()
    },
    depCheckedData: function() {
      this.updateCheckInfoByWatch()
    },
    popoverVisible: function(val) {
      if (val) {
        this.updateCheckInfoByWatch()
      }
    }
  },
  computed: {
    userSelectCount() {
      return this.checkedUserDepList.filter(item => {
        return item.type == 'user'
      }).length
    },
    depSelectCount() {
      return this.checkedUserDepList.filter(item => {
        return item.type == 'dep'
      }).length
    }
  },
  props: {
    // 弹出框宽度
    popoverWidth: {
      type: String,
      default: '600'
    },
    // 标题
    title: {
      type: String,
      default: '选择成员'
    },
    // 显示位置
    placement: {
      type: String,
      default: 'bottom-start'
    },
    // 内容框类型
    contentBlock: {
      type: Boolean,
      default: true
    },
    // 编辑时 -- 用户默认勾选的数据
    userCheckedData: {
      type: Array,
      default: () => {
        return []
      }
    },
    // 编辑时 -- 部门默认勾选的数据
    depCheckedData: {
      type: Array,
      default: () => {
        return []
      }
    },
    // 是否关闭某个类的选择
    closeUser: {
      type: Boolean,
      default: false
    },
    closeDep: {
      type: Boolean,
      default: false
    },
    userRequest: Function,
    userParams: Object
  },
  methods: {
    initInfo() {
      // 用户列表
      this.checkedUserDepList = this.userCheckedData
        .map(item => {
          item.type = 'user'
          return item
        })
        .concat(
          this.depCheckedData.map(item => {
            item.type = 'dep'
            return item
          })
        )

      if (!this.closeUser) {
        this.getUserList()
      } else if (!this.closeDep) {
        this.activeTabName = 'dep'
        this.getDepList()
      }
    },
    tabClick() {
      if (this.activeTabName == 'dep') {
        if (this.depShowList.length == 0) {
          this.getDepList()
        }
      }
    },
    userSearchChange(val) {
      this.userList = this.userList.map(item => {
        if (item.realname.indexOf(val) != -1) {
          item.hidden = false
        } else {
          item.hidden = true
        }
        return item
      })
    },
    depSearchChange(val) {
      this.depShowList = this.depShowList.map(item => {
        if (item.name.indexOf(val) != -1) {
          item.hidden = false
        } else {
          item.hidden = true
        }
        return item
      })
    },
    /**
     * 部门信息
     */
    // 部门列表数据
    getDepList() {
      this.depLoading = true
      depList({ type: 'tree' })
        .then(res => {
          this.depShowList = res.data.map((item, index, array) => {
            item.type = 'dep'
            item.isCheck = this.getItemCheckInfo(item, 'dep')
            return item
          })
          this.breadcrumbList.push({ label: '全部', data: this.depShowList })
          this.depLoading = false
        })
        .catch(() => {
          this.depLoading = false
        })
    },
    // 部门下一级
    enterDepChildren(depItem) {
      if (depItem.children) {
        this.depShowList = []
        this.depShowList = depItem.children.map((item, index, array) => {
          item.type = 'dep'
          if (item.name.indexOf(this.searchDepInput) != -1) {
            item.hidden = false
          } else {
            item.hidden = true
          }
          item.isCheck = this.getItemCheckInfo(item, 'dep')
          return item
        })
        this.breadcrumbList.push({
          label: depItem.label,
          data: this.depShowList
        })
      }
    },
    // 面包屑点击事件
    breadcrumbBtn(item, index) {
      if (index + 1 <= this.breadcrumbList.length - 1) {
        this.breadcrumbList.splice(index + 1, this.breadcrumbList.length - 1)
      }
      this.depShowList = []
      this.depShowList = item.data.map((item, index, array) => {
        if (item.name.indexOf(this.searchDepInput) != -1) {
          item.hidden = false
        } else {
          item.hidden = true
        }
        item.isCheck = this.getItemCheckInfo(item, 'dep')
        return item
      })
    },
    // 部门勾选
    depCheckboxChange(item, aindex) {
      this.$set(this.depShowList, aindex, item)
      this.updateCheckedUserDepListByCheck(item, 'dep')
    },
    /**
     * 员工操作
     */
    getUserList() {
      this.userLoading = true
      let request = usersList
      let params = {}
      if (this.userRequest) {
        request = this.userRequest
        params = this.userParams || {}
      } else {
        params = { pageType: 0 }
      }
      request(params)
        .then(res => {
          this.userList = res.data.map(item => {
            item.type = 'user'
            item.isCheck = this.getItemCheckInfo(item, 'user')
            return item
          })
          this.userLoading = false
        })
        .catch(() => {
          this.userLoading = false
        })
    },
    // 员工勾选
    userCheckboxChange(item, aindex) {
      this.$set(this.userList, aindex, item)
      this.updateCheckedUserDepListByCheck(item, 'user')
    },
    // check 操作后的 存储数据刷新
    updateCheckedUserDepListByCheck(item, type) {
      var removeIndex = -1
      for (let index = 0; index < this.checkedUserDepList.length; index++) {
        const element = this.checkedUserDepList[index]
        if (element.type == 'user' && item.userId == element.userId) {
          removeIndex = index
        } else if (element.type == 'dep' && item.id == element.id) {
          removeIndex = index
        }
      }
      if (removeIndex == -1) {
        this.checkedUserDepList.push(item)
      } else if (removeIndex != -1) {
        this.checkedUserDepList.splice(removeIndex, 1)
      }
    },
    // 获取事项标记信息
    getItemCheckInfo(item, type) {
      if (this.checkedUserDepList.length == 0) {
        return false
      }
      var hasItem = false
      for (let index = 0; index < this.checkedUserDepList.length; index++) {
        const element = this.checkedUserDepList[index]
        if (element.type == 'user' && item.userId == element.userId) {
          hasItem = true
          break
        } else if (element.type == 'dep' && item.id == element.id) {
          hasItem = true
          break
        }
      }
      return hasItem
    },
    /**
     * 删除 清空 等操作
     */
    // 删除一个选择员工或部门
    selectDelect(selectItem, index) {
      this.checkedUserDepList.splice(index, 1)
      if (selectItem.type == 'dep') {
        this.depShowList = this.depShowList.map((item, index, array) => {
          item.isCheck = this.getItemCheckInfo(item, 'dep')
          return item
        })
      } else {
        this.userList = this.userList.map((item, index, array) => {
          item.isCheck = this.getItemCheckInfo(item, 'user')
          return item
        })
      }
    },
    // 提交按钮
    popoverSubmit() {
      this.popoverVisible = false
      this.$emit(
        'popoverSubmit',
        this.checkedUserDepList.filter(item => {
          return item.type == 'user'
        }),
        this.checkedUserDepList.filter(item => {
          return item.type == 'dep'
        })
      )
    },
    // 清空按钮
    emptyClick() {
      this.checkedUserDepList = []
      for (let index = 0; index < this.userList.length; index++) {
        this.userList[index].isCheck = false
      }

      for (let index = 0; index < this.depShowList.length; index++) {
        this.depShowList[index].isCheck = false
      }
    },
    // 内容可见
    showContentClick() {
      if (!this.popoverContentShow) {
        this.popoverContentShow = true
        this.initInfo()
      }
    },
    // 监听父类改变更新check
    updateCheckInfoByWatch() {
      this.checkedUserDepList = this.userCheckedData
        .map(item => {
          item.type = 'user'
          return item
        })
        .concat(
          this.depCheckedData.map(item => {
            item.type = 'dep'
            return item
          })
        )
      this.userList = this.userList.map(item => {
        item.isCheck = this.getItemCheckInfo(item, 'user')
        return item
      })
      this.depShowList = this.depShowList.map((item, index, array) => {
        item.isCheck = this.getItemCheckInfo(item, 'dep')
        return item
      })
    }
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/mixin.scss';
.popover-content-box {
  display: flex;
  .select-input {
    flex: 1;
    margin-right: 30px;
    .select-input > .el-input {
      margin: 10px 0;
    }
  }
  .select-input /deep/ .el-tabs {
    .el-tabs__active-bar {
      display: none;
    }
    .el-tabs__nav-wrap::after {
      height: 0;
    }
    .el-tabs__content {
      border: 1px solid #e6e6e6;
      height: 300px;
      .el-checkbox {
        margin-left: 0;
        margin-right: 10px;
      }
    }
    .el-breadcrumb {
      margin-bottom: 15px;
    }
    .checkout-box {
      display: flex;
      margin-bottom: 10px;
    }
    .checkout-box > div {
      flex: 1;
      .el-icon-arrow-right {
        float: right;
      }
      span {
        cursor: pointer;
      }
    }
    .el-tab-pane {
      padding: 10px;
      height: 100%;
    }
    .el-tab-pane > .el-input {
      width: 90%;
      margin: auto 5%;
      .el-input__inner {
        border-radius: 15px;
      }
    }
  }
  .checked-content {
    flex: 1;
    .checked-top {
      height: 40px;
      line-height: 40px;
      margin-bottom: 15px;
      .title {
        color: #999999;
      }
    }
    .border-content {
      border: 1px solid #e6e6e6;
      height: 300px;
      overflow: auto;
      padding: 20px 0;
      @include scrollBar;
      .checked-list {
        height: 30px;
        line-height: 30px;
        padding: 0 20px;
        cursor: pointer;
        .el-icon-close {
          opacity: 0;
          margin-top: 8px;
          margin-right: 0;
        }
        .div-photo {
          width: 24px;
          height: 24px;
          border-radius: 12px;
          vertical-align: middle;
          margin-right: 8px;
        }
      }
      .checked-list:hover {
        -webkit-box-shadow: 0 0 8px 2px #eee;
        box-shadow: 0 0 8px 2px #eee;
        .el-icon-close {
          opacity: 1;
        }
      }
    }
  }
}
.title-icon {
  padding: 10px 20px 15px;
  border-bottom: 1px solid #e6e6e6;
  margin-bottom: 10px;
  font-size: 16px;
  .el-icon-close {
    font-size: 20px;
    color: #ccc;
    margin-right: 0;
  }
}
.popover-footer {
  float: right;
  margin: 20px 0 0;
}
/* 选择员工 */
.search-img {
  width: 24px;
  height: 24px;
  border-radius: 12px;
  vertical-align: middle;
  margin-right: 8px;
}
.search-list {
  margin: 5px;
  height: 248px;
  overflow: auto;
  margin-right: -10px;
  padding-right: 10px;
  @include scrollBar;
}
.colleagues-list {
  padding: 5px;
}
</style>

