<template>
    <!--文件-->
    <el-container>
        <el-header style="margin: 10px 0px 2px 0px">
            <!--当前大图详细信息的显示-->
            <imageInfo v-if="showImageInfo"></imageInfo>

            <!--年月日 图片上传 大中小 视图  当前图片分组名称-->
            <div style="text-align: center;">
                <el-button @click="showImageByTimeType('year')" :disabled="disabledControl" style="float:left;"
                           size="mini" round>年
                </el-button>
                <el-button @click="showImageByTimeType('month')" :disabled="disabledControl" style="float:left;"
                           size="mini" round>月
                </el-button>
                <el-button @click="showImageByTimeType('day')" :disabled="disabledControl" style="float:left;"
                           size="mini" round>日
                </el-button>
                <!--项目标题-->
                <strong>{{$store.state.listTitle}}</strong>
                <span style="color: rgba(40,59,55,0.77)">(共{{currentImagesCount}}条)</span>

                <!--小视图-->
                <el-tooltip class="item" style="float: right" content="小视图" placement="bottom">
                    <i @click="changeViewScale('small')" class="el-icon-s-grid"
                       :style="{borderBottom: imageScale == '200px'?'4px solid blue':''}" style="font-size: 30px;"></i>
                </el-tooltip>
                <el-tooltip class="item" style="float: right" content="中视图" placement="bottom">
                    <i @click="changeViewScale('medium')" class="el-icon-menu"
                       :style="{borderBottom: imageScale == '400px'?'4px solid blue':''}" style="font-size: 30px"></i>
                </el-tooltip>
                <el-tooltip class="item" style="float: right" content="大视图" placement="bottom">
                    <i @click="changeViewScale('big')" class="el-icon-s-platform"
                       :style="{borderBottom: imageScale == '600px'?'4px solid blue':''}" style="font-size: 30px"></i>
                </el-tooltip>
                <!--图片排序-->
                <el-tooltip class="item" style="float: right" content="逆序" placement="bottom">
                    <i @click="sortClick" class="el-icon-sort"
                       :style="{borderBottom: !$store.state.sortWay.reverse?'4px solid blue':''}"
                       style="font-size: 30px;"></i>
                </el-tooltip>

                <!--图片上传-->
                <el-tooltip class="item" style="float: right" content="上传" placement="bottom">
                    <el-upload
                            action="http://lpgogo.top/api/admin/file/uploadFileAndInsert"
                            :on-success="uploadSuccess"
                            :on-error="uploadFail"
                            :show-file-list="false"
                            multiple>
                        <i slot="default" class="el-icon-upload" @click="uploadClick"
                           :style="{color: uploading ? '#1d9351' : '#000000'}" style="font-size: 30px;"></i>
                    </el-upload>
                </el-tooltip>
            </div>

            <!--题头信息显示  照片的总数量-->
            <div v-if="checkedImages.length > 0" class="imageTitle" style="margin-left: 25px">
                <span>已选中 {{checkedImages.length}}条 </span>
                <!--批量删除-->
                <el-button @click="deleteImageBatch" size="mini" style="margin-left: 10px;padding: 0px">
                    <i class="el-icon-delete" style="font-size: 25px"></i>
                </el-button>

            </div>

            <!--回收站信息显示-->
            <div class="imageTitle" style="left: 50%">
                <el-button @click="clearPictures" v-if="$store.state.currentNoteBook.title == '回收站'" size="mini"
                           :disabled="disabledControl"
                           type="danger" round style="margin-left: 10px;">
                    清空回收站
                </el-button>
                <!-- 恢复所有-->
                <el-button @click="recoverPictures" v-if="$store.state.currentNoteBook.title == '回收站'" size="mini"
                           :disabled="disabledControl"
                           type="primary" round style="margin-left: 10px;">
                    {{checkedImages.length > 0 ?'恢复选中':'恢复所有' }}
                </el-button>

            </div>
        </el-header>

        <!--图片列表 -->
        <el-main style="padding: 0px 0  0 10px">
            <el-scrollbar class="page-scroll">
                <el-timeline style="padding: 7px">
                    <el-timeline-item
                            v-for="(image,index) in $store.state.currentImageList"
                            :key="index"
                            placement="top"
                            :color="$store.state.currentIndex == index ? '#0bbd87': ''"
                            @click.native="$store.state.currentIndex = index"
                            @mouseenter.native="enterIndex = index;"
                            :timestamp="image.createTime + ' ' + image.images.length + '张照片'">
                        <!--列表区  标题  标签  内容-->
                        <!--    <span style="color: #1a1a17">{{image.images.length}} 张照片</span>-->
                        <!--位置-->
                        <a href="#/map" @click="imageToMap(index)"
                           style="font-size: mini;color:#49a2de">
                            <i v-if="image.location" class="el-icon-location"></i>
                            {{ image.location}}
                        </a>
                        <el-row :style="{  backgroundColor:getBgColor(index),border:$store.state.currentIndex === index ? '1px solid #C3E5F5': '1px solid #D7DADC'}"
                                style="padding-left: 5px;border: 1px solid #D7DADC;border-radius: 5px;">
                            <!--缩略图-->
                            <el-row>
                                <el-checkbox v-model="image.checkedAll" v-if="checkedImages.length > 0"
                                             @change="handleCheckAllChange">全选
                                </el-checkbox>
                                <el-checkbox-group v-model="image.checkedImages" @change="handleCheckedImagesChange"
                                                   class="imgItem">
                                    <div v-for="(img,indexInner) in image.images">
                                        <div class="imageIcon">

                                            <!--照片多选框 鼠标移入时会显示  当选中一个后其余所有的都出现选框-->
                                            <el-checkbox
                                                    v-if="$store.state.currentImageId == img.id || checkedImages.length > 0"
                                                    :label="img" :key="img.id" class="imageCheck"><br>
                                                <!-- 多选框不显示label 只要在里面加上<br>就OK了 -->
                                            </el-checkbox>


                                            <!-- 500px 为大视图 直接显示原图 会有卡顿    "-->
                                            <!--用作解决大图预览的备选-->
                                            <!--<el-image-viewer-->
                                            <!--        v-show="imageListShow && $store.state.currentImageUrlList.length > 0"-->
                                            <!--        :on-close="()=>{imageListShow=false,showImageInfo = false}"-->
                                            <!--        :url-list="$store.state.currentImageUrlList"-->
                                            <!--        :on-switch="onSwitch"-->
                                            <!--/>-->
                                            <!-- @getCurrentImageId="getCurrentImageId"-->
                                            <imageDetail :image-scale="imageScale"
                                                         :img="img"
                                                         :index-inner="indexInner"
                                                         :images="image.images"
                                                         @getShowImageInfo="getShowImageInfo"
                                            >
                                            </imageDetail>
                                            <!-- <el-image :style="{width: imageScale,height: imageScale}"
                                                       style="margin-left:10px;"
                                                       @click="imageClick(img,image.images,indexInner)"
                                                       :src="imageScale == '500px' ? img.url :getThumbnails(img.url,img.title)"
                                                       fit="cover"
                                                       :preview-src-list="$store.state.currentImageUrlList"
                                                       @mouseover="currentImageId = img.id"
                                                       :alt="img.title">
                                             </el-image>-->
                                        </div>
                                    </div>

                                </el-checkbox-group>

                            </el-row>
                        </el-row>
                    </el-timeline-item>
                </el-timeline>
            </el-scrollbar>
        </el-main>
    </el-container>
</template>

<script>
    import cascader from "./Cascader";
    import ElImageViewer from "element-ui/packages/image/src/image-viewer";
    import ImageDetail from "./ImageDetail"
    import imageInfo from "./ImageInfo"

    export default {
        name: "ImageList",
        components: {
            cascader, ElImageViewer, ImageDetail, imageInfo
        },
        data() {
            return {
                showImageInfo: false, //图片信息的展示
                isSortShow: false,
                iconMouseLeave: false,  // 鼠标是否离开了图标区域
                sortPanelMouseLeave: true,// 鼠标是否离开了排序面板区域
                imageScale: "200px", //视图大小  默认为小等视图
                currentImageId: "", //当前图片id
                enterIndex: 0, // 鼠标移入的index
                imageUploadLastTime: 0, //上传图片的定时器
                uploading: false, //控制上传的状态
            }
        },

        computed: {
            /*当前类所含照片的数量*/
            currentImagesCount() {
                return this.$store.state.currentImageList.reduce((total, currentValue) =>
                    total + currentValue.images.length, 0);
            },
            /*图标禁用*/
            disabledControl() {
                return this.$store.state.currentImageList.length == 0
            },

            star: {
                get: function () {
                    return this.$store.state.currentImage.star
                },
                set: function (newValue) {
                    this.$store.state.currentImage.star = newValue

                }
            },
            /*选中的照片id 集合*/
            checkedImages() {
                let checkedImages = this.$store.state.currentImageList.reduce((t, c) => {
                    t.push(...c.checkedImages)
                    return t
                }, [])
                return checkedImages
            }
        },
        methods: {
            imageToMap(index) {
                this.$store.state.currentIndex = index
                this.$bus.$emit("imageToMap")
            },
            /*文件上传成功时的钩子 response.data 为单个picture*/
            uploadSuccess(response, file, fileList) {
                // console.log(response, file, fileList)
                console.log('正在上传')
                this.uploading = !this.uploading
                this.$store.state.uploadImageList.push(response.data)
                /*使用定时器来检测是否全部上传完毕*/
                this.tool.setTimeoutUpdate(this.initUploadImageList, this.imageUploadLastTime, this.$store.state.uploadImageList)
                // console.log(this.$store.state.uploadImageList)
            },
            /*文件上传失败时的钩子	*/
            uploadFail(err, file, fileList) {
                console.log('上传失败')
                this.$message({type: 'error', message: '上传失败!', duration: 1000,});
            },
            /*初始化上传页面*/
            initUploadImageList(dayImages) {
                this.imageUploadLastTime = setTimeout(() => {
                    console.log('')
                    dayImages = this.tool.groupImages("day", dayImages)
                    this.$store.state.currentImageList = dayImages
                    this.$store.state.listTitle = '本次上传'
                    /*更新所有图片的数据 涉及到排序所以直接请求服务器*/
                    this.https.getFiles().then(({data}) => {
                        this.$store.state.fileList = data.data;
                    })
                    this.$message({type: 'success', message: '上传成功!', duration: 1000,});
                }, 1000)
            },
            uploadClick() {
                console.log('清空上一次的列表')
                /*清空上一次的列表*/
                this.$store.state.uploadImageList = []
            },
            /*全选*/
            handleCheckAllChange(allChecked) {
                /*添加当前日期聚合的照片id到选中列表中*/
                if (allChecked) {
                    this.$store.state.currentImageList[this.$store.state.currentIndex].checkedImages = []
                    this.$store.state.currentImageList[this.$store.state.currentIndex].checkedImages = this.$store.state.currentImageList[this.$store.state.currentIndex].images
                } else { /*移除当前选中的list*/
                    this.$store.state.currentImageList[this.$store.state.currentIndex].checkedImages = []
                }
                console.log(this.checkedImages.length)
            },
            handleCheckedImagesChange(checkedArray) {
                /*当前时间图片都在已选中时 将当前选中checkAll 设置为true 否则为false*/
                let checkedCount = checkedArray.length;
                this.$store.state.currentImageList[this.$store.state.currentIndex].checkedAll = checkedCount === this.$store.state.currentImageList[this.$store.state.currentIndex].images.length;
                console.log(this.checkedImages.length)
            },
            /*清空回收站图片*/
            clearPictures() {
                this.$confirm('此操作将清空回收站所有图片是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    center: true
                }).then(() => {
                    this.clearRecyleBin()
                    this.$message({type: 'success', message: '成功!', duration: 1000,});
                }).catch(() => {
                    this.$message({
                        type: 'info',
                        message: '已取消',
                        duration: 1000,
                    });
                });
            },
            recoverPictures() {
                /*1.修改所有已经移入回收站的图片 */
                /*4.后台 将 wastepaper设置为 false 重新请求数据*/
                let ids = this.checkedImages.map(x => x.id)
                let count = 0 //计数
                if (this.checkedImages.length > 0) { //恢复选中照片
                    this.https.recoverSelectedPictures(ids).then(({data}) => {
                        console.log("恢复所选的图片 成功", data);
                        this.$store.state.fileList = data.data; // 进入的笔记本列表数据
                        this.$store.state.starImageList = this.$store.state.fileList.filter((i) => i.star == true)
                        this.$store.state.wastepaperPictureList = this.$store.state.wastepaperPictureList.filter((i) => ids.indexOf(i.id) == -1)

                        this.$store.state.currentImageList.forEach((i) => {
                            i.images = i.images.filter((inner) => {
                                if (ids.indexOf(inner.id) == -1) {
                                    count += 1
                                    if (count == ids.length) return /*跳出 停止循环*/
                                }
                                return ids.indexOf(inner.id) == -1
                            })
                        })
                    })
                } else { //恢复所有
                    this.https.recoverAllPictures().then(({data}) => {
                        console.log("成功 恢复所有删除的图片 ", data);
                        this.$store.state.fileList = data.data; // 进入的笔记本列表数据
                        this.$store.state.starImageList = this.$store.state.fileList.filter((i) => i.star == true)
                        /*2.清空当前回收站*/
                        this.$store.state.wastepaperPictureList = []
                        this.$store.state.currentImageList = []
                    })
                }

            },
            getCurrentImageId(currentImageId) {
                this.currentImageId = currentImageId
            },
            getShowImageInfo(showImageInfo) {
                this.showImageInfo = showImageInfo
            },

            /*批量移动图片到回收站 或彻底删除*/
            deleteImageBatch() {
                let ids = this.checkedImages.map(x => x.id)
                let checkedImages = this.checkedImages // 为异步操作保存当前值
                /*重新请求数据 避免繁琐的判断和调整*/
                this.https.deleteImageBatch(ids).then(({data}) => {
                    let addImageToRecyleBin = true
                    let dayImages = []
                    console.log(this.$store.state.currentNoteBook.id == 10 ? "彻底删除图片" : "批量移动图片到回收站", data);
                    this.$store.state.fileList = data.data; // 进入的图片列表数据
                    this.$store.state.starImageList = this.$store.state.fileList.filter((i) => i.star == true)

                    switch (this.$store.state.currentNoteBook.id) {
                        case 8: //全部图片
                            dayImages = this.$store.state.fileList;
                            break;
                        case 9: //收藏图片
                            dayImages = this.$store.state.starImageList;
                            break;
                        case 10://回收站下的批量删除  彻底删除
                            this.$store.state.wastepaperPictureList = this.$store.state.wastepaperPictureList.filter((i) => ids.indexOf(i.id) == -1)
                            dayImages = this.$store.state.wastepaperPictureList
                            addImageToRecyleBin = false
                            break;
                        case 14: //人物
                            dayImages = this.$store.state.currentPerson.pictureList.filter((i) => ids.indexOf(i.id) == -1)
                            break;
                    }
                    /*重新按天进行聚合*/
                    dayImages = this.tool.groupImages("day", dayImages)
                    this.$store.state.currentImageList = dayImages

                    /*2.将照片加入到回收站*/
                    if (addImageToRecyleBin) {
                        checkedImages = checkedImages.map(x => {
                            x.wastepaper = true
                            return x
                        })
                        this.$store.state.wastepaperPictureList.unshift(...checkedImages)
                    }
                })
            },
            clearRecyleBin() {
                let ids = this.$store.state.wastepaperPictureList.map(x => x.id)
                /*重新请求数据 避免繁琐的判断和调整*/
                this.https.deleteImageBatch(ids).then(({data}) => {
                    console.log(data);
                })
                this.$store.state.currentImageList = []
                this.$store.state.wastepaperPictureList = []
            },
            starClick(img) {
                if (img.wastepaper) return
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
                        this.imageScale = "600px";
                        break;
                    case "medium":
                        this.imageScale = "400px";
                        break;
                    case "small":
                        this.imageScale = "200px";
                        break;
                    default:
                        this.imageScale = "200px"
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
            /*控制列表颜色*/
            getBgColor(index) {
                /* 若当前 index 被选中 则直接返回选中颜色 进入就返回 hover颜色 其他情况就都返回白色(背景遮挡色)*/
                if (this.$store.state.currentIndex == index) return "#E6E6E6" /*选中时的灰色*/
                if (this.enterIndex == index) return "#EDF6FD" /*天蓝色*/
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
                this.$store.state.starImageList = this.$store.state.starImageList.reverse()
                this.$store.state.currentImageList = this.$store.state.currentImageList.reverse()
                this.$store.state.fileList = this.$store.state.fileList.reverse()
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
                        self.showImageInfo = false
                    }
                })
            })
        }
    }
</script>

<style>
    /*时间线的位置*/
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
        left: 80%;
        top: 10%;
        z-index: 1000
    }

    /*控制图片多选的图标*/
    .imageIcon .imageCheck {
        font-size: 18px;
        position: absolute;
        left: 80%;
        bottom: 10%;
        z-index: 1000
    }

    /*鼠标经过图片变大*/
    /* .el-image {
         cursor: pointer;
         transition: all 0.6s;
     }

     .el-image:hover {
         transform: scale(1.02);
     }*/

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

    /*图片上传*/
    .el-upload--picture-card {
        width: 32px !important;
        height: 28px !important;
    }

    .el-upload--picture-card i {
        font-size: 28px !important;
        color: #000000 !important;
    }
</style>