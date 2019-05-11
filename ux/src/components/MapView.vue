<template>
  <div class="map-view">
    <div id="choicemap"></div>
    <i class="el-icon-close map-close"
       @click="hiddenView"></i>
  </div>
</template>

<script type="text/javascript">
import { getMaxIndex } from '@/utils/index'

export default {
  name: 'map-view', // 地图详情
  components: {},
  computed: {},
  data() {
    return {}
  },
  props: {
    /**
     * title 内容
     * lat lng 经纬度
     */
    title: {
      type: String,
      default: ''
    },
    lat: {
      type: Number,
      default: 0
    },
    lng: {
      type: Number,
      default: 0
    }
  },
  mounted() {
    this.$el.style.zIndex = getMaxIndex()
    document.body.appendChild(this.$el)

    var map = new BMap.Map('choicemap', { enableMapClick: false })
    var point = new BMap.Point(this.lng, this.lat)
    map.centerAndZoom(point, 18)
    map.enableScrollWheelZoom()

    var marker = new BMap.Marker(point) // 创建标注
    map.addOverlay(marker) // 将标注添加到地图中
    var infoWindow = new BMap.InfoWindow(this.title) // 创建信息窗口对象
    marker.addEventListener('click', function() {
      map.openInfoWindow(infoWindow, point) //开启信息窗口
    })
  },
  methods: {
    hiddenView() {
      this.$emit('hidden')
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
.map-view {
  position: fixed;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  background-color: #000;
}

.map-close {
  position: absolute;
  right: 10px;
  top: 10px;
  font-size: 28px;
  color: #fff;
  cursor: pointer;
}

#choicemap {
  position: absolute;
  left: 100px;
  top: 100px;
  bottom: 100px;
  right: 100px;
  overflow: hidden;
}
</style>
