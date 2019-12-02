import request from '@/utils/request'

/**
 * 日志统计
 * @param {*} data
 */
export function biLogStatisticsAPI(data) {
  return request({
    url: 'biWork/logStatistics',
    method: 'post',
    data: data
  })
}

/**
 * 日志统计导出
 * @param {*} data
 */
export function biLogExcelExportAPI(data) {
  return request({
    url: 'biWork/logStatisticsExport',
    method: 'post',
    data: data,
    responseType: 'blob',
    timeout: 600000
  })
}

/**
 * 审批统计
 * @param {*} data
 */
export function biExamineStatisticsAPI(data) {
  return request({
    url: 'biWork/examineStatistics',
    method: 'post',
    data: data
  })
}

/**
* 审批统计详情列表
* @param {*} data
*/
export function biExamineIndexAPI(data) {
  return request({
    url: 'biWork/examineInfo',
    method: 'post',
    data: data
  })
}

/**
 * 审批统计导出
 * @param {*} data
 */
export function biExamineExcelExportAPI(data) {
  return request({
    url: 'biWork/examineStatisticsExport',
    method: 'post',
    data: data,
    responseType: 'blob',
    timeout: 600000
  })
}
