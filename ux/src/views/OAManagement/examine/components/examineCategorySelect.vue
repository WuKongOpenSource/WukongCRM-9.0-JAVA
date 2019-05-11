<template>
  <el-dialog :visible.sync="showDialog"
             title="审批类型"
             width="500px"
             @close="closeView">
    <div class="categorys"
         v-loading="loading">
      <flexbox wrap="wrap"
               align="stretch">
        <div v-for="(item, index) in categorys"
             :key="index"
             class="category-item"
             @click="selectCategorys(item)">
          {{item.title}}
        </div>
      </flexbox>
    </div>
  </el-dialog>
</template>

<script>
import { oaExamineCategoryList } from '@/api/oamanagement/examine'

export default {
  name: 'examine-category-select',
  components: {},
  data() {
    return {
      loading: false,
      showDialog: false,
      categorys: []
    }
  },
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  watch: {
    show: function(val) {
      this.showDialog = val
      if (this.categorys && this.categorys.length == 0) {
        this.getDetail()
      }
    }
  },
  mounted() {},
  methods: {
    // 审批类型列表
    getDetail() {
      this.loading = true
      oaExamineCategoryList()
        .then(res => {
          this.loading = false
          this.categorys = res.data
        })
        .catch(() => {
          this.loading = false
        })
    },
    // 审批类型选择
    selectCategorys(item) {
      this.$emit('select', item)
      this.$emit('close')
    },
    // 关闭操作
    closeView() {
      this.$emit('close')
    }
  }
}
</script>

<style scoped lang="scss">
.categorys {
  height: 250px;
  overflow-y: auto;
  .category-item {
    padding: 5px 10px;
    border-radius: 2px;
    border: 1px solid #e6e6e6;
    color: #727272;
    font-size: 13px;
    margin: 5px;
  }

  .category-item:hover {
    background-color: #eaeaea;
    color: #333;
  }
}
</style>
