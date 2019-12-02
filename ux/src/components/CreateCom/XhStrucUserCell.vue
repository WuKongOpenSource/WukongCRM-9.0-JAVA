<template>
  <members-dep
    :popover-display="'block'"
    :dep-checked-data="dataStrucs"
    :user-checked-data="dataUsers"
    @popoverSubmit="popoverSubmit">
    <div slot="membersDep">
      <flexbox
        wrap="wrap"
        class="structure-container">
        <div
          v-for="(item, index) in dataUsers"
          :key="'user'+index"
          class="user-item">{{ item.name ? item.name : item.realname }}
        </div>
        <div
          v-for="(item, index) in dataStrucs"
          :key="'struc'+index"
          class="user-item">{{ item.name }}
        </div>
        <div class="add-item">+添加</div>
      </flexbox>
    </div>
  </members-dep>

</template>
<script type="text/javascript">
import membersDep from '@/components/selectEmployee/membersDep'

export default {
  name: 'XhStrucUserCell', // 新建 struc-user-cell
  components: {
    membersDep
  },
  props: {
    // 员工和 部门
    users: {
      type: Array,
      default: () => {
        return []
      }
    },
    strucs: {
      type: Array,
      default: () => {
        return []
      }
    },
    value: {
      type: Object,
      default: () => {
        return {
          users: [],
          strucs: []
        }
      }
    },
    /** 索引值 用于更新数据 */
    index: Number,
    /** 包含数据源 */
    item: Object
  },
  data() {
    return {
      dataUsers: [], // 关联的时候展示name 编辑的时候展示realname
      dataStrucs: []
    }
  },
  computed: {},
  watch: {
    value: function(val) {
      this.dataUsers = val.users
      this.dataStrucs = val.strucs
    },
    users: function(val) {
      this.dataUsers = val
    },
    strucs: function(val) {
      this.dataStrucs = val
    }
  },
  created() {
    if (this.value) {
      this.dataUsers = this.value.users
      this.dataStrucs = this.value.strucs
    }
  },
  methods: {
    popoverSubmit(users, strucs) {
      this.dataUsers = users
      this.dataStrucs = strucs

      this.$emit('value-change', {
        index: this.index,
        value: { users: users, strucs: strucs }
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.structure-container {
  min-height: 34px;
  margin: 3px 0;
  position: relative;
  border-radius: 3px;
  font-size: 12px;
  border: 1px solid #ddd;
  color: #333333;
  padding: 0.5px;
  line-height: 15px;
  max-height: 105px;
  overflow-y: auto;
  .user-item {
    padding: 5px;
    background-color: #e2ebf9;
    border-radius: 3px;
    margin: 3px;
    cursor: pointer;
  }
  .add-item {
    padding: 5px;
    color: #3e84e9;
    cursor: pointer;
  }
  .delete-icon {
    color: #999;
    cursor: pointer;
  }
  &:hover {
    border-color: #c0c4cc;
  }
}
</style>
