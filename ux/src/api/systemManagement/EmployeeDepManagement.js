import request from '@/utils/request'

export function depDelete(data) {
  return request({
    url: 'system/dept/deleteDept',
    method: 'post',
    data: data
  })
}

export function depEdit(data) {
  return request({
    url: 'system/dept/updateDept',
    method: 'post',
    data: data
  })
}

export function depSave(data) {
  return request({
    url: 'system/dept/addDept',
    method: 'post',
    data: data
  })
}

export function usersEdit(params) {
  return request({
    url: 'system/user/setUser',
    method: 'post',
    data: params
  })
}

export function usersAdd(params) {
  return request({
    url: 'system/user/addUser',
    method: 'post',
    data: params
  })
}

// 角色列表
export function roleList(data) {
  return request({
    url: 'system/role/getAllRoleList',
    method: 'post',
    data: data
  })
}

// 重置密码
export function resetPassword(data) {
  return request({
    url: 'system/user/resetPassword',
    method: 'post',
    data: data
  })
}

/**
 * 批量修改密码接口
 * @param {*} data
 * password
 * id 用户数组
 */
export function adminUsersUpdatePwd(data) {
  return request({
    url: 'system/user/resetPassword',
    method: 'post',
    data: data
  })
}

/**
 * 编辑登录名
 * @param {*} data
 * username
 * password
 * id
 */
export function adminUsersUsernameEditAPI(data) {
  return request({
    url: 'system/user/usernameEdit',
    method: 'post',
    data: data
  })
}

// 用户状态修改
export function usersEditStatus(data) {
  return request({
    url: 'system/user/setUserStatus',
    method: 'post',
    data: data
  })
}
