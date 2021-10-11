<template>
  <div>
    <noteBase>
      <div slot="deleteNote">
        <deleteNote>
          <div slot="deleteTitle">{{ title }}</div>
          <span  slot="exeDelete" class="isdel GJDCG5COCC" @click="exeDelete">删除</span>
        </deleteNote>
      </div>

      <!--笔记的标题和内容展示-->
      <div slot="titleAndContent">
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

export default {
  name: "note",
  data() {
    return {
      title: JSON.parse(this.$route.params.note).title,
      content: JSON.parse(this.$route.params.note).content,
      noteId: JSON.parse(this.$route.params.note).id,
      titleIdTemp: JSON.parse(this.$route.params.note).id,
      contentIdTemp: JSON.parse(this.$route.params.note).id,
      noteBookName: '',
      index: this.$route.params.index,
    }
  },
  components: {
    noteBase,
    deleteNote
  },
  methods: {
    getNoteBookNameById(noteBookId) {
      let noteBook = this.$store.state.noteBookModule.noteBooks.filter(item => item.id == noteBookId)[0]
      return noteBook.title
    },

    //确定删除
    exeDelete() {
      // 2.1 数据库
      // # 	    2.1.1 将 数据库中该note的 status 字段改为 1 表示逻辑删除
      // # 	    2.1.2 修改该笔记所包含的所有tag数量都 -1
      //        2.1.3 修改该笔记所在笔记本数量 -1
      // #  	2.2 页面变化
      // #		2.2.1 设置成功后 将该 note从currentNotes列表和note列表中移除
      // #		2.2.2 noteBook中也删除相应的笔记
      //      2.2.3 当前的note页面显示 栈顶笔记

      this.https.deleteNote({id: this.$store.state.noteModule.currentNoteList[this.index].id}).then(({data}) => {
        this.isDeleteShow = false
        console.log("逻辑删除成功  ", data);
      })

      // 1.更新笔记本列表
      this.$store.state.noteModule.currentNoteList.splice(this.index,this.index+1)
      // 2.更新当前笔记
      this.$router.push({
        name: 'note1', params: {
          note: JSON.stringify(this.$store.state.noteModule.currentNoteList[0])
          , index: 0
        }
      })

      // 通过call()来调用vue插件方法
      // this.message.message.call(this);

    }
  },

  created() {
    console.log("note created");
    let m = JSON.parse(this.$route.params.note)
    m = JSON.parse(this.$route.params.note).title
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
    },
    // 监听标题信息  同步修改
    title(newTitle, oldValue) {
      // 初始化时触发  oldValue.length > 0 是防止在初始化阶段进行操作
      if (this.titleIdTemp === this.noteId && oldValue.length > 0) {
        // 1.切换路由对象的时候 列表中的笔记数据同步更新
        this.$store.state.noteModule.currentNoteList[this.$route.params.index].title = newTitle
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
        this.$store.state.noteModule.currentNoteList[this.$route.params.index].content = newTextArea
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
