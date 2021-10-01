export default {
  // 只能三个参数一起放，有点愚蠢  而且函数的格式一定要写对。。。
  getNoteListSortWay: (state, getters, rootState) => (sortWay) => {
    state.isSortShow = !state.isSortShow
    rootState.noteBookModule.noteListSortWay = sortWay
    return sortWay
  },


}
