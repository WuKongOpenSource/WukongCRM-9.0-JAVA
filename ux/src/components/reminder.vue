<template>
  <flexbox class="reminder-wrapper">
    <flexbox
      align="stretch"
      class="reminder-body">
      <i class="wukong wukong-reminder reminder-icon"/>
      <div
        :style="{'font-size': fontSize + 'px'}"
        class="reminder-content">
        {{ content }}
      </div>
    </flexbox>
  </flexbox>
</template>

<script type="text/javascript">
// 警示信息

export default {
  name: 'Reminder',
  components: {},
  props: {
    content: {
      type: String,
      default: '内容'
    },
    fontSize: {
      type: String,
      default: '13'
    }
  },
  data() {
    return {}
  },
  computed: {},
  mounted() {},
  destroyed() {},
  methods: {}
}
</script>
<style lang="scss" scoped>
.reminder-wrapper {
  .reminder-body {
    width: auto;
    padding: 8px 15px;
    border-radius: 4px;
    background-color: #fff4e7;
    .reminder-icon {
      color: #ff7922;
      flex-shrink: 0;
      margin-right: 8px;
    }

    .reminder-content {
      color: #9c9c9b;
      line-height: 17px;
    }
  }
}
</style>
