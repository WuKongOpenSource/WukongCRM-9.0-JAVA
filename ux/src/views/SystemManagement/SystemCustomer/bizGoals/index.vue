<template>
  <div class="system-customer">
    <div class="title">业绩目标设置</div>
    <div class="customer-content">
      <!-- 业绩目标设置 -->
      <div class="system-view-table">
        <task-set-statistics/>
      </div>
    </div>
  </div>
</template>

<script>
import TaskSetStatistics from './components/TaskSetStatistics' // 业绩目标设置

export default {
  name: 'BizGoals', // 业绩目标设置

  components: {
    TaskSetStatistics
  },

  data() {
    return {
    }
  },

  created() {},

  methods: {}
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.system-customer {
  /* padding: 0 20px 20px; */
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}
.title {
  font-size: 18px;
  height: 40px;
  padding: 0 20px;
  line-height: 40px;
  margin: 10px 0;
  color: #333;
}
.customer-content {
  flex: 1;
  position: relative;
  display: flex;
  overflow: hidden;
}

.system-view-table {
  flex: 1;
  border: 1px solid #e6e6e6;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow-x: auto;
}
</style>
