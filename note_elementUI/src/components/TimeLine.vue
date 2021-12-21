<template>
    <div style="z-index: 3000;display: flex;" class="timeLine">
        <div style="margin-top: 8px;margin-left: 5px">
            <div style="z-index: 3001;display: flex;margin-left: 10px">
                <el-button @click="frontDay"><i class="iconfont icon-before"></i></el-button>
                <el-button @click="play"><i :class="isPlay ?'el-icon-video-play':'el-icon-video-pause'"
                                            style="font-size: 20px;"></i></el-button>
                <el-button @click="nextDay"><i class="iconfont icon-play-next-button"></i></el-button>
            </div>
            <!--<div style="margin-top: 10px;text-align: center"> {{date.split(" ")[0]}}</div>-->
            <div style="margin-top: 10px;text-align: center"> {{dateData[dateIndex][0]
                ?dateData[dateIndex][0].createTime.substring(0,10)
                :dateData[dateIndex][1].createTime.substring(0,10)}}</div>
        </div>

        <div style="width: 85%;margin-left: 20px">
            <el-slider
                    v-model="dateIndex"
                    :max=maxValue
                    @input="timelineChange"
                    :format-tooltip="formatTooltip"
                    ref="slider1">
            </el-slider>
            <div style="display: flex;justify-content:space-between;margin-top: 4px;">
                <div v-for="(d,index) in dateData">
                    <el-tooltip class="item" effect="dark" :content="dateData[index][0].title ? dateData[index][0].title : (dateData[index][1].images ? '图片':'无内容')" placement="bottom">
                        <div @mouseenter="$store.state.noteClickLocation ? '':dateIndex = index"
                             :style="{backgroundColor: dateData[index][0].title || dateData[index][1].images ?'#000000' :'#ffffff'}" class="vLine"></div>
                    </el-tooltip>
                </div>
            </div>
        </div>


    </div>
</template>

<script>
    export default {
        name: "TimeLine",
        data() {
            return {
                dateIndex: 0,
                isPlay: true,
                timeIndex: 0,//定时器
                maxValue: 0,
                dateData: [],
            }
        },
        methods: {

            frontDay() {
                if (this.dateIndex > 0) {
                    this.dateIndex = this.dateIndex - 1
                    /*跳过空值*/
                    while (this.dateData[this.dateIndex][0].title == undefined && this.dateData[this.dateIndex][1].images == undefined) {
                        this.dateIndex = this.dateIndex - 1
                        if (this.dateIndex <= -1) {
                            this.dateIndex = this.maxValue
                            break
                        }
                    }
                }else {
                    this.dateIndex = this.maxValue
                }
                this.showNoteTitle()
            },
            nextDay() {
                if (this.dateIndex <= this.maxValue -1) {
                    this.dateIndex = this.dateIndex + 1
                    /*跳过空值*/
                    while (this.dateData[this.dateIndex][0].title == undefined && this.dateData[this.dateIndex][1].images == undefined) {
                        this.dateIndex = this.dateIndex + 1
                        if (this.dateIndex >= this.maxValue) break
                    }
                }else {
                    this.dateIndex = 0
                }
                this.showNoteTitle()
            },
            formatDate(date) {
                let month = '' + (date.getMonth() + 1),
                    day = '' + date.getDate(),
                    year = date.getFullYear();
                if (month.length < 2) month = '0' + month;
                if (day.length < 2) day = '0' + day;

                return [year, month, day].join('-');
            },
            initArr(arr) {
                /*判断是否要逆序*/
                if(arr.length >1){
                    if(new Date(arr[0].createTime) - new Date(arr[arr.length-1].createTime) > 0){
                        arr.reverse()
                    }
                }
                let curday = arr[0].createTime.substring(0, 10)
                for (let i = 0; i < arr.length - 1; i++) {
                    /*补全缺失的日期*/
                    let nextDay = arr[i + 1].createTime.substring(0, 10)
                    let cd = new Date(curday)
                    let nd = new Date(nextDay)
                    let detalDay = (nd - cd) / 86400000

                    /*插入缺失的天数  */
                    if (detalDay > 1) {
                        for (let j = 0; j < detalDay - 1; j++) {
                            let temp = new Date(cd)
                            let dateTime = new Date(temp.setDate(cd.getDate() + j + 1));
                            arr.splice(i + 1 + j, 0, {
                                "createTime": this.formatDate(dateTime),
                            })
                        }
                        i += detalDay - 1
                    }
                    curday = nextDay
                }

            },
            initDateData(noteArr, imageArr) {
                let minDay = noteArr[0].createTime.substring(0,10)
                let dNote = 0, dImage = 0
                if (new Date(noteArr[0].createTime) - new Date(imageArr[0].createTime) > 0) {
                    minDay = imageArr[0].createTime
                    dNote = (new Date(noteArr[0].createTime.substring(0,10)) - new Date(imageArr[0].createTime.substring(0,10))) / 86400000
                    dNote = parseInt(dNote)
                } else {
                    dImage = (new Date(imageArr[0].createTime.substring(0,10)) - new Date(noteArr[0].createTime.substring(0,10))) / 86400000
                    dImage = parseInt(dImage)
                }
                let maxDay = new Date(noteArr[noteArr.length - 1].createTime) - new Date(imageArr[imageArr.length - 1].createTime) > 0
                    ? imageArr[imageArr.length - 1].createTime.substring(0,10)
                    : imageArr[imageArr.length - 1].createTime.substring(0,10)

                this.maxValue = parseInt((new Date(maxDay) - new Date(minDay)) / 86400000)
                /*合并*/
                let data = []

                for (let i = 0; i <= this.maxValue; i++) {
                    let tNote = '', tImage = ''
                    if (i - dNote >= 0 && i - dNote < noteArr.length) tNote = noteArr[i - dNote]
                    if (i - dImage >= 0 && i - dImage < imageArr.length) tImage = imageArr[i - dImage]
                    data.push([tNote, tImage])
                }
                return data
            },
            fillAbsentDate() {
                let noteArr = require('lodash').cloneDeep(this.$store.state.notes)
                /*合并同一天中的多篇笔记*/
                this.initArr(this.groupNoteByDay(noteArr))
                //初始化当天图片
                let imageArr = this.tool.groupImages('day', require('lodash').cloneDeep(this.$store.state.fileList))
                imageArr.forEach(i => {i.createTime = i.createTime.replace("年", "-").replace("月", "-").replace("日", "")})
                this.initArr(imageArr)
                /*对齐两个时间 生成一个大数组*/
                this.dateData = this.initDateData(noteArr,imageArr)
                // console.log(this.dateData)
            },

            play() {
                /*暂停*/
                if (!this.isPlay) {
                    this.isPlay = true
                    clearInterval(this.timeIndex)
                } else { /*播放*/
                    this.isPlay = false
                    /*在末尾点击播放*/
                    if (this.dateIndex >= this.maxValue) {
                        this.dateIndex = 0
                    }
                    this.timeIndex = setInterval(() => {
                        /*跳过空值*/
                        this.nextDay()
                        /*到末尾停止*/
                        if (this.dateIndex >= this.maxValue) {
                            this.isPlay = true
                            clearInterval(this.timeIndex)
                        }
                    }, 500)
                }
            },
            timelineChange(value) {
                /*定位笔记*/
                let {createTime, lnglat, title} = this.dateData[this.dateIndex][0]
                if (title) {
                    this.$store.state.isImageTitle = false
                    this.$bus.$emit('toPoint', lnglat.split(',')[0], lnglat.split(',')[1], title, createTime)
                }

                /*展示图片*/
                if (this.dateData[this.dateIndex][1].images) {
                    //    当当天没有笔记时 查看是否有图片，有的话就显示图片的地理位置
                    this.$store.state.dayImages = this.dateData[this.dateIndex][1].images
                    /*当天无笔记时 尝试以当天的图片来进行定位*/
                    if(!title){
                        for (let dayImage of this.$store.state.dayImages) {
                            console.log('dayImage', dayImage)
                            if (dayImage.lnglat ) {
                                console.log('dayImage.lnglat', dayImage.lnglat)
                                this.$store.state.isImageTitle = true
                                this.$bus.$emit('toPoint', dayImage.lnglat.split(',')[0], dayImage.lnglat.split(',')[1], dayImage.title, dayImage.createTime)
                                break
                            }
                        }
                    }
                }else {
                    /*清空*/
                    this.$store.state.dayImages = []
                }
                // return noteTitle ? noteTitle : (this.dateData[this.dateIndex][1].images ? '图片':'无内容');
            },
            formatTooltip(val) {
                if (!val) {
                    val = 0
                }
                /*优先返回笔记的标题作为显示*/
                let noteTitle = this.dateData[this.dateIndex][0].title
                return noteTitle ? noteTitle : (this.dateData[this.dateIndex][1].images ? '图片':'无内容');
            },
            /*在note跳转时定位时间轴  通过title和 day 来确定index 同一天中可能有多条笔记*/
            setDateIndex(title, createTime) {
                for (let i = 0; i <= this.maxValue; i++) {
                    /*this.dateData[i][0].title == title*/
                    if (this.dateData[i][0].createTime.substring(0,10) == createTime) {
                        this.dateIndex = i
                        break
                    }
                }
            },
            /*显示滑块的提示信息*/
            showNoteTitle() {
                this.$refs.slider1.setPosition(this.dateIndex)
            },
            /*合并笔记中一天内的多个标题*/
            groupNoteByDay(notes) {
                for (let i = 0; i < notes.length - 1; i++) {
                    if(notes[i].createTime.substring(0,10) == notes[i+1].createTime.substring(0,10)){
                        notes[i].title = '1.' + notes[i].title + ' 2.' + notes[i+1].title
                        notes.splice(i,1)
                    }
                }
                return notes
            },

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

    .timeLine .el-button {
        padding: 0px;
        margin-left: 0px !important;
        color: #000000;
        border: 1px solid #ffffff;
    }
</style>