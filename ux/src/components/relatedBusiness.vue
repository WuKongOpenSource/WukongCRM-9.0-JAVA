<template>
  <div
    :style="{'margin-left': marginLeft}"
    class="related-business">
    <!-- 新建- 关联业务 -->
    <el-popover
      v-if="!isTask && alterable"
      v-model="showPopover"
      placement="bottom"
      width="800"
      popper-class="no-padding-popover"
      trigger="click">
      <crm-relative
        v-if="showRelative"
        ref="crmrelative"
        :show="showPopover"
        :radio="false"
        :selected-data="relatedListData"
        :show-types="showTypes"
        @close="crmrelativeClose"
        @changeCheckout="checkInfos"/>
      <p
        v-if="showCRMPermission"
        slot="reference"
        class="add-file"
        @click="showRelative = true">
        <img
          src="@/assets/img/relevance_business.png"
          alt="">
        关联业务
      </p>
    </el-popover>
    <p
      v-if="!alterable"
      :style="{color: alterableColor}"
      class="alterable-p">关联业务</p>
    <div
      v-for="(items, key) in relatedListData"
      :key="key">
      <related-business-cell
        v-for="(item, itemIndex) in items"
        :data="item"
        :cell-index="itemIndex"
        :type="key"
        :key="itemIndex"
        :show-foot="isTask"
        @unbind="delRelevance"
        @detail="checkRelatedDetail(key, item)"/>
    </div>
    <!-- 任务页面显示格式 -->
    <el-popover
      v-if="isTask"
      v-model="showPopover"
      placement="bottom"
      width="800"
      popper-class="no-padding-popover"
      trigger="click">
      <crm-relative
        v-if="showTaskRelative"
        ref="crmrelative"
        :radio="false"
        :show="showPopover"
        :selected-data="relatedListData"
        :show-types="showTypes"
        @close="crmrelativeClose"
        @changeCheckout="checkInfos"/>
      <p
        v-if="showCRMPermission"
        slot="reference"
        class="add-file"
        @click="showTaskRelative = true">
        <img
          src="@/assets/img/relevance_business.png"
          alt="">
        关联业务
      </p>
    </el-popover>
  </div>
</template>

<script>
// 关联业务 - 弹出框
import { editTaskRelation } from '@/api/oamanagement/task'
import CrmRelative from '@/components/CreateCom/CrmRelative'
import RelatedBusinessCell from '@/views/OAManagement/components/relatedBusinessCell'
import { objDeepCopy } from '@/utils'
import { mapGetters } from 'vuex'

export default {
  components: {
    CrmRelative,
    RelatedBusinessCell
  },
  props: {
    marginLeft: {
      type: String,
      default: '20px'
    },
    // 编辑时传递所有关联数据   关联联系人-contacts 关联客户-customer 商机-business 合同-contract
    allData: {
      type: Object,
      default: () => {
        return {
          contacts: [],
          customer: [],
          business: [],
          contract: []
        }
      }
    },
    // 是否是任务页面
    isTask: {
      type: Boolean,
      default: false
    },
    taskID: Number,
    alterable: {
      type: Boolean,
      default: true
    },
    alterableColor: {
      type: String,
      default: '#999'
    }
  },
  data() {
    return {
      showTypes: ['customer', 'contacts', 'business', 'contract'],
      showPopover: false,
      relevanceAll: {
        customerIds: [],
        contractIds: [],
        contactsIds: [],
        businessIds: []
      },
      // 关联业务信息
      relatedListData: {},
      showRelative: false,
      // 展示任务相关
      showTaskRelative: false
    }
  },
  computed: {
    ...mapGetters(['crm']),
    showCRMPermission() {
      return this.crm
    }
  },
  watch: {
    allData: function() {
      this.relatedListData = this.allData
    }
  },
  mounted() {
    // 编辑时勾选
    this.relatedListData = this.allData
  },
  methods: {
    crmrelativeClose() {
      this.showPopover = false
    },
    checkInfos(val) {
      this.showPopover = false
      this.relatedListData = val.data
      for (const key in val.data) {
        const list = val.data[key]
        this.relevanceAll[key + 'Ids'] = list.map(function(
          item,
          index,
          array
        ) {
          return item[key + 'Id']
        })
      }
      this.$emit('checkInfos', this.relevanceAll)
    },
    // 任务页面取消关联
    delRelevance(field, index, item) {
      this.$confirm('确认取消关联?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'is-particulars'
      })
        .then(() => {
          const params = { taskId: this.taskID }
          const tempRelatedListData = objDeepCopy(this.relatedListData)
          tempRelatedListData[field].splice(index, 1)
          for (let index = 0; index < this.showTypes.length; index++) {
            const typeItem = this.showTypes[index]
            const typeArray = tempRelatedListData[typeItem] || []
            params[typeItem + 'Ids'] = typeArray
              .map(aItem => {
                return aItem[typeItem + 'Id']
              })
              .join(',')
          }
          editTaskRelation(params)
            .then(res => {
              this.relatedListData[field].splice(index, 1)
              this.relatedListData = objDeepCopy(this.relatedListData)
              this.$message.success('关联取消成功')
            })
            .catch(() => {})
        })
        .catch(() => {
          this.$message.info('已取消操作')
        })
    },
    checkRelatedDetail(field, val) {
      val.key = val[field + 'Id']
      this.$emit('checkRelatedDetail', field, val)
    }
  }
}
</script>

<style scoped lang="scss">
.related-business {
  color: #777;
  font-size: 12px;
  margin: 10px 0;
  .alterable-p {
    padding-bottom: 10px;
    font-size: 13px;
  }
  .add-file {
    margin: 10px 0;
    font-size: 13px;
    color: #3e84e9;
    cursor: pointer;
    display: inline-block;
    img {
      vertical-align: middle;
      width: 15px;
      margin-right: 5px;
    }
  }
}
</style>
