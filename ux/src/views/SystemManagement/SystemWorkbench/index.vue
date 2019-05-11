<template>
  <flexbox style="height:100%;"
           direction="column"
           align="stretch">
    <div class="sw-header">
      工作台
    </div>
    <div class="sw-body">
      <flexbox align="stretch"
               class="sw-box">
        <div class="sw-body-side">
          <div v-for="(item, index) in leftSides"
               :key="index"
               :class="leftType==item.type? 'side-item-select' : 'side-item-default'"
               @click="sideClick(item)"
               class="side-item">
            {{item.name}}
          </div>
        </div>
        <div class="sw-body-content">
          <component :is="leftType"></component>
        </div>
      </flexbox>
    </div>
  </flexbox>
</template>

<script>
import ExamineManager from './components/ExamineManager'

export default {
  components: {
    ExamineManager
  },
  data() {
    return {
      leftType: 'ExamineManager',
      leftSides: [
        {
          name: '审批类型管理',
          type: 'ExamineManager'
        }
      ]
    }
  },
  methods: {},
  mounted() {}
}
</script>

<style lang="scss" scoped>
.sw-header {
  height: 60px;
  font-size: 18px;
  padding: 0 20px;
  line-height: 60px;
}
.sw-body {
  position: relative;
  flex: 1;
}
.sw-box {
  position: relative;
  height: 100%;
}

.sw-body-side {
  padding-top: 20px;
  width: 200px;
  font-size: 14px;
  background-color: white;
  margin-right: 10px;
  flex-shrink: 0;
  border: 1px solid #e6e6e6;
  border-radius: 2px;
  .side-item {
    height: 50px;
    line-height: 50px;
    padding: 0 20px;
    font-size: 13px;
    cursor: pointer;
  }
}
.sw-body-content {
  flex: 1;
  overflow-x: auto;
  background-color: white;
  border: 1px solid #e6e6e6;
  border-radius: 2px;
}

.side-item-default {
  color: #333;
  border-right: 2px solid transparent;
}

.side-item-select {
  color: #409eff;
  border-right: 2px solid #46cdcf;
  background-color: #ecf5ff;
}
</style>

