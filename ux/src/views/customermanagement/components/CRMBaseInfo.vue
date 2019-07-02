<template>
  <div class="b-cont">
    <div>
      <sections class="b-cells"
                title="基本信息"
                m-color="#46CDCF"
                content-height="auto">
        <flexbox :gutter="0"
                 wrap="wrap"
                 style="padding: 10px 8px 0;">
          <flexbox-item :span="0.5"
                        v-for="(item, index) in list"
                        :key="index"
                        class="b-cell">
            <flexbox align="stretch"
                     class="b-cell-b">
              <div class="b-cell-name">{{item.name}}</div>
              <div class="b-cell-value">{{item.value}}</div>
            </flexbox>
          </flexbox-item>
        </flexbox>
      </sections>
    </div>
    <map-view v-if="showMapView"
              :title="mapViewInfo.title"
              :lat="mapViewInfo.lat"
              :lng="mapViewInfo.lng"
              @hidden="showMapView=false"></map-view>
  </div>
</template>

<script>
import loading from '../mixins/loading'
import crmTypeModel from '@/views/customermanagement/model/crmTypeModel'
import Sections from '../components/Sections'
import { filedGetInformation } from '@/api/customermanagement/common'
import { getDateFromTimestamp } from '@/utils'
import moment from 'moment'
import MapView from '@/components/MapView' // 地图详情
import { downloadFile } from '@/utils'

export default {
  /** 客户管理 的 基本信息*/
  name: 'c-r-m-base-info',
  components: {
    Sections,
    MapView
  },
  mixins: [loading],
  filters: {
    addressShow: function(list) {
      return list ? list.join(' ') : ''
    }
  },
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
  watch: {
    id: function(val) {
      this.getBaseInfo()
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
          var self = this
          this.list = res.data
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
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
