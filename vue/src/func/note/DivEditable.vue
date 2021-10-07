<template>
  <div

    contenteditable="true"
    v-html="innerText"
    @input="changeText"
    @focus="isChange = false"
    @blur="blurFunc"></div>
</template>

<script>
export default {
  name: 'DivEditable',
  props: {
    value: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      innerText: this.value,
      isChange: true
    }
  },
  watch: {
    value() {
      if (this.isChange) {
        this.innerText = this.value
      }
    }
  },
  methods: {
    changeText() {
      this.$emit('input', this.$el.innerHTML)
    },
    blurFunc() {
      this.isChange = true
      this.$emit('blurFunc')
    }
  }
}
</script>

