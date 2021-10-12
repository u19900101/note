<template>

  <!-- 笔记列表区域  -->
  <div>
    <noteListBase>
      <!-- 1.笔记本信息-->
      <noteBookInfo slot="noteBookInfo">
        <span slot="noteBookTagName">
          {{ currentNoteBookName }}
        </span>
      </noteBookInfo>

      <span slot="noteCount">{{$store.state.noteModule.currentNoteList.length}}</span>
      <!--2.笔记列表-->
      <div slot="noteList" class="n-conts"
           v-for="(item,itemIndex) in $store.state.noteModule.currentNoteList"
           :key="item.id"
           @click="listItemClick(item,itemIndex)"
           :class="index == itemIndex ? 'sel' : ''">

        <h2 class="n-title">{{ item.title }}</h2>
        <div class="n-times" v-show="item.createTimeAlias">{{ item.createTimeAlias }}</div>
        <div class="n-times" v-show="!item.createTimeAlias">{{ item.createTime }}</div>
        <div class="n-wrap" v-show="$store.state.showTextState">
          {{ item.content }}
        </div>

      </div>
    </noteListBase>
    <!--3.渲染 item 列表-->
    <router-view name="note1"/>
  </div>
</template>

<script>

import noteListBase from "../note/noteListBase";
import noteBookInfo from "./noteBookInfo";

export default {
  name: "noteList",
  components: {
    noteListBase,
    noteBookInfo,
  },
  data() {
    return {
      index:0,  // 当前被选择的id，用于标记当前选中
      currentNoteBookName: '所有笔记',
    }
  },
  created() {
  },
  methods: {
    listItemClick(currentNote, index) {
      this.index = index
      this.$router.push({
        name: 'note1', params: {
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
      console.log("noteList 中打印路由发生了变化");
    },
  }
}
</script>

<style scoped>

</style>
