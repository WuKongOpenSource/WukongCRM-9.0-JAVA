import request from '@/utils/request'

/**
 * 合同金额
 * @param {*} data
 * year 年
 * status 1销售（目标）2回款（目标）
 * user_id 员工ID
 * structure_id 部门ID
 */
export function biRankingContractAPI(data) {
  return request({
    url: 'biRanking/contractRanKing',
    method: 'post',
    data: data
  })
}

/**
 * 回款金额排行
 * @param {*} data
 */
export function biRankingReceivablesAPI(data) {
  return request({
    url: 'biRanking/receivablesRanKing',
    method: 'post',
    data: data
  })
}

/**
 * 签约合同排行
 * @param {*} data
 */
export function biRankingSigningAPI(data) {
  return request({
    url: 'biRanking/contractCountRanKing',
    method: 'post',
    data: data
  })
}

/**
 * 产品销量排行
 * @param {*} data
 */
export function biRankingProductAPI(data) {
  return request({
    url: 'biRanking/productCountRanKing',
    method: 'post',
    data: data
  })
}

/**
 * 新增客户数排行
 * @param {*} data
 */
export function biRankingAddCustomerAPI(data) {
  return request({
    url: 'biRanking/customerCountRanKing',
    method: 'post',
    data: data
  })
}

/**
 * 新增联系人数排行
 * @param {*} data
 */
export function biRankingAddContactsAPI(data) {
  return request({
    url: 'biRanking/contactsCountRanKing',
    method: 'post',
    data: data
  })
}

/**
 * 跟进次数排行
 * @param {*} data
 */
export function biRankingRecordNunAPI(data) {
  return request({
    url: 'biRanking/recordCountRanKing',
    method: 'post',
    data: data
  })
}

/**
 * 跟进客户数排行
 * @param {*} data
 */
export function biRankingRecordCustomerAPI(data) {
  return request({
    url: 'biRanking/customerGenjinCountRanKing',
    method: 'post',
    data: data
  })
}

/**
 * 出差次数排行
 * @param {*} data
 */
export function biRankingExamineAPI(data) {
  return request({
    url: 'biRanking/travelCountRanKing',
    method: 'post',
    data: data
  })
}
