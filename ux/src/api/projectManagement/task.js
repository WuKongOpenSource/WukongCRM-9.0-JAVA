import request from '@/utils/request'

/**
 * 我的任务列表
 * @param {*} data
 */
export function workTaskMyTaskAPI(data) {
  return request({
    url: 'workbench/myTask',
    method: 'post',
    data: data
  })
}

/**
 * 新增任务
 * @param {*} data
 */
export function workTaskSaveAPI(data) {
  return request({
    url: 'task/setTask',
    method: 'post',
    data: data
  })
}

/**
 * 删除任务
 * @param {*} data
 */
export function workTaskDeleteAPI(data) {
  return request({
    url: 'task/deleteTask',
    method: 'post',
    data: data
  })
}

/**
 * 拖拽改变分类
 * @param {*} data
 */
export function workTaskUpdateTopAPI(data) {
  return request({
    url: 'workbench/updateTop',
    method: 'post',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    data: data
  })
}

/**
 * 项目列表
 * @param {*} data
 */
export function workIndexWorkListAPI(data) {
  return request({
    url: 'work/queryWorkNameList',
    method: 'post',
    data: data
  })
}

/**
 * 任务详情
 * @param {*} data
 */
export function workTaskReadAPI(data) {
  return request({
    url: 'task/queryTaskInfo',
    method: 'post',
    data: data
  })
}

/**
 * 编辑任务名
 * @param {*} data
 */
export function workTaskUpdateNameAPI(data) {
  return request({
    url: 'work/task/updateName',
    method: 'post',
    data: data
  })
}

/**
 * 任务归档
 * @param {*} data
 */
export function workTaskArchiveAPI(data) {
  return request({
    url: 'task/archiveByTaskId',
    method: 'post',
    data: data
  })
}

/**
 * 操作记录
 * @param {*} data
 */
export function workTaskReadLoglistAPI(data) {
  return request({
    url: 'task/queryWorkTaskLog',
    method: 'post',
    data: data
  })
}

/**
 * 任务评论添加
 * @param {*} data
 */
export function workTaskcommentSaveAPI(data) {
  return request({
    url: 'comment/setComment',
    method: 'post',
    data
  })
}

/**
 * 任务评论删除
 * @param {*} data
 */
export function workTaskcommentDeleteAPI(data) {
  return request({
    url: 'comment/deleteComment',
    method: 'post',
    data
  })
}

/**
 * 归档任务激活
 * @param {*} data
 */
export function workTaskRecoverAPI(data) {
  return request({
    url: 'work/activation',
    method: 'post',
    data
  })
}

/**
 * 任务评论
 * @param {*} data
 */
export function queryCommentListAPI(data) {
  return request({
    url: 'comment/queryCommentList',
    method: 'post',
    data
  })
}

/**
 * 编辑关联业务
 * @param {*} data
 */
export function taskSvaeTaskRelationAPI(data) {
  return request({
    url: 'task/saveTaskRelation',
    method: 'post',
    data: data
  })
}
