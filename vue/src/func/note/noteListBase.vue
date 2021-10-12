<template>

  <!-- 1.笔记列表区域-->
  <div>
    <div class="yinxList">
      <div class="yinxTitle">
        <!-- 笔记条数和选项 -->
        <slot name="noteBookInfo"></slot>
        <div class="noteNumbers clearfix">
          <div class="yinxnum"><slot name="noteCount"></slot> 条笔记</div>
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
    }
  },
  created() {
    // console.log("noteListBase created");
  },
  methods: {

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
