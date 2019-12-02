<template>
  <div class="fl-c">
    <flexbox class="fl-h">
      <div
        v-photo="data.createUser"
        v-lazy:background-image="$options.filters.filterUserLazyImg(data.createUser.img)"
        class="div-photo fl-h-img"/>
      <div class="fl-h-b">
        <div class="fl-h-name">{{ data.createUser.realname }}</div>
        <div class="fl-h-time">{{ data.createTime }}</div>
      </div>
      <el-dropdown
        v-if="data.permission && (data.permission.isUpdate == 1 || data.permission.isDelete == 1)"
        trigger="click"
        @command="handleCommand">
        <i
          style="color:#CDCDCD;"
          class="el-icon-arrow-down el-icon-more"/>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item
            v-if="data.permission && data.permission.isUpdate == 1"
            command="edit">编辑</el-dropdown-item>
          <el-dropdown-item
            v-if="data.permission && data.permission.isDelete == 1"
            command="delete">删除</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </flexbox>
    <div class="fl-b">
      <div>
        <div
          :style="{'background-color': data.color ? data.color : '#fff'}"
          class="state-color"/>{{ data.title }}
      </div>
      <div class="fl-b-des">
        <flexbox class="fl-b-des-item">
          <div class="fl-b-des-item-name">参与人：</div>
          <div class="owner-list">
            <span
              v-for="(k, j) in data.ownerList"
              :key="j">
              <el-tooltip
                placement="bottom"
                effect="light"
                popper-class="tooltip-change-border">
                <div slot="content">
                  <span>{{ k.realname }}</span>
                </div>
                <div
                  v-photo="k"
                  v-lazy:background-image="$options.filters.filterUserLazyImg(k.img)"
                  :key="k.img"
                  class="div-photo header-circle"/>
              </el-tooltip>
            </span>
          </div>
        </flexbox>
        <flexbox class="fl-b-des-item">
          <div class="fl-b-des-item-name">开始时间：</div>
          <div>{{ data.startTime }}</div>
        </flexbox>
        <flexbox class="fl-b-des-item">
          <div class="fl-b-des-item-name">结束时间：</div>
          <div>{{ data.endTime }}</div>
        </flexbox>
        <flexbox class="fl-b-des-item">
          <div class="fl-b-des-item-name">备注：</div>
          <div>{{ data.remark }}</div>
        </flexbox>
      </div>
      <!-- 关联业务 -->
      <div
        v-if="data.contactsList.length > 0 || data.customerList.length > 0 || data.businessList.length > 0 || data.contractList.length > 0"
        class="related-business">
        <div class="label">关联业务</div>
        <p
          v-for="(contacts, i) in data.contactsList"
          :key="'contacts' + i"
          @click="checkRelatedDetail('contacts',contacts)">
          <img
            src="@/assets/img/relevance_business.png"
            alt="">
          联系人-{{ contacts.name }}
        </p>
        <p
          v-for="(customer, i) in data.customerList"
          :key="'customer' + i"
          @click="checkRelatedDetail('customer',customer)">
          <img
            src="@/assets/img/relevance_business.png"
            alt="">
          客户-{{ customer.customerName }}
        </p>
        <p
          v-for="(business, i) in data.businessList"
          :key="'business' + i"
          @click="checkRelatedDetail('business',business)">
          <img
            src="@/assets/img/relevance_business.png"
            alt="">
          商机-{{ business.businessName }}
        </p>
        <p
          v-for="(contract, i) in data.contractList"
          :key="'contract' + i"
          @click="checkRelatedDetail('contract',contract)">
          <img
            src="@/assets/img/relevance_business.png"
            alt="">
          合同-{{ contract.name }}
        </p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  /** 客户管理 的 客户详情 的 任务cell */
  name: 'FollowScheduleCell',
  components: {},
  filters: {
    getRemindtypeText: function(remindtype) {
      switch (remindtype) {
        case 0:
          return '无'
        case 1:
          return '准时提醒'
        case 2:
          return '5分钟前'
        case 3:
          return '15分钟前'
        case 4:
          return '30分钟前'
        case 5:
          return '一个小时前'
        case 6:
          return '二个小时前'
        case 7:
          return '一天前'
        case 8:
          return '二天前'
        case 9:
          return '一周前'
      }
      return ''
    }
  },
  props: {
    data: Object
  },
  data() {
    return {}
  },
  computed: {},
  mounted() {},
  methods: {
    // 关联详情
    checkRelatedDetail(type, data) {
      this.$emit('on-handle', {
        type: 'related-detail',
        data: { type: type, item: data }
      })
    },
    handleCommand(command) {
      this.$emit('on-handle', {
        type: command,
        data: { item: this.data }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../styles/followcell.scss';
.fl-h-state {
  width: auto;
  .fl-h-state-mark {
    margin-right: 8px;
    width: 10px;
    height: 10px;
    border-radius: 5px;
  }
  .fl-h-state-name {
    font-size: 13px;
    color: #333;
  }
}

.el-checkbox /deep/ .el-checkbox__label {
  font-size: 13px;
}

.fl-b-des {
  margin: 15px 0;
  .fl-b-des-item {
    font-size: 12px;
    color: #777;
    margin: 8px 0;
    .fl-b-des-item-name {
      width: 60px;
      margin-right: 10px;
    }
  }
}

.state-color {
  width: 15px;
  height: 15px;
  display: inline-block;
  border-radius: 7.5px;
  margin-right: 2px;
}

// 关联业务
.related-business {
  margin: 15px 0;
  .label {
    font-size: 13px;
    margin-bottom: 7px;
  }
  p {
    cursor: pointer;
    color: #3e84e9;
    background: #f5f7fa;
    line-height: 30px;
    margin-bottom: 5px;
    font-size: 13px;
    padding-left: 7px;
    border-radius: 2px;
    img {
      width: 16px;
      vertical-align: middle;
    }
  }
}

// 参与人
.owner-list {
  display: inline-block;
  vertical-align: middle;
  span {
    display: inline-block;
  }
  .div-photo {
    width: 25px;
    height: 25px;
    margin-right: 10px;
  }
}
</style>
