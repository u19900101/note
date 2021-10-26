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


      <span slot="noteCount">{{ $store.state.noteModule.currentNoteList.length }}</span>
      <!--2.笔记列表-->
      <div slot="noteList">
            <div class="n-conts"
                 v-for="(item,itemIndex) in $store.state.noteModule.currentNoteList"
                 :key="item.id"
                 @click="listItemClick(item,itemIndex)"
                 :class="index == itemIndex ? 'sel' : ''">
              <div v-if="$store.state.noteModule.isSearchNoteListShow">
                  <h2 class="n-title" v-html="item.title"></h2>
                  <div class="n-times" v-show="item.createTimeAlias" v-html="item.createTimeAlias"></div>
                  <div class="n-times" v-show="!item.createTimeAlias" v-html="item.createTime"></div>
                  <div class="n-wrap" v-html="item.content"></div>
              </div>
              <div v-else>
                <h2 class="n-title">{{ item.title }}</h2>
                <div class="n-times" v-show="item.createTimeAlias">{{ item.createTimeAlias }}</div>
                <div class="n-times" v-show="!item.createTimeAlias">{{ item.createTime }}</div>
                <div class="n-wrap" v-show="$store.state.showTextState">
                  {{ item.content }}
                </div>
              </div>

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
      index: 0,  // 当前被选择的id，用于标记当前选中
      currentNoteBookName: '所有笔记',
    }
  },
  created() {
  },
  methods: {
    listItemClick(currentNote, index) {
      // 若修改 currentNote 则会自动更新到 currentNoteList上
      this.$store.state.noteModule.currentNote = currentNote
      this.$store.state.noteModule.currentIndex = index

      // 在搜索列表下，跳转之前 对显示进行重置,展示搜索结果
      if(this.$store.state.noteModule.isSearchNoteListShow){
        this.$store.state.noteModule.isTitleEditMode = false
        this.$store.state.noteModule.isContentEditMode = false
      }

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
div.item {
  width: 100px;
  height: 100px;
  border: 1px solid #bfa;
  line-height: 100px;
  text-align: center;
  font-size: 50px;
}
</style>
