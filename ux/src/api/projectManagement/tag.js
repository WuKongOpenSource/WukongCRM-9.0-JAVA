import request from '@/utils/request'

/**
 * 标签左侧列表
 * @param {*} data
 */
export function workTasklableIndexAPI(data) {
    return request({
        url: 'taskLabel/getLabelList',
        method: 'post',
        data: data
    })
}

/**
 * 单个标签详情
 * @param {*} data
 */
export function workTasklableReadAPI(data) {
    return request({
        url: 'taskLabel/queryById',
        method: 'post',
        data: data
    })
}

/**
 * 标签删除
 * @param {*} data
 */
export function workTasklableDeleteAPI(data) {
    return request({
        url: 'taskLabel/deleteLabel',
        method: 'post',
        data: data
    })
}

/**
 * 标签编辑
 * @param {*} data
 */
export function workTasklableSaveAPI(data) {
    return request({
        url: 'taskLabel/setLabel',
        method: 'post',
        data: data
    })
}

/**
 * 获取项目及任务表
 * @param {*} data
 */
export function workTasklableGetWokListAPI(data) {
    return request({
        url: 'taskLabel/getTaskList',
        method: 'post',
        data: data
    })
}

