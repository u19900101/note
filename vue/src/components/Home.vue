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
    <div class="searchNote" v-show="$store.state.searchBox">
      <div class="searchChild">
        <input type="text" class="searchValue" placeholder="搜索笔记"
               v-model="searchValue"
               @keydown.enter="searchDown"
               v-focus
        >
        <!--        清空搜索内容-->
        <img src="@/assets/images/qingchusousuoneirong.png" alt=""
             class="clearSearch"
             v-if="searchValue.trim().length"
             @click="clearSearchVal"
        >
        <div class="tishixinxi">
          正在搜索 <span>你的笔记本</span>
        </div>
      </div>
    </div>
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
  data() {
    return {
      searchValue: '',
    }
  },
  methods: {
    clearSearchVal() {
      this.searchValue = '';
    }
  },

  // 计算属性
  computed: {},

  created() {

  },
  watch: {
    $route() {
      // console.log("home中打印路由发生了变化");
      // this.initNoteContent();
      // this.moveNote = false;
    },
    searchValue(searchValue) {
      //  根据内容进行全局搜索,并高亮返回
      //  点击列表可进行预览
      // 点击笔记 进行修改
      console.log("搜索字段 ",searchValue);
      let queryData = {
        "query": {
          "bool": {
            "should": [
              {
                "match": {
                  "title": searchValue
                }
              },
              {
                "match": {
                  "content": searchValue
                }
              }
            ]
          }
        },
        "highlight": {
          "fields": {
            "title": {},
            "content": {}
          }
        }
      }

      this.https.searchNoteByWords(queryData).then((data) =>{
        console.log("返回的搜索结果",data.data.hits.hits);
        let result = data.data.hits.hits
        result.forEach(item => {
          console.log("title",item.highlight.title);
          console.log("content",item.highlight.content);
        })
      //  highlight:
      //   content: ['笔记2-内<em>s</em>']
      //   title: ['笔记-2<em>s</em>']
      });
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
