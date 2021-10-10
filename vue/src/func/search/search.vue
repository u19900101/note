<template>
  <div class="youbian clearfix">
    <!--搜索笔记-------------------------------------------------------------------->
    <div class="searchNote">
      <div class="searchChild">
        <input type="text" class="searchValue" placeholder="搜索笔记"
               v-model="searchValue"
               @keydown.enter="searchDown"
               v-focus>
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

      <router-view/>
    </div>
  </div>
</template>

<script>


let dayjs = require('dayjs');
dayjs().format();


export default {
  name: "home",
  components: {},
  data() {
    return {
      searchValue: '',
    }
  },
  methods: {
    clearSearchVal() {
      this.searchValue = '';
    },

  },

  // 计算属性
  computed: {},

  watch: {
    $route() {

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
          if (result.length > 0) {
            // 封装 currentNotes  直接利用查询到的数据进行封装note
            let searchNotes = []
            result.forEach(item => {
              // 覆盖
              item._source.title = item.highlight.title ? item.highlight.title[0] : item._source.title;
              item._source.content = item.highlight.content ? item.highlight.content[0] : item._source.content;
              item._source.id = item._id
              searchNotes.push(item._source)
            })

            this.$store.state.noteModule.currentNotes = searchNotes

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
              name: 'note3',
              params: {
                note: JSON.stringify(searchNotes[0]),
                index: 0
              }
            })
          }
        });
      }
    },
  },
  //自定义指令
  directives: {
    focus: {
      inserted: function (el) {
        el.focus();
      }
    }
  }

}


</script>

<style>

</style>
