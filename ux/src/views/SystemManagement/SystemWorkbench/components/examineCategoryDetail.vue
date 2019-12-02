<template>
  <slide-view
    :listener-ids="['manager-main-container']"
    :no-listener-ids="['examine-table']"
    :append-to-body="true"
    :body-style="{padding: '10px 30px', height: '100%'}"
    class="d-view"
    @side-close="hideView">
    <flexbox
      orient="vertical"
      style="height: 100%;">
      <div class="header">
        <flexbox class="detail-header">
          <div class="header-name">{{ showData.title }}<i
            v-if="showData.isSys != 1"
            class="el-icon-delete delete"
            @click="deleteClick"/></div>
          <img
            class="header-close"
            src="@/assets/img/task_close.png"
            @click="hideView" >
        </flexbox>
        <div class="detail-header-des">
          <div class="status">状态：{{ showData.status == 0 ? '停用' : '启用' }}</div>
          <div class="status-handle">
            启用
            <el-switch
              v-model="examineStatus"
              @change="examineStatusChange"/>
          </div>
        </div>
      </div>
      <div class="detail-body">
        <create-sections
          title="基本信息"
          class="create-sections">
          <div class="create-sections-content">
            <div class="dialog-content">
              <flexbox class="content-items">
                <div class="content-items-name">审批名称</div>
                <div class="content-items-value">{{ showData.title }}</div>
              </flexbox>
              <flexbox class="content-items">
                <div class="content-items-name">适用范围</div>
                <div class="content-items-value">{{ showData|formatedScopeInfo }}</div>
              </flexbox>
              <flexbox class="content-items">
                <div class="content-items-name">审批说明</div>
                <div class="content-items-value">{{ showData.remarks }}</div>
              </flexbox>
            </div>
          </div>
        </create-sections>
        <create-sections
          title="表单"
          class="create-sections">
          <div
            slot="header"
            class="preview-section">
            <el-button
              type="text"
              @click="handlePreview">预览</el-button>
          </div>
        </create-sections>
        <create-sections
          title="流程"
          class="create-sections">
          <div
            v-if="showData.examineType == 1"
            class="create-sections-content">
            <flexbox
              v-for="(item, index) in showData.stepList"
              :key="index"
              align="stretch"
              class="examine-flow">
              <div class="examine-flow-header">
                <div class="mark-circle"/>
                <div
                  v-if="index != 0"
                  class="mark-top-line"/>
                <div
                  v-if="index < showData.stepList.length - 1"
                  class="mark-bottom-line"/>
              </div>
              <div class="examine-flow-body">
                <div class="body-header"><span class="body-header-name">{{ index + 1|toRowName }}</span><span class="body-header-des">（{{ item|toRowNameDes }}）</span></div>
                <flexbox
                  v-if="item.userList.length > 0"
                  class="examine-users">
                  <div
                    v-for="(userItem, userIndex) in item.userList"
                    :key="userIndex"
                    class="examine-users-item">
                    <div
                      v-photo="userItem"
                      v-lazy:background-image="$options.filters.filterUserLazyImg(userItem.img)"
                      class="div-photo"/>
                    <div class="name">{{ userItem.realname }}</div>
                  </div>
                </flexbox>
              </div>
            </flexbox>
          </div>
          <div
            v-else
            class="create-sections-content">授权审批人</div>
        </create-sections>
      </div>
    </flexbox>
    <!-- 表单预览 -->
    <preview-field-view
      v-if="showTablePreview"
      :types="tablePreviewData.types"
      :types-id="tablePreviewData.typesId"
      label="10"
      @hiden-view="showTablePreview=false"/>
  </slide-view>
</template>

<script>
import SlideView from '@/components/SlideView'
import CreateSections from '@/components/CreateSections'
import PreviewFieldView from '@/views/SystemManagement/components/previewFieldView'
import Nzhcn from 'nzh/cn'
import {
  oaExamineCategoryDelete,
  oaExamineCategoryEnables
} from '@/api/systemManagement/workbench'

export default {
  /** 办公审批类型详情 */
  name: 'ExamineCategoryDetail',
  components: {
    SlideView,
    CreateSections,
    PreviewFieldView
  },
  filters: {
    formatedScopeInfo(data) {
      var name = ''
      var structures = data['deptIds']
        ? data['deptIds']
        : []
      for (let index = 0; index < structures.length; index++) {
        const element = structures[index]
        name = name + element.name + '、'
      }
      var users = data['userIds'] ? data['userIds'] : []
      for (let index = 0; index < users.length; index++) {
        const element = users[index]
        name =
          name + element.realname + (index === users.length - 1 ? '' : '、')
      }
      return name || '全公司'
    },
    // 标题
    toRowName: function(value) {
      return '第' + Nzhcn.encodeS(value) + '级'
    },
    // 标题描述
    toRowNameDes: function(data) {
      if (data.stepType == 1) {
        return '负责人主管'
      } else if (data.stepType == 2) {
        return data.userList.length + '人或签'
      } else if (data.stepType == 3) {
        return data.userList.length + '人会签'
      } else if (data.stepType == 4) {
        return '上一级审批人主管'
      }
      return ''
    }
  },
  props: {
    // 详情信息
    data: Object
  },
  data() {
    return {
      showData: {},
      examineStatus: false,
      // 展示表单预览
      tablePreviewData: { types: '', typesId: '' },
      showTablePreview: false
    }
  },
  computed: {},
  watch: {
    data: function(value) {
      this.getShowData()
    }
  },
  mounted() {
    this.getShowData()
  },
  methods: {
    // 预览表单
    handlePreview() {
      this.tablePreviewData.types = 'oa_examine'
      this.tablePreviewData.typesId = this.data.categoryId
      this.showTablePreview = true
    },
    deleteClick() {
      // 启用停用
      this.$confirm('您确定要删除该审批流?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          oaExamineCategoryDelete({
            id: this.data['categoryId']
          })
            .then(res => {
              this.$emit('refresh')
              this.hideView()
              this.$message({
                type: 'success',
                message: '操作成功'
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
    getShowData() {
      this.showData = Object.assign({}, this.data)
      this.examineStatus = this.showData.status != 0
    },
    // 切换审批状态
    examineStatusChange(status) {
      this.showData.status = status ? 1 : 0
      // 启用停用
      this.$confirm(
        '您确定要' + (status ? '启用' : '停用') + '该审批流?',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
        .then(() => {
          oaExamineCategoryEnables({
            id: this.data['categoryId'],
            status: status ? 1 : 0
          })
            .then(res => {
              this.$emit('refresh')
              this.$message({
                type: 'success',
                message: '操作成功'
              })
            })
            .catch(() => {
              this.cancelStatusChange()
            })
        })
        .catch(() => {
          this.cancelStatusChange()
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    },
    cancelStatusChange() {
      this.examineStatus = !this.examineStatus
      this.showData.status = this.examineStatus ? 1 : 0
    },
    //* * 点击关闭按钮隐藏视图 */
    hideView() {
      this.$emit('hide-view')
    }
  }
}
</script>

<style lang="scss" scoped>
.header {
  width: 100%;
  margin-bottom: 30px;
}
.detail-header {
  position: relative;
  min-height: 60px;
  .header-name {
    font-size: 14px;
    color: #333333;
    flex: 1;
    .delete {
      color: #3e84e9;
      padding: 0 10px;
      font-size: 13px;
      cursor: pointer;
    }
  }
  .header-close {
    display: block;
    width: 30px;
    height: 30px;
    margin-left: 20px;
    padding: 5px;
    cursor: pointer;
  }
}

.detail-header-des {
  color: #999999;
  font-size: 12px;
  .status {
    float: left;
  }
  .status-handle {
    float: right;
  }
}

.detail-body {
  flex: 1;
  overflow-y: auto;
  width: 100%;

  .create-sections {
    padding: 5px 0;
    /deep/ .section-header {
      padding: 5px 0;
    }
  }

  .create-sections-content {
    padding: 0;
  }

  .dialog-content {
    margin-top: 10px;
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
}
// 预览
.preview-section {
  flex: 1;
  button {
    float: right;
    margin-right: 20px;
  }
}

// 审批流
.examine-flow {
  .examine-flow-header {
    position: relative;
    .mark-circle {
      width: 8px;
      height: 8px;
      border-radius: 4px;
      background-color: #ee7228;
      margin: 11px 20px 0 5px;
    }
    .mark-top-line {
      width: 1px;
      background-color: #e6e6e6;
      position: absolute;
      top: 0;
      left: 9px;
      height: 11px;
    }
    .mark-bottom-line {
      width: 1px;
      background-color: #e6e6e6;
      position: absolute;
      top: 19px;
      left: 9px;
      bottom: 0;
    }
  }

  .examine-flow-body {
    .body-header {
      padding: 8px 0;
      font-size: 13px;
      .body-header-name {
        color: #333333;
      }
      .body-header-des {
        color: #777777;
      }
    }
  }
}

// 人信息
.examine-users {
  text-align: center;
  .examine-users-item {
    padding: 10px 20px 10px 0;
    .div-photo {
      width: 40px;
      height: 40px;
      border-radius: 20px;
    }
    .name {
      color: #333333;
      font-size: 12px;
      padding: 2px 0;
      width: 60px;
      overflow: hidden;
    }
  }
}

.d-view {
  position: fixed;
  width: 500px;
  top: 60px;
  bottom: 0px;
  right: 0px;
}
</style>

