<template>
  <el-dialog title="高级筛选"
             :visible.sync="visible"
             @close="handleCancel"
             width="800px">
    <div style="margin-bottom: 10px;">筛选条件</div>
    <el-form class="filter-container" id="filter-container">
      <el-form-item>
        <template v-for="(formItem, index) in form">
          <el-row :key="index">
            <el-col :span="8">
              <el-select v-model="formItem.fieldName"
                         @change="fieldChange(formItem)"
                         placeholder="请选择要筛选的字段名">
                <el-option v-for="item in fieldList"
                           :key="item.fieldName"
                           :label="item.name"
                           :value="item.fieldName">
                </el-option>
              </el-select>
            </el-col>

            <el-col :span="1"
                    v-if="formItem.formType !== 'date' && formItem.formType !== 'datetime' && formItem.formType !== 'business_type'">&nbsp;</el-col>
            <el-col :span="4"
                    v-if="formItem.formType !== 'date' && formItem.formType !== 'datetime' && formItem.formType !== 'business_type'">
              <el-select v-model="formItem.condition"
                         placeholder="请选择范围">
                <el-option v-for="item in calConditionOptions(formItem.formType, formItem)"
                           :key="item.value"
                           :label="item.label"
                           :value="item.value">
                </el-option>
              </el-select>
            </el-col>

            <!-- 商机组 -->
            <el-col :span="1"
                    v-if="formItem.formType == 'business_type'">&nbsp;</el-col>
            <el-col :span="4"
                    v-if="formItem.formType == 'business_type'">
              <el-select v-model="formItem.typeId"
                         @change="typeOptionsChange(formItem)"
                         placeholder="请选择">
                <el-option v-for="item in formItem.typeOption"
                           :key="item.typeId"
                           :label="item.name"
                           :value="item.typeId">
                </el-option>
              </el-select>
            </el-col>

            <el-col :span="1">&nbsp;</el-col>
            <el-col :span="formItem.formType === 'datetime' || formItem.formType === 'date' ? 13 : 8">
              <el-select v-if="formItem.formType === 'select'"
                         v-model="formItem.value"
                         placeholder="请选择筛选条件">
                <el-option v-for="item in formItem.setting"
                           :key="item"
                           :label="item"
                           :value="item">
                </el-option>
              </el-select>
              <el-select v-else-if="formItem.formType === 'checkStatus'"
                         v-model="formItem.value"
                         placeholder="请选择筛选条件">
                <el-option v-for="item in formItem.setting"
                           :key="item.value"
                           :label="item.name"
                           :value="item.value">
                </el-option>
              </el-select>
              <el-date-picker v-else-if="formItem.formType === 'date' || formItem.formType === 'datetime'"
                              v-model="formItem.value"
                              :value-format="formItem.formType === 'date' ? 'yyyy-MM-dd' : 'yyyy-MM-dd HH:mm:ss'"
                              :type="formItem.formType === 'date' ? 'daterange' : 'datetimerange'"
                              style="padding: 0px 10px;"
                              range-separator="-"
                              start-placeholder="开始日期"
                              end-placeholder="结束日期">
              </el-date-picker>
              <el-select v-else-if="formItem.formType === 'business_type'"
                         v-model="formItem.statusId"
                         placeholder="请选择">
                <el-option v-for="item in formItem.statusOption"
                           :key="item.statusId"
                           :label="item.name"
                           :value="item.statusId">
                </el-option>
              </el-select>
              <xh-user-cell v-else-if="formItem.formType === 'user'"
                            :item="formItem"
                            :infoParams="{m	:'crm',c: crmType,a: 'index' }"
                            @value-change="userValueChange"></xh-user-cell>
              <el-input v-else
                        v-model="formItem.value"
                        placeholder="请输入筛选条件"></el-input>
            </el-col>
            <el-col :span="1"
                    class="delete">
              <i class="el-icon-error delete-btn"
                 @click="handleDelete(index)"></i>
            </el-col>
          </el-row>
        </template>
      </el-form-item>
    </el-form>
    <p class="el-icon-warning warning-info"
       v-show="showErrors">
      <span class="desc">筛选条件中有重复项！</span>
    </p>
    <el-button type="text"
               @click="handleAdd">+ 添加筛选条件</el-button>
    <div class="save"
         v-if="!isSeas">
      <el-checkbox v-model="saveChecked">保存为场景</el-checkbox>
      <el-input class="name"
                v-show="saveChecked"
                v-model.trim="saveName"
                :maxlength="10"
                placeholder="请输入场景名称，最多10个字符">
      </el-input>
      <div class="save-setting"
           v-show="saveChecked">
        <el-checkbox v-model="saveDefault">设置为默认</el-checkbox>
      </div>
    </div>
    <div slot="footer"
         class="dialog-footer">
      <el-button @click="handleCancel">取 消</el-button>
      <el-button type="primary"
                 @click="handleConfirm">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { formatTimeToTimestamp, objDeepCopy } from '@/utils'
import { XhUserCell } from '@/components/CreateCom'
/**
 * fieldList: 高级筛选的字段
 *     type:  date || datetime || select || 其他 input
 */
export default {
  name: 'index',
  components: {
    XhUserCell
  },
  props: {
    dialogVisible: {
      type: Boolean,
      required: true,
      default: false
    },
    fieldList: {
      type: Array,
      required: true,
      default: []
    },
    obj: {
      default: {},
      required: true
    },
    /** 获取客户管理下列表权限内的员工列表 针对 usersList */
    crmType: {
      type: String,
      default: ''
    },
    // 辅助 使用 公海没有场景
    isSeas: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      form: [],
      visible: false,
      showErrors: false,
      saveChecked: false, // 展示场景
      saveDefault: false, // 设置为默认场景
      saveName: null // 场景名称
    }
  },
  watch: {
    dialogVisible: {
      handler(val) {
        if (val) {
          this.form = objDeepCopy(this.obj.form)
          if (this.form.length == 0) {
            this.form.push({
              fieldName: '',
              name: '',
              formType: '',
              condition: 'is',
              value: '',
              typeOption: [],
              statusOption: [],
              typeId: '',
              statusId: ''
            })
          }
          this.saveChecked = false
          this.saveDefault = false
          this.saveName = null
        }
        this.visible = this.dialogVisible
      },
      deep: true,
      immediate: true
    },

    form() {
      this.$nextTick(() => {
        var container = document.getElementById('filter-container')
        container.scrollTop = container.scrollHeight
      })
    }
  },
  methods: {
    /**
     * 商机组状态
     */
    typeOptionsChange(formItem) {
      if (formItem.typeId) {
        let obj = formItem.typeOption.find(item => {
          return item.typeId === formItem.typeId
        })
        formItem.statusOption = obj.statusList || []
      } else {
        formItem.statusOption = []
      }
      formItem.statusId = ''
    },
    /**
     * 用户创建人
     */
    userValueChange(data) {
      if (data.value.length > 0) {
        data.item.value = data.value
      } else {
        data.item.value = []
      }
    },
    /** 条件数据源 */
    calConditionOptions(formType, item) {
      if (
        formType == 'select' ||
        formType == 'checkbox' ||
        formType == 'user' ||
        formType == 'checkStatus'
      ) {
        return [
          { value: 'is', label: '等于', disabled: false },
          { value: 'isNot', label: '不等于', disabled: false }
        ]
      } else if (
        formType == 'module' ||
        formType == 'text' ||
        formType == 'textarea'
      ) {
        return [
          { value: 'is', label: '等于', disabled: false },
          { value: 'isNot', label: '不等于', disabled: false },
          { value: 'contains', label: '包含', disabled: false },
          { value: 'notContains', label: '不包含', disabled: false }
        ]
      } else if (formType == 'floatnumber' || formType == 'number') {
        return [
          { value: 'is', label: '等于', disabled: false },
          { value: 'isNot', label: '不等于', disabled: false },
          { value: 'contains', label: '包含', disabled: false },
          { value: 'notContains', label: '不包含', disabled: false },
          { value: 'isNull', label: '为空', disabled: false },
          { value: 'isNotNull', label: '不为空', disabled: false },
          { value: 'gt', label: '大于', disabled: false },
          { value: 'egt', label: '大于等于', disabled: false },
          { value: 'lt', label: '小于', disabled: false },
          { value: 'elt', label: '小于等于', disabled: false }
        ]
      } else {
        return [
          { value: 'is', label: '等于', disabled: false },
          { value: 'isNot', label: '不等于', disabled: false },
          { value: 'contains', label: '包含', disabled: false },
          { value: 'notContains', label: '不包含', disabled: false },
          { value: 'startWith', label: '开始于', disabled: false },
          { value: 'endWith', label: '结束于', disabled: false },
          { value: 'isNull', label: '为空', disabled: false },
          { value: 'isNotNull', label: '不为空', disabled: false },
          { value: 'gt', label: '大于', disabled: false },
          { value: 'egt', label: '大于等于', disabled: false },
          { value: 'lt', label: '小于', disabled: false },
          { value: 'elt', label: '小于等于', disabled: false }
        ]
      }
    },
    /**
     * 当前选择的字段名改变，判断是否有重复
     * @param formItem
     */
    fieldChange(formItem) {
      let obj = this.fieldList.find(item => {
        return item.fieldName === formItem.fieldName
      })
      if (obj) {
        formItem.formType = obj.formType
        formItem.name = obj.name
        if (formItem.formType == 'business_type') {
          formItem.typeOption = obj.setting
          formItem.statusOption = []
          formItem.typeId = ''
          formItem.statusId = ''
        } else if (
          formItem.formType == 'select' ||
          formItem.formType == 'checkStatus'
        ) {
          formItem.setting = obj.setting || []
        } else if (
          formItem.formType === 'date' ||
          formItem.formType === 'datetime' ||
          formItem.formType === 'user'
        ) {
          formItem.value = []
        }
      }

      let arr = this.form.filter(item => {
        return item.fieldName === formItem.fieldName
      })
      if (arr.length > 1) this.showErrors = true
      else this.showErrors = false
    },
    /**
     * 取消选择
     */
    handleCancel() {
      this.$emit('update:dialogVisible', false)
    },
    /**
     * 确定选择
     */
    handleConfirm() {
      if (this.showErrors) {
        this.$message.error('筛选条件中有重复项！')
        return
      }
      if (this.saveChecked) {
        if (!this.saveName || this.saveName === '') {
          this.$message.error('场景名称不能为空！')
          return
        }
      }
      for (let i = 0; i < this.form.length; i++) {
        let o = this.form[i]
        if (!o.fieldName || o.fieldName === '') {
          this.$message.error('要筛选的字段名称不能为空！')
          return
        }
        if (o.formType == 'business_type') {
          if (!o.typeId && !o.statusId) {
            this.$message.error('请输入筛选条件的值！')
            return
          }
        } else if (
          o.formType == 'date' ||
          o.formType == 'datetime' ||
          o.formType == 'user'
        ) {
          if (!o.value || o.value.length === 0) {
            this.$message.error('请输入筛选条件的值！')
            return
          }
        } else if (!o.value && o.value !== 0) {
          this.$message.error('请输入筛选条件的值！')
          return
        }
      }
      let obj = {}
      this.form.forEach(o => {
        if (o.formType == 'datetime' || o.formType == 'date') {
          obj[o.fieldName] = {
            start: o.value[0],
            end: o.value[1],
            formType: o.formType,
            name: o.fieldName
          }
        } else if (o.formType == 'business_type') {
          obj[o.fieldName] = {
            typeId: o.typeId,
            statusId: o.statusId,
            formType: o.formType,
            name: o.fieldName
          }
        } else if (o.formType == 'user') {
          obj[o.fieldName] = {
            condition: o.condition,
            value: o.value[0].userId,
            formType: o.formType,
            name: o.fieldName
          }
        } else {
          obj[o.fieldName] = {
            condition: o.condition,
            value: o.value,
            formType: o.formType,
            name: o.fieldName
          }
        }
      })
      let data = {
        obj: obj,
        form: this.form,
        saveChecked: this.saveChecked,
        saveDefault: this.saveDefault,
        saveName: this.saveName
      }
      this.$emit('filter', data)
    },
    /**
     * 添加筛选条件
     */
    handleAdd() {
      this.form.push({
        fieldName: '',
        condition: 'is',
        value: '',
        formType: '',
        setting: [],
        typeOption: [],
        statusOption: [],
        typeId: '',
        statusId: ''
      })
    },
    /**
     * 删除筛选条件
     * @param index
     */
    handleDelete(index) {
      this.$confirm('您确定要删除这一条数据吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.form.splice(index, 1)
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    }
  }
}
</script>

<style lang="scss" scoped>
/deep/ .el-dialog__body {
  padding: 10px 20px;
}

/deep/ .el-form-item__label {
  width: 100%;
  text-align: left;
}
.filter-container {
  max-height: 300px;
  overflow-y: auto;
}

.save {
  margin-top: 10px;
  .name {
    width: 300px;
    margin-left: 10px;
    /deep/ .el-input__inner {
      height: 32px;
    }
  }
  .save-setting {
    margin-top: 20px;
  }
}

.el-form-item {
  margin-bottom: 0;
}

.el-row {
  margin-bottom: 20px;
  .delete-btn {
    margin-left: 15px;
    color: #bbb;
    cursor: pointer;
  }
  .el-select,
  .el-date-editor {
    width: 100%;
  }
}

.warning-info {
  width: 100%;
  font-size: 14px;
  color: #f56c6c;
  margin-top: 10px;
  .desc {
    padding-left: 8px;
  }
}
</style>
