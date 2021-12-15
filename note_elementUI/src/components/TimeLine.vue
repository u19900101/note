<template>
    <div style="z-index: 3000;margin-left: 50px;">

        <span class="demonstration">{{date}}</span>
        <el-button @click="frontDay"> 前一日</el-button>
        <el-button @click="play">{{isPlay ? '播放':'暂停'}}</el-button>
        <el-button @click="nextDay"> 后一日</el-button>
        <el-slider v-model="dateIndex"
                   :max=lenOfArr
                   :marks="timeLineMarkers"
                   @input="timelineChange"
                   :format-tooltip="formatTooltip"
                   ref="slider1"
        ></el-slider>
        <div class=""></div>
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

            frontDay(){
                if(this.dateIndex > 0){
                    this.dateIndex =  this.dateIndex - 1
                    this.showNoteTitle()
                }
            },
            nextDay(){
                if(this.dateIndex < this.dateArr.length){
                    this.dateIndex =  this.dateIndex + 1
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
                // let dateArr = timeLineDataDemo.data.slice(0,10)
                let dateArr = timeLineDataDemo.data
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
                console.log('timelineChange exec',this.dateIndex)

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
</style>