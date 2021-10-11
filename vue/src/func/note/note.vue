<template>
  <div>
    <noteBase>
      <!--笔记的标题和内容展示-->
      <div class="editTitle">
        <input type="text" v-model="title" class="editValue" placeholder="请输入标题">
      </div>
      <div>
        <textarea class="textArea" v-model="content" contenteditable="true" placeholder="请输入内容"></textarea>
      </div>
    </noteBase>
  </div>
</template>

<script>

import noteBase from "../search/noteBase";

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
    }
  },
  components: {
    noteBase
  },
  methods: {
    getNoteBookNameById(noteBookId){
      let noteBook = this.$store.state.noteBookModule.noteBooks.filter(item => item.id == noteBookId)[0]
      return noteBook.title
    },
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
      this.noteId =  note.id
      this.noteBookName = this.getNoteBookNameById(note.pid)
      this.tagList = note.tagList
    },
    // 监听标题信息  同步修改
    title(newTitle,oldValue) {
      // 初始化时触发  oldValue.length > 0 是防止在初始化阶段进行操作
      if(this.titleIdTemp === this.noteId &&  oldValue.length > 0 ){
        // 1.切换路由对象的时候 列表中的笔记数据同步更新
        this.$store.state.noteModule.currentNotes[this.$route.params.index].title = newTitle
        // 更新所有的笔记
        this.$store.state.noteModule.notes.forEach(note => {
          if (note.id == this.noteId) {
            note.title = newTitle
          }
        })
        // 更新数据库
        this.https.updateNote({id: this.noteId, title: newTitle}).then(({data}) => {console.log("修改数据库成功", data);})
      }else {
        // 更新当前id
        this.titleIdTemp = this.noteId
      }
    },
    // 监听textarea内容
    content(newTextArea,oldValue) {
      if(this.contentIdTemp === this.noteId && oldValue.length > 0){
        this.$store.state.noteModule.currentNotes[this.$route.params.index].content = newTextArea
        // 更新所有的笔记
        this.$store.state.noteModule.notes.forEach(note => {
          if (note.id == this.noteId) {
            note.content = newTextArea
          }
        })
        this.https.updateNote({id: this.noteId, content: newTextArea}).then(({data}) => {console.log("修改数据库成功", data);})
      }else {
        // 更新当前id
        this.contentIdTemp = this.noteId
      }
    },
  }
}
</script>

<style>

</style>
