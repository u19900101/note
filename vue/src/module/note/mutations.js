export default {





  moveNotes(state,params){
    // 从哪个数组中移出的
    params.obj.pid = params.gid;

    state.tipsuccessState = false;

    // @ 从移出的笔记本中删除这条笔记
    state.notes.forEach(item => {
      if(item.id === params.pid){
        item.children.forEach((el,index) => {
          if(el === params.obj){
            item.children.splice(index,1)
          }
        })
      }

      //@ 将笔记移动到哪个笔记本中
      else if(item.id === params.gid){
        item.children.push(params.obj)
      }
    });

    // @ 提示框组件数据信息
    state.tipsuccessInfo = {
      objname:params.moveObj,
      tip:'已将笔记移动到'
    }
  },
  //删除笔记Home提交
  delClickHander(state,params){
    state.delNoteInfo = params.obj; //将要删除的对象同步到state状态delNoteInfo,让删除的组件显示这个对象的信息
    state.delnoteNextId = params.id;
    state.noteDelShow = true;
  },
  //取消删除 delete组件commit
  cancelHander(state){
    state.noteDelShow = false;
    state.delNoteInfo = {};
  },
  //确定删除
  isdelHander(state){
    //保持 notes和notes是同步的数据状态
    state.notes.forEach(item => {
      if(item.id == state.delNoteInfo.pid){
        item.children = item.children.filter(el => el !== state.delNoteInfo)
      }
    });
    // 搜索过滤后的数据删除当中要删除的对象
    if(state.findNotesList.length > 0){
      state.findNotesList = state.findNotesList.filter(item => item !== state.delNoteInfo);
    }
    //在详情笔记本列表中删除对象
    if(state.joinNoteList.length >= 0){
      state.joinNoteList = state.joinNoteList.filter(item => item !== state.delNoteInfo);
    }
    // 标签组件列表删除对象
    if(state.tagnotes.length > 0){
      state.tagnotes = state.tagnotes.filter(item => item !== state.delNoteInfo);
    }

    //将删除的对象同步到tipsuccessInfo中
    state.tipsuccessInfo = {
      objname:state.delNoteInfo.title,
      tip:'已成功删除笔记',
    };
    state.tipsuccessState = false; //先隐藏,再显示
    //通过调用getters中的方法,将notes第几阶段中的每条笔记同步到notes
    state.notes = this.getters.tBnotes;
    state.noteDelShow = false;
  },
  // 新建笔记 完成
  addNotes(state,params){
    state.notes.forEach(item => {
      if(item.id == params.id){
        item.children.unshift(params.obj);
      }
    });
    state.notes = this.getters.tBnotes;

    // 处理情况,如果当前在笔记本列表中新建笔记的话,如果新建完成,应该回到Home列表笔记本,展示出所有的笔记数据
    // 并且笔记本的bg为不选中的状态
    state.joinNoteList = [];
    state.joinNoteBookObj = {};
    state.noteBookBg = -1;
    state.quickShow = state.noteBookShow = state.noteTagState = state.yinListopation = false;
  },
}
