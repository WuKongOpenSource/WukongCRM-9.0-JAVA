import request from '@/utils/request'

/**
 *
 * @param {*} data
 */
export function usersList(data) {
  return request({
    url: 'system/user/queryUserList',
    method: 'post',
    data: data
  })
}

/**
 * type
 * @param {*} data
 */
export function depList(data) {
  return request({
    url: 'system/dept/queryDeptTree',
    method: 'post',
    data: data
  })
}

// 获取权限范围内部门接口
export function adminStructuresSubIndex(data) {
  return request({
    url: 'system/dept/queryDeptByAuth',
    method: 'post',
    data: data
  })
}
// 获取权限范围内部门接口
export function getUserByDeptId(data) {
  return request({
    url: 'system/user/queryUserByDeptId',
    method: 'post',
    data: data
  })
}


// 部门下的员工
export function userListByStructid(data) {
  return request({
    url: 'admin/users/userListByStructid',
    method: 'post',
    data: data
  })
}

/**
 *
 * @param {*} data
 */
export const crmFileSaveUrl = process.env.BASE_API + 'file/upload'

export function crmFileSave(data) {
  var param = new FormData()
  Object.keys(data).forEach(key => {
    param.append(key, data[key])
  })
  return request({
    url: 'file/upload',
    method: 'post',
    data: param,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function crmFileFormDataSave(data) {
  return request({
    url: 'file/upload',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 附件列表
export function crmFileIndex(data) {
  return request({
    url: 'file/queryByBatchId',
    method: 'post',
    data: data
  })
}

/** 暂时客户管理里面也有 */
// 附件删除接口
export function crmFileDelete(data) {
  return request({
    url: 'file/removeById',
    method: 'post',
    data: data
  })
}

// 附件重命名
export function crmFileUpdate(data) {
  return request({
    url: 'file/renameFileById',
    method: 'post',
    data: data
  })
}

// crm 自定义字段的添加
/**
 *
 * @param {*} data
 */
export function filedGetField(data) {
  return request({
    url: 'field/queryField',
    method: 'post',
    data: data
  })
}

// 权限数据返回
export function adminIndexAuthList(data) {
  return request({
    url: 'system/role/auth',
    method: 'post',
    data: data
  })
}
