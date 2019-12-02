import request from '@/utils/request'

// crm 新建线索
export function crmLeadsSave(data) {
  return request({
    url: 'CrmLeads/addOrUpdate',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

// crm 列表
export function crmLeadsIndex(data) {
  return request({
    url: 'CrmLeads/queryPageList',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

// 删除
export function crmLeadsDelete(data) {
  return request({
    url: 'CrmLeads/deleteByIds',
    method: 'post',
    data: data
  })
}

// crm 更新
export function crmLeadsUpdate(data) {
  return request({
    url: 'crm/leads/update',
    method: 'post',
    data: data
  })
}

// crm 详情
export function crmLeadsRead(data) {
  return request({
    url: 'CrmLeads/queryById',
    method: 'post',
    data: data
  })
}

/**
 * 线索转移
 * @param {*} data
 */
export function crmLeadsTransfer(data) {
  return request({
    url: 'CrmLeads/changeOwnerUser',
    method: 'post',
    data: data
  })
}

/**
 * 线索转换为客户
 * @param {*} data
 */
export function crmLeadsTransform(data) {
  return request({
    url: 'CrmLeads/transfer',
    method: 'post',
    data: data
  })
}

/**
 * 线索导出
 * @param {*} data
 *
 */
export function crmLeadsExcelExport(data) {
  return request({
    url: 'CrmLeads/batchExportExcel',
    method: 'post',
    data: data,
    responseType: 'blob'
  })
}

export function crmLeadsExcelAllExport(data) {
  return request({
    url: 'CrmLeads/allExportExcel',
    method: 'post',
    data: data,
    responseType: 'blob'
  })
}

/**
 * 线索导入
 * @param {*} data
 *
 */
export function crmLeadsExcelImport(data) {
  var param = new FormData()
  Object.keys(data).forEach(key => {
    param.append(key, data[key])
  })
  return request({
    url: 'CrmLeads/uploadExcel',
    method: 'post',
    data: param,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 线索导入模板下载
 * @param {*} data
 *
 */
export const crmLeadsExcelDownloadURL = process.env.BASE_API + 'CrmLeads/downloadExcel'
export function crmLeadsDownloadExcelAPI(data) {
  return request({
    url: 'CrmLeads/downloadExcel',
    method: 'post',
    data: data,
    responseType: 'blob'
  })
}

/**
 * 跟进记录
 * @param {*} data
 */
export function crmLeadsRecordSave(data) {
  return request({
    url: 'CrmLeads/addRecord',
    method: 'post',
    data: data
  })
}
export function crmLeadsRecordIndex(data) {
  return request({
    url: 'CrmLeads/getRecord',
    method: 'post',
    data: data
  })
}

/**
 * 查重
 * @param {*} data
 */
export function crmLeadsQueryListAPI(data) {
  return request({
    url: 'CrmLeads/queryList',
    method: 'post',
    data: data
  })
}

/**
 * 线索标记跟进
 * @param {*} data
 * id 客户IDs
 */
export function crmLeadsSetFollowAPI(data) {
  return request({
    url: 'CrmBackLog/setLeadsFollowup',
    method: 'post',
    data: data
  })
}
