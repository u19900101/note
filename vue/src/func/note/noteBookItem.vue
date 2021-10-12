<template>
  <div>
    <noteBase>
      <div slot="notebook">
        <noteBook>
          <span slot="noteBookName">{{noteBookName}}</span>
          <div slot="moveNoteBook"
               class="mynotesbook"
               v-for="(item,index) in $store.state.noteBookModule.noteBooks"
               :key="index"
               :class="item.title == noteBookName ? 'active' : ''"
               @click="moveByNotes(item.id,item.title)"
               @mousedown.prevent
                >
            {{ item.title }}
          </div>
        </noteBook>
      </div>
      <div slot="deleteNote">
        <deleteNote>
          <div slot="deleteTitle">{{ title }}</div>
          <span  slot="exeDelete" class="isdel GJDCG5COCC" @click="exeDelete">删除</span>
        </deleteNote>
      </div>

      <!--笔记的标题和内容展示-->
      <div slot="titleAndContent" >
        <div class="editTitle">
          <input type="text" v-model="title" class="editValue" placeholder="请输入标题">
        </div>
        <div>
          <textarea class="textArea" v-model="content" contenteditable="true" placeholder="请输入内容"></textarea>
        </div>
      </div>

    </noteBase>
  </div>
</template>

<script>

import noteBase from "../search/noteBase";
import deleteNote from "./deleteNote";
import noteBook from "./noteBook";

export default {
  name: "noteBookItem",
  data() {
    return {
      title: JSON.parse(this.$route.params.note).title,
      content: JSON.parse(this.$route.params.note).content,
      noteId: JSON.parse(this.$route.params.note).id,
      titleIdTemp: JSON.parse(this.$route.params.note).id,
      contentIdTemp: JSON.parse(this.$route.params.note).id,
      noteBookName: this.$store.state.noteBookModule.noteBooks[0].title,
      index: this.$route.params.index,
      pid: JSON.parse(this.$route.params.note).pid
    }
  },
  components: {
    noteBase,
    deleteNote,
    noteBook
  },
  methods: {
    getNoteBookNameById(noteBookId) {
      let noteBook = this.$store.state.noteBookModule.noteBooks.filter(item => item.id == noteBookId)[0]
      return noteBook.title
    },
    // 开始移动 移动到哪个阶段笔记本的id----------
    moveByNotes(noteBookId, noteBookName) {
      // 判断目标笔记本是否为当前笔记本  不为原笔记本时 进行更新笔记本操作
      if (noteBookName != this.noteBookName) {
        // 1.修改本笔记的pid
        let noteToUpdate = {
          id: this.noteId,
          pid: noteBookId
        }
        this.https.updateNote(noteToUpdate).then(({data}) => {
          this.moveNote = false; //关闭移动下拉框
          // @ 移动提醒
          // this.message.message.call(this);
          //  2.修改笔记本中笔记 数量
          //  2.1 新的笔记本 数量 +1  2.2 旧的笔记本 数量 -1
          this.$store.state.noteBookModule.noteBooks.filter(item => item.id == noteBookId)[0].noteCount += 1
          this.$store.state.noteBookModule.noteBooks.filter((item) => item.id == this.pid)[0].noteCount -= 1

          // 渲染列表
          // 1.3 修改 notes 中受影响的笔记 pid 所有的笔记列表
          this.$store.state.noteModule.notes.filter((item) => item.id == this.noteId)[0].pid = noteBookId
          // 1.4 修改 currentNotes  将当前笔从笔记本列表中移除
          this.$store.state.noteBookModule.currentNoteBookNoteList.splice(this.index,this.index+1)
          // 渲染 当前笔记内容
          let currentNote = ''
          if(this.$store.state.noteBookModule.currentNoteBookNoteList.length > 0){
            currentNote = JSON.stringify(this.$store.state.noteBookModule.currentNoteBookNoteList[0])
          }
          this.$router.push({
            name: 'noteBookItem', params: {
              note: currentNote,
              index: 0
            }
          })
          console.log("移动笔记成功", data);
        })
      }
    },


    //确定删除
    exeDelete() {

      this.https.deleteNote({id: this.$store.state.noteModule.currentNoteBookNoteList[this.index].id}).then(({data}) => {
        this.isDeleteShow = false
        console.log("逻辑删除成功  ", data);
      })

      // 1.更新笔记本列表
      this.$store.state.noteModule.currentNoteBookNoteList.splice(this.index,this.index+1)
      // 2.更新当前笔记
      this.$router.push({
        name: 'note1', params: {
          note: JSON.stringify(this.$store.state.noteModule.currentNoteBookNoteList[0])
          , index: 0
        }
      })
      // 通过call()来调用vue插件方法
      // this.message.message.call(this);
    }
  },

  created() {
    console.log("noteBookItem created");
  },
  watch: {
    // 侦听路由对象变化
    $route() {

      let note = JSON.parse(this.$route.params.note)
      this.title = note.title
      this.content = note.content
      this.noteId = note.id
      this.noteBookName = this.getNoteBookNameById(note.pid)
      this.tagList = note.tagList
      this.index = this.$route.params.index
      this.pid = note.pid
    },
    // 监听标题信息  同步修改
    title(newTitle, oldValue) {
      // 初始化时触发  oldValue.length > 0 是防止在初始化阶段进行操作
      if (this.titleIdTemp === this.noteId && oldValue.length > 0) {
        // 1.切换路由对象的时候 列表中的笔记数据同步更新
        this.$store.state.noteModule.currentNoteBookNoteList[this.$route.params.index].title = newTitle
        // 更新所有的笔记
        this.$store.state.noteModule.notes.forEach(note => {
          if (note.id == this.noteId) {
            note.title = newTitle
          }
        })
        // 更新数据库
        this.https.updateNote({id: this.noteId, title: newTitle}).then(({data}) => {
          console.log("修改数据库成功", data);
        })
      } else {
        // 更新当前id
        this.titleIdTemp = this.noteId
      }
    },
    // 监听textarea内容
    content(newTextArea, oldValue) {
      if (this.contentIdTemp === this.noteId && oldValue.length > 0) {
        this.$store.state.noteModule.currentNoteBookNoteList[this.$route.params.index].content = newTextArea
        // 更新所有的笔记
        this.$store.state.noteModule.notes.forEach(note => {
          if (note.id == this.noteId) {
            note.content = newTextArea
          }
        })
        this.https.updateNote({id: this.noteId, content: newTextArea}).then(({data}) => {
          console.log("修改数据库成功", data);
        })
      } else {
        // 更新当前id
        this.contentIdTemp = this.noteId
      }
    },
  }
}
</script>

<style>

</style>
