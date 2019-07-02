<template>
  <el-select style="width: 100%;"
             v-model="dataValue"
             :disabled="disabled"
             @change="valueChange"
             placeholder="请选择">
    <el-option v-for="(item, index) in option"
               :key="index"
               :label="item.name"
               :value="item.value">
    </el-option>
  </el-select>
</template>
<script type="text/javascript">
import stringMixin from './stringMixin'

export default {
  name: 'xh-select', // 新建 select
  components: {},
  mixins: [stringMixin],
  watch: {
    item: {
      handler(val) {
        if (val && val.data.setting) {
          var settingList = val.data.setting
          if (settingList.length > 0 && typeof settingList[0] == 'string') {
            var array = []
            for (let index = 0; index < settingList.length; index++) {
              const element = settingList[index]
              array.push({ name: element, value: element })
            }
            this.option = array
          } else if (
            settingList.length > 0 &&
            settingList[0].statusId &&
            !settingList[0].value
          ) {
            // 商机阶段
            this.option = settingList.map((item, index, array) => {
              item.value = item.statusId
              return item
            })
          } else {
            this.option = settingList
          }
        } else {
          this.option = []
        }
      },
      deep: true,
      immediate: true
    }
  },
  computed: {},
  data() {
    return {
      option: []
    }
  },
  props: {},
  mounted() {},
  methods: {}
}
</script>
<style lang="scss" scoped>
</style>
