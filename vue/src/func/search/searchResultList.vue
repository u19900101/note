<template>
  <!-- 笔记列表区域  -->
  <div style="margin-top: 100px">
    <noteListBase v-show="$store.state.noteModule.searchNotesList.length > 0">
      <span slot="noteCount">{{ $store.state.noteModule.searchNotesList.length }}</span>
      <!--搜索模式的显示-->
      <div slot="noteList" class="n-conts"
           v-for="(item,index) in $store.state.noteModule.searchNotesList"
           :key="item.id"
           @click="listItemClick(item,index)"
           :class="currentNoteId == index ? 'sel' : ''"
          >
        <div>
          <h2 class="n-title" v-html="item.title"></h2>
          <div class="n-times" v-show="item.createTimeAlias" v-html="item.createTimeAlias"></div>
          <div class="n-times" v-show="!item.createTimeAlias" v-html="item.createTime"></div>
          <div class="n-wrap" v-show="$store.state.showTextState" v-html="item.content"></div>
        </div>
      </div>
    </noteListBase>
    <!--3.渲染 item 列表-->
    <router-view name="searchResultItem"/>
  </div>
</template>

<script>
import noteListBase from "../note/noteListBase";

export default {
  name: "searchResultList",
  components: {
    noteListBase
  },
  data() {
    return {
      // 笔记选中的状态
      currentNoteId: 0,
    }
  },
  created() {
    // 4.开始渲染页面  调用子页面的方法
    console.log("searchList created");
  },
  methods: {

    listItemClick(currentNote, index) {
      this.currentNoteId = index
      this.$router.push({
        name: 'searchResultItem', params: {
          note: JSON.stringify(currentNote),
          index: index
        }
      })
    },
    // 选项列表功能
    sortClick() {
      this.$store.commit('isSortShow')
    },
  },
  watch: {
    $route() {
      console.log("search list 中打印路由发生了变化");
    },
  }
}
</script>

<style scoped>

</style>
