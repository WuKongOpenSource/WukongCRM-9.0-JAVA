import request from '@/utils/request'

// 审批流程列表
export function examineFlowIndex(data) {
  return request({
    url: 'examine/queryAllExamine',
    method: 'post',
    data: data
  })
}

// 审批流程创建
export function examineFlowSave(data) {
  return request({
    url: 'examine/saveExamine',
    method: 'post',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    data: data
  })
}

/**
 * 审批流程删除 启用 禁用
 * @param {*} data
 * id 审批流ID
 */
export function examineFlowUpdateStatus(data) {
  return request({
    url: 'examine/updateStatus',
    method: 'post',
    data: data
  })
}
