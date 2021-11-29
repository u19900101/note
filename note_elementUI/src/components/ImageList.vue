<template>
    <!--文件-->
    <div>
        <!--当前大图详细信息的显示-->
        <div v-if="imagePre" class="imgInfo">
            <div class="info" :style="{width: getBt($store.state.currentImage.title) + 'px'}">{{$store.state.currentImage.title}}</div>
            <!--拍摄日期 -->
            <div class="imgInfo">
                <el-date-picker
                        style="color: #1d9351"
                        v-model="createTime"
                        editable
                        type="datetime"
                        placeholder="选择日期时间"
                        :default-time="createTime.split(' ')[0]">
                </el-date-picker>
            </div>

            <span class="info" :style="{width: getBt($store.state.currentImage.size.toString()) + 'px'}">{{getImgageSize($store.state.currentImage.size)}}</span>
            <span class="info" :style="{width: getBt($store.state.currentImage.location) + 'px'}" v-if="$store.state.currentImage.location">
                <i  class="el-icon-location"></i>
                  <a :href="'http://maps.google.com/maps?z=6&q=' + $store.state.currentImage.lnglat"
                     style="font-size: mini;color:#49a2de">{{$store.state.currentImage.location}}</a>
            </span>
            <span class="info" :style="{width: getBt($store.state.currentImage.widthH) + 'px'}">{{$store.state.currentImage.widthH}}</span>
        </div>


        <div>
            <el-button @click="showImageByTimeType('year')">年</el-button>
            <el-button @click="showImageByTimeType('month')">月</el-button>
            <el-button @click="showImageByTimeType('day')">日</el-button>
            <el-tooltip class="item" style="float: right" content="小视图" placement="bottom">
                <i @click="changeViewScale('small')" class="el-icon-s-grid"
                   :style="{borderBottom: imageScale == '100px'?'4px solid blue':''}" style="font-size: 30px;"></i>
            </el-tooltip>
            <el-tooltip class="item" style="float: right" content="中视图" placement="bottom">
                <i @click="changeViewScale('medium')" class="el-icon-menu"
                   :style="{borderBottom: imageScale == '250px'?'4px solid blue':''}" style="font-size: 30px"></i>
            </el-tooltip>
            <el-tooltip class="item" style="float: right" content="大视图" placement="bottom">
                <i @click="changeViewScale('big')" class="el-icon-s-platform"
                   :style="{borderBottom: imageScale == '500px'?'4px solid blue':''}" style="font-size: 30px"></i>
            </el-tooltip>
        </div>


        <el-row>
            <!--笔记本名称-->
            <el-col :span="16" style="text-align: center">{{$store.state.currentNoteBook.title}}
                <span style="color: rgba(40,59,55,0.77)">(共{{$store.state.currentNoteList.length}}条)</span>
            </el-col>
            <el-col :span="8" style="text-align: right;">
                <div class="sortButton">
                    <el-button round
                               @click="sortClick(-1)"
                               @mouseleave.native="iconMouseLeave = true"
                               @mouseenter.native="iconMouseLeave = false">
                        <i class="el-icon-sort"></i>
                    </el-button>
                </div>
            </el-col>
        </el-row>
        <!--笔记排序 级联面板-->
        <cascader :isSortShow="isSortShow"
                  @sortClick="sortClick"
                  @mouseleave.native="sortPanelMouseLeave = true"
                  @mouseenter.native="sortPanelMouseLeave = false">
        </cascader>
        <!--文件列表-->
        <el-container style="height: 729px;">
            <el-scrollbar class="page-scroll">
                <div v-for="(note,index) in $store.state.currentNoteList">
                    <!--列表区  标题  标签  内容-->
                    <el-row @click.native="fileClick(note,index)"
                            @mousedown.native="$store.state.currentIndex = index"
                            @mouseenter.native="enterIndex = index"
                            :id="index"
                            :style="{  backgroundColor:getBgColor(index),
                                                   border:$store.state.currentIndex === index ? '1px solid #C3E5F5': '1px solid #D7DADC'
                                          }"
                            style="padding-left: 5px;border: 1px solid #D7DADC;border-radius: 5px;">

                        <!--标签-->
                        <el-row>
                            <!-- 给多行省略符 元素动态设置背景色-->
                            <div class="more-line">
                                <span style="color: #49a2de">标签kkk<!--{{getTagList(note)}}--></span>
                            </div>
                        </el-row>

                        <!--时间-->
                        <el-row>
                            <!--根据排序方式来决定显示的时间类型 note.createTime -->
                            <span style="font-size: mini;color: #49a2de">
                                                  {{ $store.state.sortWay.updateTime
                                                  ? (note.updateTimeAlias ? note.updateTimeAlias:note.updateTime)
                                                  : (note.createTimeAlias ? note.createTimeAlias:note.createTime)
                                                  }}</span>
                            <span style="margin-left: 10px">{{note.images.length}} 张照片</span>
                        </el-row>
                        <!--位置-->
                        <el-row>
                            <!--30.614422,114.301961-->
                            <!--百度地图-->
                            <!-- <a :href="'http://api.map.baidu.com/geocoder?location=' + note.lnglat + '&coord_type=gcj02&output=html&src=webapp.baidu.openAPIdemo'"
                                     style="font-size: mini;color:#49a2de"> {{ note.location}}</a>-->
                            <a :href="'http://maps.google.com/maps?z=6&q=' + note.lnglat"
                               style="font-size: mini;color:#49a2de">
                                <i v-if="note.location" class="el-icon-location"></i>
                                {{ note.location}}
                            </a>
                        </el-row>
                        <!--缩略图-->
                        <el-row class="imgItem">
                            <div v-for="(img,index) in note.images">
                                <!-- 500px 为大视图 直接显示原图 会有卡顿-->
                                <el-image :style="{width: imageScale,height: imageScale}" style="margin-left:10px;"
                                          @click.stop="imageClick(img,note.images,index)"
                                          :src="imageScale == '500px' ? img.url :getThumbnails(img.url,img.title)"
                                          fit="cover"
                                          :preview-src-list="$store.state.currentImageUrlList"
                                          :alt="img.title"
                                />
                            </div>
                        </el-row>

                    </el-row>
                </div>
            </el-scrollbar>
        </el-container>
    </div>
</template>

<script>
    import cascader from "./Cascader";
    let dayjs = require('dayjs');
    export default {
        name: "ImageList",
        components: {
            cascader
        },
        data() {
            return {
                imagePre: false, //图片预览
                isSortShow: false,
                iconMouseLeave: false,  // 鼠标是否离开了图标区域
                sortPanelMouseLeave: true,// 鼠标是否离开了排序面板区域
                imageScale: "250px", //视图大小  默认为中等视图
            }
        },
        computed:{
            createTime: {
                get: function () {
                    return this.$store.state.currentImage.createTime
                },
                set: function (newValue) {
                    let formatTime = dayjs(newValue).format('YYYY-MM-DD HH:mm:ss')
                    this.$store.state.currentImage.createTime = formatTime
                   /* console.log('更新笔记创建时间', this.$store.state.currentNote.title,formatTime)
                    this.https.updateNote({
                        id: this.$store.state.currentNote.id,
                        createTime: newValue
                    }).then(({data}) => {
                        console.log("修改数据库成功", data);
                    })*/
                }
            },
        },
        methods: {
            changeViewScale(viewScale) {
                switch (viewScale) {
                    case "big":
                        this.imageScale = "500px";
                        break;
                    case "medium":
                        this.imageScale = "250px";
                        break;
                    case "small":
                        this.imageScale = "100px";
                        break;
                    default:
                        this.imageScale = "100px"
                }
                console.log(viewScale)
            },
            /*照片日视图*/
            showImageByTimeType(timeType) {
                let imageList = this.$store.state.fileList
                let dayImages = this.tool.groupImages(timeType, imageList)
                this.$store.state.currentNoteList = this.$store.state.sortWay.reverse ? [...dayImages].reverse() : dayImages
            },
            /*封装缩略图的链接*/
            getThumbnails(url, title) {
                let kk = url.replace(title, title.split(".")[0] + "_thumbnails." + title.split(".")[1])
                return kk
            },
            imageClick(img, imageList, index) {
                this.imagePre = true
                this.$store.state.currentImageUrl = img.url
                this.$store.state.currentImage = img
                let currentImageUrlList = []
                imageList.forEach((i) => {
                    currentImageUrlList.push(i.url)
                })
                /*移动数组*/
                this.$store.state.currentImageUrlList = [...currentImageUrlList.slice(index), ...currentImageUrlList.slice(0, index)]

                /*关闭图片预览时 不显示图片其他信息*/
                this.$nextTick(() => {
                    // 获取遮罩层关闭按钮dom
                    let domImageMask = document.querySelector(".el-image-viewer__close");
                    if (!domImageMask) {
                        return;
                    }
                    domImageMask.addEventListener("click", () => {
                        this.imagePre = false
                    });
                    /*点击空白处关闭图片显示*/
                    let domImageMask4 = document.querySelector(".el-image-viewer__mask");
                    if (!domImageMask4) {
                        return;
                    }
                    domImageMask4.addEventListener("click", () => {
                        this.imagePre = false
                    });


                    /*上一张*/
                    let domImageMask3 = document.querySelector(".el-image-viewer__prev");
                    if (!domImageMask3) {
                        return;
                    }
                    domImageMask3.addEventListener("click", () => {
                        index -= 1
                        if (index == -1) index = imageList.length - 1
                        this.$store.state.currentImage = imageList.slice(index, index + 1)[0]
                    });

                    /*下一张*/
                    let domImageMask2 = document.querySelector(".el-image-viewer__next");  // 获取遮罩层关闭按钮dom
                    if (!domImageMask2) {
                        return;
                    }
                    domImageMask2.addEventListener("click", () => {
                        index += 1
                        if (index == imageList.length) index = 0
                        this.$store.state.currentImage = imageList.slice(index, index + 1)[0]
                    });


                })
            },
            fileClick(imageList, index) {
                /*页面显示*/
                // console.log(imageList)

            },
            /*获取带有中文字符的长度  一个中文的宽度对应两个英文的宽度*/
            getBt(str) {
                let char = str.replace(/[^\x00-\xff]/g, '**');
                return char.length*6 + 40;
            },
            getImgageSize(byteNum) {
                if (byteNum < 1024 * 1024) {
                    let kb = (byteNum / 1024).toString()
                    return kb.substring(0, kb.indexOf(".") + 2) + "kb"
                }

                if (byteNum >= 1024 * 1024) {
                    let kb = (byteNum / 1024 / 1024).toString()
                    return kb.substring(0, kb.indexOf(".") + 2) + "Mb"
                }
            },
            /*控制列表颜色*/
            getBgColor(index) {
                /* 若当前 index 被选中 则直接返回选中颜色 进入就返回 hover颜色 其他情况就都返回白色(背景遮挡色)*/
                if (this.$store.state.currentIndex == index) return "#E6E6E6"
                if (this.enterIndex == index) return "#EDF6FD"
                return "#ffffff"
            },
            // 当鼠标离开排序区(图标区 + 排序面板区 )后 点击任意位置 排序框消失
            mouseDown() {
                if (this.iconMouseLeave && this.sortPanelMouseLeave) {
                    // console.log('离开')
                    this.sortClick(-1)
                } else {
                    // console.log('进入')
                }
            },

            sortClick(sortType) {
                // 排序面板出现就开始监控鼠标按下时间
                if (sortType == -1) {
                    this.isSortShow = !this.isSortShow
                    if (this.isSortShow) {
                        document.addEventListener('mousedown', this.mouseDown)
                    } else {
                        document.removeEventListener('mousedown', this.mouseDown)
                    }
                } else {
                    console.log('sortType is ', sortType)  // sortType 为 -1 时 进行关闭操作
                }
            },
        },
        mounted() {
            /*esc键关闭图片信息显示*/
            //监听键盘按键事件
            let self = this;
            this.$nextTick(function () {
                document.addEventListener('keyup', function (e) {
                    //此处填写你的业务逻辑即可
                    if (e.keyCode == 27) {
                        self.imagePre = false
                    }
                })
            })
        }
    }
</script>

<style>
    /*鼠标经过图片变大*/
    .el-image {
        cursor: pointer;
        transition: all 0.6s;
    }

    .el-image:hover {
        transform: scale(1.02);
    }

    /*图片布局样式*/
    .imgItem {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
    }

    /*大图预览的比例*/
    .el-image-viewer__img {
        max-height: 90% !important;
    }

    /*遮罩层*/
    .el-image-viewer__wrapper {
        z-index: 1000 !important;
    }

    /*图片的描述信息*/
    .imgInfo {
        display: flex;
        flex-direction: column;
        justify-content: flex-end;
        position: absolute;
        z-index: 2001;
        left: 21px;
        top: 6px;
    }

    .imgInfo .info {
        height: 40px;
        line-height: 40px;
        background-color: #ffffff;
        border: 1px solid #D7DADC;
        border-radius: 5px;
        text-align: center;
        margin-left: auto; /*右对齐*/
    }
    /*设置时间图标的位置*/
    .el-input__prefix{
        left: -3px;
        height: 0px !important;
    }
    .imgInfo .el-input__icon {
        width: 10px !important;
    }
    .el-input__prefix, .el-input__suffix {
        position: absolute;
        top: 0;
        -webkit-transition: all .3s;
        height: 10px;
        margin-top: 1px;
        margin-left: 1px !important;
    }
    /*时间框 内容的位置*/
    .imgInfo .el-input__inner{
        padding-left: 25px !important;
        padding-right: 0px !important;
        width: 182px !important;
        color: #000000 !important;
        font-size:16px !important;
    }

    .el-date-editor.el-input, .el-date-editor.el-input__inner {
        width: 182px !important;
    }
    /*日历框中输入宽度*/
    .el-input--small .el-input__inner {
        width: 150px !important;
    }
        /* 美化列表的滚动条样式*/
    .page-scroll {
        height: 100%;
        width: 100%;
    }

    .page-scroll .el-scrollbar__wrap {
        overflow-x: hidden;
    }

</style>