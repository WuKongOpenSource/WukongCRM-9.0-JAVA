<template>
  <div class="f-container">
    <div v-loading="sendLoading">
      <mix-add
        ref="mixadd"
        :crm-type="crmType"
        :id="id"
        @mixadd-info="submitInfo"/>
      <flexbox class="se-section">
        <div class="se-name">记录类型</div>
        <el-dropdown
          style="margin-right: 20px;"
          trigger="click"
          @command="handleTypeDrop">
          <flexbox class="se-select">
            <div class="se-select-name">{{ followType ? followType : '请选择' }}</div>
            <i
              class="el-icon-arrow-down el-icon--right"
              style="color:#ccc;"/>
          </flexbox>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item
              v-for="(item, index) in followTypes"
              :key="index"
              :command="item.type">{{ item.name }}</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        <div class="se-name">下次联系时间</div>
        <el-date-picker
          v-model="nextTime"
          :default-value="new Date"
          :editable="false"
          class="se-datepicker"
          type="datetime"
          placeholder="选择日期"
          value-format="yyyy-MM-dd HH:mm:ss"/>
        <el-checkbox
          v-if="showOAPermission"
          v-model="isEvent">添加到日程提醒</el-checkbox>
        <el-button
          class="se-send"
          type="primary"
          @click.native="sendInfo">发布</el-button>
      </flexbox>
    </div>
    <div class="log-cont">
      <flexbox style="border-bottom: 1px solid #E6E6E6;">
        <flexbox
          v-for="(item, index) in logTypes"
          :key="index"
          style="width: auto;"
          @click.native="logTabsClick(item,index)">
          <div
            :style="{ color: logType==item.type ? '#F18C70' : '#777'}"
            class="log-tabs-item">{{ item.name }}</div>
          <div
            v-if="logTypes.length -1 != index"
            class="log-tabs-line"/>
        </flexbox>
      </flexbox>
      <keep-alive>
        <component
          :is="componentsName"
          :id="id"
          :crm-type="crmType"/>
      </keep-alive>
    </div>
  </div>
</template>

<script>
import MixAdd from '../../components/MixAdd'
import RecordLog from '../../components/followLog/RecordLog' // 跟进记录
import JournalLog from '../../components/followLog/JournalLog' // 日志列表
import ExamineLog from '../../components/followLog/ExamineLog' // 审批列表
import TaskLog from '../../components/followLog/TaskLog' // 任务日志列表
import ScheduleLog from '../../components/followLog/ScheduleLog' // 日程日志列表
import { crmContractRecordSave } from '@/api/customermanagement/contract'
import followLogType from '@/views/customermanagement/mixins/followLogType'

export default {
  /** 客户管理 的 合同详情 的 跟进记录*/
  name: 'ContractFollow',
  components: {
    MixAdd,
    RecordLog,
    JournalLog,
    ExamineLog,
    TaskLog,
    ScheduleLog
  },
  mixins: [followLogType],
  props: {
    /** 模块ID */
    id: [String, Number],
    /** 没有值就是全部类型 有值就是当个类型 */
    crmType: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      sendLoading: false,
      /** 下次联系时间 */
      nextTime: '',
      /** 是否添加日程提醒 */
      isEvent: false,
      logType: 'record'
    }
  },
  computed: {
    logTypes() {
      if (this.oa) {
        return [
          { type: 'record', name: '跟进记录' },
          { type: 'log', name: '日志' },
          { type: 'examine', name: '审批' },
          { type: 'task', name: '任务' },
          { type: 'schedule', name: '日程' }
        ]
      } else {
        return [{ type: 'record', name: '跟进记录' }]
      }
    },

    componentsName() {
      if (this.logType == 'record') {
        return 'RecordLog'
      } else if (this.logType == 'log') {
        return 'JournalLog'
      } else if (this.logType == 'examine') {
        return 'ExamineLog'
      } else if (this.logType == 'task') {
        return 'TaskLog'
      } else if (this.logType == 'schedule') {
        return 'ScheduleLog'
      }
      return ''
    }
  },
  mounted() {},
  activated: function() {},
  deactivated: function() {},
  methods: {
    /** 发布 时候的类型选择 */
    handleTypeDrop(command) {
      this.followType = command
    },
    logTabsClick(item, index) {
      this.logType = item.type
    },
    /** 告诉mixad 返回数据 */
    sendInfo() {
      this.$refs.mixadd.$emit('submit-info')
    },
    /** 快捷添加框内 返回的数据用于上传 */
    submitInfo(data) {
      if (this.isEvent && !this.nextTime) {
        this.$message.error('请选择下次联系时间')
        return
      } else if (!data.content) {
        this.$message.error('请输入跟进内容')
        return
      }

      var params = {}
      params.typesId = this.id
      params.content = data.content
      params.category = this.followType
      params.batchId = data.batchId
      params.isEvent = this.isEvent ? 1 : 0
      params.nextTime = this.nextTime || ''

      this.sendLoading = true
      crmContractRecordSave(params)
        .then(res => {
          this.sendLoading = false
          this.$message.success('发布成功')
          // 重置页面
          this.$refs.mixadd.resetInfo()
          this.isEvent = false
          this.nextTime = ''
          // 刷新数据
          this.$bus.emit('follow-log-refresh', { type: 'record-log' })
        })
        .catch(() => {
          this.sendLoading = false
        })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/followlog.scss';
</style>
