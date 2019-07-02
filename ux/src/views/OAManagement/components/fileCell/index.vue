<template>
  <flexbox class="cell">
    <img class="cell-head"
         src="@/assets/img/relevance_file.png"
         alt>
    <div class="cell-body"
         :class="{'cursor-pointer' :cursorPointer}">
      <span>{{data.name.length > 20 ? data.name.substring(0, 20) + '...' : data.name}}</span>
      <span class="size">({{data.size}})</span>
    </div>
    <div class="cell-foot">
      <el-button type="primary"
                 icon="el-icon-download"
                 @click="downloadClick">下载</el-button>
      <i v-if="showDelete"
         class="el-icon-delete"
         @click="deleteClick"></i>
    </div>
  </flexbox>
</template>

<script type="text/javascript">
import { downloadFile } from '@/utils'
import { crmFileDelete } from '@/api/common'

export default {
  name: 'file-cell', // 附件展示效果
  computed: {},
  watch: {},
  data() {
    return {}
  },
  props: {
    cellIndex: Number,
    data: Object,
    showFoot: {
      type: Boolean,
      default: true
    },
    cursorPointer: {
      type: Boolean,
      default: false
    },
    showDelete: {
      type: Boolean,
      default: false
    },
    module_id: [String, Number]
  },
  mounted() {},
  methods: {
    downloadClick() {
      downloadFile({ path: this.data.filePath, name: this.data.name })
    },
    deleteClick() {
      this.$confirm('确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          crmFileDelete({
            id: this.data.fileId
          })
            .then(res => {
              this.$message.success('操作成功')
              this.$emit('delete', this.cellIndex, this.data)
            })
            .catch(() => {})
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    }
  },

  beforeDestroy() {}
}
</script>
<style lang="scss" scoped>
.cell {
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 2px;
  position: relative;
  margin-bottom: 5px;

  .cell-head {
    display: block;
    width: 16px;
    height: 16px;
    margin-right: 5px;
  }

  .cell-body {
    flex: 1;
    color: #3e84e9;
    font-size: 13px;
    .size {
      color: #ccc;
    }
  }

  .cell-foot {
    margin-right: 8px;
    cursor: pointer;
    i {
      color: #ccc;
      padding: 0 2px;
    }
  }
}

.cursor-pointer {
  cursor: pointer;
}
</style>
