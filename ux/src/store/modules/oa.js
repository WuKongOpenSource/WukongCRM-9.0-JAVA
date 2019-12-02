import {
  oaMessageNumAPI
} from '@/api/oamanagement/workbench'
import {
  objDeepCopy
} from '@/utils'

/**
 * 消息记录
 */
const oa = {
  state: {
    // 待办事项消息
    messageOANum: {
      eventNum: 0,
      taskNum: 0,
      announcementNum: 0,
      logNum: 0,
      examineNum: 0
    }
  },

  mutations: {
    /**
     * 更改待办事项
     */
    SET_MESSAGEOANUM: (state, messageOANum) => {
      state.messageOANum = messageOANum
    }
  },

  actions: {
    /**
     * 获取待办消息数
     * @param {*} param
     */
    GetOAMessageNum({
      state,
      commit
    }, type) {
      return new Promise((resolve, reject) => {
        const params = {}
        if (type) {
          params.type = type
        }
        oaMessageNumAPI(params)
          .then(response => {
            if (type) {
              const copyNum = objDeepCopy(state.messageOANum)
              copyNum[type + 'Num'] = response.data[type + 'Num'] || 0
              commit('SET_MESSAGEOANUM', copyNum)
            } else {
              commit('SET_MESSAGEOANUM', response.data)
            }
            if (resolve) {
              resolve(response)
            }
          })
          .catch(error => {
            if (reject) {
              reject(error)
            }
          })
      })
    }

  }
}

export default oa
