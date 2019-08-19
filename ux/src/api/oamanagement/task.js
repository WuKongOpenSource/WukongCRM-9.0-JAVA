import request from '@/utils/request'

// 我下属的任务列表
export function taskListAPI(data) {
  return request({
    url: 'task/queryTaskList',
    method: 'post',
    data: data
  })
}
/********** oA任务 ********/
// 新增任务
export function addTask(data) {
  return request({
    url: 'task/setTask',
    method: 'post',
    data: data
  })
}
// 删除任务
export function deleteTask(data) {
  return request({
    url: 'task/deleteTask',
    method: 'post',
    data: data
  })
}
// 任务详情
export function detailsTask(data) {
  return request({
    url: 'task/queryTaskInfo',
    method: 'post',
    data: data
  })
}
// 任务编辑 -- 详情页总编辑
export function editTask(data) {
  return request({
    url: 'task/setTask',
    method: 'post',
    data: data
  })
}

// 编辑关联业务
export function editTaskRelation(data) {
  return request({
    url: 'task/svaeTaskRelation',
    method: 'post',
    data: data
  })
}

// 创建标签
export function createTag(data) {
  return request({
    url: 'taskLabel/setLabel',
    method: 'post',
    data: data
  })
}
// 编辑标签
export function editTagAPI(data) {
  return request({
    url: 'taskLabel/setLabel',
    method: 'post',
    data: data
  })
}
// 删除标签
export function deleteTagAPI(data) {
  return request({
    url: 'taskLabel/deleteLabel',
    method: 'post',
    data: data
  })
}
// 标签列表
export function tagList(data) {
  return request({
    url: 'taskLabel/getLabelList',
    method: 'post',
    data: data
  })
}

// 操作记录
export function readLoglist(data) {
  return request({
    url: 'task/queryWorkTaskLog',
    method: 'post',
    data: data
  })
}
