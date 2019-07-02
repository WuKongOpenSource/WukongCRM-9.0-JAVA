<template>
  <div class="system-customer">
    <div class="title">客户管理</div>
    <div class="customer-content"
         v-loading="loading">
      <!-- 客户保护规则设置 -->
      <div class="system-view-table">
        <div class="content-title">
          <span>客户公海规则设置</span>
          <el-button type="primary"
                     class="rt"
                     size="medium"
                     @click="customerSettingSave">保存</el-button>
        </div>
        <div class="customer-setting">
          <label>客户公海规则设置</label>
          <div class="customer-radio">
            <el-radio-group v-model="customerData.type">
              <el-radio :label="0">不启用</el-radio>
              <el-radio :label="1">启用</el-radio>
            </el-radio-group>
            <br />
            <el-input-number v-model="customerData.followupDay"
                             controls-position="right"
                             :min="0"
                             size="small"></el-input-number>
            <span>&nbsp;天不跟进或&nbsp;</span>
            <el-input-number v-model="customerData.dealDay"
                             controls-position="right"
                             :min="0"
                             size="small"></el-input-number>
            <span>&nbsp;天未成交</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { crmSettingConfig } from '@/api/systemManagement/SystemCustomer'

export default {
  name: 'customer', // 客户设置

  components: {},

  data() {
    return {
      loading: false, // 展示加载中效果
      // 客户掉报规则设置
      customerData: {
        type: 0,
        followupDay: 0,
        dealDay: 0
      }
    }
  },

  created() {
    this.customerSettingData()
  },

  methods: {
    /**
     * 客户保护规则
     */
    customerSettingData() {
      this.loading = true
      this.$store
        .dispatch('CRMSettingConfig')
        .then(res => {
          this.loading = false
          this.customerData.type = res.data.customerConfig
          this.customerData.followupDay = res.data.followupDay
          this.customerData.dealDay = res.data.dealDay
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * 设置保存
     */
    customerSettingSave() {
      this.loading = true
      crmSettingConfig(this.customerData)
        .then(res => {
          this.loading = false
          this.$message.success('操作成功')
        })
        .catch(() => {
          this.loading = false
        })
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
.customer-setting {
  padding: 30px;
}
.customer-radio {
  display: inline-block;
  vertical-align: top;
}
.customer-setting > label {
  padding-right: 30px;
}
.customer-radio > .el-radio-group {
  margin-bottom: 30px;
}
</style>
