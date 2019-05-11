import request from '@/utils/request'

// 审批类型列表
export function oaExamineCategory(data) {
  return request({
    url: 'OaExamineCategory/queryExamineCategoryList',
    method: 'post',
    data: data
  })
}

/**
 * 审批类型的创建
 * @param {*} data
 */
export function oaExamineCategorySave(data) {
  return request({
    url: 'OaExamineCategory/setExamineCategory',
    method: 'post',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    data: data
  })
}

/**
 * 审批删除
 * @param {*} data
 */
export function oaExamineCategoryDelete(data) {
  return request({
    url: 'OaExamineCategory/deleteExamineCategory',
    method: 'post',
    data: data
  })
}

/**
 * 审批状态（启用、停用）
 * @param {*} data
 */
export function oaExamineCategoryEnables(data) {
  return request({
    url: 'OaExamineCategory/updateStatus',
    method: 'post',
    data: data
  })
}
