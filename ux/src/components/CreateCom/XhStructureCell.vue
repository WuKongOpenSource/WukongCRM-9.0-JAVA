<template>
  <el-popover
    :disabled="disabled"
    placement="bottom"
    width="300"
    trigger="click">
    <xh-structure
      v-if="!disabled && showSelectView"
      ref="structure"
      :radio="radio"
      :selected-data="dataValue"
      @changeCheckout="checkStructure"/>
    <div slot="reference">
      <flexbox
        :class="[disabled ? 'is_disabled' : 'is_valid']"
        wrap="wrap"
        class="structure-container"
        @click.native="focusClick">
        <div
          v-for="(item, index) in dataValue"
          :key="index"
          class="user-item"
          @click.stop="deletestru(item,index)">{{ item.name }}
          <i class="delete-icon el-icon-close"/>
        </div>
        <div class="add-item">+添加部门</div>
      </flexbox>
    </div>
  </el-popover>
</template>
<script type="text/javascript">
import XhStructure from './XhStructure'
import arrayMixin from './arrayMixin'

export default {
  name: 'XhStructureCell', // 新建 structure-cell
  components: {
    XhStructure
  },
  mixins: [arrayMixin],
  props: {
    radio: {
      // 是否单选
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      showPopover: false, // 展示popover
      showSelectView: false // 展示选择内容列表
    }
  },
  computed: {},
  watch: {},
  mounted() {},
  methods: {
    /** 选中 */
    checkStructure(data) {
      this.dataValue = data.data
      this.$emit('value-change', {
        index: this.index,
        value: data.data
      })
    },
    /** 删除 */
    deletestru(item, index) {
      this.dataValue.splice(index, 1)
      this.$refs.structure.parentRemoveCheck({
        data: this.dataValue,
        index: index
      })
      this.$emit('value-change', {
        index: this.index,
        value: this.dataValue
      })
    },
    /** 聚焦动作 */
    focusClick() {
      this.showSelectView = true
      this.$emit('focus')
    }
  }
}
</script>
<style lang="scss" scoped>
.structure-container {
  min-height: 34px;
  margin: 3px 0;
  position: relative;
  border-radius: 3px;
  font-size: 12px;
  border: 1px solid #ddd;
  color: #333333;
  padding: 0.5px;
  line-height: 15px;
  max-height: 105px;
  overflow-y: auto;
  .user-item {
    padding: 5px;
    background-color: #e2ebf9;
    border-radius: 3px;
    margin: 3px;
    cursor: pointer;
  }
  .add-item {
    padding: 5px;
    color: #3e84e9;
    cursor: pointer;
  }
  .delete-icon {
    color: #999;
    cursor: pointer;
  }
  &:hover {
    border-color: #c0c4cc;
  }
}

.structure-container.is_disabled {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  cursor: not-allowed;
  .user-item {
    background-color: #f0f4f8ea;
    color: #c0c4cc;
    cursor: not-allowed;
  }
  .delete-icon {
    color: #c0c4cc;
    cursor: not-allowed;
  }
  .add-item {
    color: #c0c4cc;
    cursor: not-allowed;
  }
}

.structure-container.is_valid:hover {
  border-color: #c0c4cc;
}
</style>
