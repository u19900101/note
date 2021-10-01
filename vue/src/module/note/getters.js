export default {

  getNoteShow: (state, getters, rootState) =>  () => {

    // 笔记本列表显示和隐藏
    rootState.noteBookModule.isNoteBooksShow = false;
    rootState.noteBookModule.currentNoteBookName = false;
    rootState.tagModule.isTagNotesShow = false;
    state.isNotesShow = true;
    // 显示所有的的笔记
    state.currentNotes = state.notes;

  },
  getNotes: (state, getters, rootState) =>  (data) => {
    rootState.noteModule.notes = data;
  },
  // 同步 title和textarea内容
  noteEditChange: (state, getters, rootState) =>  (params) => {
    // 修改本地的显示
    state.notes.forEach(item => {
      if (item.id == params.id) {
        item.title = params.title;
        item.content = params.content;
      }
    });
  },


  // vuex中的notes有没有快捷方式为true的,如果有返回true,没有返回false
  // todo bug
  shortcutHander(Store){
     return Store.notes.some(item => item.shortcut);
  },

  // 遍历state.notes数据,找到和Home组件相同的对象
  // getParamsObj:(Store) => (obj) => {
  //    return Store.notes.filter(item => item === obj)[0];
  // }
}
