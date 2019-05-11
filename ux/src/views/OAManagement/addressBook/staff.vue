<template>
  <div class="staff">
    <div v-for="(item, index) in staffData"
         :key="index"
         class="staff-data">
      <div class="item-title">{{item.letter}}</div>
      <div v-for="(k, j) in item.list"
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
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {}
  },
  props: {
    staffData: Array
  },
  created() {},
  methods: {
    handleClick() {}
  }
}
</script>

<style scoped lang="scss">
.staff {
  padding: 0 30px;
  flex: 1;
  overflow: auto;
  margin-bottom: 20px;
  .staff-data {
    margin-top: 15px;
  }
  .item-title {
    border-bottom: 1px solid #eee;
    padding-bottom: 10px;
    color: #777;
    margin-bottom: 13px;
    font-size: 12px;
  }
  .k-realname {
    margin-bottom: 8px;
  }
}
</style>
