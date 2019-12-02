<template>
  <el-container>
    <el-header class="nav-container">
      <navbar
        :nav-index="5"
        @nav-items-click="navClick"/>
    </el-header>
    <el-container>
      <el-aside
        width="auto"
        class="aside-container">
        <sidebar
          :items="biRouterItems"
          create-button-title=""
          main-router="bi"/>
      </el-aside>
      <el-main id="crm-main-container">
        <app-main/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { mapGetters } from 'vuex'
import { Navbar, Sidebar, AppMain } from './components'
import { biRouterMenu } from '@/router/modules/business'

export default {
  name: 'BusinessLayout',
  components: {
    Navbar,
    Sidebar,
    AppMain
  },
  data() {
    return {}
  },
  computed: {
    ...mapGetters(['bi']),
    biRouterItems() {
      for (let index = 0; index < biRouterMenu.length; index++) {
        const routerMenuItem = biRouterMenu[index]
        routerMenuItem.hidden = !this.bi[routerMenuItem.meta.subType]
      }
      return biRouterMenu
    }
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
