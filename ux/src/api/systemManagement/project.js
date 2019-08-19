import request from '@/utils/request'

// 列表
export function systemRoleQueryProjectRoleListAPI(data) {
  return request({
    url: 'system/role/queryProjectRoleList',
    method: 'post',
    data: data
  })
}

/**
 * 角色
 * @param {*} data
 */
export function systemMenuGetWorkMenuListAPI(data) {
  return request({
    url: 'system/menu/getWorkMenuList',
    method: 'post',
    data: data
  })
}

/**
 * 设置
 * @param {*} data
 */
export function systemRoleSetWorkRoleAPI(data) {
  return request({
    url: 'system/role/setWorkRole',
    method: 'post',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    data: data
  })
}

/**
 * 删除项目角色
 */
export function systemRoleDeleteWorkRoleAPI(data) {
  return request({
    url: 'system/role/deleteWorkRole',
    method: 'post',
    data: data
  })
}

