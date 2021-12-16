<template>
    <div style="z-index: 3000;display: flex;" class="timeLine">
        <div style="margin-top: 8px;margin-left: 5px">
            <div style="z-index: 3001;display: flex;margin-left: 10px">
                <el-button @click="frontDay" ><i class="iconfont icon-before"></i></el-button>
                <el-button  @click="play" ><i :class="isPlay ?'el-icon-video-play':'el-icon-video-pause'" style="font-size: 20px;"></i></el-button>
                <el-button @click="nextDay"> <i class="iconfont icon-play-next-button"></i></el-button>
            </div>
           <div style="margin-top: 10px;text-align: center"> {{date.split(" ")[0]}}</div>
        </div>

        <div style="width: 85%;margin-left: 20px">
            <el-slider
                    v-model="dateIndex"
                    :max=lenOfArr
                    @input="timelineChange"
                    :format-tooltip="formatTooltip"
                    ref="slider1">
            </el-slider>
            <div style="display: flex;justify-content:space-between;margin-top: 4px;">
                <div v-for="(d,index) in dateArr">
                    <el-tooltip  class="item" effect="dark" :content="d.title ? d.title :'无'" placement="bottom">
                        <div @mouseenter="$store.state.noteClickLocation ? '':dateIndex = index" :style="{backgroundColor: d.title ?'#000000' :'#ffffff'}" class="vLine"></div>
                    </el-tooltip>
                </div>
            </div>
        </div>


    </div>
</template>

<script>
    export default {
        name: "TimeLine",
        computed: {
            date() {
                return this.dateArr[this.dateIndex].createTime
            },
            lenOfArr() {
                return this.dateArr.length - 1
            },
        },
        data() {
            return {
                // dateArr: ['2000-01-05', '2001-02-14', '2002-10-31', '2003-12-22', '2004-07-09'],
                dateArr: [],
                dateIndex: 0,
                isPlay: true,
                timeIndex: 0,//定时器
            }
        },
        methods: {

            frontDay() {
                if (this.dateIndex > 0) {
                    this.dateIndex = this.dateIndex - 1
                    while (this.dateArr[this.dateIndex].title == undefined){
                        this.dateIndex = this.dateIndex - 1
                        if(this.dateIndex == 0 )break
                    }
                    this.showNoteTitle()
                }
            },
            nextDay() {
                if (this.dateIndex < this.dateArr.length) {
                    this.dateIndex = this.dateIndex + 1
                    /*跳过空值*/
                    while (this.dateArr[this.dateIndex].title == undefined){
                        this.dateIndex = this.dateIndex + 1
                        if(this.dateIndex >= this.dateArr.length) break
                    }
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
                let dateArr = [...this.$store.state.notes]
                let curday = dateArr[0].createTime.substring(0, 10)
                for (let i = 0; i < dateArr.length - 1; i++) {
                    /*补全缺失的日期*/
                    let nextDay = dateArr[i + 1].createTime.substring(0, 10)
                    let cd = new Date(curday)
                    let nd = new Date(nextDay)
                    let detalDay = (nd - cd) / 86400000
                    /*插入缺失的天数  */

                    if (detalDay > 1) {
                        for (let j = 0; j < detalDay - 1; j++) {
                            let temp = new Date(cd)
                            let dateTime = new Date(temp.setDate(cd.getDate() + j + 1));
                            dateArr.splice(i + 1 + j, 0, {
                                "createTime": this.formatDate(dateTime),
                            })
                        }
                        i += detalDay - 1
                    }
                    curday = nextDay
                }
                this.dateArr = dateArr
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
                        /*跳过空值*/
                        while (this.dateArr[this.dateIndex].title == undefined){
                            this.dateIndex = this.dateIndex + 1
                            if(this.dateIndex >= this.dateArr.length) break
                        }
                        this.showNoteTitle()
                        /*停止*/
                        if (this.dateIndex >= this.dateArr.length - 1) {
                            this.isPlay = true
                            clearInterval(this.timeIndex)
                        }
                    }, 500)
                }
            },
            timelineChange(value) {
                // console.log('timelineChange exec', this.dateIndex)
                let {createTime, lnglat, title} = this.dateArr[this.dateIndex]

                /*同步更新地图上的中心点*/
                if (title) {
                    this.$bus.$emit('toPoint', lnglat.split(',')[0], lnglat.split(',')[1], title, createTime)
                }
            },
            formatTooltip(val) {
                if (!val) {
                    val = 0
                }
                return this.dateArr[val].title ? this.dateArr[val].title : '无内容';
            },
            /*通过title和 day 来确定index*/
            setDateIndex(title, createTime) {
                for (let i = 0; i < this.dateArr.length - 1; i++) {
                    if (this.dateArr[i].createTime == createTime && this.dateArr[i].title == title) {
                        this.dateIndex = i
                        // console.log('setDateIndex done')
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
    /*垂直密度线*/
    .vLine {
        background-color: #000000;
        width: 1px !important;
        height: 10px !important;
        borderRight: 1px solid #000000;
    }

    .timeLine .el-button{
        padding: 0px;
        margin-left: 0px !important;
        color: #000000;
        border: 1px solid #ffffff;
    }
</style>