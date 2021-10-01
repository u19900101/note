import mutations from './mutations'
import actions from './actions'
import getters from './getters'

export default {
  state: {
    notes:[], // 所有的笔记列表
    currentNotes: [],
    noteContent:{}, //Home组件展示的数据对象
    isNotesShow: true, //快捷方式进入详情笔记本,笔记本信息列表隐藏
    currentNoteToShow:{},
    noteId: '', //编辑的Id
    title: '',  //标题
    content: '', //内容


    Pid: '', // noteContent对象的pid
    noteBookName: '',
  },
  mutations,
  actions,
  getters
}
