<template>
  <div class="department">
    <el-collapse v-model="activeNames"
                 @change="handleChange">
      <el-collapse-item :name="index"
                        v-for="(item, index) in depData"
                        :key="index">
        <template slot="title">
          <span class="title">{{item.name}} （{{item.userList.length}}人）</span>
        </template>
        <div v-for="(k, j) in item.userList"
             :key="j"
             class="k-list">
          <div class="list-right">
            <div class="k-realname">{{k.realname.substring(0, 6)}}</div>
            <div class="content">
              <div v-if="k.deptName">
                <img src="@/assets/img/structure.png"
                     alt="">
                <span>{{k.deptName}}</span>
              </div>
              <div v-if="k.postName">
                <img src="@/assets/img/post.png"
                     alt="">
                <span>{{k.postName}}</span>
              </div>
              <div v-if="k.mobile">
                <img src="@/assets/img/mobile.png"
                     alt="">
                <span>{{k.mobile}}</span>
              </div>
            </div>
          </div>
        </div>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script>
export default {
  data() {
    return {
      activeNames: []
    }
  },
  props: {
    depData: Array
  },
  mounted() {},
  methods: {
    handleChange(val, key) {}
  }
}
</script>

<style scoped lang="scss">
.department {
  padding: 0 30px;
  flex: 1;
  overflow: auto;
  margin-bottom: 20px;
  .el-collapse /deep/ {
    border: 0;
    .el-collapse-item__wrap {
      border: 0;
      margin-top: 10px;
      .el-collapse-item__content {
        padding-bottom: 0;
      }
    }
    .el-collapse-item__header {
      position: relative;
      .title {
        padding-left: 25px;
        color: #999;
      }
      .el-collapse-item__arrow {
        position: absolute;
        left: 0;
        color: #999;
      }
    }
    .el-collapse-item__header.is-active {
      border-color: #ebeef5;
    }
  }
  .staff-data {
    margin-top: 20px;
  }
  .item-title {
    border-bottom: 1px solid #eee;
    padding-bottom: 10px;
    color: #777;
    font-size: 12px;
  }
}
</style>
