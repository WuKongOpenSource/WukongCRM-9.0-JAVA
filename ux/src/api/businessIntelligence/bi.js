import request from '@/utils/request'

/**
 * 业绩目标完成情况
 * @param {*} data
 */
export function biAchievementStatistics(data) {
  return request({
    url: 'bi/taskCompleteStatistics',
    method: 'post',
    data: data
  })
}

/**
 * 产品销售情况统计
 * @param {*} data
 */
export function biProductStatistics(data) {
  return request({
    url: 'bi/productStatistics',
    method: 'post',
    data: data
  })
}

/**
 * 回款统计
 * @param {*} data
 */
export function biReceivablesStatistics(data) {
  return request({
    url: 'bi/moneyStatistics',
    method: 'post',
    data: data
  })
}

/**
 * 员工客户分析
 * @param {*} data
 */
export function biCustomerStatistics(data) {
  return request({
    url: 'CrmCustomer/getUserCustomerAnalysis',
    method: 'post',
    data: data
  })
}

/**
 * 销售漏斗
 * @param {*} data
 */
export function biBusinessFunnel(data) {
  return request({
    url: 'biFunnel/sellFunnel',
    method: 'post',
    data: data
  })
}
