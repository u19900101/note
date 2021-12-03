<template>
    <!--文件-->
    <el-container>
        <el-header style="margin: 10px 0px 2px 0px">
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
            <div style="text-align: center">
                <el-button @click="showImageByTimeType('year')" style="float:left;" size="mini" round>年</el-button>
                <el-button @click="showImageByTimeType('month')" style="float:left;" size="mini" round>月</el-button>
                <el-button @click="showImageByTimeType('day')" style="float:left;" size="mini" round>日</el-button>

                {{$store.state.currentNoteBook.title}}
                <span style="color: rgba(40,59,55,0.77)">(共{{currentImagesCount}}条)</span>

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

            <!--题头信息显示  照片的总数量-->
            <div v-if="checkedImages.length > 0 " class="imageTitle">
                <span>已选中 {{checkedImages.length}}条 </span>
                <!--批量删除-->
                <el-button @click="deleteImageBatch" size="mini" style="margin-left: 10px;padding: 0px">
                    <i class="el-icon-delete" style="font-size: 25px"></i>
                </el-button>

                <!--批量收藏-->
                <el-button @click="clearPictures" size="mini"
                           style="margin-left: 10px;padding: 0px">
                    <i class="iconfont icon-like1" style="color: red;font-size: 25px"></i>
                </el-button>
                <!--批量取消收藏-->
                <el-button @click="clearPictures" size="mini"
                           style="margin-left: 10px;padding: 0px">
                    <i class="iconfont icon-like" style="font-size: 23px"></i>
                </el-button>
            </div>

            <el-row style="text-align: left;margin-bottom: 20px">
                <!--清空回收站-->
                <el-button @click="clearPictures" v-if="$store.state.currentNoteBook.title == '回收站'" size="mini"
                           type="danger" round style="margin-left: 10px;">
                    清空回收站
                </el-button>
                <!-- 恢复所有-->
                <el-button @click="recoverAllPictures" v-if="$store.state.currentNoteBook.title == '回收站'" size="mini"
                           type="primary" round style="margin-left: 10px;">
                    恢复所有
                </el-button>

            </el-row>
        </el-header>

        <!--图片列表 -->
        <el-main style="padding: 0px 0  0 10px">
            <el-scrollbar class="page-scroll">
                <el-timeline style="padding: 7px">
                    <el-timeline-item
                            v-for="(image,index) in $store.state.currentImageList"
                            :key="index"
                            placement="top"
                            :color="currentIndex == index ? '#0bbd87': ''"
                            @click.native="currentIndex = index"
                            @mouseenter.native="enterIndex = index"
                            :timestamp="image.createTime + ' ' + image.images.length + '张照片'">
                        <!--列表区  标题  标签  内容-->
                        <!--    <span style="color: #1a1a17">{{image.images.length}} 张照片</span>-->
                        <!--位置-->
                        <a :href="'http://maps.google.com/maps?z=6&q=' + image.lnglat"
                           style="font-size: mini;color:#49a2de">
                            <i v-if="image.location" class="el-icon-location"></i>
                            {{ image.location}}
                        </a>
                        <el-row :style="{  backgroundColor:getBgColor(index),border:currentIndex === index ? '1px solid #C3E5F5': '1px solid #D7DADC'}"
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
                                            <!--图片收藏图标-->
                                            <!--收藏-->
                                            <i v-if="img.star" @click="starClick(img)" class="iconfont icon-like1"
                                               style="color: red;"></i>
                                            <!--照片多选框 鼠标移入时会显示  当选中一个后其余所有的都出现选框-->
                                            <el-checkbox v-if="currentImageId == img.id || checkedImages.length > 0"
                                                         :label="img" :key="img.id" class="imageCheck"><br>
                                                <!-- 多选框不显示label 只要在里面加上<br>就OK了 -->
                                            </el-checkbox>

                                            <div v-if="currentImageId == img.id && !img.wastepaper"
                                                 @click="starClick(img)">
                                                <!--鼠标移动到图片时 显示未收藏-->
                                                <i v-if="!img.star" class="iconfont icon-like"></i>
                                            </div>
                                            <!-- 500px 为大视图 直接显示原图 会有卡顿 @mouseover="mouseOverImage(index)"-->
                                            <el-image :style="{width: imageScale,height: imageScale}"
                                                      style="margin-left:10px;"
                                                      @click="imageClick(img,image.images,indexInner)"
                                                      :src="imageScale == '500px' ? img.url :getThumbnails(img.url,img.title)"
                                                      fit="cover"
                                                      @mouseover="currentImageId = img.id"
                                                      :preview-src-list="$store.state.currentImageUrlList"
                                                      :alt="img.title"/>
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
                currentIndex: 0, //当前时间index
                enterIndex: 0, // 鼠标移入的index
                imageCreateTimeLastTime: 0, //修改照片名称的定时器
                imageNameLastTime: 0, //修改照片名称的定时器
                removeIcons: false,// 动态移除 删除和收藏图标
                lastImage: {}, // 上一张图片 用于 动态加载喜欢标签时是否重新创建
            }
        },

        computed: {
            /*当前类所含照片的数量*/
            currentImagesCount() {
                return this.$store.state.currentImageList.reduce((total, currentValue) =>
                    total + currentValue.images.length, 0);
            },
            imageName: {
                get: function () {
                    return this.$store.state.currentImage.title.split('.')[0]
                },
                set: function (newImageName) {
                    this.$store.state.currentImage.title = newImageName + '.' + this.$store.state.currentImage.title.split('.')[1]
                    if (newImageName.length > 0) {
                        this.setTimeoutUpdate(this.updateImageName, this.imageNameLastTime)
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
                    this.setTimeoutUpdate(this.updateImageCreateTime, this.imageCreateTimeLastTime, newValue)
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
            /*全选*/
            handleCheckAllChange(allChecked) {
                /*添加当前日期聚合的照片id到选中列表中*/
                if (allChecked) {
                    this.$store.state.currentImageList[this.currentIndex].checkedImages = []
                    this.$store.state.currentImageList[this.currentIndex].checkedImages = this.$store.state.currentImageList[this.currentIndex].images
                } else { /*移除当前选中的list*/
                    this.$store.state.currentImageList[this.currentIndex].checkedImages = []
                }
                console.log(this.checkedImages.length)
            },
            handleCheckedImagesChange(checkedArray) {
                /*当前时间图片都在已选中时 将当前选中checkAll 设置为true 否则为false*/
                let checkedCount = checkedArray.length;
                this.$store.state.currentImageList[this.currentIndex].checkedAll = checkedCount === this.$store.state.currentImageList[this.currentIndex].images.length;
                console.log(this.checkedImages.length)
            },
            /*清空回收站图片*/
            clearPictures() {
                this.$confirm('此操作将清空废纸篓是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    center: true
                }).then(() => {
                    this.https.clearAllWasteNotes().then(({data}) => {
                        console.log("清空废纸篓", data);
                    })
                    this.$store.state.currentNoteList = []
                    this.$store.state.wastepaperNotesList = []
                    this.$message({type: 'success', message: '成功!', duration: 1000,});
                }).catch(() => {
                    this.$message({
                        type: 'info',
                        message: '已取消',
                        duration: 1000,
                    });
                });
            },
            /*恢复当的图片*/
            recoverCurrentPictures() {
                /*1.修改 note push到 所有笔记和 todo 对应的笔记本中*/
                this.$store.state.currentNote.wastepaper = false
                this.$store.state.notes.unshift(this.$store.state.currentNote)

                /*4.后台 将 wastepaper设置为 false*/
                this.https.updateNote({
                    id: this.$store.state.currentNote.id,
                    pid: this.$store.state.currentNote.pid,
                    wastepaper: false
                }).then(({data}) => {
                    console.log("修改数据库成功", data);
                })

                /*2.将 当前笔记移出  */
                this.$store.state.currentNoteList.splice(this.$store.state.currentIndex, 1)
                /* 3.修改当前选中的笔记*/
                if (this.$store.state.currentNoteList.length > 0) {
                    this.$store.state.currentIndex = 0
                    this.$store.state.currentNote = this.$store.state.currentNoteList[0]
                }

                //  4.更新笔记数量的显示  延时加载
                setTimeout(() => {
                    this.https.getNoteBooksTree().then(({data}) => {
                        this.$store.state.noteBooksTree = data.data
                        this.tool.addNoteCount(this.$store.state.noteBooksTree)
                    })
                }, 1000)

            },
            recoverAllPictures() {
                /*1.修改所有已经移入回收站的图片 */
                /*4.后台 将 wastepaper设置为 false 重新请求数据*/
                this.https.recoverAllPictures().then(({data}) => {
                    console.log("恢复所有删除的图片 成功", data);
                    this.$store.state.fileList = data.data; // 进入的笔记本列表数据
                    this.$store.state.starImageList = this.$store.state.fileList.filter((i) => i.star == true)
                })

                /*2.清空当前回收站*/
                this.$store.state.wastepaperPictureList = []
                this.$store.state.currentImageList = []


                //  4.更新笔记数量的显示  延时加载
                /*  setTimeout(() => {
                      this.https.getNoteBooksTree().then(({data}) => {
                          this.$store.state.noteBooksTree = data.data
                          this.tool.addNoteCount(this.$store.state.noteBooksTree)
                      })
                  }, 1000)*/

            },
            setTimeoutUpdate(funcName, lastTimeType, ...param) {
                if (lastTimeType == 0) {
                    funcName(...param)
                } else {
                    clearTimeout(lastTimeType)
                    funcName(...param)
                }
            },
            updateImageName() {
                this.imageNameLastTime = setTimeout(() => {
                    this.https.updateImage({
                        id: this.$store.state.currentImage.id,
                        title: this.$store.state.currentImage.title
                    }).then(({data}) => {
                        console.log("修改图片名称成功", data);
                    })
                }, 2000)
            },
            updateImageCreateTime(createTime) {
                this.imageCreateTimeLastTime = setTimeout(() => {
                    this.https.updateImage({
                        id: this.$store.state.currentImage.id,
                        createTime: createTime
                    }).then(({data}) => {
                        console.log("修改图片创建时间成功", data);
                    })
                }, 5000)
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
                this.lastImage = img
                let currentImageUrlList = imageList.map(x => x.url);
                /*移动数组 将当前点击照片置于第一张*/
                this.$store.state.currentImageUrlList = [...currentImageUrlList.slice(index), ...currentImageUrlList.slice(0, index)]

                /*关闭图片预览时 不显示图片其他信息*/
                this.$nextTick(() => {
                    /*添加删除和收藏按钮*/
                    this.addDelIcon(index) //删除图标只需加一次
                    this.addLikeIcon()
                    // 获取遮罩层关闭按钮dom
                    let domImageMask = document.querySelector(".el-image-viewer__close");
                    if (!domImageMask) {
                        return;
                    }
                    domImageMask.addEventListener("click", () => {
                        this.imageInfo = false
                        this.removeIcons = false //解决初次点击时不出现红心
                    });
                    /*点击空白处关闭图片显示*/
                    let domImageMask4 = document.querySelector(".el-image-viewer__mask");
                    if (!domImageMask4) {
                        return;
                    }
                    domImageMask4.addEventListener("click", () => {
                        this.imageInfo = false
                        this.removeIcons = false //解决初次点击时不出现红心
                    });
                    /*上一张*/
                    let domImageMask3 = document.querySelector(".el-image-viewer__prev");
                    if (!domImageMask3) {
                        return;
                    }
                    domImageMask3.addEventListener("click", () => {
                        index -= 1
                        if (index == -1) index = imageList.length - 1
                        this.lastImage = this.$store.state.currentImage
                        this.$store.state.currentImage = imageList.slice(index, index + 1)[0]
                        this.addLikeIcon()
                    });

                    /*下一张*/
                    let domImageMask2 = document.querySelector(".el-image-viewer__next");  // 获取遮罩层关闭按钮dom
                    if (!domImageMask2) {
                        return;
                    }
                    domImageMask2.addEventListener("click", () => {
                        index += 1
                        if (index == imageList.length) index = 0
                        this.lastImage = this.$store.state.currentImage
                        this.$store.state.currentImage = imageList.slice(index, index + 1)[0]
                        this.addLikeIcon()
                    });

                })
            },
            addDelIcon(index) {
                let vm = this
                /*给预览大图添加删除和收藏按钮*/
                setTimeout(function () {/*防止dom没渲染处理，延迟一段时间再加图片。*/
                    // 因为下面是通过class获取的 所以点击一次后所有相对于class中就全部会有小图标
                    // 除了第一次点击增加小图标外，剩下的全部都是修改小图标的业务逻辑  如果点击的次数大于1  执行到这里就不往下执行了
                    let iconItem = document.querySelector('.el-image-viewer__actions__inner') // 这里获取的是图片外层的父元素   就是给这个元素追加标签
                    iconItem = $('.el-image-viewer__actions__inner')
                    let deleteIcon = `<i class="el-icon-delete fs-20 deleteImg"></i>` // fs-20 代表图标字体大小20px
                    iconItem.append(deleteIcon)
                    /*删除照片*/
                    $('.deleteImg').click(() => {
                        console.log('deleteImg')
                        vm.deleteImgClick(index)
                    })
                }, 100)

            },
            addLikeIcon() {
                let vm = this
                /*给预览大图添加收藏按钮*/
                setTimeout(function () {/*防止dom没渲染处理，延迟一段时间再加图片。*/
                    if (!vm.removeIcons || vm.lastImage.star != vm.$store.state.currentImage.star) {
                        vm.changeLikeIcon()
                    }
                    vm.removeIcons = true
                }, 100)

            },
            /*切换喜欢图标*/
            changeLikeIcon() {
                let iconItem = document.querySelector('.el-image-viewer__actions__inner') // 这里获取的是图片外层的父元素   就是给这个元素追加标签
                if (this.removeIcons) { // 第一次添加时不移除上一次的like图标
                    iconItem.removeChild(iconItem.lastChild)
                }
                iconItem = $('.el-image-viewer__actions__inner')
                let likeIcon = this.$store.state.currentImage.star ? `<i class="iconfont icon-like1 likeImg" style="color: red;font-size: 25px"></i>` : `<i class="iconfont icon-like likeImg" style="font-size: 22px"></i>`
                iconItem.append(likeIcon)
                $('.likeImg').click(() => {
                    this.starClick(this.$store.state.currentImage)
                    this.changeLikeIcon()
                })
            },
            deleteImgClick(index) {
                let msg = this.$store.state.currentImage.wastepaper ? '彻底删除' : '移入到回收站'
                this.$confirm('此操作将该照片' + msg + ', 是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    center: true
                }).then(() => {
                    this.deleteCurrentImage(index)
                    this.$message({type: 'success', message: '成功!', duration: 1000,});
                }).catch(() => {
                    this.$message({
                        type: 'info',
                        message: '已取消',
                        duration: 1000,
                    });
                });
            },
            deleteCurrentImage(index) {
                this.https.deleteImage({
                    id: this.$store.state.currentImage.id
                }).then(({data}) => {
                    console.log("移动图片到回收站", data);
                })
                this.lastImage = this.$store.state.currentImage
                /*移除 fileList,statImageList 中的该图片*/
                this.$store.state.fileList = this.$store.state.fileList.filter((i) => i.id != this.$store.state.currentImage.id)
                if (this.$store.state.currentImage.star) this.$store.state.starImageList = this.$store.state.starImageList.filter((i) => i.id != this.$store.state.currentImage.id)

                /*给回收站添加该照片 或彻底删除*/
                if (this.$store.state.currentImage.wastepaper) { /*彻底删除 从回收站移除*/
                    this.$store.state.wastepaperPictureList = this.$store.state.wastepaperPictureList.filter((i) => i.id != this.$store.state.currentImage.id)
                } else { //添加到回收站
                    this.$store.state.currentImage.wastepaper = true
                    this.$store.state.wastepaperPictureList.unshift(this.$store.state.currentImage)
                }


                let currentList = this.$store.state.currentImageList[this.currentIndex].images
                let res = currentList.filter((i) => i.id != this.$store.state.currentImage.id)
                //删除最后一张照片时清空该时间段照片
                if (res.length == 0) {
                    this.$store.state.currentImageList.splice(this.currentIndex, 1)
                } else {
                    this.$store.state.currentImageList[this.currentIndex].images = res
                }

                this.$store.state.currentImage = res[index - 1 < 0 ? 0 : index - 1]
                let currentImageUrlList = res.map(x => x.url);
                /*
                /!*不能直接置空 不然又出现不显示大图*!/
                */
                /*轮播图的变化移动数组*/
                this.$store.state.currentImageUrlList = [...currentImageUrlList.slice(index - 1), ...currentImageUrlList.slice(0, index - 1)]
                if (this.$store.state.currentImageUrlList.length == 0) {
                    this.imageInfo = false
                    this.removeIcons = false
                }
                /*todo 解决删除最后一张图片时的bug*/
                if (index == this.$store.state.currentImageUrlList.length) {
                    /*手动调用点击图片也无法解决*/
                    /*this.imageClick(this.$store.state.currentImage, res, index - 1)*/
                    /*只能手动关闭图片信息的显示*/
                    this.imageInfo = false
                    this.removeIcons = false
                }
            },
            /*批量移动图片到回收站 或彻底删除*/
            deleteImageBatch() {
                let ids = this.checkedImages.map(x => x.id)
                let checkedImages = this.checkedImages // 为异步操作保存当前值
                /*重新请求数据 避免繁琐的判断和调整*/
                this.https.deleteImageBatch(ids).then(({data}) => {
                    let addImageToRecyleBin = true
                    let dayImages = []
                    console.log(this.$store.state.currentNoteBook.id == 10 ? "彻底删除图片":"批量移动图片到回收站", data);


                    if (this.$store.state.currentNoteBook.id == 10) {  //回收站下的批量删除
                        this.$store.state.wastepaperPictureList = this.$store.state.wastepaperPictureList.filter((i) => ids.indexOf(i.id) == -1)
                        dayImages = this.$store.state.wastepaperPictureList
                        addImageToRecyleBin = false
                    }else {
                        this.$store.state.fileList = data.data; // 进入的笔记本列表数据
                        this.$store.state.starImageList = this.$store.state.fileList.filter((i) => i.star == true)
                    }

                    // 全部图片和收藏图片下的批量删除
                    /*重新初始化当前图片列表*/
                    if (this.$store.state.currentNoteBook.id == 8) {
                        dayImages = this.$store.state.fileList
                    } else if (this.$store.state.currentNoteBook.id == 9) {
                        dayImages = this.$store.state.starImageList
                    }

                    dayImages = this.tool.groupImages("day", dayImages)
                    this.$store.state.currentImageList = dayImages

                    /*2.将照片加入到回收站回收站*/
                    if (addImageToRecyleBin) {
                        checkedImages = checkedImages.map(x => {
                            x.wastepaper = true
                            return x
                        })
                        this.$store.state.wastepaperPictureList.unshift(...checkedImages)
                    }
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
                if (this.currentIndex == index) return "#E6E6E6" /*选中时的灰色*/
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
                        self.imageInfo = false
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
        left: 85px;
        top: 5px;
        z-index: 1000
    }

    /*控制图片多选的图标*/
    .imageIcon .imageCheck {
        font-size: 18px;
        position: absolute;
        left: 87px;
        bottom: 8px;
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

    .imageTitle {
        display: flex;
        justify-content: flex-start;
        position: absolute;
        z-index: 2001;
        left: 190px;
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