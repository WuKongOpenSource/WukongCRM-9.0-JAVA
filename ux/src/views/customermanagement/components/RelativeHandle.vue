<template>
  <div class="rc-cont"
       v-empty="list"
       style="padding-right:250px;min-height:500px;">
    <flexbox v-for="(item, index) in list"
             :key="index"
             class="ha-cont"
             align="stretch"
             justify="flex-start">
      <div class="ha-week">{{item.createTime|filterTimestampToFormatTime('MM-DD dddd')}}</div>
      <div class="ha-circle"></div>
      <div class="ha-time">{{item.createTime|filterTimestampToFormatTime('H:mm')}}</div>
      <div v-photo="item"
           v-lazy:background-image="$options.filters.filterUserLazyImg(item.img)"
           class="div-photo ha-img"></div>
      <div class="ha-name">{{item.realname}}</div>
      <div class="ha-content">
        <p v-for="(info, infoIndex) in item.content"
           :key="infoIndex">{{info}}</p>
      </div>
      <div class="ha-line"></div>
    </flexbox>
  </div>
</template>

<script type="text/javascript">
import loading from '../mixins/loading'
import crmTypeModel from '@/views/customermanagement/model/crmTypeModel'
import { crmIndexFieldRecord } from '@/api/customermanagement/common'
import { timestampToFormatTime } from '@/utils'

export default {
  name: 'relative-handle', //相关操作  可能再很多地方展示 放到客户管理目录下
  components: {},
  computed: {},
  mixins: [loading],
  data() {
    return {
      list: []
    }
  },
  watch: {
    id: function(val) {
      this.list = []
      this.getDetail()
    }
  },
  props: {
    /** 模块ID */
    id: [String, Number],
    /** 联系人人下 新建商机 需要联系人里的客户信息  合同下需要客户和商机信息 */
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
    },
    /** 是公海 默认是客户 */
    isSeas: {
      type: Boolean,
      default: false
    }
  },
  mounted() {
    this.getDetail()
  },
  activated: function() {},
  deactivated: function() {},
  methods: {
    getDetail() {
      this.loading = true
      crmIndexFieldRecord({
        types: crmTypeModel[this.crmType],
        actionId: this.id
      })
        .then(res => {
          this.loading = false
          this.list = res.data.map(item => {
            item.createTime = new Date(item.createTime).getTime()
            return item
          })
        })
        .catch(() => {
          this.loading = false
        })
    }
  }
}
</script>
<style lang="scss" scoped>
@import '../styles/relativecrm.scss';
.ha-cont {
  font-size: 12px;
  position: relative;
  line-height: 20px;
  min-height: 40px;
  padding-top: 3px;
  .ha-week {
    margin: 0 17px 0 10px;
    flex-shrink: 0;
    color: #777;
    width: 80px;
  }
  .ha-time {
    padding: 0 10px 0 24px;
    flex-shrink: 0;
    color: #aaa;
  }
  .ha-circle {
    flex-shrink: 0;
    width: 18px;
    height: 18px;
    border-radius: 9px;
    background-color: white;
    border: 5px solid #a5ecd7;
  }
  .ha-img {
    flex-shrink: 0;
    margin: -3px 10px 0 10px;
    display: block;
    width: 30px;
    height: 30px;
    border-radius: 15px;
  }
  .ha-name {
    padding: 0 10px;
    flex-shrink: 0;
    color: #333;
  }
  .ha-content {
    padding: 0px 10px 10px 10px;
    flex: 1;
    color: #333;
  }
  .ha-line {
    position: absolute;
    z-index: -1;
    width: 1px;
    top: 3px;
    bottom: -3px;
    left: 115px;
    background-color: #e6e6e6;
  }
}
</style>
