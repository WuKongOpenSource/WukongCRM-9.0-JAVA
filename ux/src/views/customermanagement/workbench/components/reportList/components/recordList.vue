<template>
  <slide-view
    v-loading="loading"
    :listener-ids="listenerIDs"
    :no-listener-ids="noListenerIDs"
    :no-listener-class="noListenerClass"
    :body-style="{padding: 0, height: '100%'}"
    class="d-view"
    @side-close="hideView">
    <flexbox class="t-section">
      <img
        :src="crmIcon"
        class="t-img" >
      <div class="t-name">跟进记录</div>
      <img
        class="t-close"
        src="@/assets/img/task_close.png"
        @click="hideView" >
    </flexbox>
    <div
      id="follow-log-content"
      class="t-content">
      <follow-record-cell
        v-for="(item, index) in list"
        :item="item"
        :crm-type="crmType"
        :index="index"
        :key="index"
        @on-handle="cellHandle">
        <flexbox
          class="relate-cell"
          @click.native="checkRelationDetail(item.types, item.typesId)">
          <i
            :class="item.types | crmIconClass"
            class="wukong relate-cell-head crm-type"/>
          <div
            class="relate-cell-body"
            style="color: #6394E5;cursor: pointer;">{{ item.typesName }}</div>
        </flexbox>
      </follow-record-cell>
      <div class="load">
        <el-button
          :loading="loadMoreLoading"
          type="text">{{ loadMoreLoading ? '加载更多' : '没有更多了' }}</el-button>
      </div>
    </div>

    <c-r-m-full-screen-detail
      :visible.sync="showFullDetail"
      :crm-type="relationCrmType"
      :id="relationID"/>

  </slide-view>
</template>

<script>
import FollowRecordCell from '@/views/customermanagement/components/followLog/components/FollowRecordCell'
import SlideView from '@/components/SlideView'
import { crmIndexGetRecordListAPI } from '@/api/customermanagement/workbench'

export default {
  /** 跟进记录列表 */
  name: 'RecordList',

  components: {
    FollowRecordCell,
    SlideView,
    CRMFullScreenDetail: () =>
      import('@/views/customermanagement/components/CRMFullScreenDetail.vue')
  },

  filters: {
    crmIconClass(type) {
      return type && type.replace('crm_', 'wukong-')
    }
  },

  props: {
    crmType: String,
    params: Object,
    // 监听的dom 进行隐藏详情
    listenerIDs: {
      type: Array,
      default: () => {
        return ['crm-main-container']
      }
    },
    // 不监听
    noListenerIDs: {
      type: Array,
      default: () => {
        return []
      }
    },
    noListenerClass: {
      type: Array,
      default: () => {
        return ['el-table__body']
      }
    }
  },

  data() {
    return {
      loading: false,
      loadMoreLoading: true,
      // 判断是否发请求
      isPost: false,
      page: 1,
      list: [],

      showFullDetail: false, // 查看相关客户管理详情
      relationID: '', // 相关ID参数
      relationCrmType: '' // 相关类型
    }
  },

  computed: {
    crmIcon() {
      if (this.crmType === 'crm_customer') {
        return require('@/assets/img/customer_detail.png')
      } else if (this.crmType === 'crm_leads') {
        return require('@/assets/img/clue_detail.png')
      } else if (this.crmType === 'crm_business') {
        return require('@/assets/img/business_detail.png')
      } else if (this.crmType === 'crm_contacts') {
        return require('@/assets/img/contacts_detail.png')
      } else if (this.crmType === 'crm_contract') {
        return require('@/assets/img/contract_detail.png')
      } else if (this.crmType === 'crm_receivables') {
        return require('@/assets/img/money_detail.png')
      } else if (this.crmType === 'crm_product') {
        return require('@/assets/img/product_detail.png')
      }
      return ''
    }
  },

  watch: {
    params() {
      this.refreshList()
    }
  },

  mounted() {
    // 分批次加载
    const dom = document.getElementById('follow-log-content')
    dom.onscroll = () => {
      const scrollOff = dom.scrollTop + dom.clientHeight - dom.scrollHeight
      // 滚动条到底部的条件
      if (Math.abs(scrollOff) < 10 && this.loadMoreLoading == true) {
        if (!this.isPost) {
          this.isPost = true
          this.page++
          this.getList()
        } else {
          this.loadMoreLoading = false
        }
      }
    }
    this.getList()
  },

  methods: {
    refreshList() {
      this.page = 1
      this.list = []
      this.getList()
    },

    /**
     * 获取列表
     */
    getList() {
      this.loading = true
      crmIndexGetRecordListAPI({ page: this.page, limit: 15, ...this.params })
        .then(res => {
          this.list = this.list.concat(res.data.list)
          if (res.data.list.length < 15) {
            this.loadMoreLoading = false
          } else {
            this.loadMoreLoading = true
          }
          this.isPost = false
          this.loading = false
        })
        .catch(() => {
          this.isPost = false
          this.loading = false
        })
    },

    /**
     * 行布局删除
     */
    cellHandle(data) {
      if (data.type == 'delete') {
        this.list.splice(data.data.index, 1)
        this.$emit('handle')
      }
    },

    /**
     * 查看相关客户管理详情
     */
    checkRelationDetail(type, id) {
      this.relationID = id
      this.relationCrmType = type.replace('crm_', '')
      this.showFullDetail = true
    },

    /**
     * 点击关闭按钮隐藏视图
     */
    hideView() {
      this.$emit('hide')
    }
  }
}
</script>

<style lang="scss" scoped>
.t-section {
  position: relative;
  padding: 10px 17px;
  min-height: 60px;
  line-height: 40px;
  .t-img {
    display: block;
    width: 35px;
    height: 35px;
    margin-right: 10px;
  }
  .t-name {
    font-size: 14px;
    color: #333333;
    flex: 1;
  }

  .t-close {
    display: block;
    float: right;
    width: 40px;
    height: 40px;
    margin-left: 20px;
    padding: 10px;
    cursor: pointer;
  }
}

.load {
  color: #999;
  font-size: 13px;
  margin: 0 auto 15px;
  text-align: center;
  .el-button,
  .el-button:focus {
    color: #ccc;
    cursor: auto;
  }
}

.d-view {
  position: fixed;
  width: 950px;
  top: 0;
  bottom: 0;
  right: 0;
}

.t-content {
  margin: 0 30px;
  height: calc(100% - 80px);
  overflow-y: auto;
}

.relate-cell {
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 2px;
  position: relative;

  &-head {
    display: block;
    width: 15px;
    height: 15px;
    margin-right: 8px;
  }

  &-body {
    flex: 1;
    color: #333;
    font-size: 12px;
  }

  &-foot {
    display: block;
    width: 20px;
    padding: 0 4px;
    margin-right: 8px;
  }
}

.crm-type {
  color: rgb(99, 148, 229);
  font-size: 14px;
}
</style>
