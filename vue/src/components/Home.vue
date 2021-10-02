<template>
  <div class="youbian clearfix">
    <!--快捷方式滑窗口--------------------------------------------------------------->
    <yxQuickBook></yxQuickBook>
    <!--笔记本滑动窗----------------->
    <yxNotebook></yxNotebook>
    <!--提示组件-->
    <successinfo></successinfo>
    <!--标签组件-->
    <yxNotetags></yxNotetags>
<!--搜索笔记-------------------------------------------------------------------->
<!--    <div class="searchNote" v-show="$store.state.searchBox">-->
<!--      <div class="searchChild">-->
<!--        <input type="text" class="searchValue" placeholder="搜索笔记"-->
<!--               v-model="searchValue"-->
<!--               @keydown.enter="searchDown"-->
<!--               v-focus-->
<!--        >-->
<!--        <img src="@/assets/images/qingchusousuoneirong.png" alt=""-->
<!--             class="clearSearch"-->
<!--             v-if="searchValue.trim().length"-->
<!--             @click="clearSearchVal"-->
<!--        >-->
<!--        <div class="tishixinxi">-->
<!--          正在搜索 <span>你的笔记本</span>-->
<!--        </div>-->
<!--      </div>-->
<!--    </div>-->
    <!-- 笔记列表区域 ------------------------------------------------------->
    <noteList></noteList>

  </div>
</template>

<script>

import yxQuickBook from '../func/quick/yx-QuickBook'
import yxNotebook from '../func/notebook/yx-NoteBook'

import successinfo from '../components/prompt/successInfo'
import yxNotetags from '../func/note-tag/yx-NoteTag'

import noteList from '../func/note/noteList'
import note from '../func/note/note'

let dayjs = require('dayjs');
dayjs().format();

//设置提醒组件
import {DatePicker} from 'iview'
import router from '../router'
export default {
  name: "home",
  components: {
    yxQuickBook,
    yxNotebook,
    successinfo,
    DatePicker,
    yxNotetags,
    noteList,

    router,
  },
  // data() {
  //   return {
  //
  //   }
  // },
  methods: {



    // 同步笔记时间数据
  },

  // 计算属性
  computed: {

  },

  // 钩子函数 请求数据同步vuex
  created() {



  },

  watch: {
    watch: {
      $route() {
        console.log("note中打印路由发生了变化");
        // this.initNoteContent();
        // this.moveNote = false;
      },

    }
  },
  // todo  ??
  directives: {
    focus: {
      inserted: function (el) {
        el.focus();
      }
    }
  }

}


// import router from './router'

// router.beforeEach((to, from, next) => {
//   console.log("App.vue中打印路由发生了变化");
//   //这里能够跟踪路径的变化
//   console.log(from);
//   console.log(to);
//   //最后通过钩子继续页面的跳转
//   next();
// });

</script>

<style>

</style>
