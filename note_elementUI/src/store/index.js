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
    expandedKeyId: '',//默认展开的节点id

    /**笔记本**/
    noteBooks: [],  //所有笔记本列表
    noteBooksTree: [],// 树形封装
    noteBooksTreePure: [],// 树形封装
    currentNoteBook: {
        title: '所有笔记', // 当前笔记本的名称，也有可能是当前 tag的名称
        id: 0             // 当前笔记本的id，搜索笔记时有用
    },


    /**笔记**/
    notes: [], // 所有的笔记列表
    currentNoteList: [], // 当前的所有笔记
    currentNoteBookNoteList: [], // 当前的笔记本中所有笔记
    currentNote: {
        noteId: '', //编辑的Id
        title: '',  //标题
        content: '', //内容
        pid: '', // noteContent对象的pid
        wastepaper: false
    }, //组件展示的数据对象
    currentIndex: 0,  // 笔记在当前列表中的index值  用于删除时，快速定位，更新列表
    currentNoteBookName: '',

    /*************搜索*************/
    isSearchMode: false, // 展示搜索列表 和 普通列表
    isTitleEditMode: true,  // 当前的笔记的标题是否处于编辑状态
    isContentEditMode: true,  // 当前的笔记的内容是否处于编辑状态

    searchHistroy: [],  //为搜索历史  只保存最近的10条搜索记录

    /*************收藏*************/
    starNotesList: [],

    /*************标签*************/
    tags: [],     //所有标签列表
    tagsTree: [], //标签树
    tagsTreePure: [], //标签树
    /*************排序*************/
    sortWay: {},

    /*************废纸篓*************/
    wastepaperNotesList: [],

    /*************文件*************/
    fileMode: false,  //文件模式视图控制
    fileList: [],
    currentImageList: [],
    currentImagesCount: 0,  //当前图片类的数量
    currentImage: {},//当前图片
    currentImageUrl: "",//当前图片的url
    currentImageUrlList: [],//当前图片的urlList
    starImageList:[],//收藏的图片
    wastepaperPictureList: [],//回收站中的图片
    imageTags:[], //图片标签
    imageTagsTree:[], //图片标签
    /*************页面显示控制*************/
    listAndNoteShow: true,  //显示列表区和笔记区
    tableData: [],           //封装笔记本和标签展示区
    listTitle : '',         //列表区的名称
}

//创建并暴露store
export default new Vuex.Store({
    actions,
    mutations,
    state
})