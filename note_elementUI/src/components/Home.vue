<template>
    <el-container>
        <!--数据加载-->
        <div v-if="loadingState" class="loadingImage"> <!--让图片居中显示-->
            <!--手动设置图片高度 让其占满整个屏幕  设置百分比无效-->
            <!--fill 保留全部  cover 剪裁-->
            <el-image v-if="loadingState"
                      :src="require('../assets/images/gofree.jpg')"
                      style="height: 776px"
                      fit="cover">
            </el-image>
        </div>

        <!--数据加载成功后 页面展示-->
        <el-container v-else id="home">
            <!--导航栏-->
            <el-aside class="widthSyncChild" id = "nav">
                <noteNav/>
            </el-aside>

            <!--笔记列表 和 笔记内容-->
            <keep-alive>
                <router-view name="notepage"></router-view>
            </keep-alive>
            <!--笔记和标签页面展示-->
            <el-main style="padding: 0;width: 1146px;" id = "imageList">
                <keep-alive>
                    <router-view name="imageList"></router-view>
                    <router-view name="noteBook_tag"></router-view>
                </keep-alive>
                <!-- <imageList v-if="$store.state.fileMode"></imageList>-->
                <!--笔记本和标签页面-->
                <!--  <noteBook_tag v-else></noteBook_tag>-->
            </el-main>
        </el-container>
    </el-container>
</template>
<script>

    import borderLine from "./BorderLine";

    import noteNav from "./Nav";
    import noteBook_tag from "./NoteBook_Tag";
    import imageList from "./ImageList";
    import notepage from "./Notepage";
    import {getImageTags} from "../server";

    export default {
        name: 'home',
        components: {
            borderLine,
            imageList,
            noteNav,
            noteBook_tag,
            notepage
        },
        data() {
            return {
                loadingState: true,  //数据是否正在加载
            }
        },
        methods: {
            getData() {
                // 1.获取笔记本数据 初始化笔记本
                this.https.getNotebooks().then(({data}) => {
                    this.$store.state.noteBooks = data.data[0]
                    /*默认初始化选择所有笔记*/
                    this.$store.state.currentNoteBook = data.data[0][0]
                    this.$store.state.listTitle = data.data[0][0].title
                    /* 将noteBookTree 封装上笔记的数量*/
                    this.$store.state.noteBooksTreePure = JSON.parse(JSON.stringify(data.data[1]))
                    this.tool.addNoteCount(data.data[1], 'notebook')
                    this.$store.state.noteBooksTree = data.data[1]

                    // console.log('kkkk',data.data[1])
                    this.https.getWastepaperNotes().then(({data}) => {
                        this.$store.state.wastepaperNotesList = data.data
                    })

                    this.https.getSearchHistroy().then(({data}) => {
                        let keywords = []
                        data.data.forEach((k) => {
                            keywords.push({value: k.keyword})
                        })
                        this.$store.state.searchHistroy = keywords
                    })

                    /*图片数据*/
                    this.https.getFiles().then(({data}) => {
                        this.$store.state.fileList = data.data; // 进入的笔记本列表数据
                        this.$store.state.starImageList = this.$store.state.fileList.filter((i) => i.star == true)
                    })

                    this.https.getWastepaperPictureList().then(({data}) => {
                        this.$store.state.wastepaperPictureList = data.data
                    })

                    this.https.getImageTags().then(({data}) => {
                        this.$store.state.imageTagsTree = data.data[0]
                        this.$store.state.imageTags = data.data[1]
                        /* 将标签数据 封装上笔记的数量*/
                        this.$store.state.imageTreePure = JSON.parse(JSON.stringify(data.data[0]));
                        this.tool.addNoteCount(this.$store.state.imageTagsTree, 'imageTag')
                    })

                    /*人脸数据*/
                    this.https.getPersons().then(({data}) => {
                        this.$store.state.persons = data.data
                    })

                }).then(() => {
                    // 2.获取笔记数据 初始化 notes
                    this.https.getNotes().then(({data}) => {
                        this.$store.state.notes = data.data
                    }).then(() => {
                        // 2.1 将state数据写到当前 currentNoteList, currentNote currentNoteBookNoteList(当前笔记本中所有的笔记)
                        this.$store.state.currentNoteList = this.$store.state.notes; // 进入的笔记本列表数据
                        this.$store.state.currentNoteBookNoteList = this.$store.state.notes;

                        this.$store.state.starNotesList = this.$store.state.notes.filter((note) => note.star == true)

                        // 3.获取标签数据
                        this.https.getTags().then(({data}) => {
                            /* 将标签数据 封装上笔记的数量*/
                            this.$store.state.tagsTree = data.data[0];
                            this.$store.state.tagsTreePure = JSON.parse(JSON.stringify(data.data[0]));
                            this.$store.state.tags = data.data[1];
                            this.tool.addNoteCount(this.$store.state.tagsTree, 'noteTag')
                        }).then(() => {
                            // 创建页面时初始化
                            // 1.先初始化 列表  在列表排序中初始化 noteId
                            // this.$router.push({name: 'noteList'})
                            // 2.初始化 笔记内容为 排序的第一个
                            if (this.$store.state.currentNoteList.length > 0) {
                                this.$store.state.currentNote = this.$store.state.notes[0]; // 进入的笔记本列表数据
                                // this.$router.push({name: 'note1'})
                            }
                            this.https.getSortWay().then(({data}) => {
                                this.$store.state.sortWay = data.data[0];
                                // 给笔记添加时间别名//
                                this.tool.getDateTimes(this.$store.state.currentNoteList, this.$store.state.sortWay);
                                console.log("所有数据请求完成");
                                this.loadingState = false   //关闭loading动画
                            }).catch((err) => {
                                alert('网络延迟,请刷新重试')
                                console.log(err);
                            })

                        })
                    })
                })
                /*大图预览bug*/
                this.$store.state.currentImageUrlList = [1, 2, 3]
            },
            toMap(lnglat, title, createTime) {
                this.$router.push({name: 'map'})
                /*定位时间轴 之后会自动在地图上联动定位*/
                this.$bus.$emit('setDateIndex', title, createTime)
            },
            imageToMap() {
                /*封装当天图片*/
                let indexImages = this.$store.state.currentImageList[this.$store.state.currentIndex].images
                /*当跳转之前的视图正好是日视图时 直接进行赋值 若不是就进行封装*/
                if (indexImages.length > 1 &&
                    (indexImages[0].createTime.substring(0, 10) != indexImages[indexImages.length - 1].createTime.substring(0, 10))) {
                    indexImages = this.tool.groupImages("day", indexImages)
                    indexImages = indexImages.filter(i => i.createTime.replace("年", "-").replace("月", "-").replace("日", "") == this.$store.state.currentImage.createTime.substring(0, 10))[0].images
                }
                this.$store.state.dayImages = indexImages
                if (this.$store.state.currentImage.lnglat) {
                    let {lnglat, title, createTime} = this.$store.state.currentImage
                    /*在地图上定位*/
                    this.$bus.$emit("toMap", lnglat, title, createTime)
                }else {
                //    找到第一张包含gps坐标的当天图片
                    for (let image of indexImages) {
                        if(image.lnglat){
                            let {lnglat, title, createTime} = image
                            this.$bus.$emit("toMap", lnglat, title, createTime)
                            break
                        }
                    }
                }

                /*关闭大图预览*/
                let domImageMask = document.querySelector(".el-image-viewer__close");
                if (domImageMask) {
                    domImageMask.click()
                }
            },
        },
        // 组件创建时 请求数据
        created() {
            this.getData()
            /*注册全局事件*/
            this.$bus.$on("toMap", this.toMap)
            this.$bus.$on("imageToMap", this.imageToMap)
        },
        mounted() {
            this.$router.push({name: 'notepage'})
            // this.$router.push({name: 'calendar'})
            // this.$router.push({name: 'map'})
            // this.$router.push({name: 'person'})
            // this.$router.push({name: 'imageList'})
        }
    }
</script>

<style>

    .loadingImage {
        /*   background: green;*/
        width: 100%; /*设置高宽为100% 不然总是被内容撑开*/
        height: 100%;
        display: flex;
        justify-content: center; /*主轴上居中*/
        align-items: center; /*侧轴上居中*/
    }

    /* 设置 aside 与子元素同宽度  不然总是为默认的 300px*/
    .widthSyncChild {
        width: auto !important
    }


    .el-container {
        display: flex;
        flex-direction: row;
        flex: 0;
        flex-basis: auto;
        box-sizing: border-box;
        min-width: 0;
    }

    /*设置table表格的位置*/
    .el-main {
        /*padding: 40px 20px 0px 20px !important;*/
    }

    /*nav 的背景色*/
    #home .el-aside {
        background-color: #2a333c;
    }
</style>