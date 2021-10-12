<template>

  <div class="noteNames" ref="noteInfoNamesScroll">
    <img src="@/assets/images/dijijieduanbijibenxinxi.png" alt="" class="noteinfoPic">
    <div class="title">
      <slot name="noteBookTagName"></slot>
    </div>
  </div>

</template>

<script>
export default {
  name: "noteBookInfo"
}
</script>

<style scoped>

</style>
