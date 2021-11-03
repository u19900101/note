//小工具类
import Vue from 'vue'
export default {
    install(Vue){
        Vue.prototype.tool = {
            getfirstLevelId,
            getDateTimes,
        }
    }
}

/**获取时间的别名**/
let dayjs = require('dayjs');
dayjs().format();
export function getDateTimes(data){
    //在这里计算时间
    let newTime = dayjs().unix();
    data.forEach(item => {
        // 第三种方式 "2014-07-10 10:21:12"
        let date = new Date(item.createTime); //时间对象
        let strTime = date.getTime()/1000; //转换成时
        let diffTime = newTime - strTime;   // 获取到时间差,计算 小时 分钟 天 周

        //创建时间为7天之内的笔记对象
        let day = parseInt(diffTime / 86400);
        // console.log(day);
        if(day <= 7 && day >= 1){
            this.$store.commit('sevendays',{
                time:day + ' 天前',
                obj:item,
            })
        }
        else if(day >= 0 && day < 1){
            // 计算小时 1 <= h < 24
            let hours = parseInt(diffTime / 3600);
            if( 1 <= hours && hours < 24){
                this.$store.commit('sevendays',{
                    time:hours + ' 小时前',
                    obj:item,
                })
            }
            // 划分分钟
            if( 0 <= hours && hours < 1){
                let minutes = parseInt(diffTime / 60);
                this.$store.commit('sevendays',{
                    time:minutes + ' 分钟前',
                    obj:item,
                });
                // 划分秒 > 5s || < 5s 刚刚
                if(minutes < 1){
                    let seconds = diffTime;
                    if(seconds > 5){
                        // 大于5 显示几秒前,否则显示刚刚
                        this.$store.commit('sevendays',{
                            time:seconds + ' 秒前',
                            obj:item,
                        });
                    }else {
                        this.$store.commit('sevendays',{
                            time:"刚刚",
                            obj:item,
                        });
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