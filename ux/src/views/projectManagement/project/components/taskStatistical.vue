<template>
  <div
    v-loading="loading"
    class="statistical">
    <statistical-overview
      :data="detailData.taskStatistics"
      :list="detailData.ownerList"
      class="statistical-overview"/>
    <flexbox class="statistical-task">
      <statistical-task
        :list="detailData.classStatistics"
        class="statistical-task-item"
        type="task"
        title="任务列表"/>
      <statistical-task
        :list="detailData.labelStatistics"
        class="statistical-task-item"
        type="label"
        title="标签分析"/>
    </flexbox>

    <statistical-member
      :list="detailData.memberTaskStatistics"
      class="statistical-member"/>
  </div>
</template>

<script>
import StatisticalOverview from '@/views/projectManagement/components/statisticalOverview'
import StatisticalTask from '@/views/projectManagement/components/statisticalTask'
import StatisticalMember from '@/views/projectManagement/components/statisticalMember'
// API
import { workWorkStatisticAPI } from '@/api/projectManagement/statistics'

export default {
  components: {
    StatisticalOverview,
    StatisticalTask,
    StatisticalMember
  },

  props: {
    workId: [Number, String]
  },

  data() {
    return {
      loading: false,
      detailData: {},
      firstRequst: true
    }
  },

  watch: {
    workId() {
      this.getDetail(true)
    }
  },

  mounted() {},

  activated() {
    if (this.firstRequst) {
      this.firstRequst = false
      this.getDetail(true)
    } else {
      this.getDetail(false)
    }
  },

  methods: {
    /**
     * 获取详情
     */
    getDetail(loading) {
      this.loading = loading
      workWorkStatisticAPI({
        workId: this.workId
      })
        .then(res => {
          this.detailData = res.data
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.statistical {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  overflow-y: auto;
}

.statistical-overview {
  background-color: white;
  border-radius: 3px;
  border: 1px solid #e6e6e6;
  overflow: hidden;
  padding-top: 5px;
}

.statistical-task {
  margin-top: 10px;
  .section {
    margin-top: 0;
  }
  .statistical-task-item {
    background-color: white;
    border-radius: 3px;
    border: 1px solid #e6e6e6;
    overflow: hidden;
    padding-top: 5px;
    flex: 1;
  }

  .statistical-task-item:first-child {
    margin-right: 10px;
  }
}

.statistical-member {
  background-color: white;
  border-radius: 3px;
  border: 1px solid #e6e6e6;
  overflow: hidden;
  padding-top: 5px;
  margin-top: 10px;
}
</style>

