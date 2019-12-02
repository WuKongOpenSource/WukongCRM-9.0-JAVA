<template>
  <div
    v-show="visible"
    class="photo-wrap">
    <flexbox
      :style="{'font-size' : fontSize+'px'}"
      class="photo-content"
      justify="center"
      align="center">
      <div v-if="text">{{ text }}</div>
    </flexbox>
  </div>
</template>

<script>
export default {
  data() {
    return {
      text: null,
      visible: false,
      fontSize: 12
    }
  },
  computed: {},
  watch: {
    visible: function(params) {
      this.$nextTick(() => {
        if (this.$el.getBoundingClientRect().height > 0) {
          let fontSize = this.$el.getBoundingClientRect().height * 0.38
          if (fontSize > 40) {
            fontSize = 40
          }
          this.fontSize = fontSize
        } else {
          this.fontSize = 12
        }
      })
    }
  },
  mounted() {},
  methods: {
    setText(text) {
      if (text) {
        this.text = text
      }
    },
    setIcon(icon) {
      this.icon = icon
    }
  }
}
</script>

<style lang="scss" scoped>
.photo-wrap {
  position: absolute;
  z-index: 1;
  // background-color: white;
  margin: 0;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  .photo-content {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background-color: #2486e4;
    color: white;
    div {
      width: 100%;
      text-align: center;
      transform: scale(0.9, 0.9);
      max-height: 50px;
      overflow: hidden;
    }
  }
}
</style>

