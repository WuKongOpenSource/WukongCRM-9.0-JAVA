<template>
  <div class="cr-contianer">
    <div class="title">{{ getTitle() }}</div>
    <div style="height: 100%;position: relative;">
      <div
        v-if="crmType == ''"
        class="cr-body-side">
        <div
          v-for="(item, index) in leftSides"
          :key="index"
          :class="leftType===item.type? 'side-item-select' : 'side-item-default'"
          class="side-item"
          @click="sideClick(item)">{{ item.name }}</div>
      </div>
      <div :style="{ 'padding-left': crmType == '' ? '150px' : '0'}">
        <crm-relative-table
          v-for="(item, index) in leftSides"
          v-show="item.type == leftType"
          :key="index"
          :ref="'crm'+item.type"
          :show="show && item.type == leftType"
          :radio="radio"
          :crm-type="item.type"
          :selected-data="currentSelectedData"
          :action="action"
          @changeCheckout="checkCrmTypeInfos"/>
      </div>
    </div>
    <div class="handle-bar">
      <el-button @click.native="closeView">取消</el-button>
      <el-button
        type="primary"
        @click.native="confirmClick">确定</el-button>
    </div>
  </div>
</template>

<script type="text/javascript">
import CrmRelativeTable from './CrmRelativeTable'
import { objDeepCopy } from '@/utils'

export default {
  name: 'CrmRelatieve', // 相关
  components: {
    CrmRelativeTable
  },
  props: {
    /** 多选框 只能选一个 */
    radio: {
      type: Boolean,
      default: true
    },
    /** 没有值就是全部类型 有值就是当个类型 */
    crmType: {
      type: String,
      default: ''
    },
    /** 需要展示哪些类型 默认关键字数组 */
    showTypes: {
      type: Array,
      default: () => {
        return [
          'customer',
          'contacts',
          'leads',
          'business',
          'contract',
          'product'
        ]
      }
    },
    /** 已选信息 */
    selectedData: {
      type: Object,
      default: () => {
        return {}
      }
    },
    /** true 的时候 发请求 */
    show: {
      type: Boolean,
      default: true
    },
    /**
     * default 默认  condition 固定条件筛选 moduleType 下的
     * relative: 相关 添加
     */
    action: {
      type: Object,
      default: () => {
        return {
          type: 'default',
          data: {}
        }
      }
    }
  },
  data() {
    return {
      leftType: 'customer',
      leftSides: [],
      /** 各类型选择的值 */
      currentSelectedData: {}
    }
  },
  computed: {},
  watch: {
    selectedData: function(data) {
      this.currentSelectedData = objDeepCopy(data)
    },
    // 刷新标记
    show(val) {
      if (val) {
        this.currentSelectedData = objDeepCopy(this.selectedData)
      }
    }
  },
  mounted() {
    var leftItems = {
      customer: {
        name: '客户',
        type: 'customer'
      },
      contacts: {
        name: '联系人',
        type: 'contacts'
      },
      leads: {
        name: '线索',
        type: 'leads'
      },
      business: {
        name: '商机',
        type: 'business'
      },
      contract: {
        name: '合同',
        type: 'contract'
      },
      product: {
        name: '产品',
        type: 'product'
      }
    }
    if (this.crmType) {
      this.leftType = this.crmType
      this.leftSides.push(leftItems[this.crmType])
    } else {
      for (let index = 0; index < this.showTypes.length; index++) {
        const element = this.showTypes[index]
        this.leftSides.push(leftItems[element])
      }
    }
    this.currentSelectedData = objDeepCopy(this.selectedData)
  },
  methods: {
    /**
     * 刷新列表
     */
    refreshList() {
      this.$refs['crm' + this.crmType][0].refreshList()
    },

    sideClick(item) {
      this.leftType = item.type
    },
    clearAll() {
      if (this.crmType) {
        this.$refs['crm' + this.crmType][0].clearAll()
      }
    },
    // 	当用户手动勾选全选 Checkbox 时触发的事件
    selectAll() {},
    // 关闭操作
    closeView() {
      this.$emit('close')
    },
    checkCrmTypeInfos(data) {
      this.currentSelectedData[data.type] = data.data
    },
    // 确定选择
    confirmClick() {
      if (this.crmType) {
        // 以单类型传值
        this.$emit('changeCheckout', {
          data: this.currentSelectedData[this.crmType]
            ? this.currentSelectedData[this.crmType]
            : []
        })
      } else {
        this.$emit('changeCheckout', { data: this.currentSelectedData })
      }

      this.$emit('close')
    },
    // 根据类型获取标题展示名称
    getTitle() {
      if (this.crmType == 'leads') {
        return '关联线索模块'
      } else if (this.crmType == 'customer') {
        return '关联客户模块'
      } else if (this.crmType == 'contacts') {
        return '关联联系人模块'
      } else if (this.crmType == 'business') {
        return '关联商机模块'
      } else if (this.crmType == 'product') {
        return '关联产品模块'
      } else if (this.crmType == 'contract') {
        return '关联合同模块'
      } else {
        return '关联业务模块'
      }
    }
  }
}
</script>
<style rel="stylesheet/scss" lang="scss" scoped>
.cr-contianer {
  height: 450px;
  position: relative;
  padding: 50px 0 50px 0;
}

.title {
  padding: 0 20px;
  font-size: 16px;
  line-height: 50px;
  height: 50px;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 3;
  border-bottom: 1px solid $xr-border-line-color;
}

.handle-bar {
  height: 50px;
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 2;
  button {
    float: right;
    margin-top: 10px;
    margin-right: 10px;
  }
}

.cr-body-side {
  flex-shrink: 0;
  z-index: 3;
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 150px;
  font-size: 12px;
  background-color: white;
  height: 100%;
  border-right: 1px solid $xr-border-line-color;
  .side-item {
    height: 35px;
    line-height: 35px;
    padding: 0 20px;
    cursor: pointer;
  }
}

.side-item-default {
  color: #333;
}

.side-item-select {
  color: #409eff;
  background-color: #ecf5ff;
}
</style>
