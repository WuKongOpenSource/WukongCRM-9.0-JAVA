export default {
  data() {
    return {}
  },
  props: {
    attr: {
      required: true
    },
    isShow: {
      type: Boolean,
      default: true
    }
  },
  filters: {},
  methods: {
    handleDelete() {
      this.$emit('delete')
    }
  }
}
