<template>
  <div class="details">
    <el-dialog
      :title="listData.title"
      :visible.sync="dialogVisible"
      :modal="false"
      :before-close="handleClose"
      width="400px"
      top="30vh">
      <div class="content">
        <div
          v-for="(item, index) in list"
          :key="index"
          class="list-data">
          <label>{{ item.label }}：</label>
          <span v-if="item.type=='time' && listData.time">{{ listData[item.value] }}</span>
          <span v-else-if="item.type=='createTime'">{{ listData[item.value] }}</span>
          <span
            v-else-if="item.type=='ownerList'"
            class="owner-list">
            <span
              v-for="(k, j) in listData[item.value]"
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
                  class="div-photo header-circle"/>
              </el-tooltip>
            </span>
          </span>
          <span v-else-if="item.type=='remindtype'">{{ listData.remindtypeText }}</span>
          <span v-else-if="item.type=='createUser'">{{ listData.createUser.realname }}</span>
          <span v-else-if="item.type=='bz'">{{ listData[item.value] }}</span>
        </div>
        <!-- 关联业务 -->
        <related-business
          :margin-left="'0'"
          :alterable="false"
          :all-data="allData"
          :alterable-color="'#666'"
          @checkRelatedDetail="checkRelatedDetail"/>
      </div>
      <span
        v-if="btnShow"
        slot="footer"
        class="dialog-footer">
        <el-button
          v-if="listData.permission && listData.permission.isUpdate == 1"
          type="primary"
          @click="editBtn">编辑</el-button>
        <el-button
          v-if="listData.permission && listData.permission.isDelete == 1"
          type="danger"
          @click="deleteClose">删除</el-button>
      </span>
    </el-dialog>
    <c-r-m-full-screen-detail
      :visible.sync="showRelatedDetail"
      :crm-type="relatedCRMType"
      :id="relatedID"/>
  </div>
</template>

<script>
import { scheduleDelete } from '@/api/oamanagement/schedule'
// 关联业务 - 选中列表
import relatedBusiness from '@/components/relatedBusiness'
import CRMFullScreenDetail from '@/views/customermanagement/components/CRMFullScreenDetail'
export default {
  components: {
    relatedBusiness,
    CRMFullScreenDetail
  },
  props: {
    dialogVisible: Boolean,
    listData: Object,
    // 是否显示编辑删除按钮
    btnShow: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      list: [
        { label: '开始时间', value: 'startTime', type: 'time' },
        { label: '结束时间', value: 'endTime', type: 'time' },
        { label: '参与人', value: 'ownerList', type: 'ownerList' },
        { label: '创建人', value: 'realname', type: 'createUser' },
        { label: '创建时间', value: 'createTime', type: 'createTime' },
        { label: '备注', value: 'remark', type: 'bz' }
      ],
      allData: {},
      // 相关详情的查看
      relatedID: '',
      relatedCRMType: '',
      showRelatedDetail: false
    }
  },
  created() {
    const newVal = this.listData
    this.allData.business = newVal.businessList ? newVal.businessList : ''
    this.allData.contacts = newVal.contactsList ? newVal.contactsList : ''
    this.allData.contract = newVal.contractList ? newVal.contractList : ''
    this.allData.customer = newVal.customerList ? newVal.customerList : ''
    switch (newVal.remindtype) {
      case 0:
        newVal.remindtypeText = '无'
        break
      case 1:
        newVal.remindtypeText = '准时提醒'
        break
      case 2:
        newVal.remindtypeText = '5分钟前'
        break
      case 3:
        newVal.remindtypeText = '15分钟前'
        break
      case 4:
        newVal.remindtypeText = '30分钟前'
        break
      case 5:
        newVal.remindtypeText = '一个小时前'
        break
      case 6:
        newVal.remindtypeText = '二个小时前'
        break
      case 7:
        newVal.remindtypeText = '一天前'
        break
      case 8:
        newVal.remindtypeText = '二天前'
        break
      case 9:
        newVal.remindtypeText = '一周前'
        break
    }
  },
  methods: {
    handleClose() {
      this.$emit('handleClose')
    },
    deleteClose() {
      this.$confirm('确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          scheduleDelete({ eventId: this.listData.eventId }).then(res => {
            this.$emit('deleteClose', this.listData)
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    },
    editBtn() {
      this.$emit('editBtn', this.listData)
    },
    checkRelatedDetail(crmType, item) {
      this.relatedID = item.key
      this.relatedCRMType = crmType
      this.showRelatedDetail = true
    }
  }
}
</script>

<style scoped lang="scss">
.details /deep/ .el-dialog {
  box-shadow: 0px 1px 8px 0px rgba(0, 0, 0, 0.3);
  .el-dialog__header {
    padding-left: 25px;
    padding-right: 40px;
  }
  .el-dialog__body {
    padding: 0 30px 30px;
    .content {
      padding-left: 5px;
      padding-top: 5px;
      .text {
        font-size: 16px;
        margin-bottom: 15px;
      }
      .list-data {
        margin-bottom: 15px;
        font-size: 13px;
        color: #333;
        label {
          width: 70px;
          display: inline-block;
          text-align: left;
          color: #666;
        }
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
      }
      .list-data:nth-child(3) {
        label {
          vertical-align: middle;
        }
      }
      .related-business > span {
        display: none;
      }
    }
  }
}
</style>
