<template>
  <div class="c-container">
    <div class="title">{{title}}</div>
    <el-input class="sc-container"
              :placeholder="placeholder"
              v-model="inputContent"
              @input="inputChange"
              @keyup.enter.native="searchInput">
      <el-button slot="append"
                 @click.native="searchInput"
                 icon="el-icon-search"></el-button>
    </el-input>
    <div class="right-container">
      <el-button @click="createClick"
                 v-if="canSave"
                 type="primary">{{mainTitle}}</el-button>
      <el-dropdown trigger="click"
                   v-if="moreTypes.length > 0"
                   @command="handleTypeDrop">
        <flexbox class="right-more-item">
          <div>更多</div>
          <i class="el-icon-arrow-down el-icon--right"
             style="color:#777;"></i>
        </flexbox>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item v-for="(item, index) in moreTypes"
                            :key="index"
                            :command="item.type">{{item.name}}</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
    <c-r-m-create-view v-if="isCreate"
                       :crm-type="createCRMType"
                       :action="createActionInfo"
                       @save-success="createSaveSuccess"
                       @hiden-view="hideView"></c-r-m-create-view>
    <c-r-m-import :show="showCRMImport"
                  @close="showCRMImport=false"
                  :crmType="crmType">
    </c-r-m-import>
  </div>
</template>

<script type="text/javascript">
import { mapGetters } from 'vuex'
import CRMCreateView from './CRMCreateView'
import CRMImport from './CRMImport'

export default {
  name: 'CRM-list-head', //客户管理下 重要提醒 回款计划提醒
  components: {
    CRMCreateView,
    CRMImport
  },
  computed: {
    ...mapGetters(['crm']),
    canSave() {
      if (this.isSeas) {
        return false
      }
      return this.crm[this.crmType].save
    }
  },
  data() {
    return {
      inputContent: '',
      /** 更多操作 */
      moreTypes: [],
      // 创建的相关信息
      createActionInfo: { type: 'save' },
      createCRMType: '',
      isCreate: false, //是创建
      // 导入
      showCRMImport: false
    }
  },
  props: {
    title: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: '请输入内容'
    },
    mainTitle: {
      type: String,
      default: ''
    },
    // CRM类型
    crmType: {
      type: String,
      default: ''
    },
    /** 是公海 */
    isSeas: {
      type: Boolean,
      default: false
    },
    search: String
  },
  mounted() {
    // 线索和客户判断更多操作
    if (!this.isSeas) {
      if (this.crm[this.crmType].excelimport) {
        this.moreTypes.push({ type: 'enter', name: '导入' })
      }
      if (this.crm[this.crmType].excelexport) {
        this.moreTypes.push({ type: 'out', name: '导出' })
      }
    } else {
      // 客户池的导出关键字不同
      if (this.crm.pool.excelexport) {
        this.moreTypes.push({ type: 'out', name: '导出' })
      }
    }
  },
  methods: {
    handleTypeDrop(command) {
      if (command == 'out') {
        this.$emit('on-export')
      } else if (command == 'enter') {
        this.showCRMImport = true
      }
    },
    createClick() {
      this.createCRMType = this.crmType
      this.createActionInfo = { type: 'save' }
      this.isCreate = !this.isCreate
    },
    inputChange() {
      this.$emit('update:search', this.inputContent)
    },
    // 进行搜索操作
    searchInput() {
      this.$emit('on-search', this.inputContent)
    },
    // 创建数据页面 保存成功
    createSaveSuccess(data) {
      if (data && data.saveAndCreate) {
        if (data.type == 'customer') {
          this.createCRMType = 'contacts'
          this.createActionInfo = {
            type: 'relative',
            crmType: 'customer',
            data: {}
          }
          this.createActionInfo.data['customer'] = data.data
          this.isCreate = true
        }
      } else {
        // 回到保存成功
        this.$emit('on-handle', { type: 'save-success' })
      }
    },
    hideView() {
      this.isCreate = false
    }
  }
}
</script>
<style lang="scss" scoped>
.c-container {
  height: 60px;
  position: relative;
  z-index: 100;
  .title {
    float: left;
    padding: 0 20px;
    font-size: 18px;
    line-height: 60px;
  }
  .sc-container {
    width: 300px;
    margin: -18px 0 0 -150px;
    position: absolute;
    left: 50%;
    top: 50%;
  }

  .right-container {
    margin-right: -10px;
    float: right;
    margin: 12px 20px 0 0;
    position: relative;
    .right-item {
      float: left;
      margin-right: 10px;
      padding: 8px 10px;
      font-size: 13px;
      border-radius: 2px;
    }

    .right-more-item {
      cursor: pointer;
      border: 1px solid #dcdfe6;
      background-color: white;
      font-size: 13px;
      color: #777;
      padding: 0 12px;
      border-radius: 2px;
      height: 31px;
    }
  }
}
</style>
