<template>
  <div class="edit-index">
    <el-popover :placement="placement"
                :width="popoverWidth"
                v-model="tagShow"
                trigger="click">
      <div v-if="tagContent == 0"
           class="tag-popover-box">
        <div class="tag-top">
          <span>选择标签</span>
          <span class="el-icon-close rt cursor-pointer"
                @click="tagShow = false"></span>
        </div>
        <el-input placeholder="搜索标签"
                  prefix-icon="el-icon-search"
                  size="small"
                  v-model="tagInputChange">
        </el-input>
        <div class="tag-content">
          <div v-for="(item, index) in particularsTagList"
               :key="index"
               class="tag-list"
               @click="tagBtn(item, particularsTagList)">
            <i class="wukong wukong-black-label"
               :style="{ 'color': item.color}"></i>
            <span class="item-label">{{item.name}}</span>
            <span class="el-icon-check rt"
                  v-if="item.check"></span>
          </div>
        </div>
        <div class="tag-footer">
          <p class="footer-row cursor-pointer"
             @click="createTagFun">
            <span class="el-icon-plus"></span>
            <span>创建新标签</span>
          </p>
          <p class="footer-row cursor-pointer"
             @click="managementTag">
            <span class="el-icon-setting"></span>
            <span>标签管理</span>
          </p>
        </div>
      </div>
      <!-- 新建标签页 -->
      <new-tag v-else-if="tagContent == 1"
               :newTagTitle="newTagTitle"
               :newTagInput="newTagInput"
               :bgColorProps="bgColorProps"
               @changeColor="changeColor"
               @close="tagClose"
               @tagCreateSubmit="tagCreateSubmit"
               @tagCancel="tagCancel"
               @back="back">
      </new-tag>
      <!-- 标签管理 -->
      <editTag v-else-if="tagContent == 2"
               :editTagList="editTagList"
               @back="back"
               @close="tagClose"
               @editBtn="editBtn"
               @deleteBtn="deleteBtn">
      </editTag>
      <!-- 标签管理 - 编辑 -->
      <new-tag v-else-if="tagContent == 3"
               :newTagTitle="newTagTitle"
               :newTagInput="newTagInput"
               :bgColorProps="bgColorProps"
               @changeColor="changeColor"
               @close="tagClose"
               @tagCreateSubmit="tagCreateSubmit"
               @tagCancel="tagCancel"
               @back="back">
      </new-tag>
      <span slot="reference"
            @click="referenceFun">
        <slot name="editIndex"></slot>
      </span>
    </el-popover>
  </div>
</template>

<script>
import newTag from './newTag'
import editTag from './editTag'
import { tagList } from '@/api/oamanagement/task'
import {
  editTask,
  createTag,
  deleteTagAPI,
  editTagAPI
} from '@/api/oamanagement/task'
export default {
  components: {
    newTag,
    editTag
  },
  data() {
    return {
      // 标签弹出框
      tagShow: false,
      // 显示tag页面
      tagContent: 0,
      // 标签筛选框
      tagInputChange: '',
      // 标签数据
      particularsTagList: [],
      // 新增、编辑标签标题
      newTagTitle: '',
      // 创建-编辑标签的输入框
      newTagInput: '',
      // 标签颜色
      bgColorProps: '',
      // 标签编辑ID
      editTagId: '',
      popoverWidth: '220'
    }
  },
  mounted() {
    this.tagListFun()
  },
  props: {
    taskData: {
      type: Object,
      default: () => {
        return {}
      }
    },
    placement: String
  },
  watch: {
    // 搜索标签
    tagInputChange: function(newVal) {
      this.particularsTagList = this.particularsTagListCopy.filter(item => {
        return item.name.indexOf(newVal) > -1
      })
    }
  },
  methods: {
    // 创建新标签
    createTagFun() {
      this.newTagTitle = '创建新标签'
      this.newTagInput = ''
      this.tagContent = 1
      this.popoverWidth = '330'
    },
    // 标签管理 -- 编辑
    editBtn(val) {
      this.editTagId = val.labelId
      this.newTagTitle = '编辑标签'
      this.tagContent = 3
      this.bgColorProps = val.color
      this.newTagInput = val.name
    },
    // 标签管理弹出框
    managementTag() {
      this.popoverWidth = '400'
      this.tagContent = 2
    },
    // 标签点击变色
    tagBtn(value, values) {
      // 标签点击关联页面
      let labelIds = values.filter(item => {
        if (value.check) {
          return item.check && item.labelId != value.labelId
        } else {
          return item.check || item.labelId == value.labelId
        }
      })
      if (value.check) {
        editTask({
          taskId: this.taskData.taskId,
          labelId: labelIds
            .map(item => {
              return item.labelId
            })
            .join(',')
        }).then(res => {
          let list = this.taskData.labelList
          for (let item in list) {
            if (value.labelId == list[item].labelId) {
              list.splice(item, 1)
              break
            }
          }
          value.check = false
        })
      } else {
        editTask({
          taskId: this.taskData.taskId,
          labelId: labelIds
            .map(item => {
              return item.labelId
            })
            .join(',')
        }).then(res => {
          value.check = true
          value.labelName = value.name
          this.taskData.labelList.push(value)
        })
      }
      // value.check = value.check ? false : true
      for (let item in values) {
        if (values[item].labelId == value.labelId) {
          document.getElementsByClassName('tag-list')[item].style.background =
            '#F7F8FA'
        } else {
          document.getElementsByClassName('tag-list')[item].style.background =
            '#FFF'
        }
      }
    },
    // 标签点击变色
    changeColor(val) {
      this.bgColorProps = val
    },
    // 标签管理 -- 关闭按钮
    tagClose() {
      this.tagShow = false
    },
    // 创建新标签 -- 提交
    tagCreateSubmit(val, color) {
      let _this = this
      if (this.newTagTitle == '创建新标签') {
        createTag({
          name: val,
          color: color
        }).then(res => {
          // 关闭标签页
          this.tagClose()
          // 刷新标签列表
          this.tagListFun()
          this.$message.success('创建成功')
        })
      } else {
        editTagAPI({
          name: val,
          labelId: this.editTagId,
          color: color
        }).then(res => {
          for (let item of _this.editTagList) {
            if (item.labelId == _this.editTagId) {
              item.name = val
              item.color = color
            }
          }
          this.tagContent = 2
        })
      }
    },
    // 创建新标签 -- 取消
    tagCancel() {
      if (this.newTagTitle == '创建新标签') {
        // 关闭标签页
        this.tagClose()
        this.$message.info('已取消创建')
      } else {
        this.tagContent = 2
      }
    },
    // 标签管理 ——— 返回上一页
    back() {
      if (this.newTagTitle == '创建新标签') {
        this.tagContent = 0
        this.popoverWidth = '220'
      } else if (this.newTagTitle == '编辑标签' && this.tagContent == 3) {
        this.tagContent = 2
      } else if (this.tagContent == 2) {
        this.tagContent = 0
        this.popoverWidth = '220'
      }
    },
    // 标签管理 ——— 删除按钮
    deleteBtn(val) {
      this.$confirm('确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'is-particulars'
      })
        .then(() => {
          this.tagShow = true
          this.managementTag()
          deleteTagAPI({
            labelId: val.labelId
          }).then(res => {
            for (let i in this.editTagList) {
              if (this.editTagList[i].labelId == val.labelId) {
                this.editTagList.splice(i, 1)
              }
            }
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
          this.tagShow = true
          this.managementTag()
        })
    },
    tagListFun() {
      // 标签列表
      tagList().then(res => {
        for (let item of res.data) {
          if (this.taskData.labelList) {
            for (let i of this.taskData.labelList) {
              if (i.labelId == item.labelId) {
                item.check = true
                break
              } else {
                item.check = false
              }
            }
          }
        }
        // 标签管理数据
        this.editTagList = res.data
        this.particularsTagList = res.data
        // 用作搜索功能
        this.particularsTagListCopy = res.data
      })
    },
    referenceFun() {
      this.tagContent = 0
      this.tagListFun()
    }
  }
}
</script>


<style scoped lang="scss">
// 标签按钮
.tag-popover-box {
  margin: 0 -12px;
  .tag-top {
    margin-bottom: 10px;
    .el-icon-close {
      margin-right: 0;
    }
  }
  .el-input /deep/ .el-input__inner {
    border-radius: 15px;
  }
  .tag-content {
    margin-top: 10px;
    height: 196px;
    overflow: auto;
    border-bottom: 1px solid #e6e6e6;
    .tag-list {
      cursor: pointer;
      padding: 10px;
      .el-icon-check {
        margin-right: 0;
      }
    }
    .tag-list:hover {
      background: #f7f8fa;
    }
  }
  .tag-footer {
    color: #3e84e9;
    .footer-row {
      margin: 13px 0;
    }
  }
}
.tag-top,
.tag-content,
.tag-footer {
  padding: 0 12px;
}
.tag-popover-box > .el-input {
  width: auto;
  margin: 0 12px;
}

.cursor-pointer {
  cursor: pointer;
}
</style>
