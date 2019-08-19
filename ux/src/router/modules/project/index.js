import Layout from '@/views/layout/projectLayout'
import {
  children
} from './project'

const workbenchRouter = {
  path: '/project',
  component: Layout,
  redirect: '/project/my-task',
  name: 'project',
  meta: {
    icon: 'workbench',
    title: '项目管理'
  },
  children: children
}

export default workbenchRouter
