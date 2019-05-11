/** 项目管理路由 */
import Layout from '@/views/layout/businessLayout'

const businessRouter = {
  path: '/bi',
  component: Layout,
  redirect: '/bi/employeestatistics',
  name: 'bi',
  hidden: true,
  meta: {
    requiresAuth: true,
    title: '商业智能',
    index: 0,
    type: 'bi'
  },
  children: [{
    path: 'employeestatistics', // 员工客户分析
    component: () => import('@/views/businessIntelligence/EmployeeStatistics'),
    meta: {
      requiresAuth: true,
      title: '员工客户分析',
      icon: 'employeestatistics',
      index: 1,
      type: 'bi',
      subType: 'customer'
    }
  }, {
    path: 'funnelstatistics', // 销售漏斗
    component: () => import('@/views/businessIntelligence/FunnelStatistics'),
    meta: {
      requiresAuth: true,
      title: '销售漏斗',
      icon: 'funnelstatistics',
      index: 1,
      type: 'bi',
      subType: 'business'
    }
  }, {
    path: 'moneystatistics', // 回款统计
    component: () => import('@/views/businessIntelligence/MoneyStatistics'),
    meta: {
      requiresAuth: true,
      title: '回款统计',
      icon: 'moneystatistics',
      index: 1,
      type: 'bi',
      subType: 'receivables'
    }
  }, {
    path: 'productstatistics', // 产品销售情况统计
    component: () => import('@/views/businessIntelligence/ProductStatistics'),
    meta: {
      requiresAuth: true,
      title: '产品销售情况统计',
      icon: 'productstatistics',
      index: 1,
      type: 'bi',
      subType: 'product'
    }
  }, {
    path: 'taskCompletestatistics', // 业绩目标完成情况
    component: () => import('@/views/businessIntelligence/TaskCompleteStatistics'),
    meta: {
      requiresAuth: true,
      title: '业绩目标完成情况',
      icon: 'taskCompletestatistics',
      index: 1,
      type: 'bi',
      subType: 'achievement'
    }
  }]
}

export default businessRouter
