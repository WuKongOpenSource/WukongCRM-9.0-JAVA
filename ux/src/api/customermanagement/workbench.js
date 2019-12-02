import request from '@/utils/request'

// 销售简报
export function crmIndexIndex(data) {
  return request({
    url: 'Crm/Instrument/queryBulletin',
    method: 'post',
    data: data
  })
}

/**
 * 销售简报列表
 */
export function crmIndexIndexListAPI(data) {
  return request({
    url: 'Crm/Instrument/queryBulletinInfo',
    method: 'post',
    data: data
  })
}

/**
 * 简报跟进记录数量
 * @param {*} data
 */
export function crmQueryRecordConuntAPI(data) {
  return request({
    url: 'Crm/Instrument/queryRecordConunt',
    method: 'post',
    data: data
  })
}

// 业绩指标
export function crmIndexAchievementData(data) {
  return request({
    url: 'Crm/Instrument/queryPerformance',
    method: 'post',
    data: data
  })
}

// 销售漏斗
export function crmIndexFunnel(data) {
  return request({
    url: 'Crm/Instrument/queryBusiness',
    method: 'post',
    data: data
  })
}

// 销售趋势
export function crmIndexSaletrend(data) {
  return request({
    url: 'Crm/Instrument/sellMonth',
    method: 'post',
    data: data
  })
}

// 获取简报 跟进记录信息
export function crmIndexGetRecordListAPI(data) {
  return request({
    url: 'Crm/Instrument/queryRecordList',
    method: 'post',
    data: data
  })
}
