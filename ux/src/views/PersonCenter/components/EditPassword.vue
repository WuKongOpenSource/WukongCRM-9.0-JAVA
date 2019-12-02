<template>
  <!-- 新建和编辑 -->
  <el-dialog
    v-loading="loading"
    :visible.sync="showDialog"
    :before-close="hiddenView"
    title="编辑密码"
    width="500px">
    <el-form
      ref="ruleForm"
      :model="ruleForm"
      :rules="rules"
      label-width="80px"
      label-position="top">
      <el-form-item
        label="原密码"
        prop="oldPwd">
        <el-input v-model="ruleForm.oldPwd"/>
      </el-form-item>
      <el-form-item
        label="新密码"
        prop="newPwd">
        <el-input v-model="ruleForm.newPwd"/>
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
import { adminUsersResetPassword } from '@/api/personCenter/personCenter'
import { removeAuth } from '@/utils/auth'

export default {
  name: 'EditPassword', // 编辑个人密码
  components: {},
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      loading: false,
      showDialog: false,
      rules: {
        oldPwd: [
          { required: true, message: '请输入原密码', trigger: 'blur' },
          { min: 6, max: 12, message: '长度在 6 到 12 个字符', trigger: 'blur' }
        ],
        newPwd: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 12, message: '长度在 6 到 12 个字符', trigger: 'blur' }
        ]
      },
      ruleForm: {
        id: '',
        oldPwd: '',
        newPwd: ''
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
        this.ruleForm.id = this.userInfo.id
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
          adminUsersResetPassword(this.ruleForm)
            .then(res => {
              this.loading = false
              removeAuth().then(() => {
                this.$confirm('修改成功, 请重新登录', '提示', {
                  confirmButtonText: '确定',
                  showCancelButton: false,
                  type: 'warning'
                })
                  .then(() => {
                    this.$router.push('/login')
                  })
                  .catch(() => {})
              })
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
</style>
