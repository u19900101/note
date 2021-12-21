<template>
    <div>
        <!-- 拍摄日期、图片名称、位置-->
        <div class="imgInfo">
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
            <!--位置-->
            <div class="more-line" style="background-color: #FFFFFF"
                 :style="{width: getBt($store.state.currentImage.location) + 'px'}"
                 @click="toMap"
                 v-if="$store.state.currentImage.location">
                <i class="el-icon-location"></i>
                <a href="#/map" @click="toMap" style="font-size: mini;color:#49a2de">
                    <span>
                        {{$store.state.currentImage.location}}
                    </span>
                </a>

            </div>

        </div>
        <!--大小、宽高-->
        <div class="imgInfoRightBottom">
            <strong>{{getImgageSize($store.state.currentImage.size)}}</strong> {{$store.state.currentImage.widthH}}
        </div>
        <!--标签-->
        <imageTag class="imgTag" ref="noteTag"></imageTag>
    </div>
</template>

<script>
    import imageTag from "./ImageTag";

    let dayjs = require('dayjs');
    export default {
        name: "ImageInfo",
        components: {imageTag,},
        data() {
            return {
                imageCreateTimeLastTime: 0, //修改照片名称的定时器
                imageNameLastTime: 0, //修改照片名称的定时器
            }
        },
        computed: {
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
        },
        methods: {
            toMap() {
                let {lnglat, title, createTime} = this.$store.state.currentImage
                /*在地图上定位*/
                this.$bus.$emit("toMap", lnglat, title, createTime)
                /*展示当天图片*/
                this.$store.state.dayImages = this.$store.state.fileList.filter((i) =>
                    i.createTime.split(" ")[0].substring(0,10) == createTime.split(" ")[0].substring(0,10))

                /*关闭大图预览*/
                let domImageMask = document.querySelector(".el-image-viewer__close");
                if(domImageMask){
                    domImageMask.click()
                }
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
            /*获取带有中文字符的长度  一个中文的宽度对应两个英文的宽度*/
            getBt(str) {
                let char = str.replace(/[^\x00-\xff]/g, '**');
                return char.length * 6 + 40;
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
        }
    }
</script>

<style scoped>
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

    .imgTag {
        display: flex;
        position: absolute;
        z-index: 2001;
        left: 21px;
        top: 5px;
    }

    /*图片宽高和大小信息*/
    .imgInfoRightBottom {
        display: flex;
        flex-direction: column;
        justify-content: flex-end;
        position: absolute;
        z-index: 2001;
        right: 10px;
        bottom: 40px;
        font-size: 10px;
    }

    .imageTitle {
        display: flex;
        justify-content: flex-start;
        position: absolute;
        z-index: 2001;
        left: 190px;
        top: 40px;
    }

    .imgInfo .more-line {
        height: 40px;
        line-height: 40px;
        background-color: #ffffff;
        border: 1px solid #D7DADC;
        border-radius: 5px;
        text-align: center;
        display: -webkit-box !important;
        overflow: hidden;
        text-overflow: ellipsis;
        word-break: break-all;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 1; /*行数的设置*/
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
</style>