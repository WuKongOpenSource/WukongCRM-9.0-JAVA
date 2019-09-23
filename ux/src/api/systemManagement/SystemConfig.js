import request from '@/utils/request'

// 企业首页
export function adminSystemSave(data) {
  return request({
    url: 'sysConfig/setSysConfig',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 企业首页
export function adminSystemIndex(data) {
  return request({
    url: 'sysConfig/querySysConfig',
    method: 'post',
    data: data
  })
}
