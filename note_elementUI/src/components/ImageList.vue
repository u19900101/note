<template>
    <!--文件-->
    <el-container>
        <el-header style="margin: 10px 0px">
            <!--当前大图详细信息的显示-->
            <div v-if="imageInfo" class="imgInfo">
                <!--拍摄日期 -->
                <div class="imgTime">
                    <el-date-picker
                            style="color: #1d9351"
                            v-model="createTime"
                            editable
                            type="datetime"
                            placeholder="选择日期时间"
                            :default-time="createTime.split(' ')[0]">
                    </el-date-picker>
                </div>
                <!--图片名称-->
                <el-input clearable v-model="imageName" style="width: 182px" placeholder="请输入名称">
                    <template slot="append">.{{$store.state.currentImage.title.split(".")[1]}}</template>
                </el-input>
                <!--大小、位置、尺寸-->
                <div class="info" :style="{width: getBt($store.state.currentImage.size.toString()) + 'px'}">
                    {{getImgageSize($store.state.currentImage.size)}}
                </div>
                <div class="info" :style="{width: getBt($store.state.currentImage.location) + 'px'}"
                     v-if="$store.state.currentImage.location">
                    <i class="el-icon-location"></i>
                    <a :href="'http://maps.google.com/maps?z=6&q=' + $store.state.currentImage.lnglat"
                       style="font-size: mini;color:#49a2de">{{$store.state.currentImage.location}}</a>
                </div>
                <div class="info" :style="{width: getBt($store.state.currentImage.widthH) + 'px'}">
                    {{$store.state.currentImage.widthH}}
                </div>
            </div>

            <!--年月日 大中小 视图-->
            <div>
                <el-button @click="showImageByTimeType('year')">年</el-button>
                <el-button @click="showImageByTimeType('month')">月</el-button>
                <el-button @click="showImageByTimeType('day')">日</el-button>
                <!--小视图-->
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
                <!--图片排序-->
                <el-tooltip class="item" style="float: right" content="逆序" placement="bottom">
                    <i @click="sortClick" class="el-icon-sort"
                       :style="{borderBottom: !$store.state.sortWay.reverse?'4px solid blue':''}"
                       style="font-size: 30px;"></i>
                </el-tooltip>
            </div>

            <!--笔记本名称-->
            <el-row style="text-align: center">
                {{$store.state.currentNoteBook.title}}
                <span style="color: rgba(40,59,55,0.77)">(共{{$store.state.currentImagesCount}}条)</span>
            </el-row>
        </el-header>

        <!--图片列表 -->
        <el-main style="padding: 0 0  0 10px">
            <el-scrollbar class="page-scroll">
                <div v-for="(image,index) in $store.state.currentImageList">
                    <!--列表区  标题  标签  内容-->
                    <el-row @click.native="fileClick(image,index)"
                            @mousedown.native="$store.state.currentIndex = index"
                            @mouseenter.native="enterIndex = index"
                            :id="index"
                            :style="{  backgroundColor:getBgColor(index),border:$store.state.currentIndex === index ? '1px solid #C3E5F5': '1px solid #D7DADC'}"
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
                                                  ? (image.updateTimeAlias ? image.updateTimeAlias:image.updateTime)
                                                  : (image.createTimeAlias ? image.createTimeAlias:image.createTime)
                                                  }}</span>
                            <span style="margin-left: 10px">{{image.images.length}} 张照片</span>
                        </el-row>
                        <!--位置-->
                        <el-row>
                            <!--30.614422,114.301961-->
                            <!--百度地图-->
                            <!-- <a :href="'http://api.map.baidu.com/geocoder?location=' + note.lnglat + '&coord_type=gcj02&output=html&src=webapp.baidu.openAPIdemo'"
                                     style="font-size: mini;color:#49a2de"> {{ note.location}}</a>-->
                            <a :href="'http://maps.google.com/maps?z=6&q=' + image.lnglat"
                               style="font-size: mini;color:#49a2de">
                                <i v-if="image.location" class="el-icon-location"></i>
                                {{ image.location}}
                            </a>
                        </el-row>
                        <!--缩略图-->
                        <el-row class="imgItem">
                            <div v-for="(img,index) in image.images">
                                <div class="imageIcon">
                                    <!--图片收藏图标-->
                                    <!--收藏-->
                                    <i v-if="img.star" @click="starClick(img)" class="iconfont icon-like1"
                                       style="color: red;"></i>
                                    <div v-if="currentImageId == img.id" @click="starClick(img)">
                                        <!--取消收藏-->
                                        <i v-if="!img.star" class="iconfont icon-like"></i>
                                    </div>
                                    <!-- 500px 为大视图 直接显示原图 会有卡顿 @mouseover="mouseOverImage(index)"-->
                                    <el-image :style="{width: imageScale,height: imageScale}" style="margin-left:10px;"
                                              @click="imageClick(img,image.images,index)"
                                              :src="imageScale == '500px' ? img.url :getThumbnails(img.url,img.title)"
                                              fit="cover"
                                              @mouseover="currentImageId = img.id"
                                              :preview-src-list="$store.state.currentImageUrlList"
                                              :alt="img.title"/>
                                </div>
                            </div>
                        </el-row>
                    </el-row>
                </div>
            </el-scrollbar>
        </el-main>
    </el-container>
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
                imageInfo: false, //图片预览
                isSortShow: false,
                iconMouseLeave: false,  // 鼠标是否离开了图标区域
                sortPanelMouseLeave: true,// 鼠标是否离开了排序面板区域
                imageScale: "100px", //视图大小  默认为小等视图
                currentImageId: "", //当前图片id
                imageCreateTimeLastTime:0, //修改照片名称的定时器
                imageNameLastTime:0, //修改照片名称的定时器
            }
        },
        computed: {
            imageName: {
                get: function () {
                    return this.$store.state.currentImage.title.split('.')[0]
                },
                set: function (newImageName) {
                    this.$store.state.currentImage.title = newImageName + '.' + this.$store.state.currentImage.title.split('.')[1]
                    if(newImageName.length > 0){
                        this.setTimeoutUpdate(this.updateImageName,this.imageNameLastTime)
                    }
                }
            },
            createTime: {
                get: function () {
                    return this.$store.state.currentImage.createTime
                },
                set: function (newValue) {
                    let formatTime = dayjs(newValue).format('YYYY-MM-DD HH:mm:ss')
                    this.$store.state.currentImage.createTime = formatTime
                    this.setTimeoutUpdate(this.updateImageCreateTime,this.imageCreateTimeLastTime,newValue)
                }
            },
            star: {
                get: function () {
                    return this.$store.state.currentImage.star
                },
                set: function (newValue) {
                    this.$store.state.currentImage.star = newValue

                }
            },
        },
        methods: {
            setTimeoutUpdate(funcName,lastTimeType,...param){
                if (lastTimeType == 0) {
                    funcName(...param)
                } else {
                    clearTimeout(lastTimeType)
                    funcName(...param)
                }
            },
            updateImageName(){
                this.imageNameLastTime = setTimeout(() => {
                    this.https.updateImage({id: this.$store.state.currentImage.id, title: this.$store.state.currentImage.title}).then(({data}) => {
                        console.log("修改图片名称成功", data);
                    })
                }, 2000)
            },
            updateImageCreateTime(createTime){
                this.imageCreateTimeLastTime = setTimeout(() => {
                    this.https.updateImage({id: this.$store.state.currentImage.id, createTime: createTime}).then(({data}) => {
                        console.log("修改图片创建时间成功", data);
                    })
                }, 5000)
            },
            starClick(img) {
                img.star = !img.star
                this.$message({
                    message: img.star ? '已收藏' : '取消收藏',
                    type: img.star ? 'success' : 'info',
                    duration: 1000,  //显示时间, 毫秒。设为 0 则不会自动关闭 默认3000
                    center: true
                });
                /*更新收藏图片的数据*/
                if (img.star) {
                    if (this.$store.state.starImageList.indexOf(img) == -1) {
                        this.$store.state.starImageList.push(img)
                    }
                    /* 2.更新所有图片 加入收藏*/
                    this.$store.state.fileList.forEach((n) => {
                        if (n.id == img.id) n.star = true
                    })
                } else {
                    this.$store.state.starImageList = this.$store.state.starImageList.filter((n) => n.id != img.id)
                    /* 2.更新所有笔记*/
                    this.$store.state.fileList.forEach((n) => {
                        if (n.id == img.id) n.star = false
                    })
                }

                /*更新数据库*/
                this.https.updateImage({id: img.id, star: img.star}).then(({data}) => {
                    console.log("修改数据库成功", data);
                })
            },
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
                let imageList = []
                /*将之前按照年月日分组的重新划分*/
                this.$store.state.currentImageList.forEach((i) => {
                    imageList.push(...i.images)
                })
                let dayImages = this.tool.groupImages(timeType, imageList)
                this.$store.state.currentImageList = dayImages
            },
            /*封装缩略图的链接*/
            getThumbnails(url, title) {
                let kk = url.replace(title, title.split(".")[0] + "_thumbnails." + title.split(".")[1])
                return kk
            },
            imageClick(img, imageList, index) {

                this.imageInfo = true
                this.$store.state.currentImageUrl = img.url
                this.$store.state.currentImage = img
                let currentImageUrlList = []
                imageList.forEach((i) => {
                    currentImageUrlList.push(i.url)
                })
                /*移动数组*/
                this.$store.state.currentImageUrlList = [...currentImageUrlList.slice(index), ...currentImageUrlList.slice(0, index)]
                // let k = this.$store.state.currentImageUrlList
                // console.log(this.$store.state.currentImageUrlList.length)
                /*关闭图片预览时 不显示图片其他信息*/
                this.$nextTick(() => {
                    // 获取遮罩层关闭按钮dom
                    let domImageMask = document.querySelector(".el-image-viewer__close");
                    if (!domImageMask) {
                        return;
                    }
                    domImageMask.addEventListener("click", () => {
                        this.imageInfo = false
                    });
                    /*点击空白处关闭图片显示*/
                    let domImageMask4 = document.querySelector(".el-image-viewer__mask");
                    if (!domImageMask4) {
                        return;
                    }
                    domImageMask4.addEventListener("click", () => {
                        this.imageInfo = false
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
                return char.length * 6 + 40;
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
            /*图片逆序*/
            sortClick() {
                this.$store.state.sortWay.reverse = !this.$store.state.sortWay.reverse
                this.$store.state.currentImageList = this.$store.state.currentImageList.reverse()
                // 修改全局变量 联动noteList变化
                this.https.updateSortWay(this.$store.state.sortWay).then(({data}) => {
                    console.log(data)
                })
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
                        self.imageInfo = false
                    }
                })
            })
        }
    }
</script>

<style>
    /*照片名称 input 后缀*/
    .el-input-group__append, .el-input-group__prepend {
        padding: 0 3px !important;
    }

    .el-input--suffix .el-input__inner {
        padding-right: 0px !important;
    }

    /*照片名称*/
    .imgInfo .el-input__inner {
        padding: 0 !important;
        text-align: center;
        font-size: 16px;
        color: #1a1a17;
    }


    /*图片的收藏爱心*/
    .imageIcon {
        position: relative;
    }

    .imageIcon i {
        font-size: 18px;
        position: absolute;
        left: 85px;
        top: 5px;
        z-index: 1000
    }

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
        top: 40px;
    }

    .imgInfo .info {
        height: 40px;
        line-height: 40px;
        background-color: #ffffff;
        border: 1px solid #D7DADC;
        border-radius: 5px;
        text-align: center;
        /* margin-left: auto;*/ /*右对齐*/
    }

    /*设置时间图标的位置*/
    .el-input__prefix {
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
        margin-left: 0px !important;
    }

    /*时间框 内容的位置*/
    .imgTime .el-input__inner {
        padding-left: 8px !important;
        padding-right: 0px !important;
        width: 182px !important;
        color: #000000 !important;
        font-size: 16px !important;
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