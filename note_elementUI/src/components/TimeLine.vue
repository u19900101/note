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
            <div style="margin-top: 10px;text-align: center"> {{dateData[dateIndex][0].createTime}}</div>
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
                <div v-for="(d,index) in dateData">
                    <el-tooltip class="item" effect="dark" :content="d[0] ? d[0].title :'无'" placement="bottom">
                        <div @mouseenter="$store.state.noteClickLocation ? '':dateIndex = index"
                             :style="{backgroundColor: d[0].title ?'#000000' :'#ffffff'}" class="vLine"></div>
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
           /* date() {
               /!* if (this.dateIndex - this.dNote > -1 && this.dateIndex - this.dNote < this.noteArr.length) {
                    return this.noteArr[this.dateIndex - this.dNote].createTime
                }

                if (this.dateIndex - this.dImage > -1 && this.dateIndex - this.dImage < this.imageArr.length) {
                    this.imageArr[this.dateIndex - this.dImage].createTime
                }*!/

            },*/
            /*lenOfArr() {
                return this.noteArr.length - 1
            },*/
        },
        data() {
            return {
                // noteArr: ['2000-01-05', '2001-02-14', '2002-10-31', '2003-12-22', '2004-07-09'],
                noteArr: [],
                imageArr: [],
                dateIndex: 0,
                isPlay: true,
                timeIndex: 0,//定时器
                dNote: 0, //笔记时间的偏移天数
                dImage: 0, // 照片时间的偏移天数
                lenOfArr: 0,
                dateData: [],
            }
        },
        methods: {

            frontDay() {
                if (this.dateIndex > 0) {
                    this.dateIndex = this.dateIndex - 1
                    while (this.noteArr[this.dateIndex].title == undefined) {
                        this.dateIndex = this.dateIndex - 1
                        if (this.dateIndex == 0) break
                    }
                    this.showNoteTitle()
                }
            },
            nextDay() {
                if (this.dateIndex < this.noteArr.length) {
                    this.dateIndex = this.dateIndex + 1
                    /*跳过空值*/
                    while (this.noteArr[this.dateIndex].title == undefined) {
                        this.dateIndex = this.dateIndex + 1
                        if (this.dateIndex >= this.noteArr.length) break
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

                return [year, month, day].join('-');
            },
            initArr(arr) {
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
                return arr
            },
            fillAbsentDate() {
                let notes = require('lodash').cloneDeep(this.$store.state.notes)
                this.noteArr = this.initArr(notes)
                //初始化当天图片
                let images = this.$store.state.fileList
                let imageList = require('lodash').cloneDeep(images)
                let dayImages = this.tool.groupImages('day', imageList)
                dayImages.forEach(i => {
                    i.createTime = i.createTime.replace("年", "-").replace("月", "-").replace("日", "")
                })
                this.imageArr = this.initArr(dayImages)
                /*对齐两个时间 生成一个大数组*/
                let minDay = this.noteArr[0].createTime
                let dNote = 0, dImage = 0
                if (new Date(this.noteArr[0].createTime) - new Date(this.imageArr[0].createTime) > 0) {
                    minDay = this.imageArr[0].createTime
                    dNote = (new Date(this.noteArr[0].createTime) - new Date(this.imageArr[0].createTime)) / 86400000
                    dNote = parseInt(dNote)
                } else {
                    dImage = (new Date(this.imageArr[0].createTime) - new Date(this.noteArr[0].createTime)) / 86400000
                    dImage = parseInt(dImage)
                }
                let maxDay = new Date(this.noteArr[this.noteArr.length - 1].createTime) - new Date(this.imageArr[this.imageArr.length - 1].createTime) > 0 ? this.imageArr[this.imageArr.length - 1].createTime : this.imageArr[this.imageArr.length - 1].createTime
                let dDay = parseInt((new Date(maxDay) - new Date(minDay)) / 86400000)

                this.lenOfArr = dDay
                this.dNote = dNote
                this.dImage = dImage

                /*合并*/
                let data = []

                for (let i = 0; i < dDay; i++) {
                    let tNote = '', tImage = ''
                    if (i - dNote >= 0 && i - dNote < this.noteArr.length) tNote = this.noteArr[i - dNote]
                    if (i - dImage >= 0 && i - dImage < this.imageArr.length) tImage = dayImages[i - dImage]
                    data.push([tNote, tImage])
                }
                this.dateData = data
                console.log(data)
            },

            // fillAbsentDate() {
            //     // let noteArr = [...this.$store.state.notes]
            //     let noteArr = [...this.$store.state.notes]
            //     let curday = noteArr[0].createTime.substring(0, 10)
            //     for (let i = 0; i < noteArr.length - 1; i++) {
            //         /*补全缺失的日期*/
            //         let nextDay = noteArr[i + 1].createTime.substring(0, 10)
            //         let cd = new Date(curday)
            //         let nd = new Date(nextDay)
            //         let detalDay = (nd - cd) / 86400000
            //
            //         /*插入缺失的天数  */
            //         if (detalDay > 1) {
            //             for (let j = 0; j < detalDay - 1; j++) {
            //                 let temp = new Date(cd)
            //                 let dateTime = new Date(temp.setDate(cd.getDate() + j + 1));
            //                 noteArr.splice(i + 1 + j, 0, {
            //                     "createTime": this.formatDate(dateTime),
            //                 })
            //             }
            //             i += detalDay - 1
            //         }
            //         curday = nextDay
            //     }
            //     this.noteArr = noteArr
            //     //初始化当天图片
            //     this.$store.state.dayImages = this.$store.state.fileList.filter((i) => i.createTime.split(" ")[0] == noteArr[0].createTime.split(" ")[0])
            // },
            play() {
                /*暂停*/
                if (!this.isPlay) {
                    this.isPlay = true
                    clearInterval(this.timeIndex)
                } else { /*播放*/
                    this.isPlay = false
                    /*在末尾点击播放*/
                    if (this.dateIndex >= this.noteArr.length - 1) {
                        this.dateIndex = 0
                    }
                    this.timeIndex = setInterval(() => {
                        // console.log('setTimeout',this.dateIndex,this.noteArr.length)
                        this.dateIndex += 1
                        /*跳过空值*/
                        while (this.noteArr[this.dateIndex].title == undefined) {
                            this.dateIndex = this.dateIndex + 1
                            if (this.dateIndex >= this.noteArr.length) break
                        }
                        this.showNoteTitle()
                        /*停止*/
                        if (this.dateIndex >= this.noteArr.length - 1) {
                            this.isPlay = true
                            clearInterval(this.timeIndex)
                        }
                    }, 500)
                }
            },
            timelineChange(value) {

                if (this.dateIndex - this.dNote > -1 && this.dateIndex - this.dNote < this.noteArr.length) {
                    let {createTime, lnglat, title} = this.noteArr[this.dateIndex - this.dNote]
                    if (title) {
                        this.$bus.$emit('toPoint', lnglat.split(',')[0], lnglat.split(',')[1], title, createTime)
                    }
                }

                if (this.dateIndex - this.dImage > -1
                    && this.dateIndex - this.dImage < this.imageArr.length
                    && this.imageArr[this.dateIndex - this.dImage].images) {
                    //    当当天没有笔记时 查看是否有图片，有的话就显示图片的地理位置
                    this.$store.state.dayImages = this.imageArr[this.dateIndex - this.dImage].images
                    for (let dayImage of this.$store.state.dayImages) {
                        console.log('dayImage', dayImage)
                        if (dayImage.lnglat) {
                            console.log('dayImage.lnglat', dayImage.lnglat)
                            this.$bus.$emit('toPoint', dayImage.lnglat.split(',')[0], dayImage.lnglat.split(',')[1], '', dayImage.createTime)
                            break
                        }
                    }
                }


                // this.$store.state.dayImages = this.$store.state.fileList.filter((i) => i.createTime.split(" ")[0] == createTime.split(" ")[0])
                // console.log('timelineChange exec', createTime, lnglat, title)
                /*1.同步笔记 更新地图上的中心点*/
                /*2.同步图片列表 更新地图上的中心点*/

                // console.log(this.$store.state.dayImages)
            },
            formatTooltip(val) {
                /*if (!val) {
                    val = 0
                }*/
                /*优先返回笔记的标题作为显示*/
                if (this.dateIndex - this.dNote > -1 && this.dateIndex - this.dNote < this.noteArr.length) {
                    let title = this.noteArr[this.dateIndex - this.dNote].title
                    return title ? title : '无内容';
                } else if (this.dateIndex - this.dImage > -1 && this.dateIndex - this.dImage < this.imageArr.length && this.imageArr[this.dateIndex - this.dImage].images) {
                    return '图片'
                }

                return '无内容';
            },
            /*通过title和 day 来确定index*/
            setDateIndex(title, createTime) {
                for (let i = 0; i < this.noteArr.length - 1; i++) {
                    if (this.noteArr[i].createTime == createTime && this.noteArr[i].title == title) {
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

    .timeLine .el-button {
        padding: 0px;
        margin-left: 0px !important;
        color: #000000;
        border: 1px solid #ffffff;
    }
</style>