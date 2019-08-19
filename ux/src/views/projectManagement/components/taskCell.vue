<template>
  <div class="list"
       :style="{'border-left-color': data.priority == 1 ? '#8bb5f0' : data.priority == 2 ? '#FF9668' : data.priority == 3 ? '#ED6363': 'transparent'}"
       ref="taskRow"
       @click="rowFun(data)">
    <div class="list-left"
         ref="listLeft">
      <div :class="data.checked ? 'title title-active' : 'title'"
           @click.stop>
        <el-checkbox v-model="data.checked"
                     @change="taskOverClick(data)"></el-checkbox>
      </div>
      <el-tooltip placement="bottom"
                  effect="light"
                  popper-class="task-tooltip tooltip-change-border">
        <div slot="content">
          <span>{{data.name}}</span>
        </div>
        <span ref="itemSpan"
              :class="data.checked ? 'item-name-active' : 'item-name'">
          {{data.name}}
        </span>
      </el-tooltip>
    </div>
    <div class="list-right">
      <div class="tag-box"
           v-if="data.labelList">
        <template v-if="data.labelList.length <= 2">
          <div v-for="(k, j) in data.labelList"
               :key="j"
               class="item-label">
            <span class="k-name"
                  :style="{'background': k.color}">{{k.labelName}}</span>
          </div>
        </template>
        <template v-else>
          <span class="k-name"
                :style="{'background': data.labelList[0].color}">{{data.labelList[0].labelName}}</span>
          <span class="k-name"
                :style="{'background': data.labelList[1].color}">{{data.labelList[1].labelName}}</span>
          <el-tooltip placement="top"
                      effect="light"
                      popper-class="tooltip-change-border task-tooltip">
            <div slot="content"
                 class="tooltip-content"
                 style="margin: 10px 10px 10px 0;">
              <div v-for="(k, j) in data.labelList"
                   :key="j"
                   class="item-label"
                   style="display: inline-block; margin-right: 10px;">
                <span v-if="j >= 2"
                      class="k-name"
                      :style="{'background': k.color ? k.color: '#ccc'}"
                      style="border-radius: 3px; color: #FFF; padding: 3px 10px;">{{k.labelName}}</span>
              </div>
            </div>
            <span class="color-label-more">
              <i>...</i>
            </span>
          </el-tooltip>
        </template>
      </div>
      <div class="img-group">
        <div class="img-box"
             v-if="data.relationCount">
          <i class="wukong wukong-relevance"></i>
          <span>{{data.relationCount}}</span>
        </div>
        <div class="img-box"
             v-if="data.childAllCount > 0">
          <i class="wukong wukong-sub-task"></i>
          <span>{{data.childWCCount}}/{{data.childAllCount}}</span>
        </div>
        <div class="img-box"
             v-if="data.fileCount">
          <i class="wukong wukong-file"></i>
          <span>{{data.fileCount}}</span>
        </div>
        <div class="img-box"
             v-if="data.commentCount">
          <i class="wukong wukong-comment-task"></i>
          <span>{{data.commentCount}}</span>
        </div>
        <div class="img-box"
             v-if="data.stopTime">
          <i class="wukong wukong-time-task"
             :style="{'color': data.isEnd == 1 && !data.checked ? 'red': '#999'}"></i>
          <span :style="{'color': data.isEnd == 1 && !data.checked ? 'red': '#999'}">{{new Date(data.stopTime).getTime() | moment("MM-DD")}} 截止</span>
        </div>
      </div>
      <div class="item-own-box">
        <el-tooltip placement="bottom"
                    effect="light"
                    popper-class="tooltip-change-border"
                    v-if="data.mainUser && data.mainUser.id">
          <div slot="content">
            <span>{{data.mainUser.realname}}</span>
          </div>
          <div v-photo="data.mainUser"
               v-lazy:background-image="$options.filters.filterUserLazyImg(data.mainUser.img)"
               :key="data.mainUser.img"
               class="div-photo"></div>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>
<script type="text/javascript">
import { workTaskSaveAPI } from '@/api/projectManagement/task'

export default {
  name: 'task-cell', // 任务cell

  components: {},

  data() {
    return {}
  },

  props: {
    data: Object,
    dataIndex: Number,
    dataSection: Number,
  },

  mounted() {},

  methods: {
    /**
     * 列表标记任务
     */
    taskOverClick(val) {
      workTaskSaveAPI({
        taskId: val.taskId,
        status: val.checked ? 5 : 1
      })
        .then(res => {})
        .catch(err => {
          val.checked = false
        })
    },

    /**
     * 点击显示详情
     */
    rowFun(val) {
      this.$emit('on-handle', {
        type: 'view',
        data: { item: this.data, index: this.dataIndex, section: this.dataSection }
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.list {
  background-color: white;
  padding: 0 10px;
  cursor: pointer;
  border-bottom: 1px solid #e6e6e6;
  border-left: 2px solid transparent;
  border-radius: 2px;
  height: 51px;
  line-height: 51px;
  display: flex;
  .header {
    margin-bottom: 15px;
    img {
      width: 32px;
      margin-right: 14px;
      vertical-align: middle;
    }
    .name-time {
      display: inline-block;
      vertical-align: middle;
      .time {
        color: #999;
        margin-top: 5px;
        font-size: 12px;
      }
    }
  }
  .title {
    cursor: pointer;
    color: #333;
    display: inline-block;
    .el-checkbox {
      padding-right: 7px;
    }
  }
  .title-active {
    color: #666;
    text-decoration: line-through;
    text-decoration-color: #aaa;
  }
  .img-group {
    color: #999;
    font-size: 12px;
    vertical-align: middle;
    display: inline-block;
    .img-box {
      display: inline-block;
      margin-right: 6px;
      img {
        vertical-align: middle;
        width: 16px;
        height: 16px;
      }
      span {
        vertical-align: middle;
      }
      .priority-btn {
        width: 68px;
        font-size: 12px;
        display: inline-block;
        text-align: center;
        border-radius: 10px;
        color: #fff;
        height: 16px;
        line-height: 16px;
      }
    }
  }
  .item-name-active {
    color: #8f8f8f;
    text-decoration: line-through;
  }
  .list-left,
  .list-right {
    display: inline-block;
  }
  .list-left {
    flex: 1;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    padding-right: 10px;
  }
  .list-right {
    float: right;
    .item-own-box {
      display: inline-block;
      vertical-align: middle;
      .div-photo {
        vertical-align: middle;
        width: 24px;
        height: 24px;
        border-radius: 12px;
        margin-left: 12px;
      }
    }
    .tag-box {
      display: inline-block;
      .item-label {
        display: inline-block;
      }
      .k-name {
        display: inline-block;
        height: 20px;
        line-height: 20px;
        padding: 0 10px;
        border-radius: 3px;
        color: #fff;
        margin-right: 6px;
        font-size: 12px;
      }
    }
    .tag-box /deep/ .color-label-more {
      background: #eee;
      width: 34px;
      height: 20px;
      line-height: 20px;
      text-align: center;
      display: inline-block;
      font-size: inherit;
      font-weight: 700;
      border-radius: 3px;
      vertical-align: middle;
      position: relative;
      i {
        position: absolute;
        left: 50%;
        line-height: 36px;
        top: 0%;
        height: 20px;
        transform: translate(-50%, -50%);
      }
    }
  }
}
.list:hover {
  background: #fafafa;
}
.wukong {
  font-size: 14px;
}
</style>
