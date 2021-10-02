export default {

  noteTagShow({ state, commit, rootState }){
    state.isTagNotesShow = true;
    rootState.noteBookModule.isNoteBooksShow = false;
    rootState.noteModule.isNotesShow = false;
  },
}
