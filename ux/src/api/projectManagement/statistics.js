import request from '@/utils/request'

/**
 * 项目任务统计
 * @param {*} data
 */
export function workWorkStatisticAPI(data) {
  return request({
    url: 'work/workStatistics',
    method: 'post',
    data: data
  })
}
