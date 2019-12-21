<template>
  <el-dialog
    :visible="show"
    :title="'导入'+crmTypeName"
    :append-to-body="true"
    :close-on-click-modal="false"
    width="600px"
    @close="closeView">
    <div class="dialog-body">
      <!-- <el-steps
        :active="stepsActive"
        simple>
        <el-step
          v-for="(item, index) in stepList"
          :key="index"
          :title="item.title"
          :icon="item.icon"
          :status="item.status" />
      </el-steps> -->

      <div v-if="stepsActive == 1" class="step-section">
        <div class="sections">
          <div class="sections__title">一、请按照数据模板的格式准备要导入的数据。<span
            class="download"
            @click="download">点击下载《{{ crmTypeName }}导入模板》</span></div>
          <div class="sections__tips">导入文件请勿超过2MB（约10,000条数据）</div>
        </div>
        <div class="sections">
          <div class="sections__title">二、请选择数据重复时的处理方式（查重规则：【{{ fieldUniqueInfo }}】）</div>
          <div class="sections__tips">查重规则为：添加{{ crmTypeName }}时所需填写的所有唯一字段，当前设置唯一字段为：{{ fieldUniqueInfo }}</div>
          <div class="content">
            <el-select
              v-model="config"
              placeholder="请选择">
              <el-option
                v-for="(item, index) in [{name: '覆盖系统原有数据',value: 1},{name: '跳过',value: 2}]"
                :key="index"
                :label="item.name"
                :value="item.value"/>
            </el-select>
          </div>
        </div>
        <div class="sections">
          <div class="sections__title">三、请选择需要导入的文件</div>
          <div class="content">
            <flexbox class="file-select">
              <el-input
                v-model="file.name"
                :disabled="true"/>
              <el-button
                type="primary"
                @click="selectFile">选择文件</el-button>
            </flexbox>
          </div>
        </div>
        <div class="sections">
          <div class="sections__title">四、请选择负责人（{{ crmType == 'customer' ? '如不选择，导入的客户将进入公海' : '必选' }}）</div>
          <div class="content">
            <div class="user-cell">
              <xh-user-cell
                :value="user"
                @value-change="userSelect"/>
            </div>
          </div>
        </div>
      </div>

      <div
        v-loading="loading"
        v-else-if="stepsActive == 2"
        element-loading-text="数据导入中"
        element-loading-spinner="el-icon-loading"
        class="step-section" />

      <div
        v-loading="loading"
        v-else-if="stepsActive == 3"
        class="step-section">
        <div class="result-info">
          <i class="wk wk-success result-info__icon" />
          <p class="result-info__des">数据导入完成</p>
          <p class="result-info__detail">导入总数据<span class="result-info__detail--all">{{ resultData.totalSize }}</span>条，导入成功<span class="result-info__detail--suc">{{ resultData.totalSize - resultData.errSize }}</span>条，导入失败<span class="result-info__detail--err">{{ resultData.errSize }}</span>条</p>
          <el-button
            v-if="resultData && resultData.errSize > 0"
            class="result-info__btn--err"
            type="text"
            @click="downloadErrData">下载错误数据</el-button>
        </div>
      </div>

      <input
        id="importInputFile"
        type="file"
        accept=".csv, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
        @change="uploadFile">
    </div>
    <span
      slot="footer"
      class="dialog-footer">
      <el-popover
        v-model="historyPopoverShow"
        placement="top"
        width="800"
        popper-class="no-padding-popover"
        trigger="click">
        <!-- <c-r-m-import-history
          :show="historyPopoverShow"
          :crm-type="crmType"
          @close="historyPopoverShow = false" />
        <el-button
          slot="reference"
          class="history-btn"
          type="text">查看历史导入记录</el-button> -->
      </el-popover>



      <el-button
        :class="{ 'is-hidden': !showCancel }"
        @click="closeView">取消</el-button>
      <el-button
        v-if="sureTitle"
        type="primary"
        @click="sureClick">{{ sureTitle }}</el-button>
    </span>
  </el-dialog>
</template>

<script>
import {
  crmQueryImportNumAPI,
  crmQueryImportInfoAPI,
  crmDownImportErrorAPI,
  filedGetField
} from '@/api/customermanagement/common'
import {
  crmCustomerExcelImport,
  crmCustomerDownloadExcelAPI
} from '@/api/customermanagement/customer'
import {
  crmLeadsExcelImport,
  crmLeadsDownloadExcelAPI
} from '@/api/customermanagement/clue'
import {
  crmContactsExcelImport,
  crmContactsDownloadExcelAPI
} from '@/api/customermanagement/contacts'
import {
  crmProductExcelImport,
  crmProductDownloadExcelAPI
} from '@/api/customermanagement/product'

import { XhUserCell } from '@/components/CreateCom'
// import CRMImportHistory from './CRMImportHistory'

import { mapGetters } from 'vuex'
import crmTypeModel from '@/views/customermanagement/model/crmTypeModel'
import { downloadExcelWithResData } from '@/utils/index'

export default {
  name: 'CRMImport', // 文件导入
  components: {
    XhUserCell
  },
  props: {
    show: {
      type: Boolean,
      default: false
    },
    // CRM类型
    crmType: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      loading: false,
      fieldList: [],
      config: 1, // 	1 覆盖，2跳过
      file: { name: '' },
      user: [],

      stepsActive: 1,
      stepList: [
        {
          icon: 'wk wk-upload',
          title: '上传文件',
          status: 'wait'
        },
        {
          icon: 'wk wk-data-import',
          title: '导入数据',
          status: 'wait'
        },
        {
          icon: 'wk wk-success',
          title: '导入完成',
          status: 'wait'
        }
      ],
      resultData: null,
      processData: {
        count: 0,
        status: ''
      },
      messageId: null,
      intervalTimer: null,

      historyPopoverShow: false
    }
  },
  computed: {
    ...mapGetters(['userInfo']),

    crmTypeName() {
      return (
        {
          customer: '客户',
          leads: '线索',
          contacts: '联系人',
          product: '产品'
        }[this.crmType] || ''
      )
    },

    sureTitle() {
      return {
        1: '立即导入',
        2: '最小化',
        3: '确定'
      }[this.stepsActive]
    },

    showCancel() {
      return this.stepsActive != 2
    },

    fieldUniqueInfo() {
      const uniqueList = this.fieldList.filter(item => {
        return item.isUnique == 1
      })

      return uniqueList.map(item => {
        return item.name
      }).join('/') || '无'
    }
  },
  watch: {
    show: function(val) {
      if (val) {
        if (this.stepsActive == 1) {
          if (this.userInfo) {
            this.user = [this.userInfo]
          }
        }

        this.getField()
      } else {
        if (this.stepsActive == 3) {
          this.resetData()
        }

        this.fieldList = []
      }
    },

    stepsActive() {
      this.$emit('status', {
        1: 'wait',
        2: 'process',
        3: 'finish'
      }[this.stepsActive])
    }

    // file() {
    //   this.getFirstStepStatus()
    // },

    // user() {
    //   this.getFirstStepStatus()
    // }

  },
  mounted() {
  },
  methods: {
    sureClick() {
      if (this.stepsActive == 1) {
        if (this.stepList[0].status == 'finish') {
          this.stepList[1].status = 'process'
          this.stepsActive = 2
          this.firstUpdateFile(res => {
            this.messageId = res.data
            this.secondQueryNum()
            this.intervalTimer = setInterval(() => {
              if (this.processData.status == 'end') {
                clearInterval(this.intervalTimer)
                this.intervalTimer = null
                this.thirdQueryResult()
              } else {
                this.secondQueryNum()
              }
            }, 2000)
          })
        } else {
          if (!this.file.name) {
            this.$message.error('请选择导入文件')
          } else if (
            this.crmType != 'customer' &&
            (!this.user || this.user.length == 0)
          ) {
            this.$message.error('请选择负责人')
          }
        }
      } else {
        this.closeView()
      }
    },


    /**
     * 第一步上传
     */
    firstUpdateFile(result) {
      var params = {}
      params.repeatHandling = this.config
      params.file = this.file
      params.ownerUserId = this.user.length > 0 ? this.user[0].userId : ''
      var request = {
        customer: crmCustomerExcelImport,
        leads: crmLeadsExcelImport,
        contacts: crmContactsExcelImport,
        product: crmProductExcelImport
      }[this.crmType]
      this.loading = true
      request(params)
        .then(res => {
          if (result) {
            result(res)
          }
        })
        .catch(() => {
          if (result) {
            result(false)
          }
          this.loading = false
        })
    },

    /**
     * 第二步查询数量
     */
    secondQueryNum() {
      crmQueryImportNumAPI({ messageId: this.messageId })
        .then(res => {
          if (res.data === null) {
            this.processData.status = 'end'
          } else {
            this.processData.status = ''
            this.processData.count = res.data
          }
        })
        .catch(() => {
          // this.processData.status = 'err'
        })
    },

    /**
     * 第三部 查询结果
     */
    thirdQueryResult() {
      crmQueryImportInfoAPI({ messageId: this.messageId })
        .then(res => {
          this.loading = false
          this.stepList[1].status = 'finish'
          this.stepsActive = 3
          this.$emit('status', 'finish')
          if (res) {
            this.resultData = res
            if (res.errSize > 0) {
              this.stepList[2].status = 'error'
            } else {
              this.stepList[2].status = 'finish'
            }
          }
        })
        .catch(() => {})
    },

    /**
     * 下载错误模板
     */
    downloadErrData() {
      this.loading = true
      crmDownImportErrorAPI({ messageId: this.messageId })
        .then(res => {
          downloadExcelWithResData(res)
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },

    // 下载模板操作
    download() {
      const request = {
        customer: crmCustomerDownloadExcelAPI,
        leads: crmLeadsDownloadExcelAPI,
        contacts: crmContactsDownloadExcelAPI,
        product: crmProductDownloadExcelAPI
      }[this.crmType]
      request()
        .then(res => {
          var blob = new Blob([res.data], {
            type: 'application/vnd.ms-excel;charset=utf-8'
          })
          var downloadElement = document.createElement('a')
          var href = window.URL.createObjectURL(blob) // 创建下载的链接
          downloadElement.href = href
          downloadElement.download =
            decodeURI(
              res.headers['content-disposition'].split('filename=')[1]
            ) || '' // 下载后文件名
          document.body.appendChild(downloadElement)
          downloadElement.click() // 点击下载
          document.body.removeChild(downloadElement) // 下载完成移除元素
          window.URL.revokeObjectURL(href) // 释放掉blob对象
        })
        .catch(() => {})
    },
    // 选择文件
    selectFile() {
      document.getElementById('importInputFile').click()
    },
    /** 图片选择出发 */
    uploadFile(event) {
      var files = event.target.files
      const file = files[0]
      this.file = file
      event.target.value = ''

      // 阶段一状态
      this.getFirstStepStatus()
    },
    // 用户选择
    userSelect(data) {
      if (data.value && data.value.length > 0) {
        this.user = data.value
      } else {
        this.user = []
      }

      // 阶段一状态
      this.getFirstStepStatus()
    },

    getFirstStepStatus() {
      // 阶段一状态
      const hasFile = this.file && this.file.size
      const hasUser = this.user && this.user.length > 0

      if (this.crmType === 'customer') {
        this.stepList[0].status = hasFile ? 'finish' : 'wait'
      } else {
        this.stepList[0].status = hasFile && hasUser ? 'finish' : 'wait'
      }
    },

    // 关闭操作
    closeView() {
      this.$emit('update:show', false)
      this.$emit('close', this.stepsActive == 3 ? 'finish' : '')
    },

    /**
     * 重置页面数据
     */
    resetData() {
      this.config = 1
      this.file = { name: '' }
      this.user = []

      this.stepsActive = 1
      this.stepList = [
        {
          icon: 'wk wk-upload',
          title: '上传文件',
          status: 'wait'
        },
        {
          icon: 'wk wk-data-import',
          title: '导入数据',
          status: 'wait'
        },
        {
          icon: 'wk wk-success',
          title: '导入完成',
          status: 'wait'
        }
      ]
      this.resultData = null
      this.processData = {
        count: 0,
        status: ''
      }
      this.messageId = null
    },

    /**
     * 获取验证字段
     */
    getField() {
      var params = {
        label: crmTypeModel[this.crmType]
      }

      filedGetField(params)
        .then(res => {
          this.fieldList = res.data
        })
        .catch(() => {
        })
    }
  }
}
</script>

<style scoped lang="scss">
.el-steps {
  margin-bottom: 15px;

  /deep/ .el-step__title {
    font-size: 14px;
  }

  /deep/ .el-step.is-simple .el-step__arrow::before,
  /deep/ .el-step.is-simple .el-step__arrow::after {
    height: 10px;
    width: 2px;
  }

  /deep/ .el-step.is-simple .el-step__arrow::after {
    transform: rotate(45deg) translateY(3px);
  }
  /deep/ .el-step.is-simple .el-step__arrow::before {
    transform: rotate(-45deg) translateY(-2px);
  }
}

.step-section {
  min-height: 300px;

  /deep/ .el-loading-spinner {
    top: 45%;
    .el-icon-loading {
      font-size: 40px;
      color: #999;
    }

    .el-loading-text {
      color: #333;
    }
  }
}

.sections {
  font-size: 14px;
  color: #333;

  &__title {
    font-weight: 600;
  }

  &__tips {
    padding-left: 30px;
    margin: 8px 0 15px;
    color: #999;
    font-size: 12px;
    line-height: 1.4;
  }

  .download {
    cursor: pointer;
    color: #2362FB;
  }

}

.sections__tips + .content {
  padding-top: 0;
}

.content {
  padding: 10px 10px 10px 30px;
  .el-select {
    width: 400px;
  }
  .user-cell {
    width: 400px;
  }
}

#importInputFile {
  display: none;
}

.file-select {
  .el-input {
    width: 400px;
  }
  button {
    margin-left: 20px;
  }
}

.is-hidden {
  visibility: hidden;
}

.history-btn {
  float: left;
  margin-left: 15px;
}


// 结果信息
.result-info {
  text-align: center;
  padding-top: 80px;

  &__icon {
    font-size: 40px;
    color: $xr-color-primary;
  }

  &__des {
    margin-top: 15px;
    color: #333;
    font-size: 14px;
  }

  &__detail {
    margin-top: 15px;
    font-size: 12px;
    color: #666;
    &--all {
      color: #333;
      font-weight: 600;
    }

    &--suc {
      color: $xr-color-primary;
      font-weight: 600;
    }

    &--err {
      color: #f94e4e;
      font-weight: 600;
    }
  }

  &__btn--err {
    margin-top: 10px;
  }
}
</style>
