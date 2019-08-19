export default {
  components: {},
  data() {
    return {
      // 详情数据
      taskID: '',
      detailIndex: -1,
      taskDetailShow: false
    }
  },

  mounted() {},

  methods: {
    // 关闭详情页
    closeBtn() {
      this.taskDetailShow = false
    },
    // 点击显示详情
    showDetailView(val, index, result) {
      this.taskID = val.taskId
      this.detailIndex = index
      this.taskDetailShow = true
      if (result) {
        result()
      }
    },
    detailHandle(data) {
      if (data.index == 0 || data.index) {
        // 是否完成勾选
        if (data.type == 'title-check') {
          this.$set(this.list[data.index], 'checked', data.value)
        } else if (data.type == 'delete') {
          this.list.splice(data.index, 1)
        } else if (data.type == 'change-stop-time') {
          let stopTime = new Date(data.value).getTime() / 1000 + 86399
          if (stopTime > new Date(new Date()).getTime() / 1000) {
            this.list[data.index].isEnd = false
          } else {
            this.list[data.index].isEnd = true
          }
          this.list[data.index].stopTime = data.value
        } else if (data.type == 'change-priority') {
          this.list[data.index].priority = data.value.id
        } else if (data.type == 'change-name') {
          this.list[data.index].name = data.value
        } else if (data.type == 'change-comments') {
          let commentCount = this.list[data.index].commentCount
          if (data.value == 'add') {
            this.list[data.index].commentCount = commentCount + 1
          } else {
            this.list[data.index].commentCount = commentCount - 1
          }
        } else if (data.type == 'change-sub-task') {
          this.list[data.index].childWCCount = data.value.subdonecount
          this.list[data.index].childAllCount = data.value.allcount
        }
      }
    },
  },

  deactivated: function () {}

}
