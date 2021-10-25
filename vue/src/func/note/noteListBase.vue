<template>
  <div>
    <!-- 1.笔记列表区域 todo bug 还必须要这个。。。-->
    <div class="yinxList">
    </div>
    <div class="content clearfix">
      <div class="nodescroll">
        <!-- 笔记列表-------------->
        <!-- 笔记条数和选项 -->
        <slot name="noteBookInfo"></slot>

        <!--搜索入口-->
        <el-input
          placeholder="搜索"
          v-model="searchValue"
          clearable>
        </el-input>
        <!--展示笔记的条数 以及排序方式-->
        <div class="noteNumbers clearfix">
          <div class="yinxnum">
            <slot name="noteCount"></slot>
            条笔记
          </div>
          <div class="select" @click.stop="sortClick">
            <span>选项</span>
          </div>
          <!--选项列表-->
          <yxSelectSort @hideSelectSort="sortClick"
                        v-show="isSortShow"
                        @mouseleave.native="mouseleave"
                        ></yxSelectSort>
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
      isSortShow: false,
      searchValue:''
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
    // 鼠标移出时 显示消失
    mouseleave() {
      this.isSortShow = false
    },
  },
  watch: {
    $route() {
      // console.log("noteListBase 中打印路由发生了变化");
    },
    searchValue(searchValue) {
      //  1.根据内容进行全局搜索,并高亮返回  2.点击列表可进行预览 3.点击笔记 进行修改
      console.log("搜索字段 ", searchValue);
      let queryData = {
        "query": {
          "bool": {
            "must": [ //排除逻辑删除的笔记
              {"match": {
                  "status": false
                }}
            ],
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
          if (result.length > 0) {
            // 封装 currentNotes  直接利用查询到的数据进行封装note
            let searchNotes = []
            result.forEach(item => {
              // 覆盖
              if( item.highlight){
                item._source.title = item.highlight.title ? item.highlight.title[0] : item._source.title;
                item._source.content = item.highlight.content ? item.highlight.content[0] : item._source.content;
                item._source.id = item._id
                searchNotes.push(item._source)
              }
            })
            this.$store.state.noteModule.searchNotesList = searchNotes
            // 把搜索结果显示
            this.$router.push({
              name: 'searchResultList',
              params: {
                notes: JSON.stringify(searchNotes),
                noteBookTagName: "搜索笔记"
              }
            })
            // 2.初始化 笔记内容为 排序的第一个
            this.$router.push({
              name: 'searchResultItem',
              params: {
                note: JSON.stringify(searchNotes[0]),
                index: 0
              }
            })
          }
        });
      }
      // 字段为空时清空
      else {
        this.$store.state.noteModule.searchNotesList = []
        this.$router.push({
          name: 'searchResultItem',
          params: {
            note: '',
            index: 0
          }
        })
      }
    },
  },

}
</script>

<style scoped>


</style>
