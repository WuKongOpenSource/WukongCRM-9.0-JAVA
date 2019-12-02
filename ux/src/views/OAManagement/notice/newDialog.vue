<template>
  <create-view :body-style="{height: '100%'}">
    <div
      v-loading="loading"
      class="details-box">
      <div
        slot="header"
        class="header">
        <span class="text">新建公告</span>
        <img
          class="el-icon-close rt"
          src="@/assets/img/task_close.png"
          alt=""
          @click="close">
      </div>
      <div class="content">
        <el-form
          ref="form"
          :model="formData"
          :rules="rules">
          <el-form-item
            v-for="(item, index) in formList"
            :label="item.label"
            :class="'el-form-item' + item.field"
            :prop="item.field"
            :key="index">
            <template v-if="item.type == 'date'">
              <el-date-picker
                v-model="formData[item.field]"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="选择日期"/>
            </template>
            <template v-else-if="item.type == 'textarea'">
              <el-input
                :autosize="{ minRows: 6}"
                v-model="formData[item.field]"
                type="textarea"
                placeholder="请输入内容"/>
            </template>
            <template v-else-if="item.type =='plus'">
              <members-dep
                :popover-display="'block'"
                :title="'通知部门'"
                :user-checked-data="formData[item.field].staff"
                :dep-checked-data="formData[item.field].dep"
                @popoverSubmit="popoverSubmit">
                <flexbox
                  slot="membersDep"
                  wrap="wrap"
                  class="user-container">
                  <div
                    v-for="(item, index) in formData[item.field].staff"
                    :key="'user' + index"
                    class="user-item"
                    @click.stop="deleteuser(index)">{{ item.realname }}
                    <i class="delete-icon el-icon-close"/>
                  </div>
                  <div
                    v-for="(item, index) in formData[item.field].dep"
                    :key="'dep' + index"
                    class="user-item"
                    @click.stop="deleteDepuser(index)">{{ item.name }}
                    <i class="delete-icon el-icon-close"/>
                  </div>
                  <div class="add-item">+添加</div>
                </flexbox>
              </members-dep>
            </template>
            <el-input
              v-else
              v-model="formData[item.field]"/>
          </el-form-item>
        </el-form>
      </div>
      <div class="btn-box">
        <el-button
          type="primary"
          @click="onSubmit">提交</el-button>
        <el-button @click="close">取消</el-button>
      </div>
    </div>
  </create-view>
</template>

<script>
import CreateView from '@/components/CreateView'

import membersDep from '@/components/selectEmployee/membersDep'
// API
import { noticeAdd } from '@/api/oamanagement/notice'
import { formatTimeToTimestamp } from '@/utils/index'

export default {
  components: {
    CreateView,
    membersDep
  },
  data() {
    var validateTime = (rule, value, callback) => {
      if (
        (rule.field == 'startTime' &&
          !this.formData.startTime &&
          this.formData.endTime) ||
        (rule.field == 'endTime' &&
          !this.formData.endTime &&
          this.formData.startTime)
      ) {
        callback(new Error('请完善时间'))
      } else if (this.formData.startTime && this.formData.endTime) {
        if (
          formatTimeToTimestamp(this.formData.startTime) >=
          formatTimeToTimestamp(this.formData.endTime)
        ) {
          callback(new Error('开始时间必须小于结束时间'))
        }
      }
      callback()
    }
    return {
      formList: [
        { label: '公告标题', field: 'title' },
        { label: '通知部门', field: 'dep', type: 'plus' },
        { label: '开始时间', field: 'startTime', type: 'date' },
        { label: '结束时间', field: 'endTime', type: 'date' },
        { label: '公告正文', field: 'content', type: 'textarea' }
      ],
      formData: { dep: { staff: [], dep: [] }},
      rules: {
        title: [
          { required: true, message: '公告标题不能为空', trigger: 'blur' },
          { max: 50, message: '公告标题长度最多为50个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '公告正文不能为空', trigger: 'blur' }
        ],
        startTime: [
          { required: true, message: '不能为空', trigger: 'blur' },
          { validator: validateTime, trigger: 'blur' }
        ],
        endTime: [
          { required: true, message: '不能为空', trigger: 'blur' },
          { validator: validateTime, trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  methods: {
    onSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.loading = true
          noticeAdd({
            title: this.formData.title,
            content: this.formData.content,
            startTime: this.formData.startTime,
            endTime: this.formData.endTime,
            deptIds: this.formData.dep.dep
              .map(item => {
                return item.id
              })
              .join(','),
            ownerUserIds: this.formData.dep.staff
              .map(item => {
                return item.userId
              })
              .join(',')
          })
            .then(res => {
              this.$message.success('新建公告成功')
              if (this.$route.query.routerKey == 1) {
                this.$router.push('notice')
              } else {
                this.$emit('onSubmit')
              }
              this.loading = false
            })
            .catch(() => {
              this.$message.error('新建公告失败')
              this.loading = false
            })
        } else {
          return false
        }
      })
    },
    close() {
      if (this.$route.query.routerKey == 1) {
        this.$router.go(-1)
      } else {
        this.$emit('close')
      }
    },
    // 关闭按钮
    popoverSubmit(members, dep) {
      this.$set(this.formData, 'dep', { staff: members, dep: dep })
    },
    // 删除部门和用户
    deleteuser(index) {
      this.formData.dep.staff.splice(index, 1)
    },
    deleteDepuser(index) {
      this.formData.dep.dep.splice(index, 1)
    }
  }
}
</script>

<style scoped lang="scss">
.details-box {
  display: flex;
  flex-direction: column;
  height: 100%;
  .header {
    line-height: 40px;
    height: 40px;
    padding: 0 0 0 10px;
    .text {
      font-size: 17px;
    }
    .el-icon-close {
      margin-right: 0;
      width: 40px;
      line-height: 40px;
      padding: 10px;
      cursor: pointer;
    }
  }
  .content {
    padding: 15px 20px;
    flex: 1;
    overflow: auto;
    .el-form /deep/ {
      .el-form-item {
        margin-bottom: 10px;
        padding-bottom: 10px;
        float: left;
        width: 50%;
        // .el-form-item__content {
        //     line-height: 40px;
        //     height: 40px;
        // }
        .el-form-item__content > .el-textarea {
          margin-top: 6px;
        }
        .el-form-item__label {
          float: none;
          font-size: 12px;
        }
        .el-input {
          // width: 45%;
          .el-input__inner {
            vertical-align: bottom;
          }
        }
        .members-dep {
          display: inline-block;
          width: 100%;
          position: relative;
          .el-textarea {
            cursor: pointer;
            .el-textarea__inner {
              resize: none;
              min-height: 34px !important;
              // height: 34px !important;
              padding-right: 28px;
              overflow: hidden;
            }
          }
          .el-icon-plus {
            position: absolute;
            right: 7px;
            top: 50%;
            margin-top: -4px;
          }
        }
        .el-date-editor {
          vertical-align: bottom;
          width: 100%;
          .el-range-separator {
            width: 7%;
          }
        }
      }
      .el-form-itemtitle,
      .el-form-itemstartTime {
        padding-right: 25px;
      }
      .el-form-itemdep,
      .el-form-itemendTime {
        padding-left: 25px;
      }
      .el-form-itemdep {
        margin-bottom: 11px;
      }
      .el-form-itemcontent {
        width: 100%;
      }
    }
  }
  .btn-box {
    text-align: right;
    padding-right: 20px;
  }
}

.user-container {
  margin-top: 3px;
  min-height: 34px;
  position: relative;
  border-radius: 3px;
  font-size: 12px;
  border: 1px solid #ddd;
  color: #333333;
  padding: 5px;
  line-height: 15px;
  max-height: 105px;
  overflow-y: auto;
  .user-item {
    padding: 5px;
    background-color: #e2ebf9;
    border-radius: 3px;
    margin: 3px;
    cursor: pointer;
  }
  .add-item {
    padding: 5px;
    color: #3e84e9;
    cursor: pointer;
  }
  .delete-icon {
    color: #999;
    cursor: pointer;
  }
  &:hover {
    border-color: #c0c4cc;
  }
}
</style>
