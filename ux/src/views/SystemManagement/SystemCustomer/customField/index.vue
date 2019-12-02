<template>
  <div class="system-customer">
    <div class="title">客户管理</div>
    <div class="customer-content">
      <div
        v-loading="loading"
        class="system-view-table">
        <div class="content-title">
          <span>自定义字段设置</span>
        </div>
        <div class="table-box">
          <div
            v-for="(item, index) in tableList"
            :key="index"
            class="table-list">
            <img
              :src="getCustomFieldIcon(item.types)"
              class="table-item-icon" >
            <div class="table-item-label">{{ item.name }}</div>
            <div class="table-item-time">{{ item.updateTime == 0 ? '暂无' : item.updateTime }}更新</div>
            <div class="table-right-btn">
              <el-button
                style="font-weight: 400;"
                type="text"
                @click="handleCustomField('edit', item, index)">编 辑</el-button>
              <el-button
                style="font-weight: 400;"
                type="text"
                @click="handleCustomField('preview', item, index)">预 览</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 表单预览 -->
    <preview-field-view
      v-if="showTablePreview"
      :types="tablePreviewData.types"
      :types-id="tablePreviewData.typesId"
      :label="tablePreviewData.label"
      @hiden-view="showTablePreview=false"/>
  </div>
</template>

<script>
import { customFieldIndex } from '@/api/systemManagement/SystemCustomer'
import PreviewFieldView from '@/views/SystemManagement/components/previewFieldView'

export default {
  name: 'CustomField',

  components: {
    PreviewFieldView
  },

  data() {
    return {
      loading: false,
      // 自定义字段设置
      tableList: [],
      // 展示表单预览
      tablePreviewData: { types: '', typesId: '' },
      showTablePreview: false
    }
  },

  created() {
    this.getDetail()
  },

  methods: {
    /**
     * 详情
     */
    getDetail() {
      this.loading = true
      customFieldIndex()
        .then(res => {
          this.tableList = res.data
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * 列表的编辑和预览
     */
    handleCustomField(type, item, index) {
      if (type == 'edit') {
        this.$router.push({
          name: 'handlefield',
          params: {
            type: item.types,
            id: 'none',
            label: item.label
          }
        })
      } else if (type == 'preview') {
        this.tablePreviewData = item
        this.showTablePreview = true
      }
    },

    /**
     * 根据自定义字段types 获取展示icon
     */
    getCustomFieldIcon(type) {
      if (type == 'crm_leads') {
        return require('@/assets/img/field_leads_manager.png')
      } else if (type == 'crm_customer') {
        return require('@/assets/img/field_customer_manager.png')
      } else if (type == 'crm_contacts') {
        return require('@/assets/img/field_contacts_manager.png')
      } else if (type == 'crm_business') {
        return require('@/assets/img/field_business_manager.png')
      } else if (type == 'crm_contract') {
        return require('@/assets/img/field_contract_manager.png')
      } else if (type == 'crm_product') {
        return require('@/assets/img/field_product_manager.png')
      } else if (type == 'crm_receivables') {
        return require('@/assets/img/field_receivables_manager.png')
      }
      return require('@/assets/img/field_other_manager.png')
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.system-customer {
  /* padding: 0 20px 20px; */
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}
.title {
  font-size: 18px;
  height: 40px;
  padding: 0 20px;
  line-height: 40px;
  margin: 10px 0;
  color: #333;
}
.customer-content {
  flex: 1;
  position: relative;
  display: flex;
  overflow: hidden;
}
.system-view-table {
  flex: 1;
  border: 1px solid #e6e6e6;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow-x: auto;
}
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
.table-list {
  border-bottom: 1px solid #e6e6e6;
  height: 60px;
  overflow: hidden;
  align-items: center;
  font-size: 13px;
  display: flex;
}
.table-item-label {
  text-align: left;
  flex: 1;
}
.table-item-icon {
  display: block;
  width: 35px;
  height: 35px;
  margin-right: 15px;
}
.table-box {
  margin: 30px;
  overflow: auto;
}
.table-right-btn {
  text-align: right;
  flex: 1;
  padding-right: 40px;
}
.table-item-time {
  flex: 1;
  color: #999;
}
</style>
