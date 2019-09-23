import request from '@/utils/request'

/**
 * 应用列表接口
 * @param {*} data
 *
 */
export function adminConfigsetIndex(data) {
  return request({
    url: 'sysConfig/queryModuleSetting',
    method: 'post',
    data: data
  })
}

/**
 * 应用状态改变
 * @param {*} data
 * id 应用ID
 * status 1开启 0关闭
 */
export function adminConfigsetUpdate(data) {
  return request({
    url: 'sysConfig/setModuleSetting',
    method: 'post',
    data: data
  })
}
