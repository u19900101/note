import mutations from './mutations'
import actions from './actions'
import getters from './getters'

export default {
  namespaced:true,  //开启命名空间
  state: {
    noteBooks: [],  //所有笔记本列表
    currentNoteBookNoteList:[],


    isNoteBooksShow: false, //笔记本组件显示和隐藏 Notebook 组件
    currentNoteBook: {
      name: '所有笔记', // 当前笔记本的名称，也有可能是当前 tag的名称
      id: 0             // 当前笔记本的id，搜索笔记时有用
    },

    isSortShow: false,
    noteListSortWay: 'createLatest', //笔记本列表排序方式,默认 创建日期(最新优先)

    delNoteBookShow: false,  //删除笔记本组件显隐
    delNoteBookObj: {}, // 要删除的笔记本
  },
  mutations,
  actions,
  getters
}
