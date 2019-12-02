import request from '@/utils/request'

export function roleListFun(data) {
  return request({
    url: 'system/role/getAllRoleList',
    method: 'post',
    data: data
  })
}

export function rulesList(data) {
  return request({
    url: 'system/menu/getAllMenuList',
    method: 'post',
    data: data
  })
}

export function roleAdd(data) {
  return request({
    url: 'system/role/add',
    method: 'post',
    data: data
  })
}

export function roleDelete(data) {
  return request({
    url: 'system/role/delete',
    method: 'post',
    data: data
  })
}

export function roleUpdate(data) {
  return request({
    url: 'system/role/update',
    method: 'post',
    data: data
  })
}
export function updateRoleMenu(data) {
  return request({
    url: 'system/role/updateRoleMenu',
    method: 'post',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    data: data
  })
}

// 角色复制
export function roleCopy(data) {
  return request({
    url: 'system/role/copy',
    method: 'post',
    data: data
  })
}

// 添加编辑员工
export function usersEdit(data) {
  return request({
    url: 'system/role/relatedUser',
    method: 'post',
    data: data
  })
}

// 删除员工
export function usersDelete(data) {
  return request({
    url: 'system/role/unbindingUser',
    method: 'post',
    data: data
  })
}

// 角色分类列表
export function adminGroupsTypeListAPI(data) {
  return request({
    url: 'system/role/getRoleTypeList',
    method: 'post',
    data: data
  })
}

// 参数 roleType   根据角色类型查询角色列表
export function systemRoleByTypeAPI(data) {
  return request({
    url: 'system/role/getRoleByType',
    method: 'post',
    data: data
  })
}

// 规则
export function systemRuleByTypeAPI(data) {
  return request({
    url: 'system/menu/getMenuListByType',
    method: 'post',
    data: data
  })
}
