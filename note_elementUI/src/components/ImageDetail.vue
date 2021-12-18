<template>
    <div>
        <!--当前大图详细信息的显示-->
       <!-- <div>
            &lt;!&ndash;拍摄日期、图片名称、位置&ndash;&gt;
            <div  class="imgInfo">
                &lt;!&ndash;拍摄日期 &ndash;&gt;
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
                &lt;!&ndash;图片名称&ndash;&gt;
                <el-input clearable v-model="imageName" style="width: 182px" placeholder="请输入名称">
                    <template slot="append">.{{$store.state.currentImage.title.split(".")[1]}}</template>
                </el-input>
                &lt;!&ndash;位置&ndash;&gt;
                <div class="info" :style="{width: getBt($store.state.currentImage.location) + 'px'}"
                     v-if="$store.state.currentImage.location">
                    <i class="el-icon-location"></i>
                    <a :href="'http://maps.google.com/maps?z=6&q=' + $store.state.currentImage.lnglat"
                       style="font-size: mini;color:#49a2de">{{$store.state.currentImage.location}}</a>
                </div>

            </div>
            &lt;!&ndash;大小、宽高&ndash;&gt;
            <div class="imgInfoRightBottom">
                <strong>{{getImgageSize($store.state.currentImage.size)}}</strong> {{$store.state.currentImage.widthH}}
            </div>
            &lt;!&ndash;标签&ndash;&gt;
            <imageTag class="imgTag" ref="noteTag"></imageTag>
        </div>-->

        <!--     <imageDetail
                                                 :imageScale="imageScale"
                                                 :img="img"
                                                 :images="image.images"
                                                 :indexInner="indexInner"
                                                 :currentIndex = "currentIndex"
                                         ></imageDetail>-->
        <el-image :style="{width: imageScale,height: imageScale}"
                  style="margin-left:10px;"
                  @click="imageClick(img,images,indexInner)"
                  :src="imageScale == '500px' ? img.url : tool.getThumbnails(img.url,img.title)"
                  fit="cover"
                  @mouseover="$store.state.currentImageId = img.id"
                  :preview-src-list="$store.state.currentImageUrlList"
                  :alt="img.title"/>
    </div>

</template>

<script>
    export default {
        name: "ImageDetail",
        props:["imageScale","img","images","indexInner","currentIndex"],
        data(){
            return{
                imageInfo: false, //图片预览
                currentImageId: "", //当前图片id
                lastImage: {}, // 上一张图片 用于 动态加载喜欢标签时是否重新创建
            }
        },
        methods:{

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
            deleteImgClick(index) {
                this.deleteCurrentImage(index)
                /*let msg = this.$store.state.currentImage.wastepaper ? '彻底删除' : '移入到回收站'
                this.$confirm('此操作将该照片' + msg + ', 是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    center: true
                }).then(() => {
                    this.deleteCurrentImage(index)
                    this.$message({type: 'success', message: '成功!', duration: 1000,});
                }).catch((e) => {
                    console.log('error ',e)
                    this.$message({
                        type: 'error',
                        message: '删除失败',
                        duration: 1000,
                    });
                });*/
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
                this.$message({type: 'success', message: '成功!', duration: 1000,});
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

        },
        mounted() {

        },
        created() {
            this.$bus.$on('starClick',this.starClick)
        }
    }
</script>

<style scoped>

</style>