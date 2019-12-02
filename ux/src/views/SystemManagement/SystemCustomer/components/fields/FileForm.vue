<template>
  <div class="box-wrapper">
    <div class="title">
      <span>{{ attr.isNull ? '*' : '' }}</span>{{ attr.name }}<span v-if="attr.inputTips">{{ '（'+attr.inputTips+'）' }}</span>
    </div>
    <div class="box">
      请选择文件
    </div>
    <span
      v-if="isShow"
      class="el-icon-delete control"
      @click="handleDelete"/>
  </div>
</template>

<script>
/**
 * 文件
 */
import mixins from './mixin'

export default {
  name: 'FileForm',
  mixins: [mixins],
  data() {
    return {}
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/mixin.scss';
@import 'form.scss';

.box {
  width: 100px;
  font-size: 14px;
  padding: 10px 0;
  border: 1px solid #e1e1e1;
  border-radius: 3px;
  background: white;
  text-align: center;
}
</style>
