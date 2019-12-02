<template>
  <div class="system-customer">
    <div class="title">业务参数设置</div>
    <div class="customer-content">
      <!-- 客户管理导航 -->
      <div class="system-view-nav">
        <el-menu
          default-active="follow-log-type-set"
          @select="menuSelect">
          <el-menu-item
            v-for="(item, index) in menuList"
            :key="index"
            :index="item.key">
            <span slot="title">{{ item.label }}</span>
          </el-menu-item>
        </el-menu>
      </div>
      <keep-alive>
        <component
          :is="menuIndex"
          :types="types"
          class="system-view-content"/>
      </keep-alive>
    </div>
  </div>
</template>

<script>
import FollowLogTypeSet from './components/followLogTypeSet' // 跟进记录类型设置
import BusinessGroupSet from './components/businessGroupSet' // 商机组设置
import ProductCategorySet from './components/productCategorySet' // 产品类别设置
import ContractExpireSet from './components/contractExpireSet' // 合同到期提醒设置
import CustomerLimitSet from './components/customerLimitSet' // 拥有/锁定客户数限制

export default {
  name: 'BizParam',

  components: {
    BusinessGroupSet,
    ProductCategorySet,
    FollowLogTypeSet,
    ContractExpireSet,
    CustomerLimitSet
  },

  data() {
    return {
      menuList: [
        { label: '跟进记录类型设置', key: 'follow-log-type-set' },
        { label: '商机组设置', key: 'business-group-set' },
        { label: '产品类别设置', key: 'product-category-set' },
        { label: '合同到期提醒设置', key: 'contract-expire-set' },
        { label: '拥有客户数限制', key: 'own' },
        { label: '锁定客户数限制', key: 'lock' }
      ],
      menuIndex: 'follow-log-type-set',
      types: '' // 区分拥有客户 和 锁定客户
    }
  },

  methods: {
    /**
     * 菜单选择
     */
    menuSelect(i, key) {
      if (i == 'own' || i == 'lock') {
        this.types = {
          own: 1,
          lock: 2
        }[i]
        this.menuIndex = 'customer-limit-set'
      } else {
        this.menuIndex = i
      }
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.system-customer {
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
.system-view-nav {
  min-width: 200px;
  background: #fff;
  margin-right: 10px;
  padding-top: 20px;
  border: 1px solid #e6e6e6;
}
.system-view-content {
  flex: 1;
  border: 1px solid #e6e6e6;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow-x: auto;
}
.system-view-nav .is-active {
  border-right: 2px solid #46cdcf;
  background-color: #ecf5ff;
}
.system-view-nav /deep/ .el-menu {
  border-right: none;
  margin-right: 1px;
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
</style>
