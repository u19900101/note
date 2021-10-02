export default {

  noteBookShow(state,rootState){
    //第几阶段笔记本列表显示和隐藏
    state.isNoteBooksShow = true;
    rootState.tagModule.isTagNotesShow = false;
    rootState.noteModule.isNotesShow = false;

    state.shareState = false;
    state.yinListopation = state.noteBookShow;
    // : 添加过渡动画
    state.quickShow = false; //quick components
  },
// 进入笔记本详细列表
  enterNoteBook(state, {rootState,currentNoteBook}) {
    state.currentNoteBook = currentNoteBook;
    state.currentNoteBookName = currentNoteBook.title;

    // 在 notes中查找该笔记本对应的笔记装填到 当前笔记列表中
    rootState.noteModule.currentNotes = rootState.noteModule.notes.filter(item => item.pid == currentNoteBook.id);
    // 展示 笔记列表
    rootState.noteModule.isNotesShow = true;
    state.isNoteBooksShow = false; //笔记本组件隐藏

    state.yinListopation = false;
  },
  // 选项列表显示
  isSortShow(state) {
    state.isSortShow = !state.isSortShow;
  },
  // 修改排序方式
  changeSort(state, {rootState,sortWay}) {
    // rootState.noteBookModule.noteListSortWay = sortWay;
    state.noteListSortWay = sortWay;
    state.isSortShow = false; //隐藏排序选项列表
  },
}
