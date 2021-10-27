<template>
  <div>

    <div class="yinxDet clearfix" id="yinxdet" :class="$store.state.yinxdetWidth ? 'notWidth' : ''">

      <!--透明度遮罩层-->
      <div class="opationWindow" v-show="$store.state.yinListopation" @click="closeOpationsHander"></div>
      <!--删除 收藏-->
      <div class="dethead" @mousedown.prevent>
        <div class="detfunc">
          <!--设置提醒-->
          <div class="deftimes main"
               title="设置提醒"
               @click.stop="remindHander"
          >
            <!--             :class="noteContent.remind && !noteContent.completeState ? 'active' : ''"-->
            <!--提醒已添加 通知我弹窗-->
            <setremin></setremin>

            <!--修改弹窗提醒-->
            <changeremin></changeremin>

            <!--撤销 修改提醒 清除日期-->
            <undoremin></undoremin>

            <!--<setdate :open="open"></setdate>-->
            <showtimes></showtimes>

          </div>
          <!--          <div class="tixingshijian" :class="noteContent.completeState ? 'wancheng' : ''">-->
          <!--            {{ noteContent.remindTime }}-->
          <!--          </div>-->

          <!--收藏笔记-->
          <div @click="starNoteClick" class="defshake main" :title="!star ? '收藏' : '取消收藏'">
            <img v-if="!star" src="@/assets/images/defshoucang.png" alt="">
            <!--取消收藏笔记-->
            <img v-else src="@/assets/images/shanchukuaijiefangshiwujiaoxing.png" alt="">
          </div>

          <div class="definfo main bj-n" title="笔记信息" @click="infoHander"></div>
          <!--          存放 deleteNote 搜素和编辑各自展示  便于删除后列表的更新-->
          <slot name="deleteNote"></slot>

        </div>
      </div>

      <!--移动笔记和标签-->
      <div class="stages">
        <div class="dijijieduanBJ clearfix">
          <!--1当前笔记本-->
          <div class="yidong clearfix">
            <img src="@/assets/images/dijijieduanbiji.png" alt="" class="tubiao" title="移动笔记" @mousedown.prevent>

            <!--1.1显示笔记本和移动笔记-->
            <slot name="notebook"/>
          </div>

          <!--2.标签-->
          <div class="addtag">
            <!--@mousedown.prevent-->
            <div class="tianjiaBQ">
              <Tag v-for="(item,index) in tagList" :key="item" :name="item" closable
                   @on-close="handleClose2(item,index)">
                {{ item }}
              </Tag>
              <Button icon="ios-plus-empty" type="dashed" size="small" @click="handleAdd" v-show="!editTagShow">添加标签
              </Button>
              <input type="text" class="tagValue"
                     v-show="editTagShow"
                     ref="tagValue"
                     v-model="tagVal"
                     :style="tagWidth"
                     @blur="BlurFn"
                     @keydown.enter="enterFn"
              >
            </div>
          </div>
        </div>
      </div>

      <!--笔记的标题和内容展示-->
      <div class="editCount" ref="editScroll">
        <div class="root">
          <slot name="titleAndContent"></slot>
        </div>
      </div>
      <!--遮罩层-->
      <div class="noteList" v-show="$store.state.notelistNumber"></div>

    </div>
  </div>

</template>

<script>
import setremin from '@/func/reminders/SetRemin'
import changeremin from '@/func/reminders/changeremin'
import undoremin from '@/func/reminders/UndoRemin'
import showtimes from '@/func/reminders/showTimes'
import {Tag, Button} from 'iview'
import deleteNote from "../note/deleteNote";
import {updateNote} from "../../server";
import {mapState} from "vuex";

export default {
  name: "noteBase",
  data() {
    return {
      // todo 搜索功能完善
      // title: JSON.parse(this.$route.params.note).title.replace(/<font style="background:yellow" color="red">/gi, "").replace(/<\/font>/gi, ""),
      // content: JSON.parse(this.$route.params.note).content.replace(/<font style="background:yellow" color="red">/gi, "").replace(/<\/font>/gi, ""),
      // searchTitle: JSON.parse(this.$route.params.note).title,
      // searchContent: JSON.parse(this.$route.params.note).content,


      //--------------------------------------------------


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
    Tag,
    Button,
    setremin,
    changeremin,
    undoremin,
    showtimes,

  },
  computed: {
    //借助mapState生成计算属性，从state中读取数据。（数组写法）
    ...mapState('noteModule', {
      noteId: state => state.currentNote.id,
      tagList: state => state.currentNote.tagList
    }),
    star: {
      get: function () {
        return this.$store.state.noteModule.currentNote.star
      },
      set: function (newValue) {
      }
    },
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
  methods: {
    // 收藏/取消收藏笔记
    starNoteClick() {
      // 1.操作数据库 更新笔记的收藏信息
      this.https.updateNote({id: this.noteId, star: !this.star}).then(({data}) => {
        console.log("收藏/取消收藏笔记成功  ", data);
      })
      // 3.更新笔记本列表 和 starNoteList
      this.$store.state.noteModule.currentNote.star = !this.star// 此时会同时修改 this.star
      if (this.star) {
        // 添加收藏
        this.$store.state.noteModule.starNoteList.unshift(this.$store.state.noteModule.currentNote)
        this.$store.state.noteBookModule.noteBooks[1].noteCount += 1
      } else {
        // 移除收藏 1.修改 starNoteList  2.更新 starNoteBook
        this.$store.state.noteModule.starNoteList = this.$store.state.noteModule.starNoteList.filter((note) => note.id != this.noteId)
        // 在收藏列表中取消收藏 要跳转  其他情况则不需要进行页面跳转，只需要进行数据更新
        if (this.$store.state.noteBookModule.currentNoteBook.id == 1){
          this.$store.state.noteModule.currentNoteList = this.$store.state.noteModule.starNoteList
          if( this.$store.state.noteModule.starNoteList.length == 0){
            this.$router.push({name: 'noteList'})
          }else {
            this.$store.state.noteModule.currentNote = this.$store.state.noteModule.currentNoteList[0]
          }
        }
        this.$store.state.noteBookModule.noteBooks[1].noteCount -= 1
      }

    },

    updateNotes() {
      this.$store.state.noteModule.notes.forEach((note) => {
        if (note.id == this.noteId) {
          note.star = !note.star
        }
      })
    },
    getNoteBookNameById(noteBookId) {
      let noteBook = this.$store.state.noteBookModule.noteBooks.filter(item => item.id == noteBookId)[0]
      return noteBook.title
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
      // if (!this.noteContent.remind) {
      //   this.$store.commit('setRemin', {
      //     obj: this.noteContent,
      //   })
      // } else {
      //   // 如果已经设置了提醒,判断有没有已经设置时间
      //   //并且未标记完成状态状态下进行展示
      //   if (!this.noteContent.completeState) {
      //     this.$store.commit('changeReminComputed'); //
      //   } else {
      //     // 已标记完成,标记完成功能组件
      //     this.$store.commit('UndoReminHander');
      //   }
      //
      // }
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

  created() {

  },
  watch: {
    // 侦听路由对象变化
    $route() {

    },

  },

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
