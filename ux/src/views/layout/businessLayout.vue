<template>
  <el-container>
    <el-header class="nav-container">
      <navbar :navIndex="5"
              @nav-items-click="navClick"></navbar>
    </el-header>
    <el-container>
      <el-aside width="auto"
                class="aside-container">
        <sidebar :items="biRouterItems"
                 createButtonTitle=""
                 mainRouter="bi"></sidebar>
      </el-aside>
      <el-main id="crm-main-container">
        <app-main></app-main>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { mapGetters } from 'vuex'
import { Navbar, Sidebar, AppMain } from './components'
import { biRouterMenu } from '@/router/modules/business'

export default {
  name: 'business-layout',
  components: {
    Navbar,
    Sidebar,
    AppMain
  },
  computed: {
    ...mapGetters(['bi']),
    biRouterItems() {
      for (let index = 0; index < biRouterMenu.length; index++) {
        const routerMenuItem = biRouterMenu[index]
        routerMenuItem.hidden = this.bi[routerMenuItem.meta.subType]
          ? false
          : true
      }
      return biRouterMenu
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

.el-container {
  overflow: hidden;
}
</style>
