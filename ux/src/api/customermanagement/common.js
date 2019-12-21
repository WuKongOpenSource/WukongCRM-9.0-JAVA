import request from '@/utils/request'

/**
 *crm 自定义字段的添加
 * @param {*} data
 */
export function filedGetField(data) {
  return request({
    url: 'field/queryField',
    method: 'post',
    data: data
  })
}

/**
 * 详情页基本信息
 * @param {*} data
 */
export function filedGetInformation(data) {
  return request({
    url: 'field/information',
    method: 'post',
    data: data
  })
}


/**
 * 表头
 * @param {*} data
 */
export function filedGetTableField(data) {
  return request({
    url: 'field/queryListHead',
    method: 'post',
    data: data
  })
}

// crm 自定义字段验重
export function filedValidates(data) {
  return request({
    url: 'field/verify',
    method: 'post',
    data: data
  })
}

// crm 自定义字段(高级筛选)
export function filterIndexfields(data) {
  return request({
    url: 'scene/queryField',
    method: 'post',
    data: data
  })
}

// 商机状态组列表 systemCustomer.js 也包含该接口
export function businessGroupList(data) {
  return request({
    url: 'businessType/queryBusinessTypeList',
    method: 'post',
    data: data
  })
}
// 场景列表
export function crmSceneIndex(data) {
  return request({
    url: 'scene/queryScene',
    method: 'post',
    data: data
  })
}
// 场景设置列表
export function crmSceneSetIndex(data) {
  return request({
    url: 'scene/querySceneConfig',
    method: 'post',
    data: data
  })
}

// 场景创建
export function crmSceneSave(data) {
  return request({
    url: 'scene/addScene',
    method: 'post',
    data: data
  })
}

// 场景编辑
export function crmSceneUpdate(data) {
  return request({
    url: 'scene/updateScene',
    method: 'post',
    data: data
  })
}

// 场景默认
export function crmSceneDefaults(data) {
  return request({
    url: 'scene/setDefaultScene',
    method: 'post',
    data: data
  })
}

// 场景删除
export function crmSceneDelete(data) {
  return request({
    url: 'scene/deleteScene',
    method: 'post',
    data: data
  })
}

// 场景排序
export function crmSceneSort(data) {
  return request({
    url: 'scene/sceneConfig',
    method: 'post',
    data: data
  })
}

// 列表字段排序数据
export function crmFieldConfigIndex(data) {
  return request({
    url: 'field/queryFieldConfig',
    method: 'post',
    data: data
  })
}

// 列表排序编辑
export function crmFieldConfig(data) {
  return request({
    url: 'field/fieldConfig',
    method: 'post',
    data: data
  })
}

// 列表宽度设置
export function crmFieldColumnWidth(data) {
  return request({
    url: 'field/setFelidStyle',
    method: 'post',
    data: data
  })
}

// 查询关联日志
export function crmQueryLogRelation(data) {
  return request({
    url: 'OaLog/queryLogRelation',
    method: 'post',
    data: data
  })
}

// 查询关联日程
export function crmQueryEventRelation(data) {
  return request({
    url: 'OaEvent/queryEventRelation',
    method: 'post',
    data: data
  })
}

// 查询关联任务
export function crmQueryTaskRelation(data) {
  return request({
    url: 'task/queryTaskRelation',
    method: 'post',
    data: data
  })
}

// 查询关联审批
export function crmQueryExamineRelation(data) {
  return request({
    url: 'OaExamine/queryExamineRelation',
    method: 'post',
    data: data
  })
}

// 操作记录
export function crmIndexFieldRecord(data) {
  return request({
    url: 'CrmRecord/queryRecordList',
    method: 'post',
    data: data
  })
}

/**
 * 记录类型 数据 与系统设置系统(repeat)
 * @param {*} data
 */
export function crmSettingRecordListAPI(data) {
  return request({
    url: 'CrmRecord/queryRecordOptions',
    method: 'post',
    data: data
  })
}

// 客户管理下 合同审批信息 新建
export function crmExamineFlowStepList(data) {
  return request({
    url: 'examineRecord/queryExamineRecordList',
    method: 'post',
    data: data
  })
}

// CRM合同回款创建时候的审批流
export function crmCreateExamineFlow(data) {
  return request({
    url: 'examine/queryExaminStep',
    method: 'post',
    data: data
  })
}

/**
 * 审批记录
 * @param {*} data
 * types
 * typesId
 */
export function crmExamineFlowRecordList(data) {
  return request({
    url: 'examineRecord/queryExamineLogList',
    method: 'post',
    data: data
  })
}

// 审核合同或者回款
export function crmExamineFlowAuditExamine(data) {
  return request({
    url: 'examineRecord/auditExamine',
    method: 'post',
    data: data
  })
}

// 跟进记录删除
export function crmRecordDelete(data) {
  return request({
    url: 'CrmRecord/deleteFollowRecord',
    method: 'post',
    data: data
  })
}

/**
 * 查看当前导入数量
 * @param {*} data
 */
export function crmQueryImportNumAPI(data) {
  return request({
    url: 'system/message/queryImportNum',
    method: 'post',
    data: data
  })
}

/**
 * 停止时候的状态
 * @param {*} data
 */
export function crmQueryImportInfoAPI(data) {
  return request({
    url: 'system/message/queryImportInfo',
    method: 'post',
    data: data
  })
}

/**
 * 查看错误数据接口
 * @param {*} data
 */
export function crmDownImportErrorAPI(data) {
  return request({
    url: 'system/message/downImportError',
    method: 'post',
    data: data,
    responseType: 'blob'
  })
}
