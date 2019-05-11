<template>
  <div class="box-wrapper">
    <div class="title">
      <span>{{attr.isNull ? '*' : ''}}</span>{{attr.name}}<span v-if="attr.inputTips">{{'（'+attr.inputTips+'）'}}</span>
    </div>
    <div class="box">
      <div class="box-content">{{attr.defaultValue}}</div>
      <div class="max-tips">{{attr.defaultValue.length+'/'+attr.maxLength}}</div>
    </div>
    <span class="el-icon-delete control"
          @click="handleDelete"
          v-if="isShow"></span>

  </div>
</template>

<script>
/**
 * 多行文本
 */
import mixins from './mixin'

export default {
  name: 'MultiLineText',
  mixins: [mixins]
}
</script>

<style scoped lang="scss">
@import 'form.scss';

.box {
  width: 310px;
  height: 80px;
  position: relative;

  padding: 10px 10px 15px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 2px;
  color: #333;
  background: white;
  font-size: 14px;
  .box-content {
    height: 48px;
    overflow: hidden;
    word-break: break-all;
  }
  .max-tips {
    text-align: right;
    font-size: 12px;
    color: #999;
    overflow: hidden;
    word-break: break-all;
  }
}
</style>
