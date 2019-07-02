<template>
  <div v-loading="loading">
    <div class="content-title">
      <span>跟进记录类型设置</span>
      <el-button type="primary"
                 class="rt"
                 size="medium"
                 @click="save">保存</el-button>
    </div>
    <div class="content-body">
      <div v-for="(item, index) in list"
           :key="index"
           class="input-item">
        <el-input v-model="item.value"></el-input>
        <i class="el-icon-remove"
           @click="deleteItem(item, index)"></i>
      </div>
      <el-button @click="addItem"
                 type="text">+添加类型</el-button>
    </div>
  </div>
</template>

<script>
import {
  crmSettingRecordListAPI,
  crmSettingRecordEditAPI
} from '@/api/systemManagement/SystemCustomer'

export default {
  name: 'follow-log-type-set',

  components: {},

  data() {
    return {
      loading: false, // 展示加载中效果

      list: [] // 展示类型数据
    }
  },

  created() {
    this.getDetail()
  },

  methods: {
    /**
     * 获取详情
     */
    getDetail() {
      this.loading = true
      crmSettingRecordListAPI()
        .then(res => {
          this.loading = false
          this.list = res.data.map(item => {
            return { value: item }
          })
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * 增加类型
     */
    addItem() {
      this.list.push({ value: '' })
    },

    /**
     * 删除事项操作
     */
    deleteItem(item, index) {
      this.list.splice(index, 1)
    },

    /**
     * 保存操作
     */
    save() {
      let value = []
      for (let index = 0; index < this.list.length; index++) {
        const element = this.list[index]
        if (element.value) {
          value.push(element.value)
        }
      }
      this.loading = true
      crmSettingRecordEditAPI({ value: value })
        .then(res => {
          this.loading = false
          this.$message.success(res.data)
        })
        .catch(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.content-title {
  padding: 10px;
  border-bottom: 1px solid #e6e6e6;
}

.content-title > span {
  display: inline-block;
  height: 36px;
  line-height: 36px;
  margin-left: 20px;
}

.content-body {
  height: calc(100% - 57px);
  padding: 30px;
  overflow-y: auto;
}

/* 事项布局 */
.input-item {
  margin-bottom: 10px;

  .el-input {
    width: 300px;
  }

  .el-icon-remove {
    color: #ff6767;
    cursor: pointer;
    margin-left: 2px;
    display: none;
  }
}

.input-item:hover {
  .el-icon-remove {
    display: inline;
  }
}
</style>
