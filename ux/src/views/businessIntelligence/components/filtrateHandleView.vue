
<template>
  <div class="filtrate-content">
    <time-type-select v-if="!showYearSelect"
                      @change="timeTypeChange"></time-type-select>
    <!-- 展示年筛选 -->
    <el-date-picker v-if="showYearSelect"
                    v-model="yearValue"
                    type="year"
                    :clearable="false"
                    value-format="yyyy"
                    :picker-options="pickerOptions"
                    placeholder="选择年">
    </el-date-picker>
    <!-- 展示部门筛选 -->
    <el-select v-model="structuresSelectValue"
               @change="structuresValueChange"
               placeholder="选择部门">
      <el-option v-for="item in deptList"
                 :key="item.id"
                 :label="item.name"
                 :value="item.id">
      </el-option>
    </el-select>
    <el-select v-if="showUserSelect"
               v-model="userSelectValue"
               :clearable="true"
               placeholder="选择员工">
      <el-option v-for="item in userOptions"
                 :key="item.id"
                 :label="item.realname"
                 :value="item.id">
      </el-option>
    </el-select>
    <!-- 展示商机状态筛选 -->
    <el-select v-model="businessStatusValue"
               v-if="showBusinessSelect"
               placeholder="商机组">
      <el-option v-for="item in businessOptions"
                 :key="item.typeId"
                 :label="item.name"
                 :value="item.typeId">
      </el-option>
    </el-select>
    <el-cascader v-if="showProductSelect"
                 style="width: 100%;"
                 :options="productOptions"
                 change-on-select
                 :show-all-levels="false"
                 :props="{
                    children: 'children',
                    label: 'label',
                    value: 'categoryId'
                  }"
                 v-model="productValue">
    </el-cascader>
    <el-select v-model="customValue"
               v-if="showCustomSelect"
               placeholder="图标类型"
               @change="customSelectChange">
      <el-option v-for="item in customOptions"
                 :key="item.value"
                 :label="item.name"
                 :value="item.value">
      </el-option>
    </el-select>
    <el-button @click.native="postFiltrateValue()"
               type="primary">搜索</el-button>
    <slot></slot>
  </div>
</template>

<script type="text/javascript">
import {
  adminStructuresSubIndex,
  userListByStructid,
  getUserByDeptId
} from '@/api/common'
import { crmBusinessStatusList } from '@/api/customermanagement/business'
import { productCategoryIndex } from '@/api/systemManagement/SystemCustomer'
import timeTypeSelect from '@/components/timeTypeSelect'
import moment from 'moment'

export default {
  name: 'filtrate-handle-view', // 筛选条件
  components: { timeTypeSelect },
  watch: {},
  data() {
    return {
      //年筛选
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now()
        }
      },
      yearValue: '',

      /** 时间类型值 */
      timeTypeValue: {},
      /** 部门选择解析数据 */
      structuresProps: {
        children: 'children',
        label: 'label',
        value: 'id'
      },
      deptList: [], // 部门列表
      structuresSelectValue: '',
      /** 用户列表 */
      userOptions: [],
      userSelectValue: '',
      /** 商机状态 */
      businessOptions: [],
      businessStatusValue: '',
      /** 产品类别 */
      productValue: [],
      productOptions: [],
      // 图标类型
      customValue: ''
    }
  },
  props: {
    // 模块类型
    moduleType: {
      required: true,
      type: String
    },
    // 是否展示年
    showYearSelect: {
      default: false,
      type: Boolean
    },
    // 是否展示商机状态筛选
    showBusinessSelect: {
      default: false,
      type: Boolean
    },
    // 展示员工选择
    showUserSelect: {
      default: true,
      type: Boolean
    },
    showCustomSelect: {
      default: false,
      type: Boolean
    },
    customDefault: '',
    customOptions: {
      default: () => {
        return []
      },
      type: Array
    },
    // 展示产品类别选择
    showProductSelect: {
      default: false,
      type: Boolean
    }
  },
  mounted() {
    // 自定义选择项 默认值
    if (this.showCustomSelect) {
      this.customValue = this.customDefault
    }
    if (this.showYearSelect) {
      this.yearValue = moment(new Date())
        .year()
        .toString()
    }

    this.$emit('load')
    this.getDeptList(() => {
      if (this.showBusinessSelect) {
        this.getBusinessStatusList(() => {
          this.postFiltrateValue()
        })
      } else {
        this.postFiltrateValue()
      }
    })

    if (this.showProductSelect) {
      this.getProductCategoryIndex()
    }
  },
  methods: {
    // 选择更改
    customSelectChange() {
      this.$emit('typeChange', this.customValue)
    },
    // 类型选择
    timeTypeChange(data) {
      this.timeTypeValue = data
    },
    /**
     * 获取部门列表
     */
    getDeptList(result) {
      adminStructuresSubIndex({
        m: 'bi',
        c: this.moduleType,
        a: 'read'
      })
        .then(res => {
          this.deptList = res.data
          if (res.data.length > 0) {
            this.structuresSelectValue = res.data[0].id
            if (this.showUserSelect) {
              this.getUserList() // 更新员工列表
            }
          } else {
            this.structuresSelectValue = ''
          }
          result(true)
        })
        .catch(() => {
          this.$emit('error')
        })
    },
    /** 部门更改 */
    structuresValueChange(data) {
      // 展示员工筛选，执行更新逻辑
      if (this.showUserSelect) {
        this.userSelectValue = ''
        this.userOptions = []
        this.getUserList() // 更新员工列表
      }
    },
    /** 部门下员工 */
    getUserList() {
      var params = {}
      params.deptId = this.structuresSelectValue
      getUserByDeptId(params)
        .then(res => {
          this.userOptions = res.data
        })
        .catch(() => {
          this.$emit('error')
        })
    },
    /** 商机阶段 */
    getBusinessStatusList(result) {
      crmBusinessStatusList()
        .then(res => {
          this.businessOptions = res.data
          if (res.data.length > 0) {
            this.businessStatusValue = res.data[0].typeId
          }
          result(true)
        })
        .catch(() => {
          this.$emit('error')
        })
    },
    /** 获取产品分类数据 */
    getProductCategoryIndex() {
      productCategoryIndex({
        type: 'tree'
      })
        .then(res => {
          this.productOptions = res.data
        })
        .catch(() => {})
    },
    postFiltrateValue() {
      let params = {
        deptId: this.structuresSelectValue
      }

      // 展示员工，返回员工参数
      if (this.showUserSelect) {
        params.userId = this.userSelectValue
      }

      // 展示商机状态 返回商机状态参数
      if (this.showBusinessSelect) {
        params.typeId = this.businessStatusValue
      }

      if (this.showProductSelect) {
        params.categoryId =
          this.productValue.length > 0
            ? this.productValue[this.productValue.length - 1]
            : ''
      }

      // 展示年和展示时间段类型不同时出现
      if (this.showYearSelect) {
        params.year = this.yearValue
      } else {
        if (this.timeTypeValue.type == 'custom') {
          params.startTime = this.timeTypeValue.startTime
          params.endTime = this.timeTypeValue.endTime
        } else {
          params.type = this.timeTypeValue.value
        }
      }
      this.$emit('change', params)
    }
  },

  beforeDestroy() {}
}
</script>
<style lang="scss" scoped>
.filtrate-content {
  padding: 15px 20px 5px 20px;
  .el-date-editor {
    width: 130px;
    margin-right: 15px;
  }

  .el-cascader {
    width: 130px;
    margin-right: 15px;
  }
  .el-select {
    width: 120px;
    margin-right: 15px;
  }

  .el-cascader {
    width: 120px !important;
    margin-right: 15px;
  }
}

// 时间选择
.type-popper {
  .type-content {
    height: 250px;
    overflow-y: auto;
    .type-content-item {
      height: 34px;
      line-height: 34px;
      padding: 0 20px;
      color: #606266;
      position: relative;
      cursor: pointer;
      .mark {
        display: inline-block;
        width: 8px;
        height: 8px;
        border-radius: 4px;
        margin-right: 5px;
        background-color: transparent;
      }
    }

    .selected {
      color: #409eff;
      font-weight: 700;
      .mark {
        background-color: #409eff;
      }
    }
    .type-content-item:hover {
      background-color: #f5f7fa;
    }
  }

  .type-content-custom {
    padding: 5px 20px 10px;
    position: relative;
    overflow: hidden;
    .el-date-editor {
      width: 100%;
      margin-bottom: 8px;
    }

    button {
      float: right;
    }
  }
}
</style>
