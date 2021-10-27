import mutations from './mutations'
import actions from './actions'
import getters from './getters'

export default {
  namespaced:true,  //开启命名空间
  state: {
    notes:[], // 所有的笔记列表
    currentNoteList: [], // 当前的所有笔记
    currentNote:{
      noteId: '', //编辑的Id
      title: '',  //标题
      content: '', //内容
      pid: '', // noteContent对象的pid
    }, //组件展示的数据对象
    isNotesShow: true, //快捷方式进入详情笔记本,笔记本信息列表隐藏

    currentIndex:0,  // 笔记在当前列表中的index值  用于删除时，快速定位，更新列表
    currentNoteBookName: '',

   /* ************搜索*************/
    isSearchNoteListShow: false, // 展示搜索列表 和 普通列表
    isTitleEditMode: true,  // 当前的笔记的标题是否处于编辑状态
    isContentEditMode: true,  // 当前的笔记的内容是否处于编辑状态

    searchNotesList: [],  //为搜索的结果单独开启空间保存
    searchNote: {},        // 展示的当前搜素结果
    /*************收藏*************/
    starNoteList: [],


  },
  mutations,
  actions,
  getters
}
