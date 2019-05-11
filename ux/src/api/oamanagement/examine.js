import request from '@/utils/request'

// 审批类型列表
export function oaExamineCategoryList(data) {
  return request({
    url: 'OaExamineCategory/queryAllExamineCategoryList',
    method: 'post',
    data: data
  })
}

// 审批新建
export function oaExamineSaveAndUpdate(data) {
  return request({
    url: 'OaExamine/setOaExamine',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

// 我发起的审批
export function oaExamineMyCreateIndex(data) {
  return request({
    url: 'OaExamine/myInitiate',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

// 我审批的
export function oaExamineMyExamineIndex(data) {
  return request({
    url: 'OaExamine/myOaExamine',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

// 审批删除
export function oaExamineDelete(data) {
  return request({
    url: 'OaExamine/deleteOaExamine',
    method: 'post',
    data: data
  })
}

// 审批详情
export function oaExamineRead(data) {
  return request({
    url: 'OaExamine/queryOaExamineInfo',
    method: 'post',
    data: data
  })
}

//新建的审批流
// CRM合同回款创建时候的审批流
export function oaCreateExamineFlow(data) {
  return request({
    url: 'OaExamine/queryExaminStep',
    method: 'post',
    data: data
  })
}

// 审批详情 基本信息
export function OaExamineGetField(data) {
  return request({
    url: 'OaExamine/getField',
    method: 'post',
    data: data
  })
}

// 办公下 审批的审批信息
export function oaExamineFlowStepList(data) {
  return request({
    url: 'OaExamine/queryExamineRecordList',
    method: 'post',
    data: data
  })
}

/**
 * 审批记录
 * @param {*} data
 */
export function oaExamineFlowRecordList(data) {
  return request({
    url: 'OaExamine/queryExamineLogList',
    method: 'post',
    data: data
  })
}

// 审批的审核
export function oaExamineFlowAuditExamine(data) {
  return request({
    url: 'OaExamine/auditExamine',
    method: 'post',
    data: data
  })
}
