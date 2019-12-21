import request from '@/utils/request'

// crm 新建回款
export function crmReceivablesSave(data) {
  return request({
    url: 'CrmReceivables/saveOrUpdate',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 回款列表
 * @param {*} data
 */
export function crmReceivablesIndex(data) {
  return request({
    url: 'CrmReceivables/queryPageList',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 删除
 * @param {*} data
 *
 */
export function crmReceivablesDelete(data) {
  return request({
    url: 'CrmReceivables/deleteByIds',
    method: 'post',
    data: data
  })
}

/**
 * 回款详情
 * @param {*} data
 */
export function crmReceivablesRead(data) {
  return request({
    url: 'CrmReceivables/queryById',
    method: 'post',
    data: data
  })
}

/**
 * 回款计划列表
 * @param {*} data
 * page 页码
 * limit 每页数量
 * search 普通搜索
 */
export function crmReceivablesPlanIndex(data) {
  return request({
    url: 'CrmCustomer/queryReceivablesPlan',
    method: 'post',
    data: data
  })
}

/**
 * 回款计划删除
 * @param {*} data
 */
export function crmReceivablesPlanDeleteAPI(data) {
  return request({
    url: 'Crm/ReceivablesPlan/deleteByIds',
    method: 'post',
    data: data
  })
}

/**
 * 回款审核
 * @param {*} data
 */
export function crmReceivablesCheck(data) {
  return request({
    url: 'crm/receivables/check',
    method: 'post',
    data: data
  })
}

/**
 * 回款撤回审核
 * @param {*} data
 */
export function crmReceivablesRevokeCheck(data) {
  return request({
    url: 'crm/receivables/revokeCheck',
    method: 'post',
    data: data
  })
}

/**
 * 导出
 * @param {*} data
 */
export function crmReceivablesExcelExport(data) {
  return request({
    url: 'CrmReceivables/batchExportExcel',
    method: 'post',
    data: data,
    responseType: 'blob'
  })
}

export function crmReceivablesExcelAllExport(data) {
  return request({
    url: 'CrmReceivables/allExportExcel',
    method: 'post',
    data: data,
    responseType: 'blob',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
