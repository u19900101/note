<template>
    <el-container style="height: 100%;">
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
        <el-container v-else id = "home">
            <!--导航栏-->
            <el-aside class="widthSyncChild">
                <noteNav/>
            </el-aside>

            <el-container v-if="$store.state.listAndNoteShow">
                <!--笔记列表-->
                <el-aside class="widthSyncChild">
                    <noteList/>
                </el-aside>
                <!--笔记内容-->
                <note v-if="$store.state.fileMode || $store.state.currentNoteList.length > 0"></note>
                <div v-else style="text-align: center;width: 100%;">
                    <h1>空空如也 </h1>
                </div>
            </el-container>
            <!--笔记和标签页面展示-->
            <el-main v-else>
                <imageList v-if = "$store.state.fileMode"></imageList>
                <noteBook_tag v-else></noteBook_tag>
            </el-main>
        </el-container>
    </el-container>
</template>
<script>
    import note from "./Note";
    import borderLine from "./BorderLine";
    import noteList from "./NoteList";
    import noteNav from "./Nav";
    import noteBook_tag from "./NoteBook_Tag";
    import imageList from "./ImageList";

    export default {
        name: 'home',
        components: {
            borderLine,
            noteList,imageList,
            noteNav,
            note,
            noteBook_tag,
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
                    /* 将noteBookTree 封装上笔记的数量*/
                    this.$store.state.noteBooksTreePure = JSON.parse(JSON.stringify(data.data[1]))
                    this.tool.addNoteCount(data.data[1])
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
                        let images = data.data
                        this.$store.state.fileList = data.data; // 进入的笔记本列表数据
                        // this.$store.state.currentNoteBookNoteList = this.$store.state.notes;
                        //
                        // this.$store.state.starNotesList = this.$store.state.notes.filter((note) => note.star == true)

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
                            this.tool.addNoteCount(this.$store.state.tagsTree)
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
            }
        },
        // 组件创建时 请求数据
        created() {
            this.getData()
        },

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
        padding: 40px 20px 0px 20px !important;
    }

    /*nav 的背景色*/
    #home .el-aside {
        background-color: #2a333c;
    }
</style>