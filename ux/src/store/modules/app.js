import {
  adminSystemIndex
} from '@/api/systemManagement/SystemConfig'
import {
  crmSettingConfigData
} from '@/api/systemManagement/SystemCustomer'
import Lockr from 'lockr'

/** 记录 侧边索引 */
const app = {
  state: {
    logo: '',
    name: '',
    lang: localStorage.lang || 'cn',
    sidebar: {
      activeIndex: '' // 目前激活的 行
    },
    navbar: {
      activeIndex: '' // 导航目前是第几个 个人中心需要
    },
    /** CRM配置信息 */
    CRMConfig: {}
  },

  mutations: {
    SET_ACTIVEINDEX: (state, path) => {
      state.sidebar.activeIndex = path
    },
    SET_NAVACTIVEINDEX: (state, path) => {
      state.navbar.activeIndex = path
    },
    SET_APPLOGO: (state, logo) => {
      state.logo = logo
    },
    SET_APPNAME: (state, name) => {
      state.name = name
    },
    SET_LANG: (state, lang) => {
      state.lang = lang
      window.app.$i18n.locale = lang
      localStorage.setItem('lang', lang)
      window.location.reload()
    },
    SET_CRMCONFIG: (state, config) => {
      state.CRMConfig = config
    }
  },

  actions: {
    // 登录
    SystemLogoAndName({
      commit
    }) {
      return new Promise((resolve, reject) => {
        adminSystemIndex().then(response => {
          commit('SET_APPNAME', response.data.name)
          commit('SET_APPLOGO', response.data.logo)
          Lockr.set('systemLogo', response.data.logo)
          Lockr.set('systemName', response.data.name)
          resolve(response)
        }).catch(error => {
          reject(error)
        })
      })
    },

    /**
     * 获取客户管理配置
     */
    CRMSettingConfig({
      commit
    }) {
      return new Promise((resolve, reject) => {
        crmSettingConfigData().then(response => {
          commit('SET_CRMCONFIG', response.data)
          resolve(response)
        }).catch(error => {
          reject(error)
        })
      })
    }
  }
}

export default app
