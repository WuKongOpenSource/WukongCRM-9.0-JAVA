/** crm自定义列表 公共逻辑 */
import {
  filedGetTableField
} from '@/api/customermanagement/common'
import crmTypeModel from '@/views/customermanagement/model/crmTypeModel'
import {
  crmMessageCheckContractAPI,
  crmMessageCheckReceivablesAPI,
  crmMessageTodayCustomerAPI,
  crmMessageFollowLeadsAPI,
  crmMessageFollowCustomerAPI,
  crmMessagEndContractAPI,
  crmMessagRemindreceivablesplanAPI,
  crmMessagRemindCustomerAPI
} from '@/api/customermanagement/message'

export default {
  components: {},
  data() {
    return {
      loading: false, // 加载动画
      tableHeight: document.documentElement.clientHeight - 300, // 表的高度
      list: [],
      fieldList: [],
      currentPage: 1,
      pageSize: 15,
      pageSizes: [15, 30, 45, 60],
      total: 0
    }
  },

  computed: {
    // 展示options下拉选择
    showOptions() {
      if (this.infoType == 'putInPoolRemind') {
        return false
      }
      return true
    }
  },

  mounted() {
    this.$bus.on('message-scroll', tableHeight => {
      this.tableHeight = tableHeight
    })

    this.$bus.on('examine-handle-bus', () => {
      this.getList()
    })

    /** 控制table的高度 */
    window.onresize = () => {
      this.updateTableHeight()
    }
  },

  beforeDestroy() {
    this.$bus.off('message-scroll')
    this.$bus.off('examine-handle-bus')

    if (document.getElementById('crm-table')) {
      document.getElementById('crm-table').removeEventListener('click', e => {
        e.stopPropagation()
      })
    }
  },

  methods: {
    /**
     * 当某一行被点击时会触发该事件
     * @param {*} row
     * @param {*} column
     * @param {*} event
     */
    handleRowClick(row, column, event) {
      if (this.crmType === 'leads') {
        this.rowID = row.leadsId
        this.rowType = 'leads'
        this.showDview = true
      } else if (this.crmType === 'customer') {
        this.rowID = row.customerId
        this.rowType = 'customer'
        this.showDview = true
      } else if (this.crmType === 'contacts') {
        if (column.property === 'customerName') {
          this.rowID = row.customerId
          this.rowType = 'customer'
        } else {
          this.rowID = row.contactsId
          this.rowType = 'contacts'
        }
        this.showDview = true
      } else if (this.crmType === 'business') {
        if (column.property === 'customerName') {
          this.rowID = row.customerId
          this.rowType = 'customer'
        } else {
          this.rowID = row.businessId
          this.rowType = 'business'
        }
        this.showDview = true
      } else if (this.crmType === 'contract') {
        if (column.property === 'customerName') {
          this.rowID = row.customerId
          this.rowType = 'customer'
        } else if (column.property === 'businessName') {
          this.rowID = row.businessId
          this.rowType = 'business'
        } else if (column.property === 'contactsName') {
          this.rowID = row.contactsId
          this.rowType = 'contacts'
        } else {
          this.rowID = row.contractId
          this.rowType = 'contract'
        }
        this.showDview = true
      } else if (this.crmType === 'product') {
        this.rowID = row.productId
        this.showDview = true
      } else if (this.crmType === 'receivables') {
        if (column.property === 'customerName') {
          this.rowID = row.customerId
          this.rowType = 'customer'
        } else if (column.property === 'contractNum' || column.property === 'contractName') {
          this.rowID = row.contractId
          this.rowType = 'contract'
        } else {
          this.rowID = row.receivablesId
          this.rowType = 'receivables'
        }
        this.showDview = true
      } else if (this.crmType === 'receivables_plan') {
        if (column.property === 'customerName') {
          this.rowID = row.customerId
          this.rowType = 'customer'
        } else if (column.property === 'contractNum' || column.property === 'contractName') {
          this.rowID = row.contractId
          this.rowType = 'contract'
        }
        this.showDview = true
      }
    },

    /** 获取列表数据 */
    getList() {
      this.loading = true
      var crmIndexRequest = this.getIndexRequest()
      const params = {
        page: this.currentPage,
        limit: this.pageSize,
        isSub: this.isSubType
      }

      if (this.showOptions) {
        params.type = this.optionsType
      }

      const filterObj = this.filterObj.obj
      if (filterObj && Object.keys(filterObj).length > 0) {
        params.data = filterObj
      }

      crmIndexRequest(params)
        .then(res => {
          this.list = res.data.list
          this.total = res.data.totalRow

          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },

    /** 获取列表请求 */
    getIndexRequest() {
      return {
        'todayCustomer': crmMessageTodayCustomerAPI,
        'followLeads': crmMessageFollowLeadsAPI,
        'followCustomer': crmMessageFollowCustomerAPI,
        'checkContract': crmMessageCheckContractAPI,
        'checkReceivables': crmMessageCheckReceivablesAPI,
        'remindReceivablesPlan': crmMessagRemindreceivablesplanAPI,
        'endContract': crmMessagEndContractAPI,
        'putInPoolRemind': crmMessagRemindCustomerAPI
      }[this.infoType]
    },

    /** 获取字段 */
    getFieldList() {
      if (this.crmType == 'receivables_plan') {
        const list = [{
          fieldName: 'num',
          formType: 'text',
          name: '期数'
        },
        {
          fieldName: 'customerName',
          formType: 'customerName',
          name: '客户名称'
        },
        {
          fieldName: 'contractNum',
          formType: 'contractNum',
          name: '合同编号'
        },
        {
          fieldName: 'money',
          formType: 'text',
          name: '计划回款金额'
        },
        {
          fieldName: 'returnDate',
          formType: 'text',
          name: '计划回款日期'
        },
        {
          fieldName: 'returnType',
          formType: 'text',
          name: '计划回款方式'
        },
        {
          fieldName: 'remind',
          formType: 'text',
          name: '提前几日提醒'
        },
        {
          fieldName: 'remark',
          formType: 'text',
          name: '备注'
        }
        ]
        this.handelFieldList(list)
        // 获取好字段开始请求数据
        this.getList()
        return
      }

      filedGetTableField({
        label: crmTypeModel[this.crmType]
      })
        .then(res => {
          this.handelFieldList(res.data)

          // 获取好字段开始请求数据
          this.getList()
        })
        .catch(() => {
          this.loading = false
        })
    },

    handelFieldList(list) {
      for (let index = 0; index < list.length; index++) {
        const element = list[index]
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

        this.fieldList.push({
          prop: element.fieldName,
          label: element.name,
          width: width
        })
      }
    },

    /** 格式化字段 */
    fieldFormatter(row, column) {
      // 如果需要格式化
      return row[column.property] || '--'
    },

    // 更改每页展示数量
    handleSizeChange(val) {
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
      var removeHeight = Object.keys(this.filterObj).length > 0 ? 360 : 300
      this.tableHeight = offsetHei - removeHeight
    }
  }
}
