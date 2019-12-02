/** 项目管理路由 */
import Layout from '@/views/layout/workbenchLayout'

const workbenchRouter = {
  path: '/workbench',
  component: Layout,
  redirect: '/workbench/index',
  name: 'oa',
  hidden: true,
  meta: {
    requiresAuth: true,
    title: '工作台',
    index: 0,
    type: 'oa'
  },
  children: [{
    path: 'index',
    component: () => import('@/views/OAManagement/workbench/index'),
    meta: {
      title: '工作台',
      icon: 'workbench'
    }
  },
  {
    path: 'schedule',
    component: () => import('@/views/OAManagement/schedule/index'),
    meta: {
      title: '日程',
      icon: 'schedule',
      num: 0,
      numType: 'eventNum'
    }
  },
  {
    path: 'task',
    component: () => import('@/views/OAManagement/task/index'),
    meta: {
      title: '任务',
      icon: 'task',
      num: 0,
      numType: 'taskNum'
    }
  },
  {
    path: 'notice',
    component: () => import('@/views/OAManagement/notice/index'),
    meta: {
      requiresAuth: false,
      title: '公告',
      icon: 'notice',
      index: 1,
      type: 'oa',
      subType: 'announcement',
      num: 0,
      numType: 'announcementNum'
    }
  },
  {
    path: 'journal',
    component: () => import('@/views/OAManagement/journal/index'),
    meta: {
      title: '日志',
      icon: 'log',
      num: 0,
      numType: 'logNum'
    }
  },
  {
    path: 'examine',
    component: () => import('@/views/OAManagement/examine/index'),
    meta: {
      title: '审批',
      icon: 'examine',
      num: 0,
      numType: 'examineNum'
    }
  },
  {
    path: 'notice-new',
    component: () => import('@/views/OAManagement/notice/newDialog'),
    hidden: true
  },
  {
    path: 'schedule-new',
    component: () => import('@/views/OAManagement/schedule/components/createSchedule'),
    hidden: true
  },
    // 通讯录
  {
    path: 'address-book',
    component: () => import('@/views/OAManagement/addressBook/index'),
    meta: {
      requiresAuth: true,
      title: '通讯录',
      icon: 'address',
      index: 1,
      type: 'oa',
      subType: 'book'
    }
  }
  ]
}

export default workbenchRouter
