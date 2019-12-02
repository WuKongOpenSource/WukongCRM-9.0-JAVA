<template>
  <div class="edit-tag-dialog">
    <div class="title">
      <span
        class="el-icon-arrow-left"
        @click="back"/>
      <span>标签管理</span>
      <span
        class="el-icon-close rt"
        @click="close"/>
    </div>
    <div class="search">
      <el-input
        v-model="inputModel"
        :maxlength="10"
        size="mini"
        placeholder="输入标签名，最多十个字符"/>
    </div>
    <div class="content">
      <div
        v-for="(item, index) in list"
        :key="index"
        class="tag-list">
        <span
          :style="{'background': item.color ? item.color: '#ccc'}"
          class="tag-name">
          {{ item.name.length > 10 ? item.name.substring(0, 10) + '...' : item.name }}
        </span>
        <div class="rt">
          <span
            class="el-icon-edit"
            @click="editBtn(item)"/>
          <span
            class="el-icon-delete"
            @click="deleteBtn(item)"/>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    editTagList: Array
  },
  data() {
    return {
      inputModel: '',
      list: this.editTagList
    }
  },
  watch: {
    inputModel: function(newVal) {
      this.list = this.editTagList.filter(item => {
        return item.name.indexOf(newVal) > -1
      })
    }
  },
  methods: {
    back() {
      this.$emit('back')
    },
    close() {
      this.$emit('close')
    },
    deleteBtn(item) {
      this.$emit('deleteBtn', item)
    },
    editBtn(item) {
      this.$emit('editBtn', item)
    }
  }
}
</script>


<style scoped lang="scss">
.edit-tag-dialog {
  overflow: hidden;
  // position: absolute;
  // top: 0;
  // left: -110px;
  // right: 0;
  background: #fff;
  min-height: 200px;
  // box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
  .title {
    padding: 10px;
    border-bottom: 1px solid#E6E6E6;
    margin-bottom: 20px;
    .el-icon-close {
      margin-right: 0;
      cursor: pointer;
    }
    .el-icon-arrow-left {
      cursor: pointer;
    }
  }
  .search {
    .el-input {
      width: 90%;
      margin: 0 5%;
    }
    .el-input /deep/ .el-input__inner {
      border-radius: 15px;
    }
  }
  .content {
    margin: 10px 0;
    height: 196px;
    overflow: auto;
    .tag-list {
      padding: 5px 5%;
      .rt {
        margin-right: 0;
        color: #aaa;
        span {
          margin-right: 10px;
          cursor: pointer;
        }
      }
      .tag-name {
        font-size: 12px;
        border-radius: 3px;
        padding: 3px 10px;
        color: #fff;
      }
    }
    .tag-list:hover {
      background: #f7f8fa;
    }
  }
}
</style>
