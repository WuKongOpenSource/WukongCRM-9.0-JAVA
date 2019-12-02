<template>
  <el-dialog
    :visible.sync="showDialog"
    title="审批类型"
    width="500px"
    @close="closeView">
    <div class="title">请选择您的审批类型（管理后台可自定义配置审批类型）</div>
    <div
      v-loading="loading"
      class="categorys">
      <flexbox
        wrap="wrap"
        align="stretch">
        <div
          v-for="(item, index) in categorys"
          :key="index"
          class="category-item"
          @click="selectCategorys(item)">
          <i
            :class="item.iconClass"
            class="wukong"/>
          {{ item.title }}
        </div>
      </flexbox>
    </div>
  </el-dialog>
</template>

<script>
import { oaExamineCategoryList } from '@/api/oamanagement/examine'

export default {
  name: 'ExamineCategorySelect',
  components: {},
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      loading: false,
      showDialog: false,
      categorys: []
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
          this.categorys = res.data.map(item => {
            item.iconClass = this.getCategoryIcon(item.categoryId)
            return item
          })
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
    },

    /**
     * 获取图标
     */
    getCategoryIcon(categoryId) {
      // 系统审批
      categoryId = parseInt(categoryId)
      if (categoryId <= 6) {
        return [
          'wukong-examine-category-ordinary',
          'wukong-examine-category-leave',
          'wukong-examine-category-business',
          'wukong-examine-category-overtime',
          'wukong-examine-category-billing',
          'wukong-examine-category-borrowing'
        ][categoryId - 1]
      } else {
        return [
          'wukong-examine-category-one',
          'wukong-examine-category-two',
          'wukong-examine-category-three',
          'wukong-examine-category-four',
          'wukong-examine-category-five',
          'wukong-examine-category-six',
          'wukong-examine-category-seven',
          'wukong-examine-category-eight',
          'wukong-examine-category-nine'
        ][categoryId % 9]
      }
    }
  }
}
</script>

<style scoped lang="scss">
.title {
  position: absolute;
  left: 25px;
  top: 55px;
  font-size: 12px;
  color: #ccc;
}

.categorys {
  height: 250px;
  overflow-y: auto;
  .category-item {
    padding: 5px 10px;
    border-radius: 3px;
    border: 1px solid #e6e6e6;
    color: #727272;
    font-size: 12px;
    margin: 5px 7px;
    cursor: pointer;

    .wukong {
      color: $xr-color-primary;
      margin-right: 2px;
    }
  }

  .category-item:hover {
    color: $xr-color-primary;
    border-color: $xr-color-primary;
  }
}
</style>
