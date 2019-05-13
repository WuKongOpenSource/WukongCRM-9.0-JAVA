import request from '@/utils/request'

// 审批类型列表
export function addresslist(data) {
  return request({
    url: 'system/user/queryListName',
    method: 'post',
    data: data
  })
}

// 通讯录部门列表
export function queryListNameByDept(data) {
  return request({
    url: 'system/user/queryListNameByDept',
    method: 'post',
    data: data
  })
}
