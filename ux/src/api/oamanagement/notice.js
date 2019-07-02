import request from '@/utils/request'

// 公告添加
export function noticeList(data) {
  return request({
    url: 'OaAnnouncement/queryList',
    method: 'post',
    data: data
  })
}
// 公告添加
export function noticeAdd(data) {
  return request({
    url: 'OaAnnouncement/saveAndUpdate',
    method: 'post',
    data: data
  })
}

// 公告删除
export function noticeDelete(data) {
  return request({
    url: 'OaAnnouncement/delete',
    method: 'post',
    data: data
  })
}

/**
 * 公告设为已读
 * @param {*} data
 */
export function noticeIsReadAPI(data) {
  return request({
    url: 'OaAnnouncement/readAnnouncement',
    method: 'post',
    data: data
  })
}
