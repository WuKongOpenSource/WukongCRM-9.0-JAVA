<template>
  <el-popover
    v-model="showTypePopover"
    placement="bottom"
    width="200"
    popper-class="no-padding-popover"
    trigger="click">
    <div class="type-popper">
      <div class="type-content">
        <div
          v-for="(item, index) in typeOptions"
          :key="index"
          :class="{ 'selected' : selectType.value == item.value && !showCustomContent}"
          class="type-content-item"
          @click="typeSelectClick(item)">
          <div class="mark"/>{{ item.label }}
        </div>
        <div
          :class="{ 'selected' : showCustomContent}"
          class="type-content-item"
          @click="showCustomContent = true">
          <div class="mark"/>自定义
        </div>
      </div>
      <div
        v-if="showCustomContent"
        class="type-content-custom">
        <el-date-picker
          v-model="startTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="选择日期"/>
        <el-date-picker
          v-model="endTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="选择日期"/>
        <el-button @click="customSureClick">确定</el-button>
      </div>
    </div>
    <el-input
      slot="reference"
      v-model="typeShowValue"
      :readonly="true"
      placeholder="请选择选择"
      class="type-select">
      <i
        slot="suffix"
        :class="['el-input__icon', 'el-icon-' + iconClass]"/>
    </el-input>
  </el-popover>
</template>

<script type="text/javascript">
export default {
  name: 'TimeTypeSelect',
  props: {
    defaultType: Object
  },
  data() {
    return {
      selectType: { label: '本年', value: 'year' },
      showTypePopover: false,
      showCustomContent: false, // 展示自定义时间内容
      sureCustomContent: false, // 确定

      startTime: '',
      endTime: '',
      typeOptions: [
        { label: '今天', value: 'today' },
        { label: '昨天', value: 'yesterday' },
        { label: '本周', value: 'week' },
        { label: '上周', value: 'lastWeek' },
        { label: '本月', value: 'month' },
        { label: '上月', value: 'lastMonth' },
        { label: '本季度', value: 'quarter' },
        { label: '上季度', value: 'lastQuarter' },
        { label: '本年', value: 'year' },
        { label: '去年', value: 'lastYear' }
      ]
    }
  }, // 时间类型选择
  computed: {
    iconClass() {
      return this.showTypePopover ? 'arrow-up' : 'arrow-down'
    },
    typeShowValue() {
      if (this.sureCustomContent) {
        if (this.startTime || this.endTime) {
          return (this.startTime || '') + '-' + (this.endTime || '')
        }
        return ''
      } else {
        return this.selectType.label
      }
    }
  },
  mounted() {
    if (this.defaultType) {
      this.selectType = this.defaultType
    } else {
      this.$emit('change', { type: 'default', value: this.selectType.value })
    }
  },
  methods: {
    // 类型选择
    typeSelectClick(item) {
      this.showTypePopover = false
      this.sureCustomContent = false
      this.showCustomContent = false
      this.selectType = item
      this.$emit('change', { type: 'default', value: this.selectType.value })
    },
    // 选择自定义时间 确定
    customSureClick() {
      if (this.startTime && this.endTime) {
        this.sureCustomContent = true
        this.showTypePopover = false

        this.$emit('change', {
          type: 'custom',
          startTime: this.startTime,
          endTime: this.endTime
        })
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.type-select {
  width: 200px;
  margin-right: 15px;
}

// 时间选择
.type-popper {
  .type-content {
    height: 250px;
    overflow-y: auto;
    .type-content-item {
      height: 34px;
      line-height: 34px;
      padding: 0 20px;
      color: #606266;
      position: relative;
      cursor: pointer;
      .mark {
        display: inline-block;
        width: 8px;
        height: 8px;
        border-radius: 4px;
        margin-right: 5px;
        background-color: transparent;
      }
    }

    .selected {
      color: #409eff;
      font-weight: 700;
      .mark {
        background-color: #409eff;
      }
    }
    .type-content-item:hover {
      background-color: #f5f7fa;
    }
  }

  .type-content-custom {
    padding: 5px 20px 10px;
    position: relative;
    overflow: hidden;
    .el-date-editor {
      width: 100%;
      margin-bottom: 8px;
    }

    button {
      float: right;
    }
  }
}
</style>
