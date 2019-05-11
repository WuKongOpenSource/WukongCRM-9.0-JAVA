<template>
  <div class="error-container">
    <div class="errPage-container">
      <el-button icon="arrow-left"
                 class="pan-back-btn"
                 @click="back">返回</el-button>
      <el-row>
        <el-col :span="12">
          <h1 class="text-jumbo text-ginormous">Oops!</h1>
          <h2>你没有权限去该页面</h2>
          <h6>如有不满请联系你领导</h6>
          <ul class="list-unstyled">
            <li>或者你可以去:</li>
            <li class="link-type">
              <router-link to="workbench/index">回首页</router-link>
            </li>
            <li><a href="#"
                 @click.prevent="dialogVisible=true">点我看图</a></li>
          </ul>
        </el-col>
        <el-col :span="12">
          <img src="@/assets/401_images/401.gif"
               width="313"
               height="428"
               alt="Girl has dropped her ice cream.">
        </el-col>
      </el-row>
      <el-dialog :visible.sync="dialogVisible"
                 title="随便看">
        <img src="@/assets/401_images/1.gif"
             class="pan-img">
      </el-dialog>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Page401',
  data() {
    return {
      dialogVisible: false
    }
  },
  methods: {
    back() {
      this.$router.go(-1)
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.error-container {
  width: 100%;
  height: 100%;
  min-height: 560px;
  background: #fff;
  .errPage-container {
    // transform: translate(-40%,-30%);
    width: 800px;
    max-width: 100%;
    margin: 0 auto;
    background: #fff;
    padding-top: 100px;
    .pan-back-btn {
      background: #008489;
      color: #fff;
      border: none !important;
    }
    .pan-gif {
      margin: 0 auto;
      display: block;
    }
    .pan-img {
      display: block;
      margin: 0 auto;
      width: 100%;
    }
    .text-jumbo {
      font-size: 60px;
      font-weight: 700;
      color: #484848;
    }
    .list-unstyled {
      font-size: 14px;
      li {
        padding-bottom: 5px;
      }
      a {
        color: #008489;
        text-decoration: none;
        &:hover {
          text-decoration: underline;
        }
      }
    }
    h2 {
      font-size: 1.5em;
      margin-block-start: 0.83em;
      margin-block-end: 0.83em;
      margin-inline-start: 0px;
      margin-inline-end: 0px;
      font-weight: bold;
    }
    h6 {
      display: block;
      font-size: 0.67em;
      margin-block-start: 2.33em;
      margin-block-end: 2.33em;
      margin-inline-start: 0px;
      margin-inline-end: 0px;
      font-weight: bold;
    }
    li {
      list-style: unset;
    }
    ul {
      padding-inline-start: 40px;
    }
  }
}
</style>
