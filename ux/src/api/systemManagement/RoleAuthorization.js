import request from '@/utils/request'


export function roleListFun(data) {
  return request({
    url: 'manager/role/getAllRoleList',
    method: 'post',
    data: data
  })
}

export function rulesList(data) {
  return request({
    url: 'manager/menu/getAllMenuList',
    method: 'post',
    data: data
  })
}

export function roleAdd(data) {
  return request({
    url: 'manager/role/add',
    method: 'post',
    data: data
  })
}

export function roleDelete(data) {
  return request({
    url: 'manager/role/delete',
    method: 'post',
    data: data
  })
}

export function roleUpdate(data) {
  return request({
    url: 'manager/role/update',
    method: 'post',
    data: data
  })
}
export function updateRoleMenu(data) {
  return request({
    url: 'manager/role/updateRoleMenu',
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
    url: 'manager/role/copy',
    method: 'post',
    data: data
  })
}

// 添加编辑员工
export function usersEdit(data) {
  return request({
    url: 'manager/role/relatedUser',
    method: 'post',
    data: data
  })
}

// 删除员工
export function usersDelete(data) {
  return request({
    url: 'manager/role/unbindingUser',
    method: 'post',
    data: data
  })
}
