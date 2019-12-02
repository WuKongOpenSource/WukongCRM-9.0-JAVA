import request from '@/utils/request'

/**
 * 日历任务
 * @param {*} data
 */
export function workTaskDateListAPI(data) {
  return request({
    url: 'workbench/dateList',
    method: 'post',
    data: data
  })
}
