<template>

  <!-- 笔记列表区域  todo 冒泡------------------------------------------------------->
  <!--  <div class="yinxList" @mousedown.prevent="BlurFn" v-show="$store.state.noteModule.isNotesShow">-->
  <div>
    <div class="yinxList" v-show="$store.state.noteModule.isNotesShow">
      <div class="yinxTitle">
        <!--首页显示        如果搜索到笔记就不显示-->
        <div class="cuthide"
             v-show="!$store.state.searchBox"
             v-if="!$store.state.noteBookModule.currentNoteBookName">
          <h2 class="notTitle">所有笔记</h2>
<!--          <div class="yinxcut">-->
<!--            <a href="https://www.yinxiang.com/webclipper/" target="_blank">网页剪藏</a>-->
<!--          </div>-->
        </div>
        <div class="tagNames" v-if="$store.state.noteBookModule.currentNoteBookName">
          当前笔记本(标签):{{ $store.state.noteBookModule.currentNoteBookName }}
        </div>
        <!--进入笔记本的时候 显示的笔记本名称 或者 标签名称-->
        <noteBookInfo></noteBookInfo>


        <!-- 笔记条数和选项 -->
        <div class="noteNumbers clearfix">
          <div class="yinxnum">{{ $store.state.noteModule.currentNotes.length }} 条笔记</div>
          <div class="select" @click.stop="sortClick">
            <span>选项</span>
          </div>
          <!--选项列表-->
          <yxSelectSort></yxSelectSort>
        </div>
      </div>

      <!-- 笔记列表-------------->
      <div class="NodesTwoList">
<!--        编辑模式下的显示-->
        <div class="nodescroll" id="nodescroll" ref="homeScroll" v-if="$store.state.noteModule.editMode">
<!--            弃用路由跳转 改为使用点击触发函数跳转-->
          <div class="n-conts"
               v-for="(item,index) in $store.state.noteModule.currentNotes"
               :key="item.id"
               @click="listItemClick(item.id)"
               :class="$store.state.noteModule.noteId == item.id ? 'sel' : ''"
               v-show="!$store.state.notelistNumber">
            <h2 class="n-title">{{ item.title }}</h2>
            <div class="n-times" v-show="item.createTimeAlias">{{ item.createTimeAlias }}</div>
            <div class="n-times" v-show="!item.createTimeAlias">{{ item.createTime }}</div>
            <div class="n-wrap" v-show="$store.state.showTextState">
              {{ item.content }}
            </div>

            <!-- 笔记列表 分享 闹钟 收藏 删除 -->
            <!--          <div class="n-fnc">-->
            <!--            <div class="n-shake cont-icon" @click.stop="shareHander(item)" title="分享"></div>-->
            <!--            <div class="n-remind cont-icon remins"-->
            <!--                 title="设置提醒" :class="item.remind ? 'active' : ''"-->
            <!--                 @click.stop="remindHander"-->
            <!--            >-->
            <!--            </div>-->

            <!--            <div class="n-collection cont-icon" :title="!item.shortcut ? '添加快捷方式' : '移除快捷方式'">-->
            <!--              <img src="@/assets/images/shoucang_white_24x24.png" alt=""-->
            <!--                   v-if="!kJshow && !item.shortcut"-->
            <!--                   @mouseover="kJoverHander"-->
            <!--              >-->
            <!--              <img src="@/assets/images/shortcuts_solid_white_24x24.png" alt=""-->
            <!--                   v-if="kJshow || item.shortcut"-->
            <!--                   @mouseout="kJoutHander"-->
            <!--                   @click.stop="addkJHander(item)"-->
            <!--              >-->
            <!--            </div>-->
            <!--            <div class="n-delete cont-icon" id="delicom" title="删除笔记" @click.stop="delNoteHandel(item)"></div>-->
            <!--            &lt;!&ndash;设置提醒组件&ndash;&gt;-->
            <!--          </div>-->
            <div class="n-bot"></div>
          </div>


          <!--未找到搜索的笔记  动态计算高度----------------->
          <noteSearch></noteSearch>
          <my-tip></my-tip>
          <!--未找到标签笔记-->
          <notFindtag></notFindtag>
        </div>

<!--        搜索模式下的显示-->
        <div class="nodescroll" id="nodescroll2" ref="homeScroll" v-if="!$store.state.noteModule.editMode">
          <!--            弃用路由跳转 改为使用点击触发函数跳转-->
          <div class="n-conts"
               v-for="(item,index) in $store.state.noteModule.searchNotes"
               :key="item.id"
               @click="listItemClick(item.id)"
               :class="$store.state.noteModule.noteId == item.id ? 'sel' : ''"
               v-show="!$store.state.notelistNumber">
            <h2 class="n-title" v-html="item.title"></h2>
            <div class="n-times" v-show="item.createTimeAlias" v-html="item.createTimeAlias"></div>
            <div class="n-times" v-show="!item.createTimeAlias" v-html="item.createTime"></div>
            <div class="n-wrap" v-show="$store.state.showTextState" v-html="item.content"></div>
            <div class="n-bot"></div>
          </div>


          <!--未找到搜索的笔记  动态计算高度----------------->
          <noteSearch></noteSearch>
          <my-tip></my-tip>
          <!--未找到标签笔记-->
          <notFindtag></notFindtag>
        </div>
      </div>
    </div>



    <!--最右侧笔记本内容信息区域-------------------------------->
    <note ref="noteM"></note>

  </div>
</template>

<script>
import noteSearch from '../../func/note/noteSearch'
import myTip from '../../components/prompt/tip'
import notFindtag from '../../components/prompt/not-findtag'
import noteBookInfo from '../../components/prompt/noteBookInfo'
import yxSelectSort from '@/func/select/yx-SelectSort'
import note from "./note";


export default {
  name: "noteList",
  components: {
    noteSearch,
    'my-tip': myTip,
    notFindtag,
    noteBookInfo,
    yxSelectSort,
    note,
  },
  data() {
    return {
      // 笔记选中的状态
      state: 1,
    }
  },
  created() {
    // 4.开始渲染页面  调用子页面的方法
    this.getData();
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
            this.initList();
            // 2.初始化 笔记内容为 排序的第一个
            this.$refs.noteM.initNoteContent(this.$store.state.noteModule.noteId);

          }).catch((err) => {
            // alert('网络延迟,请刷新重试')
            console.log(err);
          })
        })
      })
    },

    listItemClick(currentNoteId) {

      this.$refs.noteM.initNoteContent(currentNoteId);
    },

    // 选项列表功能
    sortClick() {
      this.$store.commit('isSortShow')
    },
    // 失去焦点
    // BlurFn() {
    //   //保存数据 提交mutations 修改当前对象的标签
    //   let isHsh = this.count.some(el => el === this.tagVal);  //判断标签有没有重复的
    //
    //   if (this.tagVal.length && !isHsh) {
    //     this.$store.commit('saveLabel', {
    //       obj: this.noteContent,
    //       label: this.tagVal
    //     })
    //   }
    // },
    // 初始化 笔记/笔记本/标签 列表
    initList() {
      if (this.$store.state.noteModule.currentNotes.length > 0) {
        this.$store.commit('showNoteList')
      }
      // 根据排序方式进行当前笔记的排序
      this.sortWay.sortWay.call(this, this.$store.state.noteModule.currentNotes);

      // 初始化 笔记内容id,便于下一步显示
      this.$store.state.noteModule.noteId = this.$store.state.noteModule.currentNotes[0].id

      // this.noteBooks = this.$store.state.noteBookModule.noteBooks; // 全部的第几阶段笔记
      //
      // // 根据Pid过滤不同阶段的笔记
      // // 在笔记本列表中过滤出与Pid一样的数据,就是当前显示数据的父亲
      // let dJ = this.noteBooks.filter(item => item.id == this.Pid)[0];
      // this.noteBookTitle = dJ.title;

    },

  },
  watch: {
    $route() {
      // this.moveNote = false;
      //每次路由更新就调用这个方法,同步textarea和笔记列表数据
      console.log("noteList中打印路由发生了变化");
      this.initList();
      this.$refs.noteM.initNoteContent(this.$store.state.noteModule.noteId);


    },

  }
}
</script>

<style scoped>

</style>
