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

          <div class="newSearch" @mouseover="sSoverHander" @mouseout="sSoutHander">
            <img src="./assets/images/sousuo1.png" alt="" v-show="!searchshow ">
            <img src="./assets/images/sousuo2.png" alt="" v-show="searchshow" title="搜索" @click="searchState">
          </div>
          <div class="newshare active"
               @mouseover="shareOverHander"
               @mouseout="shareOutHander"
          >
            <img src="./assets/images/fenxiang1.png" alt="" v-show="!share">
            <img src="./assets/images/fenxiang2.png" alt="" v-show="share" title="分享" @click="shareHander">
          </div>
        </div>
        <!-- 收藏 笔记 笔记本 标签 -->
        <div class="yinxlb">
          <div v-for="(item,index) in navList"
               :class="[item.class,navState === index ? 'active' : '']"
               :title="item.title"
               :key="item.id" @click="navClickHandler(item,index)"
          ></div>
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
        this.$store.getters.initNoteBooks(data.data);
      }).then(() => {
        // 2.获取笔记数据
        this.https.getNotes().then(({data}) => {
          // 2.1 初始化 notes
          // 2.2 初始化 noteId
          this.$store.getters.initNotes(data.data);
        }).then(() => {
          // 2.1 将state数据写到当前 notes
          this.$store.state.noteModule.currentNotes = this.$store.state.noteModule.notes; // 进入的笔记本列表数据

          // 3.获取标签数据
          this.https.getTags().then(({data}) => {
            this.$store.getters.initTags(data.data);
          }).then(() => {
            console.log("标签数据请求完成");
            //关闭loading动画
            this.$store.commit('closeLoadding');
            // 创建页面时初始化
            // 1.先初始化 列表  在列表排序中初始化 noteId
            let currentNotes = this.$store.state.noteModule.notes
            this.$router.push({
              name: 'noteList',
              params: {
                notes: JSON.stringify(currentNotes),
                noteBookTagName: "所有笔记"
              }
            })

            let m = JSON.stringify(currentNotes[0])
            // 2.初始化 笔记内容为 排序的第一个
            this.$router.push({
              name: 'note1', params: {
                note: JSON.stringify(currentNotes[0])
                , index: 0
              }
            })

          }).catch((err) => {
            // alert('网络延迟,请刷新重试')
            console.log(err);
          })
        })
      })
    },
    navClickHandler(obj, index) {
      //
      // 收藏
      if (obj.click === 'start') {
        this.$store.commit('startShow')
      }
      // 笔记
      else if (obj.click === 'note') {
        this.$store.getters.getNoteShow();
        let currentNotes = this.$store.state.noteModule.notes
        this.$router.push({
          name: 'noteList',
          params: {
            notes: JSON.stringify(currentNotes),
            noteBookTagName: "所有笔记"
          }
        })
        this.$router.push({name: 'note1', params: {note: JSON.stringify(currentNotes[0]),index:0}})

        // //删除vuex中管理的 搜索框隐藏
        // this.$store.commit('searchNone');
        // this.$store.commit('isNot404Yes');
        // this.$store.commit('noteBookList'); //清空第几阶段展示的信息
        //
        // //定位到/home/1
        // let n = this.$store.state.noteModule.notes;
        // if (n.length > 0) {
        //   this.$router.push({
        //     path: '/home/11111111'
        //   });
        //   this.getDateTimes.getDateTimes.call(this, n)
        // }
        //
        // //让显示笔记列表的盒模型显示出来
        // this.$store.commit('noteListTrue');
        // // 让yinList 笔记内容信息展示盒模型的margin-left为300多
        // this.$store.commit('closeQuickbox');
        // this.$store.commit('closeHander'); //显示展开图标
        // // 清空vuex中的标签笔记列表
        // this.$store.commit('clearTagList');
        // // 去除标签组件信息展示
        // this.$store.commit('closeTagShow');
      }
      //笔记本
      else if (obj.click === 'noteBook') {
        this.$router.push({name: 'noteBookList'})
        // this.$store.dispatch('noteBookShow')
      }
      //标签
      else if (obj.click === 'tag') {
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
      // 1.操作数据库 新建一条空笔记  返回id  添加到默认笔记本中  默认笔记本可设置
      let defaultNoteBookId = 1
      this.https.insertNote({'pid': defaultNoteBookId}).then(({data}) => {
        let newNote = data.data;
        console.log("note created ", newNote);
        this.$store.state.noteModule.noteId = newNote.id
        // 默认情况下未返回 title 和 content
        // 给当前 note 清空
        this.$store.state.noteModule.title = ''
        this.$store.state.noteModule.content = ''
        newNote = {
          "id": newNote.id,
          "pid": defaultNoteBookId,
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
        this.$store.state.noteModule.currentNotes.unshift(newNote)

        // 3.刷新路由 让 note组件显示最新新建的值
        this.$router.push({name: 'note1', params: {note: JSON.stringify(newNote),index:0}})

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

    // 让搜索框通过vuex中的状态让它显示出来,并且网页剪辑隐藏
    searchState() {

      this.$router.push({name: 'search'})

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
  },

}


</script>
<style>
[v-cloak] {
  display: none;
}

.content {
  width: 100%;
  height: 100%;
}

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
