<template>
  <el-container>
    <el-header class="nav-container">
      <navbar :navIndex="5"
              @nav-items-click="navClick"></navbar>
    </el-header>
    <el-container>
      <el-aside width="auto"
                class="aside-container">
        <sidebar :items="biRouters.children"
                 createButtonTitle=""
                 mainRouter="bi"></sidebar>
      </el-aside>
      <el-main id="crm-main-container"
               style="padding:15px; height: 100%;">
        <app-main></app-main>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { mapGetters } from 'vuex'
import { Navbar, Sidebar, AppMain } from './components'

export default {
  name: 'business-layout',
  components: {
    Navbar,
    Sidebar,
    AppMain
  },
  computed: {
    ...mapGetters(['bi', 'biRouters'])
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
</style>
