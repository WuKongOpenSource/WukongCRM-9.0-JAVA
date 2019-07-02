<template>
  <div v-loading="loading"
       class="main-container">
    <div class="tabs-bar">
      <el-tabs v-model="tabType"
               @tab-click="tabTypeClick">
        <el-tab-pane label="部门目标设置"
                     name="department"></el-tab-pane>
        <el-tab-pane label="员工目标设置"
                     name="user"></el-tab-pane>
      </el-tabs>
    </div>
    <div class="handle-bar">
      <el-date-picker v-model="dateSelect"
                      type="year"
                      :clearable="false"
                      value-format="yyyy"
                      :picker-options="pickerOptions"
                      placeholder="选择年">
      </el-date-picker>
      <el-select v-model="typeSelect"
                 placeholder="请选择">
        <el-option v-for="item in [{label:'合同金额', value:1},{label:'回款金额', value:2}]"
                   :key="item.value"
                   :label="item.label"
                   :value="item.value">
        </el-option>
      </el-select>
      <el-cascader placeholder="选择部门"
                   change-on-select
                   :options="deptList"
                   :show-all-levels="false"
                   :props="structuresProps"
                   @change="structuresValueChange"
                   v-model="structuresSelectValue">
      </el-cascader>
      <el-select v-if="this.tabType === 'user'"
                 v-model="userSelectValue"
                 :clearable="true"
                 placeholder="选择员工">
        <el-option v-for="item in userOptions"
                   :key="item.userId"
                   :label="item.realname"
                   :value="item.userId">
        </el-option>
      </el-select>
      <el-button @click.native="handleClick('search')"
                 type="primary">搜索</el-button>

      <div style="float: right;"
           v-if="!isEdit">
        <el-button @click.native="handleClick('edit')"
                   type="primary">编辑</el-button>
        <el-button @click.native="handleClick('export')"
                   type="primary">导出</el-button>
      </div>

      <div style="float: right;"
           v-if="isEdit">
        <el-button @click.native="handleClick('save')"
                   type="primary">保存</el-button>
        <el-button @click.native="handleClick('cancel')"
                   type="primary">取消</el-button>
      </div>
    </div>
    <div class="content">
      <el-table :data="list"
                id="task-set-table"
                :height="tableHeight"
                border
                header-align="center"
                align="center"
                :cell-style="cellStyle"
                highlight-current-row>
        <el-table-column v-for="(item, index) in fieldList"
                         :key="index"
                         :fixed="index==0"
                         show-overflow-tooltip
                         :prop="item.field"
                         width="150"
                         :label="item.name">
          <template slot-scope="scope">
            <div v-if="item.field === 'name'
            || item.field === 'yeartarget'
            || item.field === 'first'
            || item.field === 'second'
            || item.field === 'third'
            || item.field === 'fourth' || !isEdit || (tabType === 'department'&&scope.$index===1)"
                 class="table-show-item">
              {{scope.row[item.field]}}
            </div>
            <el-input v-else
                      type="number"
                      v-model="scope.row[item.field]"
                      @input="handleInputEdit('change', scope)"
                      @blur="handleInputEdit('blur', scope)">
            </el-input>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import { depList, usersList } from '@/api/common'
import {
  crmAchievementIndex,
  crmAchievementUpdate
} from '@/api/systemManagement/SystemCustomer'

import moment from 'moment'
import FileSaver from 'file-saver'
import XLSX from 'xlsx'

export default {
  /** 业绩目标设置 */
  name: 'task-set-statistics',
  components: {},
  data() {
    return {
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now()
        }
      },
      loading: false,
      tableHeight: document.documentElement.clientHeight - 290,
      tabType: 'department',

      dateSelect: '', // 选择的date区间
      typeSelect: 1, // 类型选择 1销售（目标）2回款（目标）
      /** 部门选择解析数据 */
      structuresProps: {
        children: 'children',
        label: 'label',
        value: 'id'
      },
      deptList: [], // 部门列表
      structuresSelectValue: [],
      /** 用户列表 */
      userOptions: [],
      userSelectValue: '',

      /** 编辑控制 */
      isEdit: false, //是否是编辑中

      list: [],
      fieldList: [
        { field: 'name', name: '' },
        { field: 'yeartarget', name: '全年' },
        { field: 'first', name: '第一季度' },
        { field: 'january', name: '1月' },
        { field: 'february', name: '2月' },
        { field: 'march', name: '3月' },
        { field: 'second', name: '第二季度' },
        { field: 'april', name: '4月' },
        { field: 'may', name: '5月' },
        { field: 'june', name: '6月' },
        { field: 'third', name: '第三季度' },
        { field: 'july', name: '7月' },
        { field: 'august', name: '8月' },
        { field: 'september', name: '9月' },
        { field: 'fourth', name: '第四季度' },
        { field: 'october', name: '10月' },
        { field: 'november', name: '11月' },
        { field: 'december', name: '12月' }
      ]
    }
  },
  computed: {},
  mounted() {
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 290
    }
    
    this.dateSelect = moment(new Date())
      .year()
      .toString()
    this.getDeptList()
  },
  methods: {
    tabTypeClick() {
      if (this.tabType === 'department') {
        this.getAhievementList()
      } else if (this.tabType === 'user') {
        this.getUserList() // 更新员工列表
        this.getAhievementListForUser()
      }
    },
    /** 获取部门业绩目标列表 */
    getAhievementList() {
      this.loading = true
      var id = this.structuresSelectValue[this.structuresSelectValue.length - 1]
      crmAchievementIndex({
        year: this.dateSelect,
        type: 2, // 2部门3员工
        status: this.typeSelect,
        deptId: id
      })
        .then(res => {
          var self = this
          this.list = res.data.map(item => {
            return this.getShowItem(item)
          })
          if (this.list.length >= 2) {
            // 有子部门
            this.getSubTotalModel()
          }
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },
    getShowItem(item) {
      item['first'] = this.calculateFirst(item)
      item['second'] = this.calculateSecond(item)
      item['third'] = this.calculateThird(item)
      item['fourth'] = this.calculateFourth(item)
      item['yeartarget'] = this.calculateAll(item)
      return item
    },
    /** 获取直属下级目标总和 */
    getSubTotalModel() {
      var initModel = {
        name: '直属下级目标总和',
        january: '0.00',
        february: '0.00',
        march: '0.00',
        april: '0.00',
        may: '0.00',
        june: '0.00',
        july: '0.00',
        august: '0.00',
        september: '0.00',
        october: '0.00',
        november: '0.00',
        december: '0.00',
        yeartarget: '0.00',
        first: '0.00',
        second: '0.00',
        third: '0.00',
        fourth: '0.00',
        ignore: true
      }

      // 从下属数据1 开始循环
      for (let index = 1; index < this.list.length; index++) {
        const element = this.list[index]
        for (
          let fieldIndex = 0;
          fieldIndex < this.fieldList.length;
          fieldIndex++
        ) {
          const fieldItem = this.fieldList[fieldIndex]
          if (fieldItem.field !== 'name') {
            initModel[fieldItem.field] = (
              parseFloat(initModel[fieldItem.field]) +
              parseFloat(element[fieldItem.field])
            )
              .toFixed(2)
              .toString()
          }
        }
      }
      this.list.splice(1, 0, initModel)
    },
    handleInputEdit(type, scope) {
      if (type === 'change') {
        var value = scope.row[scope.column.property]
          ? scope.row[scope.column.property]
          : '0.00'
        var newValue = value.replace(/^(\-)*(\d+)\.(\d\d).*$/, '$1$2.$3')
        if (value !== newValue) {
          scope.row[scope.column.property] = newValue
          this.$set(this.list, scope.$index, scope.row)
        }
      } else if (type === 'blur') {
        /** 一季度 */
        if (
          scope.column.property === 'january' ||
          scope.column.property === 'february' ||
          scope.column.property === 'march'
        ) {
          scope.row['first'] = this.calculateFirst(scope.row)
          scope.row['yeartarget'] = this.calculateAll(scope.row)
        } else if (
          scope.column.property === 'april' ||
          scope.column.property === 'may' ||
          scope.column.property === 'june'
        ) {
          scope.row['second'] = this.calculateSecond(scope.row)
          scope.row['yeartarget'] = this.calculateAll(scope.row)
        } else if (
          scope.column.property === 'july' ||
          scope.column.property === 'august' ||
          scope.column.property === 'september'
        ) {
          scope.row['third'] = this.calculateThird(scope.row)
          scope.row['yeartarget'] = this.calculateAll(scope.row)
        } else if (
          scope.column.property === 'october' ||
          scope.column.property === 'november' ||
          scope.column.property === 'december'
        ) {
          scope.row['fourth'] = this.calculateFourth(scope.row)
          scope.row['yeartarget'] = this.calculateAll(scope.row)
        }

        if (this.tabType === 'department' && this.list.length >= 2) {
          // 部门下 有子部门的时候 计算其值
          this.calculateSubTotal(scope)
        }
      }
    },
    /** 计算下属目标综合 */
    calculateSubTotal(scope) {
      var item = this.list[1]
      var subValue = '0'
      for (let index = 2; index < this.list.length; index++) {
        const element = this.list[index]
        subValue = (
          parseFloat(subValue) + parseFloat(element[scope.column.property])
        )
          .toFixed(2)
          .toString()
      }
      item[scope.column.property] = subValue

      /** 一季度 */
      if (
        scope.column.property === 'january' ||
        scope.column.property === 'february' ||
        scope.column.property === 'march'
      ) {
        item['first'] = this.calculateFirst(item)
        item['yeartarget'] = this.calculateAll(item)
      } else if (
        scope.column.property === 'april' ||
        scope.column.property === 'may' ||
        scope.column.property === 'june'
      ) {
        item['second'] = this.calculateSecond(item)
        item['yeartarget'] = this.calculateAll(item)
      } else if (
        scope.column.property === 'july' ||
        scope.column.property === 'august' ||
        scope.column.property === 'september'
      ) {
        item['third'] = this.calculateThird(item)
        item['yeartarget'] = this.calculateAll(item)
      } else if (
        scope.column.property === 'october' ||
        scope.column.property === 'november' ||
        scope.column.property === 'december'
      ) {
        item['fourth'] = this.calculateFourth(item)
        item['yeartarget'] = this.calculateAll(item)
      }
    },
    /** 计算各季度 */
    calculateFirst(data) {
      return (
        parseFloat(data.january) +
        parseFloat(data.february) +
        parseFloat(data.march)
      )
        .toFixed(2)
        .toString()
    },
    calculateSecond(data) {
      return (
        parseFloat(data.april) +
        parseFloat(data.may) +
        parseFloat(data.june)
      )
        .toFixed(2)
        .toString()
    },
    calculateThird(data) {
      return (
        parseFloat(data.july) +
        parseFloat(data.august) +
        parseFloat(data.september)
      )
        .toFixed(2)
        .toString()
    },
    calculateFourth(data) {
      return (
        parseFloat(data.october) +
        parseFloat(data.november) +
        parseFloat(data.december)
      )
        .toFixed(2)
        .toString()
    },
    /** 计算全年 */
    calculateAll(data) {
      return (
        parseFloat(data.first) +
        parseFloat(data.second) +
        parseFloat(data.third) +
        parseFloat(data.fourth)
      )
        .toFixed(2)
        .toString()
    },
    /**
     * 获取部门列表
     */
    getDeptList() {
      depList({ type: 'tree' }).then(res => {
        this.deptList = res.data
        if (res.data.length > 0) {
          this.structuresSelectValue = [res.data[0].id]
          this.tabTypeClick() // 获取信息
        }
      })
    },
    /** 部门更改 */
    structuresValueChange(data) {
      if (this.tabType === 'department') {
        if (this.userSelectValue) {
          // 在部门目标设置下更新部门 清空员工下的员工列表信息
          this.userSelectValue = ''
          this.userOptions = []
        }
        // this.getAhievementList()
      } else if (this.tabType === 'user') {
        this.userSelectValue = ''
        this.userOptions = []
        this.getUserList() // 更新员工列表
        // this.getAhievementListForUser() // 获取部门下的员工目标列表
      }
    },
    /** 部门下员工 */
    getUserList() {
      var params = { pageType: 0 }
      if (this.structuresSelectValue.length > 0) {
        params.deptId = this.structuresSelectValue[
          this.structuresSelectValue.length - 1
        ]
        usersList(params)
          .then(res => {
            this.userOptions = res.data
          })
          .catch(() => {})
      } else {
        this.userSelectValue = ''
        this.userOptions = []
      }
    },
    /** 获取员工业绩目标列表 */
    getAhievementListForUser() {
      this.loading = true
      var id = this.structuresSelectValue[this.structuresSelectValue.length - 1]
      crmAchievementIndex({
        year: this.dateSelect,
        type: 3, // 2部门3员工
        status: this.typeSelect,
        deptId: id,
        userId: this.userSelectValue
      })
        .then(res => {
          var self = this
          this.list = res.data.map(item => {
            return this.getShowItem(item)
          })
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },
    /** 顶部操作 */
    handleClick(type) {
      if (type == 'search') {
        this.updateAhievementList()
      } else if (type == 'edit') {
        this.isEdit = true
      } else if (type == 'export') {
        let fix = document.querySelector('.el-table__fixed')
        let wb
        if (fix) {
          wb = XLSX.utils.table_to_book(
            document.getElementById('task-set-table').removeChild(fix)
          )
          document.getElementById('task-set-table').appendChild(fix)
        } else {
          wb = XLSX.utils.table_to_book(
            document.getElementById('task-set-table')
          )
        }
        let wopts = {
            bookType: 'xlsx',
            bookSST: false,
            type: 'binary'
          },
          wbout = XLSX.write(wb, wopts)

        let name = `${this.dateSelect} 年${
          { department: '部门目标', user: '员工目标' }[this.tabType]
        }.xlsx`
        FileSaver.saveAs(
          new Blob([this.s2ab(wbout)], {
            type: 'application/octet-stream;charset=utf-8'
          }),
          name
        )
      } else if (type == 'save') {
        this.loading = true
        var list = this.list
        if (this.tabType === 'department') {
          list = this.list.filter(function(item, index, array) {
            return !item.ignore
          })
        } else if (this.tabType === 'user') {
          list = this.list
        }
        crmAchievementUpdate(list)
          .then(res => {
            this.$message.success('操作成功')
            this.loading = false
            this.isEdit = false
            this.updateAhievementList()
          })
          .catch(() => {
            this.loading = false
          })
      } else if (type == 'cancel') {
        this.updateAhievementList()
        this.isEdit = false
      }
    },
    s2ab(s) {
      var cuf
      var i
      if (typeof ArrayBuffer !== 'undefined') {
        cuf = new ArrayBuffer(s.length)
        var view = new Uint8Array(cuf)
        for (i = 0; i !== s.length; i++) {
          view[i] = s.charCodeAt(i) & 0xff
        }
        return cuf
      } else {
        cuf = new Array(s.length)
        for (i = 0; i !== s.length; ++i) {
          cuf[i] = s.charCodeAt(i) & oxFF
        }
        return cuf
      }
    },
    /** 点击搜索 保存 取消时更新信息 */
    updateAhievementList() {
      if (this.tabType === 'department') {
        this.getAhievementList()
      } else if (this.tabType === 'user') {
        this.getAhievementListForUser()
      }
    },
    /** 通过回调控制style */
    cellStyle({ row, column, rowIndex, columnIndex }) {
      if (rowIndex === 1 && this.tabType === 'department') {
        return {
          backgroundColor: '#E5F4FE'
        }
      } else if (
        columnIndex == 1 ||
        columnIndex == 2 ||
        columnIndex == 6 ||
        columnIndex == 10 ||
        columnIndex == 14
      ) {
        return {
          backgroundColor: '#E5F4FE',
          textAlign: 'center'
        }
      } else {
        return { textAlign: 'center' }
      }
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.main-container {
  flex: 1;
  background-color: white;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow-x: auto;
}

.handle-bar {
  padding: 15px 20px 5px 20px;
  .el-date-editor {
    width: 130px;
    margin-right: 15px;
  }
  .el-cascader {
    width: 130px;
    margin-right: 15px;
  }
  .el-select {
    width: 120px;
    margin-right: 15px;
  }
}

.content {
  padding: 10px 20px;
  flex: 1;
  overflow-y: hidden;
}
.content /deep/ .el-table {
  border: 1px solid #e6e6e6;
}

.tabs-bar {
  border-bottom: 1px solid #e6e6e6;
}
.el-tabs /deep/ .el-tabs__nav-wrap::after {
  display: none !important;
}
.el-tabs /deep/ .el-tabs__header {
  padding-left: 17px;
  height: 57px;
  margin-bottom: 0;
}
.el-tabs /deep/ .el-tabs__item {
  font-size: 13px;
  height: 57px;
  line-height: 57px;
}

.el-table /deep/ td {
  padding: 3px 0;
}
.el-table /deep/ th {
  text-align: center;
}

.table-show-item {
  height: 34px;
  line-height: 34px;
  text-align: center;
}
</style>
