<template>
  <create-view
    :loading="loading"
    :body-style="{ height: '100%'}">
    <flexbox
      direction="column"
      align="stretch"
      class="crm-create-container">
      <flexbox class="crm-create-header">
        <div style="flex:1;font-size:17px;color:#333;">{{ title }}</div>
        <img
          class="close"
          src="@/assets/img/task_close.png"
          @click="hidenView" >
      </flexbox>
      <flexbox
        class="crm-create-flex"
        direction="column"
        align="stretch">
        <div
          v-show="currentPage == 1"
          class="crm-create-body">
          <div class="create-name">基本信息</div>
          <el-form
            ref="crmForm"
            :model="crmForm"
            label-position="top"
            class="crm-create-box">
            <el-form-item
              v-for="(item, index) in crmForm.crmFields"
              :key="item.key"
              :prop="'crmFields.' + index + '.value'"
              :class="{ 'crm-create-block-item': item.showblock, 'crm-create-item': !item.showblock }"
              :rules="crmRules[item.key]"
              :style="{'padding-left': getPaddingLeft(item, index), 'padding-right': getPaddingRight(item, index)}">
              <div
                slot="label"
                style="display: inline-block;">
                <div style="margin:5px 0;font-size:12px;word-wrap:break-word;word-break:break-all;">
                  {{ item.data.name }}
                  <span style="color:#999;">
                    {{ item.data.inputTips ? '（'+item.data.inputTips+'）':'' }}
                  </span>
                </div>
              </div>
              <component
                :is="item.data.formType | typeToComponentName"
                :value="item.value"
                :index="index"
                :item="item"
                :radio="false"
                @value-change="fieldValueChange"/>
            </el-form-item>
          </el-form>
        </div>
        <div
          v-show="currentPage == 2"
          class="crm-create-body">
          <div style="padding: 0 20px; font-size: 12px;">
            <el-radio
              v-model="examineType"
              :label="1">固定审批流</el-radio>
            <div class="examine-items">
              <flexbox
                v-for="(item, index) in examineList"
                :key="index"
                class="examine-item">
                <div class="examine-item-name">第{{ index+1 | numberToZh }}级</div>
                <el-select
                  v-model="item.type"
                  class="examine-item-select"
                  placeholder="请选择"
                  @focus="selectOptionsFocus(item, index)"
                  @change="selectOptionsChange(item)">
                  <el-option
                    v-for="itemOption in item.options"
                    :key="itemOption.value"
                    :label="itemOption.name"
                    :value="itemOption.value"/>
                </el-select>
                <xh-user-cell
                  v-if="item.show"
                  :radio="false"
                  :index="index"
                  :value="item.value"
                  class="examine-item-user"
                  @value-change="flowUserSelect"/>
                <i
                  class="el-icon-remove examine-item-delete"
                  @click="deleteExamineItems(index)"/>
              </flexbox>
            </div>
            <div class="examine-items-add"><span @click="examineItemsAdd">+ 添加审批层级</span></div>
            <div class="examine-add-des">
              <p><span class="examine-add-required">*</span>当选择“负责人主管”审批时。系统仅会通知负责人主管。</p>
              <p><span class="examine-add-required">*</span>当选择多个“指定用户”审批时。如果指定用户没有权限查看对应的合同，系统会通知其审批，但是他无法查看此数据信息。 </p>
              <p><span class="examine-add-required">*</span>当选择“指定用户（任意一人）”表示指定用户中任意一人审批即可。当选择“指定用户（多人会签）”表示 指定用户中所有人都要审批。</p>
            </div>
            <el-radio
              v-model="examineType"
              :label="2">授权审批人</el-radio>
          </div>
        </div>
      </flexbox>
      <div
        v-if="currentPage == 1"
        class="handle-bar">
        <el-button
          class="handle-button"
          @click.native="hidenView">取消</el-button>
        <el-button
          class="handle-button"
          type="primary"
          @click.native="nextPage">下一页</el-button>
      </div>
      <div
        v-if="currentPage == 2"
        class="handle-bar">
        <el-button
          class="handle-button"
          @click.native="hidenView">取消</el-button>
        <el-button
          class="handle-button"
          type="primary"
          @click.native="saveField">保存</el-button>
        <el-button
          class="handle-button"
          type="primary"
          @click.native="currentPage = 1">上一页</el-button>
      </div>
    </flexbox>
  </create-view>
</template>
<script type="text/javascript">
import CreateView from '@/components/CreateView'
import {
  oaExamineCategorySave
} from '@/api/systemManagement/workbench' // 审批类型创建

import {
  XhInput,
  XhTextarea,
  XhSelect,
  XhUserCell,
  XhStrucUserCell
} from '@/components/CreateCom'
import Nzhcn from 'nzh/cn'

export default {
  name: 'CreateExamineCategory', // 所有新建效果的view
  components: {
    CreateView,
    XhInput,
    XhTextarea,
    XhSelect,
    XhUserCell,
    XhStrucUserCell
  },
  filters: {
    /** 根据type 找到组件 */
    typeToComponentName(formType) {
      if (formType == 'text') {
        return 'XhInput'
      } else if (formType == 'textarea') {
        return 'XhTextarea'
      } else if (formType == 'select') {
        return 'XhSelect'
      } else if (formType == 'structure') {
        return 'XhStrucUserCell'
      }
    },
    numberToZh: function(value) {
      return Nzhcn.encodeS(value)
    }
  },
  props: {
    handle: {
      type: Object,
      default: () => {
        return {
          type: 'examineflow', // examine 审批 审批流 examineflow
          action: 'save', // save 创建  update 编辑
          id: '',
          data: null // 编辑数据
        }
      }
    }
  },
  data() {
    return {
      // 标题展示名称
      loading: false,
      // 自定义字段验证规则
      crmRules: {},
      // 自定义字段信息表单
      crmForm: {
        crmFields: []
      },
      // 总两页 当前页
      currentPage: 1,
      examineType: 1, // 1 固定审批 2 授权审批
      examineList: [
        {
          type: 1,
          value: [],
          show: false,
          options: [
            { name: '负责人主管', value: 1 },
            { name: '指定用户（任意一人）', value: 2 },
            { name: '指定用户（多人会签）', value: 3 }
          ]
        }
      ]
    }
  },
  computed: {
    title() {
      if (this.handle.action === 'save') {
        return '新建审批类型'
      } else if (this.handle.action === 'update') {
        return '编辑审批类型'
      }
    }
  },
  mounted() {
    document.body.appendChild(this.$el)

    this.getField()
    if (this.handle.data) {
      // data 存在就处理 save
      if (this.handle.data.examineType && this.handle.data.examineType === 1) {
        this.examineList = []
        for (let index = 0; index < this.handle.data.stepList.length; index++) {
          const element = this.handle.data.stepList[index]
          var item = {}
          item['type'] = element.stepType
          if (element.stepType === 2 || element.stepType === 3) {
            item['show'] = true
            item['value'] = element.userList
          } else {
            item['show'] = false
            item['value'] = []
          }
          if (index === 0) {
            item['options'] = [
              { name: '负责人主管', value: 1 },
              { name: '指定用户（任意一人）', value: 2 },
              { name: '指定用户（多人会签）', value: 3 }
            ]
          } else {
            item['options'] = [
              { name: '负责人主管', value: 1 },
              { name: '指定用户（任意一人）', value: 2 },
              { name: '指定用户（多人会签）', value: 3 },
              { name: '上一级审批人主管', value: 4 }
            ]
          }
          this.examineList.push(item)
        }
      } else {
        this.examineType = 2
      }
    }
  },
  destroyed() {
    // remove DOM node after destroy
    if (this.$el && this.$el.parentNode) {
      this.$el.parentNode.removeChild(this.$el)
    }
  },
  methods: {
    // 字段的值更新
    fieldValueChange(data) {
      var item = this.crmForm.crmFields[data.index]
      item.value = data.value
      // 无事件的处理 后期可换成input实现
      // if (item.data.formType == 'structure') {
      //   this.$refs.crmForm.validateField('crmFields.' + data.index + '.value')
      // }
    },
    // 获取自定义字段
    getField() {
      var field = []
      // 如果是审批添加 加入审批相关信息
      field.push({
        field: 'title',
        formType: 'text',
        isNull: 1,
        name: '审批类型名称',
        setting: [],
        inputTips: '',
        value: this.handle.data ? this.handle.data.title : ''
      })
      field.push({
        field: 'dept',
        formType: 'structure',
        isNull: 0,
        name: '可视范围',
        setting: [],
        inputTips: '默认全公司',
        value: {
          users: this.handle.data ? this.handle.data.userIds : [],
          strucs: this.handle.data ? this.handle.data.deptIds : []
        }
      })
      field.push({
        field: 'remarks',
        formType: 'textarea',
        isNull: 0,
        name: '审批类型说明',
        setting: [],
        inputTips: '',
        value: this.handle.data ? this.handle.data.remarks : ''
      })

      this.getcrmRulesAndModel(field)
    },
    // 根据自定义字段获取自定义字段规则
    getcrmRulesAndModel(list) {
      for (let index = 0; index < list.length; index++) {
        const item = list[index]
        /** 规则数据 */
        var tempList = []

        // 验证必填
        if (item.isNull == 1) {
          tempList.push({
            required: true,
            message: item.name + '不能为空',
            trigger: ['blur', 'change']
          })
        }

        this.crmRules[item.field] = tempList

        /** 表单数据 */
        var params = {}
        params['value'] = item.value
        params['key'] = item.field
        params['data'] = item
        if (item.formType == 'textarea') {
          params['showblock'] = true // 展示整行效果
        }
        this.crmForm.crmFields.push(params)
      }
    },
    // 保存数据
    saveField() {
      this.$refs.crmForm.validate(valid => {
        if (valid) {
          if (this.validStepsInfo()) {
            this.submiteParams(this.crmForm.crmFields)
          }
        } else {
          this.$message.error('请完善必填信息')
          return false
        }
      })
    },
    validStepsInfo() {
      for (let index = 0; index < this.examineList.length; index++) {
        const element = this.examineList[index]
        if (
          (element.type === 2 || element.type === 3) &&
          element.value.length === 0
        ) {
          this.$message.error('请添加员工')
          return false
        }
      }
      return true
    },
    /** 上传 */
    submiteParams(array) {
      this.loading = true
      var params = this.getSubmiteParams(array)
      if (this.handle.action == 'update') {
        params.id = this.handle.id
      }
      oaExamineCategorySave(params)
        .then(res => {
          this.loading = false
          // 回到保存成功
          this.$emit('save')
          if (this.handle.action == 'save') {
            // 如果是新建 提示去创建表单
            this.$confirm('您将继续完成审批表单的创建', '创建成功', {
              showCancelButton: false,
              confirmButtonText: '确定',
              type: 'warning'
            })
              .then(() => {
                this.hidenView()
                this.$router.push({
                  name: 'handlefield',
                  params: {
                    type: 'oa_examine',
                    label: '10',
                    id: res.data.categoryId
                  }
                })
              })
              .catch(() => {})
          } else {
            this.$message.success('操作成功')
            this.hidenView()
          }
        })
        .catch(() => {
          this.loading = false
        })
    },
    // 请求参数
    getSubmiteParams(array) {
      var params = {}
      for (let index = 0; index < array.length; index++) {
        const element = array[index]
        // 关联产品数据需要特殊拼接
        if (element.key === 'dept') {
          params['userIds'] = element.value['users'].map(function(item) {
            return item.userId
          })
          params['deptIds'] = element.value['strucs'].map(function(item) {
            return item.id
          })
        } else {
          params[element.key] = element.value
        }
      }

      var steps = []
      for (let index = 0; index < this.examineList.length; index++) {
        const element = this.examineList[index]
        steps.push({
          stepType: element.type,
          checkUserId: element.value.map(function(item) {
            return item.userId
          })
        })
      }
      params['examineType'] = this.examineType
      params['step'] = steps
      return params
    },
    // 下一页
    nextPage() {
      this.$refs.crmForm.validate(valid => {
        if (valid) {
          this.currentPage = 2
        }
      })
    },
    selectOptionsChange(item) {
      if (item.type == 2 || item.type == 3) {
        item.show = true
      } else {
        item.show = false
      }
    },
    selectOptionsFocus(item, index) {
      if (this.examineList.length > 1) {
        var lastItem = index > 0 ? this.examineList[index - 1] : null
        var nextItem = null
        if (index < this.examineList.length - 1) {
          var nextItem = this.examineList[index + 1]
        }

        var removeTwo = false
        var removeThree = false
        var removeFour = false

        // 任一 会签 的下面不能是 上一级
        if (lastItem && (lastItem.type === 2 || lastItem.type === 3)) {
          removeFour = true
        }

        // 上一级 的上面不能是 会签 任一
        if (nextItem && nextItem.type === 4) {
          removeTwo = true
          removeThree = true
        }

        var options = [{ name: '负责人主管', value: 1 }]

        if (!removeTwo) {
          options.push({ name: '指定用户（任意一人）', value: 2 })
        }
        if (!removeThree) {
          options.push({ name: '指定用户（多人会签）', value: 3 })
        }
        if (!removeFour) {
          options.push({ name: '上一级审批人主管', value: 4 })
        }

        item.options = options
      }
    },
    examineItemsAdd() {
      this.examineList.push({
        type: 1,
        value: [],
        show: false,
        options: [
          { name: '负责人主管', value: 1 },
          { name: '指定用户（任意一人）', value: 2 },
          { name: '指定用户（多人会签）', value: 3 },
          { name: '上一级审批人主管', value: 4 }
        ]
      })
    },
    // 类型选择
    flowUserSelect(data) {
      var item = this.examineList[data.index]
      item.value = data.value
    },
    deleteExamineItems(index) {
      this.examineList.splice(index, 1)
    },
    hidenView() {
      this.$emit('hiden-view')
    },
    // 获取左边padding
    getPaddingLeft(item, index) {
      if (item.showblock && item.showblock == true) {
        return '0'
      }
      return index % 2 == 0 ? '0' : '25px'
    },
    // 获取左边padding
    getPaddingRight(item, index) {
      if (item.showblock && item.showblock == true) {
        return '0'
      }
      return index % 2 == 0 ? '25px' : '0'
    }
  }
}
</script>
<style lang="scss" scoped>
.crm-create-container {
  position: relative;
  height: 100%;
}

.crm-create-flex {
  position: relative;
  overflow-x: hidden;
  overflow-y: auto;
  flex: 1;
}

.crm-create-header {
  height: 40px;
  margin-bottom: 20px;
  padding: 0 10px;
  flex-shrink: 0;
  .close {
    display: block;
    width: 40px;
    height: 40px;
    margin-right: -10px;
    padding: 10px;
  }
}
.create-name {
  font-size: 12px;
  padding: 0 10px;
  margin-left: 15px;
  margin-bottom: 10px;
  color: #333333;
  border-left: 2px solid #46cdcf;
}

.crm-create-body {
  flex: 1;
  overflow-x: hidden;
  overflow-y: auto;
}

/** 将其改变为flex布局 */
.crm-create-box {
  display: flex;
  flex-wrap: wrap;
  padding: 0 20px;
}

.crm-create-item {
  flex: 0 0 50%;
  flex-shrink: 0;
  padding-bottom: 10px;
}

// 占用一整行
.crm-create-block-item {
  flex: 0 0 100%;
  flex-shrink: 0;
  padding-bottom: 10px;
}

.el-form-item /deep/ .el-form-item__label {
  line-height: normal;
  font-size: 13px;
  color: #333333;
  position: relative;
  padding-left: 8px;
  padding-bottom: 0;
}

.el-form /deep/ .el-form-item {
  margin-bottom: 0px;
}

.el-form /deep/ .el-form-item.is-required .el-form-item__label:before {
  content: '*';
  color: #f56c6c;
  margin-right: 4px;
  position: absolute;
  left: 0;
  top: 5px;
}

.examine-items {
  padding: 10px 0;
}

.examine-item {
  padding-bottom: 8px;
  .examine-item-name {
    width: 60px;
    padding-left: 20px;
    font-size: 13px;
    margin-right: 20px;
  }
  .examine-item-select {
    margin-right: 20px;
  }
  .examine-item-user {
    flex: 1;
    margin-right: 42px;
  }
  .examine-item-delete {
    color: #ff6767;
    font-size: 22px;
    margin: 0 10px;
    display: none;
  }
}

.examine-item:hover {
  .examine-item-delete {
    display: block;
  }
  .examine-item-user {
    margin-right: 0px;
  }
}

.examine-items-add {
  padding: 5px 0 20px 0;
  font-size: 13px;
  color: $xr-color-primary;
}

.examine-add-des {
  font-size: 12px;
  background-color: #fffcf0;
  padding: 10px;
  line-height: 23px;
  color: #999;
  margin-bottom: 10px;
  .examine-add-required {
    color: #ff6767;
  }
}
.handle-bar {
  position: relative;
  .handle-button {
    float: right;
    margin-top: 5px;
    margin-right: 20px;
  }
}
</style>
