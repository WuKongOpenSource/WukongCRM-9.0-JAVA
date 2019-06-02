import request from '@/utils/request'

/**
 * 员工客户总量分析
 */
export function biCustomerTotalAPI(data) {
  return request({
    url: 'biCustomer/totalCustomerStats',
    method: 'post',
    data: data
  })
}

export function biCustomerTotalListAPI(data) {
  return request({
    url: 'biCustomer/totalCustomerTable',
    method: 'post',
    data: data
  })
}

/**
 * 员工客户跟进次数分析
 * @param {*} data
 */
export function biCustomerRecordTimesAPI(data) {
  return request({
    url: 'biCustomer/customerRecordStats',
    method: 'post',
    data: data
  })
}

/**
 * 员工客户跟进次数分析 具体员工列表
 * @param {*} data
 */
export function biCustomerRecordListAPI(data) {
  return request({
    url: 'biCustomer/customerRecordInfo',
    method: 'post',
    data: data
  })
}

/**
 * 员工跟进方式分析
 * @param {*} data
 */
export function biCustomerRecordModeAPI(data) {
  return request({
    url: 'biCustomer/customerRecodCategoryStats',
    method: 'post',
    data: data
  })
}

/**
 * 客户转化率分析具体数据
 * @param {*} data
 */
export function biCustomerConversionInfoAPI(data) {
  return request({
    url: 'biCustomer/customerConversionInfo',
    method: 'post',
    data: data
  })
}

/**
 * 客户转化率分析
 * @param {*} data
 */
export function biCustomerConversionAPI(data) {
  return request({
    url: 'biCustomer/customerConversionStats',
    method: 'post',
    data: data
  })
}


/**
 * 公海客户分析
 * @param {*} data
 */
export function biCustomerPoolAPI(data) {
  return request({
    url: 'biCustomer/poolStats',
    method: 'post',
    data: data
  })
}

/**
 * 公海客户分析
 * @param {*} data
 */
export function biCustomerPoolListAPI(data) {
  return request({
    url: 'biCustomer/poolTable',
    method: 'post',
    data: data
  })
}

/**
 * 员工客户成交周期
 * @param {*} data
 */
export function biCustomerUserCycleAPI(data) {
  return request({
    url: 'biCustomer/employeeCycle',
    method: 'post',
    data: data
  })
}

/**
 *
 */
export function employeeCycleInfo(data) {
  return request({
    url: 'biCustomer/employeeCycleInfo',
    method: 'post',
    data: data
  })
}

/**
 * 地区成交周期
 * @param {*} data
 */
export function biCustomerAddressCycleAPI(data) {
  return request({
    url: 'biCustomer/districtCycle',
    method: 'post',
    data: data
  })
}

/**
 * 产品成交周期
 * @param {*} data
 */
export function biCustomerProductCycleAPI(data) {
  return request({
    url: 'biCustomer/productCycle',
    method: 'post',
    data: data
  })
}
