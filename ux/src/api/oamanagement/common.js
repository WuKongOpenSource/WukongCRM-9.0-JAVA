import request from '@/utils/request'

// 任务评论添加
export function setCommentAPI(data) {
  return request({
    url: 'comment/setComment',
    method: 'post',
    data
  })
}

export function deleteCommentAPI(data) {
  return request({
    url: 'comment/deleteComment',
    method: 'post',
    data
  })
}


export function queryCommentListAPI(data) {
  return request({
    url: 'comment/queryCommentList',
    method: 'post',
    data
  })
}
