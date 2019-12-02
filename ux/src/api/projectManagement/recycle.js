import request from '@/utils/request'

/**
 * 回收站列表
 * @param {*} data
 */
export function workTrashIndexAPI(data) {
  return request({
    url: 'workTrash/queryList',
    method: 'post',
    data: data
  })
}

/**
 * 回收站彻底删除
 * @param {*} data
 */
export function workTrashDeleteAPI(data) {
  return request({
    url: 'workTrash/deleteTask',
    method: 'post',
    data: data
  })
}

/**
 * 回收站恢复
 * @param {*} data
 */
export function workTrashRecoverAPI(data) {
  return request({
    url: 'workTrash/restore',
    method: 'post',
    data: data
  })
}



