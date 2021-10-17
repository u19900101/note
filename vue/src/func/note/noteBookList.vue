<template>
  <div>
    <!-- 1.笔记本列表区  -->
    <div class="yinxList">
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
             v-for="(item,noteBooksIndex) in $store.state.noteBookModule.noteBooks"
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

    <!-- 2.笔记列表 todo 页面的布局-->
    <div style=" width: 100px;">
      <noteListBase>
        <!-- 1.笔记本信息-->
        <noteBookInfo slot="noteBookInfo"><span slot="noteBookTagName">
          {{ currentNoteBookName }}</span></noteBookInfo>
        <!-- 当前笔记的数量-->
        <span slot="noteCount">{{ $store.state.noteBookModule.currentNoteBookNoteList.length }}</span>
        <!--2.编辑模式的显示-->
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
    </div>

    <!-- 3.渲染 item 列表-->
    <router-view name="noteBookItem"/>



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
      currentNoteBookName: this.$store.state.noteBookModule.noteBooks[0].title,
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
      let currentNoteBookNoteList = this.$store.state.noteModule.notes.filter(item => item.pid == currentNoteBook.id);
      this.currentNoteBookName = currentNoteBookName
      this.$store.state.noteBookModule.currentNoteBookNoteList = currentNoteBookNoteList
      // 切换笔记本时 渲染右侧显示
      this.$router.push({
        name: 'noteBookItem', params: {
          note: JSON.stringify(this.$store.state.noteBookModule.currentNoteBookNoteList[0]),
          index: 0
        }
      })
      /*this.$router.push({
        name: 'noteListInNoteBook',
        params: {
          notes: JSON.stringify(currentNoteBookNoteList),
          noteBookTagName: currentNoteBook.title
        }
      })*/
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
    this.$router.push({
      name: 'noteBookItem', params: {
        note: JSON.stringify(this.$store.state.noteBookModule.currentNoteBookNoteList[0]),
        index: 0
      }
    })
  }

}
</script>

<style scoped>


.bijibenHDC {
  left: 0px;
}

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
