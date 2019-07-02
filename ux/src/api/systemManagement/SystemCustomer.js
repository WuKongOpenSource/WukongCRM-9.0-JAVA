import request from '@/utils/request'

export function businessGroupList(data) {
  return request({
    url: 'businessType/queryBusinessTypeList',
    method: 'post',
    data: data
  })
}

export function businessGroupAdd(data) {
  return request({
    url: 'businessType/setBusinessType',
    method: 'post',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    data: data
  })
}

/** 商机状态组详情 */
export function businessGroupRead(data) {
  return request({
    url: 'businessType/getBusinessType',
    method: 'post',
    data: data
  })
}

// 商机状态组编辑
export function businessGroupUpdate(data) {
  return request({
    url: 'businessType/setBusinessType',
    method: 'post',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    data: data
  })
}

/** 商机状态组删除 */
export function businessGroupDelete(data) {
  return request({
    url: 'businessType/deleteById',
    method: 'post',
    data: data
  })
}

/** 自定义字段（字段数据）的添加编辑操作 */
export function customFieldHandle(data) {
  return request({
    url: 'field/save',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/** 自定义字段（字段数据）的详情 */
export function customFieldList(data) {
  return request({
    url: 'field/list',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/** 自定义字段（字段数据）的列表更新时间 */
export function customFieldIndex(data) {
  return request({
    url: 'field/queryFields',
    method: 'post',
    data: data
  })
}

/** 产品类别 数据获取 */
export function productCategoryIndex(data) {
  return request({
    url: 'CrmProductCategory/queryList',
    method: 'post',
    data: data
  })
}

/** 产品分类添加*/
export function productCategorySave(data) {
  return request({
    url: 'CrmProductCategory/saveAndUpdate',
    method: 'post',
    data: data
  })
}

/** 产品分类删除*/
export function productCategoryDelete(data) {
  return request({
    url: 'CrmProductCategory/deleteById',
    method: 'post',
    data: data
  })
}

/** 客户保护规则*/
export function crmSettingConfig(data) {
  return request({
    url: 'CrmCustomer/updateRulesSetting',
    method: 'post',
    data: data
  })
}

/** 客户保护规则*/
export function crmSettingConfigData(data) {
  return request({
    url: 'CrmCustomer/getRulesSetting',
    method: 'post',
    data: data
  })
}

/**
 * 部门业绩目标列表
 * @param {*} data
 */
export function crmAchievementIndex(data) {
  return request({
    url: 'achievement/queryAchievementList',
    method: 'post',
    data: data
  })
}

/**
 * 业绩目标编辑接口
 * @param {*} data
 */
export function crmAchievementUpdate(data) {
  return request({
    url: 'achievement/setAchievement',
    method: 'post',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    data: data
  })
}

/**
 * 记录类型 数据
 * @param {*} data
 */
export function crmSettingRecordListAPI(data) {
  return request({
    url: 'CrmRecord/queryRecordOptions',
    method: 'post',
    data: data
  })
}

/**
 * 记录类型 编辑
 * @param {*} data
 */
export function crmSettingRecordEditAPI(data) {
  return request({
    url: 'CrmRecord/setRecordOptions',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 合同到期提醒 设置
 * @param {*} data 
 */
export function crmSettingContractDayAPI(data) {
  return request({
    url: 'CrmContract/setContractConfig',
    method: 'post',
    data: data
  })
}
