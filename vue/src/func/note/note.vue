<template>
  <div>
    <noteBase>
      <!--笔记的标题和内容展示-->
      <div class="editCount" ref="editScroll" @click="closeQuick">
        <div class="root" v-if="!$store.state.noteModule.isSearchNoteShow">
          <div class="editTitle">
            <input type="text" v-model="title" class="editValue" placeholder="请输入标题">
          </div>
          <div>
            <textarea class="textArea" v-model="content" contenteditable="true" placeholder="请输入内容"></textarea>
          </div>
        </div>
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
      noteContent: {}, // title 和 textarea展示内容的对象  也是Home组件消息弹窗的数据
      title: JSON.parse(this.$route.params.note).title,
      content: JSON.parse(this.$route.params.note).content,
      noteId: JSON.parse(this.$route.params.note).id,
      titleIdTemp: JSON.parse(this.$route.params.note).id,
      contentIdTemp: JSON.parse(this.$route.params.note).id,
      noteBookName: '',
      tagList: [], // 当前笔记的 标签

      //--------------------------------------------------
      searchContent: '',
      pid: '', // noteContent对象的pid

      moveNote: false, //移动笔记本显隐

      findNotes: '', //查找笔记本

      // iView数据
      tagVal: '', //绑定tag输入框数据
      count: [],  //保存标签数据
      editTagShow: false, //自定义标签输入框的显示隐藏

      kJshow: false, //快捷方式显示隐藏
      tkJshow: false, //顶部快捷方式显示隐藏
      sCshow: false,  //删除图标显示和隐藏
      delNextId: 1,  //删除对象下一个兄弟的id

      searchValue: '', //搜索关键字
      open: true,
      messageState: false, // 弹窗显隐
      remindTime: '',
      completeState: '',
      remind: false,
    }
  },
  components: {
    noteBase
  },
  methods: {
    syncListAndItem(currentNoteList,noteBookTagName,currentNote){
      this.$router.push({
        name: 'noteList',
        params: {
          notes: JSON.stringify(currentNoteList),
          noteBookTagName: noteBookTagName
        }
      })
      this.$router.push({ name: 'note1', params: { note: JSON.stringify(currentNote)}})
    },


    getNoteBookNameById(noteBookId){
      let noteBook = this.$store.state.noteBookModule.noteBooks.filter(item => item.id == noteBookId)[0]
      return noteBook.title
    },


    // 开始移动 移动到哪个阶段笔记本的id----------
    moveByNotes(noteBookId,noteBookName) {

      // 判断目标笔记本是否为当前笔记本
      // 不为原笔记本时 进行更新笔记本操作
      if(noteBookId != this.$store.state.noteModule.pid){
        // 1.修改本笔记的pid
        // 2.修改 tag相关的笔记数量
        // 刷新数据 重新请求
        let noteToUpdate = {
          id: this.$store.state.noteModule.noteId,
          pid: noteBookId
        }
        this.https.updateNote(noteToUpdate).then(({data}) => {

          this.moveNote = false; //关闭移动下拉框
          // @ 移动提醒
          // this.message.message.call(this);
          //  2.修改笔记本
          //  2.1 新的笔记本 数量 +1
          this.$store.state.noteBookModule.noteBooks.filter((item) => item.id == noteBookId)[0].noteCount += 1
          this.$store.state.noteBookModule.noteBooks.filter((item) => item.id == this.$store.state.noteModule.pid)[0].noteCount -= 1
          // .push(currentNote)
          //  2.2 旧的笔记本 数量 -1
          // this.$store.state.noteBookModule.noteBooks.filter((item) => item.id == this.$store.state.noteModule.pid)[0].pop(currentNote)

          // 1.修改当前的笔记
          //  1.1修改 pid
          this.$store.state.noteModule.pid = noteBookId
          //  1.2.修改 currentNoteBookName
          this.$store.state.noteModule.currentNoteBookName = noteBookName
          console.log("移动笔记成功",data);
          // 1.3 修改 notes 中受影响的笔记 pid 所有的笔记列表
          let noteId = this.$store.state.noteModule.noteId
          this.$store.state.noteModule.notes.filter((item) => item.id == noteId)[0].pid = noteBookId

          // 1.4 修改 currentNotes  将当前笔记移除
          this.$store.state.noteModule.currentNotes = this.$store.state.noteModule.currentNotes.filter((item) => item.id != noteId)
          let currentNote = this.$store.state.noteModule.currentNotes[0]
          this.$store.state.noteModule.currentNoteToShow = currentNote
          this.title = currentNote.title
          this.content = currentNote.content
        })
      }
    },

    // 点击搜索结果，笔记展示内容 切换到编辑模式，list保持原状
    switchToEditMode(){
      //
      this.$store.state.noteModule.isSearchNoteShow = false
      // 直接渲染一个  避免直接赋值产生的修改上一篇笔记的 bug
      // this.initNoteContent(this.$store.state.noteModule.noteId)
    },
    // 点击 移动笔记
    clickMove() {
      this.moveNote = !this.moveNote;
      let bl = this.$refs.findval;
      this.$nextTick(function () {
        bl.focus()
      })

    },
    //关闭某些弹窗
    closeTag() {
      //当第几阶段笔记弹窗为true的时候再执行
      if (this.moveNote) {
        this.moveNote = false;
      }
    },
    // iView组件 添加标签点击事件
    handleAdd() {
      this.editTagShow = true;
      let val = this.$refs.tagValue;
      this.$nextTick(function () {
        val.style.width = '26px';
        val.focus();
      })
    },
    handleClose2(tag, index) {
      this.$store.commit('delTaglabel', {
        obj: this.noteContent,
        tag: tag,
        index: index,
      })
    },

    // 失去焦点
    BlurFn() {
      //保存数据 提交mutations 修改当前对象的标签
      let isHsh = this.count.some(el => el === this.tagVal);  //判断标签有没有重复的

      if (this.tagVal.length && !isHsh) {
        this.$store.commit('saveLabel', {
          obj: this.noteContent,
          label: this.tagVal
        })
      }

      this.editTagShow = false;
      this.tagVal = '';
    },
    // 键盘Enter事件保存标签
    enterFn() {
      this.BlurFn();
    },

    // 快捷鼠标移入移出, 鼠标移入
    kJoverHander() {
      this.kJshow = true;
    },
    tkJoverHander() {
      this.tkJshow = true;
    },
    //鼠标移开
    kJoutHander() {
      this.kJshow = false;
    },
    tkJoutHander() {
      this.tkJshow = false;
    },
    //添加快捷方式
    addkJHander(item) {
      this.$store.commit('addkJHander', {
        obj: item,
      })
    },

    // 删除笔记点击事件
    delNoteHandel(obj) {
      if (obj.id === 123456) return;
      //保存当前删除对象的下一个兄弟对象id
      // 问题: 删除完笔记列表,新建一个笔记再次删除会报错id问题
      this.$store.state.noteModule.currentNotes.forEach((item, i) => {
        if (item === obj && this.$store.state.noteModule.currentNotes.length > 1) {
          if (this.$store.state.noteModule.currentNotes[i + 1]) {
            // 如果下个兄弟存在
            this.delNextId = this.$store.state.noteModule.currentNotes[i + 1].id;
          } else if (this.$store.state.noteModule.currentNotes[i - 1]) {
            this.delNextId = this.$store.state.noteModule.currentNotes[i - 1].id;
          }
          // 新建笔记 待添加 标签 收藏 以及及时删除------------------------
        } else if (this.$store.state.noteModule.currentNotes.length === 1) {
          this.delNextId = '';
        }
      });
      /*----------------------------------------------------*/
      // 在Home组件中的删除功能,点击删除传入当前显示的对象,应该判断它pid的chilren的长度来决定第几阶段的笔记删除完了
      // 不能只考虑笔记列表中的删除功能
      this.noteBooks.forEach(el => {
        if (el.id == obj.pid) {
          if (el.children.length < 1) {
            this.$store.commit('deleteAll')
          }
        }
      });

      this.$store.commit('delClickHander', {
        obj: obj,
        id: this.delNextId,
      });
    },

    // searchDown  搜索笔记本列表
    // 从vuex中的笔记列表中过滤,同步到当前组件
    searchDown() {
      //将搜索的关键字同步到vuex
      this.$store.commit('searchHander', {
        text: this.searchValue,
      });
      let arr = this.$store.state.allList.filter(item => {
        return item.title.match(this.$store.state.searchValue) || item.content.match(this.$store.state.searchValue)
      });
      this.$store.state.noteModule.currentNotes = arr;
      // this.searchDate = arr;   //保存过滤后的数组  //将搜索到的列表集合同步到vuex中
      this.$store.commit('savefilterNote', {
        obj: arr,
      });


      //判断搜索笔记有没有 -> 如果没有搜索到笔记
      if (arr.length < 1) {
        // this.Not404 = false;  //路由跳转到allList的第一个id
        this.$store.commit('isNot404False');
        let n = this.$store.state.allList;
      } else if (arr.length >= 1) {
        //搜索到了笔记
        this.$store.commit('isNot404Yes');
        this.$router.push({
          path: '/home/' + arr[0].id
        })
      }

    },
    // 清空搜索关键字
    clearSearchVal() {
      this.searchValue = '';
    },

    //笔记本展开
    openHander() {
      this.$store.commit('openHander'); //显示完成按钮
      this.$store.commit('searchNone'); //搜索框隐藏
      this.$store.commit('noteListshow'); //隐藏Home组件笔记本列表
      this.$store.commit('yinLeftHander'); //margin-left 设置为0
    },
    // 笔记本收起 完成
    closeHander() {
      this.$store.commit('closeHander'); //显示展开图标
      this.$store.commit('noteListTrue'); //margin-left为原始值,笔记本列表显示
      this.synchronous(); //点击完成也同步左右两边的数据
    },

    //点击内容,隐藏快捷栏
    closeQuick() {
      this.$store.commit('closeQuickbox')
    },

    //前往笔记本
    qWnoteBooks() {
      this.$store.commit('noteListTrue');
      this.$store.commit('closeHander');
      this.$store.commit('QWNOTEBOOK', {
        obj: this.noteBookName
      });
      // 前往笔记本,同步笔记列表的时间
      this.getDateTimes.getDateTimes.call(this, this.$store.state.noteModule.currentNotes);


      // home组件前往笔记本,需要刷新路由,同步笔记本数据
      if (this.$store.state.joinNoteList.length >= 1) {
        this.$router.push({
          path: '/home/' + Math.random()
        })
      }

    },

    // 新建笔记
    createNoteBook() {
      this.$store.commit('createHanderShow');
      this.moveNote = false;
    },

    //infoMation组件显示的笔记信息对象
    infoHander() {
      this.$store.commit('infoHander', {
        obj: this.noteContent,
      })
    },


    // 点击设置提醒 给当前对象设置
    remindHander() {
      this.$store.commit('closeSelectHander'); //设置提醒和选项菜单都阻止了冒泡,应该手动关闭
      // 如果当前对象没有设置提醒,就设置
      if (!this.noteContent.remind) {
        this.$store.commit('setRemin', {
          obj: this.noteContent,
        })
      } else {
        // 如果已经设置了提醒,判断有没有已经设置时间
        //并且未标记完成状态状态下进行展示
        if (!this.noteContent.completeState) {
          this.$store.commit('changeReminComputed'); //
        } else {
          // 已标记完成,标记完成功能组件
          this.$store.commit('UndoReminHander');
        }

      }
    },

    // 更多 复制笔记链接
    moreHander() {
      this.$store.commit('moreHander')
    },

    //关闭遮罩层
    closeOpationsHander() {
      this.$store.commit('closeQuickbox');
      // 判断vuex状态中的标签列表是否为空,如果不为空就清空
      if (this.$store.state.tagAllList.length > 0) {
        this.$store.commit('clearTagList')
      }
    },
    //分享
    shareHander(obj) {
      this.$store.commit('showShakeMessage', {
        obj: obj,
      })
    },

    // Home组件的消息弹窗
    messageHander() {
      this.messageState = true;
      // 如果App组件弹窗显示,会关闭
      this.$store.commit('closeMessageHander')
    },
    // 关闭弹窗
    closeHanderMessage() {
      this.messageState = false;
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
