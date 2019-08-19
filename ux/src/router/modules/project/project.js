/** 项目管理路由 */

const workbenchRouter = {
  path: 'workbench',
  meta: {
    icon: 'workbench',
    title: '工作台'
  },
  children: [{
    name: 'my-task',
    path: 'my-task',
    component: () => import('@/views/projectManagement/task/index'),
    meta: {
      title: '我的任务'
    }
  },
  {
    path: 'task-calendars',
    component: () => import('@/views/projectManagement/calendars/index'),
    meta: {
      title: '任务日历'
    }
  }
  ]
}

// 项目
const projectRouter = {
  path: 'project',
  meta: {
    icon: 'project',
    title: '项目'
  },
  children: []
}

// 统计分析
const statisticsRouter = {
  path: 'statistics',
  component: () => import('@/views/projectManagement/statistics/index'),
  meta: {
    icon: 'statistics',
    title: '统计分析'
  }
}

// 项目归档
const archiveRouter = {
  path: 'archive-project',
  component: () => import('@/views/projectManagement/archive/index'),
  meta: {
    icon: 'product',
    title: '归档项目'
  }
}

// 标签
const tagRouter = {
  path: 'tag',
  meta: {
    icon: 'tag',
    title: '标签',
    fontSize: '18px'
  },
  children: []
}

// 回收站
const recycleRouter = {
  path: 'recycle',
  component: () => import('@/views/projectManagement/recycle/index'),
  meta: {
    icon: 'recycle',
    title: '回收站',
    fontSize: '18px'
  }
}

// 其他
const otherRouter = {
  hidden: true,
  meta: {
    title: '项目管理'
  },
  children: [{
    path: 'tag/:id',
    component: () => import('@/views/projectManagement/tag/index')
  },
  {
    name: 'project-list',
    path: 'list/:id',
    component: () => import('@/views/projectManagement/project/index')
  }
  ]
}

export const children = [...workbenchRouter.children, ...projectRouter.children, statisticsRouter, archiveRouter, ...tagRouter.children, recycleRouter, ...otherRouter.children]
export const childrenMenu = [workbenchRouter, projectRouter, statisticsRouter, archiveRouter, tagRouter, recycleRouter]
