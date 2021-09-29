import {noteEditChange} from "../../server";

let dayjs = require('dayjs');
dayjs().format();

export default {


  getNotes({state, commit, rootState}, data) {
    commit('notesSuccess', {rootState, data})
  },
  noteShow({state, commit, rootState}) {
    // todo commit?
    //第几阶段笔记本列表显示和隐藏
    rootState.noteBookModule.isNoteBooksShow = false;
    rootState.noteBookModule.currentNoteBookName = false;
    rootState.tagModule.isTagNotesShow = false;
    state.isNotesShow = true;
    // 显示所有的的笔记
    state.currentNotes = state.notes;

  },
  // 同步 title和textarea内容
  noteEditChange({state, commit, rootState}, params) {
    // 修改本地的显示
    state.notes.forEach(item => {
      if (item.id == params.id) {
        item.title = params.title;
        item.content = params.content;
      }
    });
  },
}
