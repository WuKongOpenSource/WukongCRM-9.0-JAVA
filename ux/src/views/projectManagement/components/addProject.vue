<template>
  <create-view
    v-loading="loading"
    :body-style="{height: '100%'}">
    <div class="add-project">
      <div
        slot="header"
        class="header">
        <span class="text">创建项目</span>
        <span
          class="el-icon-close"
          @click="close"/>
      </div>
      <div class="content">
        <div class="project-name">
          <div class="label color-label">项目名称</div>
          <el-input
            v-model="name"
            placeholder="请输入内容">
            <i
              slot="prefix"
              class="el-input__icon">
              <span
                :style="{'background': typeColor}"
                class="bg-color"/>
            </i>
          </el-input>
          <div class="color-box">
            <span
              v-for="(item, index) in typeColorList"
              :key="index"
              :style="{'background': item}"
              @click="typeColor = item"/>
          </div>
        </div>
        <div class="describe">
          <div class="label">项目描述</div>
          <el-input
            :rows="4"
            v-model="description"
            type="textarea"
            placeholder="请输入内容"/>
        </div>
        <div class="range">
          <div class="label">可见范围</div>
          <el-select
            v-model="openType"
            placeholder="请选择">
            <el-option
              v-for="item in openOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"/>
          </el-select>
        </div>
        <div
          v-if="openType == 0"
          class="member">
          <div class="label">项目成员</div>
          <div>
            <div
              v-photo="k"
              v-lazy:background-image="$options.filters.filterUserLazyImg(k.img)"
              v-for="(k, j) in selectUserList"
              :key="j"
              class="div-photo k-img header-circle"/>
            <members-dep
              :user-checked-data="selectUserList"
              :content-block="false"
              :close-dep="true"
              @popoverSubmit="userSelectChange">
              <img
                slot="membersDep"
                class="sent-img"
                src="@/assets/img/task_add.png">
            </members-dep>
          </div>
        </div>
        <div class="footer">
          <el-button
            type="primary"
            @click="submitBtn">确定</el-button>
          <el-button @click="close">取消</el-button>
        </div>
      </div>
    </div>
  </create-view>
</template>
<script>
import { mapGetters } from 'vuex'
import CreateView from '@/components/CreateView'
import { workWorkSaveAPI } from '@/api/projectManagement/project'
import MembersDep from '@/components/selectEmployee/membersDep'

export default {
  components: {
    CreateView,
    MembersDep
  },

  props: {},

  data() {
    return {
      loading: false,
      name: '',
      description: '',
      typeColor: '#53D397',
      typeColorList: [
        '#53D397',
        '#20C1BD',
        '#58DADA',
        '#0FC9E7',
        '#3498DB',
        '#4586FF',
        '#8983F3',
        '#AEA1EA',
        '#FF6699',
        '#F24D70',
        '#FF6F6F'
      ],
      openType: 0,
      openOptions: [
        {
          value: 0,
          label: '私有：只有加入的成员才能看见此项目'
        },
        {
          value: 1,
          label: '公开：企业所有成员都可以看见此项目'
        }
      ],

      selectUserList: []
    }
  },

  computed: {
    ...mapGetters(['userInfo'])
  },

  created() {
    this.selectUserList.push(this.userInfo)
  },

  mounted() {
    document.body.appendChild(this.$el)
  },

  destroyed() {
    // if appendToBody is true, remove DOM node after destroy
    if (this.appendToBody && this.$el && this.$el.parentNode) {
      this.$el.parentNode.removeChild(this.$el)
    }
  },

  methods: {
    /**
     * 保存
     */
    submitBtn() {
      this.loading = true
      const params = {
        name: this.name,
        description: this.description,
        color: this.typeColor,
        isOpen: this.openType
      }
      if (this.openType == 0) {
        params.ownerUserId = this.selectUserList
          .map(item => {
            return item.userId
          })
          .join(',')
      }
      workWorkSaveAPI(params)
        .then(res => {
          this.loading = false
          this.$emit('save-success')
          this.$bus.$emit('add-project', this.name, res.work.workId)
          this.close()
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * 关闭窗口
     */
    close() {
      this.$emit('close')
    },

    /**
     * 项目成员
     */
    userSelectChange(members, dep) {
      this.selectUserList = members
    }
  }
}
</script>

<style scoped lang="scss">
@mixin rt {
  float: right;
  color: #ccc;
  font-size: 26px;
}
$color3: #333;
.add-project {
  position: relative;
  height: 100%;
}
.header {
  overflow: hidden;
  margin-bottom: 20px;
  .text {
    color: $color3;
    font-size: 17px;
    height: 40px;
    line-height: 40px;
  }
  .el-icon-close {
    @include rt;
  }
}
.content {
  padding: 0 10px;
  .project-name {
    .color-box {
      margin: 10px 0;
      span {
        display: inline-block;
        width: 15px;
        height: 15px;
        margin-right: 10px;
        border-radius: 50%;
        cursor: pointer;
      }
      span:last-child {
        margin: 0;
      }
    }
    .color-label {
      padding-left: 10px;
      border-left: 2px solid #f56c6c;
    }
    .el-input {
      width: 40%;
    }
    .bg-color {
      width: 15px;
      height: 15px;
      border-radius: 50%;
      display: inline-block;
      vertical-align: middle;
    }
  }
  .label {
    margin-bottom: 12px;
    color: $color3;
    font-size: 13px;
  }
  .range {
    margin: 15px 0;
    .el-select {
      width: 40%;
    }
  }
  .member {
    img {
      width: 25px;
      cursor: pointer;
    }
    .item-name {
      margin-right: 7px;
    }
    .k-img {
      width: 25px;
      height: 25px;
      border-radius: 17.5px;
      margin-right: 7px;
    }
    .sent-img {
      width: 25px;
      height: 25px;
    }
  }
  .footer {
    position: absolute;
    bottom: 0;
    right: 20px;
  }
}
</style>
