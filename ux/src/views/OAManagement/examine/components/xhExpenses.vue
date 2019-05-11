<template>
  <div>
    <div class="expense-item"
         v-for="(item, index) in mainList"
         :key="index">
      <flexbox class="expense-item-head">
        <div class="expense-item-head-title">报销费用明细（{{index+1}}）</div>
        <i @click="deleteItems(index)"
           v-if="index != 0"
           class="el-icon-delete expense-item-head-delete"></i>
      </flexbox>
      <flexbox wrap="wrap"
               align="stretch"
               class="clauses">
        <flexbox-item :span="1/2"
                      v-for="(subItem, subIndex) in showItems"
                      :key="subIndex"
                      class="clauses-item">
          <div class="clauses-item-title">
            {{subItem.name}}
          </div>
          <el-date-picker v-if="subItem.formType == 'date'"
                          v-model="item[subItem.field]"
                          type="date"
                          value-format="yyyy-MM-dd"
                          placeholder="选择日期"
                          @change="valueChange">
          </el-date-picker>
          <el-input v-else
                    @input="calculateValueChange(index, subIndex)"
                    v-model="item[subItem.field]"></el-input>
        </flexbox-item>
        <flexbox-item :span="1/2"
                      class="clauses-item">
          <div class="sub-total">
            合计（元）：<span>{{item['money']}}</span>
          </div>
        </flexbox-item>
      </flexbox>
      <div class="description">
        <div class="description-title">费用明细描述</div>
        <el-input v-model="item['description']"
                  type="textarea"
                  resize="none"
                  :rows="3"
                  @input="valueChange"></el-input>
      </div>
      <div class="files">
        <el-button type="text"
                   class="add-files"
                   @click="addFiles(index)">上传发票图片</el-button>
        <flexbox wrap="wrap">
          <div class="img-item"
               v-for="(imgItem, imgIndex) in item['imgList']"
               :key="imgIndex"
               :style="{ 'background-image': 'url('+imgItem.url+')' }"
               @mouseover="mouseImgOver(imgItem, imgIndex, item['imgList'])"
               @mouseleave="mouseImgLeave(imgItem, imgIndex, item['imgList'])">
            <div v-if="imgItem.showDelete"
                 class="img-delete"
                 @click="deleteFile(imgItem, imgIndex, item['imgList'])">×</div>
          </div>
        </flexbox>
      </div>
    </div>
    <div class="handle-bar">
      <el-button class="handle-bar-button"
                 type="text"
                 @click="addItems(index)"
                 icon="el-icon-plus">添加事项</el-button>
      <!-- <div class="handle-bar-total">
        合计（元）：<span>{{totalMoney}}</span>
      </div> -->
    </div>
    <input id="imageFileInput"
           type="file"
           accept="image/*"
           multiple
           @change="uploadImageFile" />
  </div>
</template>
<script type="text/javascript">
import { crmFileSave, crmFileDelete } from '@/api/common'
import objMixin from '@/components/CreateCom/objMixin'
import { guid } from '@/utils'

export default {
  name: 'xh-expenses', // 差旅报销事项
  components: {},
  mixins: [objMixin],
  computed: {},
  watch: {
    value: function(val) {
      this.dataValue = val
      if (val.list && val.list.length > 0) {
        this.mainList = val.list
      } else {
        this.mainList.push(this.getValueItem())
      }
    }
  },
  data() {
    return {
      mainList: [],
      imageIndex: -1,
      totalMoney: '0', //合计
      showItems: [
        {
          field: 'startAddress',
          name: '出发城市',
          formType: 'text'
        },
        {
          field: 'endAddress',
          name: '目的城市',
          formType: 'text'
        },
        {
          field: 'startTime',
          name: '开始时间',
          formType: 'date'
        },
        {
          field: 'endTime',
          name: '结束时间',
          formType: 'date'
        },
        {
          field: 'traffic',
          name: '交通费（元）',
          formType: 'text'
        },
        {
          field: 'stay',
          name: '住宿费（元）',
          formType: 'text'
        },
        {
          field: 'diet',
          name: '餐饮费（元）',
          formType: 'text'
        },
        {
          field: 'other',
          name: '其他费用（元）',
          formType: 'text'
        }
      ]
    }
  },
  props: {},
  mounted() {
    if (this.dataValue.list && this.dataValue.list.length > 0) {
      this.mainList = this.dataValue.list
    } else {
      this.mainList.push(this.getValueItem())
    }
  },
  methods: {
    addFiles(index) {
      this.imageIndex = index
      document.getElementById('imageFileInput').click()
    },
    /** 图片选择出发 */
    uploadImageFile(event) {
      var files = event.target.files
      var self = this

      for (let index = 0; index < files.length; index++) {
        const file = files[index]
        crmFileSave({
          type: 'img',
          file: file,
          batchId: this.mainList[this.imageIndex].batchId
        })
          .then(res => {
            if (res) {
              this.mainList[this.imageIndex].imgList.push(res)
              this.submitValueChange()
            }
          })
          .catch(() => {})
      }
      event.target.value = ''
    },
    /** 鼠标移入和移除 图片区域 */
    mouseImgOver(item, index, items) {
      item.showDelete = true
      this.$set(items, index, item)
    },
    mouseImgLeave(item, index, items) {
      item.showDelete = false
      this.$set(items, index, item)
    },
    deleteFile(item, index, items) {
      this.$confirm('您确定要删除该文件吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          crmFileDelete({
            id: item.fileId
          })
            .then(res => {
              items.splice(index, 1)
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
    deleteItems(index) {
      this.mainList.splice(index, 1)
    },
    addItems() {
      this.mainList.push(this.getValueItem())
    },
    valueChange() {
      this.submitValueChange(false)
    },
    // 值更新的回调
    calculateValueChange(mainIndex, subIndex) {
      if (subIndex < 2) {
        this.submitValueChange(false)
        return
      }
      var subTotal = 0
      const calculateFields = ['traffic', 'stay', 'diet', 'other']
      const mainItem = this.mainList[mainIndex]
      calculateFields.forEach(field => {
        subTotal = subTotal + parseFloat(mainItem[field] ? mainItem[field] : 0)
      })
      mainItem.money = subTotal

      var total = 0
      for (let index = 0; index < this.mainList.length; index++) {
        const element = this.mainList[index]
        total = total + parseFloat(element.money ? element.money : 0)
      }
      this.totalMoney = total

      this.submitValueChange(true)
    },
    submitValueChange(update) {
      this.$emit('value-change', {
        index: this.index,
        value: {
          list: this.mainList,
          update: update, //是否更新总数
          money: this.totalMoney
        }
      })
    },
    getValueItem() {
      return {
        startAddress: '',
        endAddress: '',
        startTime: '',
        endTime: '',
        traffic: '',
        stay: '',
        diet: '',
        other: '',
        money: '0',
        description: '',
        imgList: [],
        batchId: guid()
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.expense-item {
  border: 1px solid #e6e6e6;
  border-radius: 2px;
  padding-bottom: 15px;
  margin-bottom: 10px;
  &-head {
    padding: 10px 20px;
    background-color: #f5f5f5;
    &-title {
      height: auto;
      font-size: 12px;
      color: #333;
      flex: 1;
      line-height: normal;
    }

    &-delete {
      padding: 0 10px;
      color: #3e84e9;
      font-size: 14px;
    }
  }
}

.clauses {
  padding: 10px 10px;
  &-item {
    margin-left: 0 !important;
    display: flex;
    padding: 0 10px;
    &-title {
      width: 90px;
      flex-shrink: 0;
      font-size: 12px;
      color: #333;
    }
  }
}

.sub-total {
  font-size: 12px;
  color: #333;
}

.description {
  padding: 0 20px;
  &-title {
    font-size: 12px;
    color: #333;
  }
}

.el-input /deep/ .el-input__inner {
  border-color: #ddd !important;
}

.el-select /deep/ .el-input__inner {
  border-color: #ddd !important;
}

.el-textarea /deep/ .el-textarea__inner {
  border-color: #ddd !important;
}

.handle-bar {
  padding: 0 20px;
  height: 29px;
  &-button {
    float: right;
    border: none;
    color: #3e84e9;
  }
  &-total {
    margin-top: 10px;
    text-align: left;
    font-size: 13px;
    color: #333;
    span {
      color: #666;
    }
  }
}

.files {
  padding: 0 20px;
}

.img-item {
  width: 98px;
  height: 98px;
  border: 1px solid #ccc;
  display: inline-block;
  margin: 0 4px 4px 0;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  position: relative;
  .img-delete {
    position: absolute;
    cursor: pointer;
    top: 0;
    right: 0;
    width: 20px;
    height: 20px;
    line-height: 20px;
    text-align: center;
    font-size: 17px;
    background-color: #666;
    color: white;
  }
}

#imageFileInput {
  display: none;
}
</style>
