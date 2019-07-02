<template>
  <div>
    <flexbox v-show="selectionList.length == 0"
             class="th-container">
      <div v-if="!isSeas">场景：</div>
      <el-popover v-if="!isSeas"
                  trigger="click"
                  popper-class="no-padding-popover"
                  v-model="showScene"
                  width="150">
        <flexbox slot="reference">
          <div class="condition_title">{{sceneData.name || getDefaultSceneName()}}</div>
          <i class="el-icon-arrow-down el-icon--right"
             style="color:#777;"></i>
        </flexbox>
        <scene-list ref="sceneList"
                    :crmType="crmType"
                    @scene="sceneSelect"
                    @scene-handle="sceneHandle"
                    @hidden-scene="showScene=false"></scene-list>
      </el-popover>
      <img @click="showFilterClick"
           class="c-filtrate"
           :style="{ 'margin-left': isSeas ? 0 : '30px'}"
           src="@/assets/img/c_filtrate.png" />
      <div class="condition_title"
           @click="showFilterClick">高级筛选</div>
      <filter-form :fieldList="fieldList"
                   :dialogVisible.sync="showFilter"
                   :obj="filterObj"
                   :crmType="crmType"
                   :isSeas="isSeas"
                   @filter="handleFilter">
      </filter-form>
    </flexbox>
    <flexbox v-if="selectionList.length > 0"
             class="selection-bar">
      <div class="selected—title">已选中<span class="selected—count">{{selectionList.length}}</span>项</div>
      <flexbox class="selection-items-box">
        <flexbox class="selection-item"
                 v-for="(item, index) in getSelectionHandleItemsInfo()"
                 :key="index"
                 v-if="whetherTypeShowByPermision(item.type)"
                 @click.native="selectionBarClick(item.type)">
          <img class="selection-item-icon"
               :src="item.icon" />
          <div class="selection-item-name">{{item.name}}</div>
        </flexbox>
      </flexbox>
    </flexbox>
    <filter-content v-if="filterObj.form && filterObj.form.length > 0"
                    :obj="filterObj"
                    @delete="handleDeleteField">
    </filter-content>

    <transfer-handle :crmType="crmType"
                     :selectionList="selectionList"
                     @handle="handleCallBack"
                     :dialogVisible.sync="transferDialogShow"></transfer-handle>
    <teams-handle :crmType="crmType"
                  :title="teamsTitle"
                  :selectionList="selectionList"
                  @handle="handleCallBack"
                  :dialogVisible.sync="teamsDialogShow"></teams-handle>
    <alloc-handle :crmType="crmType"
                  :selectionList="selectionList"
                  @handle="handleCallBack"
                  :dialogVisible.sync="allocDialogShow"></alloc-handle>

    <scene-set :dialogVisible.sync="showSceneSet"
               @save-success="updateSceneList"
               :crmType="crmType">
    </scene-set>

    <scene-create :fieldList="fieldList"
                  :crmType="crmType"
                  :dialogVisible.sync="showSceneCreate"
                  @saveSuccess="updateSceneList"
                  :obj="sceneFilterObj">
    </scene-create>
  </div>
</template>

<script type="text/javascript">
import { mapGetters } from 'vuex'
import crmTypeModel from '@/views/customermanagement/model/crmTypeModel'
import {
  filterIndexfields,
  crmSceneIndex,
  crmSceneSave
} from '@/api/customermanagement/common'
import {
  crmLeadsTransform,
  crmLeadsExcelExport,
  crmLeadsDelete
} from '@/api/customermanagement/clue'
import {
  crmCustomerLock,
  crmCustomerPutInPool,
  crmCustomerExcelExport,
  crmCustomerPoolExcelExportAPI,
  crmCustomerDelete,
  crmCustomerReceive
} from '@/api/customermanagement/customer'
import {
  crmContactsDelete,
  crmContactsExcelExport
} from '@/api/customermanagement/contacts'
import { crmBusinessDelete } from '@/api/customermanagement/business'
import { crmContractDelete } from '@/api/customermanagement/contract'
import { crmReceivablesDelete } from '@/api/customermanagement/money'
import {
  crmProductStatus,
  crmProductExcelExport
} from '@/api/customermanagement/product'

import filterForm from './filterForm'
import filterContent from './filterForm/filterContent'
import SceneList from './sceneForm/SceneList' // 场景
import SceneSet from './sceneForm/SceneSet' // 场景设置
import SceneCreate from './sceneForm/SceneCreate'

import TransferHandle from './selectionHandle/TransferHandle' // 转移
import TeamsHandle from './selectionHandle/TeamsHandle' // 操作团队成员
import AllocHandle from './selectionHandle/AllocHandle' // 公海分配操作

export default {
  name: 'CRM-table-head', //客户管理下 重要提醒 回款计划提醒
  components: {
    filterForm,
    filterContent,
    SceneList,
    TransferHandle,
    TeamsHandle,
    AllocHandle,
    SceneCreate,
    SceneSet
  },
  computed: {
    ...mapGetters(['crm', 'CRMConfig'])
  },
  data() {
    return {
      sceneTypes: [
        { type: 'enter', name: '我负责的' },
        { type: 'out', name: '我' }
      ],
      sceneType: null,
      showScene: false, // 场景操作
      showFilter: false, // 控制筛选框
      fieldList: [],
      filterObj: { form: [] }, // 筛选确定数据

      sceneData: { id: '', bydata: '', name: '' },
      showSceneSet: false, // 展示场景设置
      showSceneCreate: false, // 展示场景添加
      sceneFilterObj: { form: [] }, // 筛选确定数据

      /** 勾选操作数据 */
      selectionList: [],
      transferDialogShow: false,
      teamsDialogShow: false, // 团队操作提示框
      teamsTitle: '', // 团队操作标题名
      allocDialogShow: false // 公海分配操作提示框
    }
  },
  watch: {},
  props: {
    title: {
      type: String,
      default: ''
    },
    /** 没有值就是全部类型 有值就是当个类型 */
    crmType: {
      type: String,
      default: ''
    },
    // 辅助 使用 公海没有场景
    isSeas: {
      type: Boolean,
      default: false
    }
  },
  mounted() {},
  methods: {
    /** 发布 时候的类型选择 */
    handleTypeDrop(command) {
      this.sceneType = command
    },
    /** 展示高级筛选 */
    showFilterClick() {
      this.getFilterFieldInfo()
    },
    // 获取高级筛选字段数据
    getFilterFieldInfo() {
      filterIndexfields({
        label: crmTypeModel[this.crmType]
      })
        .then(res => {
          this.fieldList = res.data
          this.showFilter = true
        })
        .catch(() => {})
    },
    handleFilter(form) {
      this.filterObj = form
      this.showFilter = false
      if (form.saveChecked) {
        crmSceneSave({
          type: crmTypeModel[this.crmType],
          isDefault: form.saveDefault ? 1 : 0,
          name: form.saveName,
          data: JSON.stringify(form.obj)
        })
          .then(res => {
            this.updateSceneList()
          })
          .catch(() => {})
      }
      this.$emit('filter', form.obj)
    },
    //删除
    handleDeleteField(data) {
      this.filterObj = data.obj
      this.$emit('filter', this.filterObj.obj)
    },
    // 场景操作
    /** 选择了场景 */
    sceneSelect(data) {
      this.sceneData = data
      this.$emit('scene', data)
    },
    sceneHandle(data) {
      if (data.type == 'set') {
        this.showSceneSet = true
      } else if (data.type == 'add') {
        filterIndexfields({
          label: crmTypeModel[this.crmType]
        })
          .then(res => {
            this.fieldList = res.data
            this.showSceneCreate = true
          })
          .catch(() => {})
      }
    },
    /**  创建保存成功 */
    updateSceneList() {
      this.$refs.sceneList.getSceneList()
    },
    /** 勾选后的表头操作 */
    headSelectionChange(array) {
      this.selectionList = array
    },
    /** 操作 */
    selectionBarClick(type) {
      if (type == 'transfer') {
        // 转移
        this.transferDialogShow = true
      } else if (type == 'export') {
        var params = {}
        var request = null
        if (this.isSeas) {
          request = crmCustomerPoolExcelExportAPI
          params.ids = this.selectionList
            .map(function(item, index, array) {
              return item.customerId
            })
            .join(',')
        } else if (this.crmType == 'customer') {
          request = crmCustomerExcelExport
          params.ids = this.selectionList
            .map(function(item, index, array) {
              return item.customerId
            })
            .join(',')
        } else if (this.crmType == 'leads') {
          request = crmLeadsExcelExport
          params.ids = this.selectionList
            .map(function(item, index, array) {
              return item.leadsId
            })
            .join(',')
        } else if (this.crmType == 'contacts') {
          request = crmContactsExcelExport
          params.ids = this.selectionList
            .map(function(item, index, array) {
              return item.contactsId
            })
            .join(',')
        } else if (this.crmType == 'product') {
          request = crmProductExcelExport
          params.ids = this.selectionList
            .map(function(item, index, array) {
              return item.productId
            })
            .join(',')
        }
        request(params)
          .then(res => {
            var blob = new Blob([res.data], {
              type: 'application/vnd.ms-excel;charset=utf-8'
            })
            var downloadElement = document.createElement('a')
            var href = window.URL.createObjectURL(blob) //创建下载的链接
            downloadElement.href = href
            downloadElement.download =
              decodeURI(
                res.headers['content-disposition'].split('filename=')[1]
              ) || '' //下载后文件名
            document.body.appendChild(downloadElement)
            downloadElement.click() //点击下载
            document.body.removeChild(downloadElement) //下载完成移除元素
            window.URL.revokeObjectURL(href) //释放掉blob对象
          })
          .catch(() => {})
      } else if (
        type == 'transform' ||
        type == 'put_seas' ||
        type == 'delete' ||
        type == 'lock' ||
        type == 'unlock' ||
        type == 'start' ||
        type == 'disable' ||
        type == 'get'
      ) {
        var message = ''
        if (type == 'transform') {
          message = '确定将这些线索转换为客户吗?'
        } else if (type == 'put_seas') {
          message = '确定转移到公海吗?'
        } else if (type == 'delete') {
          message = '确定要删除这些数据吗?'
        } else if (type == 'lock') {
          message = '确定要锁定这些客户吗？锁定后将不会掉入公海。'
        } else if (type == 'unlock') {
          message = '确定要解锁这些客户吗？'
        } else if (type == 'start') {
          message = '确定要上架这些产品吗?'
        } else if (type == 'disable') {
          message = '确定要下架这些产品吗?'
        } else if (type == 'get') {
          message = '确定要领取该客户吗?'
        }
        this.$confirm(message, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            this.confirmHandle(type)
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '已取消操作'
            })
          })
      } else if (type == 'add_user') {
        // 团队操作
        this.teamsTitle = '添加团队成员'
        this.teamsDialogShow = true
      } else if (type == 'delete_user') {
        // 团队操作
        this.teamsTitle = '移除团队成员'
        this.teamsDialogShow = true
      } else if (type == 'alloc') {
        // 公海分配操作
        this.allocDialogShow = true
      }
    },
    confirmHandle(type) {
      if (type === 'lock' || type === 'unlock') {
        var customerId = this.selectionList.map(function(item, index, array) {
          return item.customerId
        })
        crmCustomerLock({
          isLock: type === 'lock' ? '1' : '0', // 1锁0不锁
          ids: customerId.join(',')
        })
          .then(res => {
            this.$message({
              type: 'success',
              message: '操作成功'
            })
            this.$emit('handle', { type: type })
          })
          .catch(() => {})
      } else if (type === 'put_seas') {
        var customerId = this.selectionList.map(function(item, index, array) {
          return item.customerId
        })
        crmCustomerPutInPool({
          ids: customerId.join(',')
        })
          .then(res => {
            this.$message({
              type: 'success',
              message: '操作成功'
            })
            this.$emit('handle', { type: type })
          })
          .catch(() => {})
      } else if (type === 'transform') {
        var leadsId = this.selectionList.map(function(item, index, array) {
          return item.leadsId
        })
        crmLeadsTransform({
          leadsIds: leadsId.join(',')
        })
          .then(res => {
            this.$message({
              type: 'success',
              message: '转化成功'
            })
            this.$emit('handle', { type: type })
          })
          .catch(() => {})
      } else if (type === 'start' || type === 'disable') {
        var productId = this.selectionList.map(function(item, index, array) {
          return item.productId
        })
        crmProductStatus({
          ids: productId.join(','),
          status: type === 'start' ? '1' : '0'
        })
          .then(res => {
            this.$message({
              type: 'success',
              message: '操作成功'
            })
            this.$emit('handle', { type: type })
          })
          .catch(() => {})
      } else if (type === 'delete') {
        let self = this
        var ids = this.selectionList.map(function(item, index, array) {
          return item[self.crmType + 'Id']
        })
        let request = {
          leads: crmLeadsDelete,
          customer: crmCustomerDelete,
          contacts: crmContactsDelete,
          business: crmBusinessDelete,
          contract: crmContractDelete,
          receivables: crmReceivablesDelete
        }[this.crmType]
        request({
          [this.crmType + 'Ids']: ids.join(',')
        })
          .then(res => {
            this.$message({
              type: 'success',
              message: '删除成功'
            })
            this.$emit('handle', { type: type })
          })
          .catch(() => {})
      } else if (type === 'get') {
        // 领取
        var customerId = this.selectionList.map(function(item, index, array) {
          return item.customerId
        })
        crmCustomerReceive({
          ids: customerId.join(',')
        })
          .then(res => {
            this.$message({
              type: 'success',
              message: '操作成功'
            })
            this.$emit('handle', { type: type })
          })
          .catch(() => {})
      }
    },
    /** 获取展示items */
    getSelectionHandleItemsInfo() {
      let handleInfos = {
        transfer: {
          name: '转移',
          type: 'transfer',
          icon: require('@/assets/img/selection_transfer.png')
        },
        transform: {
          name: '转化为客户',
          type: 'transform',
          icon: require('@/assets/img/selection_convert_customer.png')
        },
        export: {
          name: '导出选中',
          type: 'export',
          icon: require('@/assets/img/selection_export.png')
        },
        delete: {
          name: '删除',
          type: 'delete',
          icon: require('@/assets/img/selection_delete.png')
        },
        put_seas: {
          name: '放入公海',
          type: 'put_seas',
          icon: require('@/assets/img/selection_putseas.png')
        },
        lock: {
          name: '锁定',
          type: 'lock',
          icon: require('@/assets/img/selection_lock.png')
        },
        unlock: {
          name: '解锁',
          type: 'unlock',
          icon: require('@/assets/img/selection_unlock.png')
        },
        add_user: {
          name: '添加团队成员',
          type: 'add_user',
          icon: require('@/assets/img/selection_add_user.png')
        },
        delete_user: {
          name: '移除团队成员',
          type: 'delete_user',
          icon: require('@/assets/img/selection_delete_user.png')
        },
        alloc: {
          name: '分配',
          type: 'alloc',
          icon: require('@/assets/img/selection_alloc.png')
        },
        get: {
          name: '领取',
          type: 'get',
          icon: require('@/assets/img/selection_get.png')
        },
        start: {
          name: '上架',
          type: 'start',
          icon: require('@/assets/img/selection_start.png')
        },
        disable: {
          name: '下架',
          type: 'disable',
          icon: require('@/assets/img/selection_disable.png')
        }
      }
      if (this.crmType == 'leads') {
        return this.forSelectionHandleItems(handleInfos, [
          'transfer',
          'transform',
          'export',
          'delete'
        ])
      } else if (this.crmType == 'customer') {
        if (this.isSeas) {
          return this.forSelectionHandleItems(handleInfos, [
            'alloc',
            'get',
            'export'
          ])
        } else {
          return this.forSelectionHandleItems(handleInfos, [
            'transfer',
            'export',
            'put_seas',
            'delete',
            'lock',
            'unlock',
            'add_user',
            'delete_user'
          ])
        }
      } else if (this.crmType == 'contacts') {
        return this.forSelectionHandleItems(handleInfos, [
          'transfer',
          'export',
          'delete'
        ])
      } else if (this.crmType == 'business') {
        return this.forSelectionHandleItems(handleInfos, [
          'transfer',
          'delete',
          'add_user',
          'delete_user'
        ])
      } else if (this.crmType == 'contract') {
        return this.forSelectionHandleItems(handleInfos, [
          'transfer',
          'delete',
          'add_user',
          'delete_user'
        ])
      } else if (this.crmType == 'receivables') {
        return this.forSelectionHandleItems(handleInfos, ['delete'])
      } else if (this.crmType == 'product') {
        return this.forSelectionHandleItems(handleInfos, [
          'export',
          'start',
          'disable'
        ])
      }
    },
    forSelectionHandleItems(handleInfos, array) {
      var tempsHandles = []
      for (let index = 0; index < array.length; index++) {
        tempsHandles.push(handleInfos[array[index]])
      }
      return tempsHandles
    },
    // 判断是否展示
    whetherTypeShowByPermision: function(type) {
      if (type == 'transfer') {
        return this.sceneData.bydata == 'transform'
          ? false
          : this.crm[this.crmType].transfer
      } else if (type == 'transform') {
        return this.sceneData.bydata == 'transform'
          ? false
          : this.crm[this.crmType].transform
      } else if (type == 'export') {
        if (this.isSeas) {
          return this.crm.pool.excelexport
        }
        return this.crm[this.crmType].excelexport
      } else if (type == 'delete') {
        return this.crm[this.crmType].delete
      } else if (type == 'put_seas') {
        // 放入公海(客户)
        return this.crm[this.crmType].putinpool
      } else if (type == 'lock' || type == 'unlock') {
        // 锁定解锁(客户)
        return this.crm[this.crmType].lock && this.CRMConfig.customerConfig == 1
      } else if (type == 'add_user' || type == 'delete_user') {
        // 添加 移除团队成员
        return this.crm[this.crmType].teamsave
      } else if (type == 'alloc') {
        // 分配(公海)
        return this.crm.pool.distribute
      } else if (type == 'get') {
        // 领取(公海)
        return this.crm.pool.receive
      } else if (type == 'start' || type == 'disable') {
        // 上架 下架(产品)
        return this.crm[this.crmType].status
      }

      return true
    },
    // 子组件 回调的 结果
    handleCallBack(data) {
      this.$emit('handle', { type: data.type })
    },
    // 获取默认场景名字
    getDefaultSceneName() {
      if (this.crmType == 'leads') {
        return '全部线索'
      } else if (this.crmType == 'customer') {
        return '全部客户'
      } else if (this.crmType == 'contacts') {
        return '全部联系人'
      } else if (this.crmType == 'business') {
        return '全部商机'
      } else if (this.crmType == 'contract') {
        return '全部合同'
      } else if (this.crmType == 'receivables') {
        return '全部回款'
      } else if (this.crmType == 'product') {
        return '全部产品'
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.th-container {
  font-size: 13px;
  height: 50px;
  padding: 0 20px;
}
/** 场景和筛选 */
.condition_title {
  cursor: pointer;
}
.condition_title:hover {
  color: $xr-color-primary;
}

.m-arrow {
  margin: 0 8px;
}
.c-filtrate {
  margin: 0 10px 0 30px;
  width: 12px;
}

/** 勾选操作 */
.selection-bar {
  font-size: 12px;
  height: 50px;
  padding: 0 20px;
  color: #777;

  .selected—title {
    flex-shrink: 0;
    padding-right: 20px;
    border-right: 1px solid $--table-border-color;
    .selected—count {
      color: $xr-color-primary;
    }
  }
}

.selection-items-box {
  .selection-item {
    width: auto;
    padding: 15px;
    .selection-item-icon {
      display: block;
      margin-right: 5px;
      width: 15px;
      height: 15px;
    }
    .selection-item-name {
      cursor: pointer;
      color: #777;
    }
    .selection-item-name:hover {
      color: $xr-color-primary;
    }
  }
}
</style>
