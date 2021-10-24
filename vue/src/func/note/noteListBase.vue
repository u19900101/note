<template>


  <!--    -->
  <div>
    <!-- 1.笔记列表区域 todo bug 还必须要这个。。。-->
    <div class="yinxList">
    </div>
    <div class="content clearfix">
      <div class="nodescroll">
        <!-- 笔记列表-------------->
        <!-- 笔记条数和选项 -->
        <slot name="noteBookInfo"></slot>
        <div class="noteNumbers clearfix">
          <div class="yinxnum">
            <slot name="noteCount"></slot>
            条笔记
          </div>
          <div class="select" @click.stop="sortClick">
            <span>选项</span>
          </div>
          <!--选项列表-->
<!--          <yxSelectSort :hideSelectSort="sortClick" v-show="isSortShow"></yxSelectSort>-->
          <yxSelectSort @hideSelectSort="sortClick" v-show="isSortShow"></yxSelectSort>
        </div>

        <!-- 插槽的style要连续使用 不然bug太多-->
        <div class="scroll-area">
          <slot name="noteList"></slot>
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
  name: "noteListBase",
  components: {
    noteBookInfo,
    yxSelectSort,
  },
  data() {
    return {
      isSortShow: false
    }
  },
  created() {
    // console.log("noteListBase created");
  },
  methods: {

    // 选项列表功能
    sortClick() {
      this.isSortShow = !this.isSortShow
    },
    // 失去焦点
    BlurFn() {
      console.log('失去焦点')
      //保存数据 提交mutations 修改当前对象的标签
      // let isHsh = this.count.some(el => el === this.tagVal);  //判断标签有没有重复的
      //
      // if (this.tagVal.length && !isHsh) {
      //   this.$store.commit('saveLabel', {
      //     obj: this.noteContent,
      //     label: this.tagVal
      //   })
      // }
    },


  },
  watch: {
    $route() {
      // console.log("noteListBase 中打印路由发生了变化");
    },
  },

}
</script>

<style scoped>


</style>
