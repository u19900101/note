<template>
  <div class="youbian clearfix">
<!--    &lt;!&ndash;快捷方式滑窗口-&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&ndash;&gt;-->
<!--    <yxQuickBook></yxQuickBook>-->
<!--    &lt;!&ndash;笔记本滑动窗-&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&ndash;&gt;-->
<!--    <yxNotebook></yxNotebook>-->
<!--    &lt;!&ndash;提示组件&ndash;&gt;-->
<!--    <successinfo></successinfo>-->
<!--    &lt;!&ndash;标签组件&ndash;&gt;-->
<!--    <yxNotetags></yxNotetags>-->
<!--    &lt;!&ndash;搜索笔记&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&ndash;&gt;-->
<!--    <div class="searchNote" v-show="$store.state.searchBox">-->
<!--      <div class="searchChild">-->
<!--        <input type="text" class="searchValue" placeholder="搜索笔记"-->
<!--               v-model="searchValue"-->
<!--               @keydown.enter="searchDown"-->
<!--               v-focus-->
<!--        >-->
<!--        &lt;!&ndash;        清空搜索内容&ndash;&gt;-->
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
<!--    &lt;!&ndash; 笔记列表区域 -&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&#45;&ndash;&gt;-->
<!--    <noteList></noteList>-->

    <router-view />
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
    },
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
            // this.initList();
            // 2.初始化 笔记内容为 排序的第一个
            this.$router.push({ name: 'note1', params: { note: JSON.stringify(this.$store.state.noteModule.currentNotes[0])}})
            // this.$refs.noteM.initNoteContent(this.$store.state.noteModule.noteId);

          }).catch((err) => {
            // alert('网络延迟,请刷新重试')
            console.log(err);
          })
        })
      })
    },
  },

  // 计算属性
  computed: {},
  created() {
    this.getData()
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
      console.log("搜索字段 ", searchValue);
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
              },
              {
                "wildcard": {
                  "title": "*" + searchValue + "*"
                }
              },
              {
                "wildcard": {
                  "content": "*" + searchValue + "*"
                }
              }
            ]
          }
        },
        "highlight": {
          "pre_tags": ["<font style=\"background:yellow\" color=\"red\">"],
          "post_tags": ["</font>"],
          "fields": {
            "title": {},
            "content": {}
          }
        }
      }
      if (searchValue.length > 0) {
        this.https.searchNoteByWords(queryData).then((data) => {
          console.log("返回的搜索结果", data.data.hits.hits);
          let result = data.data.hits.hits
          this.$store.state.noteModule.title = '未搜索到相关信息';
          this.$store.state.noteModule.content = '';
          if (result.length > 0) {
            // 封装 currentNotes  直接利用查询到的数据进行封装note
            let searchNotes = []
            result.forEach(item => {
              let noteTemp = []
              noteTemp.id = item._id
              // 有高亮就显示高亮  没高亮就原始值
              for (let keyValue in item._source) {
                noteTemp[keyValue] = item._source[keyValue]
              }
              // 覆盖
              noteTemp.title = item.highlight.title ? item.highlight.title[0] : item._source.title;
              noteTemp.content = item.highlight.content ? item.highlight.content[0] : item._source.content;
              searchNotes.push(noteTemp)
            })

            this.$store.state.noteModule.currentNoteToShow = searchNotes[0]
            this.$store.state.noteModule.noteId = searchNotes[0].id
            this.$store.state.noteModule.title = searchNotes[0].title
            this.$store.state.noteModule.content = searchNotes[0].content
            this.$store.state.noteModule.searchNotes = searchNotes

          }
          // 展示搜素结果
          this.$store.state.noteModule.editMode = false
        });
      }

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
