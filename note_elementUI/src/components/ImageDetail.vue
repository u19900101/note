<template>
    <div>


        <el-image :style="{width: imageScale,height: imageScale}"
                  style="margin-left:10px;"
                  @click="imageClick(img,images,indexInner)"
                  :src="imageScale == '500px' ? img.url :getThumbnails(img.url,img.title)"
                  fit="cover"
                  :preview-src-list="$store.state.currentImageUrlList"
                  @mouseover="upId(img.id)"
                  :alt="img.title">
        </el-image>
    </div>

</template>

<script>
    import imageTag from "./ImageTag";

    export default {
        name: "ImageDetail",
        components: {
            imageTag
        },
        props: ["imageScale", "img", "images", "indexInner", "currentIndex"],
        data() {
            return {
                removeIcons: false,// 动态移除 删除和收藏图标
                lastImage: {}, // 上一张图片 用于 动态加载喜欢标签时是否重新创建
                innerIndex: 0, //照片组内部序号
                innerImageList: [],
                deleteExceted: false, //是否执行了删除 用于大图预览退出时的更新
                showImageInfo : false,
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
            imageName: {
                get: function () {
                    return this.$store.state.currentImage.title.split('.')[0]
                },
                set: function (newImageName) {
                    this.$store.state.currentImage.title = newImageName + '.' + this.$store.state.currentImage.title.split('.')[1]
                    if (newImageName.length > 0) {
                        this.tool.setTimeoutUpdate(this.updateImageName, this.imageNameLastTime)
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
                    this.tool.setTimeoutUpdate(this.updateImageCreateTime, this.imageCreateTimeLastTime, newValue)
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
            upId(currentImageId) {
                this.$emit('getCurrentImageId', currentImageId)
            },
            imageClick(img, imageList, index) {
                this.showImageInfo = true
                this.$emit('getShowImageInfo', true)
                this.$store.state.currentImageUrl = img.url
                this.$store.state.currentImage = img
                this.lastImage = img

                /*移动数组 将当前点击照片置于第一张*/
                this.innerImageList = [...imageList.slice(index), ...imageList.slice(0, index)]
                this.$store.state.currentImage = this.innerImageList[0]
                this.$store.state.currentImageUrlList = this.innerImageList.map((x) => x.url)
                this.innerIndex = 0
                /*关闭图片预览时 不显示图片其他信息*/

                this.$nextTick(() => {
                    /*添加删除和收藏按钮*/
                    this.addDelIcon()  //存在bug，当删除最后一张时存在bug 只提供在外部进行删除
                    this.addLikeIcon()
                    this.addListener()
                })
            },
            onSwitch(index) {
                this.$store.state.currentImage = this.innerImageList[index]
                this.innerIndex = index
                console.log(index)
            },
            addListener() {
                // 获取遮罩层关闭按钮dom
                let domImageMask = document.querySelector(".el-image-viewer__close");
                if (!domImageMask) {
                    return;
                }
                domImageMask.addEventListener("click", () => {
                    console.log('退出 大图预览...')
                    //删除最后一张照片时清空该时间段照片
                    this.updateCurrentImageList()
                    this.$emit('getShowImageInfo', false)
                    this.removeIcons = false //解决初次点击时不出现红心
                });
                /*点击空白处关闭图片显示*/
                let domImageMask4 = document.querySelector(".el-image-viewer__mask");
                if (!domImageMask4) {
                    return;
                }
                domImageMask4.addEventListener("click", () => {
                    /*在退出大图预览时再更新数据 避免更新了url*/
                    console.log('点击空白处关闭图片显示 ...')
                    this.updateCurrentImageList()
                    this.$emit('getShowImageInfo', false)
                    this.removeIcons = false //解决初次点击时不出现红心
                });
                /*上一张*/
                let domImageMask3 = document.querySelector(".el-image-viewer__prev");
                if (!domImageMask3) {
                    return;
                }
                domImageMask3.addEventListener("click", () => {
                    this.toBeforeImage()
                });

                /*下一张*/
                let domImageMask2 = document.querySelector(".el-image-viewer__next");  // 获取遮罩层关闭按钮dom
                if (!domImageMask2) {
                    return;
                }
                domImageMask2.addEventListener("click", () => {
                    this.toNextImage()
                });

                /*监听键盘左右键按下*/
                document.onkeydown =  (e) => {
                    //事件对象兼容
                    let e1 = e || event || window.event || arguments.callee.caller.arguments[0]
                    //键盘按键判断:左箭头-37;上箭头-38；右箭头-39;下箭头-40
                    if (e1 && e1.keyCode == 37) {
                        // 按下左箭头
                        this.toBeforeImage()
                    } else if (e1 && e1.keyCode == 39) {
                        // 按下右箭头
                        this.toNextImage()
                    }
                }
            },
            addDelIcon() {
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
                        vm.deleteImgClick()
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
            deleteImgClick() {
                this.deleteCurrentImage()
                // this.d()
                /*let msg = this.$store.state.currentImage.wastepaper ? '彻底删除' : '移入到回收站'
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
                });*/
            },
            deleteCurrentImage() {
                this.deleteExceted = true
                let imageId = this.$store.state.currentImage.id
                this.deleteFromServe(imageId)
                // this.lastImage = this.$store.state.currentImage
                /*移除 fileList,statImageList 中的该图片*/
                this.updateOtherAssoc(imageId)

                this.innerImageList.splice(this.innerIndex, 1)

                this.$store.state.currentImage = this.innerImageList[this.innerIndex]
                /*todo 解决删除最后一张图片时的bug  或者是进入后直接删除时的*/
                this.$store.state.currentImageUrlList.splice(this.innerIndex, 1)
                if (this.innerIndex == this.$store.state.currentImageUrlList.length) {
                    /*手动调用点击图片也无法解决*/
                    /*只能手动关闭图片信息的显示*/
                    console.log('last image. ..')
                    this.$emit('getShowImageInfo', false)
                    this.removeIcons = false
                    /*强行关闭大图*/
                    document.querySelector(".el-image-viewer__close").click();
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
            /*封装缩略图的链接*/
            getThumbnails(url, title) {
                let kk = url.replace(title, title.split(".")[0] + "_thumbnails." + title.split(".")[1])
                return kk
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
            /*获取带有中文字符的长度  一个中文的宽度对应两个英文的宽度*/
            getBt(str) {
                let char = str.replace(/[^\x00-\xff]/g, '**');
                return char.length * 6 + 40;
            },
            getImgageSize(byteNum) {
                if (byteNum < 1024 * 1024) {
                    let kb = (byteNum / 1024).toString()
                    return kb.substring(0, kb.indexOf(".") + 2) + " Kb"
                }

                if (byteNum >= 1024 * 1024) {
                    let kb = (byteNum / 1024 / 1024).toString()
                    return kb.substring(0, kb.indexOf(".") + 2) + " Mb"
                }
            },
            /*控制列表颜色*/
            getBgColor(index) {
                /* 若当前 index 被选中 则直接返回选中颜色 进入就返回 hover颜色 其他情况就都返回白色(背景遮挡色)*/
                if (this.currentIndex == index) return "#E6E6E6" /*选中时的灰色*/
                if (this.enterIndex == index) return "#EDF6FD" /*天蓝色*/
                return "#ffffff"
            },
            deleteFromServe(id) {
                let deleteImageId = require('lodash').cloneDeep(id);
                this.https.deleteImage({
                    id: deleteImageId
                }).then(({data}) => {
                    console.log("移动图片到回收站", data);
                })
            },
            updateOtherAssoc(imageId) {
                this.$store.state.fileList = this.$store.state.fileList.filter((i) => i.id != imageId)
                if (this.$store.state.currentImage.star) this.$store.state.starImageList = this.$store.state.starImageList.filter((i) => i.id != imageId)


                /*给回收站添加该照片 或彻底删除*/
                if (this.$store.state.currentImage.wastepaper) { /*彻底删除 从回收站移除*/
                    this.$store.state.wastepaperPictureList = this.$store.state.wastepaperPictureList.filter((i) => i.id != imageId)
                } else { //添加到回收站
                    this.$store.state.currentImage.wastepaper = true
                    this.$store.state.wastepaperPictureList.unshift(this.$store.state.currentImage)
                }
            },
            updateCurrentImageList() {
                if (this.deleteExceted) {
                    console.log('updateCurrentImageList')
                    if (this.innerImageList.length == 0) {
                        this.$store.state.currentImageList.splice(this.currentIndex, 1)
                    } else {
                        this.$store.state.currentImageList[this.currentIndex].images = this.innerImageList
                    }
                }
            },
            toBeforeImage() {
                this.innerIndex -= 1
                if (this.innerIndex == -1) this.innerIndex = this.innerImageList.length - 1
                this.lastImage = this.$store.state.currentImage
                this.$store.state.currentImage = this.innerImageList.slice(this.innerIndex, this.innerIndex + 1)[0]
                console.log('上一张', this.innerIndex, this.$store.state.currentImage)
                this.addLikeIcon()
            },
            toNextImage() {
                console.log('下一张')
                this.innerIndex += 1
                if (this.innerIndex == this.innerImageList.length) this.innerIndex = 0
                this.lastImage = this.$store.state.currentImage
                this.$store.state.currentImage = this.innerImageList.slice(this.innerIndex, this.innerIndex + 1)[0]
                this.addLikeIcon()
            }
        },
        mounted() {
            /*esc键关闭图片信息显示*/
            //监听键盘按键事件
            let self = this;
            this.$nextTick(function () {
                document.addEventListener('keyup', function (e) {
                    //此处填写你的业务逻辑即可
                    if (e.keyCode == 27) {
                        self.$emit('getShowImageInfo', false)
                    }
                })
            })
        },
        created() {
            this.$bus.$on('starClick', this.starClick)
        }
    }
</script>

<style scoped>

</style>