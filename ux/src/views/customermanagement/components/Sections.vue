<template>
  <div class="section">
    <div class="section-header"
         v-if="title && title.length > 0">
      <div class="section-mark"
           :style="{ 'border-left-color': mColor }"></div>
      <div class="section-title">{{title}}</div>
      <flexbox v-if="showF"
               class="f-container">
        <div v-for="(item, index) in fItems"
             :key="index"
             @click="fClick(item)"
             class="f-item"
             :class="{ 'f-item-select':fIndex==item.type }">{{item.title}}</div>
      </flexbox>
    </div>
    <div class="content"
         :style="{ 'height': contentHeight }">
      <div v-if="showNoData"
           class="no-data-container">
        <img class="no-data"
             src="@/assets/img/no_data.png" />
        <div class="no-data-name">暂无数据</div>
      </div>
      <slot></slot>
    </div>
  </div>
</template>
<script type="text/javascript">
export default {
  name: 'sections',
  components: {},
  computed: {},
  data() {
    return {
      fIndex: -1
    }
  },
  props: {
    title: {
      type: String,
      default: ''
    },
    mColor: {
      type: String,
      default: '#FF6767'
    },
    /** 内容区域固定高度 */
    contentHeight: {
      type: String,
      default: '327px'
    },
    /** 展示客户工作台  重要提醒 时间筛选 */
    showF: {
      type: Boolean,
      default: false
    },
    fItems: {
      type: Array,
      default: () => {
        return []
      }
    },
    /** 展示无数据 */
    showNoData: {
      type: Boolean,
      default: false
    }
  },
  mounted() {},
  methods: {
    fClick(item) {
      this.fIndex = item.type
      this.$emit('f-click', 'val')
    }
  }
}
</script>
<style lang="scss" scoped>
.section {
  position: relative;
  background-color: white;
  margin-top: 8px;
}
.section:first-child {
  margin-top: 0;
}

.section-mark {
  border-left-width: 3px;
  border-left-style: solid;
  height: 10px;
}

.section-header {
  display: flex;
  align-items: center;
  padding: 5px 15px;
}
.section-title {
  font-size: 13px;
  color: #333;
  margin-left: 8px;
  flex-shrink: 0;
}

.f-container {
  width: auto;
  margin-left: 40px;
  .f-item {
    padding: 2px 4px;
    font-size: 12px;
    margin-right: 10px;
    color: #666;
  }
  .f-item-select {
    border-radius: 2px;
    background-color: #ff6767;
    color: white;
  }
}

.content {
  overflow: auto;
  .no-data-container {
    text-align: center;
    .no-data {
      margin-top: 30px;
    }
    .no-data-name {
      font-size: 12px;
      margin-top: 8px;
      color: #666;
    }
  }
}
</style>
