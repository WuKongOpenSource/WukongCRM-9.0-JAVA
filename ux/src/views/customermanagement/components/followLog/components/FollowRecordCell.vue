<template>
  <div class="fl-c">
    <flexbox class="fl-h">
      <div v-photo="{img: item.userImg, realname: item.realname}"
           v-lazy:background-image="$options.filters.filterUserLazyImg(item.userImg)"
           class="div-photo fl-h-img"></div>
      <div class="fl-h-b">
        <div class="fl-h-name">{{item.realname}}</div>
        <div class="fl-h-time">{{item.createTime}}</div>
      </div>
      <flexbox class="fl-h-mark">
        <img class="fl-h-mark-img"
             src="@/assets/img/follow_record.png" />
        <div class="fl-h-mark-name">跟进记录</div>
      </flexbox>
      <el-dropdown @command="handleCommand"
                   trigger="click">
        <i style="color:#CDCDCD;margin-left: 8px;"
           class="el-icon-arrow-down el-icon-more"></i>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="delete">删除</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </flexbox>
    <div class="fl-b">
      <div class="fl-b-content">{{item.content}}</div>
      <flexbox class="fl-b-images"
               v-if="item.img && item.img.length > 0"
               wrap="wrap">
        <div class="fl-b-img-item"
             v-for="(file, index) in item.img"
             :key="file.filePath"
             @click="previewImg(item.img, index)"
             v-lazy:background-image="file.filePath"></div>
      </flexbox>
      <div v-if="item.file && item.file.length > 0"
           class="fl-b-files">
        <flexbox class="cell"
                 v-for="(file, index) in item.file"
                 :key="index">
          <img class="cell-head"
               src="@/assets/img/relevance_file.png" />
          <div class="cell-body">{{file.name}}<span style="color: #ccc;">（{{file.size}}）</span></div>
          <el-button type="primary"
                     icon="el-icon-download"
                     @click="downloadFile(file)">下载</el-button>
        </flexbox>
      </div>
      <div class="follow"
           v-if="item.category || item.nextTime">
        <span v-if="item.category"
              class="follow-info">{{item.category}}</span>
        <span v-if="item.nextTime"
              class="follow-info">{{item.nextTime}}</span>
      </div>
      <div class="fl-b-other"
           v-if="item.contactsList && item.contactsList.length > 0">
        <div class="fl-b-other-name">关联联系人</div>
        <div>
          <flexbox class="cell"
                   v-for="(item, index) in item.contactsList"
                   @click.native="checkRelationDetail('contacts', item.contactsId)"
                   :key="index">
            <i class="wukong wukong-contacts cell-head crm-type"
               :style="{'opacity': index == 0 ? 1 : 0}"></i>
            <div class="cell-body"
                 style="color: #6394E5;cursor: pointer;">{{item.name}}</div>
          </flexbox>
        </div>
      </div>
      <div class="fl-b-other"
           v-if="item.businessList && item.businessList.length > 0">
        <div class="fl-b-other-name">关联商机</div>
        <div>
          <flexbox class="cell"
                   v-for="(item, index) in item.businessList"
                   @click.native="checkRelationDetail('business', item.businessId)"
                   :key="index">
            <i class="wukong wukong-business cell-head crm-type"
               :style="{'opacity': index == 0 ? 1 : 0}"></i>
            <div class="cell-body"
                 style="color: #6394E5;cursor: pointer;">{{item.businessName}}</div>
          </flexbox>
        </div>
      </div>
    </div>
    <c-r-m-full-screen-detail :visible.sync="showFullDetail"
                              :crmType="relationCrmType"
                              :id="relationID"></c-r-m-full-screen-detail>
  </div>
</template>

<script>
import { downloadFile } from '@/utils'
import { crmRecordDelete } from '@/api/customermanagement/common'

export default {
  /** 客户管理 的 客户详情 的 跟进记录cell*/
  name: 'follow-record-cell',
  components: {
    CRMFullScreenDetail: () =>
      import('@/views/customermanagement/components/CRMFullScreenDetail.vue')
  },
  props: {
    item: {
      type: Object,
      default: () => {
        return {}
      }
    },
    /** 没有值就是全部类型 有值就是当个类型 */
    crmType: {
      type: String,
      default: ''
    },
    index: [String, Number]
  },
  data() {
    return {
      showFullDetail: false, // 查看相关客户管理详情
      relationID: '', // 相关ID参数
      relationCrmType: '' // 相关类型
    }
  },
  computed: {},
  mounted() {},
  methods: {
    previewImg(list, index) {
      this.$bus.emit('preview-image-bus', {
        index: index,
        data: list.map(function(item, index, array) {
          item.url = item.filePath
          return item
        })
      })
    },
    downloadFile(file) {
      downloadFile({ path: file.filePath, name: file.name })
    },
    /**
     * 删除跟进记录
     */
    handleCommand(command) {
      this.$confirm('确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          crmRecordDelete({
            recordId: this.item.recordId
          })
            .then(res => {
              this.$emit('on-handle', {
                type: command,
                data: { item: this.item, index: this.index }
              })
              this.$message.success('操作成功')
            })
            .catch(() => {})
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消操作'
          })
        })
    },
    /**
     * 查看相关客户管理详情
     */
    checkRelationDetail(type, id) {
      this.relationID = id
      this.relationCrmType = type
      this.showFullDetail = true
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../styles/followcell.scss';
.follow {
  .follow-info {
    padding: 5px 10px;
    background-color: #f5f7fa;
    color: #999;
    height: 40px;
    line-height: 40px;
    border-radius: 28px;
    font-size: 12px;
    margin-right: 10px;
  }
}

.crm-type {
  color: rgb(99, 148, 229);
  font-size: 14px;
}
</style>
