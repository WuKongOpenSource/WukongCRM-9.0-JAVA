import request from '@/utils/request'

// crm 新建产品
export function crmProductSave(data) {
  return request({
    url: 'CrmProduct/saveAndUpdate',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

// crm 列表
export function crmProductIndex(data) {
  return request({
    url: 'CrmProduct/queryList',
    method: 'post',
    data: data
  })
}

// crm 详情
export function crmProductRead(data) {
  return request({
    url: 'CrmProduct/queryById',
    method: 'post',
    data: data
  })
}

/**
 * 产品上架、下架
 * @param {*} data
 */
export function crmProductStatus(data) {
  return request({
    url: 'CrmProduct/updateStatus',
    method: 'post',
    data: data
  })
}
