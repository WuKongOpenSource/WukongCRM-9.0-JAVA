<template>
  <slide-view
    v-empty="!canShowDetail"
    :listener-ids="listenerIDs"
    :no-listener-ids="noListenerIDs"
    :no-listener-class="noListenerClass"
    :body-style="{padding: 0, height: '100%'}"
    xs-empty-icon="nopermission"
    xs-empty-text="暂无权限"
    @side-close="hideView">
    <flexbox
      v-loading="loading"
      v-if="canShowDetail"
      direction="column"
      align="stretch"
      class="d-container">
      <c-r-m-detail-head
        :detail="detailData"
        :head-details="headDetails"
        :id="id"
        crm-type="business"
        @handle="detailHeadHandle"
        @close="hideView">
        <div class="busi-line"/>
      </c-r-m-detail-head>
      <div
        v-if="status.length > 0"
        style="padding:10px 50px;">
        <flexbox
          :style="{'opacity' : detailData.isEnd != 0 ? 1 : 1}"
          class="busi-state">
          <a
            v-for="(item, index) in status"
            :key="index">
            <el-popover
              :title="item.name"
              :content="'赢单率' + item.rate + '%'"
              placement="bottom"
              width="150"
              trigger="hover">
              <div
                v-if="status.length -1 !=index"
                slot="reference"
                :class="item.class"
                class="busi-state-item"
                @click="handleStatuChange(item, index)">
                {{ item.name }}
                <div
                  v-if="index==0"
                  class="state-circle circle-left"/>
                <div
                  v-if="index!=0"
                  class="state-arrow arrow-left"/>
                <div class="state-arrow arrow-right"/>
              </div>
            </el-popover>

            <el-popover
              v-if="status.length -1 ==index"
              :title="item.type == 0 ? '' : item.title"
              :content="item.type == 0 ? '' : item.detail"
              placement="bottom"
              width="150"
              trigger="hover">
              <div
                v-if="item.type == 0"
                class="state-handel-cont">
                <flexbox
                  v-for="(item, index) in statuHandleItems"
                  :key="index"
                  class="state-handel-item"
                  @click.native="handleStatuResult(item, index)">
                  <img
                    :src="item.img"
                    class="state-handel-item-img">
                  <div class="state-handel-item-name">{{ item.name }}</div>
                  <div class="state-handel-item-value">{{ item.value }}</div>
                </flexbox>
              </div>
              <div
                slot="reference"
                :class="item.class"
                class="busi-state-item">
                <i
                  :class="item.overIcon"
                  style="margin-right:8px;"/>
                {{ item.name }}
                <div class="state-arrow arrow-left"/>
                <div class="state-circle circle-right"/>
              </div>
            </el-popover>
          </a>
        </flexbox>

      </div>
      <div class="tabs">
        <el-tabs
          v-model="tabCurrentName"
          @tab-click="handleClick">
          <el-tab-pane
            v-for="(item, index) in tabnames"
            :key="index"
            :label="item.label"
            :name="item.name"/>
        </el-tabs>
      </div>
      <div
        id="follow-log-content"
        class="t-loading-content">
        <keep-alive>
          <component
            :is="tabName"
            :detail="detailData"
            :id="id"
            crm-type="business"/>
        </keep-alive>
      </div>
    </flexbox>
    <c-r-m-create-view
      v-if="isCreate"
      :action="{type: 'update', id: id, batchId: detailData.batchId}"
      crm-type="business"
      @save-success="editSaveSuccess"
      @hiden-view="isCreate=false"/>
  </slide-view>
</template>

<script>
import {
  crmBusinessRead,
  crmBusinessAdvance,
  crmBusinessStatusById
} from '@/api/customermanagement/business'

import SlideView from '@/components/SlideView'
import CRMDetailHead from '../components/CRMDetailHead'
import BusinessFollow from './components/BusinessFollow' // 跟进记录
import CRMBaseInfo from '../components/CRMBaseInfo' // 商机基本信息
import RelativeContract from '../components/RelativeContract' // 相关合同
import RelativeContacts from '../components/RelativeContacts' // 相关联系人
import RelativeHandle from '../components/RelativeHandle' // 相关操作
import RelativeTeam from '../components/RelativeTeam' // 相关团队
import RelativeProduct from '../components/RelativeProduct' // 相关团队
import RelativeFiles from '../components/RelativeFiles' // 相关附件

import CRMCreateView from '../components/CRMCreateView' // 新建页面

import detail from '../mixins/detail'

export default {
  /** 客户管理 的 商机详情 */
  name: 'BusinessDetail',
  components: {
    SlideView,
    CRMDetailHead,
    BusinessFollow,
    CRMBaseInfo,
    RelativeContract,
    RelativeContacts,
    RelativeHandle,
    RelativeTeam,
    RelativeProduct,
    RelativeFiles,
    CRMCreateView
  },
  mixins: [detail],
  props: {
    // 详情信息id
    id: [String, Number],
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
      loading: false, // 展示加载loading
      crmType: 'business',
      detailData: {}, // read 详情
      headDetails: [
        { title: '客户名称', value: '' },
        { title: '商机金额（元）', value: '' },
        { title: '商机状态', value: '' },
        { title: '负责人', value: '' },
        { title: '创建时间', value: '' }
      ],
      tabCurrentName: 'followlog',
      /** 商机状态数据 */
      status: [],
      /** 完结状态 */
      statuHandleItems: [
        {
          name: '赢单',
          type: 1,
          value: '100%',
          img: require('@/assets/img/check_suc.png')
        },
        {
          name: '输单',
          type: 2,
          value: '0%',
          img: require('@/assets/img/check_fail.png')
        },
        {
          name: '无效',
          type: 3,
          value: '0%',
          img: require('@/assets/img/check_cancel.png')
        }
      ],
      isCreate: false // 编辑操作
    }
  },
  computed: {
    tabName() {
      if (this.tabCurrentName == 'followlog') {
        return 'business-follow'
      } else if (this.tabCurrentName == 'basicinfo') {
        return 'c-r-m-base-info'
      } else if (this.tabCurrentName == 'team') {
        return 'relative-team'
      } else if (this.tabCurrentName == 'contract') {
        return 'relative-contract'
      } else if (this.tabCurrentName == 'operationlog') {
        return 'relative-handle'
      } else if (this.tabCurrentName == 'product') {
        return 'relative-product'
      } else if (this.tabCurrentName == 'file') {
        return 'relative-files'
      } else if (this.tabCurrentName == 'contacts') {
        return 'relative-contacts'
      }
      return ''
    },
    tabnames() {
      var tempsTabs = []
      tempsTabs.push({ label: '跟进记录', name: 'followlog' })
      if (this.crm.business && this.crm.business.read) {
        tempsTabs.push({ label: '基本信息', name: 'basicinfo' })
      }

      if (this.crm.contacts && this.crm.contacts.index) {
        tempsTabs.push({ label: '联系人', name: 'contacts' })
      }

      if (this.crm.contract && this.crm.contract.index) {
        tempsTabs.push({ label: '合同', name: 'contract' })
      }

      if (this.crm.product && this.crm.product.index) {
        tempsTabs.push({ label: '产品', name: 'product' })
      }

      tempsTabs.push({ label: '相关团队', name: 'team' })
      tempsTabs.push({ label: '附件', name: 'file' })
      tempsTabs.push({ label: '操作记录', name: 'operationlog' })
      return tempsTabs
    }
  },
  mounted() {},
  methods: {
    getDetial() {
      this.loading = true
      this.getBusinessStatusById()
      crmBusinessRead({
        businessId: this.id
      })
        .then(res => {
          this.loading = false
          this.detailData = res.data

          this.headDetails[0].value = res.data.customerName

          this.headDetails[1].value = this.moneyFormat(res.data.money)
          this.headDetails[2].value = res.data.typeName
          // // 负责人
          this.headDetails[3].value = res.data.ownerUserName
          this.headDetails[4].value = res.data.createTime
        })
        .catch(() => {
          this.loading = false
        })
    },
    // 获取详情下的状态信息
    getBusinessStatusById() {
      this.loading = true
      crmBusinessStatusById({
        businessId: this.id
      })
        .then(res => {
          this.loading = false
          const data = res.data
          this.handleBusinessStatus(data.isEnd, data.statusId, data.statusList, data.statusRemark)
        })
        .catch(() => {
          this.loading = false
        })
    },
    //* * 点击关闭按钮隐藏视图 */
    hideView() {
      this.$emit('hide-view')
    },
    //* * tab标签点击 */
    handleClick(tab, event) {},
    /** 处理商机状态数据 */
    handleBusinessStatus(isEnd, statusId, statusList, statusRemark) {
      this.status = []
      if (statusList && statusList.length > 0) {
        const isdoing = isEnd == 0
        let isdoingIndex = -1
        for (let index = 0; index < statusList.length; index++) {
          const item = statusList[index]
          if (isdoing) {
            if (item.statusId == statusId) {
              isdoingIndex = index
              item['class'] = 'state-suc'
            } else {
              item['class'] = isdoingIndex >= 0 ? 'state-undo' : 'state-suc'
            }
          } else {
            if (item.statusId == statusId) {
              isdoingIndex = index
              item['class'] = 'state-suc'
            } else {
              item['class'] = isdoingIndex >= 0 ? 'state-invalid' : 'state-suc'
            }
          }
          this.status.push(item)
        }

        const overItem = { type: isEnd }
        if (isEnd == 0) {
          overItem.name = '结束'
          overItem['overIcon'] = ['el-icon-arrow-down', 'el-icon--right']
          if (isdoingIndex == statusList.length - 1) {
            overItem['class'] = 'state-doing'
          } else {
            if (this.status.length > 0 && statusId != 0) {
              // 有推进状态 才会有下一阶段
              this.status[isdoingIndex + 1].class = 'state-doing'
            }
            overItem['class'] = 'state-undo'
          }
        } else if (isEnd == 1) {
          overItem.name = '赢单'
          overItem.title = '赢单' // 详情标题 和 内容
          overItem.detail = '赢单率100%'
          overItem['overIcon'] = ['el-icon-check', 'el-icon--right']
          overItem['class'] = 'state-suc'
        } else if (isEnd == 2) {
          overItem.name = '输单'
          overItem.title = '赢单率0%'
          overItem.detail = statusRemark
          overItem['overIcon'] = ['el-icon-circle-close', 'el-icon--right']
          overItem['class'] = 'state-fail'
        } else if (isEnd == 3) {
          overItem.name = '无效'
          overItem.title = '赢单率0%'
          overItem.detail = statusRemark
          overItem['overIcon'] = ['el-icon-remove-outline', 'el-icon--right']
          overItem['class'] = 'state-invalid'
        }
        this.status.push(overItem)
      }
    },
    /** 操作状态改变 */
    handleStatuChange(item, index) {
      if (this.detailData.isEnd != 0) {
        // 非完结状态下 可推进
        return
      }
      if (!item.isdoing) {
        var message = '确定进入' + item.name + '阶段'
        this.$confirm(message, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            this.loading = true
            crmBusinessAdvance({
              businessId: this.id,
              statusId: item.statusId
            })
              .then(res => {
                this.loading = false
                this.$message.success('操作成功')
                this.getDetial()
              })
              .catch(() => {
                this.loading = false
              })
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '已取消操作'
            })
          })
      }
    },
    /** 完结状态结果 */
    handleStatuResult(item, index) {
      if (this.detailData.isEnd != 0) {
        // 非完结状态下 可推进
        return
      }
      /** 输单 和 无效 */
      if (item.type == 2 || item.type == 3) {
        var message = '请填写' + item.name + '原因：'
        var title = item.name + '原因'
        this.$prompt(message, title, {
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        })
          .then(({ value }) => {
            this.loading = true
            crmBusinessAdvance({
              businessId: this.id,
              statusId: item.statusId,
              isEnd: item.type,
              statusRemark: value
            })
              .then(res => {
                this.loading = false
                this.$message.success('操作成功')
                this.getDetial()
              })
              .catch(() => {
                this.loading = false
              })
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '取消输入'
            })
          })
      } else {
        var message = '确定将当前商机设为' + item.name + '吗?'
        this.$confirm(message, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            this.loading = true
            crmBusinessAdvance({
              businessId: this.id,
              statusId: item.statusId,
              isEnd: item.type
            })
              .then(res => {
                this.loading = false
                this.$message.success('操作成功')
                this.getDetial()
              })
              .catch(() => {
                this.loading = false
              })
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '已取消操作'
            })
          })
      }
    },
    editSaveSuccess() {
      this.$emit('handle', { type: 'save-success' })
      this.getDetial()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../styles/crmdetail.scss';
.busi-line {
  position: absolute;
  bottom: 0;
  left: 17px;
  right: 17px;
  height: 1px;
  background-color: #e6e6e6;
}

.busi-state {
  position: relative;
  padding-left: 20px;
  overflow-x: auto;
  overflow-y: hidden;
  a {
    flex-shrink: 0;
  }
}

.busi-state-item {
  padding: 10px 30px;
  margin: 5px 0;
  height: 38px;
  position: relative;
  margin-right: 5px;
  border-top: 2px solid #ebebeb;
  border-bottom: 2px solid #ebebeb;
  .state-circle {
    width: 38px;
    height: 38px;
    border-radius: 19px;
    background-color: white;
    transform: rotate(45deg);
    position: absolute;
    z-index: -1;
  }
  .state-arrow {
    width: 38px;
    height: 38px;
    transform: scale(0.707) rotate(45deg);
    border-top: 2px solid #ebebeb;
    border-right: 2px solid #ebebeb;
    background-color: white;
    position: absolute;
  }
}

.state-undo {
  border-top: 2px solid #ebebeb;
  border-bottom: 2px solid #ebebeb;
  background-color: white;
  color: #666;
  .state-circle {
    border-color: #ebebeb;
    background-color: white;
  }
  .state-arrow {
    border-color: #ebebeb;
    background-color: white;
  }
}

.state-doing {
  border-top: 2px solid #34cecf;
  border-bottom: 2px solid #34cecf;
  background-color: white;
  color: #34cecf;
  .circle-left {
    border-color: #34cecf;
    background-color: white;
  }
  .circle-right {
    border-color: #34cecf;
    background-color: white;
  }
  .arrow-left {
    border-color: #34cecf;
    background-color: white;
  }
  .arrow-right {
    border-color: #34cecf;
    background-color: white;
  }
}

.state-suc {
  border-top: 2px solid #34cecf;
  border-bottom: 2px solid #34cecf;
  background-color: #34cecf;
  color: white;
  .circle-left {
    border-color: #34cecf;
    background-color: #34cecf;
  }
  .circle-right {
    border-color: #34cecf;
    background-color: #34cecf;
  }
  .arrow-left {
    border-color: #34cecf;
    background-color: white;
  }
  .arrow-right {
    border-color: #34cecf;
    background-color: #34cecf;
  }
}

.state-fail {
  border-top: 2px solid #ff6767;
  border-bottom: 2px solid #ff6767;
  background-color: #ff6767;
  color: white;
  .circle-left {
    border-color: #ff6767;
    background-color: #ff6767;
  }
  .circle-right {
    border-color: #ff6767;
    background-color: #ff6767;
  }
  .arrow-left {
    border-color: #ff6767;
    background-color: white;
  }
  .arrow-right {
    border-color: #ff6767;
    background-color: #ff6767;
  }
}

.state-invalid {
  border-top: 2px solid #ebebeb;
  border-bottom: 2px solid #ebebeb;
  background-color: #ebebeb;
  color: #666;
  .state-circle {
    border-color: #ebebeb;
    background-color: #ebebeb;
  }
  .state-arrow {
    border-color: #ebebeb;
    background-color: #ebebeb;
  }

  .circle-left {
    border-color: #ebebeb;
    background-color: #ebebeb;
  }
  .circle-right {
    border-color: #ebebeb;
    background-color: #ebebeb;
  }
  .arrow-left {
    border-color: #ebebeb;
    background-color: white;
  }
  .arrow-right {
    border-color: #ebebeb;
    background-color: #ebebeb;
  }
}

.arrow-right {
  z-index: 1;
  top: -2px;
  right: -19px;
}
.arrow-left {
  top: -2px;
  left: -18px;
}
.circle-right {
  border-top: 2px solid #ebebeb;
  border-right: 2px solid #ebebeb;
  z-index: 1;
  top: -2px;
  right: -15.5px;
}
.circle-left {
  border-left: 2px solid #ebebeb;
  border-bottom: 2px solid #ebebeb;
  top: -2px;
  left: -17px;
}

/** 状态操作布局 */
.state-handel-cont {
  font-size: 13px;
  color: #333;
  .state-handel-item {
    padding: 10px 0;
    cursor: pointer;
    .state-handel-item-img {
      width: 16px;
      height: 16px;
      border-radius: 8px;
      margin-right: 8px;
      flex-shrink: 0;
      display: block;
    }
    .state-handel-item-name {
      flex: 1;
    }
    .state-handel-item-value {
      flex-shrink: 0;
    }
  }
  .state-handel-item:hover {
    background-color: #f7f8fa;
  }
}
</style>
