<template>
  <flexbox
    style="height:100%;"
    direction="column"
    align="stretch">
    <div class="header">
      <div>应用管理</div>
    </div>
    <div
      v-loading="loading"
      class="body">
      <div
        v-for="(bigItem, bigIndex) in allList"
        :key="bigIndex"
        class="section">
        <flexbox class="section-header">
          {{ bigItem.name }}
        </flexbox>
        <flexbox
          wrap="wrap"
          class="section-body">
          <flexbox
            v-for="(item, index) in bigItem.sublist"
            :key="index"
            class="section-item">
            <img
              :src="getModuleIcon(item.status, item.module)"
              class="item-icon" >
            <span class="item-name">{{ item.name }}</span>
            <el-dropdown
              v-if="item.type == 1 && configSetAuth"
              class="more-menu"
              @command="handleMoreCommand($event, item)">
              <i class="el-icon-more"/>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :command="item.status ? 'disable' : 'enable'">{{ item.status ? '停用' : '启用' }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <span
              v-else-if="item.type == 2"
              class="more-mark">即将发布</span>
          </flexbox>
        </flexbox>
      </div>
    </div>

    <call-detail :visible.sync="showCallDetail"/>
  </flexbox>
</template>

<script>
import {
  adminConfigsetIndex,
  adminConfigsetUpdate
} from '@/api/systemManagement/applicationManagement'
import CallDetail from './components/callDetail'
import { mapGetters } from 'vuex'

export default {
  /** 系统管理 的 应用管理 */
  name: 'SystemModule',
  components: {
    CallDetail
  },
  mixins: [],
  data() {
    return {
      loading: false,
      allList: [
        {
          name: '已启用应用',
          type: 1,
          status: 1,
          sublist: []
        },
        {
          name: '已停用应用',
          type: 1,
          status: 0,
          sublist: []
        },
        {
          name: '敬请期待',
          type: 2,
          sublist: []
        }
      ],
      // 展示详情
      showCallDetail: false
    }
  },
  computed: {
    ...mapGetters(['manage']),
    configSetAuth() {
      return this.manage && this.manage.configSet && this.manage.configSet.update
    }
  },
  mounted() {
    this.getDetail()
  },
  methods: {
    /**
     * 详情
     */
    getDetail() {
      this.loading = true
      adminConfigsetIndex()
        .then(res => {
          this.loading = false
          for (let index = 0; index < this.allList.length; index++) {
            const element = this.allList[index]
            element.sublist = res.data.filter(item => {
              if (element.hasOwnProperty('status')) {
                return (
                  element.type == item.type && element.status == item.status
                )
              }
              return element.type == item.type
            })
          }
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * 更多操作
     */
    handleMoreCommand(command, item) {
      this.getConfirmMessage(command, item, () => {
        this.loading = true
        adminConfigsetUpdate({
          settingId: item.settingId,
          status: command == 'disable' ? 0 : 1
        })
          .then(res => {
            this.loading = false
            window.location.reload()
          })
          .catch(() => {
            this.loading = false
          })
      })
    },

    /**
     * 操作提示
     */
    getConfirmMessage(command, item, result) {
      if (command == 'enable') {
        result()
      } else {
        const message = {
          oa:
            '停用办公后，与客户管理、项目管理中的关联项也将被停用。确定要停用吗？',
          crm:
            '停用客户管理后，与办公、项目管理中的关联项也将被停用。确定要停用吗？',
          work:
            '停用项目管理后，与办公、客户管理中的关联项也将被停用。确定要停用吗？'
        }[item.module]
        this.$confirm(message, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            result()
          })
          .catch(() => {
            this.$message({
              type: 'info',
              message: '已取消操作'
            })
          })
      }
    },

    getModuleIcon(status, moduleType) {
      if (moduleType == 'jxc' || moduleType == 'hrm') {
        return {
          jxc: require('@/assets/img/system/app/hrm.png'),
          hrm: require('@/assets/img/system/app/inventory.png')
        }[moduleType]
      } else if (moduleType == 'call') {
        return require('@/assets/img/system/app/call_enable.png')
      } else {
        return {
          oa: {
            disable: require('@/assets/img/system/app/oa_disable.png'),
            enable: require('@/assets/img/system/app/oa_enable.png')
          },
          crm: {
            disable: require('@/assets/img/system/app/crm_disable.png'),
            enable: require('@/assets/img/system/app/crm_enable.png')
          },
          project: {
            disable: require('@/assets/img/system/app/project_disable.png'),
            enable: require('@/assets/img/system/app/project_enable.png')
          }
        }[moduleType][status == 1 ? 'enable' : 'disable']
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.header {
  height: 60px;
  position: relative;
  z-index: 100;
  padding: 0 20px;
  font-size: 18px;
  div:first-child {
    margin-top: 15px;
    margin-bottom: 5px;
  }
  div:nth-child(2) {
    font-size: 12px;
    color: #999;
  }
}

.body {
  flex: 1;
  overflow-y: auto;
  padding-top: 20px;
  background-color: white;
  border: 1px solid #e6e6e6;
  border-radius: 2px;
}

.section-header {
  padding: 3px 20px;
  font-size: 12px;
  color: #999;
}

.section-body {
  padding: 20px;
  .section-item {
    width: auto;
    min-width: 240px;
    position: relative;
    padding: 20px 35px 12px 20px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    margin-right: 15px;
    margin-bottom: 10px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    vertical-align: middle;
    .item-icon {
      width: 40px;
      height: 40px;
      margin-right: 15px;
    }
    .item-name {
      font-size: 14px;
      color: #333333;
    }
  }
}

.el-icon-more {
  color: #cdcdcd;
  transform: rotate(90deg);
}

.more-menu {
  position: absolute;
  top: 8px;
  right: 8px;
}

.detail-button {
  position: absolute;
  top: 0px;
  right: 8px;
  color: #999;
  font-size: 12px;
}

.more-mark {
  color: #b3884f;
  font-size: 12px;
  background: linear-gradient(to right, #f9f3ed, #f8e1be);
  padding: 5px;
  border-radius: 2px;
  transform: scale(0.8);
  position: absolute;
  top: 6px;
  right: 4px;
}
</style>
