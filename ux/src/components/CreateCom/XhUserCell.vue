<template>
  <el-popover
    :disabled="disabled"
    placement="bottom"
    width="300"
    trigger="click">
    <xh-user
      v-if="!disabled&&showSelectView"
      ref="xhuser"
      :info-type="infoType"
      :info-params="infoParams"
      :radio="radio"
      :selected-data="dataValue"
      @changeCheckout="checkUsers"/>
    <div slot="reference">
      <flexbox
        :class="[disabled ? 'is_disabled' : 'is_valid']"
        wrap="wrap"
        class="user-container"
        @click.native="focusClick">
        <div
          v-for="(item, index) in dataValue"
          :key="index"
          class="user-item"
          @click.stop="deleteuser(index)">{{ item.realname }}
          <i class="delete-icon el-icon-close"/>
        </div>
        <div class="add-item">+{{ placeholder }}</div>
      </flexbox>
    </div>
  </el-popover>

</template>
<script type="text/javascript">
import XhUser from './XhUser'
import arrayMixin from './arrayMixin'

export default {
  name: 'XhUserCell', // 新建 user-cell
  components: {
    XhUser
  },
  mixins: [arrayMixin],
  props: {
    radio: {
      // 是否单选
      type: Boolean,
      default: true
    },
    placeholder: {
      type: String,
      default: '添加员工'
    },
    /** 获取不同的员工展示列表 */
    infoType: {
      type: String,
      default: 'default' // 返回全部  crm_contract crm_receivables oa_examine 合同审核人自选列表
    },
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
      showPopover: false, // 展示popover
      showSelectView: false // 展示选择内容列表
    }
  },
  computed: {},
  watch: {},
  mounted() {},
  methods: {
    /** 选中 */
    checkUsers(data) {
      this.dataValue = data.data
      this.$emit('value-change', {
        item: this.item,
        index: this.index,
        value: data.data
      })
    },
    /** 删除 */
    deleteuser(index) {
      if (this.$refs.xhuser) {
        this.$refs.xhuser.cancelCheckItem(this.dataValue[index])
      }
      this.dataValue.splice(index, 1)
      this.$emit('value-change', {
        item: this.item,
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
.user-container {
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

.user-container.is_disabled {
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

.user-container.is_valid:hover {
  border-color: #c0c4cc;
}
</style>
