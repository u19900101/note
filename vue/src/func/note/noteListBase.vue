<template>

  <!-- 1.笔记列表区域-->
  <div>
    <div class="yinxList">
      <noteBookInfo></noteBookInfo>
      <div class="yinxTitle">
        <!-- 笔记条数和选项 -->
        <div class="noteNumbers clearfix">
          <div class="yinxnum">{{ JSON.parse($route.params.notes).length }} 条笔记</div>
          <div class="select" @click.stop="sortClick">
            <span>选项</span>
          </div>
          <!--选项列表-->
          <yxSelectSort></yxSelectSort>
        </div>
      </div>

      <!-- 笔记列表-------------->
      <div class="NodesTwoList">
        <div class="nodescroll" id="nodescroll" ref="homeScroll">
          <slot></slot>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import noteSearch from '../../func/note/noteSearch'
import myTip from '../../components/prompt/tip'
import notFindtag from '../../components/prompt/not-findtag'
import noteBookInfo from "./noteBookInfo";
import yxSelectSort from '@/func/select/yx-SelectSort'
import note from "./note";


export default {
  name: "noteList",
  components: {
    noteBookInfo,
    yxSelectSort,
  },
  data() {
    return {
    }
  },
  created() {
    // console.log("noteListBase created");
  },
  methods: {
    listItemClick(currentNote, index) {
      this.$router.push({
        name: 'note1', params: {
          note: JSON.stringify(currentNote),
          index: index
        }
      })
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
      // console.log("noteListBase 中打印路由发生了变化");
    },

  }
}
</script>

<style scoped>

</style>
