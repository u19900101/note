<template>
    <div style="height: 100%">
        <!--周数的显示-->
        <div class="weekNum" style="position: absolute;padding-left: 8px;top: 45px">
            <el-button @click="changeWeek" round size="mini" class="weekButton" style="margin-left: -7px;width: 25px">
                周
            </el-button>
            <div v-for="i in weekthArr"
                 style="font-size: 12px;"
                 :style="{height: $store.state.clientH*0.9*0.16 + 'px',
                 lineHeight: $store.state.clientH*0.9*0.16 + 'px'}">
                {{i}}
            </div>
        </div>
        <div class="calendar">
            <div class="banner" style=" height: 40px;text-align: center;">
                <div class="arrow-wrap arrow-wrap--left">
                    <div v-show="yearPaneVisible" class="arrow arrow--outer" @click="toPreYearRange"></div>
                    <div v-show="!yearPaneVisible" class="arrow arrow--outer" @click="toPreYear"></div>
                    <div v-show="datePaneVisible" class="arrow arrow--inner" @click="toPreMonth"></div>
                </div>

                <!-- banner locale -->
                <span class="bannerSpan" v-show="yearPaneVisible">{{ localeYearRange }}</span>
                <span class="bannerSpan" v-show="!yearPaneVisible" @click="showYearPane">{{ localeYear }}</span>
                <span class="bannerSpan" v-show="datePaneVisible" @click="showMonthPane">{{ localeMonth }}</span>
                <div class="arrow-wrap arrow-wrap--right">
                    <div v-show="datePaneVisible" class="arrow arrow--inner" @click="toNextMonth"></div>
                    <div v-show="!yearPaneVisible" class="arrow arrow--outer" @click="toNextYear"></div>
                    <div v-show="yearPaneVisible" class="arrow arrow--outer" @click="toNextYearRange"></div>
                </div>
            </div>
            <div v-show="datePaneVisible" class="pane pane--date" style="height: 90%">
                <!--星期-->
                <div class="week-text">
                    <div v-for="text in weekText" :key="text" class="date-item__week">
                        {{ text }}
                    </div>
                </div>
                <div style="height: 100%">
                    <!--每日div-->
                    <div v-for="(item, j) in dateArr"
                         :key="'date' + j"
                         class="date-item"
                         :style="{background: item.monthFlag === 1 && item.dayOfWeek == 0 ?'#FFFF00':(item.monthFlag === 1 && item.dayOfWeek == 6 ?'#00B050':'')}"
                         @click="_handleDateItemSelect(item)">
                        <!--当前day-->
                        <div :class="{
                          'date-item--current-month': item.monthFlag === 1,
                          'date-item--today': item.isToday,
                           'date-item--selected':item.monthFlag === 1 && item.val === `${value.getFullYear()}-${value.getMonth() + 1}-${value.getDate()}`,
                          'date-item--dotted': item.dotted}"
                             class="date-item-div"
                             style=" height: 100%;width: 100%;">
                            <!--天数-->
                            <div class="dayName">{{ item.date }}</div>

                            <!--自定义内容--> <!-- 动态设置行数 0.95 为高的比例 0.16为竖直6等分 -20 padding  /20为字高 -->
                            <div class="dayContent more-line"
                                 style="font-size: 18px;line-height: 18px;"
                                 :style="{WebkitLineClamp: parseInt(($store.state.clientH*0.9*0.16 - 20)/20 )}"
                                 :class="{'date-item-content-div': item.monthFlag !== 1,}">
                                <!-- <slot name="comment">

                                 </slot>-->
                                <span>
                               kkk kkk kkk kkk kkk kkk
                            {{item.content}}  kkk kkk kkk kkk kkk kkk
                            </span>

                            </div>

                        </div>
                    </div>
                </div>
            </div>
            <div v-show="monthPaneVisible" class="pane pane--month">
                <div
                        v-for="(month, k) in monthArr"
                        :key="'month' + k"
                        :class="{
          'month-item': true,
          'month-item--selected':
            k === value.getMonth() &&
            curMonth === value.getMonth() + 1 &&
            curYear === value.getFullYear()
              ? 'bolder'
              : ''
        }"
                        @click="_handleMonthItemSelect(k)"
                >
                    {{ month }}
                </div>
            </div>
            <div v-show="yearPaneVisible" class="pane pane--year">
                <div
                        class="year-item"
                        v-for="(year, l) in yearArr"
                        :key="'year' + l"
                        :class="{ 'year-item': true, 'year-item--selected': year === value.getFullYear() }"
                        @click="_handleYearItemSelect(year)"
                >
                    {{ year }}
                </div>
            </div>
        </div>
    </div>

</template>

<script>
    // prettier-ignore
    const MONTH_ARR_CN = ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月']
    // prettier-ignore
    const MONTH_ARR_EN = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Ang', 'Sep', 'Oct', 'Nov', 'Dec']

    function getMonthMaxDate(year, month) {
        const isGapYear = (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31
            case 4:
            case 6:
            case 9:
            case 11:
                return 30
            case 2:
                return isGapYear ? 29 : 28
        }
    }

    function getDayOfWeek(year, month, date) {
        return new Date(year, month - 1, date).getDay()
    }

    function isNaNs(...args) {
        return Array.prototype.map.call(args, item => isNaN(item))
    }

    export default {
        name: 'Calendar',

        props: {
            value: {
                type: Date,
                default: () => new Date()
            },
            today: {
                type: Date,
                default: () => new Date()
            },
            locale: {
                type: String,
                default: 'cn',
                validator: val => ['cn', 'en'].includes(val)
            },
            /* Notice: dotArr: Array<boolean>
                       length of dotArr must be same as
                       max date of current month */
            dotArr: {
                type: Array,
                default: () => []
            }
        },

        data() {
            return {
                curYear: this.value.getFullYear(),
                curMonth: this.value.getMonth() + 1,
                curYearRangeStart: Math.floor(this.value.getFullYear() / 10) * 10,
                curYearRangeEnd: Math.floor(this.value.getFullYear() / 10) * 10 + 9,
                paneStatus: 0 /* 0 date-pane, 1 month-pane, 2 year-pane */,
                dateArr: [],
                firstDayOfWeek: 1, //0:周日作为一周的开始 1：周一作为第一天
            }
        },

        watch: {
            curYear(val) {
                this.curYearRangeStart = Math.floor(val / 10) * 10
                this.curYearRangeEnd = Math.floor(val / 10) * 10 + 9
            },
            dotArr: {
                deep: true,
                immediate: true,
                handler() {
                    this._updateMonthArr()
                }
            },
        },

        /*入口初始化数据*/
        created() {
            this._updateMonthArr()
        },

        computed: {
            weekthArr() {
                let weekArr = []
                let firstW = this.getWeekth(this.dateArr[6].val)
                weekArr.push(firstW)
                for (let i = 1; i < this.dateArr.length / 7; i++) {
                    weekArr.push(firstW + i)
                }
                /*检测最后一天是否跨年了 若跨年则周数为1*/
                let d1 = new Date(this.dateArr[6].val)
                let d2 = new Date(this.dateArr[this.dateArr.length - 1].val)
                if (d1.getFullYear() != d2.getFullYear()) weekArr[weekArr.length - 1] = 1
                return weekArr
            },
            localeYearRange() {
                if (this.locale === 'cn') {
                    return `${this.curYearRangeStart}年-${this.curYearRangeEnd}年`
                } else if (this.locale === 'en') {
                    return `${this.curYearRangeStart}-${this.curYearRangeEnd}`
                }
            },
            localeYear() {
                if (this.locale === 'cn') {
                    return this.curYear + '年'
                } else if (this.locale === 'en') {
                    return this.curYear
                }
            },
            localeMonth() {
                if (this.locale === 'cn') {
                    return this.curMonth + '月'
                } else if (this.locale === 'en') {
                    return MONTH_ARR_EN[this.curMonth - 1]
                }
            },
            weekText() {
                switch (this.locale) {
                    case 'cn':
                        let arr = ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
                        this.firstDayOfWeek == 1 ? arr.push('星期日') : arr.unshift('星期日')
                        return arr
                    case 'en':
                        return ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
                }
            },
            monthArr() {
                switch (this.locale) {
                    case 'cn':
                        return MONTH_ARR_CN
                    case 'en':
                        return MONTH_ARR_EN
                }
            },
            yearArr() {
                const that = this
                return new Array(10).fill().map((_, index) => that.curYearRangeStart + index)
            },
            datePaneVisible() {
                return this.paneStatus === 0
            },
            monthPaneVisible() {
                return this.paneStatus === 1
            },
            yearPaneVisible() {
                return this.paneStatus === 2
            }
        },

        methods: {
            changeWeek() {
                this.firstDayOfWeek = this.firstDayOfWeek == 0 ? 1 : 0
                this._updateMonthArr()
            },
            /*获取当前天数所在的周数  */
            getWeekth(currentDay) {
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
            },
            backToToday() {
                this.curYear = this.today.getFullYear()
                this.curMonth = this.today.getMonth() + 1
                this.$emit('input', new Date(this.curYear, this.curMonth - 1, this.today.getDate()))
                this._updateMonthArr()
                this.showDatePane()
            },
            toPreMonth() {
                if (this.curMonth === 1) {
                    this.curYear--
                    this.curMonth = 12
                } else {
                    this.curMonth--
                }
                this.$emit('premonth', {year: this.curYear, month: this.curMonth})
                this._updateMonthArr()
                this.showDatePane()
            },
            toNextMonth() {
                if (this.curMonth === 12) {
                    this.curYear++
                    this.curMonth = 1
                } else {
                    this.curMonth++
                }
                this._updateMonthArr()
                this.$emit('nextmonth', {year: this.curYear, month: this.curMonth})
                this.showDatePane()
            },
            toPreYear() {
                this.curYear--
                this._updateMonthArr()
                this.$emit('preyear', {year: this.curYear, month: this.curMonth})
                this.showDatePane()
            },
            toNextYear() {
                this.curYear++
                this._updateMonthArr()
                this.$emit('nextyear', {year: this.curYear, month: this.curMonth})
                this.showDatePane()
            },
            toSpecificDate(year, month, date) {
                // v0.1.2 fix bug: params of toSpecificDate must be integer
                const intYear = parseInt(year)
                const intMonth = parseInt(month)
                const intDate = parseInt(date)
                if (isNaNs(intYear, intMonth, intDate).some(item => item === true)) return

                this.curYear = intYear
                this.curMonth = intMonth
                this._updateMonthArr()
                this.$emit('input', new Date(intYear, intMonth - 1, intDate))
                this.showDatePane()
            },
            toPreYearRange() {
                this.curYearRangeStart -= 10
                this.curYearRangeEnd -= 10
            },
            toNextYearRange() {
                this.curYearRangeStart += 10
                this.curYearRangeEnd += 10
            },
            showYearPane() {
                this.paneStatus = 2
            },
            showMonthPane() {
                this.paneStatus = 1
            },
            showDatePane() {
                this.paneStatus = 0
            },
            _handleDateItemSelect(item) {
                console.log(item)
                if (item.monthFlag === 0) {
                    this.toPreMonth()
                } else if (item.monthFlag === 2) {
                    this.toNextMonth()
                }
                this.$emit('input', new Date(this.curYear, this.curMonth - 1, item.date))
            },
            _handleMonthItemSelect(index) {
                this.curMonth = index + 1
                this._updateMonthArr()
                this.showDatePane()
            },
            _handleYearItemSelect(year) {
                this.curYear = year
                this._updateMonthArr()
                this.showMonthPane()
            },
            // monthFlag: 0 previous month, 1 current month, 2 next month
            _getDateArr(beginDate = 1, endDate = 31, monthFlag = 1) {
                if (beginDate > endDate) {
                    return []
                }

                let tarMonth = this.curMonth
                let tarYear = this.curYear
                if (monthFlag === 0) {
                    if (this.curMonth === 1) {
                        tarMonth = 12
                        tarYear = this.curYear - 1
                    } else {
                        tarMonth = this.curMonth - 1
                    }
                } else if (monthFlag === 2) {
                    if (this.curMonth === 12) {
                        tarMonth = 1
                        tarYear = this.curYear + 1
                    } else {
                        tarMonth = this.curMonth + 1
                    }
                }

                return new Array(endDate - beginDate + 1).fill().map((_, index) => ({
                    val: `${tarYear}-${tarMonth}-${index + beginDate}`,
                    date: index + beginDate,
                    monthFlag,
                    dotted: monthFlag === 1 && this.dotArr[index],
                    isToday: this._getDateStr(this.today) === `${tarYear}-${tarMonth}-${index + beginDate}`,
                    dayOfWeek: getDayOfWeek(tarYear, tarMonth, index + beginDate),
                    content: this.getContent(tarYear, tarMonth, index + beginDate),
                }))
            },
            /*获取笔记内容*/
            getContent(tarYear, tarMonth, tarDay) {
                return tarYear + 'xxx ' + tarMonth + 'xxx ' + tarDay
            },
            _updateMonthArr() {
                let maxDateOfPreMonth
                if (this.curMonth === 1) {
                    maxDateOfPreMonth = getMonthMaxDate(this.curYear - 1, 12)
                } else {
                    maxDateOfPreMonth = getMonthMaxDate(this.curYear, this.curMonth - 1)
                }
                /*获取当前是周几*/
                const firstDayOfCurMonth = getDayOfWeek(this.curYear, this.curMonth, 1)
                const preDateArr = this._getDateArr(
                    maxDateOfPreMonth - firstDayOfCurMonth + 1,
                    maxDateOfPreMonth,
                    0
                )
                const curDateArr = this._getDateArr(1, getMonthMaxDate(this.curYear, this.curMonth), 1)
                const nextDateArr = this._getDateArr(1, getMonthMaxDate(this.curYear, this.curMonth + 1), 2)

                // 6 line max: 6 * 7 = 42  根据月份选择显示几行日历数据

                this.dateArr = preDateArr
                    .concat(curDateArr)
                    .concat(nextDateArr)
                    .slice(this.firstDayOfWeek, curDateArr.length + preDateArr.length - this.firstDayOfWeek > 35 ? 42 + this.firstDayOfWeek : 35 + this.firstDayOfWeek)

            },
            _getDateStr(date) {
                return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
            }
        }
    }
</script>

<style>

    .weekButton span {
        margin-left: -7px;
    }

    .calendar {
        box-sizing: border-box;
        /*width: 90%;*/
        margin-left: 25px;
        height: 100%;
        padding: 5px;
        color: #000;
        user-select: none;
    }

    /* pane--date relevant */
    .calendar .date-item {
        position: relative;
        box-sizing: border-box;
        display: inline-block;
        width: 14%;
        height: 16%;
        /*line-height: 16%;*/
        color: #c0c4cc;
        text-align: center;
        user-select: none;
        border: 1px solid; /*设置边框线*/
    }

    /*年月选择的样式*/
    .calendar .bannerSpan {
        box-sizing: content-box;
        display: inline-block;
        /* color: #1d9351;*/
        /*width: 20px;*/
        height: 25px;
        /* padding: 8px;
         margin: 6px;*/
    }

    .calendar .date-item div {
        box-sizing: content-box;
        display: inline-block;
        /* width: 20px;
         height: 20px;*/
        /* padding: 8px;
         margin: 6px;*/
    }

    /*不适用line-height */
    .calendar .date-item__week {
        color: #000 !important;
        position: relative;
        box-sizing: border-box;
        display: inline-block;
        width: 14%;
        height: 20%;
        background-color: #B4C6E7;
        text-align: center;
        user-select: none;
        font-weight: bold; /*粗体*/
        border: 1px solid; /*设置边框线*/
    }

    .calendar .pane--date .date-item--current-month {
        color: #000 !important;
        height: 100%;
        width: 100%;
    }

    .calendar .date-item--current-month:hover, .date-item-content-div:hover {
        cursor: pointer;
        background-color: #a8cdf3;
        border-radius: 9px;
    }

    .dayName {
        width: 100%;
        height: 20%;
        line-height: 120%;
        margin: 0 !important;
        padding: 0 !important;
        text-align: left;
        /*background: aqua;*/
        /*text-align: center;*/

    }

    .dayContent {
        text-indent: 2em; /*行首空格*/
        /*line-height: 140%; !*百分比的不准确性*!*/
        /*background: #28c40c;*/
    }

    /*笔记列表中的内容样式  多行截断  (超级简化版)*/
    .calendar .more-line {
        /*font-size: mini;*/
        display: -webkit-box !important;
        overflow: hidden;
        text-overflow: ellipsis;
        word-break: break-all;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 4; /*行数的设置*/
    }

    .calendar .pane--date .date-item--today {
        color: #409eff !important;
        background-color: #d8e7f7;
        border-radius: 9px;

    }

    .calendar .pane--date .date-item--selected {
        background-color: #409eff66 !important;
        border-radius: 9px !important;
    }

    .calendar .date-item--dotted::after {
        position: absolute;
        bottom: 10px;
        left: 50%;
        content: '';
        border: 1.5px solid #000;
        border-radius: 50%;
        transform: translateX(-50%);
    }

    /* pane--month relevant */
    .calendar .pane--month,
    .calendar .pane--year {
        margin: 16px;
        border-top: 1px solid #e7e6e6;
    }

    .calendar .pane--month .month-item,
    .calendar .pane--year .year-item {
        display: inline-block;
        box-sizing: border-box;
        width: 25%;
        text-align: center;
        padding: 16px;
        margin: 16px 0;
        color: #000;
        cursor: pointer;
    }

    .calendar .pane--month .month-item:hover,
    .calendar .pane--year .year-item:hover {
        color: #409eff;
    }

    .calendar .pane--month .month-item--selected,
    .calendar .pane--year .year-item--selected {
        color: #409eff;
        font-weight: bold;
    }

    .calendar .comment {
        color: #c0c4cc;
    }

    /*年月水平布局*/
    .calendar .banner {
        position: relative;
        display: flex;
        flex-wrap: wrap; /*不换行*/
        justify-content: center;
        align-items: center; /*侧轴上居中*/

        text-align: center;
        font-size: 25px;
    }

    .calendar .banner span {
        margin: 0 8px;
    }

    .calendar .banner span:hover {
        color: #409eff;
        cursor: pointer;
    }

    /* arrow relevant */
    .arrow-wrap {
        display: inline-block;
        color: #607d8b;
    }

    .arrow-wrap--left {
        /*position: absolute;*/
        /*left: 4px;*/
    }

    .arrow-wrap--left .arrow--inner::before {
        content: '<';
    }

    .arrow-wrap--left .arrow--outer {
        /*position: relative;*/
    }

    .arrow-wrap--left .arrow--outer::after {
        content: '<';
        display: inline-block;
        transform: translateX(-6px);
    }

    .arrow-wrap--left .arrow--outer::before {
        content: '<';
    }

    .arrow-wrap--right {
        /*position: absolute;*/
        right: 4px;
    }

    .arrow-wrap--right .arrow--inner::before {
        content: '>';
    }

    .arrow-wrap--right .arrow--outer {
        /*position: relative;*/
    }

    .arrow-wrap--right .arrow--outer::before {
        content: '>';
        display: inline-block;
        transform: translateX(6px);
    }

    .arrow-wrap--right .arrow--outer::after {
        content: '>';
    }

    /*箭头的大小*/
    .arrow-wrap .arrow {
        display: inline-block;
        font-size: 25px;
        line-height: 25px;
        margin: 0 16px;
        cursor: pointer;
    }

    .arrow-wrap .arrow:hover {
        color: #409eff;
    }
</style>