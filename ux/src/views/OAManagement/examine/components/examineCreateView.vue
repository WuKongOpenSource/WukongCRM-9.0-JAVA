<template>
  <create-view :loading="loading"
               :body-style="{ height: '100%'}">
    <flexbox direction="column"
             align="stretch"
             class="crm-create-container">
      <flexbox class="crm-create-header">
        <div style="flex:1;font-size:17px;color:#333;">{{title}}</div>
        <img @click="hidenView"
             class="close"
             src="@/assets/img/task_close.png" />
      </flexbox>
      <div class="crm-create-flex">
        <!-- 基本信息 -->
        <create-sections title="基本信息">
          <flexbox direction="column"
                   align="stretch">
            <div class="crm-create-body">
              <el-form ref="crmForm"
                       :model="crmForm"
                       label-position="top"
                       class="crm-create-box">
                <el-form-item v-for="(item, index) in this.crmForm.crmFields"
                              :key="item.key"
                              :prop="'crmFields.' + index + '.value'"
                              :class="{ 'crm-create-block-item': item.showblock, 'crm-create-item': !item.showblock }"
                              :rules="crmRules[item.key]"
                              :style="{'padding-left': getPaddingLeft(item, index), 'padding-right': getPaddingRight(item, index)}">
                  <div slot="label"
                       style="display: inline-block;">
                    <div style="margin:5px 0;font-size:12px;word-wrap:break-word;word-break:break-all;">
                      {{item.data.name}}
                      <span style="color:#999;">
                        {{item.data.inputTips ? '（'+item.data.inputTips+'）':''}}
                      </span>
                    </div>
                  </div>
                  <!-- 员工 和部门 为多选（radio=false）  relation 相关合同商机使用-->
                  <component :is="item.data.formType | typeToComponentName"
                             :value="item.value"
                             :index="index"
                             :item="item"
                             :relation="item.relation"
                             :radio="false"
                             :disabled="item.disabled"
                             @value-change="fieldValueChange">
                  </component>
                </el-form-item>
              </el-form>
            </div>
          </flexbox>
        </create-sections>
        <!-- 图片附件 -->
        <div class="img-accessory">
          <div class="img-box">
            <el-upload ref="imageUpload"
                       :action="crmFileSaveUrl"
                       :headers="httpHeader"
                       name="file"
                       :data="{type: 'img', batchId: batchId}"
                       multiple
                       accept="image/*"
                       list-type="picture-card"
                       :on-preview="handleFilePreview"
                       :before-remove="beforeRemove"
                       :on-success="imgFileUploadSuccess"
                       :file-list="imgFileList">
              <p class="add-img">
                <span class="el-icon-picture"></span>
                <span>添加图片</span>
              </p>
              <i class="el-icon-plus"></i>
            </el-upload>
          </div>
          <p class="add-accessory">
            <el-upload ref="fileUpload"
                       :action="crmFileSaveUrl"
                       :headers="httpHeader"
                       name="file"
                       :data="{type: 'file', batchId: batchId}"
                       multiple
                       accept="*.*"
                       :on-preview="handleFilePreview"
                       :before-remove="handleFileRemove"
                       :on-success="fileUploadSuccess"
                       :file-list="fileList">
              <p>
                <img src="@/assets/img/relevance_file.png"
                     alt="">
                添加附件
              </p>
            </el-upload>
          </p>
        </div>
        <!-- 关联业务 -->
        <related-business class="related-business"
                          :selectedInfos="relatedBusinessInfo"
                          @value-change="relativeValueChange"></related-business>
        <!-- 审核信息 -->
        <create-sections v-if="showExamine"
                         title="审核信息">
          <div slot="header"
               v-if="examineInfo.examineType===1 || examineInfo.examineType===2"
               class="examine-type">{{examineInfo.examineType===1 ? '固定审批流' : '授权审批人'}}</div>
          <create-examine-info ref="examineInfo"
                               types="oa_examine"
                               :typesId="categoryId"
                               @value-change="examineValueChange"></create-examine-info>
        </create-sections>
      </div>

      <div class="handle-bar">
        <el-button class="handle-button"
                   @click.native="hidenView">取消</el-button>
        <el-button class="handle-button"
                   type="primary"
                   @click.native="saveField()">保存</el-button>
      </div>
    </flexbox>
  </create-view>
</template>
<script type="text/javascript">
import { filedGetField, filedValidates } from '@/api/customermanagement/common'
import { OaExamineGetField } from '@/api/oamanagement/examine'
import { crmFileSave, crmFileDelete, crmFileSaveUrl } from '@/api/common'
import axios from 'axios'
import { oaExamineSaveAndUpdate } from '@/api/oamanagement/examine'

import CreateView from '@/components/CreateView'
import CreateSections from '@/components/CreateSections'
import CreateExamineInfo from '@/components/Examine/CreateExamineInfo'
import XhExpenses from './xhExpenses' // 报销事项
import XhLeaves from './xhLeaves' // 出差事项
import RelatedBusiness from './relatedBusiness'

import {
  regexIsNumber,
  regexIsCRMNumber,
  regexIsCRMMoneyNumber,
  regexIsCRMMobile,
  regexIsCRMEmail,
  guid,
  objDeepCopy
} from '@/utils'

import {
  XhInput,
  XhTextarea,
  XhSelect,
  XhMultipleSelect,
  XhDate,
  XhDateTime,
  XhUserCell,
  XhStructureCell,
  XhFiles,
  CrmRelativeCell
} from '@/components/CreateCom'

export default {
  name: 'examine-create-view', // 所有新建效果的view
  components: {
    CreateView,
    CreateSections,
    CreateExamineInfo, // 审核信息
    XhInput,
    XhTextarea,
    XhSelect,
    XhMultipleSelect,
    XhDate,
    XhDateTime,
    XhUserCell,
    XhStructureCell,
    XhFiles,
    CrmRelativeCell,
    XhExpenses,
    XhLeaves,
    RelatedBusiness
  },
  computed: {
    /** 审批 下展示审批人信息 */
    showExamine() {
      return true
    },
    crmFileSaveUrl() {
      return crmFileSaveUrl
    },
    httpHeader() {
      return {
        'Admin-Token': axios.defaults.headers['Admin-Token']
      }
    }
  },
  watch: {},
  data() {
    return {
      // 标题展示名称
      title: '',
      loading: false,
      // 自定义字段验证规则
      crmRules: {},
      // 自定义字段信息表单
      crmForm: {
        crmFields: []
      },
      batchId: guid(),
      // 图片附件
      imgFileList: [],
      fileList: [],
      // 审批信息
      examineInfo: {},
      relatedBusinessInfo: {} // 关联业务信息
    }
  },
  filters: {
    /** 根据type 找到组件 */
    typeToComponentName(formType) {
      if (
        formType == 'text' ||
        formType == 'number' ||
        formType == 'floatnumber' ||
        formType == 'mobile' ||
        formType == 'email'
      ) {
        return 'XhInput'
      } else if (formType == 'textarea') {
        return 'XhTextarea'
      } else if (formType == 'select') {
        return 'XhSelect'
      } else if (formType == 'checkbox') {
        return 'XhMultipleSelect'
      } else if (formType == 'date') {
        return 'XhDate'
      } else if (formType == 'datetime') {
        return 'XhDateTime'
      } else if (formType == 'user') {
        return 'XhUserCell'
      } else if (formType == 'structure') {
        return 'XhStructureCell'
      } else if (formType == 'file') {
        return 'XhFiles'
      } else if (
        formType == 'contacts' ||
        formType == 'customer' ||
        formType == 'contract' ||
        formType == 'business'
      ) {
        return 'CrmRelativeCell'
      } else if (formType == 'examine_cause') {
        return 'XhExpenses'
      } else if (formType == 'business_cause') {
        return 'XhLeaves'
      }
    }
  },
  props: {
    // 类型ID
    categoryId: {
      type: [String, Number],
      default: ''
    },
    type: [String, Number],
    // 类型名称
    categoryTitle: {
      type: String,
      default: ''
    },
    /**
     * save:添加、update:编辑(action_id)、read:详情、index:列表
     * relative: 相关 添加
     */
    action: {
      type: Object,
      default: () => {
        return {
          type: 'save',
          id: ''
        }
      }
    }
  },
  mounted() {
    // 获取title展示名称
    document.body.appendChild(this.$el)
    this.title = this.getTitle()
    this.getField()

    if (this.action.type == 'update') {
      this.batchId = this.action.data.batchId
    }
  },
  methods: {
    // 关联业务的值更新
    relativeValueChange(data) {
      this.relatedBusinessInfo = data.value
    },
    // 审批信息值更新
    examineValueChange(data) {
      this.examineInfo = data
    },
    // 字段的值更新
    fieldValueChange(data) {
      var item = this.crmForm.crmFields[data.index]
      item.value = data.value

      // 出差事项
      if (item.data.formType == 'business_cause' && item.value.update) {
        for (let index = 0; index < this.crmForm.crmFields.length; index++) {
          const element = this.crmForm.crmFields[index]
          if (element.key === 'duration') {
            element.value = item.value.duration
            break
          }
        }
        // 报销
      } else if (item.data.formType == 'examine_cause' && item.value.update) {
        for (let index = 0; index < this.crmForm.crmFields.length; index++) {
          const element = this.crmForm.crmFields[index]
          if (element.key === 'money') {
            element.value = item.value.money
            break
          }
        }
      }

      //无事件的处理 后期可换成input实现
      if (
        item.data.formType == 'user' ||
        item.data.formType == 'structure' ||
        item.data.formType == 'file'
      ) {
        this.$refs.crmForm.validateField('crmFields.' + data.index + '.value')
      }
    },
    // 获取自定义字段
    getField() {
      this.loading = true
      // 获取自定义字段的更新时间
      var params = {}
      params.label = 10
      params.id = this.categoryId

      // 进行编辑操作
      if (this.action.type == 'update') {
        params.examineId = this.action.id
        params.isDetail = 2 // 1详情 2 编辑
      }

      let request = {
        update: OaExamineGetField,
        save: filedGetField
      }[this.action.type]
      request(params)
        .then(res => {
          this.getcrmRulesAndModel(res.data)
          if (this.action.type == 'update') {
            this.getUpdateOtherInfo()
          }
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },
    // 更新图片附件关联业务信息
    getUpdateOtherInfo() {
      this.imgFileList = this.action.data.img.map(function(item, index, array) {
        item.url = item.filePath
        return item
      })
      this.fileList = this.action.data.file.map(function(item, index, array) {
        item.url = item.filePath
        return item
      })
      this.relatedBusinessInfo = {
        contacts: this.action.data.contactsList,
        customer: this.action.data.customerList,
        business: this.action.data.businessList,
        contract: this.action.data.contractList
      } // 关联业务信息
    },
    // 根据自定义字段获取自定义字段规则
    getcrmRulesAndModel(list) {
      let showStyleIndex = -1
      for (let index = 0; index < list.length; index++) {
        const item = list[index]
        showStyleIndex += 1
        /**
         *
         * 规则数据
         */
        var tempList = []
        //验证必填
        if (item.isNull == 1) {
          if (item.formType == 'business_cause') {
            var validateFunction = (rule, value, callback) => {
              if (!value.list) {
                this.$message.error('请完善明细')
                callback(new Error('请完善明细'))
              } else {
                var hasError = false
                for (let index = 0; index < value.list.length; index++) {
                  const item = value.list[index]
                  let keys = [
                    'startAddress',
                    'endAddress',
                    'startTime',
                    'endTime',
                    'duration'
                  ]
                  for (let keyIndex = 0; keyIndex < keys.length; keyIndex++) {
                    const key = keys[keyIndex]
                    if (!item[key]) {
                      hasError = true
                      this.$message.error('请完善明细')
                      callback(new Error('请完善明细'))
                      break
                    }
                  }
                }
                if (!hasError) {
                  callback()
                }
              }
            }

            tempList.push({
              validator: validateFunction,
              trigger: []
            })
          } else if (item.formType == 'examine_cause') {
            var validateFunction = (rule, value, callback) => {
              if (!value.list) {
                this.$message.error('请完善明细')
                callback(new Error('请完善明细'))
              } else {
                var hasError = false
                for (let index = 0; index < value.list.length; index++) {
                  const item = value.list[index]
                  let keys = [
                    'startAddress',
                    'endAddress',
                    'startTime',
                    'endTime',
                    'traffic',
                    'stay',
                    'diet',
                    'other'
                  ]
                  for (let keyIndex = 0; keyIndex < keys.length; keyIndex++) {
                    const key = keys[keyIndex]
                    if (!item[key]) {
                      hasError = true
                      this.$message.error('请完善明细')
                      callback(new Error('请完善明细'))
                      break
                    }
                  }
                }
                if (!hasError) {
                  callback()
                }
              }
            }

            tempList.push({
              validator: validateFunction,
              trigger: []
            })
          } else {
            tempList.push({
              required: true,
              message: item.name + '不能为空',
              trigger: ['blur', 'change']
            })
          }
        }

        //验证唯一
        if (item.isUnique == 1) {
          var validateUnique = (rule, value, callback) => {
            if ((isArray(value) && value.length == 0) || !value) {
              callback()
            } else {
              var validatesParams = {}
              validatesParams.name = item.name
              if (isArray(value)) {
                let postValue = ''
                if (value.length > 0) {
                  if (
                    rule.item.formType == 'user' ||
                    rule.item.formType == 'structure'
                  ) {
                    postValue = value
                      .map(valueItem => {
                        return rule.item.formType == 'user'
                          ? valueItem.userId
                          : valueItem.id
                      })
                      .join(',')
                  } else if (rule.item.fieldName == 'categoryId') {
                    postValue = element.value[element.value.length - 1]
                  } else if (rule.item.formType == 'checkbox') {
                    postValue = value.join(',')
                  }
                }
                validatesParams.val = postValue
              } else {
                validatesParams.val = value
              }
              validatesParams.types = 10
              if (this.action.type == 'update') {
                validatesParams.id = this.action.id
              }
              filedValidates(validatesParams)
                .then(res => {
                  callback()
                })
                .catch(error => {
                  callback(new Error(error.error ? error.error : '验证出错'))
                })
            }
          }
          tempList.push({
            validator: validateUnique,
            item: item,
            trigger:
              item.formType == 'checkbox' || item.formType == 'select'
                ? ['change']
                : ['blur']
          })
        }

        // 特殊字符
        if (item.formType == 'number') {
          var validateCRMNumber = (rule, value, callback) => {
            if (!value || value == '' || regexIsCRMNumber(value)) {
              callback()
            } else {
              callback(new Error('数字的整数部分须少于12位，小数部分须少于4位'))
            }
          }
          tempList.push({
            validator: validateCRMNumber,
            item: item,
            trigger: ['blur']
          })
        } else if (item.formType == 'floatnumber') {
          var validateCRMMoneyNumber = (rule, value, callback) => {
            if (!value || value == '' || regexIsCRMMoneyNumber(value)) {
              callback()
            } else {
              callback(new Error('货币的整数部分须少于10位，小数部分须少于2位'))
            }
          }
          tempList.push({
            validator: validateCRMMoneyNumber,
            item: item,
            trigger: ['blur']
          })
        } else if (item.formType == 'mobile') {
          var validateCRMMobile = (rule, value, callback) => {
            if (!value || value == '' || regexIsCRMMobile(value)) {
              callback()
            } else {
              callback(new Error('手机格式有误'))
            }
          }
          tempList.push({
            validator: validateCRMMobile,
            item: item,
            trigger: ['blur']
          })
        } else if (item.formType == 'email') {
          var validateCRMEmail = (rule, value, callback) => {
            if (!value || value == '' || regexIsCRMEmail(value)) {
              callback()
            } else {
              callback(new Error('邮箱格式有误'))
            }
          }
          tempList.push({
            validator: validateCRMEmail,
            item: item,
            trigger: ['blur']
          })
        }

        this.crmRules[item.fieldName || item.name] = tempList

        /**
         *
         *
         *
         *
         *
         *
         *
         *
         * 表单数据
         */
        if (item.formType == 'datetime') {
          // 返回的时间戳  要处理为格式化时间（编辑的时候）
          // 关联产品信息比较多 用字典接收
          var params = {}

          if (this.action.type == 'update') {
            params['value'] = item.value || ''
          } else {
            params['value'] = item.defaultValue // 加入默认值 可能编辑的时候需要调整
          }

          params['key'] = item.fieldName || item.name
          params['data'] = item
          params['disabled'] = false // 是否可交互
          params['styleIndex'] = showStyleIndex
          this.crmForm.crmFields.push(params)
        } else if (
          item.formType == 'examine_cause' ||
          item.formType == 'business_cause'
        ) {
          // 报销事项
          var params = {}

          if (this.action.type == 'update') {
            let list = item.value.map(function(element, index, array) {
              if (element.img) {
                element.imgList = element.img.map(function(file, index, array) {
                  file.url = file.filePath
                  return file
                })
              } else {
                element.imgList = []
              }
              return element
            })
            params['value'] = { list: list } // 编辑的值 在value字段
          } else {
            params['value'] = {} // 加入默认值 可能编辑的时候需要调整
          }
          params['key'] = item.fieldName || item.name
          params['data'] = item
          params['disabled'] = false // 是否可交互
          params['showblock'] = true // 展示整行效果
          if (index % 2 == 0) {
            showStyleIndex = -1
          }
          this.crmForm.crmFields.push(params)
        } else if (
          // 出差审批 差旅报销
          (item.fieldName == 'duration' && this.type == 3) ||
          (item.fieldName == 'money' && this.type == 5)
        ) {
          // 报销事项
          var params = {}

          if (this.action.type == 'update') {
            params['value'] = item.value // 编辑的值 在value字段
          } else {
            params['value'] = item.defaultValue || '' // 加入默认值 可能编辑的时候需要调整
          }
          params['key'] = item.fieldName || item.name
          params['data'] = item
          params['disabled'] = true // 是否可交互
          params['styleIndex'] = showStyleIndex
          this.crmForm.crmFields.push(params)
        } else {
          var params = {}
          if (this.action.type == 'update') {
            params['value'] = item.value // 编辑的值 在value字段
          } else {
            if (
              item.formType == 'user' ||
              item.formType == 'structure' ||
              item.formType == 'file' ||
              item.formType == 'category' ||
              item.formType == 'customer' ||
              item.formType == 'business' ||
              item.formType == 'contract'
            ) {
              params['value'] = item.defaultValue
                ? objDeepCopy(item.defaultValue)
                : []
            } else {
              params['value'] = item.defaultValue || ''
            }
          }
          params['key'] = item.fieldName || item.name
          params['data'] = item
          params['disabled'] = false // 是否可交互
          params['styleIndex'] = showStyleIndex
          this.crmForm.crmFields.push(params)
        }
      }
    },
    // 保存数据
    saveField() {
      this.$refs.crmForm.validate(valid => {
        if (valid) {
          if (this.showExamine) {
            /** 验证审批数据 */
            this.$refs.examineInfo.validateField(() => {
              let params = {
                oaExamine: { categoryId: this.categoryId },
                oaExamineRelation: {},
                field: [],
                oaExamineTravelList: []
              }
              this.getSubmiteParams(this.crmForm.crmFields, params)
              if (this.examineInfo.examineType === 2) {
                params['checkUserId'] = this.examineInfo.value[0].userId
              }
              this.submiteParams(params)
            })
          } else {
            let params = {
              oaExamine: { categoryId: this.categoryId },
              oaExamineRelation: {},
              field: [],
              oaExamineTravelList: []
            }
            this.getSubmiteParams(this.crmForm.crmFields, params)
            this.submiteParams(params)
          }
        } else {
          return false
        }
      })
    },
    /** 上传 */
    submiteParams(params) {
      /** 注入关联参数 */
      for (let key in this.relatedBusinessInfo) {
        const list = this.relatedBusinessInfo[key]
        params.oaExamineRelation[key + 'Ids'] = list
          .map(function(item, index, array) {
            return item[key + 'Id']
          })
          .join(',')
      }

      params.oaExamine['batchId'] = this.batchId

      this.loading = true
      if (this.action.type == 'update') {
        params.oaExamine.examineId = this.action.id
      }
      oaExamineSaveAndUpdate(params)
        .then(res => {
          this.loading = false
          this.hidenView()
          this.$message.success('操作成功')
          // 回到保存成功
          this.$emit('save-success')
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 拼接上传传输 */
    getSubmiteParams(array, params) {
      for (let index = 0; index < array.length; index++) {
        const element = array[index]
        if (element.key == 'cause') {
          if (element.data.formType == 'business_cause') {
            params.oaExamineTravelList = element.value.list
            // params['duration'] = element.value.duration
          } else if (element.data.formType == 'examine_cause') {
            var causeList = []
            for (let index = 0; index < element.value.list.length; index++) {
              const cause = element.value.list[index]
              var causeCopy = Object.assign({}, cause)
              delete causeCopy['imgList']
              params.oaExamineTravelList.push(causeCopy)
            }
            params[element.key] = causeList
          }
        } else {
          if (element.data.fieldType == 1) {
            params.oaExamine[element.key] = this.getRealParams(element)
          } else {
            element.data.value = this.getRealParams(element)
            params.field.push(element.data)
          }
        }
      }
      return params
    },
    // 图片和附件
    // 上传图片
    imgFileUploadSuccess(response, file, fileList) {
      this.imgFileList = fileList
    },
    // 查看图片
    handleFilePreview(file) {
      if (file.response || file.fileId) {
        let perviewFile
        if (file.response) {
          perviewFile = file.response
        } else {
          perviewFile = {
            url: file.filePath,
            name: file.name
          }
        }
        this.$bus.emit('preview-image-bus', {
          index: 0,
          data: [perviewFile]
        })
      }
    },
    beforeRemove(file, fileList) {
      if (file.response || file.fileId) {
        let fileId
        if (file.response) {
          fileId = file.response.fileId
        } else {
          fileId = file.fileId
        }
        this.$confirm('您确定要删除该文件吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            crmFileDelete({
              id: fileId
            })
              .then(res => {
                this.$message.success('操作成功')
                var removeIndex = this.getFileIndex(
                  this.$refs.imageUpload.uploadFiles,
                  fileId
                )
                if (removeIndex != -1) {
                  this.$refs.imageUpload.uploadFiles.splice(removeIndex, 1)
                }
                removeIndex = this.getFileIndex(this.imgFileList, fileId)
                if (removeIndex != -1) {
                  this.imgFileList.splice(removeIndex, 1)
                }
              })
              .catch(() => {})
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '已取消操作'
            })
          })
        return false
      } else {
        return true
      }
    },
    // 附件索引
    getFileIndex(files, fileId) {
      var removeIndex = -1
      for (let index = 0; index < files.length; index++) {
        const item = files[index]
        let itemFileId
        if (item.response) {
          itemFileId = item.response.fileId
        } else {
          itemFileId = item.fileId
        }
        if (itemFileId == fileId) {
          removeIndex = index
          break
        }
      }
      return removeIndex
    },
    fileUploadSuccess(response, file, fileList) {
      this.fileList = fileList
    },
    handleFileRemove(file, fileList) {
      if (file.response || file.fileId) {
        let fileId
        if (file.response) {
          fileId = file.response.fileId
        } else {
          fileId = file.fileId
        }
        this.$confirm('您确定要删除该文件吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            crmFileDelete({
              id: fileId
            })
              .then(res => {
                this.$message.success('操作成功')
                var removeIndex = this.getFileIndex(
                  this.$refs.fileUpload.uploadFiles,
                  fileId
                )
                if (removeIndex != -1) {
                  this.$refs.fileUpload.uploadFiles.splice(removeIndex, 1)
                }
                removeIndex = this.getFileIndex(this.fileList, fileId)
                if (removeIndex != -1) {
                  this.fileList.splice(removeIndex, 1)
                }
              })
              .catch(() => {})
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '已取消操作'
            })
          })
        return false
      } else {
        return true
      }
    },
    // 关联客户 联系人等数据要特殊处理
    getRealParams(element) {
      if (
        element.key == 'customerId' ||
        element.key == 'contactsId' ||
        element.key == 'businessId' ||
        element.key == 'leadsId' ||
        element.key == 'contractId'
      ) {
        if (element.value.length) {
          return element.value[0][element.key]
        } else {
          return ''
        }
      } else if (element.key == 'categoryId') {
        if (element.value.length) {
          return element.value[element.value.length - 1]
        } else {
          return ''
        }
      } else if (
        element.data.formType == 'user' ||
        element.data.formType == 'structure'
      ) {
        return element.value
          .map(function(item, index, array) {
            return element.data.formType == 'user' ? item.userId : item.id
          })
          .join(',')
      } else if (element.data.formType == 'file') {
        if (element.value && element.value.length > 0) {
          return element.value[0].batchId
        }
        return ''
      } else if (element.data.formType == 'checkbox') {
        if (element.value && element.value.length > 0) {
          return element.value.join(',')
        }
        return ''
      }

      return element.value
    },
    hidenView() {
      this.$emit('hiden-view')
    },
    // 根据类型获取标题展示名称
    getTitle() {
      return this.action.type == 'update'
        ? '编辑' + this.categoryTitle
        : '新建' + this.categoryTitle
    },
    // 获取左边padding
    getPaddingLeft(item, index) {
      if (item.showblock && item.showblock == true) {
        return '0'
      }
      return item.styleIndex % 2 == 0 ? '0' : '25px'
    },
    // 获取左边padding
    getPaddingRight(item, index) {
      if (item.showblock && item.showblock == true) {
        return '0'
      }
      return item.styleIndex % 2 == 0 ? '25px' : '0'
    }
  },
  destroyed() {
    // remove DOM node after destroy
    if (this.$el && this.$el.parentNode) {
      this.$el.parentNode.removeChild(this.$el)
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
    cursor: pointer;
  }
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
  padding: 0 10px;
}

.crm-create-item {
  flex: 0 0 50%;
  flex-shrink: 0;
  // overflow: hidden;
  padding-bottom: 10px;
}

// 图片 附件
.img-accessory {
  padding: 0 20px;
  font-size: 12px;
  img {
    width: 16px;
    vertical-align: middle;
  }
  .img-box /deep/ .el-upload {
    width: 80px;
    height: 80px;
    line-height: 90px;
  }
  .img-box /deep/ .el-upload-list {
    .el-upload-list__item {
      width: 80px;
      height: 80px;
    }
  }
  .img-box {
    position: relative;
    margin-top: 40px;
    .add-img {
      position: absolute;
      left: 0;
      top: -30px;
      height: 20px;
      line-height: 20px;
      margin-bottom: 10px;
      color: #3e84e9;
    }
  }
  .add-accessory {
    margin-top: 25px;
    margin-bottom: 20px;
    color: #3e84e9;
  }
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

.handle-bar {
  position: relative;
  .handle-button {
    float: right;
    margin-top: 5px;
    margin-right: 20px;
  }
}

// 审核信息 里的审核类型
.examine-type {
  font-size: 12px;
  color: white;
  background-color: #fd715a;
  padding: 0 8px;
  margin-left: 5px;
  height: 16px;
  line-height: 16px;
  border-radius: 8px;
  transform: scale(0.8, 0.8);
}

.related-business {
  padding: 0 20px;
}
</style>
