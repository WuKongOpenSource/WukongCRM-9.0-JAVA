import request from '@/utils/request'

// 系统配置
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

// 系统配置
export function adminSystemIndex(data) {
  return request({
    url: 'sysConfig/querySysConfig',
    method: 'post',
    data: data
  })
}
