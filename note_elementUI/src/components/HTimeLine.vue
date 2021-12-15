<template>
    <div style="z-index: 3000;float: right;padding-right: 10px;" class="timeLine">
        <div>{{date.split(" ")[0]}}</div>
        <div style="float:right;display: flex;flex-direction:column;justify-content:space-between;margin-top: -5px;">
            <div v-for="(d,index) in dateArr"> <!---->
                <el-tooltip class="item" effect="dark"  placement="left">
                    <div slot="content" class="timeLine more-line">
                        <!--时间信息-->
                        <b>{{d.day}}</b><br/>
                        <!-- style="width: 500px;"-->
                        <div >
                              <span>
                             {{d.title ? d.title :'无'}}
                        </span>
                        </div>

                       </div>
                    <div @mouseenter="dateIndex = index" :style="{backgroundColor: d.title ?'#000000' :'#ffffff'}" class="hLine"></div>
                </el-tooltip>
            </div>
        </div>

    </div>
</template>

<script>
    import timeLineDataDemo from '../data/timeLineDataDemo.json'

    export default {
        name: "TimeLine",
        computed: {
            date() {
                return this.dateArr[this.dateIndex].day
            },
            lenOfArr() {
                return this.dateArr.length - 1
            },
        },
        data() {
            return {
                // dateArr: ['2000-01-05', '2001-02-14', '2002-10-31', '2003-12-22', '2004-07-09'],
                dateArr: [],
                timeLineMarkers: [],
                dateIndex: 0,
                isPlay: true,
                timeIndex: 0,//定时器
            }
        },
        methods: {

            frontDay() {
                if (this.dateIndex > 0) {
                    this.dateIndex = this.dateIndex - 1
                    this.showNoteTitle()
                }
            },
            nextDay() {
                if (this.dateIndex < this.dateArr.length) {
                    this.dateIndex = this.dateIndex + 1
                    this.showNoteTitle()
                }
            },
            formatDate(date) {
                let month = '' + (date.getMonth() + 1),
                    day = '' + date.getDate(),
                    year = date.getFullYear();
                if (month.length < 2) month = '0' + month;
                if (day.length < 2) day = '0' + day;

                return [year, month, day].join('/');
            },

            fillAbsentDate() {
                let dateArr = timeLineDataDemo.data.slice(0,370)
                // let dateArr = timeLineDataDemo.data
                let marker = {}
                let curday = dateArr[0].day.substring(0, 10)
                for (let i = 0; i < dateArr.length - 1; i++) {
                    /*补全缺失的日期*/
                    let nextDay = dateArr[i + 1].day.substring(0, 10)
                    let cd = new Date(curday)
                    let nd = new Date(nextDay)
                    let detalDay = (nd - cd) / 86400000
                    /*插入缺失的天数  */
                    marker[i] = {
                        style: {
                            /*使用样式 生成密度线  label: '|',*/
                            color: '#000000',
                            width: '1px !important',
                            height: '10px !important',
                            borderRight: 'solid #000000 1px !important'
                        }
                    }
                    if (detalDay > 1) {
                        for (let j = 0; j < detalDay - 1; j++) {
                            let temp = new Date(cd)
                            let dateTime = new Date(temp.setDate(cd.getDate() + j + 1));
                            dateArr.splice(i + 1 + j, 0, {
                                "day": this.formatDate(dateTime),
                            })
                            marker[i + 1 + j] = {
                                style: {
                                    color: '#000000',
                                    fontSize: '10px',
                                },
                                label: ''
                            }
                        }
                        i += detalDay - 1
                    }
                    curday = nextDay
                }
                /*覆盖*/
                marker[0] = {
                    style: {
                        color: '#000000',
                    },
                    label: dateArr[0].day.split(" ")[0]
                }
                marker[dateArr.length - 1] = {
                    style: {
                        color: '#000000',
                    },
                    label: dateArr[dateArr.length - 1].day.split(" ")[0]
                }
                this.dateArr = dateArr
                this.timeLineMarkers = marker
            },


            play() {
                /*暂停*/
                if (!this.isPlay) {
                    this.isPlay = true
                    clearInterval(this.timeIndex)
                } else { /*播放*/
                    this.isPlay = false
                    /*在末尾点击播放*/
                    if (this.dateIndex >= this.dateArr.length - 1) {
                        this.dateIndex = 0
                    }
                    this.timeIndex = setInterval(() => {
                        // console.log('setTimeout',this.dateIndex,this.dateArr.length)
                        this.dateIndex += 1
                        this.showNoteTitle()
                        if (this.dateIndex >= this.dateArr.length - 1) {
                            this.isPlay = true
                            clearInterval(this.timeIndex)
                        }
                    }, 500)
                }
            },
            timelineChange(value) {
                console.log('timelineChange exec', this.dateIndex)

                let {day, lat, lng, title} = this.dateArr[this.dateIndex]
                // console.log(day, lat, lng, title)
                if (title) {
                    this.$bus.$emit('toPoint', lng, lat, title, day)
                }

            },
            formatTooltip(val) {
                if (!val) {
                    val = 0
                }
                // console.log('formatTooltip',val,this.dateArr[val])
                return this.dateArr[val].title ? this.dateArr[val].title : '无内容';
            },
            /*通过title和 day 来确定index*/
            setDateIndex(title, day) {
                for (let i = 0; i < this.dateArr.length - 1; i++) {
                    if (this.dateArr[i].day == day && this.dateArr[i].title == title) {
                        this.dateIndex = i
                        break
                    }
                }
            },
            /*显示滑块的提示信息*/
            showNoteTitle() {
                this.$refs.slider1.setPosition(this.dateIndex)
            }
        },
        mounted() {
            this.$bus.$on('setDateIndex', this.setDateIndex)
        },
        created() {
            this.fillAbsentDate();
        },
    }
</script>

<style>
    /*水平密度线*/
    .hLine {
        background-color: #000000;
        width: 10px !important;
        height: 1px !important;
        borderRight: 1px solid #000000;
    }

    .timeLine .el-button{
        padding: 0px;
        margin-left: 0px !important;
        color: #000000;
        border: 1px solid #ffffff;
    }
    .timeLine .more-line {
        /*font-size: mini;*/
        display: -webkit-box !important;
        overflow: hidden;
        text-overflow: ellipsis;
        word-break: break-all;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 4; /*行数的设置*/
    }
</style>