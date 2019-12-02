<template>
  <div class="list">
    <div class="list-content">
      <flexbox class="header">
        <div
          v-photo="data.createUser"
          v-lazy:background-image="$options.filters.filterUserLazyImg(data.createUser.img)"
          class="div-photo head-img"/>
        <div class="name-time">
          <span class="name">{{ data.createUser.realname }}</span>
          <span class="time">{{ data.createtime }}</span>
        </div>
        <div class="rt-setting">
          <span
            :style="{ 'background-color': getStatusColor(data.examineStatus) }"
            class="bg-color"/>
          <span class="dep">
            <span>{{ data.categoryTitle }} - </span>
            <span>{{ getStatusName(data.examineStatus) }}</span>
          </span>
          <!-- 编辑 -->
          <el-dropdown
            v-if="data.permission && (data.permission.isChecked || data.permission.isUpdate || data.permission.isDelete)"
            trigger="click"
            @command="handleCommand">
            <i
              style="color:#CDCDCD; cursor: pointer;"
              class="el-icon-arrow-down el-icon-more"/>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item
                v-if="data.permission && data.permission.isChecked"
                command="withdraw">撤回</el-dropdown-item>
              <el-dropdown-item
                v-if="data.permission && data.permission.isUpdate"
                command="edit">编辑</el-dropdown-item>
              <el-dropdown-item
                v-if="data.permission && data.permission.isDelete"
                command="delete">删除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </flexbox>
      <div
        class="row"
        @click="checkDetail(data)">
        <p
          v-if="data.content"
          class="text">{{ data.content }}</p>
        <p
          v-if="data.causeTitle"
          class="title">{{ data.causeTitle }}</p>
      </div>
      <div
        v-if="data.file.length > 0 || data.img.length > 0"
        class="accessory">
        <!-- 图片 -->
        <div class="upload-img-box">
          <div
            v-for="(imgItem, k) in data.img"
            :key="k"
            class="img-list"
            @click="imgZoom(data.img, k)">
            <img v-lazy="imgItem.filePath">
          </div>
        </div>
        <!-- 附件 -->
        <div class="accessory-box">
          <file-cell
            v-for="(file, fileIndex) in data.file"
            :key="fileIndex"
            :data="file"
            :cell-index="fileIndex"/>
        </div>
      </div>
      <!-- 关联业务 -->
      <div
        v-if="relatedListData.contacts.length > 0 || relatedListData.customer.length > 0 || relatedListData.business.length > 0 || relatedListData.contract.length > 0"
        class="related-business">
        <div class="label">关联业务</div>
        <div
          v-for="(items, key) in relatedListData"
          :key="key">
          <related-business-cell
            v-for="(item, itemIndex) in items"
            :data="item"
            :cell-index="itemIndex"
            :type="key"
            :key="itemIndex"
            :show-foot="false"
            @click.native="checkRelatedDetail(key, item)"/>
        </div>
      </div>
    </div>
  </div>
</template>
<script type="text/javascript">
import RelatedBusinessCell from '@/views/OAManagement/components/relatedBusinessCell'
import FileCell from '@/views/OAManagement/components/fileCell'

export default {
  name: 'ExamineCell', // 审批cell
  components: {
    RelatedBusinessCell,
    FileCell
  },
  mixins: [],
  props: {
    data: Object
  },
  data() {
    return {}
  },
  computed: {
    relatedListData() {
      return {
        contacts: this.data.contactsList || [],
        customer: this.data.customerList || [],
        business: this.data.businessList || [],
        contract: this.data.contractList || []
      }
    }
  },
  watch: {},
  mounted() {},
  methods: {
    // 获取状态名称  0 未审核 1 审核通过 2 审核拒绝 3 审核中 4 已撤回
    getStatusName(status) {
      if (status == 0) {
        return '待审'
      } else if (status == 1) {
        return '审核通过'
      } else if (status == 2) {
        return '审核拒绝'
      } else if (status == 3) {
        return '审核中'
      } else if (status == 4) {
        return '撤回'
      }
      return ''
    },
    getStatusColor(status) {
      if (status == 0) {
        return '#F3A633'
      } else if (status == 1) {
        return '#93E06D'
      } else if (status == 2) {
        return '#FF0000'
      } else if (status == 3) {
        return '#F3A633'
      } else if (status == 4) {
        return '#FF0000'
      }
      return ''
    },
    // 放大图片
    imgZoom(images, k) {
      this.$bus.emit('preview-image-bus', {
        index: k,
        data: images.map(function(item, index, array) {
          return {
            url: item.filePath,
            name: item.name
          }
        })
      })
    },
    // 编辑 删除 撤回
    handleCommand(command) {
      this.$emit('on-handle', { type: command, data: { item: this.data }})
    },
    // 查看详情
    checkDetail(data) {
      this.$emit('on-handle', { type: 'view', data: { item: this.data }})
    },
    // 关联详情
    checkRelatedDetail(type, data) {
      this.$emit('on-handle', {
        type: 'related-detail',
        data: { type: type, item: data }
      })
    }
  }
}
</script>
<style lang="scss" scoped>
@import '../../styles/content.scss';

.list {
  margin-bottom: 20px;
  vertical-align: middle;
  .list-content {
    padding: 0 10px 10px 0;
    .header {
      margin-bottom: 15px;
      @include color9;
      font-size: 12px;
      .head-img {
        width: 35px;
        height: 35px;
        border-radius: 17.5px;
      }
      .name-time {
        display: inline-block;
        margin-left: 13px;
        flex: 1;
        .name {
          font-size: 15px;
          color: #333333;
          display: block;
          margin-bottom: 5px;
        }
      }
      .rt-setting {
        float: right;
        line-height: 30px;
        .dep {
          color: #333333;
          margin-right: 20px;
        }
        img {
          width: 16px;
          @include cursor;
          @include v-align;
        }
        .bg-color {
          display: inline-block;
          width: 10px;
          height: 10px;
          border-radius: 50%;
          margin-right: 5px;
        }
      }
    }
    .row {
      white-space: pre-wrap;
      word-wrap: break-word;
      letter-spacing: 0.5px;
      line-height: 18px;
      cursor: pointer;
      color: #3e84e9;
      .text {
        padding-bottom: 10px;
      }
      .title {
        @include color9;
        font-size: 13px;
        padding-bottom: 10px;
      }
    }
    .accessory {
      .upload-img-box {
        margin: 10px 0;
        .img-list {
          display: inline-block;
          position: relative;
          margin-right: 10px;
          width: 80px;
          height: 60px;
          line-height: 60px;
          cursor: pointer;
          img {
            width: 80px;
            height: 60px;
          }
          .img-hover {
            position: absolute;
            top: 0;
            right: 0;
            left: 0;
            bottom: 0;
            background-color: rgba(0, 0, 0, 0.5);
            text-align: center;
            font-size: 12px;
            color: #fff;
            display: none;
            span {
              @include cursor;
              display: inline-block;
            }
          }
        }
        .img-list:hover {
          .img-hover {
            display: inline-block;
          }
        }
      }
    }
    .related-business {
      margin: 15px 0;
      .label {
        font-size: 13px;
        margin-bottom: 7px;
        color: #666;
      }
    }
  }
}
.list:not(:last-child) {
  .list-content {
    border-bottom: 1px solid #e6e6e6;
  }
}
</style>
