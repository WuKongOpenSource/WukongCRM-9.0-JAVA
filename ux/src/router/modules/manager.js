/** 项目管理路由 */
import Layout from '@/views/layout/managerLayout'

const managerRouter = {
  path: '/manager',
  component: Layout,
  redirect: '/manager/systemconfig',
  name: 'manager',
  hidden: true,
  meta: {
    title: '系统管理',
    index: 0,
    type: 'manage'
  },
  children: [{
      name: 'systemconfig',
      path: 'systemconfig', // 系统配置
      component: () => import('@/views/SystemManagement/SystemConfig/index'),
      meta: {
        requiresAuth: true,
        title: '系统配置',
        icon: 'systemconfig',
        index: 1,
        type: 'manage',
        subType: 'system'
      }
    },
    {
      name: 'employee-dep',
      path: 'employee-dep', // 员工与部门管理
      component: () => import('@/views/SystemManagement/SystemEmployee/EmployeeDepManagement'),
      meta: {
        requiresAuth: true,
        title: '员工与部门管理',
        icon: 'department',
        index: 1,
        type: 'manage',
        subType: 'user'
      }
    },
    {
      name: 'role-authorization',
      path: 'role-authorization',
      component: () => import('@/views/SystemManagement/RoleAuthorization'),
      meta: {
        requiresAuth: true,
        title: '角色权限管理',
        icon: 'contacts',
        index: 1,
        type: 'manage',
        subType: 'permission'
      }
    },
    {
      name: 'system-examine',
      path: 'system-examine',
      component: () => import('@/views/SystemManagement/SystemExamine/SystemExamine'),
      meta: {
        requiresAuth: true,
        title: '审批流程管理',
        icon: 'examine',
        index: 1,
        type: 'manage',
        subType: 'examineFlow'
      }
    },
    {
      name: 'systemworkbench',
      path: 'systemworkbench', // 工作台
      component: () => import('@/views/SystemManagement/SystemWorkbench'),
      meta: {
        requiresAuth: true,
        title: '工作台',
        icon: 'workbench',
        index: 1,
        type: 'manage',
        subType: 'oa'
      }
    },
    {
      name: 'system-customer',
      path: 'system-customer',
      component: () => import('@/views/SystemManagement/SystemCustomer/SystemCustomer'),
      meta: {
        requiresAuth: true,
        title: '客户管理',
        icon: 'customer',
        index: 1,
        type: 'manage',
        subType: 'crm'
      }
    },
    {
      name: 'handlefield',
      path: 'system-customer/handlefield/:type/:id/:label', // type customer contacts business contract money
      component: () => import('@/views/SystemManagement/SystemCustomer/HandleField'),
      meta: {
        changeMenu: false, // 跳转不更改路径
        menuSelct: 'system-customer'
      },
      hidden: true
    }
  ]
}

export default managerRouter
