<template>
  <div
    ref="taskRow"
    :style="{'border-left-color': data.priority == 1 ? '#8bb5f0' : data.priority == 2 ? '#FF9668' : data.priority == 3 ? '#ED6363': 'transparent'}"
    class="list"
    @click="rowFun(data)">
    <div
      ref="listLeft"
      class="list-left">
      <div
        :class="data.checked ? 'title title-active' : 'title'"
        @click.stop>
        <el-checkbox
          v-model="data.checked"
          @change="taskOverClick(data)"/>
      </div>
      <el-tooltip
        placement="bottom"
        effect="light"
        popper-class="task-tooltip tooltip-change-border">
        <div slot="content">
          <span>{{ data.name }}</span>
        </div>
        <span
          ref="itemSpan"
          :class="data.checked ? 'item-name-active' : 'item-name'">
          {{ data.name }}
        </span>
      </el-tooltip>
    </div>
    <div class="list-right">
      <div
        v-if="data.labelList && data.labelList.length > 0"
        class="tag-box">
        <template v-if="data.labelList.length <= 2">
          <div
            v-for="(k, j) in data.labelList"
            :key="j"
            class="item-label">
            <span
              :style="{'background': k.color}"
              class="k-name">{{ k.labelName }}</span>
          </div>
        </template>
        <template v-else>
          <span
            :style="{'background': data.labelList[0].color}"
            class="k-name">{{ data.labelList[0].labelName }}</span>
          <span
            :style="{'background': data.labelList[1].color}"
            class="k-name">{{ data.labelList[1].labelName }}</span>
          <el-tooltip
            placement="top"
            effect="light"
            popper-class="tooltip-change-border task-tooltip">
            <div
              slot="content"
              class="tooltip-content"
              style="margin: 10px 10px 10px 0;">
              <div
                v-for="(k, j) in data.labelList"
                :key="j"
                class="item-label"
                style="display: inline-block; margin-right: 10px;">
                <span
                  v-if="j >= 2"
                  :style="{'background': k.color || '#ccc'}"
                  class="k-name"
                  style="border-radius: 3px; color: #FFF; padding: 3px 10px;">{{ k.labelName }}</span>
              </div>
            </div>
            <span class="color-label-more">
              <i>...</i>
            </span>
          </el-tooltip>
        </template>
      </div>
      <div class="img-group">
        <div
          v-if="data.relationCount"
          class="img-box">
          <i class="wukong wukong-relevance"/>
          <span>{{ data.relationCount }}</span>
        </div>
        <div
          v-if="data.childAllCount > 0"
          class="img-box">
          <i class="wukong wukong-sub-task"/>
          <span>{{ data.childWCCount }}/{{ data.childAllCount }}</span>
        </div>
        <div
          v-if="data.fileCount"
          class="img-box">
          <i class="wukong wukong-file"/>
          <span>{{ data.fileCount }}</span>
        </div>
        <div
          v-if="data.commentCount"
          class="img-box">
          <i class="wukong wukong-comment-task"/>
          <span>{{ data.commentCount }}</span>
        </div>
        <div
          v-if="data.stopTime"
          class="img-box">
          <i
            :style="{'color': data.isEnd == 1 && !data.checked ? 'red': '#999'}"
            class="wukong wukong-time-task"/>
          <span :style="{'color': data.isEnd == 1 && !data.checked ? 'red': '#999'}">{{ data.stopTime | moment("MM-DD") }} 截止</span>
        </div>
      </div>
      <div class="item-own-box">
        <el-tooltip
          v-if="data.mainUser && data.mainUser.id"
          placement="bottom"
          effect="light"
          popper-class="tooltip-change-border">
          <div slot="content">
            <span>{{ data.mainUser.realname }}</span>
          </div>
          <div
            v-photo="data.mainUser"
            v-lazy:background-image="$options.filters.filterUserLazyImg(data.mainUser.img)"
            class="div-photo"/>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>
<script type="text/javascript">
// API
import { editTask } from '@/api/oamanagement/task'

export default {
  name: 'TaskCell', // 任务cell
  components: {},
  mixins: [],
  props: {
    data: Object,
    dataIndex: Number
  },
  data() {
    return {}
  },
  computed: {},
  watch: {},
  mounted() {},
  methods: {
    // 列表标记任务
    taskOverClick(val) {
      editTask({
        taskId: val.taskId,
        status: val.checked ? 5 : 1
      })
        .then(res => {
          this.$store.dispatch('GetOAMessageNum', 'task')
        })
        .catch(() => {
          val.checked = false
        })
    },
    // 点击显示详情
    rowFun(val) {
      this.$emit('on-handle', {
        type: 'view',
        data: { item: this.data, index: this.dataIndex }
      })
    },
    onmouseoverFun(item) {
      if (
        this.$refs.itemSpan.offsetWidth >
        this.$refs.listLeft.offsetWidth - 21
      ) {
        this.$set(item, 'show', true)
      } else {
        this.$set(item, 'show', false)
      }
    }
  }
}
</script>
<style lang="scss" scoped>
@import '../../styles/content.scss';
.list {
  // padding-bottom: 12px;
  // padding-top: 12px;
  padding: 0 10px;
  cursor: pointer;
  border-bottom: 1px solid #e6e6e6;
  border-left: 2px solid transparent;
  border-radius: 2px;
  // position: relative;
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
    // margin-top: 6px;
    color: #999;
    font-size: 12px;
    // margin-left: 27px;
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
    // max-width: 60%;
    flex: 1;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    padding-right: 10px;
  }
  .list-right {
    // position: absolute;
    // right: 0;
    // top: 50%;
    // max-width: 40%;
    // transform: translateY(-50%);
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
</style>
