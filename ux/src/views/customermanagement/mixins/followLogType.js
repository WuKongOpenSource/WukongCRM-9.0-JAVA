import {
  crmSettingRecordListAPI
} from '@/api/customermanagement/common'
import { mapGetters } from 'vuex'

export default {
  data() {
    return {
      /** 记录类型 */
      followTypes: [],
      followType: ''
    }
  },

  computed: {
    ...mapGetters([
      'oa'
    ]),
    showOAPermission() {
      return this.oa
    }
  },

  created() {
    this.getFollowLogType()
  },

  methods: {
    /**
     * 获取详情
     */
    getFollowLogType() {
      this.loading = true
      crmSettingRecordListAPI()
        .then(res => {
          this.loading = false
          this.followTypes = res.data.map(item => {
            return {
              type: item,
              name: item
            }
          })
          this.followType = res.data.length > 0 ? res.data[0] : ''
        })
        .catch(() => {
          this.loading = false
        })
    }
  },

  deactivated: function() { }

}
