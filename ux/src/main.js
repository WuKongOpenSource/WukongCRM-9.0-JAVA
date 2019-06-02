import 'babel-polyfill'
import Vue from 'vue'

import 'normalize.css/normalize.css' // A modern alternative to CSS resets

import ElementUI from 'element-ui'
Vue.use(ElementUI)
import 'element-ui/lib/theme-chalk/index.css'

import '@/styles/index.scss' // global css

import App from './App'
import router from './router'
import store from './store'

import cache from '@/utils/cache'
cache.loadingCache()

import '@/permission' // permission control
import '@/styles/animate.css'

/** 事件传递 */
import VueBus from 'vue-bus'
Vue.use(VueBus)

/** 常用flex组件 */
import {
  Flexbox,
  FlexboxItem
} from '@/components/flexbox'
Vue.component('flexbox', Flexbox)
Vue.component('flexbox-item', FlexboxItem)

/** 懒加载图片 */
import VueLazyload from 'vue-lazyload'
Vue.use(VueLazyload, {
  preLoad: 1.3,
  error: require('@/assets/img/send_img.png'),
  loading: require('@/assets/img/loading.gif'),
  attempt: 1
})

import * as filters from './filters' // global filters
// 注册全局过滤器
Object.keys(filters).forEach(key => {
  Vue.filter(key, filters[key])
})

// 处理时间的过滤器
Vue.use(require('vue-moment'))
import moment from 'moment'
moment.locale('zh_cn')

// 自定义全局点击空白处组件
import clickoutside from './directives'
Vue.directive('clickoutside', clickoutside)
import empty from './directives/empty'
Vue.use(empty)
import photo from './directives/photo'
Vue.use(photo)

// 表情
import {
  emoji
} from './utils/emoji'
Vue.prototype.emoji = emoji

Vue.config.productionTip = false

window.app = new Vue({
  el: '#app',
  router,
  store,
  data: {
    eventHub: new Vue()
  },
  render: h => h(App)
})
