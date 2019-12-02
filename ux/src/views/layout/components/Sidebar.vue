<template>
  <div class="container">
    <div
      :style="{ 'padding-top': createButtonTitle != '' ? '40px' : '25px', 'background-color':backgroundColor }"
      class="create-button-container">
      <el-popover
        v-if="createButtonTitle != ''"
        :offset="addOffset"
        :visible-arrow="false"
        placement="right"
        popper-class="no-padding-popover"
        trigger="hover">
        <slot name="add"/>
        <div
          slot="reference"
          :style="{ 'background-color': createButtonBackgroundColor }"
          class="create-button"
          @click="quicklyCreate">
          <div
            v-show="!buttonNameCollapse"
            class="button-name">{{ createButtonTitle }}</div>
          <div
            v-show="!buttonNameCollapse"
            class="button-line"/>
          <i
            :class="createButtonIcon"
            class="button-mark"/>
        </div>
      </el-popover>
    </div>
    <el-menu
      :default-active="activeIndex"
      :style="{'border-right-color': backgroundColor, 'padding-top': createButtonTitle != '' ? '90px' : '40px'}"
      :text-color="textColor"
      :background-color="backgroundColor"
      :active-text-color="activeTextColor"
      :collapse="collapse"
      class="el-menu-vertical"
      unique-opened>
      <template
        v-for="(item, index) in getShowMenu(items)">
        <router-link
          v-if="!item.children"
          :key="index"
          :to="getFullPath(item.path)">
          <el-menu-item
            :index="getFullPath(item.path)"
            :class="{'menu-item-select': activeIndex == getFullPath(item.path)}"
            class="menu-item-defalt">
            <i
              :class="'wukong-' + item.meta.icon"
              :style="{ 'color': activeIndex == getFullPath(item.path) ? activeTextColor : textColor, fontSize: item.meta.fontSize || '16px'}"
              class="wukong"/>
            <span slot="title">{{ item.meta.title }}</span>
            <el-badge
              v-if="item.meta.num && item.meta.num > 0"
              :max="99"
              :value="item.meta.num"/>
          </el-menu-item>
        </router-link>
        <el-submenu
          v-else
          :key="index"
          :index="getFullPath(item.path)">
          <template
            v-if="!item.hidden"
            slot="title">
            <i
              :class="'wukong-' + item.meta.icon"
              :style="{fontSize: item.meta.fontSize || '16px'}"
              class="wukong"/>
            <span slot="title">{{ item.meta.title }}</span>
          </template>
          <router-link
            v-for="(subitem, subindex) in getShowMenu(item.children)"
            :key="subindex"
            :to="getFullPath(subitem.path)">
            <el-menu-item
              :index="getFullPath(subitem.path)"
              :class="{'menu-item-select': activeIndex == getFullPath(subitem.path) }"
              class="menu-item-defalt">
              {{ subitem.meta.title }}
            </el-menu-item>
          </router-link>
        </el-submenu>
      </template>
    </el-menu>
    <div
      :style="{ 'background-color':backgroundColor }"
      class="sidebar-bottom">
      <div class="sidebar-container">
        <img
          :style="{ 'right': buttonNameCollapse ? '3px' : '0' }"
          class="collapse-button"
          src="@/assets/img/collapse_white.png"
          alt=""
          @click="toggleSideBarClick">
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Sidebar',
  props: {
    mainRouter: {
      type: String,
      default: ''
    },
    addOffset: {
      type: Number,
      default: 70
    },
    /** 选择项目 */
    items: {
      type: Array,
      default: () => {
        return []
      }
    },
    backgroundColor: {
      type: String,
      default: '#2D3037'
    },
    activeTextColor: {
      type: String,
      default: '#fff'
    },
    textColor: {
      type: String,
      default: '#bebec0'
    },
    selectLineColor: {
      type: String,
      default: '#3E84E9'
    },
    selectBackgroundColor: {
      type: String,
      default: '#454E57'
    },
    createButtonTitle: {
      type: String,
      default: ''
    },
    createButtonBackgroundColor: {
      type: String,
      default: '#3E84E9'
    },
    createButtonIcon: {
      type: String,
      default: 'el-icon-arrow-right'
    }
  },
  data() {
    return {
      collapse: false, // 菜单开关
      buttonNameCollapse: false
    }
  },
  computed: {
    ...mapGetters(['activeIndex'])
  },
  watch: {
    collapse: function(val) {
      if (val) {
        this.buttonNameCollapse = val
      } else {
        setTimeout(() => {
          this.buttonNameCollapse = val
        }, 300)
      }
    }
  },
  mounted() {},
  methods: {
    toggleSideBarClick() {
      this.collapse = !this.collapse
    },

    // 快速创建
    quicklyCreate() {
      this.$emit('quicklyCreate')
    },

    getFullPath(path) {
      return `/${this.mainRouter}/${path}`
    },

    getShowMenu(array) {
      return array.filter(item => {
        return !item.hidden
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  position: relative;
  height: 100%;
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 200px;
  min-height: 400px;
}

.el-menu-vertical {
  height: 100%;
  overflow: auto;
  padding-bottom: 48px;
  .el-submenu.is-active {
    .el-submenu__title {
      .wukong {
        color: white;
      }
      span {
        color: white;
      }
    }
  }
}

.menu-item-icon-container {
  display: inline-block;
  margin-right: 10px;
  .menu-item-icon-flex {
    width: 22px;
    height: 22px;
    position: relative;
    .menu-item-icon {
      display: block;
    }
  }
}

.menu-item-defalt {
  border-left: 2px solid transparent;
  height: 46px;
  line-height: 46px;
}

.menu-item-select {
  border-left: 2px solid #3e84e9;
  background-color: #454e57 !important;
}

.create-button-container {
  padding: 15px 12px 15px 12px;
  color: white;
  font-size: 14px;
  cursor: pointer;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 2;

  .create-button {
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    padding: 0 15px;
    height: 36px;
    border-radius: 4px;

    .button-name {
      flex: 1;
    }

    .button-line {
      height: 10px;
      background-color: white;
      width: 1px;
      margin: 0 20px 0 10px;
      opacity: 0.3;
    }

    .button-mark {
      width: 12px;
    }
  }
}

.side-bar {
  height: 32px;
  padding: 0 16px 16px;
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: #eee;
}

.sidebar-bottom {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 48px;

  .sidebar-container {
    position: relative;
    height: 48px;
  }
}

.collapse-button {
  position: absolute;
  top: 0;
  padding: 18px 20px;
}

.wukong {
  margin-right: 8px;
}

// 消息数
.el-badge {
  position: absolute;
  right: 15px;
  top: 5px;
  /deep/ .el-badge__content {
    border-width: 0;
  }
}
</style>
