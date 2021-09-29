export default {
  getTags(state,data){
    state.commit('tagsSuccess',data)
  },
  noteTagShow({ state, commit, rootState }){
    state.isTagNotesShow = true;
    rootState.noteBookModule.isNoteBooksShow = false;
    rootState.noteModule.isNotesShow = false;
  },
}
