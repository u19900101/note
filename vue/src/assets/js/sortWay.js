// 对传入的笔记列表，根据指定的方式进行排序进行排序
export default {
  install(Vue){
    // 也可以使用这种注册全局函数
    // Vue.prototype.sortWay = () => sortWay()
    Vue.prototype.sortWay = sortWay
    // 写对象可定义多个
    // } Vue.prototype.sortWay = {
    //   sortWay,
    // }
  }
}

function sortWay(notes,noteListSortWay){

  //创建日期(最新优先) 更新日期(最新优先)
  if(noteListSortWay === 'createLatest' || noteListSortWay === 'updataEarliest'){
   notes.sort(function(a,b){
      return getTime(b.createTime) - getTime(a.createTime);
    });
  }

  //创建日期(最早优先) 更新日期(最新优先)
  else if(noteListSortWay === 'createEarliest' || noteListSortWay === 'updataLatest'){
   notes.sort(function(a,b){
      return getTime(a.createTime) - getTime(b.createTime);
    });
  }

  // 标题排序 (升序) 最短的排列越靠前
  else if(noteListSortWay === 'titleOrder'){
     notes.sort(function(a,b){
        return b.title.length - a.title.length;
     })
  }

  else if(noteListSortWay === 'titleAscending'){
    notes.sort(function(a,b){
      return a.title.length - b.title.length;
    })
  }

  // 实时同步笔记列表时间
  this.getDateTimes.getDateTimes.call(this,notes)

}

function getTime(strTime){
  // 第三种方式 "2014-07-10 10:21:12"
  let date = new Date(strTime); //时间对象
  let str = date.getTime(); //转换成时间戳
  // str = str / 1000;
  return str
}


