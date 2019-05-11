import request from '@/utils/request'

// crm 新建联系人
export function crmContactsSave(data) {
  return request({
    url: 'CrmContacts/addOrUpdate',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

// crm 列表
export function crmContactsIndex(data) {
  return request({
    url: 'CrmContacts/queryList',
    method: 'post',
    data: data
  })
}

// 删除
export function crmContactsDelete(data) {
  return request({
    url: 'CrmContacts/deleteByIds',
    method: 'post',
    data: data
  })
}

// crm 详情
export function crmContactsRead(data) {
  return request({
    url: 'CrmContacts/queryById',
    method: 'post',
    data: data
  })
}

/**
 * 联系人转移
 * @param {*} data
 */
export function crmContactsTransfer(data) {
  return request({
    url: 'CrmContacts/transfer',
    method: 'post',
    data: data
  })
}

/**
 * 联系人下商机
 * @param {*} data 
 */
export function crmContactsQueryBusiness(data) {
  return request({
    url: 'CrmContacts/queryBusiness',
    method: 'post',
    data: data
  })
}

/**
 * 跟进记录
 * @param {*} data 
 */
export function crmContactsRecordSave(data) {
  return request({
    url: 'CrmContacts/addRecord',
    method: 'post',
    data: data
  })
}
export function crmContactsRecordIndex(data) {
  return request({
    url: 'CrmContacts/getRecord',
    method: 'post',
    data: data
  })
}
