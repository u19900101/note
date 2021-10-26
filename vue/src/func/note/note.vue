<template>
  <div>

    <noteBase>
      <!--显示和变更笔记本-->
      <div slot="notebook">
        <noteBook>
          <span slot="noteBookName"> {{ noteBookName }}</span>
          <div slot="moveNoteBook"
               class="mynotesbook"
               v-for="(item,index) in $store.state.noteBookModule.noteBooks"
               :key="index"
               :class="item.title == noteBookName ? 'active' : ''"
               @click="moveByNotes(item.id,item.title)"
               @mousedown.prevent>
            {{ item.title }}
          </div>
        </noteBook>
      </div>

      <div slot="deleteNote">
        <deleteNote>
          <div slot="deleteTitle">{{ $store.state.noteModule.currentNote.title }}</div>
          <span slot="exeDelete" class="isdel GJDCG5COCC" @click="exeDelete">删除</span>
        </deleteNote>
      </div>

      <!--笔记的标题和内容展示-->
      <div slot="titleAndContent">
        <!--<div v-if="!$store.state.noteModule.isSearchNoteShow">-->

        <div v-if="$store.state.noteModule.isTitleEditMode" class="editTitle">
          <input type="text" v-model="$store.state.noteModule.currentNote.title" class="editValue"
                 placeholder="请输入标题" v-focus>
        </div>
        <div v-else>
          <div @click="titleClick" v-html="$store.state.noteModule.currentNote.title" class="editValue"></div>
        </div>

        <div v-if="$store.state.noteModule.isContentEditMode">
          <textarea class="textArea" v-model="$store.state.noteModule.currentNote.content" contenteditable="true"
                    placeholder="请输入内容" v-focus></textarea>
        </div>
        <div v-else>
          <div @click="contentClick" class="textArea" v-html="$store.state.noteModule.currentNote.content"></div>
        </div>

      </div>
    </noteBase>
  </div>
</template>

<script>
import {mapState} from 'vuex'

import noteBase from "../search/noteBase";
import deleteNote from "./deleteNote";
import noteBook from "./noteBook";

export default {
  name: "note",
  data() {
    return {
      isTitleEditMode: false,  // 当前的笔记的标题是否处于编辑状态
      isContentEditMode: false,  // 当前的笔记的内容是否处于编辑状态
    }
  },
  components: {
    noteBase,
    deleteNote,
    noteBook,
  },
  methods: {
    titleClick() {
      this.$store.state.noteModule.currentNote.title = this.$store.state.noteModule.currentNote.title.replace(/<font style="background:yellow" color="red">/gi, "").replace(/<\/font>/gi, "")
      this.$store.state.noteModule.isTitleEditMode = true
    },

    contentClick() {
      this.$store.state.noteModule.currentNote.content = this.$store.state.noteModule.currentNote.content.replace(/<font style="background:yellow" color="red">/gi, "").replace(/<\/font>/gi, "")
      this.$store.state.noteModule.isContentEditMode = true
    },

    // 点击搜索结果时 切换到编辑模式，将搜索结果替换掉
    toEditMode() {
      this.$store.state.noteModule.isSearchNoteShow = false
    },
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
      this.$store.state.noteModule.currentNoteList.splice(this.index, this.index + 1)
      this.$store.state.noteModule.currentNote = this.$store.state.noteModule.currentNoteList[0]

      // 2.更新当前笔记
      // this.$router.push({
      //   name: 'note1', params: {
      //     index: 0
      //   }
      // })

      // 通过call()来调用vue插件方法
      // this.message.message.call(this);

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
          // 无需移除笔记
          // 渲染页面
          this.noteBookName = noteBookName
          console.log("移动笔记成功", data);
        })
      }
    },
  },
  computed: {
    ...mapState('noteModule', {
      // 对于不修改的数据 使用这种方式能简写 比较方便
      noteId: state => state.currentNote.id,
      pid: state => state.currentNote.pid,
    }),
    noteBookName: {
      get: function () {
        let name = this.getNoteBookNameById(this.$store.state.noteModule.currentNote.pid)
          || this.$store.state.noteBookModule.noteBooks[0].title
        return name
      },
      // 可以啥也不写  要是直接写在data中 页面就不是响应式
      set: function (newValue) {
        console.log(newValue);
      }
    },
  },
  created() {
    this.titleIdTemp = this.contentIdTemp = this.noteId
  }
  ,
  watch: {
    // 监听标题信息  同步修改
    '$store.state.noteModule.currentNote.title'(newTitle, oldValue) {
      // console.log('输入值变化时，直接修改了绑定值', this.$store.state.noteModule.currentNote.title == newTitle)
      // 初始化时触发  oldValue.length > 0 是防止在初始化阶段进行操作
      // 搜索模式转换引起的变化
      if (oldValue.includes('<font style="background:yellow" color="red">')) {
        return
      }
      if (this.titleIdTemp === this.noteId && oldValue.length > 0) {
        // 1.更新数据库
        this.https.updateNote({id: this.noteId, title: newTitle}).then(({data}) => {
          console.log("修改数据库成功", data);
        })
        // 2.搜索状态时 更新列表中的笔记数据同步更新
        if (this.$store.state.noteModule.isSearchNoteListShow) {
          // 更新所有的笔记
          this.$store.state.noteModule.notes.forEach(note => {
            if (note.id == this.noteId) {
              note.title = newTitle
            }
          })
        }
      } else {
        // 更新当前id
        this.titleIdTemp = this.noteId
      }

    }
    ,
    // 监听textarea内容
    '$store.state.noteModule.currentNote.content'(newTextArea, oldValue) {
      // 搜索模式转换引起的变化 则不反应
      if (oldValue.includes('<font style="background:yellow" color="red">')) {
        return
      }
      if (this.contentIdTemp === this.noteId && oldValue.length > 0) {
        this.https.updateNote({id: this.noteId, content: newTextArea}).then(({data}) => {
          console.log("修改数据库成功", data);
        })

        if (this.$store.state.noteModule.isSearchNoteListShow) {
          this.$store.state.noteModule.notes.forEach(note => {
            if (note.id == this.noteId) {
              note.content = newTextArea
            }
          })
        }

      } else {
        // 更新当前id
        this.contentIdTemp = this.noteId
      }

    },

  },
  // 鼠标自动聚焦
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

</style>
