<template>
  <div>
    <el-popover
      v-model="showPopover"
      placement="bottom"
      width="800"
      popper-class="no-padding-popover"
      trigger="click">
      <crm-relative
        v-if="showRelative"
        ref="crmrelative"
        :radio="false"
        :selected-data="selectedData"
        :show-types="showTypes"
        @close="crmrelativeClose"
        @changeCheckout="checkInfos"/>
      <p
        slot="reference"
        class="add-file"
        @click="showRelative = true">
        <img
          src="@/assets/img/relevance_business.png"
          alt="">
        关联业务
      </p>
    </el-popover>
    <div class="related-business">
      <div
        v-for="(items, key) in selectedData"
        :key="key">
        <related-business-cell
          v-for="(item, itemIndex) in items"
          :data="item"
          :cell-index="itemIndex"
          :type="key"
          :key="itemIndex"
          :cursor-pointer="false"
          @unbind="delRelevance"/>
      </div>
    </div>
  </div>
</template>
<script type="text/javascript">
import CrmRelative from '@/components/CreateCom/CrmRelative'
import RelatedBusinessCell from '@/views/OAManagement/components/relatedBusinessCell'
import { objDeepCopy } from '@/utils'

export default {
  name: 'RelatedBusiness', // 关联业务
  components: {
    CrmRelative,
    RelatedBusinessCell
  },
  props: {
    /** 已选信息 */
    selectedInfos: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    return {
      showTypes: ['customer', 'contacts', 'business', 'contract'],
      // 业务弹窗
      showRelative: false,
      showPopover: false,
      // 编辑时勾选
      selectedData: {}
    }
  },
  computed: {},
  watch: {
    selectedInfos: function(data) {
      this.selectedData = data
    }
  },
  mounted() {},
  methods: {
    delRelevance(field, index) {
      this.$confirm('确认取消关联?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'is-particulars'
      })
        .then(() => {
          this.selectedData[field].splice(index, 1)
          this.selectedData = objDeepCopy(this.selectedData)
          this.submitValueChange()
        })
        .catch(() => {
          this.$message.info('已取消操作')
        })
    },
    getTypeName(type) {
      if (type == 'customer') {
        return '客户'
      } else if (type == 'contacts') {
        return '联系人'
      } else if (type == 'business') {
        return '商机'
      } else if (type == 'contract') {
        return '合同'
      }
    },
    crmrelativeClose() {
      this.showPopover = false
    },
    checkInfos(val) {
      this.showPopover = false
      this.selectedData = val.data
      this.submitValueChange()
    },
    submitValueChange() {
      this.$emit('value-change', {
        index: this.index,
        value: this.selectedData
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.add-file {
  margin: 5px 0;
  font-size: 13px;
  color: #3e84e9;
  cursor: pointer;
  display: inline-block;
  img {
    vertical-align: middle;
    width: 15px;
  }
}

.related-business {
  margin-top: 10px;
}
</style>
