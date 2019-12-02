import request from '@/utils/request'

/**
 * 归档项目列表
 * @param {*} data
 */
export function workWorkArchiveListAPI(data) {
  return request({
    url: 'work/queryArchiveWorkList',
    method: 'post',
    data: data
  })
}
