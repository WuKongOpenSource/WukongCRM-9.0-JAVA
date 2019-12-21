<template>
  <el-dialog
    :visible.sync="showDialog"
    :title="'导出'+crmTypeName"
    :append-to-body="true"
    :close-on-click-modal="false"
    :before-close="beforeClose"
    :close-on-press-escape="false"
    width="550px"
    @close="closeView"
  >
    <div class="dialog-body">
      <p
        v-if="error"
        class="error"
        v-text="error" />
      <p
        v-if="done"
        class="done">
        <i class="el-icon-success"/>
        导出已完成
      </p>
      <p
        v-else-if="cancel"
        class="cancel">
        <i class="el-icon-warning"/>
        导出已取消
      </p>
      <div v-else>
        <i class="el-icon-loading" />
        导出中...
        {{ progress }}
      </div>
    </div>
    <span slot="footer" class="dialog-footer" />
  </el-dialog>
</template>

<script>
import { mapGetters } from 'vuex'
import {
  crmCustomerExcelExport,
  crmCustomerExcelAllExport,
  crmCustomerPoolExcelExportAPI
} from '@/api/customermanagement/customer'
import { crmLeadsExcelExport, crmLeadsExcelAllExport } from '@/api/customermanagement/clue'
import { crmContactsExcelExport, crmContactsExcelAllExport } from '@/api/customermanagement/contacts'
import { crmProductExcelExport, crmProductExcelAllExport } from '@/api/customermanagement/product'
import { crmBusinessExcelExport, crmBusinessExcelAllExport } from '@/api/customermanagement/business'
import { crmContractExcelExport, crmContractExcelAllExport } from '@/api/customermanagement/contract'
import { crmReceivablesExcelExport, crmReceivablesExcelAllExport } from '@/api/customermanagement/money'

export default {
  name: 'CRMExport', // 文件导出
  components: {},

  props: {
    show: {
      type: Boolean,
      default: false
    },
    // CRM类型
    crmType: {
      type: String,
      default: ''
    },
    /** 是公海 */
    isSeas: {
      type: Boolean,
      default: false
    },
    // 搜索条件
    search: {
      type: String,
      default: ''
    },
    // 场景
    scene_id: {
      type: [Number, String],
      default: ''
    },
    // 高级搜索
    filterObj: {
      type: Object,
      default: () => {
        return {}
      }
    },
    exportParams: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    return {
      showDialog: false,
      progress: '',
      error: '',
      // 导出已完成
      done: false,
      // 取消导出
      cancel: false,
      // 队列标识
      exportQueueIndex: '',
      // 临时数据
      tempData: {
        temp_file: ''
      },
      // 排序
      sortData: {}
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
    }
  },
  watch: {
    show(val) {
      this.showDialog = val
      if (val) {
        this.cancel = false
        this.exportInfos()
        window.onbeforeunload = (event) => {
          this.exportInfos({
            page: -1,
            temp_file: this.tempData.temp_file
          })
          return event
        }
      } else {
        window.onbeforeunload = null
      }
    }
  },
  mounted() {
    this.$bus.off('getSortData')
    this.$bus.on('getSortData', (sortData) => {
      this.sortData = sortData
    })
  },
  methods: {
    // 关闭操作
    closeView() {
      this.error = ''
      this.done = false
      this.exportQueueIndex = ''
      this.progress = ''
      this.$emit('close')
    },
    // 点叉
    beforeClose(done) {
      if (this.error || this.done) {
        done()
        return
      }
      this.$confirm('此操作将终止导出, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.cancel = true
          done()
        })
        .catch(() => {
        })
    },
    // 导出操作
    exportInfos(data = {}) {
      var params = {
        export_queue_index: this.exportQueueIndex,
        search: this.search,
        ...this.exportParams,
        ...this.sortData,
        ...data
      }
      if (this.scene_id) {
        params.scene_id = this.scene_id
      }
      for (var key in this.filterObj) {
        params[key] = this.filterObj[key]
      }

      let exportAll = 'All'
      if (params['ids']) {
        exportAll = ''
      }

      let request
      // 公海的请求
      if (this.isSeas) {
        request = crmCustomerPoolExcelExportAPI
      } else {
        request = {
          customer: crmCustomerExcelExport,
          customerAll: crmCustomerExcelAllExport,
          leads: crmLeadsExcelExport,
          leadsAll: crmLeadsExcelAllExport,
          contacts: crmContactsExcelExport,
          contactsAll: crmContactsExcelAllExport,
          product: crmProductExcelExport,
          productAll: crmProductExcelAllExport,
          business: crmBusinessExcelExport,
          businessAll: crmBusinessExcelAllExport,
          contract: crmContractExcelExport,
          contractAll: crmContractExcelAllExport,
          receivables: crmReceivablesExcelExport,
          receivablesAll: crmReceivablesExcelAllExport
        }[this.crmType + exportAll]
      }
      request(params)
        .then(res => {
          if (res.data.type.indexOf('json') !== -1) {
            var blob = new Blob([res.data], {
              type: 'application/json'
            })
            var reader = new FileReader()
            reader.readAsText(blob, 'utf-8')
            reader.onload = () => {
              var temp = JSON.parse(reader.result)
              this.tempData = temp.data
              if (temp.error) {
                this.error = temp.error
                return
              }
              this.exportQueueIndex = temp.data.export_queue_index
              if (this.cancel) {
                if (temp.data.page !== -1) {
                  this.exportInfos({
                    page: -1,
                    temp_file: temp.data.temp_file
                  })
                }
              } else if (temp.data.page === -2) {
                setTimeout(() => {
                  this.exportInfos({
                    page: 1,
                    temp_file: temp.data.temp_file
                  })
                }, 1000)
              } else if (temp.data.page > 0) {
                this.exportInfos({
                  page: temp.data.page,
                  temp_file: temp.data.temp_file
                })
                this.progress = String(temp.data.done) + ' / ' + String(temp.data.total)
              }
            }
          } else {
            this.exportQueueIndex = ''
            this.done = true
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
          }
        })
        .catch(() => {
        })
    }
  }
}
</script>

<style scoped lang="scss">

.dialog-body {
  height: 150px;
  text-align: center;
  padding-top: 40px;
  color: #659DED;
  .el-icon-loading {
    margin-bottom: 10px;
    display: block;
  }
  p.error {
    color: #E6A23C;
  }
  p.done {
    color: #67C23A;
    .el-icon-success {
      font-size: 30px;
      display: block;
    }
  }
}

.cancel {
  color: #E6A23C;
  .el-icon-warning {
    font-size: 30px;
    display: block;
  }
}

.el-dialog__wrapper {
  /deep/ .el-icon-loading {
    font-size: 30px;
  }

  /deep/ .el-loading-text {
    font-size: 18px;
    margin-top: 10px;
  }
}
</style>
