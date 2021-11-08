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
        <el-container v-else>
            <!--导航栏-->
            <el-aside class="widthSyncChild">
                <noteNav/>
            </el-aside>

            <!--笔记列表-->
            <el-aside class="widthSyncChild">
                <noteList/>
            </el-aside>


            <!--笔记内容-->
            <note v-if="$store.state.currentNoteList.length > 0 "></note>
            <div v-else style="text-align: center;width: 100%;">
                <h1>空空如也 </h1>
            </div>
        </el-container>


    </el-container>
</template>
<script>
    import note from "./Note";
    import borderLine from "./BorderLine";
    import noteList from "./NoteList";
    import noteNav from "./Nav";

    export default {
        name: 'home',
        components: {
            borderLine,
            noteList,
            noteNav,
            note
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
                    this.addNoteCount(data.data[1])
                    this.$store.state.noteBooksTree = data.data[1]
                    // console.log('kkkk',data.data[1])
                    this.https.getWastepaperNotes().then(({data}) => {
                        this.$store.state.wastepaperNotesList = data.data
                    })
                }).then(() => {
                    // 2.获取笔记数据 初始化 notes
                    this.https.getNotes().then(({data}) => {
                        this.$store.state.notes = data.data
                    }).then(() => {
                        // 2.1 将state数据写到当前 currentNoteList, currentNote currentNoteBookNoteList(当前笔记本中所有的笔记)
                        this.$store.state.currentNoteList = this.$store.state.notes; // 进入的笔记本列表数据
                        this.$store.state.currentNoteBookNoteList = this.$store.state.notes;

                        this.$store.state.starNoteList = this.$store.state.notes.filter((note) => note.star == true)

                        // 3.获取标签数据
                        this.https.getTags().then(({data}) => {
                            this.$store.state.tags = data.data;
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
                                this.tool.getDateTimes(this.$store.state.currentNoteList,this.$store.state.sortWay);
                                console.log("所有数据请求完成");
                                this.loadingState = false   //关闭loading动画
                            }).catch((err) => {
                                alert('网络延迟,请刷新重试')
                                console.log(err);
                            })

                        })
                    })
                })
            },
            addNoteCount(treeData) {
                treeData.forEach((n) =>{
                    n.title += ' (' + n.noteCount + ')'
                    if(n.children.length > 0 ) this.addNoteCount(n.children)
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
</style>