<template>
  <flexbox class="cell">
    <i
      :class="'wukong-' + type"
      class="cell-head wukong"/>
    <div
      :class="{'cursor-pointer' :cursorPointer}"
      class="cell-body"
      @click="bodyClick">
      {{ getShowName() }}
    </div>
    <img
      v-if="showFoot"
      class="cell-foot"
      style="cursor: pointer;"
      src="@/assets/img/cancel_associated.png"
      @click="footClick" >
  </flexbox>
</template>

<script type="text/javascript">
export default {
  name: 'RelatedBusinessCell',
  props: {
    type: {
      type: String,
      default: ''
    },
    cellIndex: Number,
    data: Object,
    showFoot: {
      type: Boolean,
      default: true
    },
    cursorPointer: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {}
  }, // 相关CRM等类型展示布局
  computed: {
    typeName() {
      if (this.type == 'customer') {
        return '客户'
      } else if (this.type == 'contacts') {
        return '联系人'
      } else if (this.type == 'business') {
        return '商机'
      } else if (this.type == 'contract') {
        return '合同'
      }
      return ''
    }
  },
  watch: {},
  mounted() {},

  beforeDestroy() {},
  methods: {
    footClick() {
      this.$emit('unbind', this.type, this.cellIndex, this.data)
    },
    bodyClick() {
      this.$emit('detail', this.type, this.cellIndex, this.data)
    },
    getShowName(item) {
      return (
        this.typeName +
        '-' +
        (this.data.name ||
          this.data.businessName ||
          this.data.customerName ||
          this.data.number)
      )
    }
  }
}
</script>
<style lang="scss" scoped>
.cell {
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 2px;
  position: relative;
  margin-bottom: 5px;

  .cell-head {
    display: block;
    width: 15px;
    height: 15px;
    margin-right: 8px;
    color: #6394e5;
    font-size: 14px;
  }

  .cell-body {
    flex: 1;
    color: #3e84e9;
    font-size: 12px;
  }

  .cell-foot {
    display: block;
    width: 24px;
    padding: 0 4px;
    margin-right: 8px;
  }
}

.cursor-pointer {
  cursor: pointer;
}
</style>
