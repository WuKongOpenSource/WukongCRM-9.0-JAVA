<template>
  <el-container>
    <el-header class="nav-container">
      <manager-navbar></manager-navbar>
    </el-header>
    <el-container>
      <el-aside width="auto"
                class="aside-container">
        <sidebar :items="managerRouterItems"
                 createButtonTitle=""
                 mainRouter="manager"></sidebar>
      </el-aside>
      <el-main id="manager-main-container">
        <app-main></app-main>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { mapGetters } from 'vuex'
import { ManagerNavbar, Sidebar, AppMain } from './components'
import { managerRouterMenu } from '@/router/modules/manager'

export default {
  name: 'Layout',
  components: {
    ManagerNavbar,
    Sidebar,
    AppMain
  },
  computed: {
    ...mapGetters(['manage']),
    managerRouterItems() {
      for (let index = 0; index < managerRouterMenu.length; index++) {
        const routerMenuItem = managerRouterMenu[index]
        routerMenuItem.hidden = this.manage[routerMenuItem.meta.subType]
          ? false
          : true
      }
      return managerRouterMenu
    }
  },
  data() {
    return {}
  },
  methods: {
    navClick(index) {}
  }
}
</script>

<style lang="scss" scoped>
.aside-container {
  position: relative;
  background-color: #2d3037;
  box-sizing: border-box;
  border-right: solid 1px #e6e6e6;
}

.nav-container {
  padding: 0;
  box-shadow: 0px 1px 2px #dbdbdb;
  z-index: 100;
  min-width: 1200px;
}

#manager-main-container {
  max-height: 100%;
}
</style>
