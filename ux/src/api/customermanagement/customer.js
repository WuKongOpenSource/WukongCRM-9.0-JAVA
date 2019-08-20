import request from '@/utils/request'

// crm 新建客户
export function crmCustomerSave(data) {
  return request({
    url: 'CrmCustomer/addOrUpdate',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

// crm 客户列表
export function crmCustomerIndex(data) {
  return request({
    url: 'CrmCustomer/queryPageList',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

// 删除
export function crmCustomerDelete(data) {
  return request({
    url: 'CrmCustomer/deleteByIds',
    method: 'post',
    data: data
  })
}

// crm 公海列表
export function crmCustomerPool(data) {
  return request({
    url: 'CrmCustomer/queryPoolPageList',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

// crm 详情
export function crmCustomerRead(data) {
  return request({
    url: 'CrmCustomer/queryById',
    method: 'post',
    data: data
  })
}

// 操作
/**
 * 客户锁定，解锁
 * @param {*} data
 */
export function crmCustomerLock(data) {
  return request({
    url: 'CrmCustomer/lock',
    method: 'post',
    data: data
  })
}

/**
 * 客户放入公海
 * @param {*} data
 * customer_id 	客户数组
 */
export function crmCustomerPutInPool(data) {
  return request({
    url: 'CrmCustomer/updateCustomerByIds',
    method: 'post',
    data: data
  })
}

/**
 * 客户转移
 * @param {*} data
 */
export function crmCustomerTransfer(data) {
  return request({
    url: 'CrmCustomer/transfer',
    method: 'post',
    data: data
  })
}

/**
 * 客户导出
 * @param {*} data
 * customer_id 客户ID
 */
export function crmCustomerExcelExport(data) {
  return request({
    url: 'CrmCustomer/batchExportExcel',
    method: 'post',
    data: data,
    responseType: 'blob'
  })
}

export function crmCustomerExcelAllExport(data) {
  return request({
    url: 'CrmCustomer/allExportExcel',
    method: 'post',
    data: data,
    responseType: 'blob',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 客户导入
 * @param {*} data
 * customer_id 客户ID
 */
export function crmCustomerExcelImport(data) {
  var param = new FormData()
  Object.keys(data).forEach(key => {
    param.append(key, data[key])
  })
  return request({
    url: 'CrmCustomer/uploadExcel',
    method: 'post',
    data: param,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 客户导入模板下载
 * @param {*} data
 *
 */
export const crmCustomerExcelDownloadURL = process.env.BASE_API + 'CrmCustomer/downloadExcel'
export function crmCustomerDownloadExcelAPI(data) {
  return request({
    url: 'CrmCustomer/downloadExcel',
    method: 'post',
    data: data,
    responseType: 'blob'
  })
}

/**
 * 公海导出
 * @param {*} data
 */
export function crmCustomerPoolExcelExportAPI(data) {
  return request({
    url: 'CrmCustomer/poolBatchExportExcel',
    method: 'post',
    data: data,
    responseType: 'blob',
    timeout: 60000
  })
}

export function crmCustomerPoolExcelAllExport(data) {
  return request({
    url: 'CrmCustomer/poolAllExportExcel',
    method: 'post',
    data: data,
    responseType: 'blob',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 客户分配
 * @param {*} data
 */
export function crmCustomerDistribute(data) {
  return request({
    url: 'CrmCustomer/distributeByIds',
    method: 'post',
    data: data
  })
}

/**
 * 客户领取
 * @param {*} data
 */
export function crmCustomerReceive(data) {
  return request({
    url: 'CrmCustomer/receiveByIds',
    method: 'post',
    data: data
  })
}

/**
 * 客户下联系人
 * @param {*} data 
 */
export function crmCustomerQueryContacts(data) {
  return request({
    url: 'CrmCustomer/queryContacts',
    method: 'post',
    data: data
  })
}

/**
 * 客户下商机
 * @param {*} data 
 */
export function crmCustomerQueryBusiness(data) {
  return request({
    url: 'CrmCustomer/queryBusiness',
    method: 'post',
    data: data
  })
}

/**
 * 客户下合同
 * @param {*} data 
 */
export function crmCustomerQueryContract(data) {
  return request({
    url: 'CrmCustomer/queryContract',
    method: 'post',
    data: data
  })
}

/**
 * 客户下回款计划
 * @param {*} data 
 */
export function crmCustomerQueryReceivablesPlan(data) {
  return request({
    url: 'CrmCustomer/queryReceivablesPlan',
    method: 'post',
    data: data
  })
}

/**
 * 客户下回款
 * @param {*} data 
 */
export function crmCustomerQueryReceivables(data) {
  return request({
    url: 'CrmCustomer/queryReceivables',
    method: 'post',
    data: data
  })
}


/**
 * 跟进记录
 * @param {*} data 
 */
export function crmCustomerRecordSave(data) {
  return request({
    url: 'CrmCustomer/addRecord',
    method: 'post',
    data: data
  })
}
export function crmCustomerRecordIndex(data) {
  return request({
    url: 'CrmCustomer/getRecord',
    method: 'post',
    data: data
  })
}
// 团队操作

/**
 * 相关团队创建
 * @param {*} data
 * types crm_leads
 * typesId 分类ID
 */
export function crmCustomerSettingTeamSave(data) {
  return request({
    url: 'CrmCustomer/addMembers',
    method: 'post',
    data: data
  })
}

export function crmCustomerSettingTeamDelete(data) {
  return request({
    url: 'CrmCustomer/deleteMembers',
    method: 'post',
    data: data
  })
}

export function crmCustomerTeamMembers(data) {
  return request({
    url: 'CrmCustomer/getMembers',
    method: 'post',
    data: data
  })
}

export function crmCustomerUpdateMembers(data) {
  return request({
    url: 'CrmCustomer/updateMembers',
    method: 'post',
    data: data
  })
}

/**
 * 查重
 * @param {*} data
 */
export function crmCustomerQueryListAPI(data) {
  return request({
    url: 'CrmCustomer/queryList',
    method: 'post',
    data: data
  })
}

/**
 * 客户标记跟进
 * @param {*} data
 * id 客户IDs
 */
export function crmCustomerSetFollowAPI(data) {
  return request({
    url: 'CrmBackLog/setCustomerFollowup',
    method: 'post',
    data: data
  })
}
