<template>
  <div>
    <el-input placeholder="搜索部门名称"
              size="small"
              v-model="searchInput"
              suffix-icon="el-icon-search"
              @input="inputchange">
    </el-input>
    <div v-loading="loading"
         class="search-list">
      <el-breadcrumb style="padding: 5px 0;"
                     separator-class="el-icon-arrow-right">
        <el-breadcrumb-item v-for="(item, index) in breadcrumbList"
                            :key="index">
          <a href="javascript:;"
             @click="breadcrumbBtn(item, index)">{{item.label}}</a>
        </el-breadcrumb-item>
      </el-breadcrumb>
      <flexbox v-for="(item, index) in showlist"
               :key="index"
               v-if="item.show"
               class="stru-list">
        <el-checkbox class="stru-check"
                     v-model="item.isCheck"
                     :disabled="item.disabled"
                     @change="checkChange(item, index)"></el-checkbox>
        <div @click="enterChildren(item)"
             class="stru-name">{{item.name}}</div>
        <div class="el-icon-arrow-right stru-enter"
             v-if="item.children"></div>
      </flexbox>
    </div>
  </div>
</template>
<script type="text/javascript">
import { depList } from '@/api/common'

export default {
  name: 'xh-structure', // 新建 structure
  components: {},
  computed: {},
  watch: {},
  data() {
    return {
      breadcrumbList: [], // 面包屑数据
      selectItems: [], // 选择项
      showlist: [], // 展示数据
      loading: false, // 加载动画
      searchInput: ''
    }
  },
  props: {
    value: {
      type: Array,
      default: () => {
        return []
      }
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
    }
  },
  mounted() {
    this.selectItems = this.selectedData
    // 部门列表数据
    this.getStructureList()
  },
  methods: {
    // 部门列表数据
    getStructureList() {
      this.loading = true
      depList({
        type: 'tree'
      })
        .then(res => {
          this.showlist = this.addIsCheckProp(res.data)
          this.breadcrumbList.push({ label: '全部', data: this.showlist })

          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },
    // 面包屑点击事件
    breadcrumbBtn(item, index) {
      if (this.radio && this.selectItems.length == 1) return
      if (index + 1 <= this.breadcrumbList.length - 1) {
        this.breadcrumbList.splice(index + 1, this.breadcrumbList.length - 1)
      }
      this.showlist = []
      this.showlist = this.handelCheck(item.data)
    },
    // 点击checkbox选中
    checkChange(item, aindex) {
      this.$set(this.showlist, aindex, item)
      var removeIndex = -1
      for (let index = 0; index < this.selectItems.length; index++) {
        const element = this.selectItems[index]
        if (item.id == element.id) {
          removeIndex = index
        }
      }
      if (removeIndex == -1 && item.isCheck) {
        this.selectItems.push(item)
      } else if (removeIndex != -1) {
        this.selectItems.splice(removeIndex, 1)
      }

      /** 单选逻辑 */
      if (this.radio) {
        if (item.isCheck) {
          this.showlist = this.showlist.map(function(element, index, array) {
            if (element.id == item.id) {
              element.disabled = false
            } else {
              element.disabled = true
            }
            return element
          })
        } else {
          this.showlist = this.showlist.map(function(item, index, array) {
            item.disabled = false
            return item
          })
        }
      }

      this.$emit('changeCheckout', { data: this.selectItems })
    },
    /** 数据重新刷新时 循环标记展示数组 */
    handelCheck(list) {
      var self = this
      list = list.map(function(item, index, array) {
        item.isCheck = self.selectItemsHasItem(item)
        return item
      })
      this.inputchange()
      return list
    },
    selectItemsHasItem(item) {
      if (this.selectItems.length == 0) {
        return false
      }
      var hasItem = false
      for (let index = 0; index < this.selectItems.length; index++) {
        const element = this.selectItems[index]
        if (item.id == element.id) {
          hasItem = true
          break
        }
      }
      return hasItem
    },
    /**  */
    /** 点击进入子数组 */
    enterChildren(item) {
      // 保证单选环境下 没有选中 才可进入children
      if (item.children && !(this.radio && this.selectItems.length == 1)) {
        this.showlist = []
        this.showlist = this.handelCheck(this.addIsCheckProp(item.children))
        this.breadcrumbList.push({ label: item.label, data: this.showlist })
      }
    },
    /** 给默认数据加isCheck属性 */
    addIsCheckProp(list) {
      if (list.length > 0) {
        var item = list[0]
        if (item.hasOwnProperty('isCheck')) {
          return list
        } else {
          return list.map(function(item, index, array) {
            item.disabled = false
            item.isCheck = false
            item.show = true
            return item
          })
        }
      }
      return list
    },
    // 父组件 选中
    parentRemoveCheck(data) {
      this.selectItems = data.data
      var temps = this.showlist
      this.showlist = []
      this.showlist = this.handelCheck(temps)
      /** 单选逻辑 */
      if (this.radio) {
        this.showlist = this.showlist.map(function(item, index, array) {
          item.disabled = false
          return item
        })
      }
    },
    /** 搜索 */
    inputchange(val) {
      this.showlist = this.showlist.map(function(item, index, array) {
        if (item.name.indexOf(val) != -1) {
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
.search-list {
  padding: 5px;
  height: 248px;
  overflow: auto;
}
.stru-list {
  padding: 5px;
  font-size: 13px;
  .stru-check {
    margin-right: 8px;
  }
  .stru-name {
    flex: 1;
  }
  .stru-enter {
    margin-right: 8px;
  }
}
</style>
