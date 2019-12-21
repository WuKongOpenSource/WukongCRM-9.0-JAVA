/** crm自定义列表 公共逻辑 */
import {
  mapGetters
} from 'vuex'
import crmTypeModel from '@/views/customermanagement/model/crmTypeModel'
import CRMListHead from '../components/CRMListHead'
import CRMTableHead from '../components/CRMTableHead'
import FieldsSet from '../components/fieldsManager/FieldsSet'
import {
  filedGetTableField,
  crmFieldColumnWidth
} from '@/api/customermanagement/common'
import {
  crmCustomerIndex,
  crmCustomerPool,
  crmCustomerExcelAllExport,
  crmCustomerPoolExcelAllExport
} from '@/api/customermanagement/customer'
import {
  crmLeadsIndex,
  crmLeadsExcelAllExport
} from '@/api/customermanagement/clue'
import {
  crmContactsIndex,
  crmContactsExcelAllExport
} from '@/api/customermanagement/contacts'
import {
  crmBusinessIndex
} from '@/api/customermanagement/business'
import {
  crmContractIndex
} from '@/api/customermanagement/contract'
import {
  crmProductIndex,
  crmProductExcelAllExport
} from '@/api/customermanagement/product'
import {
  crmReceivablesIndex
} from '@/api/customermanagement/money'
import Lockr from 'lockr'
import { Loading } from 'element-ui'
import { moneyFormat } from '@/utils'

export default {
  components: {
    CRMListHead,
    CRMTableHead,
    FieldsSet
  },
  data() {
    return {
      loading: false, // 加载动画
      tableHeight: document.documentElement.clientHeight - 240, // 表的高度
      list: [],
      fieldList: [],
      sortData: {}, // 字段排序
      currentPage: 1,
      pageSize: Lockr.get('crmPageSizes') || 15,
      pageSizes: [15, 30, 60, 100],
      total: 0,
      search: '', // 搜索内容
      /** 控制详情展示 */
      rowID: '', // 行信息
      rowType: '', // 详情类型
      showDview: false,
      /** 高级筛选 */
      filterObj: {}, // 筛选确定数据
      sceneId: '', // 场景筛选ID
      sceneName: '', // 场景名字
      /** 列表展示字段管理 */
      showFieldSet: false,
      /** 勾选行 */
      // 勾选数据 用于全局导出
      selectionList: [],
      /** 格式化规则 */
      formatterRules: {}
    }
  },

  computed: {
    ...mapGetters(['crm'])
  },

  mounted() {
    /** 控制table的高度 */
    window.onresize = () => {
      this.updateTableHeight()
    }
    // document.getElementById('crm-table').addEventListener('click', e => {
    //   e.stopPropagation()
    // })
    if (this.isSeas && this.crm.pool.index) {
      this.getFieldList()
    } else if (this.crm[this.crmType].index) {
      this.loading = true
    }
  },

  methods: {
    /** 获取列表数据 */
    getList() {
      this.loading = true
      var crmIndexRequest = this.getIndexRequest()
      var params = {
        page: this.currentPage,
        limit: this.pageSize,
        search: this.search,
        type: this.isSeas ? crmTypeModel.pool : crmTypeModel[this.crmType] // 8是公海
      }
      if (this.sortData.order) {
        params.sortField = this.sortData.prop
        params.order = this.sortData.order == 'ascending' ? 2 : 1
      }

      if (this.sceneId) {
        params.sceneId = this.sceneId
      }
      if (this.filterObj && Object.keys(this.filterObj).length > 0) {
        params.data = this.filterObj
      }
      crmIndexRequest(params)
        .then(res => {
          if (this.crmType === 'customer') {
            this.list = res.data.list.map(element => {
              if (element.dealStatus === 1) {
                element.dealStatus = '已成交'
              } else {
                element.dealStatus = '未成交'
              }
              element.show = false // 控制列表商机展示
              return element
            })
          } else {
            if (this.crmType === 'contract') {
              // 合同列表展示金额信息
              this.moneyData = res.data.money
            }
            this.list = res.data.list
          }

          this.total = res.data.totalRow

          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 获取列表请求 */
    getIndexRequest() {
      if (this.crmType === 'leads') {
        return crmLeadsIndex
      } else if (this.crmType === 'customer') {
        if (this.isSeas) {
          return crmCustomerPool
        } else {
          return crmCustomerIndex
        }
      } else if (this.crmType === 'contacts') {
        return crmContactsIndex
      } else if (this.crmType === 'business') {
        return crmBusinessIndex
      } else if (this.crmType === 'contract') {
        return crmContractIndex
      } else if (this.crmType === 'product') {
        return crmProductIndex
      } else if (this.crmType === 'receivables') {
        return crmReceivablesIndex
      }
    },
    /** 获取字段 */
    getFieldList() {
      if (this.fieldList.length == 0) {
        this.loading = true
        filedGetTableField({
          label: this.isSeas ? crmTypeModel.pool : crmTypeModel[this.crmType] // 9 是公海
        })
          .then(res => {
            for (let index = 0; index < res.data.length; index++) {
              const element = res.data[index]
              var width = 0
              if (!element.width) {
                if (element.name && element.name.length <= 6) {
                  width = element.name.length * 15 + 45
                } else {
                  width = 140
                }
              } else {
                width = element.width
              }

              if (element.formType === 'floatnumber') {
                this.formatterRules[element.fieldName] = {
                  type: 'floatnumber',
                  formatter: function fieldFormatter(info) {
                    return moneyFormat(info)
                  }
                }
              }

              this.fieldList.push({
                prop: element.fieldName,
                label: element.name,
                width: width
              })
            }

            // 获取好字段开始请求数据
            this.getList()
          })
          .catch(() => {
            this.loading = false
          })
      } else {
        // 获取好字段开始请求数据
        this.getList()
      }
    },
    /** 格式化字段 */
    fieldFormatter(row, column) {
      var aRules = this.formatterRules[column.property]
      if (aRules) {
        return aRules.formatter(row[column.property])
      }
      // 如果需要格式化
      return row[column.property] || '--'
    },
    /** */
    /** */
    /** 搜索操作 */
    crmSearch(value) {
      this.currentPage = 1
      this.search = value
      if (this.fieldList.length) {
        this.getList()
      }
    },
    /** 列表操作 */
    // 当某一行被点击时会触发该事件
    handleRowClick(row, column, event) {
      if (column.type === 'selection') {
        return // 多选布局不能点击
      }
      if (this.crmType === 'leads') {
        if (column.property === 'leadsName') {
          this.rowID = row.leadsId
          this.showDview = true
        } else {
          this.showDview = false
        }
      } else if (this.crmType === 'customer') {
        if (column.property === 'businessCheck' && row.businessCount > 0) {
          return // 列表查看商机不展示详情
        }
        if (column.property === 'customerName') {
          this.rowID = row.customerId
          this.rowType = 'customer'
          this.showDview = true
        } else {
          this.showDview = false
        }
      } else if (this.crmType === 'contacts') {
        if (column.property === 'customerName') {
          this.rowID = row.customerId
          this.rowType = 'customer'
          this.showDview = true
        } else if (column.property === 'name') {
          this.rowID = row.contactsId
          this.rowType = 'contacts'
          this.showDview = true
        } else {
          this.showDview = false
        }
      } else if (this.crmType === 'business') {
        if (column.property === 'customerName') {
          this.rowID = row.customerId
          this.rowType = 'customer'
          this.showDview = true
        } else if (column.property === 'businessName') {
          this.rowID = row.businessId
          this.rowType = 'business'
          this.showDview = true
        } else {
          this.showDview = false
        }
      } else if (this.crmType === 'contract') {
        if (column.property === 'customerName') {
          this.rowID = row.customerId
          this.rowType = 'customer'
          this.showDview = true
        } else if (column.property === 'businessName') {
          this.rowID = row.businessId
          this.rowType = 'business'
          this.showDview = true
        } else if (column.property === 'contactsName') {
          this.rowID = row.contactsId
          this.rowType = 'contacts'
          this.showDview = true
        } else if (column.property === 'num') {
          this.rowID = row.contractId
          this.rowType = 'contract'
          this.showDview = true
        } else {
          this.showDview = false
        }
      } else if (this.crmType === 'product') {
        if (column.property === 'name') {
          this.rowID = row.productId
          this.showDview = true
        } else {
          this.showDview = false
        }
      } else if (this.crmType === 'receivables') {
        if (column.property === 'customerName') {
          this.rowID = row.customerId
          this.rowType = 'customer'
          this.showDview = true
        } else if (column.property === 'contractNum') {
          this.rowID = row.contractId
          this.rowType = 'contract'
          this.showDview = true
        } else if (column.property === 'number') {
          this.rowID = row.receivablesId
          this.rowType = 'receivables'
          this.showDview = true
        } else {
          this.showDview = false
        }
      }
    },
    /**
     * 导出 线索 客户 联系人 产品
     * @param {*} data
     */
    // 导出操作
    exportInfos() {
      var params = {
        search: this.search
      }
      if (this.sceneId) {
        params.sceneId = this.sceneId
      }
      if (this.filterObj && Object.keys(this.filterObj).length > 0) {
        params.data = this.filterObj
      }
      let request
      // 公海的请求
      if (this.isSeas) {
        request = crmCustomerPoolExcelAllExport
      } else {
        request = {
          customer: crmCustomerExcelAllExport,
          leads: crmLeadsExcelAllExport,
          contacts: crmContactsExcelAllExport,
          product: crmProductExcelAllExport
        }[this.crmType]
      }
      const loading = Loading.service({ fullscreen: true, text: '导出中...' })
      request(params)
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
          loading.close()
        })
        .catch(() => {
          loading.close()
        })
    },
    /** 筛选操作 */
    handleFilter(data) {
      this.filterObj = data
      var offsetHei = document.documentElement.clientHeight
      var removeHeight = Object.keys(this.filterObj).length > 0 ? 310 : 240
      this.tableHeight = offsetHei - removeHeight
      this.currentPage = 1
      this.getList()
    },
    /** 场景操作 */
    handleScene(data) {
      this.sceneId = data.id
      this.sceneName = data.name
      this.currentPage = 1
      this.getFieldList()
    },
    /** 勾选操作 */
    handleHandle(data) {
      if (data.type === 'alloc' || data.type === 'get' || data.type === 'transfer' || data.type === 'transform' || data.type === 'delete' || data.type === 'put_seas') {
        this.showDview = false
      }

      if (data.type !== 'edit') {
        this.getList()
      }
    },
    /** 自定义字段管理 */
    setSave() {
      this.fieldList = []
      this.getFieldList()
    },
    /** */
    /** 页面头部操作 */
    listHeadHandle(data) {
      if (data.type === 'save-success') {
        // 重新请求第一页数据
        this.currentPage = 1
        this.getList()
      }
    },
    // 设置点击
    handleTableSet() {
      this.showFieldSet = true
    },
    /**
     * 字段排序
     */
    sortChange(column, prop, order) {
      this.currentPage = 1
      this.sortData = column
      this.getList()
    },
    /** 勾选操作 */
    // 当选择项发生变化时会触发该事件
    handleSelectionChange(val) {
      this.selectionList = val // 勾选的行
      this.$refs.crmTableHead.headSelectionChange(val)
    },
    // 当拖动表头改变了列的宽度的时候会触发该事件
    handleHeaderDragend(newWidth, oldWidth, column, event) {
      if (column.property) {
        const crmType = this.isSeas ? this.crmType + '_pool' : this.crmType
        crmFieldColumnWidth({
          types: 'crm_' + crmType,
          field: column.property,
          width: newWidth
        })
          .then(res => {
          })
          .catch(() => { })
      }
    },
    // 更改每页展示数量
    handleSizeChange(val) {
      Lockr.set('crmPageSizes', val)
      this.pageSize = val
      this.getList()
    },
    // 更改当前页数
    handleCurrentChange(val) {
      this.currentPage = val
      this.getList()
    },
    // 0待审核、1审核中、2审核通过、3已拒绝 4已撤回 5未提交 修正 以 getStatusName 为准
    getStatusStyle(status) {
      if (status == 0) {
        return {
          'border-color': '#E6A23C',
          'background-color': '#FDF6EC',
          'color': '#E6A23C'
        }
      } else if (status == 3) {
        return {
          'border-color': '#409EFF',
          'background-color': '#ECF5FF',
          'color': '#409EFF'
        }
      } else if (status == 1) {
        return {
          'border-color': '#67C23A',
          'background-color': '#F0F9EB',
          'color': '#67C23A'
        }
      } else if (status == 2) {
        return {
          'border-color': '#F56C6B',
          'background-color': '#FEF0F0',
          'color': '#F56C6B'
        }
      } else if (status == 4 || status == 5) {
        return {
          'background-color': '#FFFFFF'
        }
      } else if (status == 6) {
        return {
          'border-color': '#E9E9EB',
          'background-color': '#F4F4F5',
          'color': '#909399'
        }
      }
    },
    getStatusName(status) {
      if (status == 0) {
        return '待审核'
      } else if (status == 1) {
        return '通过'
      } else if (status == 2) {
        return '拒绝'
      } else if (status == 3) {
        return '审核中'
      } else if (status == 4) {
        return '撤回'
      } else if (status == 5) {
        return '未提交'
      } else if (status == 6) {
        return '已作废'
      }
      return ''
    },

    /**
     * 更新表高
     */
    updateTableHeight() {
      var offsetHei = document.documentElement.clientHeight
      var removeHeight = Object.keys(this.filterObj).length > 0 ? 310 : 240
      this.tableHeight = offsetHei - removeHeight
    },
    /**
     * 导出
     */
    exportData(params) {
      this.$refs.listHead.handleTypeDrop('out', params)
    }
  },

  beforeDestroy() { }
}
