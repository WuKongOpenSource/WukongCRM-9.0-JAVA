import request from '@/utils/request'

/**
 * 待审核合同
 * @param {*} data
 */
export function crmMessageCheckContractAPI(data) {
  return request({
    url: 'CrmBackLog/checkContract',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 待审核回款
 * @param {*} data
 */
export function crmMessageCheckReceivablesAPI(data) {
  return request({
    url: 'CrmBackLog/checkReceivables',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 今日需联系客户
 * @param {*} data
 */
export function crmMessageTodayCustomerAPI(data) {
  return request({
    url: 'CrmBackLog/todayCustomer',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 待跟进线索
 * @param {*} data
 */
export function crmMessageFollowLeadsAPI(data) {
  return request({
    url: 'CrmBackLog/followLeads',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}


/**
 * 待跟进客户
 * @param {*} data
 */
export function crmMessageFollowCustomerAPI(data) {
  return request({
    url: 'CrmBackLog/followCustomer',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 即将到期合同
 * @param {*} data
 */
export function crmMessagEndContractAPI(data) {
  return request({
    url: 'CrmBackLog/endContract',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 待回款合同
 * @param {*} data
 */
export function crmMessagRemindreceivablesplanAPI(data) {
  return request({
    url: 'CrmBackLog/remindReceivables',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 待办消息数
 * @param {*} data
 */
export function crmMessagNumAPI(data) {
  return request({
    url: 'CrmBackLog/num',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 待进入客户池
 * @param {*} data
 */
export function crmMessagRemindCustomerAPI(data) {
  return request({
    url: 'CrmBackLog/putInPoolRemind',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
