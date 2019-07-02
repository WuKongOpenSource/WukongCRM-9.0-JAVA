import request from '@/utils/request'

// 工作圈列表
export function workbenchList(data) {
    return request({
        url: 'OaRecord/getOaRecordPageList',
        method: 'post',
        data: data
    })
}
// 工作圈列表
export function eventList(data) {
    return request({
        url: 'OaRecord/queryEvent',
        method: 'post',
        data: data
    })
}
// 根据时间查日程
export function scheduleDayList(data) {
    return request({
        url: 'OaRecord/queryEventByDay',
        method: 'post',
        data: data
    })
}
// 工作圈列表
export function taskListAPI(data) {
    return request({
        url: 'OaRecord/queryTask',
        method: 'post',
        data: data
    })
}

/**
 * 待办消息数
 * @param {*} data 
 */
export function oaMessageNumAPI(data) {
    return request({
        url: 'OaBackLog/num',
        method: 'post',
        data: data
    })
}

