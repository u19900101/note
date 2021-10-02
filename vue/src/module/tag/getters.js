export default {

  initTags: (state, getters, rootState) =>  (data) => {
    rootState.tagModule.allTags = data;
  },
}
