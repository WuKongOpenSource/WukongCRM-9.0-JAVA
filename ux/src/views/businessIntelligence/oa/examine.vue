<template>
  <div
    v-loading="loading"
    class="main-container">
    <filtrate-handle-view
      class="filtrate-bar"
      module-type="oa"
      @load="loading=true"
      @change="getDataList">
      <el-button
        class="export-button"
        type="primary"
        @click.native="exportExcel">导出</el-button>
    </filtrate-handle-view>
    <div class="content">
      <div class="table-content">
        <el-table
          :data="list"
          :height="tableHeight"
          :cell-style="cellStyle"
          stripe
          border
          show-overflow-tooltip
          highlight-current-row
          @row-click="handleRowClick">
          <el-table-column
            v-for="(item, index) in fieldList"
            :key="index"
            :fixed="index==0"
            :formatter="fieldFormatter"
            :prop="item.field"
            :min-width="item.width"
            :label="item.name"
            align="center"
            header-align="center"/>
        </el-table>
      </div>
    </div>

    <!-- 列表详情 -->
    <examine-list
      v-if="showList"
      :request="indexRequest"
      :params="indexParams"
      @hide="showList = false"/>
  </div>
</template>

<script>
import {
  biExamineStatisticsAPI,
  biExamineExcelExportAPI,
  biExamineIndexAPI
} from '@/api/businessIntelligence/oa'
import ExamineList from './components/examineList'
import filtrateHandleView from '../components/filtrateHandleView'

export default {
  /** 审批统计表 */
  name: 'ExamineStatistics',

  components: {
    filtrateHandleView,
    ExamineList
  },

  data() {
    return {
      loading: false,
      tableHeight: document.documentElement.clientHeight - 190,
      postParams: {},
      list: [],
      fieldList: [],

      // 列表详情
      indexParams: {},
      showList: false
    }
  },

  computed: {
    // 列表详情请求
    indexRequest() {
      return biExamineIndexAPI
    }
  },

  mounted() {
    /** 控制table的高度 */
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 190
    }
  },

  methods: {
    /**
     * 列表数据
     */
    getDataList(params) {
      this.postParams = params
      this.loading = true
      biExamineStatisticsAPI(params)
        .then(res => {
          this.fieldList = this.getFieldList(res.data.categoryList || [])
          this.list = res.data.userList || []
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * 表头
     */
    getFieldList(list) {
      return [
        {
          name: '员工',
          width: 100,
          field: 'realname'
        }
      ].concat(
        list.map(item => {
          var width = 0
          if (item.title && item.title.length <= 6) {
            width = item.title.length * 15 + 45
          } else {
            width = 140
          }
          return {
            name: item.title,
            width: width,
            field: `count_${item.categoryId}`
          }
        })
      )
    },

    /**
     * 格式化字段
     */
    fieldFormatter(row, column) {
      return row[column.property] || '--'
    },

    /**
     * 通过回调控制style
     */
    cellStyle({ row, column, rowIndex, columnIndex }) {
      if (column.property !== 'realname' && row[column.property]) {
        return { color: '#3E84E9', cursor: 'pointer' }
      } else {
        return ''
      }
    },

    /**
     * 列表的点击
     */
    handleRowClick(row, column, event) {
      if (column.property !== 'realname' && row[column.property]) {
        const propertys = column.property.split('_')
        const categoryId = propertys.length > 1 ? propertys[1] : ''

        const params = {
          userId: row.userId,
          categoryId: categoryId
        }

        if (this.postParams.type) {
          params.type = this.postParams.type
        } else {
          params.startTime = this.postParams.startTime
          params.endTime = this.postParams.endTime
        }
        this.indexParams = params
        this.showList = true
      }
    },

    /**
     * 导出
     */
    exportExcel() {
      this.loading = true
      biExamineExcelExportAPI(this.postParams)
        .then(res => {
          this.loading = false
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
        .catch(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import '../styles/detail.scss';
</style>
