<template>
  <create-view :body-style="{ height: '100%'}" id="duplicate-check-container">
    <div class="container">
      <div class="header">
        查重工具
        <img @click="hidenView"
             class="close"
             src="@/assets/img/task_close.png" />
      </div>
      <el-tabs v-model="tabActiveName"
               class="content">
        <el-tab-pane v-for="(item, index) in tabList"
                     :key="index"
                     :label="item.label"
                     :name="item.name">
          <check-content :type="item.name"></check-content>
        </el-tab-pane>
      </el-tabs>
    </div>
  </create-view>
</template>
<script type="text/javascript">
import CreateView from '@/components/CreateView'
import CheckContent from './checkContent'

export default {
  name: 'duplicate-check', // 验重页面
  components: {
    CreateView,
    CheckContent
  },
  computed: {},
  watch: {},
  data() {
    return {
      tabActiveName: 'customer',
      tabList: [
        { label: '客户', name: 'customer' },
        { label: '联系人', name: 'contacts' },
        { label: '线索', name: 'leads' }
      ]
    }
  },
  props: {},
  mounted() {
    // 获取title展示名称
    document.body.appendChild(this.$el)
  },
  methods: {
    /**
     * 关闭窗口
     */
    hidenView() {
      this.$emit('hiden-view')
    }
  },
  destroyed() {
    // remove DOM node after destroy
    if (this.$el && this.$el.parentNode) {
      this.$el.parentNode.removeChild(this.$el)
    }
  }
}
</script>
<style lang="scss" scoped>
.container {
  overflow: hidden;
  height: 100%;

  .header {
    padding: 0 10px;
    height: 40px;
    line-height: 40px;
    font-size: 17px;
    color: #333;
    position: relative;

    .close {
      float: right;
      display: block;
      width: 40px;
      height: 40px;
      margin-right: -10px;
      padding: 10px;
      cursor: pointer;
    }
  }

  .content {
    height: calc(100% - 40px);
    position: relative;
    overflow: hidden;

    /deep/ .el-tabs__header {
      padding: 10px 20px 0;
      margin-bottom: 0;

      .el-tabs__nav-wrap::after {
        height: 1px;
      }
    }

    /deep/ .el-tabs__content {
      overflow: hidden;
      height: calc(100% - 50px);

      .el-tab-pane {
        overflow: hidden;
        height: 100%;
      }
    }
  }
}
</style>
