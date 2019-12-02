<template>
  <!-- 新建和编辑 -->
  <el-dialog
    v-loading="loading"
    :visible.sync="showDialog"
    :before-close="hiddenView"
    title="编辑个人信息"
    width="500px">
    <el-form
      ref="ruleForm"
      :model="ruleForm"
      :rules="rules"
      label-width="80px"
      class="create-box"
      label-position="top">
      <el-form-item
        label="姓名"
        class="create-item"
        prop="realname">
        <el-input v-model="ruleForm.realname"/>
      </el-form-item>
      <el-form-item
        label="邮箱"
        class="create-item"
        prop="email">
        <el-input v-model="ruleForm.email"/>
      </el-form-item>
      <el-form-item
        label="性别"
        class="create-item"
        prop="sex">
        <el-select
          v-model="ruleForm.sex"
          style="display: block;"
          placeholder="请选择">
          <el-option
            v-for="item in [{ label: '男', value: 1 }, { label: '女', value: 2 }]"
            :key="item.value"
            :label="item.label"
            :value="item.value"/>
        </el-select>
      </el-form-item>
      <el-form-item
        label="手机号（登录名）"
        class="create-item"
        prop="username">
        <el-input
          v-model="ruleForm.username"
          :disabled="true"/>
      </el-form-item>
    </el-form>
    <span
      slot="footer"
      class="dialog-footer">
      <el-button
        type="primary"
        @click="save">保 存</el-button>
      <el-button @click="hiddenView">取 消</el-button>
    </span>
  </el-dialog>
</template>
<script type="text/javascript">
import { mapGetters } from 'vuex'
import { adminUsersUpdate } from '@/api/personCenter/personCenter'
import { regexIsCRMMobile, regexIsCRMEmail } from '@/utils'

export default {
  name: 'EditInfo', // 编辑个人信息
  components: {},
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  data() {
    var validateCRMMobile = (rule, value, callback) => {
      if (!value || value == '' || regexIsCRMMobile(value)) {
        callback()
      } else {
        callback(new Error('手机格式有误'))
      }
    }
    var validateCRMEmail = (rule, value, callback) => {
      if (!value || value == '' || regexIsCRMEmail(value)) {
        callback()
      } else {
        callback(new Error('邮箱格式有误'))
      }
    }
    return {
      loading: false,
      showDialog: false,
      rules: {
        realname: [{ required: true, message: '请填写姓名', trigger: 'blur' }],
        email: [{ validator: validateCRMEmail, trigger: 'change' }],
        username: [
          { required: true, message: '请填写姓名', trigger: 'blur' },
          { validator: validateCRMMobile, trigger: 'change' }
        ]
      },
      ruleForm: {
        realname: '',
        email: '',
        sex: '',
        username: ''
      }
    }
  },
  computed: {
    ...mapGetters(['userInfo'])
  },
  watch: {
    show: {
      handler(val) {
        this.showDialog = val
        this.ruleForm = {
          realname: this.userInfo.realname,
          email: this.userInfo.email,
          sex: this.userInfo.sex,
          username: this.userInfo.username
        }
      },
      deep: true,
      immediate: true
    }
  },
  mounted() {},
  methods: {
    hiddenView() {
      this.$emit('close')
    },
    save() {
      this.$refs.ruleForm.validate(valid => {
        if (valid) {
          this.loading = true
          adminUsersUpdate(this.ruleForm)
            .then(res => {
              this.loading = false
              this.$emit('save')
              this.hiddenView()
            })
            .catch(() => {
              this.loading = false
            })
        } else {
          return false
        }
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.create-box {
  display: flex;
  flex-wrap: wrap;
}

.create-item {
  flex: 0 0 50%;
  flex-shrink: 0;
  padding: 0 10px;
}

.create-item {
  flex: 0 0 50%;
  flex-shrink: 0;
  padding: 0 10px;
}

.el-dialog__wrapper /deep/.el-dialog__body {
  padding: 20px;
}
</style>
