<template>
  <el-container>
    <el-header class="nav-container">
      <navbar :navIndex="navActiveIndex"
              @nav-items-click="navClick"></navbar>
    </el-header>
    <el-container>
      <el-main id="crm-main-container"
               style="padding:15px;">
        <app-main></app-main>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { Navbar, Sidebar, AppMain } from './components'
import { mapGetters } from 'vuex'

export default {
  name: 'Layout',
  components: {
    Navbar,
    Sidebar,
    AppMain
  },
  computed: {
    ...mapGetters(['navActiveIndex'])
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
