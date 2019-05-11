import request from '@/utils/request'

// 日志列表
export function journalList(data) {
  return request({
    url: 'OaLog/queryList',
    method: 'post',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    data: data
  })
}
// 新建日志
export function journalAdd(data) {
  return request({
    url: 'OaLog/addOrUpdate',
    method: 'post',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    data
  })
}
// 日志编辑
export function journalEdit(data) {
  return request({
    url: 'OaLog/addOrUpdate',
    method: 'post',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    data
  })
}
// 日志评论添加
export function journalCommentSave(data) {
  return request({
    url: 'OaLog/addComment',
    method: 'post',
    data
  })
}
// 日志删除
export function journalDelete(data) {
  return request({
    url: 'OaLog/deleteById',
    method: 'post',
    data
  })
}
// 日志评论删除
export function journalCommentDelete(data) {
  return request({
    url: 'OaLog/delComment',
    method: 'post',
    data
  })
}
// 日志标记已读
export function journalSetread(data) {
  return request({
    url: 'OaLog/readLog',
    method: 'post',
    data
  })
}
