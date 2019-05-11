<template>
  <el-date-picker style="width: 100%;"
                  v-model="dataValue"
                  type="datetime"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  @change="valueChange"
                  placeholder="选择日期"
                  :disabled="disabled">
  </el-date-picker>
</template>
<script type="text/javascript">
import stringMixin from './stringMixin'
import { getDateFromTimestamp } from '@/utils'
import moment from 'moment'

export default {
  name: 'xh-input', // 新建 datetime
  components: {},
  mixins: [stringMixin],
  computed: {},
  watch: {},
  data() {
    return {}
  },
  props: {
    value: {
      type: String,
      default: ''
    }
  },
  mounted() {
    if (this.value && this.value.toString().length == 10) {
      // 编辑的时候 值是时间戳
      this.dataValue = moment(getDateFromTimestamp(this.value)).format(
        'YYYY-MM-DD HH:mm:ss'
      )
    }
  },
  methods: {}
}
</script>
<style lang="scss" scoped>
</style>
