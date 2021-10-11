import mutations from './mutations'
import actions from './actions'
import getters from './getters'

export default {
  state: {
    notes:[], // 所有的笔记列表
    currentNotes: [], // 当前的所有笔记  todo 性能提升 分离出 searchResultNotes
    noteContent:{}, //Home组件展示的数据对象
    isNotesShow: true, //快捷方式进入详情笔记本,笔记本信息列表隐藏
    currentNoteToShow:{},
    noteId: '', //编辑的Id
    title: '',  //标题
    content: '', //内容
    pid: '', // noteContent对象的pid
    currentNoteBookName: '',
    isSearchNoteShow: false, // 展示搜索笔记内容
    isSearchNoteListShow: false, // 展示搜索列表
    searchNotes: []
  },
  mutations,
  actions,
  getters
}
