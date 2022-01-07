<template>
    <div style="z-index: 3000;display: flex;" class="timeLine">
        <div style="margin-top: 8px;margin-left: 5px">
            <div style="z-index: 3001;display: flex;margin-left: 10px">
                <el-button @click="frontDay"><i class="iconfont icon-before"></i></el-button>
                <el-button @click="play"><i :class="isPlay ?'el-icon-video-play':'el-icon-video-pause'"
                                            style="font-size: 20px;"></i></el-button>
                <el-button @click="nextDay"><i class="iconfont icon-play-next-button"></i></el-button>
            </div>
            <!--时间显示-->
            <div style="margin-top: 10px;text-align: center">
                {{getDayKey(this.dateIndex)}}
            </div>
        </div>

        <div style="width: 85%;margin-left: 20px">
            <el-slider
                    v-model="dateIndex"
                    :max=maxValue
                    @input="timelineChange"
                    :format-tooltip="formatTooltip"
                    ref="slider1">
            </el-slider>


            <div style="display: flex;justify-content:space-between;margin-top: 4px" id="kkktest">
                <!--990为滑条长度-->
                <div style="position: relative">
                    <div  v-for="i in maxValue"
                          v-if="noteData[getDayKey(i)] || imageData[getDayKey(i)]"
                          style="float:left;position: absolute"
                         @mouseenter="mouseEnterLine(i)"
                         :style="{
                         left:  i*$store.state.sliderW/maxValue + 'px'}"
                         class="vLine">
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    let dayjs = require('dayjs');
    export default {
        name: "TimeLine",
        data() {
            return {
                dateIndex: 0,
                isPlay: true,
                timeIndex: 0,//定时器
                maxValue: 0,
                dateData: [],
                noteData: [],
                imageData: [],
                yearFrom: '2021',//起始年份
                isTitleSet: false, //是否已经设置过标题
            }
        },
        methods: {
            /*控制从笔记第一次进入地图时 鼠标经过密度线 地图中心不移动*/
            mouseEnterLine(index) {
                if (!this.$store.state.noteClickLocation) {
                    this.dateIndex = index
                } else {
                    this.$store.state.noteClickLocation = false
                }
            },
            frontDay() {
                if (this.dateIndex > 0) {
                    this.dateIndex = this.dateIndex - 1
                    /*跳过空值*/
                    while (!this.noteData[this.getDayKey(this.dateIndex)] && !this.imageData[this.getDayKey(this.dateIndex)]) {
                        this.dateIndex = this.dateIndex - 1
                        if (this.dateIndex <= -1) {
                            this.dateIndex = this.maxValue
                            break
                        }
                    }
                } else {
                    this.dateIndex = this.maxValue
                }
            },
            nextDay() {
                if (this.dateIndex <= this.maxValue - 1) {
                    let temp = this.dateIndex
                    /*跳过空值*/
                    temp = temp + 1
                    while (!this.noteData[this.getDayKey(temp)] && !this.imageData[this.getDayKey(temp)]) {
                        temp = temp + 1
                        if (temp >= this.maxValue) break
                    }
                    this.dateIndex = temp
                } else {
                    this.dateIndex = 0
                }
                // this.showNoteTitle()
                console.log('nextDay ...')
            },
            createDayKey(arr) {
                let res = {}
                /*判断是否要逆序*/
                if (arr.length > 1) {
                    if (new Date(arr[0].createTime) - new Date(arr[arr.length - 1].createTime) > 0) {
                        arr.reverse()
                    }
                }
                arr.forEach(f => {
                    /*对笔记中的时间进行可能的 -1 操作*/
                    let day = f.createTime
                    if(f.createTime.length > 10){
                        let hour = f.createTime.split(" ")[1].split(":")[0]
                        let day = f.createTime.substring(0, 10)
                        if(Number(hour) < 12){
                            day = dayjs(day).add(-1, 'day').format('YYYY-MM-DD')
                        }
                    }
                    res[day] = f
                })
                // 将一年中的第几天转化为 yyyy-MM-dd
                return res
            },
            initArr(arr) {
                /*判断是否要逆序*/
                if (arr.length > 1) {
                    if (new Date(arr[0].createTime) - new Date(arr[arr.length - 1].createTime) > 0) {
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
                                "createTime": this.tool.formatDate(dateTime),
                            })
                        }
                        i += detalDay - 1
                    }
                    curday = nextDay
                }

            },
            /*按照年区分*/
            initDateData(noteArr, imageArr) {
                let minDay = noteArr[0].createTime.substring(0, 10)
                let dNote = 0, dImage = 0
                if (new Date(noteArr[0].createTime) - new Date(imageArr[0].createTime) > 0) {
                    minDay = imageArr[0].createTime.substring(0, 10)
                    dNote = (new Date(noteArr[0].createTime.substring(0, 10)) - new Date(imageArr[0].createTime.substring(0, 10))) / 86400000
                    dNote = parseInt(dNote)
                } else {
                    dImage = (new Date(imageArr[0].createTime.substring(0, 10)) - new Date(noteArr[0].createTime.substring(0, 10))) / 86400000
                    dImage = parseInt(dImage)
                }
                let maxDay = new Date(noteArr[noteArr.length - 1].createTime) - new Date(imageArr[imageArr.length - 1].createTime) > 0
                    ? imageArr[imageArr.length - 1].createTime.substring(0, 10)
                    : imageArr[imageArr.length - 1].createTime.substring(0, 10)

                let maxValue = parseInt((new Date(maxDay) - new Date(minDay)) / 86400000)
                /*合并*/
                let data = []

                for (let i = 0; i <= maxValue; i++) {
                    let tNote = '', tImage = ''
                    if (i - dNote >= 0 && i - dNote < noteArr.length) tNote = noteArr[i - dNote]
                    if (i - dImage >= 0 && i - dImage < imageArr.length) tImage = imageArr[i - dImage]
                    data.push([tNote, tImage])
                }
                // this.maxValue = 366
                return data
                // return data
            },
            fillAbsentDate() {

                // let noteArr = require('lodash').cloneDeep(this.$store.state.notes)
                let noteArr = [...this.$store.state.notes]
                //获取note的年份
                let noteYearFrom = noteArr[0].createTime.substring(0, 4)
                let noteYearTo = noteArr[noteArr.length - 1].createTime.substring(0, 4)
                /*合并同一天中的多篇笔记*/
                // noteArr = this.createDayKey(this.groupNoteByDay(noteArr))
                this.noteData = this.createDayKey(noteArr)

                //初始化当天图片
                // let imageArr = this.tool.groupImages('day', require('lodash').cloneDeep(this.$store.state.fileList))
                let imageArr = this.tool.groupImages('day', this.$store.state.fileList)
                imageArr.forEach(i => {
                    i.createTime = i.createTime.replace("年", "-").replace("月", "-").replace("日", "")
                })
                let imageYearFrom = imageArr[0].createTime.substring(0, 4)
                let imageYearTo = imageArr[imageArr.length - 1].createTime.substring(0, 4)
                imageArr = this.createDayKey(imageArr)
                this.imageData = imageArr

                let yearFrom = Math.min(Number(imageYearFrom), Number(imageYearTo), Number(noteYearFrom), Number(noteYearTo))
                let yearTo = Math.max(Number(imageYearFrom), Number(imageYearTo), Number(noteYearFrom), Number(noteYearTo))
                this.yearFrom = yearFrom
                this.maxValue = (new Date((yearTo) + "-12-31").getTime() - new Date(yearFrom + "-01-01").getTime()) / (24 * 3600 * 1000)
                console.log("kk")
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
            getDayKey(index) {
                let dayKey = new Date(new Date(this.yearFrom.toString()).getTime() + index * 24 * 3600 * 1000).toISOString().substring(0, 10)
                return dayKey
            },
            timelineChange(value) {
                /*定位笔记*/
                console.log('timelineChange')
                let note = this.noteData[this.getDayKey(this.dateIndex)]
                if (!this.isTitleSet && note) {

                    let {createTime, lnglat, title} = note
                    this.$store.state.isImageTitle = false
                    if (title) {
                        this.$bus.$emit('toPoint', lnglat.split(',')[0], lnglat.split(',')[1], title, createTime)
                    }
                    /*初始化地图要展示的图片*/
                    this.setDayImages(title)
                } else {
                    /*初始化地图要展示的图片*/
                    this.setDayImages('')
                }
                this.isTitleSet = false


            },
            setDayImages(title) {
                if (this.imageData[this.getDayKey(this.dateIndex)]) {
                    //    当当天没有笔记时 查看是否有图片，有的话就显示图片的地理位置
                    this.$store.state.dayImages = this.imageData[this.getDayKey(this.dateIndex)].images
                    /*当天无笔记时 尝试以当天的图片来进行定位*/
                    if (!title) {
                        let getImageMapInfo = false //标记是否找到了在地图上标注的信息
                        for (let dayImage of this.$store.state.dayImages) {
                            console.log('dayImage', dayImage)
                            if (dayImage.lnglat) {
                                console.log('dayImage.lnglat', dayImage.lnglat)
                                this.$store.state.isImageTitle = true
                                getImageMapInfo =true
                                this.$bus.$emit('toPoint', dayImage.lnglat.split(',')[0], dayImage.lnglat.split(',')[1], dayImage.title, dayImage.createTime)
                                break
                            }
                        }
                        /*若照片均无坐标 则将第一张照片作为标题和时间*/
                        if(!getImageMapInfo){
                            this.$store.state.isImageTitle = true
                            this.$bus.$emit('toPoint', 0, 0, this.$store.state.dayImages[0].title, this.$store.state.dayImages[0].createTime)
                        }
                    }
                } else {
                    /*清空*/
                    this.$store.state.dayImages = []
                }
            },
            formatTooltip(val) {
                if (!val) {
                    val = 0
                }
                /*优先返回笔记的标题作为显示*/
                return this.noteData[this.getDayKey(this.dateIndex)]
                    ? this.noteData[this.getDayKey(this.dateIndex)].title
                    : (this.imageData[this.getDayKey(this.dateIndex)] ? '图片' : '无内容');
            },
            /*在note跳转时定位时间轴  通过title和 day 来确定index 同一天中可能有多条笔记*/
            setDateIndex(title, createTime) {
                //12点以前的笔记都算前一天
                let hour = createTime.split(" ")[1].split(":")[0]
                let day = createTime.substring(0, 10)
                if(Number(hour) < 12){
                    day = dayjs(day).add(-1, 'day').format('YYYY-MM-DD')
                }
                let isNote = this.noteData[day]
                let isImage = this.imageData[day]
                if (isImage || isNote) {
                    let temp = isNote ? isNote : isImage
                    this.$bus.$emit('toPoint', temp.lnglat.split(',')[0], temp.lnglat.split(',')[1], title, createTime)
                    this.dateIndex = i
                    if (this.imageData[this.getDayKey(i)]) {
                        //  当当天没有笔记时 查看是否有图片，有的话就显示图片的地理位置
                        this.$store.state.dayImages = this.imageData[this.getDayKey(i)].images
                    } else {
                        /*清空*/
                        this.$store.state.dayImages = []
                    }
                    this.isTitleSet = true
                }
                /*for (let i = 0; i <= this.maxValue; i++) {
                    /!*笔记或者图片的定位*!/
                    let isNote = this.noteData[this.getDayKey(i)]
                    let isImage = this.imageData[this.getDayKey(i)]
                    if (isImage || isNote) {
                        let temp = isNote ? isNote : isImage
                        this.$bus.$emit('toPoint', temp.lnglat.split(',')[0], temp.lnglat.split(',')[1], title, createTime)
                        this.dateIndex = i
                        if (this.imageData[this.getDayKey(i)]) {
                            //  当当天没有笔记时 查看是否有图片，有的话就显示图片的地理位置
                            this.$store.state.dayImages = this.imageData[this.getDayKey(i)].images
                        } else {
                            /!*清空*!/
                            this.$store.state.dayImages = []
                        }
                        this.isTitleSet = true
                        break
                    }
                }*/
            },
            /*显示滑块的提示信息*/
            showNoteTitle() {
                // this.$refs.slider1.setPosition(this.dateIndex)
            },
            /*合并笔记中一天内的多个标题*/
            groupNoteByDay(notes) {
                for (let i = 0; i < notes.length - 1; i++) {
                    if (notes[i].createTime.substring(0, 10) == notes[i + 1].createTime.substring(0, 10)) {
                        notes[i].title = '1.' + notes[i].title + ' 2.' + notes[i + 1].title
                        notes.splice(i, 1)
                    }
                }
                return notes
            },


        },
        mounted() {
            this.$bus.$on('setDateIndex', this.setDateIndex)
            this.$store.state.sliderW = document.getElementById("kkktest").offsetWidth
            let lastPixelRatio = window.devicePixelRatio;
            let vm = this
            window.addEventListener('resize', function () {
                let currentPixelRatio = window.devicePixelRatio;
                if (currentPixelRatio !== lastPixelRatio) {
                    vm.$store.state.sliderW = document.getElementById("kkktest").offsetWidth
                    console.log('timeline页面缩放变化了');
                }
                lastPixelRatio = currentPixelRatio;
            });
        },
        created() {
            console.log('timeline created...')
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