export default {
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
