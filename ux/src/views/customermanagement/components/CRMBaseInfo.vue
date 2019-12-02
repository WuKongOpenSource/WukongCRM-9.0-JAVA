<template>
  <div class="b-cont">
    <div>
      <sections
        class="b-cells"
        title="基本信息"
        m-color="#46CDCF"
        content-height="auto">
        <flexbox
          :gutter="0"
          wrap="wrap"
          style="padding: 10px 8px 0;">
          <flexbox-item
            v-for="(item, index) in list"
            :span="0.5"
            :key="index"
            class="b-cell">
            <flexbox
              v-if="item.type === 8"
              align="stretch"
              class="b-cell-b">
              <div class="b-cell-name">{{ item.name }}</div>
              <div class="b-cell-value">
                <flexbox
                  v-for="(file, index) in item.value"
                  :key="index"
                  class="f-item">
                  <img
                    class="f-img"
                    src="@/assets/img/relevance_file.png" >
                  <div class="f-name">{{ file | fileName }}</div>
                  <el-button
                    type="text"
                    @click.native="handleFile('preview', item, index)">预览</el-button>
                  <el-button
                    type="text"
                    @click.native="handleFile('download', file, index)">下载</el-button>
                </flexbox>
              </div>
            </flexbox>

            <flexbox
              v-else
              align="stretch"
              class="b-cell-b">
              <div class="b-cell-name">{{ item.name }}</div>
              <div class="b-cell-value">{{ item.value }}</div>
            </flexbox>
          </flexbox-item>
        </flexbox>
      </sections>
    </div>
    <map-view
      v-if="showMapView"
      :title="mapViewInfo.title"
      :lat="mapViewInfo.lat"
      :lng="mapViewInfo.lng"
      @hidden="showMapView=false"/>
  </div>
</template>

<script>
import loading from '../mixins/loading'
import crmTypeModel from '@/views/customermanagement/model/crmTypeModel'
import Sections from '../components/Sections'
import { filedGetInformation } from '@/api/customermanagement/common'
import { fileSize, downloadFile } from '@/utils'
import MapView from '@/components/MapView' // 地图详情

export default {
  /** 客户管理 的 基本信息*/
  name: 'CRMBaseInfo',
  components: {
    Sections,
    MapView
  },
  filters: {
    addressShow: function(list) {
      return list ? list.join(' ') : ''
    },
    fileName(file) {
      const name = file.name && file.name.length > 10 ? (file.name.substring(0, 10) + '...') : file.name
      return name + '(' + fileSize(file.size) + ')'
    }
  },
  mixins: [loading],
  props: {
    /** 模块ID */
    id: [String, Number],
    detail: {
      type: Object,
      default: () => {
        return {}
      }
    },
    /** 没有值就是全部类型 有值就是当个类型 */
    crmType: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      list: [],
      showMapView: false, // 控制展示地图详情
      mapViewInfo: {} // 地图详情信息
    }
  },
  computed: {},
  watch: {
    id: function(val) {
      this.getBaseInfo()
    }
  },
  mounted() {
    this.getBaseInfo()
  },
  activated: function() {},
  deactivated: function() {},
  methods: {
    // 获取基础信息
    getBaseInfo() {
      this.loading = true
      filedGetInformation({
        types: crmTypeModel[this.crmType],
        id: this.id
      })
        .then(res => {
          this.list = res.data
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },
    /**
     * 附件查看
     */
    handleFile(type, item, index) {
      if (type === 'preview') {
        var previewList = item.value.map(element => {
          element.url = element.filePath
          return element
        })
        this.$bus.emit('preview-image-bus', {
          index: index,
          data: previewList
        })
      } else if (type === 'download') {
        downloadFile({ path: item.filePath, name: item.name })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.b-cont {
  position: relative;
  padding: 0 50px 20px 20px;
}

.b-cells {
  margin-top: 25px;
}

.b-cell {
  padding: 0 10px;
  .b-cell-b {
    width: auto;
    padding: 8px;
    .b-cell-name {
      width: 100px;
      margin-right: 10px;
      font-size: 13px;
      flex-shrink: 0;
      color: #777;
    }
    .b-cell-value {
      font-size: 13px;
      color: #333;
      white-space: pre-wrap;
      word-wrap: break-word;
    }
    .b-cell-foot {
      flex-shrink: 0;
      display: block;
      width: 15px;
      height: 15px;
      margin-left: 8px;
    }
  }
}

.f-item {
  padding: 3px 0;
  height: 25px;
  .f-img {
    position: block;
    width: 15px;
    height: 15px;
    padding: 0 1px;
    margin-right: 8px;
  }
  .f-name {
    color: #666;
    font-size: 12px;
    margin-right: 10px;
  }
}
</style>
