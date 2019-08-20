<template>
  <flexbox orient="vertical"
           align="stretch"
           :style="{'height': contentHeight + 'px'}">
    <div class="title">{{'编辑'+getTitle()+'字段'}}</div>
    <el-container class="wrapper">
      <el-aside class="left">
        <div class="mini-title">字段库</div>
        <ul>
          <draggable class="list-wrapper"
                     :list="fieldList"
                     :options="{group: {pull: 'clone', put: false, name: 'list'},forceFallback:true, sort:false }"
                     :clone="handleMove"
                     @end="handleEnd">
            <li class="field-item"
                v-for="item in fieldList"
                @click="handleClick(item)"
                :key="item.id">
              <img class="icon"
                   :src="item.icon" />
              <span>{{item.name}}</span>
            </li>
          </draggable>
        </ul>
      </el-aside>
      <el-container class="content"
                    v-loading="loading">
        <el-header>
          <el-button type="text"
                     style="padding: 8px 22px;border-radius:2px;"
                     @click="handlePreview">预览</el-button>
          <el-button type="primary"
                     style="padding: 8px 22px;border-radius:2px;"
                     @click="handleSave">保存</el-button>
          <el-button style="padding: 8px 22px;border-radius:2px;"
                     @click="handleCancel">返回</el-button>

        </el-header>
        <el-main>
          <draggable :list="fieldArr"
                     :options="{group: 'list',forceFallback:true, fallbackClass:'draggingStyle'}"
                     @end="handleListMove">
            <component v-for="(item, index) in fieldArr"
                       :class="{selected: selectedIndex == index}"
                       :isShow="selectedIndex == index && (item.operating == null || item.operating == 0 || item.operating == 2)"
                       :key="index"
                       :attr="item"
                       :is="item | typeToComponentName"
                       @delete="handleDelete(item, index)"
                       @select="handleChildSelect"
                       @click.native="handleSelect(item, index)">
            </component>
          </draggable>
          <p class="no-list"
             v-if="fieldArr.length == 0">从左侧点击或拖拽来添加字段</p>
        </el-main>
      </el-container>
      <el-aside class="right"
                width="310px">
        <div class="mini-title">字段属性</div>
        <field-info v-if="form"
                    :field="form"
                    :canTransform="canTransform"
                    :transformData="transformData"></field-info>
      </el-aside>
    </el-container>
    <!-- 表单预览 -->
    <preview-field-view v-if="showTablePreview"
                        :types="tablePreviewData.type"
                        :typesId="tablePreviewData.id"
                        :label="tablePreviewData.label"
                        @hiden-view="showTablePreview=false"></preview-field-view>
  </flexbox>
</template>

<script>
import crmTypeModel from '@/views/customermanagement/model/crmTypeModel'
import {
  customFieldHandle,
  customFieldList
} from '@/api/systemManagement/SystemCustomer'
import { filedGetField } from '@/api/customermanagement/common'
import PreviewFieldView from '@/views/SystemManagement/components/previewFieldView'
import {
  SingleLineText,
  MultiLineText,
  SelectForm,
  CheckboxForm,
  FileForm,
  TableForm
} from './components/fields'
import draggable from 'vuedraggable'
import Field from './model/field'
import FieldList from './model/fieldList'
import FieldInfo from './components/FieldInfo'
import { objDeepCopy, regexIsCRMMobile, regexIsCRMEmail } from '@/utils'

export default {
  name: 'handlefield',
  components: {
    SingleLineText,
    MultiLineText,
    SelectForm,
    CheckboxForm,
    FileForm,
    TableForm,
    draggable,
    FieldInfo,
    PreviewFieldView
  },
  computed: {
    // 能转移
    canTransform() {
      return this.$route.params.type == 'crm_leads'
    }
  },
  data() {
    return {
      fieldList: FieldList,
      fieldArr: [], // 数据没有返回时 根据null 判断不能操作
      movedItem: {},
      selectedIndex: -1,
      rejectHandle: true, // 请求未获取前不能操作
      /** 右边展示数据 */
      form: null, // operating 0 改删 1改 2删 3无
      loading: false, // 加载动画
      // 展示表单预览
      tablePreviewData: { types: '', id: '' },
      showTablePreview: false,
      contentHeight: document.documentElement.clientHeight - 100,
      // 转移匹配字段源
      transformData: null
    }
  },
  filters: {
    /** 根据type 找到组件 */
    typeToComponentName(item) {
      if (
        item.formType === 'text' ||
        item.formType === 'number' ||
        item.formType === 'floatnumber' ||
        item.formType === 'mobile' ||
        item.formType === 'email' ||
        item.formType === 'date' ||
        item.formType === 'datetime' ||
        item.formType === 'user' ||
        item.formType === 'structure' ||
        item.formType === 'contacts' ||
        item.formType === 'customer' ||
        item.formType === 'contract' ||
        item.formType === 'business' ||
        item.type == 0
      ) {
        return 'SingleLineText'
      } else if (item.formType === 'textarea') {
        return 'MultiLineText'
      } else if (item.formType === 'select') {
        return 'SelectForm'
      } else if (item.formType === 'checkbox') {
        return 'CheckboxForm'
      } else if (item.formType === 'file') {
        return 'FileForm'
      } else if (item.formType === 'form') {
        return 'TableForm'
      }
    }
  },
  watch: {
    selectedIndex: {
      handler(newVal) {
        if (newVal >= 0) {
          this.form = this.fieldArr[newVal]
        } else {
          this.form = null
        }
      },
      deep: true,
      immediate: true
    }
  },
  mounted() {
    window.onresize = () => {
      this.contentHeight = document.documentElement.clientHeight - 100
    }
    // 获取当前模块的自定义数据
    this.getCustomInfo()

    // 配置转移字段
    if (this.canTransform) {
      this.getTransformField()
    }
  },
  methods: {
    // 获取当前模块的自定义数据
    getCustomInfo() {
      this.loading = true
      var params = {}
      params.label = this.$route.params.label
      if (this.$route.params.type === 'oa_examine') {
        params.categoryId = this.$route.params.id
      }
      customFieldList(params)
        .then(res => {
          for (let index = 0; index < res.data.length; index++) {
            const element = res.data[index]
            if (
              element.formType == 'select' ||
              element.formType == 'checkbox'
            ) {
              var temps = []
              for (let i = 0; i < element.setting.length; i++) {
                // 必须有属性 才能for绑定 所以处理了下数据
                const item = element.setting[i]
                temps.push({ value: item })
              }
              element.showSetting = temps //放到showSeeting上

              // 删除无效的多选默认值
              if (element.formType == 'checkbox') {
                element.defaultValue = element.defaultValue.filter(item => {
                  return element.setting.indexOf(item) != -1
                })
              }
            }
            element.isNull = element.isNull == 1 ? true : false
            element.isUnique = element.isUnique == 1 ? true : false
          }
          this.fieldArr = res.data
          if (res.data.length > 0) {
            this.selectedIndex = 0
            this.form = this.fieldArr[0]
          }
          this.rejectHandle = false
          this.loading = false
        })
        .catch(error => {
          this.loading = false
        })
    },
    // 删除一行自定义数据
    handleDelete(item, index) {
      this.$confirm('确定删除该自定义字段吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.fieldArr.splice(index, 1)
          this.$nextTick(() => {
            this.selectedIndex = -1
          })
        })
        .catch(() => {})
    },
    // 主列表的选择
    handleSelect(item, index) {
      this.selectedIndex = index
      if (this.selectedIndex === index) {
        // 表自定义字段的刷新
        if (index >= 0) {
          this.form = this.fieldArr[index]
        }
      }
    },
    // 表的选择
    handleChildSelect(data) {
      this.form = data.data
    },
    // 预览表单
    handlePreview() {
      this.tablePreviewData = this.$route.params
      this.showTablePreview = true
    },
    // 保存数据
    handleSave() {
      if (this.rejectHandle) return
      var save = true

      var tempFieldArr = objDeepCopy(this.fieldArr)
      for (let index = 0; index < tempFieldArr.length; index++) {
        const item = tempFieldArr[index]

        item.isNull = item.isNull == true ? 1 : 0
        item.isUnique = item.isUnique == true ? 1 : 0
        if (!item.name) {
          save = false
          this.$message({
            type: 'error',
            message: '第' + (index + 1) + '行的自定义字段，标识名不能为空'
          })
          break
        } else if (item.formType == 'select' || item.formType == 'checkbox') {
          var temps = []
          for (let i = 0; i < item.showSetting.length; i++) {
            const element = item.showSetting[i]
            if (element.value) {
              temps.push(element.value)
            }
          }
          item.options = temps.join(',')
          if (item.formType == 'checkbox') {
            item.defaultValue = item.defaultValue.join(',')
          }
        }
        item.type = this.getTypeFromFormType(item.formType)
      }

      if (save) {
        var params = {}
        params.data = tempFieldArr
        params.label = this.$route.params.label
        if (this.$route.params.type === 'oa_examine') {
          params.categoryId = this.$route.params.id
        }
        for (let item of params.data) {
          for (let key in item) {
            if (JSON.stringify(item[key]) == '{}') {
              item[key] = null
            }
          }
        }
        customFieldHandle(params)
          .then(res => {
            this.$message({
              type: 'success',
              message: '操作成功'
            })
            this.getCustomInfo()
          })
          .catch(() => {
            this.getCustomInfo()
          })
      }
    },
    getTypeFromFormType(formType) {
      return (
        {
          text: 1,
          textarea: 2,
          select: 3,
          date: 4,
          number: 5,
          floatnumber: 6,
          mobile: 7,
          file: 8,
          checkbox: 9,
          user: 10,
          structure: 12,
          datetime: 13,
          email: 14,
          customer: 15,
          business: 16,
          contacts: 17,
          map_address: 18,
          category: 19,
          contract: 20,
          receivables_plan: 21
        }[formType] || '0'
      )
    },
    // 返回
    handleCancel() {
      this.$router.go(-1)
    },
    /**  拖拽操作部分 */
    // 从左侧移动到右侧
    handleEnd(e) {
      if (!this.rejectHandle) {
        let newField = new Field({
          name: this.movedItem.name,
          formType: this.movedItem.formType
        })
        // 如果当前选中的table 则加入到table中
        if (
          this.form &&
          this.form.formType === 'form' &&
          this.movedItem.formType !== 'form'
        ) {
          this.form.formValue.push(newField)
        } else {
          this.fieldArr.push(newField)
          this.selectedIndex = this.fieldArr.length - 1
        }
      }
    },
    // 从左侧移动到右侧 时候的数据对象
    handleMove(obj) {
      this.movedItem = obj
    },
    // 点击左侧进行添加
    handleClick(obj) {
      this.movedItem = obj
      this.handleEnd()
    },
    // list move
    handleListMove(e) {
      this.selectedIndex = e.newIndex
    },
    /**  拖拽操作部分 */
    /**  左上角title */
    getTitle() {
      if (this.$route.params.type == 'crm_leads') {
        return '线索'
      } else if (this.$route.params.type == 'crm_customer') {
        return '客户'
      } else if (this.$route.params.type == 'crm_contacts') {
        return '联系人'
      } else if (this.$route.params.type == 'crm_business') {
        return '商机'
      } else if (this.$route.params.type == 'crm_contract') {
        return '合同'
      } else if (this.$route.params.type == 'crm_product') {
        return '产品'
      } else {
        return ''
      }
    },
    /**
     * 获取添加字段
     */
    getTransformField() {
      filedGetField({
        label: crmTypeModel['customer']
      })
        .then(res => {
          let data = {
            text: [],
            textarea: [],
            select: [],
            checkbox: [],
            number: [],
            floatnumber: [],
            mobile: [],
            email: [],
            date: [],
            datetime: [],
            user: [],
            structure: []
          }

          for (let index = 0; index < res.data.length; index++) {
            const element = res.data[index]
            let array = data[element.formType]
            if (array) {
              array.push({
                label: element.name,
                value: element.fieldId
              })
            }
          }

          this.transformData = data
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/mixin.scss';

.el-form-item {
  margin: 0;
  padding-bottom: 16px;
  border-bottom: 1px solid #e1e1e1;
  .desc {
    color: #999;
    font-size: 12px;
  }
  &:last-child {
    margin-top: 15px;
  }
}

.title {
  padding-bottom: 20px;
  font-size: 18px;
  color: #333;
  font-weight: normal;
}

.wrapper {
  padding: 10px 0;
  background-color: white;
  min-width: 1000px;
  overflow: hidden;
  flex: 1;
  -moz-user-select: none;
  -webkit-user-select: none;
  -ms-user-select: none;
  -khtml-user-select: none;
  user-select: none;
  .left {
    min-width: 310px;
    .mini-title {
      font-size: 14px;
      margin: 30px 0 20px 20px;
    }
    .list-wrapper {
      padding: 0 20px;
      display: flex;
      justify-content: space-between;
      flex-wrap: wrap;
      .field-item {
        width: 130px;
        height: 30px;
        font-size: 13px;
        padding: 0 10px;
        background: #ebf3ff;
        margin-bottom: 10px;
        border-radius: 3px;
        cursor: pointer;
        @include left;
        .icon {
          color: #74b2f2;
          margin-right: 8px;
          width: 20px;
          height: 20px;
        }
      }
    }
  }

  .content {
    border-left: 1px solid #e1e1e1;
    border-right: 1px solid #e1e1e1;
    .el-header {
      border-bottom: 1px solid #e1e1e1;
      @include right;
    }
    .el-main {
      padding: 0;
      .selected {
        border-left: 2px solid #46cdcf;
        background: #f7f8fa;
      }
      .no-list {
        margin: 200px 0;
        color: #ccc;
        @include center;
      }
    }
  }

  .right {
    font-size: 14px;
    .mini-title {
      height: 60px;
      border-bottom: 1px solid #e1e1e1;
      padding-left: 20px;
      @include left;
    }
  }
}
</style>
