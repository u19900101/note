/*获取当前天数所在的周数  */
function getWeekth(currentDay) {
    // date = formatTimebytype(date, 'yyyy-MM-dd');//将日期转换成yyyy-mm-dd格式
    currentDay = new Date(currentDay);
    let firstDay = new Date(currentDay.getFullYear(), 0, 1);
    let day2 = firstDay.getDay();

    let day1 = currentDay.getDay();
    if (day1 == 0) day1 = 7;

    /*以周一作为一周的第一天*/
    let deltDay = 0
    if (this.firstDayOfWeek == 1) {
        if (day2 == 0) day2 = 7;
        deltDay = day2 - 1
    } else {
        deltDay = day2
    }
    let d = Math.round((currentDay.getTime() - firstDay.getTime() + deltDay * (24 * 60 * 60 * 1000)) / 86400000);
    return Math.ceil(d / 7);
}

