<template>
  <div>
    <noteBase v-show="isResultItemShow">

      <noteBook slot="notebook">
        <span slot="noteBookName">{{ noteBookName }}</span>
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


      <deleteNote slot="deleteNote">
        <div slot="deleteTitle">{{ title }}</div>
        <span slot="exeDelete" class="isdel GJDCG5COCC" @click="exeDelete">删除</span>
      </deleteNote>


      <div slot="titleAndContent">
        <div v-if="isTitleEditMode" class="editTitle">
          <input type="text" v-model="title" class="editValue" placeholder="请输入标题" v-focus>
        </div>
        <div v-else class="editTitle" @click="isTitleEditMode = true">
          <div type="text" id="searchTitleResult" v-html="searchTitle" class="editValue"></div>
        </div>

        <div v-if="isContentEditMode">
          <textarea class="textArea" v-model="content" contenteditable="true" placeholder="请输入内容" v-focus></textarea>
        </div>
        <div v-else>
          <div class="textArea" v-html="searchContent" @click="isContentEditMode = true"></div>
        </div>
      </div>
    </noteBase>
  </div>
</template>

<script>

import noteBase from "./noteBase";
import deleteNote from "../note/deleteNote";
import noteBook from "../note/noteBook";
export default {
  name: "searchResultItem",
  data() {
    return {
      title: JSON.parse(this.$route.params.note).title.replace(/<font style="background:yellow" color="red">/gi, "").replace(/<\/font>/gi, ""),
      content: JSON.parse(this.$route.params.note).content.replace(/<font style="background:yellow" color="red">/gi, "").replace(/<\/font>/gi, ""),
      searchTitle: JSON.parse(this.$route.params.note).title,
      searchContent: JSON.parse(this.$route.params.note).content,
      noteId: JSON.parse(this.$route.params.note).id,
      titleIdTemp: JSON.parse(this.$route.params.note).id,
      contentIdTemp: JSON.parse(this.$route.params.note).id,
      noteBookName: this.getNoteBookNameById(JSON.parse(this.$route.params.note).pid),
      isTitleEditMode: false,  // 当前的笔记的标题是否处于编辑状态
      isContentEditMode: false,  // 当前的笔记的内容是否处于编辑状态
      isResultItemShow: true,   // 控制搜索结果为空时，页面不能点击
      index: this.$route.params.index,
      pid: JSON.parse(this.$route.params.note).pid,
    }
  },
  components: {
    noteBase,
    deleteNote,
    noteBook,
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
          this.$store.state.noteModule.searchNotesList.splice(this.index,this.index+1)
          // 渲染 当前笔记内容
          let currentNote = ''
          if(this.$store.state.noteModule.searchNotesList.length > 0){
            currentNote = JSON.stringify(this.$store.state.noteModule.searchNotesList[0])
          }
          this.$router.push({
            name: 'searchResultItem', params: {
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

      this.https.deleteNote({id: this.$store.state.noteModule.searchNotesList[this.index].id}).then(({data}) => {
        this.isDeleteShow = false
        console.log("逻辑删除成功  ", data);
      })

      // 1.更新笔记本列表
      this.$store.state.noteModule.searchNotesList.splice(this.index, this.index + 1)
      // 2.更新当前笔记   若列表中还有笔记 就显示栈顶笔记  若无 就清空当前的显示
      let currentNote = this.$store.state.noteModule.searchNotesList.length > 0 ?
        JSON.stringify(this.$store.state.noteModule.searchNotesList[0]) : ''

      this.$router.push({
        name: 'searchResultItem', params: {
          note: currentNote
          , index: 0
        }
      })
      //  3.更新 currentNoteList 和 notes
      // 更新所有的笔记
      this.$store.state.noteModule.notes = this.$store.state.noteModule.notes.filter(note => note.id != this.noteId)
      this.$store.state.noteModule.currentNoteList = this.$store.state.noteModule.notes

    }
  },
  computed: {
    //搜索笔记
    filterNoteBooks() {
      if (this.findNotes == "" || this.findNotes.length == 0) {
        return []
      } else {
        return this.noteBooks.filter(item => {
          return item.title.trim().match(this.findNotes)
        })
      }

    },
    //tag输入框的动态宽度计算
    tagWidth() {
      return {
        width: this.tagVal.length * 12 + 26 + 'px'
      }
    },
  },
  created() {
    console.log("note created");
  },
  watch: {
    // 侦听路由对象变化
    $route() {
      console.log('searchResultItem  route changed');
      if (this.$route.params.note.length == 0) {
        this.searchTitle = ''
        this.searchContent = ''
        this.isResultItemShow = false
      } else {
        this.isResultItemShow = true
        let note = JSON.parse(this.$route.params.note)

        this.title = note.title.replace(/<font style="background:yellow" color="red">/gi, "").replace(/<\/font>/gi, "")
        this.content = note.content.replace(/<font style="background:yellow" color="red">/gi, "").replace(/<\/font>/gi, "")

        this.searchTitle = JSON.parse(this.$route.params.note).title
        this.searchContent = JSON.parse(this.$route.params.note).content
        this.noteId = note.id
        this.noteBookName = this.getNoteBookNameById(note.pid)
        this.tagList = note.tagList
        this.pid = note.pid
      }
      this.index = this.$route.params.index
      // 初始化显示结果为search，带高亮的状态
      this.isTitleEditMode = false
      this.isContentEditMode = false
    },
    // // 监听标题信息  同步修改
    title(newTitle, oldValue) {
      // 初始化时触发  oldValue.length > 0 是防止在初始化阶段进行操作
      if (this.titleIdTemp === this.noteId && oldValue.length > 0) {
        // 1.切换路由对象的时候 列表中的笔记数据同步更新  使用index 替换 foreach遍历
        this.$store.state.noteModule.searchNotesList[this.$route.params.index].title = newTitle
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
        this.$store.state.noteModule.searchNotesList[this.$route.params.index].content = newTextArea
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

  },
  directives: {
    focus: {
      inserted: function (el) {
        el.focus();
      }
    }
  }
}


</script>

<style>

.notWidth {
  margin-left: 0px;
}

.noteList {
  background: rgb(248, 248, 248);
  position: absolute;
  left: 0;
  top: 0;
  z-index: 800;
  width: 100%;
  height: 100%;
}

.bj-n {
  width: 24px;
  height: 24px;
  background: url("../../assets/images/defbijixinxipng.png") no-repeat;
}

.bj-n:hover {
  background: url("../../assets/images/bijixinxihover.png") no-repeat;
}

.upgrade .g-message {
  left: -290px;
  top: 38px;
}
</style>
