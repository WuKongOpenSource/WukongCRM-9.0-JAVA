<template>
  <div v-loading="loading"
       class="person-container">
    <div class="header-handle">
      <el-button type="primary"
                 @click="editClick('password')">修改密码</el-button>
      <el-button type="primary"
                 @click="editClick('info')">编辑</el-button>
      <el-button plain
                 @click="editClick('back')">返回</el-button>
    </div>
    <flexbox class="person-info">
      <div class="person-head">
        <div v-photo="userInfo"
             v-lazy:background-image="$options.filters.filterUserLazyImg(userInfo.img)"
             class="div-photo person-head-img"></div>
        <div class="select-picture"
             @click="changePersonImage">
          <div>点击更换头像</div>
        </div>
      </div>
      <div class="person-body">
        <div class="person-name">{{userInfo.realname}}</div>
        <div class="person-detail">部门：{{userInfo.deptName}}&nbsp;&nbsp;&nbsp;&nbsp;职位：{{userInfo.post}}</div>
      </div>
    </flexbox>
    <div class="segmentation"></div>
    <div class="section">
      <div class="section-header">
        <div class="section-mark"></div>
        <div class="section-title">基本信息</div>
      </div>
      <flexbox :gutter="0"
               wrap="wrap"
               class="content">
        <flexbox-item v-for="(item, index) in list"
                      :key="index"
                      :span="0.5"
                      class="info-cell">
          <flexbox align="stretch"
                   class="info-cell-box">
            <div class="info-cell-box-name">{{item.name}}</div>
            <div class="info-cell-box-value">{{item.props!=='sex'?userInfo[item.props]:userInfo[item.props]===1?'男':userInfo[item.props]===2?'女':""}}</div>
          </flexbox>
        </flexbox-item>
      </flexbox>
    </div>
    <input type="file"
           id="inputFile"
           accept="image/png, image/jpeg, image/gif, image/jpg"
           @change="uploadFile">
    <edit-image :show="showEditImage"
                :file="editFile"
                :image="editImage"
                @save="submiteImage"
                @close="showEditImage=false"></edit-image>
    <edit-info :show="showEditInfo"
               @save="getDetail"
               @close="showEditInfo=false"></edit-info>
    <edit-password :show="showEditPassword"
                   @save="getDetail"
                   @close="showEditPassword=false"></edit-password>
  </div>
</template>

<script>
import {
  adminUsersRead,
  adminUsersUpdateImg
} from '@/api/personCenter/personCenter'

import { mapGetters } from 'vuex'
import EditImage from '@/components/EditImage'
import EditInfo from './components/EditInfo'
import EditPassword from './components/EditPassword'

export default {
  name: 'person',
  components: {
    EditImage,
    EditInfo,
    EditPassword
  },
  computed: {
    ...mapGetters(['userInfo'])
  },
  data() {
    return {
      loading: false,
      showEditImage: false,
      editImage: null,
      editFile: null,
      showEditInfo: false,
      showEditPassword: false,
      list: [
        { name: '姓名', props: 'realname' },
        { name: '性别', props: 'sex' },
        { name: '手机号（登录名）', props: 'username' },
        { name: '邮箱', props: 'email' },
        { name: '部门', props: 'deptName' },
        { name: '岗位', props: 'post' },
        { name: '直属上级', props: 'parentName' }
      ]
    }
  },
  mounted() {
    this.getDetail()
  },
  methods: {
    getDetail() {
      this.loading = true
      this.$store
        .dispatch('GetUserInfo')
        .then(res => {
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },
    editClick(type) {
      if (type == 'password') {
        this.showEditPassword = true
      } else if (type == 'info') {
        this.showEditInfo = true
      } else if (type == 'back') {
        this.$router.go(-1)
      }
    },
    changePersonImage() {
      document.getElementById('inputFile').click()
    },
    /** 图片选择出发 */
    uploadFile(event) {
      var files = event.target.files
      const file = files[0]
      const reader = new FileReader()
      var self = this
      reader.onload = function(e) {
        let result
        if (typeof e.target.result === 'object') {
          // 把Array Buffer转化为blob 如果是base64不需要
          result = window.URL.createObjectURL(new Blob([e.target.result]))
        } else {
          result = e.target.result
        }
        self.editImage = result
        self.editFile = file
        self.showEditImage = true
        e.target.value = ''
      }
      reader.readAsDataURL(file)
    },
    submiteImage(data) {
      this.loading = true
      var param = new FormData()
      param.append('userId', this.userInfo.userId)
      param.append('file', data.blob, data.file.name)
      adminUsersUpdateImg(param)
        .then(res => {
          this.loading = false
          this.getDetail()
        })
        .catch(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.person {
  &-container {
    width: 900px;
    min-width: 900px;
    margin: 0 auto;
    height: 100%;
    border: 1px solid #e6e6e6;
    background-color: white;
    border-radius: 2px;
    overflow-y: auto;
  }
}

.header-handle {
  float: right;
  padding: 10px 30px;
}

.person-info {
  padding: 0 80px 30px;
  position: relative;
  .person-head {
    .person-head-img {
      display: block;
      width: 120px;
      height: 120px;
      border-radius: 60px;
      margin-right: 35px;
      cursor: pointer;
    }
    .select-picture {
      position: absolute;
      display: none;
      z-index: 3;
      top: 0;
      left: 80px;
      width: 120px;
      height: 120px;
      border-radius: 60px;
      background-color: rgba(0, 0, 0, 0.2);
      text-align: center;
      cursor: pointer;
      div {
        cursor: pointer;
        margin-top: 55px;
        color: white;
        font-size: 12px;
      }
    }
  }
  .person-head:hover {
    .select-picture {
      display: block;
    }
  }
  .person-body {
    .person-name {
      font-size: 25px;
      margin-bottom: 15px;
    }
    .person-detail {
      font-size: 12px;
      color: #777;
    }
  }
}

// 分割线
.segmentation {
  height: 4px;
  background-color: #f5f6f9;
}

.section {
  padding: 30px 80px;
  position: relative;
  background-color: white;
  margin-top: 8px;
  .section-mark {
    border-left: 2px solid #46cdcf;
    height: 10px;
  }

  .section-header {
    display: flex;
    align-items: center;
    padding: 5px 15px;
  }
  .section-title {
    font-size: 13px;
    color: #333;
    margin-left: 8px;
    flex-shrink: 0;
  }
}

.content {
  position: relative;
  padding: 10px 20px;
}

.info-cell {
  padding: 0 10px;
  &-box {
    width: auto;
    padding: 8px;
    &-name {
      width: 100px;
      margin-right: 10px;
      font-size: 12px;
      flex-shrink: 0;
      color: #777;
    }
    &-value {
      font-size: 12px;
      color: #333;
    }
  }
}

#inputFile {
  display: none;
}
</style>
