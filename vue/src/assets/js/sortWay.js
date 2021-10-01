// 在路未变动的时候,先进行排序。
// 组件中调用该方式使用call(),使sortWay函数中的this指向当前组件的实例对象

import Vue from 'vue'
export function sortWay(notes){

  let sWay = this.$store.state.noteBookModule.noteListSortWay;

  //创建日期(最新优先) 更新日期(最新优先)
  // console.log("排序前 ",this.$store.state.noteModule.currentNotes);
  if(sWay === 'createLatest' || sWay === 'updataEarliest'){
   this.$store.state.noteModule.currentNotes.sort(function(a,b){
      return getTime(b.createTime) - getTime(a.createTime);
    });
  }

  //创建日期(最早优先) 更新日期(最新优先)
  else if(sWay === 'createEarliest' || sWay === 'updataLatest'){
   this.$store.state.noteModule.currentNotes.sort(function(a,b){
      return getTime(a.createTime) - getTime(b.createTime);
    });
  }

  // 标题排序 (升序) 最短的排列越靠前
  else if(sWay === 'titleOrder'){
     this.$store.state.noteModule.currentNotes.sort(function(a,b){
        return b.title.length - a.title.length;
     })
  }

  else if(sWay === 'titleAscending'){
    this.$store.state.noteModule.currentNotes.sort(function(a,b){
      return a.title.length - b.title.length;
    })
  }
  // console.log("排序后 ",this.$store.state.noteModule.currentNotes);
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
export default {
  install(Vue){
    Vue.prototype.sortWay = {
      sortWay,
    }
  }
}

