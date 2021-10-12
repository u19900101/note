<template>
  <div>
    <!--1.1显示笔记本和移动笔记-->
    <div class="notecont" title="移动笔记" @click.stop="clickMove" @mousedown.prevent>
      <slot name="noteBookName"></slot>
    </div>
    <!--1.2.笔记本列表 可查找  todo 点击别处时不自动隐藏-->
    <div class="yidongBJB" v-show="moveNote" @click.stop>
      <div class="findnotes">
        <input type="text" class="findValue" placeholder="查找笔记本" v-model="findNotes" ref="findval">
      </div>
      <!--新建笔记本-->
      <div class="chuanjian" @mousedown.prevent @click="createNoteBook">
        <div class="chuangjianIco"></div>
        <span @mousedown.prevent>新建笔记本</span>
      </div>
      <div @click = "moveNote = false">
        <slot name="moveNoteBook"></slot>
      </div>

    </div>
  </div>
</template>

<script>
export default {
  name: "noteBook",
  data() {
    return{
      findNotes:'', //查找笔记本
      moveNote: false
    }
  },

  methods:{
    // 点击 移动笔记
    clickMove() {
      this.moveNote = !this.moveNote;
      let bl = this.$refs.findval; // 点击笔记本后 让光标到 笔记本地搜素框
      this.$nextTick(function () {
        bl.focus()
      })
    },
    // 新建笔记
    createNoteBook() {
      this.$store.commit('createHanderShow');
      this.moveNote = false;
    },
  }
}
</script>

<style scoped>

</style>
