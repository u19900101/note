export default {

  //第几阶段笔记本列表显示和隐藏
  noteBookShow({state, commit, rootState}) {
   commit('noteBookShow', rootState)
  },
  enterNoteBook({state, commit, rootState},currentNoteBook) {
   commit('enterNoteBook', {rootState,currentNoteBook})
  },
  changeSort({state, commit, rootState},sortWay) {
   commit('changeSort', {rootState,sortWay})
  },
}
