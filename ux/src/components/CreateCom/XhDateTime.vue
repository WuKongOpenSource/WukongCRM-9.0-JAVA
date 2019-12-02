<template>
  <el-date-picker
    v-model="dataValue"
    :disabled="disabled"
    style="width: 100%;"
    type="datetime"
    value-format="yyyy-MM-dd HH:mm:ss"
    placeholder="选择日期"
    @change="valueChange"/>
</template>
<script type="text/javascript">
import stringMixin from './stringMixin'
import { getDateFromTimestamp } from '@/utils'
import moment from 'moment'

export default {
  name: 'XhInput', // 新建 datetime
  components: {},
  mixins: [stringMixin],
  props: {
    value: {
      type: String,
      default: ''
    }
  },
  data() {
    return {}
  },
  computed: {},
  watch: {},
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
