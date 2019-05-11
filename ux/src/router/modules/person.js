/** 个人中西管理路由 */
import Layout from '@/views/layout/personCenterLayout'

const personRouter = {
  path: '/person',
  component: Layout,
  redirect: '/person/index',
  name: 'person',
  hidden: true,
  meta: {
    title: '个人中心'
  },
  children: [{
    path: 'index',
    component: () => import('@/views/PersonCenter/index')
  }]
}

export default personRouter
