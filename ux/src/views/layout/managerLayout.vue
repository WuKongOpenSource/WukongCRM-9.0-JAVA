<template>
  <el-container>
    <el-header class="nav-container">
      <manager-navbar/>
    </el-header>
    <el-container>
      <el-aside
        width="auto"
        class="aside-container">
        <sidebar
          :items="routerItems"
          create-button-title=""
          main-router="manager"/>
      </el-aside>
      <el-main id="manager-main-container">
        <app-main/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { mapGetters } from 'vuex'
import { ManagerNavbar, Sidebar, AppMain } from './components'
import { managerRouterMenu } from '@/router/modules/manager'
import { adminGroupsTypeListAPI } from '@/api/systemManagement/RoleAuthorization'

export default {
  name: 'Layout',
  components: {
    ManagerNavbar,
    Sidebar,
    AppMain
  },

  data() {
    return {
      routerItems: []
    }
  },

  computed: {
    ...mapGetters(['manage'])
  },

  mounted() {
    this.routerItems = this.filterAsyncRouter(managerRouterMenu, {
      manage: this.manage
    })
    this.getAuthMenu()
  },

  methods: {
    filterAsyncRouter(routers, authInfo) {
      const res = []
      routers.forEach(router => {
        const tmp = {
          ...router
        }
        if (this.checkAuth(tmp, authInfo)) {
          if (tmp.children) {
            tmp.children = this.filterAsyncRouter(tmp.children, authInfo)
          }
          res.push(tmp)
        }
      })
      return res
    },

    checkAuth(router, authInfo) {
      if (router.meta) {
        const metaInfo = router.meta
        if (!metaInfo.requiresAuth) {
          return true
        } else {
          if (metaInfo.index == 0) {
            return !!authInfo[metaInfo.type]
          } else if (metaInfo.index == 1) {
            if (authInfo[metaInfo.type]) {
              return !!authInfo[metaInfo.type][metaInfo.subType]
            }
            return false
          } else {
            var typeAuth = authInfo[metaInfo.type]
            for (let index = 0; index < metaInfo.subType.length; index++) {
              const field = metaInfo.subType[index]
              typeAuth = typeAuth[field]
              if (typeAuth && metaInfo.subType.length - 1 == index) {
                return true
              } else if (!typeAuth) {
                return false
              }
            }
          }
        }
      }
      return true
    },

    navClick(index) {},

    getAuthMenu() {
      adminGroupsTypeListAPI()
        .then(res => {
          for (let index = 0; index < this.routerItems.length; index++) {
            const menuItem = this.routerItems[index]
            if (menuItem.meta.icon == 'contacts' && !menuItem.hidden) {
              menuItem.children = res.data.map(item => {
                return {
                  name: 'role-auth',
                  path: `role-auth/${item.roleType}/${encodeURI(item.name)}`,
                  meta: {
                    title: item.name
                  }
                }
              })
            }
          }
        })
        .catch(() => {})
    }
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

.el-container {
  overflow: hidden;
}
</style>
