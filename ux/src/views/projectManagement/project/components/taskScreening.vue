<template>
  <transition name="slide-fade">
    <el-card
      :style="{ 'z-index': zIndex }"
      class="project-screening">
      <p class="header">
        <span class="label">任务筛选</span>
        <el-button
          type="text"
          @click="resetBtn">重置</el-button>
        <span
          class="rt el-icon-close"
          @click="close"/>
      </p>
      <div class="content">
        <div
          v-for="(item, index) in menuList"
          :key="index"
          class="menu-list">
          <p
            class="item-label"
            @click="rowFun(item)">
            {{ item.label }}
            <span :class="item.expand ? 'el-icon-arrow-right item-expand' : 'el-icon-arrow-down item-expand'"/>
          </p>
          <div
            v-for="(val, i) in item.list"
            v-show="item.expand == false"
            :key="i"
            :class="['item-list', val.checked ? 'item-list-active' : '']"
            @click="rowChecked(val)">
            <div
              v-photo="val"
              v-lazy:background-image="$options.filters.filterUserLazyImg(val.img)"
              v-if="val.type == 'user'"
              :key="val.img"
              class="div-photo"/>
            <i
              v-if="val.type == 'tag'"
              :style="{color:val.color}"
              style="margin-right:10px; vertical-align: text-top;"
              class="wukong wukong-tag"/>
            <span>{{ val.name }}</span>
            <span class="el-icon-check rt"/>
          </div>
        </div>
      </div>
    </el-card>
  </transition>
</template>

<script>
import { workWorkOwnerListAPI } from '@/api/projectManagement/project'
import { workTasklableIndexAPI } from '@/api/projectManagement/tag'
import { getMaxIndex } from '@/utils/index'

export default {

  props: {
    workId: [Number, String]
  },
  data() {
    return {
      zIndex: getMaxIndex(),
      menuList: [
        {
          label: '成员',
          id: '1',
          expand: false,
          list: []
        },
        {
          label: '截止时间',
          id: '2',
          expand: false,
          list: [
            {
              id: '1',
              name: '今天',
              type: 'time',
              checked: false
            },
            {
              id: '2',
              name: '明天',
              type: 'time',
              checked: false
            },
            {
              id: '3',
              name: '本周',
              type: 'time',
              checked: false
            },
            {
              id: '4',
              name: '本月',
              type: 'time',
              checked: false
            },
            {
              id: '5',
              name: '未设置截止时间',
              type: 'time',
              checked: false
            },
            {
              id: '6',
              name: '已延期',
              type: 'time',
              checked: false
            },
            {
              id: '7',
              name: '今日更新',
              type: 'time',
              checked: false
            }
          ]
        },
        {
          label: '标签',
          id: '3',
          expand: false,
          list: []
        }
      ]
    }
  },
  created() {
    this.getUserList()
    this.getTagList()
  },

  mounted() {
    document
      .getElementById('app')
      .addEventListener('click', this.taskShowHandle, false)
  },
  methods: {
    /**
     * 获取成员
     */
    getUserList() {
      this.menuList[0].list = []
      workWorkOwnerListAPI({
        workId: this.workId
      }).then(res => {
        this.menuList[0].list = res.data.map(item => {
          item.checked = false
          item.name = item.realname
          item.type = 'user'
          return item
        })
      })
    },

    /**
     * 获取标签
     */
    getTagList() {
      this.menuList[2].list = []
      workTasklableIndexAPI()
        .then(res => {
          this.menuList[2].list = res.data.map(item => {
            item.id = item.labelId
            item.checked = false
            item.type = 'tag'
            return item
          })
        })
        .catch(() => {})
    },

    close() {
      this.$emit('close')
    },

    rowChecked(val) {
      if (val.type == 'time') {
        for (const k of this.menuList[1].list) {
          if (val.id == k.id) {
            k.checked = !k.checked
          } else {
            k.checked = false
          }
        }
      } else {
        val.checked = !val.checked
      }
      // 过滤数据 -- 传值
      // 人员
      const userIds = []
      for (const item of this.menuList[0].list) {
        if (item.checked) {
          userIds.push(item.userId)
        }
      }
      // 时间
      let timeId = ''
      for (const item of this.menuList[1].list) {
        if (item.checked) {
          timeId = item.id
          break
        }
      }

      // 标签
      const tagIds = []
      for (const item of this.menuList[2].list) {
        if (item.checked) {
          tagIds.push(item.id)
        }
      }
      this.$bus.$emit('search', userIds, timeId, tagIds)
    },

    resetBtn() {
      for (const item of this.menuList) {
        for (const i of item.list) {
          i.checked = false
        }
      }
      this.$bus.$emit('search', [], '', [])
    },

    rowFun(val) {
      val.expand ? (val.expand = false) : (val.expand = true)
    },

    /**
     * 点击空白处关闭详情
     */
    taskShowHandle(e) {
      if (!this.$el.contains(e.target)) {
        this.close()
      }
    }
  }
}
</script>

<style scoped lang="scss">
.project-screening /deep/ .el-card__body {
  padding: 0;
}
.project-screening {
  position: fixed;
  top: 60px;
  bottom: 0;
  right: 0;
  width: 300px;
  overflow: auto;
  .header {
    border-bottom: 1px solid #e6e6e6;
    margin-bottom: 10px;
    padding: 9px 20px;
    .label {
      font-size: 16px;
      margin-right: 10px;
    }
    .el-icon-close {
      color: #ccc;
      font-size: 18px;
      margin-top: 11px;
      margin-right: 0;
      cursor: pointer;
    }
  }
  .content {
    font-size: 13px;
    .menu-list {
      margin-bottom: 10px;
      .item-label {
        position: relative;
        height: 36px;
        line-height: 36px;
        padding: 0 20px;
        cursor: pointer;
        color: #666;
        .item-expand {
          height: 36px;
          line-height: 36px;
          position: absolute;
          right: 12px;
          top: 0;
        }
      }
      .item-list {
        height: 30px;
        line-height: 30px;
        padding: 0 10px 0 35px;
        margin: 5px 0;
        color: #333;
        .el-icon-check {
          margin-top: 8px;
          opacity: 0;
        }
        .div-photo {
          width: 24px;
          height: 24px;
          border-radius: 12px;
          vertical-align: middle;
          margin-right: 5px;
        }
        span {
          vertical-align: middle;
        }
      }
      .item-list:hover {
        background: #f7f8fa;
        cursor: pointer;
      }
      .item-list-active {
        background: #f7f8fa;
        cursor: pointer;
        .el-icon-check {
          opacity: 1;
        }
      }
    }
  }
}
</style>
