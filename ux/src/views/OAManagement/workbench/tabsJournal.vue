<template>
  <div class="content">
    <div id="journal-list-box" class="list-box">
      <journal-cell
        v-for="(item, index) in journalData"
        :key="index"
        :log-index="index"
        :data="item"
        :show-workbench="true"
        :style="{margin: marginDefaults ? '0' : '0 20px 20px'}"
        class="list-cell"
        @on-handle="jourecallCellHandle"/>
      <slot name="load"/>
    </div>
    <!-- 相关业务页面 -->
    <c-r-m-all-detail
      :visible.sync="showRelatedDetail"
      :crm-type="relatedCRMType"
      :listener-ids="['workbench-main-container']"
      :no-listener-ids="['journal-list-box']"
      :id="relatedID"
      class="d-view"/>
  </div>
</template>

<script>
import JournalCell from '@/views/OAManagement/journal/journalCell'
import CRMAllDetail from '@/views/customermanagement/components/CRMAllDetail'

export default {
  components: {
    CRMAllDetail,
    JournalCell
  },
  props: {
    // 数据
    journalData: Array,
    marginDefaults: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      // 相关详情的查看
      relatedID: '',
      relatedCRMType: '',
      showRelatedDetail: false
    }
  },
  computed: {},
  methods: {
    jourecallCellHandle(data) {
      this.relatedID = data.data.item.key
      this.relatedCRMType = data.data.type
      this.showRelatedDetail = true
    }
  }
}
</script>

<style scoped lang="scss">
.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  .list-box {
    flex: 1;
    overflow: auto;
  }
}

.list-cell {
  border: 1px solid #e6e6e6;
  margin-bottom: 20px;
  border-radius: 4px;
}
</style>
