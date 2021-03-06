export default {

  // 请求成功,关闭loadding动画
  closeLoadding(state) {
    state.loadingState = false;
  },




  // 修改state,保存标签
  saveLabel(state, params) {
    state.notes.forEach(item => {
      if (item === params.obj) {
        item.label.push(params.label)
      }
    })
  },

  // 删除tag保存的标签
  delTaglabel(state, params) {
    state.notes.forEach(item => {
      if (item === params.obj) {
        item.label.splice(params.index, 1);
      }
    })
  },

  //添加快捷方式
  addkJHander(state, {obj}) {
    state.notes.forEach(item => {
      if (item === obj) {
        item.shortcut = !item.shortcut
      }
    })
  },


  // 将搜索到的列表集合同步到vuex中
  savefilterNote(state, params) {
    state.findNotesList = params.obj;
  },

  // 搜索框显示隐藏
  searchShow(state) {
    state.searchBox = true;
    state.quickShow = state.yinListopation = state.noteBookShow = state.noteTagState = false;
  },
  //回Home页,搜索框隐藏, 搜索关键字清空
  searchNone(state) {
    state.searchBox = false;
    state.searchValue = '';
    state.noteBookBg = -1;   // 回Home页,让笔记本bg-color为未选中
  },
  // 隐藏搜框显示
  hideSearchShow(state) {
    state.searchBox = false;
  },
  //未搜索到笔记
  isNot404False(state) {
    state.Not404 = false;
  },
  //搜索到笔记
  isNot404Yes(state) {
    state.Not404 = true;
  },

  //收藏组件显示和隐藏
  startShow(state) {
    state.quickShow = !state.quickShow;
    state.yinListopation = state.quickShow;
    // : 加过渡动画
    state.noteBookShow = false;
    state.noteTagState = false;
    state.shareState = false;
  },

  //点击快捷方式的详细信息,隐藏Home组件笔记列表
  noteListshow(state) {
    state.notesShow = false;
  },
  // 让笔记本列表盒模型显示出来
  noteListTrue(state) {
    state.notesShow = true;
    state.yinxdetWidth = false;
  },


  // 删除快捷方式
  delquickHander(state, {obj}) {
    state.notes.forEach(item => {
      if (item === obj) {
        item.shortcut = false;
      }
    })
  },

  // 修改笔记内容详情的margin-left 设置为0
  yinLeftHander(state) {
    state.yinxdetWidth = true;
  },

  //Home组件展开 显示完成按钮
  openHander(state) {
    state.unfoldShow = true;
  },
  //Home组件收起, 显示展开图标   // 隐藏笔记信息组件
  closeHander(state) {
    state.unfoldShow = false;
    state.information = false;
    state.navState = 1;
  },

  //当点击textarea 和 title区域,隐藏快捷方式栏
  closeQuickbox(state) {
    if (state.quickShow || state.noteBookShow || state.noteTagState || state.shareState) {
      state.quickShow = false;
      state.noteBookShow = false;
      state.yinListopation = false;
      state.noteTagState = false;
      state.shareState = false;
    }
    state.navState = 1;
  },




  // Notebook组件
  // 让删除笔记本二次确认组件显示
  deleteNoteBook(state, params) {
    state.delNoteBookShow = true;
    state.delNoteBookObj = params.obj;
  },
  // 取消删除
  cancelDelete(state) {
    state.delNoteBookShow = false;
    state.delNoteBookObj = {}
  },
  //确定删除
  isDeleteBook(state) {
    state.tipsuccessState = false;
    state.notes = state.notes.filter(item => item !== state.delNoteBookObj);
    state.notes = this.getters.tBnotes;
    state.delNoteBookShow = false;
    state.tipsuccessInfo = {
      objname: state.delNoteBookObj.title,
      tip: '已删除笔记本'
    }
  },


  // Home键,清空第几阶段展示的笔记列表
  noteBookList(state) {
    state.joinNoteList = [];
    state.joinNoteBookObj = {};
  },
  //Home组件 前往当前笔记本
  QWNOTEBOOK(state, params) {
    state.noteBookBg = params.obj.id; //同步笔记本的id
    state.joinNoteBookObj = params.obj;
    state.joinNoteList = params.obj.children;
    if (state.isJoinNotesTagList) {
      // console.log('进入笔记本列表,标签组件显示呢')
      state.isJoinNotesTagList = false;
    }
  },

  // 搜索关键字同步到vuex
  searchHander(state, params) {
    state.searchValue = params.text;
  },
  // 清空搜索关键字
  clearHanderValue(state) {
    state.searchValue = '';
  },
  //笔记列表删除完了
  deleteAll(state) {
    state.notelistNumber = true;
  },
  // 笔记列表中不为空
  showNoteList(state) {
    state.notelistNumber = false;
  },


  //创建笔记本组件显示
  createHanderShow(state) {
    state.createBookShow = true;
  },
  // 创建笔记本组件隐藏
  createHanderNone(state) {
    state.createBookShow = false;
  },
  //创建笔记本同步数据
  addBooks(state, params) {
    state.notes.push(params.obj);
  },
  // 笔记本背景颜色下标
  notebookState(state, index) {
    state.noteBookBg = index;
  },

  //组件显示的笔记信息对象
  infoHander(state, params) {
    state.noteInfos = params.obj;
    state.information = true; //展示的组件显示出来
  },
  // 保存笔记信息的 url 作者信息更改
  saveHander(state, params) {
    state.notes.forEach(item => {
      if (item.id == params.id) {
        item.url = params.url;
        item.author = params.author;
      }
    });
    state.information = false;
  },



  /***********同步数据日期****************/

  /*---------七天之内 一天之上的日期 ----------*/
  sevendays(state, params) {
    state.noteModule.notes.forEach(item => {
      if (item === params.obj) {
        item.createTimeAlias = params.time;
      }
    })
  },
  /***************************/


  // 点击任何移除,收起选项下拉框
  closeSelectHander(state) {
    state.selectDown = false;
  },

  // 修改vuex showTextState状态,作为Home组件vuex过滤条件
  changeShowHander(state, params) {
    state.showTextState = params.st;
  },

  // 提示组件2s之后隐藏提示 [successInfo组件提交commit]
  closetipsuccess(state) {
    state.tipsuccessState = false;
  },
  // 显示 successinfo提示
  successShow(state) {
    state.tipsuccessState = true;
  },


  /*-----------------将Home组件中展示的数据,同步到vuex状态-----------------------------*/
  noteconten(state, obj) {
    state.noteContent = obj;
  },


  // 点击任意一处,关闭通知提醒弹窗
  closeRemin(state) {
    state.setRemin = false;
    state.changeRemin = false;
    state.undoRemin = false;
    state.setTimersRemin = false;
    state.copyurlNotes = false;
  },
  // 设置提醒,同步notes数据
  setRemin(state, params) {
    state.notes.forEach(item => {
      if (item === params.obj) {
        item.remind = true;
      }
    });
    // 通知我组件加载..
    state.setRemin = true;
  },

  // 通知我,加载时间组件
  reminMehander(state) {
    state.setRemin = false;
    state.setTimersRemin = true;
  },
  // 设置通知时间
  setTimes(state, params) {
    state.notes.forEach(item => {
      if (item === params.obj) {
        item.remindTime = params.remint;
      }
    });
    state.setTimersRemin = false; //iview时间组件隐藏
  },

  // 设置提醒,但没有设置时间的组件
  changeReminComputed(state) {
    state.changeRemin = true;
  },

  // ChangeRemin组件标记完成同步状态
  TAGSUCCESS(state, params) {
    state.notes.forEach(item => {
      if (item === params.obj) {
        item.completeState = true;
      }
    });
    state.changeRemin = false;
  },

  // 清除提醒
  clearReminHander(state, params) {
    state.notes.forEach(item => {
      if (item === params.obj) {
        item.remind = false; //清除提醒
        item.remindTime = ''; //提醒时间清空
      }
    });
    state.changeRemin = false;
  },

  // 修改日期,iview日期组件加载..
  changeDate(state) {
    state.changeRemin = false;
    state.setTimersRemin = true;
  },

  // 标记完成功能组件
  UndoReminHander(state) {
    state.undoRemin = true;
  },

  // UndoRemin组件 ,撤销标记完成
  undoReminHander(state, params) {
    state.notes.forEach(item => {
      if (item === params.obj) {
        item.completeState = false; //标记是否完成
      }
    });
    state.undoRemin = false; //撤销组件隐藏
  },

  // 已标记完成,清除提醒,应该把当前对象的标记完成取消
  clearUndoHander(state, params) {
    let n = this.getters.getParamsObj(params.obj);
    n.remind = false; //是否被提醒
    n.completeState = false; //是否未完成状态
    n.remindTime = ''; //提醒的时间
    state.undoRemin = false;
  },
  // UndoRemin 修改日期
  UndochangeDate(state) {
    state.setTimersRemin = true;
    state.undoRemin = false;
  },

  // 复制笔记链接
  moreHander(state) {
    state.copyurlNotes = !state.copyurlNotes;
  },

  //标签组件加载
  noteTagShow(state) {
    state.noteTagState = !state.noteTagState;
    state.yinListopation = state.noteTagState;
    if (!state.noteTagState) {
      state.tagnotes = []
    }
    // 过渡动画
    state.noteBookShow = false;
    state.quickShow = false;
    state.shareState = false;
  },

  // 进入标签,找到当前标签名字的笔记对象
  joinTagNotes(state, tag) {
    state.tagNoteBookName = tag.title;
    // 先清空上一个标签的笔记列表
    state.tagNotes = state.joinNoteList = [];
    state.notes.forEach(item => {
      if (item.tagList.length >= 1) {
        let bl = item.tagList.some(el => el === tag.id);
        if (bl) {
          state.tagNotes.push(item)
        }
      }
    });
    //透明度去除
    state.yinListopation = false;
    state.noteTagState = false;
    // 进入笔记列表
    state.isJoinNotesTagList = true;
  },

  // 清空标签笔记列表
  clearTagList(state) {
    state.tagnotes = [];
  },
  // 去除标签组件在Home list展示
  closeTagShow(state) {
    state.isJoinNotesTagList = false;
    state.tagNoteBookName = '';
  },
  // 去除第几阶段笔记信息
  closeJdShowTag(state) {
    state.joinNoteBookObj = {};
  },

  // 笔记列表删除完了
  tagDeleteAll(state) {
    state.noteFindTagList = true;
  },

  // 修改标签内容
  changeEditHander(state, params) {
    state.notes.forEach(item => {
      if (item.label.length > 0) {
        item.label.forEach((el, index) => {
          if (el === params.tag) {
            item.label.splice(index, 1, params.val)
          }
        })
      }
    });
  },

  //删除标签同步到vuex名字,供删除组件显示数据
  deleteTagHander(state, tag) {
    state.deleteTagName = tag;
    state.deleteTagComponentsShow = true;
    state.noteTagState = false;
  },

  // 取消修改标签
  calcelDelTag(state) {
    state.deleteTagComponentsShow = false;
    state.noteTagState = true;
    state.deleteTagName = '';
  },
  // 确定删除当前标签
  deleteTags(state) {
    state.notes.forEach(item => {
      let label = item.label;
      if (label.length > 0) {
        label.forEach((el, i) => {
          if (el === state.deleteTagName) {
            label.splice(i, 1)
          }
        })
      }
    });
    state.deleteTagComponentsShow = false;
    state.noteTagState = true;
  },
  // 将标签列表同步到vuex状态,根据这个的长度,判断出标签列表是否删除完了
  setAllTags(state, tags) {
    state.allTags = tags;
  },

  /*********deleteNoteState 删除笔记本时状态************/
  deleteNoteState(state, s) {
    state.deleteNotesState = s;
  },

  //分享组件加载..
  shareBlock(state) {
    state.shareState = !state.shareState;
    state.yinListopation = state.shareState;

    //关闭其他组件的显示
    if (state.quickShow || state.noteBookShow || state.noteTagState) {
      state.quickShow = state.noteBookShow = state.noteTagState = false;
    }
  },

  // 分享数据
  showShakeMessage(state, params) {
    state.messageDate = params.obj;
    state.messageShow = true;
  },
  // 消息弹窗状态
  closeMessageHander(state) {
    state.messageShow = false;
  },

  //修改navState
  changeNavState(state, index) {
    state.navState = index;
  }
}
