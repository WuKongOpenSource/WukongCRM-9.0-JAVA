<template>
  <create-view :body-style="{height: '100%'}">
    <div class="details-box"
         v-loading="loading">
      <div slot="header"
           class="header">
        <span class="text">编辑公告</span>
        <img class="el-icon-close rt"
             src="@/assets/img/task_close.png"
             @click="close"
             alt="">
      </div>
      <div class="content">
        <el-form ref="form"
                 :model="formData"
                 :rules="rules">
          <el-form-item :label="item.label"
                        :class="'el-form-item' + item.field"
                        :prop="item.field"
                        v-for="(item, index) in formList"
                        :key="index">
            <template v-if="item.type == 'date'">
              <el-date-picker v-model="formData[item.field]"
                              type="date"
                              value-format="yyyy-MM-dd"
                              placeholder="选择日期">
              </el-date-picker>
            </template>
            <template v-else-if="item.type == 'textarea'">
              <el-input type="textarea"
                        autosize
                        placeholder="请输入内容"
                        v-model="formData[item.field]">
              </el-input>
            </template>
            <el-input v-else
                      v-model="formData[item.field]"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div class="btn-box">
        <el-button type="primary"
                   @click="onSubmit">提交</el-button>
        <el-button @click="close">取消</el-button>
      </div>
    </div>
  </create-view>
</template>

<script>
import CreateView from '@/components/CreateView'
import { formatTimeToTimestamp } from '@/utils/index'

export default {
  components: {
    CreateView
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
        { label: '开始时间', field: 'startTime', type: 'date' },
        { label: '结束时间', field: 'endTime', type: 'date' },
        { label: '公告正文', field: 'content', type: 'textarea' }
      ],
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
      }
    }
  },
  props: {
    formData: Object,
    loading: Boolean
  },
  methods: {
    onSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.$emit('editSubmit')
        } else {
          return false
        }
      })
    },
    close() {
      this.$emit('editClose')
    },
    inputChange() {
      this.popoverVisible = true
    }
  }
}
</script>

<style scoped lang="scss">
$size16: 16px;
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
    padding: 15px 18px;
    flex: 1;
    overflow: auto;
    padding-right: 20px;
    .el-form /deep/ {
      .el-form-item {
        margin-bottom: 10px;
        padding-bottom: 10px;
        float: left;
        width: 50%;
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
        .el-date-editor {
          vertical-align: bottom;
          width: 100%;
          .el-range-separator {
            width: 7%;
          }
        }
      }
      .el-form-itemtitle,
      .el-form-itemendTime {
        padding-right: 25px;
      }
      .el-form-itemstartTime {
        padding-left: 25px;
      }
      .el-form-itemstartTime {
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
</style>