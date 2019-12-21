import request from '@/utils/request'

// crm 新建合同
export function crmContractSave(data) {
  return request({
    url: 'CrmContract/saveAndUpdate',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

// crm 列表
export function crmContractIndex(data) {
  return request({
    url: 'CrmContract/queryPageList',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

// 删除
export function crmContractDelete(data) {
  return request({
    url: 'CrmContract/deleteByIds',
    method: 'post',
    data: data
  })
}

// crm 详情
export function crmContractRead(data) {
  return request({
    url: 'CrmContract/queryById',
    method: 'post',
    data: data
  })
}

/**
 * 回款计划创建
 * @param {*} data
 */
export function crmReceivablesPlanSave(data) {
  return request({
    url: 'Crm/ReceivablesPlan/saveAndUpdate',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 合同相关产品
 * @param {*} data
 * contract_id
 */
export function crmContractProduct(data) {
  return request({
    url: 'CrmContract/qureyProductListByContractId',
    method: 'post',
    data: data
  })
}

// 转移
export function crmContractTransfer(data) {
  return request({
    url: 'CrmContract/transfer',
    method: 'post',
    data: data
  })
}
// 作废合同 CrmContract/contractDiscard
export function CrmContractContractDiscard(data) {
  return request({
    url: 'CrmContract/contractDiscard',
    method: 'post',
    data: data
  })
}
/**
 * 合同下回款
 * @param {*} data
 */
export function crmContractQueryReceivables(data) {
  return request({
    url: 'CrmContract/qureyReceivablesListByContractId',
    method: 'post',
    data: data
  })
}

/**
 * 合同下回款计划
 * @param {*} data
 */
export function crmContractQueryReceivablesPlan(data) {
  return request({
    url: 'CrmContract/qureyReceivablesPlanListByContractId',
    method: 'post',
    data: data
  })
}

/**
 * 跟进记录
 * @param {*} data
 */
export function crmContractRecordSave(data) {
  return request({
    url: 'CrmContract/addRecord',
    method: 'post',
    data: data
  })
}
export function crmContractRecordIndex(data) {
  return request({
    url: 'CrmContract/getRecord',
    method: 'post',
    data: data
  })
}
// 团队操作

/**
 * 相关团队创建
 * @param {*} data
 */
export function crmContractSettingTeamSave(data) {
  return request({
    url: 'CrmContract/addMembers',
    method: 'post',
    data: data
  })
}

export function crmContractSettingTeamDelete(data) {
  return request({
    url: 'CrmContract/deleteMembers',
    method: 'post',
    data: data
  })
}

export function crmContractTeamMembers(data) {
  return request({
    url: 'CrmContract/getMembers',
    method: 'post',
    data: data
  })
}

export function crmContractUpdateMembers(data) {
  return request({
    url: 'CrmContract/updateMembers',
    method: 'post',
    data: data
  })
}

/**
 * 新建回款查询回款计划
 * @param {*} data
 */
export function crmQueryReceivablesPlansByContractId(data) {
  return request({
    url: 'CrmContract/queryReceivablesPlansByContractId',
    method: 'post',
    data: data
  })
}

/**
 * 导出
 * @param {*} data
 */
export function crmContractExcelExport(data) {
  return request({
    url: 'CrmContract/batchExportExcel',
    method: 'post',
    data: data,
    responseType: 'blob'
  })
}

/**
 * 导出
 * @param {*} data
 */
export function crmContractExcelAllExport(data) {
  return request({
    url: 'CrmContract/allExportExcel',
    method: 'post',
    data: data,
    responseType: 'blob',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
