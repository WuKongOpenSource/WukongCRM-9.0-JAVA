import request from '@/utils/request'

/**
 * 修改头像
 * @param {*} data
 */
export function adminUsersUpdateImg(data) {
  return request({
    url: 'system/user/updateImg',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 个人详情
 * @param {*} data
 */
export function adminUsersRead(data) {
  return request({
    url: 'system/user/queryLoginUser',
    method: 'post',
    data: data
  })
}

/**
 * 修改个人信息
 * @param {*} data
 */
export function adminUsersUpdate(data) {
  return request({
    url: 'system/user/updateUser',
    method: 'post',
    data: data
  })
}

/**
 * 修改密码
 * @param {*} data
 */
export function adminUsersResetPassword(data) {
  return request({
    url: 'system/user/updatePassword',
    method: 'post',
    data: data
  })
}
