import router from './router'
import store from './store'
import NProgress from 'nprogress' // Progress 进度条
import 'nprogress/nprogress.css' // Progress 进度条样式
import {
  Message
} from 'element-ui'
import {
  getAuth
} from '@/utils/auth' // 验权


const whiteList = ['/login'] // 不重定向白名单
router.beforeEach((to, from, next) => {
  localStorage.setItem("routerBefore", from.path)
  NProgress.start()
  /** 请求头包含授权信息 并且 页面必须授权 直接进入 */
  if (getAuth()) {
    if (to.path === '/login') {
      next({
        path: '/workbench/index'
      })
      NProgress.done()
    } else {
      if (!store.getters.allAuth) { // 判断当前用户是否获取权限
        store.dispatch('getAuth').then(res => { // 拉取user_info
          const auths = res
          store.dispatch('GenerateRoutes', auths).then(() => { // 根据auths权限生成可访问的路由表
            router.addRoutes(store.getters.addRouters) // 动态添加可访问路由表
            next({
              ...to,
              replace: true
            })
          })
        }).catch((err) => {
          store.dispatch('LogOut').then(() => {
            Message.error(err || '获取用户信息失败')
            next({
              path: '/'
            })
          })
        })
      } else {
        next()
      }
    }
  } else {
    if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      next(`/login?redirect=${to.path}`) // 否则全部重定向到登录页
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done() // 结束Progress
})

router.onError((error) => {
  const pattern = /Loading chunk (\d)+ failed/g
  const isChunkLoadFailed = error.message.match(pattern)
  const targetPath = router.history.pending.fullPath
  if (isChunkLoadFailed) {
    router.replace(targetPath)
  }
})
