<template>
  <flexbox align="stretch">
    <flexbox-item style="margin-right: 50px;">
      <div class="area-title">定位</div>
      <el-autocomplete
        v-model="searchInput"
        :fetch-suggestions="querySearchAsync"
        style="width: 100%;"
        placeholder="请输入详细位置名称"
        @blur="inputBlur"
        @focus="inputFocus"
        @select="handleSelect">
        <template slot-scope="{ item }">
          <div class="name">{{ item.address + item.title }}</div>
        </template>
      </el-autocomplete>
      <div
        id="choicemap"
        class="map"/>
      <div class="area-title">详细地址</div>
      <el-input
        v-model="detailAddress"
        placeholder=""/>
    </flexbox-item>
    <flexbox-item>
      <div class="area-title">省/市/区</div>
      <v-distpicker
        :province="addressSelect.province"
        :city="addressSelect.city"
        :area="addressSelect.area"
        @province="selectProvince"
        @city="selectCity"
        @area="selectArea"/>
    </flexbox-item>
  </flexbox>
</template>
<script type="text/javascript">
import VDistpicker from 'v-distpicker'

export default {
  name: 'XhCustomerAddress', // 新建 客户位置
  components: {
    VDistpicker
  },
  props: {
    value: {
      type: Object,
      default: () => {
        return {}
      }
    },
    /** 索引值 用于更新数据 */
    index: Number,
    /** 包含数据源 */
    item: Object
  },
  data() {
    return {
      map: null,
      /** 搜索地图输入框 */
      searchInput: '',
      searchCopyInput: '', // 避免修改
      /** 完整地址输入框 */
      detailAddress: '',
      pointAddress: null, // 经纬度点
      /** 区域选择 */
      addressSelect: {
        province: '',
        city: '',
        area: ''
      },
      /** 防止联动情况  */
      canExecute: true
    }
  },
  computed: {},
  watch: {
    pointAddress: function(newValue) {
      this.valueChange()
    },
    detailAddress: function(newValue) {
      this.valueChange()
    }
  },
  mounted() {
    var map = new BMap.Map('choicemap', { enableMapClick: false })
    map.centerAndZoom(new BMap.Point(116.404, 39.915), 14)
    // map.disableDragging() //禁止拖拽
    // map.disableDoubleClickZoom()
    // map.disableScrollWheelZoom()
    // map.disableContinuousZoom()
    map.enableScrollWheelZoom()
    this.map = map
    if (this.value && JSON.stringify(this.value) !== '{}') {
      this.initInfo(this.value)
    } else {
      var geolocation = new BMap.Geolocation()
      var self = this
      geolocation.getCurrentPosition(
        function(r) {
          if (this.getStatus() == BMAP_STATUS_SUCCESS) {
            self.addMarkerLabel(r.point)
          }
        },
        { enableHighAccuracy: true }
      )
    }
  },
  methods: {
    initInfo(val) {
      this.searchInput = val.location
      this.detailAddress = val.detailAddress
      if (Object.prototype.toString.call(val.address) == '[object Array]') {
        var address = {}
        for (let index = 0; index < val.address.length; index++) {
          index === 0 ? (address['province'] = val.address[0]) : ''
          index === 1 ? (address['city'] = val.address[1]) : ''
          index === 2 ? (address['area'] = val.address[2]) : ''
        }
        this.addressSelect = address
      }
      if (val.lng != 0 && val.lat != 0) {
        this.pointAddress = new BMap.Point(val.lng, val.lat)
        this.addMarkerLabel(this.pointAddress)
      }
    },
    querySearchAsync(queryString, cb) {
      if (queryString) {
        var options = {
          onSearchComplete: function(results) {
            if (local.getStatus() == BMAP_STATUS_SUCCESS) {
              var address = []
              for (var i = 0; i < results.getCurrentNumPois(); i++) {
                address.push(results.getPoi(i))
              }
              cb(address)
            } else {
              cb([])
            }
          },
          pageCapacity: 20
        }
        var local = new BMap.LocalSearch(this.map, options)
        local.search(queryString)
      } else {
        cb([])
      }
    },
    /** 搜索结果选择 */
    handleSelect(item) {
      this.searchInput = item.address + item.title
      this.searchCopyInput = this.searchInput // 只能通过这种方式修改

      this.detailAddress = this.searchInput
      this.addMarkerLabel(item.point)
      this.pointAddress = item.point
      this.mapSelectArea(item)
    },
    /** Input 失去焦点  searchInput 只能通过选择更改*/
    inputBlur() {
      if (this.searchCopyInput !== this.searchInput) {
        this.searchInput = this.searchCopyInput
      }
    },
    inputFocus() {
      this.searchCopyInput = this.searchInput
    },
    // 创建标注
    addMarkerLabel(point) {
      this.map.clearOverlays()
      this.map.centerAndZoom(point, 14)
      this.map.addOverlay(new BMap.Marker(point))
    },
    /** 区域选择 */
    selectProvince(value) {
      this.addressSelect.province = value.value
      this.valueChange()
    },
    selectCity(value) {
      this.addressSelect.city = value.value
      this.valueChange()
    },
    selectArea(value) {
      this.addressSelect.area = value.value
      this.valueChange()
    },
    /** 地图选择区域 */
    mapSelectArea(data) {
      if (this.canExecute) {
        this.canExecute = false
        // this.addressSelect.province = data.province ? data.province : "";
        // this.addressSelect.city = data.city ? data.city : "";
        /** 因为poi里面不包含区域 所以需要逆地址解析 */
        var myGeo = new BMap.Geocoder()
        // 根据坐标得到地址描述
        var self = this
        myGeo.getLocation(
          new BMap.Point(data.point.lng, data.point.lat),
          function(result) {
            if (result) {
              // 获取经纬度点
              self.pointAddress = result.point
              self.addressSelect.province = result.addressComponents.province
                ? result.addressComponents.province
                : ''
              self.addressSelect.city = result.addressComponents.city
                ? result.addressComponents.city
                : ''
              self.addressSelect.area = result.addressComponents.district
                ? result.addressComponents.district
                : ''
            }
          }
        )

        setTimeout(() => {
          self.canExecute = true
        }, 500)
      }
    },
    // 值更新的回调
    valueChange() {
      this.$emit('value-change', {
        index: this.index,
        value: {
          address: [
            this.addressSelect.province,
            this.addressSelect.city,
            this.addressSelect.area
          ],
          location: this.searchInput,
          detailAddress: this.detailAddress,
          lat: this.pointAddress ? this.pointAddress.lat : '',
          lng: this.pointAddress ? this.pointAddress.lng : ''
        }
      })
    }
  }
}
</script>
<style rel="stylesheet/scss" lang="scss" scoped>
.map {
  height: 150px;
  width: 100%;
  overflow: hidden;
  margin-top: 5px;
}

.area-title {
  font-size: 12px;
  color: #aaa;
  padding-left: 10px;
}

.distpicker-address-wrapper /deep/ select {
  height: 34px;
  font-size: 12px;
  border-radius: 0.1rem;
}
</style>
