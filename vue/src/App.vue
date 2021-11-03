<template>
  <div id="app" v-cloak>

    <div class="content clearfix" @click="closeSelect">
      <!--      <yxDeleteNote></yxDeleteNote>-->
      <!--      <yxDeleteNoteBooks></yxDeleteNoteBooks>-->
      <yx-CreateBook></yx-CreateBook>
      <yxInforMationBook></yxInforMationBook>
      <!--删除标签-->
      <yxDeleteTags></yxDeleteTags>
      <yxMessage :data="mesdate" :state="messtate" @close-hander="closeMessageHander"></yxMessage>
      <div class="loading" v-show="$store.state.loadingState">
        <img src="./assets/images/loadding.gif" alt="" class="loadingPic">
      </div>
      <!-- 左侧操作栏 -->
      <div class="yinxleft" id="navl" v-show="navShow" @mousedown.prevent>
        <div class="yinxt">
          <img src="./assets/images/leftToppic.png" alt="">
        </div>
        <!-- 新建笔记 -->
        <div class="yinxfcn">
          <div class="newnotes" @mouseover="overxJ" @mouseout="outxJ">
            <img src="./assets/images/xinjian1.png" alt="" v-if="xJ">
            <img src="./assets/images/xinjian.png" alt="" v-if="!xJ" title="新建笔记" @click="insertNote">
          </div>

          <div class="newshare active"
               @mouseover="shareOverHander"
               @mouseout="shareOutHander">
            <img src="./assets/images/fenxiang1.png" alt="" v-show="!share">
            <img src="./assets/images/fenxiang2.png" alt="" v-show="share" title="分享" @click="shareHander">
          </div>
        </div>
        <!-- 收藏 笔记 笔记本 标签 -->
        <div class="yinxlb">
          <div v-for="(item,index) in navList"
               :class="[item.class,navClickIndex === index ? 'active' : '']"
               :title="item.title"
               :key="item.id" @click="navClickHandler(item,index)"></div>
        </div>

        <!-- 左侧底部图标 -->
        <div class="yinxbotpic">
          <div class="botpic">
            <a href="https://github.com/qiqingfu/vue-Impression--notes" target="_blank" title="github">
              <img src="./assets/images/leftbottomlogo.png" alt="">
            </a>
          </div>
        </div>
      </div>
      <router-view/>
    </div>

  </div>
</template>

<script>

// import yxDeleteNote from '@/func/delete/del-notes/yx-DeleteNote'  //删除笔记组件
// import yxDeleteNoteBooks from '@/func/delete/del-notebook/yx-DeleteNoteBooks' //删除笔记本组件
import yxCreateBook from '@/func/create/yx-CreateBook'
import yxInforMationBook from '@/func/info-book/yx-InforMationBook'
import yxDeleteTags from '@/func/delete/del-notetags/yx-DeleteTags'
import yxMessage from '@/func/group/message/yx-group-message'
import navdataList from '@/assets/js/navDatelist'

//设置提醒组件
import setremin from '@/func/reminders/SetRemin'
import changeremin from '@/func/reminders/changeremin'
import undoremin from '@/func/reminders/UndoRemin'
import showtimes from '@/func/reminders/showTimes'


export default {
  name: 'App',
  data() {
    return {
      navList: navdataList, //笔记导航
      xJ: true,   //新建显隐
      navShow: true, // 导航显隐
      searchshow: false, //搜索显隐
      share: false, // 分享显隐
      navClickIndex: 1 //收藏- 0  笔记-1(初始化选中)  笔记本-2  标签-3
    }
  },
  components: {
    // yxDeleteNote,
    // yxDeleteNoteBooks,
    'yx-CreateBook': yxCreateBook,
    yxInforMationBook,
    yxDeleteTags,
    yxMessage,
    setremin,
    changeremin,
    undoremin,
    showtimes,
  },
  methods: {
    // 获取 数据
    getData() {
      // 1.获取笔记本数据
      this.https.getNotebooks().then(({data}) => {
        this.$store.getters['noteBookModule/initNoteBooks'](data.data);

        this.$store.state.noteBookModule.currentNoteBook = data.data[0]
      }).then(() => {
        // 2.获取笔记数据
        this.https.getNotes().then(({data}) => {
          // 2.1 初始化 notes
          // 2.2 初始化 noteId
          this.$store.getters['noteModule/initNotes'](data.data);
        }).then(() => {
          // 2.1 将state数据写到当前 currentNoteList, currentNote currentNoteBookNoteList(当前笔记本中所有的笔记)
          this.$store.state.noteModule.currentNoteList = this.$store.state.noteModule.notes; // 进入的笔记本列表数据
          this.$store.state.noteBookModule.currentNoteBookNoteList = this.$store.state.noteModule.notes;

          this.$store.state.noteModule.starNoteList = this.$store.state.noteModule.notes.filter((note) => note.star == true)
          // 给笔记添加时间别名//
          this.getDateTimes.getDateTimes.call(this, this.$store.state.noteModule.currentNoteList);
          // 3.获取标签数据
          this.https.getTags().then(({data}) => {
            this.$store.getters.initTags(data.data);
          }).then(() => {
            console.log("标签数据请求完成");
            //关闭loading动画
            this.$store.commit('closeLoadding');
            // 创建页面时初始化
            // 1.先初始化 列表  在列表排序中初始化 noteId
            this.$router.push({name: 'noteList'})
            // 2.初始化 笔记内容为 排序的第一个
            if(this.$store.state.noteModule.currentNoteList.length >0){
              this.$store.state.noteModule.currentNote = this.$store.state.noteModule.notes[0]; // 进入的笔记本列表数据
              this.$router.push({name: 'note1'})
            }

          }).catch((err) => {
            // alert('网络延迟,请刷新重试')
            console.log(err);
          })
        })
      })
    },

    navClickHandler(obj, index) {
      // 收藏
      if (obj.click === 'star') {
        this.navClickIndex = 0
        this.$store.state.noteModule.currentNoteList = this.$store.state.noteModule.starNoteList; // 进入的笔记本列表数据
        // 序号为 1的是收藏笔记本
        this.$store.state.noteBookModule.currentNoteBook = this.$store.state.noteBookModule.noteBooks[1]
        this.$router.push({name: 'noteList'})
        if(this.$store.state.noteModule.currentNoteList.length >0){
          this.$store.state.noteModule.currentNote = this.$store.state.noteModule.currentNoteList[0]; // 进入的笔记本列表数据
          this.$router.push({name: 'note1'})
        }
      }
      // 笔记
      else if (obj.click === 'note') {
        this.navClickIndex = 1
        this.$store.state.noteModule.currentNoteList = this.$store.state.noteModule.notes; // 进入的笔记本列表数据
        this.$store.state.noteBookModule.currentNoteBook =  this.$store.state.noteBookModule.noteBooks[0]
        this.$router.push({name: 'noteList'})
        if(this.$store.state.noteModule.currentNoteList.length >0){
          this.$store.state.noteModule.currentNote = this.$store.state.noteModule.notes[0]; // 进入的笔记本列表数据
          this.$router.push({name: 'note1'})
        }

      }
      //笔记本
      else if (obj.click === 'noteBook') {
        this.navClickIndex = 2  // 图标选中的样式控制
        // 初始化默认显示的笔记本
        let defaultNoteBookId = this.$store.state.noteBookModule.noteBooks[0].id
        let currentNoteBookNoteList = this.$store.state.noteModule.notes.filter(item => item.pid ==defaultNoteBookId);
        this.$store.state.noteBookModule.currentNoteBookNoteList = currentNoteBookNoteList
        this.$store.state.noteBookModule.isNoteBooksShow = true
        this.$router.push({name: 'noteBookList'})
      }
      //标签
      else if (obj.click === 'tag') {
        this.navClickIndex = 3
        this.$store.dispatch('noteTagShow');
      }
      // 选中标识  todo
      let selectState = this.$store.state.deleteNotesState;  //当前导航所在的状态
      if (this.$store.state.quickShow) {
        this.$store.commit('changeNavState', 0);
      } else if (this.$store.state.noteBookShow) {
        this.$store.commit('changeNavState', 2);
      } else if (this.$store.state.noteTagState || this.$store.state.isJoinNotesTagList) {
        this.$store.commit('changeNavState', 3)
      } else {
        this.$store.commit('changeNavState', 1);
      }

    },
    // 新建笔记
    insertNote() {
      // 1.操作数据库
      // 在当前的笔记本新建笔记
      let currentNoteBookId = this.$store.state.noteBookModule.currentNoteBook.id
      let pid = currentNoteBookId == 0 ? 1:currentNoteBookId
      this.https.insertNote({'pid': pid}).then(({data}) => {
        let newNote = data.data;
        console.log("新建笔记 ", newNote);
        this.$store.state.noteModule.noteId = newNote.id
        // 默认情况下未返回 title 和 content

        newNote = {
          "id": newNote.id,
          "pid": pid,
          "title": "",
          "status": false,
          "summary": "",
          "createTime": newNote.createTime,
          "updateTime": "",
          "remindTime": "",
          "content": "",
          "tagUid": "",
          "mediaUid": "",
          "star": false,
          "tagList": [],
          "mediaList": []
        }
        // 2.修改 currentNotes,将新建置顶
        // 让置顶的笔记处于选中状态
        this.$store.state.noteModule.currentNoteList.unshift(newNote)
        this.$store.state.noteModule.currentNote = newNote
        this.$store.state.noteModule.currentIndex = 0  // 让新建的笔记处于选中状态
        // 选择笔记本的情况下 需要对notes 进行追加，因为此时 currentNoteList 指向为当前笔记本
        if(currentNoteBookId != 0){
          this.$store.state.noteModule.notes.unshift(newNote)
        }
        this.$store.state.noteBookModule.noteBooks.filter(item => item.id == pid)[0].noteCount += 1

        // // 3.刷新路由 让 note组件显示最新新建的值,并且让光标自动定位到标题栏
        this.$router.push({name: 'noteList'})
        this.$router.push({name: 'note1'})
      })


    },
    // 新建鼠标移入移出事件
    overxJ() {
      this.xJ = false;
    },
    outxJ() {
      this.xJ = true;
    },
    // 鼠标移入搜索框
    sSoverHander() {
      this.searchshow = true;
    },
    sSoutHander() {
      this.searchshow = false;
    },


    // 选项下拉菜单收起 通知提醒弹框显示
    closeSelect(state) {
      if (this.$store.state.selectDown) {
        this.$store.commit('closeSelectHander')
      }
      this.$store.commit('closeRemin');
    },
    // 分享移入
    shareOverHander() {
      this.share = true;
    },
    // 分享移出
    shareOutHander() {
      this.share = false;
    },
    // 分享组件显示
    shareHander() {
      this.$store.commit('shareBlock')
    },
    //关闭消息弹窗
    closeMessageHander() {
      this.$store.commit('closeMessageHander')
    }
  },
  computed: {
    //消息组件状态 2018-06-15 调整
    mesdate() {
      return this.$store.state.messageDate;
    },
    messtate() {
      return this.$store.state.messageShow;
    },
    navState() {
      return this.$store.state.navState;
    }
  },
  watch: {
    $route() {
      //通过侦听路由对象的变化
      // console.log("app中 route函数执行了");
      // let routeName = this.$route.path;
      // if (routeName === '/insertNote') {
      //   // if (this.navShow !== false) {
      //   //   this.navShow = false;
      //   // }
      // } else if (routeName === '/home') {
      //   this.navShow = true;
      // }
    }
  },
  created() {
    this.getData()
    // console.log(this)
  },

}


</script>
<style>

[v-cloak] {
  display: none;
}

/*.content {*/
/*  width: 100%;*/
/*  height: 100%;*/
/*}*/

.loading {
  position: relative;
  z-index: 99999;
  width: 100%;
  height: 100%;
  background: #5e4674;
}

.loadingPic {
  width: 400px;
  height: 325px;
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  margin: auto;
}
</style>
