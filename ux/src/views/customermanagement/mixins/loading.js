import {
  Loading
} from 'element-ui'

export default {
  data() {
    return {
      loading: false,
      tab_loading: null // 放在 tabs-content 里客户的
    }
  },

  watch: {
    loading: function(val) {
      if (val) {
        this.tab_loading = Loading.service({
          target: document.querySelector('.t-loading-content')
        })
      } else {
        this.tab_loading.close()
      }
    }
  },

  mounted() {},

  deactivated: function() {}

}
