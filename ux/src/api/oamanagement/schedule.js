import request from '@/utils/request'

// 日程列表
export function scheduleList(data) {
  return request({
    url: 'OaEvent/queryList',
    method: 'post',
    data: data
  })
}
// 日程添加
export function scheduleAdd(data) {
  return request({
    url: 'OaEvent/add',
    method: 'post',
    data: data
  })
}
// 日程删除
export function scheduleDelete(data) {
  return request({
    url: 'OaEvent/delete',
    method: 'post',
    data: data
  })
}
// 日程编辑
export function scheduleEdit(data) {
  return request({
    url: 'OaEvent/update',
    method: 'post',
    data: data
  })
}
