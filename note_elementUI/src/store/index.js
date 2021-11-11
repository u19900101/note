//引入Vue核心库
import Vue from 'vue'
//引入Vuex
import Vuex from 'vuex'
//应用Vuex插件
Vue.use(Vuex)

//准备actions对象——响应组件中用户的动作
const actions = {}
//准备mutations对象——修改state中的数据
const mutations = {}
//准备state对象——保存具体的数据
const state = {
    /**导航栏**/
    currentNode: {},  //当前拖拽的节点

    /**笔记本**/
    noteBooks: [],  //所有笔记本列表
    noteBooksTree:[],// 树形封装
    currentNoteBookNoteList:[],


    isNoteBooksShow: false, //笔记本组件显示和隐藏 Notebook 组件
    currentNoteBook: {
        title: '所有笔记', // 当前笔记本的名称，也有可能是当前 tag的名称
        id: 0             // 当前笔记本的id，搜索笔记时有用
    },

    isSortShow: false,
    noteListSortWay: 'createLatest', //笔记本列表排序方式,默认 创建日期(最新优先)

    delNoteBookShow: false,  //删除笔记本组件显隐
    delNoteBookObj: {}, // 要删除的笔记本

    /**笔记**/
    notes:[], // 所有的笔记列表
    currentNoteList: [], // 当前的所有笔记
    currentNote:{
        noteId: '', //编辑的Id
        title: '',  //标题
        content: '', //内容
        pid: '', // noteContent对象的pid
        wastepaper: false
    }, //组件展示的数据对象
    isNotesShow: true, //快捷方式进入详情笔记本,笔记本信息列表隐藏

    currentIndex:0,  // 笔记在当前列表中的index值  用于删除时，快速定位，更新列表
    currentNoteBookName: '',

    /*************搜索*************/
    isSearchNoteListShow: false, // 展示搜索列表 和 普通列表
    isTitleEditMode: true,  // 当前的笔记的标题是否处于编辑状态
    isContentEditMode: true,  // 当前的笔记的内容是否处于编辑状态

    searchNotesList: [],  //为搜索的结果单独开启空间保存
    searchNote: {},        // 展示的当前搜素结果
    /*************收藏*************/
    starNotesList: [],

    /*************标签*************/
    isTagNotesShow: false, //标签笔记是否显示
    tags:[],     //所有标签列表
    tagsTree:[], //标签树
    // tagNotesList: [],
    /*************排序*************/
    sortWay:{},

    /*************废纸篓*************/
    wastepaperNotesList:[]
}

//创建并暴露store
export default new Vuex.Store({
    actions,
    mutations,
    state
})