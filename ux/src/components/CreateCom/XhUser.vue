<template>
  <div>
    <el-input
      v-model="searchInput"
      placeholder="搜索成员"
      size="small"
      suffix-icon="el-icon-search"
      @input="inputchange"/>
    <div
      v-loading="loading"
      class="search-list">
      <el-checkbox-group
        v-model="selectItems"
        @change="changeCheckout">
        <el-checkbox
          v-for="(item, i) in list"
          v-if="item.show"
          :key="i"
          :label="item"
          :disabled="item.disabled"
          class="colleagues-list">
          <div
            v-photo="item"
            v-lazy:background-image="$options.filters.filterUserLazyImg(item.img)"
            class="div-photo search-img"/>
          <span>{{ item.realname }}</span>
        </el-checkbox>
      </el-checkbox-group>
    </div>
  </div>
</template>
<script type="text/javascript">
import { usersList } from '@/api/common'

export default {
  name: 'XhUser', // 新建 user
  components: {},
  props: {
    value: {
      type: String,
      default: ''
    },
    /** 多选框 只能选一个 */
    radio: {
      type: Boolean,
      default: false
    },
    /** 已选信息 */
    selectedData: {
      type: Array,
      default: () => {
        return []
      }
    },
    /** 获取不同的员工展示列表 */
    infoType: {
      type: String,
      default: 'default' // 返回全部  crm_contract crm_receivables oa_examine 合同审核人自选列表
    },
    infoRequest: Function,
    /** 请求辅助参数 */
    infoParams: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    return {
      list: [],
      selectItems: [], // 选择项
      loading: false, // 加载动画
      searchInput: ''
    }
  },
  computed: {},
  watch: {
    selectedData: function(value) {
      this.checkItems(value)
    }
  },
  mounted() {
    this.getUserList()
  },
  methods: {
    // 获取员工列表
    getUserList() {
      this.loading = true
      this.getRequest()(this.getParams())
        .then(res => {
          var self = this
          this.list = res.data.map(function(item, index, array) {
            item.disabled = false // 要求单选
            item.show = true
            if (self.selectedData.length > 0) {
              let disabled = true
              for (let index = 0; index < self.selectedData.length; index++) {
                const element = self.selectedData[index]
                if (element.userId == item.userId) {
                  disabled = false
                  self.selectItems.push(item)
                }
              }
              if (self.radio) {
                item.disabled = disabled
              }
            }
            return item
          })
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },
    getRequest() {
      if (this.infoRequest) {
        return this.infoRequest
      } else if (this.infoType === 'default') {
        return usersList
      } else if (
        this.infoType === 'crm_contract' ||
        this.infoType === 'crm_receivables' ||
        this.infoType === 'oa_examine'
      ) {
        return usersList
      }
    },
    getParams() {
      const params =
        this.infoParams && Object.keys(this.infoParams.length !== 0)
          ? this.infoParams
          : {}
      if (this.infoType === 'default') {
        params.pageType = 0
        return params
      } else if (
        this.infoType === 'crm_contract' ||
        this.infoType === 'crm_receivables' ||
        this.infoType === 'oa_examine'
      ) {
        params.pageType = 0
        return params
      }
    },
    changeCheckout(items) {
      if (this.radio) {
        if (items.length) {
          var element = items[0]
          this.list = this.list.map(function(item, index, array) {
            if (element.userId == item.userId) {
              item.disabled = false
            } else {
              item.disabled = true
            }
            return item
          })
        } else {
          this.list = this.list.map(function(item, index, array) {
            item.disabled = false
            return item
          })
        }
      }

      this.$emit('changeCheckout', { data: this.selectItems })
    },
    /** 取消勾选 */
    cancelCheckItem(item) {
      var removeIndex = -1
      for (let index = 0; index < this.selectItems.length; index++) {
        const element = this.selectItems[index]
        if (element.userId == item.userId) {
          removeIndex = index
        }
      }
      this.selectItems.splice(removeIndex, 1)
      if (this.radio && this.selectItems.length == 0) {
        this.list = this.list.map(function(item, index, array) {
          item.disabled = false
          return item
        })
      }
    },
    /** 标记勾选 */
    checkItems(items) {
      this.selectItems = []
      if (items.length > 0) {
        for (let bigIndex = 0; bigIndex < this.list.length; bigIndex++) {
          const item = this.list[bigIndex]

          let disabled = true
          for (let index = 0; index < items.length; index++) {
            const element = items[index]
            if (element.userId == item.userId) {
              disabled = false
              this.selectItems.push(item)
            }
          }
          if (this.radio) {
            item.disabled = disabled
          }
        }
      }
    },
    // 搜索
    inputchange(val) {
      this.list = this.list.map(function(item, index, array) {
        if (item.realname.indexOf(val) != -1) {
          item.show = true
        } else {
          item.show = false
        }
        return item
      })
    }
  }
}
</script>
<style lang="scss" scoped>
/* 选择员工 */
.search-img {
  width: 24px;
  height: 24px;
  border-radius: 12px;
  vertical-align: middle;
  margin-right: 8px;
}
.search-list {
  padding: 5px;
  height: 248px;
  overflow: auto;
}
.colleagues-list {
  padding: 5px;
  display: block;
  margin-left: 0;
}
</style>
