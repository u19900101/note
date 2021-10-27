<template>
  <div>
    <!-- 1.笔记本列表区  -->
    <div v-if="isNoteBooksShow" >
      <div class="bijibenInfo">
        <img src="@/assets/images/huachuangbijiben.png" alt="" class="xinjian" title="创建笔记本" @click="createHander">
        <div class="message">
          <input type="text" class="messageValue" placeholder="查找笔记本">
        </div>
      </div>
      <!--笔记本列表展示-->
      <div class="bijibenList">
        <div class="notList">
          暂无笔记本信息...
        </div>
        <div class="liebiao"
             v-for="(item,noteBooksIndex) in $store.state.noteBookModule.noteBooks.slice(1)"
             :key="item.id" @click="enterNoteBook(item,item.title)"
             :style="{backgroundColor:$store.state.noteBookBg === item.id ? 'rgb(236,236,236)' : ''}"
             @mouseover="state=noteBooksIndex"
             @mouseout="state=-1">
          <div class="list_main">
            <div class="bijixinxi">
              <div class="noteTitle">
                {{ item.title }}
              </div>
              <p class="number">{{ item.noteCount }} 条笔记</p>
              <div class="delnotes" title="删除笔记本" @click.stop="deleteNoteBook(item)" v-show="state===index"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else>
      <!-- 2.笔记列表 todo 页面的布局-->
      <noteListBase >
        <!--       1.笔记本信息-->
        <noteBookInfo slot="noteBookInfo"><span slot="noteBookTagName">
          {{ $store.state.noteBookModule.currentNoteBook.title }}</span></noteBookInfo>
        <!--       当前笔记的数量-->
        <span slot="noteCount">{{ $store.state.noteBookModule.currentNoteBookNoteList.length }}</span>
        <!--      2.编辑模式的显示-->
        <div slot="noteList" class="n-conts"
             v-for="(item,itemIndex) in $store.state.noteBookModule.currentNoteBookNoteList"
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
      <!--     3.渲染 item 列表-->
      <router-view name="noteBookItem"/>
    </div>

  </div>

</template>

<script>
import noteListBase from "./noteListBase";
import noteBookInfo from "./noteBookInfo";
import note from "./note";
import noteBook from "./noteBook";

export default {
  name: "noteBookList",
  data() {
    return {
      index: 0,
      state: -1,
      isNoteBooksShow:true,
    }
  },
  components: {
    noteListBase,
    noteBookInfo,
    note,

  },
  methods: {
    //删除笔记本 提交vuex
    deleteNoteBook(obj) {
      if (obj.id === 'f7') return;
      this.$store.commit('deleteNoteBook', {
        obj: obj,
      })
    },
    // 进入详细的笔记本信息
    enterNoteBook(currentNoteBook, currentNoteBookName) {
      // 判断当前笔记本是否有笔记
      let currentNoteList = this.$store.state.noteModule.notes.filter(item => item.pid == currentNoteBook.id);
      if(currentNoteList.length > 0){
        this.isNoteBooksShow = false
        this.$store.state.noteBookModule.currentNoteBookNoteList = currentNoteList
        this.$store.state.noteModule.currentNoteList = this.$store.state.noteBookModule.currentNoteBookNoteList; // 进入的笔记本列表数据
        this.$store.state.noteModule.currentNote = this.$store.state.noteBookModule.currentNoteBookNoteList[0]; // 进入的笔记本列表数据
        this.$store.state.noteBookModule.currentNoteBook = currentNoteBook
        this.$router.push({name: 'noteList'})
        this.$router.push({name: 'note1'})
      }else {
        alert('当前笔记本暂无笔记...')
      }
    },
    listItemClick(currentNote, index) {
      this.index = index
      this.$router.push({
        name: 'noteBookItem', params: {
          note: JSON.stringify(currentNote),
          index: index
        }
      })
    },
    //创建笔记本
    createHander() {
      this.$store.commit('createHanderShow')
    },


  },

  created() {
    console.log("noteBookList created..");
  },
  watch:{
    $route() {
      console.log("jjj");
    },
  }


}
</script>

<style scoped>


.liebiao .list_main:hover {
  background: rgb(64, 188, 108);
}

.liebiao .list_main:hover .noteTitle {
  color: #ffffff;
}

.liebiao .list_main:hover .number {
  color: #c9f2d0;
}
</style>
