// console.log(format(new Date(),"yyyy-MM-dd"))
/*
function format(date, fmt="yyyy-MM-dd hh:mm:ss") {
    let o = {
        "M+": date.getMonth() + 1, //月份
        "d+": date.getDate(), //日
        "h+": date.getHours(), //小时
        "m+": date.getMinutes(), //分
        "s+": date.getSeconds(), //秒
        "q+": Math.floor((date.getMonth() + 3) / 3), //季度
        "S": date.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
    }
    for (let k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ?
                (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        }
    }
    return fmt;
}*/
let dayjs = require('dayjs');
let nowDate = dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss').split(" ")[0]
let arr = nowDate.split("-")
if(arr.length == 3){
    console.log(arr[0] + "年" + arr[1] + "月" +arr[2] + "日")
}

