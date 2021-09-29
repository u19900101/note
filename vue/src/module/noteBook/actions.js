export default {
  getNotebooks(state, data) {
    state.commit('noteBooksSuccess', data)
  },
  //第几阶段笔记本列表显示和隐藏
  noteBookShow({state, commit, rootState}) {
   commit('noteBookShow', rootState)
  },
  enterNoteBook({state, commit, rootState},currentNoteBook) {
   commit('enterNoteBook', {rootState,currentNoteBook})
  },
}
