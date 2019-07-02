<template>
  <div class="v-task">
    <div class="title">任务</div>
    <div class="content-box"
         v-loading="loading"
         :style="{'padding-right': list.length > 4 ? '7px' : '0'}">
      <template v-if="list.length != 0">
        <div class="content"
             ref="taskRow"
             v-for="(item, index) in list"
             :key="index"
             @click="checkRowDetail(item, index)">
          <div class="name-time">
            <p class="task-name"
               @click.stop>
              <el-checkbox v-model="item.checked"
                           :disabled="item.checked"
                           @change="taskOverFun(item, index)"></el-checkbox>
            </p>
            <span :class="item.lineThrough ? 'item-name item-name-active': 'item-name'">
              {{item.name}}
            </span>
            <p v-if="item.stop_time"
               class="time"
               :style="{color: item.task_status == 2 ? 'red' : '#999'}">
              <span class="el-icon-time"></span>
              <span>{{item.stop_time | moment("YYYY-MM-DD")}}</span>
            </p>
          </div>
          <div class="rt">
            <span class="type-color"
                  v-for="(k, j) in colorGroup"
                  :key="j"
                  v-if="item.priority == k.id"
                  :style="{background: k.color}">
              {{k.label}}
            </span>
          </div>
        </div>
      </template>
      <div class="no-task"
           v-else-if="list.length == 0 && loading == false">
        <img src="@/assets/img/no_task.png"
             alt="">
        <p>目前没有任务，快去<span class="add"
                @click="addTask">添加</span>吧！</p>
      </div>
    </div>
    <!-- 详情 -->
    <particulars v-if="taskDetailShow"
                 ref="particulars"
                 :id="taskID"
                 :detailIndex="detailIndex"
                 @on-handle="getList"
                 @close="closeBtn">
    </particulars>
  </div>
</template>

<script>
import listTaskDetail from '../task/mixins/listTaskDetail.js'
// API
import {
  detailsTask,
  readLoglist,
  editTask,
  deleteTask
} from '@/api/oamanagement/task'
import { taskListAPI } from '@/api/oamanagement/workbench'
// 详情
import particulars from '../task/components/particulars'
import { timestampToFormatTime } from '@/utils'

export default {
  components: {
    particulars
  },
  data() {
    return {
      // 加载中
      loading: false,
      list: [],
      colorGroup: [
        { id: null, label: '', color: '' },
        { id: 0, label: '无', color: '#ccc' },
        { id: 1, label: '低', color: '#8bb5f0' },
        { id: 2, label: '中', color: '#FF9668' },
        { id: 3, label: '高', color: '#ED6363' }
      ]
    }
  },
  mixins: [listTaskDetail],
  mounted() {
    document
      .getElementById('workbench-main-container')
      .addEventListener('click', this.subtasksSubmit, false)
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      taskListAPI()
        .then(res => {
          for (let item of res.data) {
            item.checked = false
          }
          this.list = res.data
          this.loading = false
        })
        .catch(err => {
          this.loading = false
        })
    },
    // 点击空白处关闭详情
    subtasksSubmit(e) {
      if (
        this.$refs.particulars &&
        !this.$refs.particulars.$el.contains(e.target)
      ) {
        this.taskDetailShow = false
      }
    },
    checkRowDetail(item, index) {
      this.showDetailView(item, index)
    },
    // 列表标记任务
    taskOverFun(item, index) {
      editTask({
        taskId: item.taskId,
        status: item.checked ? 5 : 1
      })
        .then(res => {
          if (item.checked) {
            this.list.splice(index, 1)
          }
          this.$store.dispatch('GetOAMessageNum', 'task')
        })
        .catch(err => {
          item.checked = !item.checked
        })
    },
    addTask() {
      this.$router.push({ path: 'task', query: { routerKey: 1 } })
    }
  }
}
</script>

<style scoped lang="scss">
.v-task {
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  .title {
    height: 44px;
    line-height: 44px;
    margin: 0 20px;
    border-bottom: 1px solid #e6e6e6;
  }
  .content-box {
    flex: 1;
    overflow: hidden;
    position: relative;
    margin-bottom: 10px;
    .content {
      margin: 21px;
      cursor: pointer;
      overflow: hidden;
      .name-time {
        // width: 240px;
        width: 89%;
        float: left;
        .task-name {
          margin-bottom: 8px;
          margin-right: 6px;
          display: inline-block;
        }
        .time {
          font-size: 12px;
          padding-left: 27px;
          .el-icon-time {
            font-weight: 600;
          }
        }
        .item-name {
          width: 80%;
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
          display: inline-block;
          vertical-align: middle;
        }
        .item-name-active {
          color: #666;
          text-decoration: line-through;
        }
      }
      .rt {
        margin-right: 0;
        float: left;
        .type-color {
          display: inline-block;
          text-align: center;
          width: 34px;
          height: 34px;
          line-height: 34px;
          border-radius: 50%;
          color: #fff;
          font-size: 12px;
        }
      }
    }
  }
  .content-box:hover {
    overflow: auto;
    padding-right: 0 !important;
  }
  .no-task {
    position: absolute;
    top: 50%;
    left: 50%;
    text-align: center;
    transform: translate(-50%, -50%);
    // margin-top: 10px;
    p {
      color: #777;
      margin-top: 10px;
    }
    .add {
      color: #3e84e9;
      cursor: pointer;
    }
  }
}
</style>
