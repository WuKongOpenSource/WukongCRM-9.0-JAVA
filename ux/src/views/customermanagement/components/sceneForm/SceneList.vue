<template>
  <div class="scene-container">
    <div class="scene-list">
      <div v-for="(item, index) in sceneList"
           :key="index"
           @click="selectScene(item, index)"
           :class="{'scene-list-item-select':item.sceneId == sceneSelectId}"
           class="scene-list-item">
        {{item.name}}
      </div>
    </div>
    <div>
      <flexbox class="handle-button"
               @click.native="addScene">
        <img class="handle-button-icon"
             src="@/assets/img/scene_add.png" />
        <div class="handle-button-name">新建场景</div>
      </flexbox>
      <flexbox class="handle-button"
               @click.native="setScene">
        <img class="handle-button-icon"
             src="@/assets/img/scene_set.png" />
        <div class="handle-button-name">管理</div>
      </flexbox>
    </div>

  </div>
</template>

<script type="text/javascript">
import crmTypeModel from '@/views/customermanagement/model/crmTypeModel'
import { mapGetters } from 'vuex'
import { crmSceneIndex } from '@/api/customermanagement/common'

export default {
  name: 'scene-list', //客户管理下 重要提醒 回款计划提醒
  components: {},
  computed: {
    ...mapGetters(['crm'])
  },
  data() {
    return {
      // 场景列表
      sceneSelectId: -1,
      sceneList: []
    }
  },
  watch: {},
  props: {
    /** 没有值就是全部类型 有值就是当个类型 */
    crmType: {
      type: String,
      default: ''
    }
  },
  mounted() {
    if (this.crm[this.crmType].index) {
      this.getSceneList()
    }
  },
  methods: {
    getSceneList() {
      crmSceneIndex({
        type: crmTypeModel[this.crmType]
      })
        .then(res => {
          let defaultScenes = res.data.filter(function(item, index) {
            return item.isDefault === 1
          })

          if (defaultScenes && defaultScenes.length > 0) {
            let defaultScene = defaultScenes[0]
            this.sceneSelectId = defaultScene.sceneId
            this.$emit('scene', {
              id: defaultScene.sceneId,
              name: defaultScene.name,
              bydata: defaultScene.bydata || ''
            })
          } else {
            this.sceneSelectId = ''
            this.$emit('scene', { id: '', name: '', bydata: '' })
          }

          this.sceneList = res.data
        })
        .catch(() => {
          this.$emit('scene', { id: '', name: '', bydata: '' })
        })
    },

    // 选择场景、
    selectScene(item, index) {
      this.sceneSelectId = item.sceneId
      this.$emit('scene', {
        id: item.sceneId,
        name: item.name,
        bydata: item.bydata
      })
      this.$emit('hidden-scene')
    },
    // 添加场景
    addScene() {
      this.$emit('scene-handle', { type: 'add' })
    },
    // 设置场景
    setScene() {
      this.$emit('scene-handle', { type: 'set' })
    }
  }
}
</script>
<style rel="stylesheet/scss" lang="scss" scoped>
.scene-container {
  position: relative;
  width: 150px;
}

.scene-list {
  max-height: 240px;
  overflow-y: auto;
  font-size: 12px;
  .scene-list-item {
    color: #333;
    padding: 10px 15px;
    cursor: pointer;
    background-color: white;
  }
  .scene-list-item:hover {
    background-color: #f7f8fa;
    color: $xr-color-primary;
  }

  .scene-list-item-select {
    color: $xr-color-primary;
    background-color: #f7f8fa;
  }
}

.handle-button {
  padding: 10px 20px;
  cursor: pointer;
  .handle-button-icon {
    display: block;
    width: 15px;
    height: 15px;
    margin-right: 8px;
  }
  .handle-button-name {
    font-size: 12px;
  }
}
.handle-button:hover {
  color: $xr-color-primary;
}
</style>
