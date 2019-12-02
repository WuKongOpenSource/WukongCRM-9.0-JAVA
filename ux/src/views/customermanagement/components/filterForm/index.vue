<template>
  <el-dialog
    :visible.sync="visible"
    title="高级筛选"
    width="900px"
    @close="handleCancel">
    <div style="margin-bottom: 10px;">筛选条件</div>
    <el-form
      id="filter-container"
      class="filter-container">
      <el-form-item>
        <template v-for="(formItem, index) in form">
          <el-row :key="index">
            <el-col :span="8">
              <el-select
                v-model="formItem.fieldName"
                placeholder="请选择要筛选的字段名"
                @change="fieldChange(formItem)">
                <el-option
                  v-for="item in fieldList"
                  :key="item.fieldName"
                  :label="item.name"
                  :value="item.fieldName"/>
              </el-select>
            </el-col>

            <el-col
              v-if="showCalCondition(formItem.formType)"
              :span="1">&nbsp;</el-col>
            <el-col
              v-if="showCalCondition(formItem.formType)"
              :span="4">
              <el-select
                v-model="formItem.condition"
                placeholder="请选择范围">
                <el-option
                  v-for="item in calConditionOptions(formItem.formType, formItem)"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"/>
              </el-select>
            </el-col>

            <!-- 商机组 -->
            <el-col
              v-if="formItem.formType == 'business_type'"
              :span="1">&nbsp;</el-col>
            <el-col
              v-if="formItem.formType == 'business_type'"
              :span="4">
              <el-select
                v-model="formItem.typeId"
                placeholder="请选择"
                @change="typeOptionsChange(formItem)">
                <el-option
                  v-for="item in formItem.typeOption"
                  :key="item.typeId"
                  :label="item.name"
                  :value="item.typeId"/>
              </el-select>
            </el-col>

            <el-col :span="1">&nbsp;</el-col>
            <el-col :span="formItem.formType === 'datetime' || formItem.formType === 'date' || formItem.formType === 'map_address' ? 13 : 8">
              <el-select
                v-if="formItem.formType === 'select'"
                v-model="formItem.value"
                placeholder="请选择筛选条件">
                <el-option
                  v-for="item in formItem.setting"
                  :key="item"
                  :label="item"
                  :value="item"/>
              </el-select>
              <el-select
                v-else-if="formItem.formType === 'checkStatus'"
                v-model="formItem.value"
                placeholder="请选择筛选条件">
                <el-option
                  v-for="item in formItem.setting"
                  :key="item.value"
                  :label="item.name"
                  :value="item.value"/>
              </el-select>
              <el-date-picker
                v-else-if="formItem.formType === 'date' || formItem.formType === 'datetime'"
                v-model="formItem.value"
                :value-format="formItem.formType === 'date' ? 'yyyy-MM-dd' : 'yyyy-MM-dd HH:mm:ss'"
                :type="formItem.formType === 'date' ? 'daterange' : 'datetimerange'"
                style="padding: 0px 10px;"
                range-separator="-"
                start-placeholder="开始日期"
                end-placeholder="结束日期"/>
              <el-select
                v-else-if="formItem.formType === 'business_type'"
                v-model="formItem.statusId"
                placeholder="请选择">
                <el-option
                  v-for="item in formItem.statusOption"
                  :key="item.statusId"
                  :label="item.name"
                  :value="item.statusId"/>
              </el-select>
              <xh-user-cell
                v-else-if="formItem.formType === 'user'"
                :item="formItem"
                :info-params="{m	:'crm',c: crmType,a: 'index' }"
                @value-change="arrayValueChange"/>
              <xh-prouct-cate
                v-else-if="formItem.formType === 'category'"
                :item="formItem"
                @value-change="arrayValueChange"/>
              <v-distpicker
                v-else-if="formItem.formType === 'map_address'"
                :province="formItem.address.state"
                :city="formItem.address.city"
                :area="formItem.address.area"
                @province="selectProvince($event,formItem)"
                @city="selectCity($event,formItem)"
                @area="selectArea($event,formItem)"/>
              <el-input
                v-else
                v-model="formItem.value"
                placeholder="请输入筛选条件"/>
            </el-col>
            <el-col
              :span="1"
              class="delete">
              <i
                class="el-icon-error delete-btn"
                @click="handleDelete(index)"/>
            </el-col>
          </el-row>
        </template>
      </el-form-item>
    </el-form>
    <p
      v-show="showErrors"
      class="el-icon-warning warning-info">
      <span class="desc">筛选条件中有重复项！</span>
    </p>
    <el-button
      type="text"
      @click="handleAdd">+ 添加筛选条件</el-button>
    <div
      v-if="!isSeas"
      class="save">
      <el-checkbox v-model="saveChecked">保存为场景</el-checkbox>
      <el-input
        v-show="saveChecked"
        v-model.trim="saveName"
        :maxlength="10"
        class="name"
        placeholder="请输入场景名称，最多10个字符"/>
      <div
        v-show="saveChecked"
        class="save-setting">
        <el-checkbox v-model="saveDefault">设置为默认</el-checkbox>
      </div>
    </div>
    <div
      slot="footer"
      class="dialog-footer">
      <el-button @click="handleCancel">取 消</el-button>
      <el-button
        type="primary"
        @click="handleConfirm">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { objDeepCopy } from '@/utils'
import { XhUserCell, XhProuctCate } from '@/components/CreateCom'
import VDistpicker from 'v-distpicker'
/**
 * fieldList: 高级筛选的字段
 *     type:  date || datetime || select || 其他 input
 */
export default {
  name: 'Index',
  components: {
    XhUserCell,
    XhProuctCate,
    VDistpicker
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
      default: () => {
        return []
      }
    },
    obj: {
      type: Object,
      required: true,
      default: () => {
        return {}
      }
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
              statusId: '',
              address: {
                state: '',
                city: '',
                area: ''
              }
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
     * 位置更改
     */
    selectProvince(data, formItem) {
      formItem.address.state = data.value
    },
    selectCity(data, formItem) {
      formItem.address.city = data.value
    },
    selectArea(data, formItem) {
      formItem.address.area = data.value
    },

    /**
     * 商机组状态
     */
    typeOptionsChange(formItem) {
      if (formItem.typeId) {
        const obj = formItem.typeOption.find(item => {
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
     * 产品类别
     */
    arrayValueChange(data) {
      if (data.value.length > 0) {
        data.item.value = data.value
        data.item.valueContent = data.valueContent
      } else {
        data.item.value = []
      }
    },

    /**
     * 是否展示条件
     */
    showCalCondition(formType) {
      if (
        formType == 'date' ||
        formType == 'datetime' ||
        formType == 'business_type' ||
        formType == 'category' ||
        formType == 'map_address'
      ) {
        return false
      }
      return true
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
      } else if (formType == 'category') {
        return [
          { value: 'is', label: '等于', disabled: false },
          { value: 'isnot', label: '不等于', disabled: false },
          { value: 'contains', label: '包含', disabled: false },
          { value: 'not_contain', label: '不包含', disabled: false }
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
      const obj = this.fieldList.find(item => {
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
          formItem.value = ''
        } else if (
          formItem.formType == 'select' ||
          formItem.formType == 'checkStatus'
        ) {
          formItem.setting = obj.setting || []
          formItem.value = ''
        } else if (formItem.formType == 'map_address') {
          formItem.address = {
            state: '',
            city: '',
            area: ''
          }
        } else if (
          formItem.formType === 'date' ||
          formItem.formType === 'datetime' ||
          formItem.formType === 'user' ||
          formItem.formType === 'category'
        ) {
          formItem.value = []
        } else {
          formItem.value = ''
        }
      }

      const arr = this.form.filter(item => {
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
        const o = this.form[i]
        if (!o.fieldName || o.fieldName === '') {
          this.$message.error('要筛选的字段名称不能为空！')
          return
        }
        if (o.formType == 'business_type') {
          if (!o.typeId && !o.statusId) {
            this.$message.error('请输入筛选条件的值！')
            return
          }
        } else if (o.formType == 'map_address') {
          if (!o.address.state && !o.address.city && !o.address.area) {
            this.$message.error('请选择筛选条件的值！')
            return
          }
        } else if (
          o.formType == 'date' ||
          o.formType == 'datetime' ||
          o.formType == 'user' ||
          o.formType == 'category'
        ) {
          if (!o.value || o.value.length === 0) {
            this.$message.error('请选择筛选条件的值！')
            return
          }
        } else if (!o.value && o.value !== 0) {
          this.$message.error('请输入筛选条件的值！')
          return
        }
      }
      const obj = {}
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
        } else if (o.formType == 'category') {
          obj[o.fieldName] = {
            condition: 'is',
            value: o.value[o.value.length - 1],
            formType: o.formType,
            name: o.fieldName
          }
        } else if (o.formType == 'map_address') {
          let value = ''
          for (const key in o.address) {
            if (o.address[key]) {
              value += o.address[key] + ','
            }
          }
          obj[o.fieldName] = {
            value: value,
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
      const data = {
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

.distpicker-address-wrapper /deep/ select {
  border-radius: 2px;
  font-size: 13px;
  height: 34px;
  -webkit-appearance: none;
  background-image: url(data:image/png;base64,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czp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgQ0MgMjAxOCAoTWFjaW50b3NoKSIgeG1wOkNyZWF0ZURhdGU9IjIwMTgtMDctMjdUMDc6NTY6NDctMDc6MDAiIHhtcDpNb2RpZnlEYXRlPSIyMDE4LTA3LTI3VDA3OjU5OjA4LTA3OjAwIiB4bXA6TWV0YWRhdGFEYXRlPSIyMDE4LTA3LTI3VDA3OjU5OjA4LTA3OjAwIiBkYzpmb3JtYXQ9ImltYWdlL3BuZyIgcGhvdG9zaG9wOkNvbG9yTW9kZT0iMyIgcGhvdG9zaG9wOklDQ1Byb2ZpbGU9IkRpc3BsYXkiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6OWVmMTg3ZTUtMWVmNy00NzlhLWE1Y2QtMmVkMTE5NjQ2Y2ZmIiB4bXBNTTpEb2N1bWVudElEPSJhZG9iZTpkb2NpZDpwaG90b3Nob3A6MTRmNTk5YTMtYTc1Zi05NDQxLTk3ZDQtMmY3OTNjNWQ3NTI3IiB4bXBNTTpPcmlnaW5hbERvY3VtZW50SUQ9InhtcC5kaWQ6M2FmYmY3ZWYtNWFiOS00MTk4LWFjODUtNzY2NzE2NDk2ODM1Ij4gPHhtcE1NOkhpc3Rvcnk+IDxyZGY6U2VxPiA8cmRmOmxpIHN0RXZ0OmFjdGlvbj0iY3JlYXRlZCIgc3RFdnQ6aW5zdGFuY2VJRD0ieG1wLmlpZDozYWZiZjdlZi01YWI5LTQxOTgtYWM4NS03NjY3MTY0OTY4MzUiIHN0RXZ0OndoZW49IjIwMTgtMDctMjdUMDc6NTY6NDctMDc6MDAiIHN0RXZ0OnNvZnR3YXJlQWdlbnQ9IkFkb2JlIFBob3Rvc2hvcCBDQyAyMDE4IChNYWNpbnRvc2gpIi8+IDxyZGY6bGkgc3RFdnQ6YWN0aW9uPSJzYXZlZCIgc3RFdnQ6aW5zdGFuY2VJRD0ieG1wLmlpZDo5ZWYxODdlNS0xZWY3LTQ3OWEtYTVjZC0yZWQxMTk2NDZjZmYiIHN0RXZ0OndoZW49IjIwMTgtMDctMjdUMDc6NTk6MDgtMDc6MDAiIHN0RXZ0OnNvZnR3YXJlQWdlbnQ9IkFkb2JlIFBob3Rvc2hvcCBDQyAyMDE4IChNYWNpbnRvc2gpIiBzdEV2dDpjaGFuZ2VkPSIvIi8+IDwvcmRmOlNlcT4gPC94bXBNTTpIaXN0b3J5PiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/Pr46tjAAAACBSURBVCjPY/j//z8DOgaCVVjFsSgMB2IQIxuvYiBgBuJHUMXfgVgCn+I+qEIYXoxVMRBYoymEYX9sio/hUHz79+/fCMVAUIxDIQy3Q9UxyAPxPwKKQdgEHKZAfI4IvJ8BW+DjwoNFMUkeJCnoSIoUkqOb5IREUhIlOfGTlK0IZVgAkODRpD7i30MAAAAASUVORK5CYII=);
  background-position: calc(100% - 4px) 50%;
  background-repeat: no-repeat;
  background-size: 5px 11px;
}
</style>
