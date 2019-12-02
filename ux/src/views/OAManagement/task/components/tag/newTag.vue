<template>
  <div class="new-tag-dialog">
    <div class="title">
      <span
        class="el-icon-arrow-left"
        @click="back"/>
      <span>{{ newTagTitle }}</span>
      <span
        class="el-icon-close rt"
        @click="close"/>
    </div>
    <div class="search">
      <el-input
        v-model="input"
        :maxlength="10"
        size="mini"
        placeholder="输入标签名，最多十个字符"/>
      <span
        :style="{'background': showBgColor}"
        class="checked-color"/>
    </div>
    <div class="color-box">
      <span
        v-for="(item, index) in colorList"
        :key="index"
        :style="{'background': item}"
        @click="changeColor(item)"/>
    </div>
    <div class="footer">
      <el-button
        type="primary"
        size="medium"
        @click="tagCreateSubmit">保存</el-button>
      <el-button
        size="medium"
        @click="tagCancel">取消</el-button>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    newTagTitle: String,
    newTagInput: String,
    bgColorProps: String
  },
  data() {
    return {
      colorList: [
        '#53D397',
        '#20C1BD',
        '#58DADA',
        '#0FC9E7',
        '#3498DB',
        '#4586FF',
        '#8983F3',
        '#AEA1EA',
        '#FF6699',
        '#F24D70',
        '#FF6F6F'
      ],
      input: this.newTagInput,
      showBgColor: ''
    }
  },
  watch: {
    bgColorProps: function(color) {
      this.showBgColor = color || '#53D397'
    }
  },
  mounted() {
    this.showBgColor = this.bgColorProps || '#53D397'
  },
  methods: {
    back() {
      this.$emit('back')
    },
    close() {
      this.$emit('close')
    },
    // 点击变色
    changeColor(val) {
      this.$emit('changeColor', val)
    },
    // 创建按钮
    tagCreateSubmit() {
      this.$emit('tagCreateSubmit', this.input, this.showBgColor)
    },
    // 取消按钮
    tagCancel() {
      this.$emit('tagCancel')
    }
  }
}
</script>


<style scoped lang="scss">
.new-tag-dialog {
  overflow: hidden;
  // position: absolute;
  // top: 0;
  // left: -110px;
  // bottom: 0;
  // right: 0;
  background: #fff;
  min-height: 200px;
  // box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
  margin: 0 -12px -12px;
  .title {
    padding: 10px;
    border-bottom: 1px solid#E6E6E6;
    margin-bottom: 20px;
    .el-icon-close {
      margin-right: 0;
      cursor: pointer;
    }
    .el-icon-arrow-left {
      cursor: pointer;
    }
  }
  .search {
    position: relative;
    .el-input {
      width: 90%;
      margin: 0 5%;
    }
    .el-input /deep/ .el-input__inner {
      padding-left: 40px;
    }
    .checked-color {
      position: absolute;
      left: 5%;
      top: 0;
      display: inline-block;
      width: 20px;
      height: 20px;
      border-radius: 50%;
      margin: 4px 10px;
    }
  }
  .footer {
    text-align: right;
    margin: 20px;
  }
  .color-box {
    margin: 10px 5%;
    span {
      display: inline-block;
      width: 20px;
      height: 20px;
      margin-right: 5px;
      border-radius: 50%;
      cursor: pointer;
    }
  }
}
</style>
