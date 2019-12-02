<template>
  <div class="my-task">
    <slot name="searchInput"/>
    <div class="select-box">
      <!-- 筛选 -->
      <div
        v-if="listType == 'subtask'"
        class="select-group">
        <label>任务类型</label>
        <el-select
          v-model="fromData.type"
          placeholder="请选择"
          @change="selectChange">
          <el-option
            v-for="item in subordinateOption"
            :key="item.key"
            :label="item.label"
            :value="item.key"/>
        </el-select>
      </div>
      <div
        v-else
        class="select-group">
        <label>任务类型</label>
        <el-select
          v-model="fromData.type"
          placeholder="请选择"
          @change="selectChange">
          <el-option
            v-for="item in typeOptions"
            :key="item.key"
            :label="item.label"
            :value="item.key"/>
        </el-select>
      </div>
      <div class="select-group">
        <label>状态</label>
        <el-select
          v-model="fromData.status"
          placeholder="请选择"
          @change="selectChange">
          <el-option
            v-for="item in statusOptions"
            :key="item.key"
            :label="item.label"
            :value="item.key"/>
        </el-select>
      </div>
      <div class="select-group">
        <label>优先级</label>
        <el-select
          v-model="fromData.priority"
          placeholder="请选择"
          @change="selectChange">
          <el-option
            v-for="item in priorityOptions"
            :key="item.key"
            :label="item.label"
            :value="item.key"/>
        </el-select>
      </div>
      <div class="select-group">
        <label>截止时间</label>
        <el-select
          v-model="fromData.date"
          placeholder="请选择"
          @change="selectChange">
          <el-option
            v-for="item in timeOptions"
            :key="item.key"
            :label="item.label"
            :value="item.key"/>
        </el-select>
      </div>
      <div
        v-if="listType == 'subtask'"
        class="select-group">
        <label class="min-width">负责人</label>
        <el-select
          v-model="fromData.subUser"
          placeholder="请选择"
          @change="selectChange">
          <el-option
            v-for="item in subUserListData"
            :key="item.userId"
            :label="item.realname"
            :value="item.userId"/>
        </el-select>
      </div>
    </div>
    <div class="list-box-container">
      <div class="list-box">
        <task-cell
          v-for="(item, index) in list"
          :key="index"
          :data="item"
          :data-index="index"
          @on-handle="taskCellHandle"/>
      </div>
      <p class="load">
        <el-button
          :loading="loadMoreLoading"
          type="text">{{ loadMoreLoading ? '加载更多' : '没有更多了' }}</el-button>
      </p>
    </div>
    <!-- 详情 -->
    <particulars
      v-if="taskDetailShow"
      ref="particulars"
      :id="taskID"
      :detail-index="detailIndex"
      @on-handle="detailHandle"
      @close="closeBtn"/>
  </div>
</template>

<script>
import particulars from '../components/particulars'
import listTaskDetail from '../mixins/listTaskDetail.js'
import TaskCell from './taskCell'

export default {
  components: {
    TaskCell,
    particulars
  },
  mixins: [listTaskDetail],
  props: {
    listType: '',
    list: Array,
    // 负责人
    subUserListData: {
      type: Array,
      default: () => {
        return []
      }
    },
    loadMoreLoading: false
  },
  data() {
    return {
      fromData: {
        type: '0',
        status: '1',
        priority: '',
        date: '',
        subUser: ''
      },
      // 任务类型
      typeOptions: [
        { label: '全部', key: '0' },
        { label: '我负责的', key: '1' },
        { label: '我创建的', key: '2' },
        { label: '我参与的', key: '3' }
      ],
      // 下属任务类型
      subordinateOption: [
        { label: '全部', key: '0' },
        { label: '下属负责的', key: '1' },
        { label: '下属创建的', key: '2' },
        { label: '下属参与的', key: '3' }
      ],
      // 状态
      statusOptions: [
        { label: '全部', key: '' },
        { label: '正在进行', key: '1' },
        { label: '已完成', key: '5' }
      ],
      // 优先级
      priorityOptions: [
        { label: '全部', key: '' },
        { label: '高', key: '3' },
        { label: '中', key: '2' },
        { label: '低', key: '1' },
        { label: '无', key: '0' }
      ],
      // 截至时间
      timeOptions: [
        { label: '全部', key: '' },
        { label: '今天到期', key: '1' },
        { label: '明天到期', key: '2' },
        { label: '一周到期', key: '3' },
        { label: '一个月到期', key: '4' }
      ]
    }
  },
  mounted() {
    document
      .getElementById('workbench-main-container')
      .addEventListener('click', this.taskShowHandle, false)
  },
  methods: {
    selectChange(val, key) {
      this.$emit('selectChange', { type: this.listType, data: this.fromData })
    },
    // 点击空白处关闭详情
    taskShowHandle(e) {
      if (
        this.$refs.particulars &&
        !this.$refs.particulars.$el.contains(e.target)
      ) {
        let hidden = true
        const items = document.getElementsByClassName('list-box')
        for (let index = 0; index < items.length; index++) {
          const element = items[index]
          if (element.contains(e.target)) {
            hidden = false
            break
          }
        }
        this.taskDetailShow = !hidden
      }
    },
    taskCellHandle(data) {
      if (data.type == 'view') {
        this.showDetailView(data.data.item, data.data.index)
      }
    }
  }
}
</script>

<style scoped lang="scss">
.my-task {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: hidden;
  .wukong {
    vertical-align: middle;
  }
  .select-box {
    margin-top: 22px;
    margin-bottom: 20px;
    // margin-right: -33px;
    max-width: 855px;
    overflow: hidden;
    .select-group {
      margin-bottom: 12px;
      width: 190px;
      float: left;
      label {
        color: #999;
        margin-right: 10px;
      }
      .el-select /deep/ {
        width: 116px;
        .el-input__inner {
          height: 30px;
          line-height: 30px;
        }
      }
      .min-width {
        width: 56px;
        display: inline-block;
      }
    }
    .select-group:nth-child(2) {
      padding-left: 15px;
    }
    .select-submit {
      float: right;
      margin-right: 10px;
    }
  }
  .list-box-container {
    flex: 1;
    overflow-y: auto;
  }
}

.load {
  color: #999;
  font-size: 13px;
  margin: 0 auto 15px;
  text-align: center;
  .el-button,
  .el-button:focus {
    color: #ccc;
    cursor: auto;
  }
}
</style>
