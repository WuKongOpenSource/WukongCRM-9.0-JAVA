<template>
  <div class="wrapper">
    <div class="left">
      <div class="left-pic" />
    </div>
    <div class="right">
      <el-form ref="loginForm"
               :model="loginForm"
               :rules="loginRules"
               class="login-form"
               auto-complete="on"
               label-position="left">
        <div class="title">{{name}}</div>
        <el-form-item prop="username">
          <el-input ref="name"
                    v-model="loginForm.username"
                    autofocus="autofocus"
                    name="username"
                    type="text"
                    auto-complete="on"
                    placeholder="请输入用户名"
                    @keyup.enter.native="handleLogin" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input type="password"
                    v-model="loginForm.password"
                    name="password"
                    auto-complete="on"
                    placeholder="请输入密码"
                    @keyup.enter.native="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button :loading="loading"
                     @click.native.prevent="handleLogin"
                     class="submit-btn">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="copyright">
        悟空CRM受国家计算机软件著作权保护，未经授权不得进行商业行为，违者必究。<br>
        ©2019 悟空软件<a target="_blank"
           href="http://www.5kcrm.com">www.5kcrm.com</a>
      </div>
    </div>

    <img class="logo"
         :src="logo" />
  </div>
</template>

<script>
import { isvalidUsername } from '@/utils/validate'
import { mapGetters } from 'vuex'
import Lockr from 'lockr'

export default {
  name: 'Login',
  data() {
    const validateUsername = (rule, value, callback) => {
      if (value.length == 0) {
        callback(new Error('请输入账号'))
      } else {
        callback()
      }
    }
    const validatePass = (rule, value, callback) => {
      if (value.length < 5) {
        callback(new Error('密码不能小于5位'))
      } else {
        callback()
      }
    }
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loginRules: {
        username: [
          { required: true, trigger: 'blur', validator: validateUsername }
        ],
        password: [{ required: true, trigger: 'blur', validator: validatePass }]
      },
      loading: false,
      redirect: undefined,
      remember: false
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  computed: {
    ...mapGetters(['logo', 'name'])
  },
  mounted() {},
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          this.$store
            .dispatch('Login', this.loginForm)
            .then(res => {
              this.loading = false
              this.$router.push({ path: this.redirect || '/workbench/index' })
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
$dark_gray: #ccc;
$light_gray: #333;
$login_theme: #00aaee;

/deep/ input {
  border: 0 none;
  background-color: white;
  -webkit-appearance: none;
  &:-webkit-autofill {
    background-image: none;
    -webkit-box-shadow: 0 0 0 1000px white inset !important;
    -webkit-text-fill-color: $light_gray !important;
  }
}
/deep/ .el-input__inner {
  height: 40px;
  padding: 0 12px;
  background-color: white;
  border: 0 none;
  border-bottom: 1px solid #e6e6e6 !important;
}
/deep/ .el-form-item__error {
  left: 12px;
}
.wrapper {
  position: relative;
  width: 100%;
  min-width: 1300px;
  display: flex;
  .left {
    width: 68%;
    .left-pic {
      width: 100%;
      height: 100%;
      background: url('../../assets/img/login/login.png') no-repeat center;
      background-size: cover;
    }
  }
  .right {
    position: relative;
    width: 32%;
    background-color: #fff;
    display: flex;
    align-items: center;
    flex-direction: column;
    padding-top: 6%;
    .el-form {
      width: 70%;
      .title {
        font-size: 26px;
        color: $light_gray;
        margin: 0 auto 50px;
        text-align: center;
      }
      .submit-btn {
        width: 100%;
        line-height: 2;
        font-size: 16px;
        color: white;
        border-radius: 3px;
        background-color: $login_theme;
        display: block;
      }
      .el-button {
        border: 0 none;
      }
      .action-control {
        color: #999;
        /deep/ .el-checkbox {
          .el-checkbox__label {
            color: #999;
          }
          .el-checkbox__input.is-checked .el-checkbox__inner {
            background-color: $login_theme;
            border-color: $login_theme;
          }
        }

        .forget {
          cursor: pointer;
          float: right;
        }
      }
    }

    .register {
      width: 70%;
      padding-top: 30px;
      color: $light_gray;
      border-top: 1px solid #e6e6e6;
      text-align: center;
      margin-top: 28px;
      .register-btn {
        color: $login_theme;
        cursor: pointer;
      }
    }

    .copyright {
      width: 92%;
      position: absolute;
      bottom: 2%;
      color: #d0d0d0;
      font-size: 12px;
      text-align: center;
      line-height: 1.5;
    }
  }

  .logo {
    position: absolute;
    left: 60px;
    top: 50px;
    width: 180px;
    height: 48px;
    z-index: 200;
  }
}
</style>
