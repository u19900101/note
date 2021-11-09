//小工具类
import Vue from 'vue'
import el from "element-ui/src/locale/lang/el";
export default {
    install(Vue){
        Vue.prototype.tool = {
            getfirstLevelId,
            getDateTimes,
            sortWay,
            addNoteCount
        }
    }
}

/**获取时间的别名**/
let dayjs = require('dayjs');
dayjs().format();
export function getDateTimes(data,sortWay){
    //在这里计算时间
    let newTime = dayjs().unix();
    data.forEach(item => {
        // 第三种方式 "2014-07-10 10:21:12"
        //时间对象
        let date = new Date(item.createTime)
        let timeAliasType = 'createTimeAlias'
        if(sortWay.updateTime) {
            date = new Date(item.updateTime);
            timeAliasType = 'updateTimeAlias'
        }

        let strTime = date.getTime()/1000; //转换成时
        let diffTime = newTime - strTime;   // 获取到时间差,计算 小时 分钟 天 周

        //创建时间为7天之内的笔记对象
        let day = parseInt(diffTime / 86400);
        //对7天以内的笔记添加别名字段
        if(day <= 7 && day >= 1){
            item[timeAliasType] = day + ' 天前';
        }
        else if(day >= 0 && day < 1){
            // 计算小时 1 <= h < 24
            let hours = parseInt(diffTime / 3600);
            if( 1 <= hours && hours < 24){
                item[timeAliasType] = hours + ' 小时前';
            }
            // 划分分钟
            if( 0 <= hours && hours < 1){
                let minutes = parseInt(diffTime / 60);
                item[timeAliasType] = minutes + ' 分钟前';
                // 划分秒 > 5s || < 5s 刚刚
                if(minutes < 1){
                    let seconds = diffTime;
                    if(seconds > 5){
                        // 大于5 显示几秒前,否则显示刚刚
                        item[timeAliasType] = seconds + ' 秒前';
                    }else {
                        item[timeAliasType] = ' 刚刚';
                    }
                }
            }
        }
    });
}

/* 递归获取el-tree的一级节点id*/
function getfirstLevelId(node) {
    if (node.level != 1) {
        return this.getfirstLevelId(node.parent)
    } else {
        return node.data.id
    }
}

/**排序*/
function sortWay(notes,sortWay){

    //创建时间
    if(sortWay.createTime){
        notes.sort(function(a,b){
            return getTime(b.createTime) - getTime(a.createTime);
        });
    } // 更新时间
    else if(sortWay.updateTime){
        notes.sort(function(a,b){
            return getTime(b.updateTime) - getTime(a.updateTime);
        });
    } //地点 todo

    // 实时同步笔记列表时间
   getDateTimes(notes,sortWay)

}

function getTime(strTime){
    // 第三种方式 "2014-07-10 10:21:12"
    let date = new Date(strTime); //时间对象
    let str = date.getTime(); //转换成时间戳
    // str = str / 1000;
    return str
}

/**给笔记本名称后封装数量的显示*/
//遍历树 更新全部的笔记数量
function addNoteCount(treeData) {
    treeData.forEach((n) =>{
        n.title += ' (' + n.noteCount + ')'
        if(n.children.length > 0 ) this.addNoteCount(n.children)
    })
}

