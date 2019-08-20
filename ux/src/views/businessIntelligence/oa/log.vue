<template>
  <div v-loading="loading"
       class="main-container">
    <filtrate-handle-view class="filtrate-bar"
                          moduleType="oa"
                          @load="loading=true"
                          @change="getDataList">
      <el-button @click.native="exportExcel"
                 class="export-button"
                 type="primary">导出</el-button>
    </filtrate-handle-view>
    <div class="content">
      <div class="table-content">
        <el-table :data="list"
                  :height="tableHeight"
                  stripe
                  border
                  highlight-current-row>
          <el-table-column v-for="(item, index) in fieldList"
                           :key="index"
                           align="center"
                           header-align="center"
                           :prop="item.field"
                           :label="item.name">
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import {
  biLogStatisticsAPI,
  biLogExcelExportAPI
} from '@/api/businessIntelligence/oa'
import filtrateHandleView from '../components/filtrateHandleView'

export default {
  /** 日志统计表 */
  name: 'log-statistics',

  components: {
    filtrateHandleView
  },

  data() {
    return {
      loading: false,
      tableHeight: document.documentElement.clientHeight - 190,
      postParams: {},
      list: [],
      fieldList: [
        { field: 'realname', name: '员工' },
        { field: 'count', name: '填写数' },
        { field: 'unReadCont', name: '接收人未读数' },
        { field: 'unCommentCount', name: '未评论数' },
        { field: 'commentCount', name: '已评论数' }
      ]
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
      biLogStatisticsAPI(params)
        .then(res => {
          this.list = res.data || []
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * 导出
     */
    exportExcel() {
      this.loading = true
      biLogExcelExportAPI(this.postParams)
        .then(res => {
          this.loading = false
          var blob = new Blob([res.data], {
            type: 'application/vnd.ms-excel;charset=utf-8'
          })
          var downloadElement = document.createElement('a')
          var href = window.URL.createObjectURL(blob) //创建下载的链接
          downloadElement.href = href
          downloadElement.download =
            decodeURI(
              res.headers['content-disposition'].split('filename=')[1]
            ) || '' //下载后文件名
          document.body.appendChild(downloadElement)
          downloadElement.click() //点击下载
          document.body.removeChild(downloadElement) //下载完成移除元素
          window.URL.revokeObjectURL(href) //释放掉blob对象
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
