<template>
  <div class="box-wrapper">
    <div class="title"><span>{{attr.isNull ? '*' : ''}}</span>{{attr.name}}<span v-if="attr.inputTips">{{'（'+attr.inputTips+'）'}}</span></div>
    <div class="box">
      <el-checkbox-group v-model="attr.defaultValue"
                         :disabled="disabled">
        <el-checkbox class="checkbox"
                     v-for="(item, index) in attr.showSetting"
                     :key="index"
                     :label="item.value"></el-checkbox>
      </el-checkbox-group>
    </div>
    <span class="el-icon-delete control"
          @click="handleDelete"
          v-if="isShow"></span>
  </div>
</template>

<script>
/**
 * 选项
 */
import mixins from './mixin'

export default {
  name: 'CheckboxForm',
  mixins: [mixins],
  computed: {
    /** 只读 */
    disabled() {
      // operating 0 改删 1改 2删 3无
      if (this.attr.operating == 2 || this.attr.operating == 3) {
        return true
      }
      return false
    }
  },
  mounted() {
  },
  data() {
    return {}
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/mixin.scss';
@import 'form.scss';

.box {
  font-size: 13px;
  .checkbox {
    display: block;
    margin-left: 0;
    margin-top: 5px;
  }
}

.el-checkbox /deep/ .el-checkbox__label {
  font-size: 13px;
  color: #333333;
}
</style>
