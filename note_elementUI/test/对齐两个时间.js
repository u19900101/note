let a = ['2021-01-07','2021-01-08','2021-01-11','2021-01-21','2021-01-31']
let b = ['2021-01-01','2021-01-03','2021-01-17','2021-01-21','2021-01-28']
a = initArr(a)
b = initArr(b)
let minDay = a[0]
let dA = 0,dB = 0
if(new Date(a[0]) - new Date(b[0]) > 0){
    minDay = b[0]
    dA = (new Date(a[0]) - new Date(b[0]))/86400000
}else {
    dB = (new Date(b[0]) - new Date(a[0]))/86400000
}
let maxDay = new Date(a[a.length -1]) - new Date(b[b.length -1]) > 0 ? a[a.length -1] : b[b.length -1]
let dDay = (new Date(maxDay) - new Date(minDay))/86400000

for (let i = 0; i < dDay; i++) {
    console.log(a[i-dA],b[i-dB]);
}
// console.log(minDay,maxDay,dDay)
function initArr(arr){
    let dateArr = arr
    let curday = dateArr[0]
    for (let i = 0; i < dateArr.length - 1; i++) {
        /*补全缺失的日期*/
        let nextDay = dateArr[i + 1]
        let cd = new Date(curday)
        let nd = new Date(nextDay)
        let detalDay = (nd - cd) / 86400000

        /*插入缺失的天数  */
        if (detalDay > 1) {
            for (let j = 0; j < detalDay - 1; j++) {
                let temp = new Date(cd)
                let dateTime = new Date(temp.setDate(cd.getDate() + j + 1));
                dateArr.splice(i + 1 + j, 0, formatDate(dateTime))
            }
            i += detalDay - 1
        }
        curday = nextDay
    }
    return arr
}
function formatDate(date) {
    let month = '' + (date.getMonth() + 1),
        day = '' + date.getDate(),
        year = date.getFullYear();
    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [year, month, day].join('-');
}