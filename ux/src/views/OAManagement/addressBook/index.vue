<template>
  <div class="address-book oa-bgcolor">
    <div class="header">
      <el-tabs v-model="activeName">
        <el-tab-pane label="员工"
                     name="1">
          <el-input placeholder="搜索成员"
                    prefix-icon="el-icon-search"
                    @blur="blurFun"
                    @keyup.enter.native="blurFun"
                    v-model="userInput">
          </el-input>
          <v-staff v-loading="staffLoading"
                   :staffData="staffData">
          </v-staff>
        </el-tab-pane>
        <el-tab-pane label="部门"
                     name="2">
          <el-input placeholder="搜索成员"
                    prefix-icon="el-icon-search"
                    @blur="blurFun"
                    v-model="depInput">
          </el-input>
          <v-department :depData="depData">
          </v-department>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import {
  addresslist,
  queryListNameByDept
} from '@/api/oamanagement/addressBook'
import VStaff from './staff'
import VDepartment from './department'
export default {
  components: {
    VStaff,
    VDepartment
  },
  data() {
    return {
      activeName: '1',
      staffData: [],
      depData: [],
      userInput: '',
      depInput: '',
      staffLoading: true
    }
  },
  created() {
    // 员工
    this.dataUserFun()
    // 部门
    this.dataDepFun()
  },
  methods: {
    dataUserFun() {
      this.staffLoading = true
      addresslist({
        search: this.userInput
      }).then(res => {
        let words = [
          '#',
          'A',
          'B',
          'C',
          'D',
          'E',
          'F',
          'G',
          'H',
          'I',
          'J',
          'K',
          'L',
          'M',
          'N',
          'O',
          'P',
          'Q',
          'R',
          'S',
          'T',
          'U',
          'V',
          'W',
          'X',
          'Y',
          'Z'
        ]
        
        for (let key of words) {
          let list = res.data[key]
          if (list) {
            this.staffData.push({
              letter: key,
              list: list
            })
          }
        }
        this.staffLoading = false
      })
    },
    dataDepFun(search) {
      queryListNameByDept({
        name: this.depInput
      }).then(res => {
        this.depData = res.data
      })
    },
    blurFun() {
      this.staffData = []
      this.activeName == '1' ? this.dataUserFun() : this.dataDepFun()
    }
  }
}
</script>

<style scoped lang="scss">
@import '../styles/tabs.scss';
.address-book {
  .header {
    height: 100%;
    .el-tabs /deep/ {
      height: 100%;
      display: flex;
      flex-direction: column;
      .el-tabs__content {
        flex: 1;
        display: flex;
        flex-direction: column;
        .el-tab-pane {
          display: flex;
          flex-direction: column;
          flex: 1;
          min-height: 0;
        }
        .el-input {
          width: 230px;
          margin: 10px 30px;
        }
        .k-list {
          margin-bottom: 10px;
          .list-right,
          .header-circle {
            display: inline-block;
          }
          .header-circle {
            width: 42px;
            height: 42px;
            margin-right: 10px;
            vertical-align: middle;
          }
          .list-right {
            vertical-align: middle;
            .content {
              color: #777;
              img,
              span {
                vertical-align: middle;
              }
            }
            .content > div {
              display: inline-block;
              margin-right: 15px;
            }
            .k-realname {
              font-size: 14px;
              margin-right: 15px;
              width: 96px;
            }
            .k-realname,
            .content {
              display: inline-block;
            }
          }
        }
      }
    }
  }
}
</style>
