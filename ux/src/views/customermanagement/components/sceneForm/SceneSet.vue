<template>
  <el-dialog
    :visible.sync="visible"
    :append-to-body="true"
    title="场景管理"
    width="700px"
    @close="handleCancel">
    <div class="scene-name">您可通过拖拽管理标签</div>
    <flexbox class="scene-list">
      <div class="scene-list-box">
        <flexbox class="scene-list-head">
          <el-checkbox
            :indeterminate="isleftIndeterminate"
            v-model="checkleftAll"
            @change="handleleftCheckAllChange"/>
          <div class="scene-list-head-name">显示的标签</div>
          <div class="scene-list-head-detail">{{ leftCheckItems.length + '/' + checkedLeftData.length }}</div>
        </flexbox>
        <div class="scene-list-body">
          <flexbox
            v-for="(item, index) in checkedLeftData"
            v-if="item.isSystem == 1"
            :key="index"
            class="list-item">
            <div
              :class="{'default-mark-active': item.sceneId == defaultId}"
              class="default-mark"/>
            <el-checkbox
              v-model="item.check"
              :disabled="true"
              class="list-item-check"
              @change="leftCheckItemChange"/>
            <div class="list-item-name">{{ item.name }}</div>
            <div class="list-item-handle">
              <el-dropdown
                trigger="click"
                @command="defaultHandle">
                <i
                  class="el-icon-arrow-down"
                  @click="itemHandle('default', item, index)"/>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item>设置为默认标签</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </flexbox>
          <draggable
            v-model="checkedLeftData"
            :move="leftMove"
            :options="{group: 'list',forceFallback:false, fallbackClass:'draggingStyle'}"
            style="min-height: 100px;"
            @end="leftMoveEnd">
            <flexbox
              v-for="(item, index) in checkedLeftData"
              v-if="item.isSystem != 1"
              :key="index"
              class="list-item">
              <div
                :class="{'default-mark-active': item.sceneId == defaultId}"
                class="default-mark"/>
              <el-checkbox
                v-model="item.check"
                class="list-item-check"
                @change="leftCheckItemChange"/>
              <div class="list-item-name">{{ item.name }}</div>
              <div class="list-item-handle">
                <i
                  class="el-icon-edit"
                  @click="itemHandle('edit', item, index)"/>
                <i
                  class="el-icon-delete"
                  @click="itemHandle('delete', item, index)"/>
                <el-dropdown @command="defaultHandle">
                  <i
                    class="el-icon-arrow-down"
                    @click="itemHandle('default', item, index)"/>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item>设置为默认标签</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </div>
            </flexbox>
          </draggable>

        </div>
      </div>
      <div class="scene-middle-list">
        <el-button
          :class="{'scene-middle-button-select':rightCheckItems.length > 0}"
          :disabled="rightCheckItems.length == 0"
          class="scene-middle-left-button"
          @click="changePositon('left')">
          <i class="el-icon-arrow-left scene-middle-icon"/>
        </el-button>
        <el-button
          :class="{'scene-middle-button-select':leftCheckItems.length > 0}"
          :disabled="leftCheckItems.length == 0"
          class="scene-middle-right-button"
          @click="changePositon('right')">
          <i class="el-icon-arrow-right scene-middle-icon"/>
        </el-button>
      </div>
      <div class="scene-list-box">
        <flexbox class="scene-list-head">
          <el-checkbox
            :indeterminate="isrightIndeterminate"
            v-model="checkrightAll"
            @change="handlerightCheckAllChange"/>
          <div class="scene-list-head-name">隐藏的标签</div>
          <div class="scene-list-head-detail">{{ rightCheckItems.length + '/' + checkedRightData.length }}</div>
        </flexbox>
        <div class="scene-list-body">
          <draggable
            v-model="checkedRightData"
            :move="rightMove"
            :options="{group: 'list',forceFallback:false, fallbackClass:'draggingStyle'}"
            style="min-height: 100px;"
            @end="rightMoveEnd">
            <flexbox
              v-for="(item, index) in checkedRightData"
              :key="index"
              class="list-item">
              <el-checkbox
                v-model="item.check"
                style="margin-left:9px;"
                class="list-item-check"
                @change="rightCheckItemChange"/>
              <div class="list-item-name">{{ item.name }}</div>
            </flexbox>
          </draggable>
        </div>
      </div>
    </flexbox>
    <div class="handle-bar">
      <div
        class="handle-bar-add"
        @click="addAndEditScene('add',{})">+ 新建场景</div>
      <div class="handle-bar-save">
        <el-button @click.native="handleCancel">取消</el-button>
        <el-button
          type="primary"
          @click.native="handleConfirm">保存</el-button>
      </div>
    </div>
    <scene-create
      :field-list="fieldList"
      :crm-type="crmType"
      :dialog-visible.sync="showCreateScene"
      :obj="filterObj"
      :name="filterName"
      :edit_id="filterEditId"
      :is-default="filterDefault"
      @saveSuccess="getSceneList"/>
  </el-dialog>
</template>

<script type="text/javascript">
import crmTypeModel from '@/views/customermanagement/model/crmTypeModel'
import {
  crmSceneSetIndex,
  crmSceneSort,
  crmSceneDefaults,
  crmSceneDelete,
  filterIndexfields
} from '@/api/customermanagement/common'
import draggable from 'vuedraggable'
import SceneCreate from './SceneCreate' // 新建编辑场景

export default {
  name: 'SceneSet', // 场景 设置
  components: {
    draggable,
    SceneCreate
  },
  props: {
    dialogVisible: {
      type: Boolean,
      required: true,
      default: false
    },
    /** 没有值就是全部类型 有值就是当个类型 */
    crmType: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      defaultId: '', // 默认场景id
      visible: false, // 控制展示
      isleftIndeterminate: false, // 标注头部是多选框效果
      checkleftAll: false, // 关联全选操作多选框

      checkedLeftData: [], // 数据源
      leftCheckItems: [], // 选择的数据源

      isrightIndeterminate: false,
      checkrightAll: false,

      checkedRightData: [],
      rightCheckItems: [],

      moveItem: {}, // 移动中的item
      handlDefaultItem: {}, // 设置默认的中间item

      /** 添加 编辑 场景 */
      showCreateScene: false, // 展示场景添加
      fieldList: [],
      filterObj: { form: [] }, // 筛选确定数据
      filterName: '',
      filterDefault: false,
      filterEditId: '' // 编辑id
    }
  },
  computed: {},
  watch: {
    dialogVisible: {
      handler(val) {
        this.visible = val
        if (val) {
          this.getSceneList()
        }
      },
      deep: true,
      immediate: true
    }
  },
  mounted() {},
  methods: {
    getSceneList() {
      crmSceneSetIndex({
        type: crmTypeModel[this.crmType]
      })
        .then(res => {
          this.checkedLeftData = res.data.value.map(function(item, index) {
            item.check = false
            return item
          })
          this.checkedRightData = res.data.hide_value.map(function(
            item,
            index
          ) {
            item.check = false
            return item
          })
          var defaultScene = this.checkedLeftData.filter(function(item, index) {
            return item.isDefault == 1
          })
          // 如果有默认场景
          if (defaultScene && defaultScene.length > 0) {
            this.defaultId = defaultScene[0].sceneId
          } else {
            this.defaultId = ''
          }
        })
        .catch(() => {})
    },
    /**
     * 确定选择
     */
    handleConfirm() {
      // 如果选择默认的 不包含在左侧列表 重置为没有默认
      var self = this
      var leftTemps = this.checkedLeftData.filter(function(item, index) {
        return item.sceneId == self.defaultId
      })
      if (leftTemps.length == 0) {
        this.defaultId = ''
      }
      crmSceneSort({
        type: crmTypeModel[this.crmType],
        noHideIds: this.checkedLeftData
          .map(function(item, index, array) {
            return item.sceneId
          })
          .join(','),
        hideIds: this.checkedRightData
          .map(function(item, index, array) {
            return item.sceneId
          })
          .join(',')
      })
        .then(res => {
          this.$message({
            type: 'success',
            message: '操作成功'
          })
          this.handleCancel()
          this.$emit('save-success')
        })
        .catch(() => {})
    },
    /** 事项操作 */
    itemHandle(type, item, index) {
      if (type == 'edit') {
        this.addAndEditScene('edit', item)
      } else if (type == 'delete') {
        this.$confirm('您确定要删除这一条数据吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            crmSceneDelete({
              sceneId: item.sceneId
            })
              .then(res => {
                this.$message({
                  type: 'success',
                  message: '删除成功'
                })
                this.checkedLeftData.splice(index, 1)
                this.leftCheckItemChange()
                this.$emit('save-success')
              })
              .catch(() => {})
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '已取消删除'
            })
          })
      } else if (type == 'default') {
        this.handlDefaultItem = item
      }
    },
    /** 操作默认 */
    defaultHandle() {
      crmSceneDefaults({
        sceneId: this.handlDefaultItem.sceneId
      })
        .then(res => {
          this.$message({
            type: 'success',
            message: '操作成功'
          })
          this.defaultId = this.handlDefaultItem.sceneId
        })
        .catch(() => {})
    },
    /** 添加编辑场景 */
    addAndEditScene(type, data) {
      filterIndexfields({
        label: crmTypeModel[this.crmType]
      })
        .then(res => {
          this.fieldList = res.data
          if (type == 'edit') {
            this.filterObj = { form: [], obj: data.data }
            this.filterName = data.name
            this.filterDefault = data.isDefault == 1
            this.filterEditId = data.sceneId.toString()
          } else {
            this.filterObj = { form: [] }
            this.filterEditId = ''
            this.filterName = ''
            this.filterDefault = false
          }
          this.showCreateScene = true
        })
        .catch(() => {})
    },
    /**
     * 取消选择
     */
    handleCancel() {
      this.visible = false
      this.$emit('update:dialogVisible', false)
    },
    /** 拖拽操作 */
    rightMoveEnd(evt) {
      this.moveItem.check = false
      this.leftCheckItemChange()
      this.rightCheckItemChange()
    },
    rightMove(obj) {
      this.moveItem = obj.draggedContext.element
    },
    leftMoveEnd(evt) {
      this.moveItem.check = false
      this.leftCheckItemChange()
      this.rightCheckItemChange()
    },
    leftMove(obj) {
      this.moveItem = obj.draggedContext.element
    },
    // 选择全部
    handleleftCheckAllChange(value) {
      if (value) {
        this.isleftIndeterminate = false
      }
      this.checkedLeftData = this.checkedLeftData.filter(function(
        item,
        index,
        array
      ) {
        item.check = value
        return item
      })
      this.leftCheckItems = value ? this.checkedLeftData : []
    },
    leftCheckItemChange() {
      this.leftCheckItems = this.checkedLeftData.filter(function(
        item,
        index,
        array
      ) {
        return item.check == true
      })
      if (this.leftCheckItems.length > 0) {
        if (this.leftCheckItems.length == this.checkedLeftData.length) {
          this.checkleftAll = true
          this.isleftIndeterminate = false
        } else {
          this.checkleftAll = false
          this.isleftIndeterminate = true
        }
      } else {
        this.checkleftAll = false
        this.isleftIndeterminate = false
      }
    },
    // 选择全部
    handlerightCheckAllChange(value) {
      if (value) {
        this.isrightIndeterminate = false
      }
      this.checkedRightData = this.checkedRightData.filter(function(
        item,
        index,
        array
      ) {
        item.check = value
        return item
      })
      this.rightCheckItems = value ? this.checkedRightData : []
    },
    rightCheckItemChange() {
      this.rightCheckItems = this.checkedRightData.filter(function(
        item,
        index,
        array
      ) {
        return item.check == true
      })
      if (this.rightCheckItems.length > 0) {
        if (this.rightCheckItems.length == this.checkedRightData.length) {
          this.checkrightAll = true
          this.isrightIndeterminate = false
        } else {
          this.checkrightAll = false
          this.isrightIndeterminate = true
        }
      } else {
        this.checkrightAll = false
        this.isrightIndeterminate = false
      }
    },
    // 按钮操作
    changePositon(type) {
      var self = this
      // 从右往左
      if (type == 'left') {
        this.checkedRightData = this.checkedRightData.filter(function(
          item,
          index,
          array
        ) {
          var remove = false
          self.rightCheckItems.forEach(function(element, index) {
            if (item.sceneId == element.sceneId) {
              remove = true
            }
          })
          return !remove
        })

        this.rightCheckItems.forEach(function(element, index) {
          element.check = false
          self.checkedLeftData.push(element)
        })

        this.rightCheckItems = []
        this.isrightIndeterminate = false
        this.checkrightAll = false

        // 刷新左侧效果
        this.leftCheckItemChange()
        this.rightCheckItemChange()
      } else {
        this.checkedLeftData = this.checkedLeftData.filter(function(
          item,
          index,
          array
        ) {
          var remove = false
          self.leftCheckItems.forEach(function(element, index) {
            if (item.sceneId == element.sceneId) {
              remove = true
            }
          })
          return !remove
        })

        this.leftCheckItems.forEach(function(element, index) {
          element.check = false
          self.checkedRightData.push(element)
        })

        this.leftCheckItems = []
        this.isleftIndeterminate = false
        this.checkleftAll = false

        // 刷新右侧效果
        this.leftCheckItemChange()
        this.rightCheckItemChange()
      }
    }
  }
}
</script>
<style rel="stylesheet/scss" lang="scss" scoped>
.scene-name {
  font-size: 12px;
  padding-bottom: 5px;
  color: #aaa;
}

.default-mark {
  width: 4px;
  height: 4px;
  border-radius: 2px;
  margin-right: 5px;
}
.default-mark-active {
  background-color: $xr-color-primary;
}

.scene-list {
  .scene-list-box {
    flex: 1;
    border: 1px solid $xr-border-line-color;
    border-radius: 2px;
    height: 300px;
    position: relative;
    padding: 15px;
  }
  .scene-middle-list {
    width: 50px;
    text-align: center;
    button {
      border: 1px solid #cccccc;
      width: 34px;
      height: 34px;
      border-radius: 17px;
      background-color: #f5f7fa;
    }
    .scene-middle-icon {
      color: #cccccc;
      font-size: 14px;
    }
    .scene-middle-left-button {
    }
    .scene-middle-right-button {
      margin-top: 15px;
      margin-left: 0;
    }

    .scene-middle-button-select {
      border: 1px solid $xr-color-primary !important;
      background-color: $xr-color-primary !important;
      .scene-middle-icon {
        color: white !important;
      }
    }
  }
  .scene-list-head {
    padding-bottom: 15px;
    padding-left: 9px;
    border-bottom: 1px dashed $xr-border-line-color;
    font-size: 13px;
    .scene-list-head-name {
      color: #333;
      flex: 1;
      margin: 0 8px;
    }

    .scene-list-head-detail {
      color: #aaa;
    }
  }
  .scene-list-body {
    padding: 5px 0;
    height: 240px;
    position: relative;
    overflow-y: auto;
  }

  .list-item {
    padding: 5px 0;
    cursor: pointer;
    .list-item-check {
      margin-right: 8px;
    }
    .list-item-name {
      font-size: 13px;
      color: #333;
      flex: 1;
    }
    .list-item-handle {
      flex-shrink: 0;
      display: none;
      i {
        color: #aaa;
        font-size: 12px;
        margin-right: 5px;
      }
      i:hover {
        color: $xr-color-primary;
      }
    }
  }

  .list-item:hover {
    .list-item-handle {
      display: block;
    }
  }
}

.handle-bar {
  font-size: 12px;
  .handle-bar-add {
    cursor: pointer;
    color: #3e84e9;
    padding: 20px 10px;
  }
  .handle-bar-save {
    position: relative;
    height: 30px;
    button {
      float: right;
      margin-right: 10px;
    }
  }
}
</style>
