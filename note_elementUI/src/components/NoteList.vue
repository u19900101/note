<template>
    <el-container id="noteList">
        <el-aside :style="{width: noteListWidth + 'px'}">
            <el-scrollbar class="page-scroll">
                <!--笔记本名称 && 笔记排序按钮 搜索-->
                <div v-if="!$store.state.fileMode">
                    <el-row>
                        <!--笔记本名称-->
                        <el-col class="listTitle" style="text-align: center">
                            <span>{{$store.state.currentNoteBook.title}}
                            <span style="color: rgba(40,59,55,0.77)">(共{{$store.state.currentNoteList.length}}条)</span>
                            </span>
                            <el-button @click="clearAllWasteNotes" style="padding: 6px 7px;" size="mini" type="danger"
                                       round
                                       v-if="$store.state.currentNoteBook.title == '废纸篓'">
                                清空
                            </el-button>
                            <el-button @click="recoverAllNote" style="padding: 6px 7px;" size="mini" type="primary"
                                       round
                                       v-if="$store.state.currentNoteBook.title == '废纸篓'">
                                恢复
                            </el-button>
                            <el-button round style="padding: 6px 7px;" size="mini"
                                       @click="sortClick(-1)"
                                       @mouseleave.native="iconMouseLeave = true"
                                       @mouseenter.native="iconMouseLeave = false">
                                <i class="el-icon-sort"></i>
                            </el-button>
                        </el-col>
                    </el-row>

                    <!--笔记排序 级联面板-->
                    <cascader :isSortShow="isSortShow"
                              @sortClick="sortClick"
                              @mouseleave.native="sortPanelMouseLeave = true"
                              @mouseenter.native="sortPanelMouseLeave = false"
                              :style="{'margin-left': (noteListWidth-30) + 'px'}">
                    </cascader>

                    <!--搜索入口-->
                    <el-row class="search">
                        <!--带历史记录的输入框-->
                        <el-autocomplete
                                v-model="searchValue"
                                clearable
                                :fetch-suggestions="querySearch"
                                placeholder="搜索"
                                @select="handleSelect"
                                prefix-icon="el-icon-search"
                                style="width: 100%;"></el-autocomplete>
                    </el-row>
                    <!--笔记列表-->
                    <el-container>
                        <el-aside :style="{width: noteListWidth + 'px'}">
                            <div v-for="(note,index) in $store.state.currentNoteList">
                                <!-- type="flex" 为了让图片居中 -->
                                <!--列表区  标题  标签  内容-->
                                <el-row @click.native="noteClick(note,index)"
                                        @mousedown.native="$store.state.currentIndex = index"
                                        @mouseenter.native="enterIndex = index"
                                        :id="index"
                                        :style="{  backgroundColor:getBgColor(index),
                                             border:$store.state.currentIndex === index ? '1px solid #C3E5F5': '1px solid #D7DADC'
                                    }"
                                        style="padding-left: 5px;border: 1px solid #D7DADC;border-radius: 5px;"
                                        type="flex">

                                    <el-col :span="16">
                                        <!--标题-->
                                        <el-row>
                                            <div v-if="$store.state.isSearchMode" v-html="note.title"></div>
                                            <div v-else class='titleInList'>
                                                {{note.title}}
                                            </div>
                                        </el-row>
                                        <!--标签 & 内容-->
                                        <el-row>
                                            <!-- 给多行省略符 元素动态设置背景色-->
                                            <div class="more-line">
                                                <span style="color: #49a2de">{{getTagList(note)}}</span>
                                                <span v-if="$store.state.isSearchMode" v-html="note.content"></span>
                                                <span v-else> {{note.content.replace("# " + note.title + "\n\n","")}}</span>
                                            </div>
                                        </el-row>

                                        <!--时间-->
                                        <el-row>
                                            <!--根据排序方式来决定显示的时间类型 note.createTime -->
                                            <span style="font-size: mini;color: #49a2de">
                                            {{$store.state.sortWay.updateTime ? '更新时间' : '创建时间'}}
                                                <!--若有别名字段就显示别名-->
                                            {{
                                            $store.state.sortWay.updateTime
                                            ? (note.updateTimeAlias ? note.updateTimeAlias:note.updateTime)
                                            : (note.createTimeAlias ? note.createTimeAlias:note.createTime)
                                            }}
                                        </span>
                                        </el-row>
                                    </el-col>
                                    <!--图片-->
                                    <el-col :span="8" class="innerCenter">
                                        <el-image v-if="note.mediaUid"
                                                  :src="require('../assets/images/'+ note.mediaUid)"
                                                  fit="cover">
                                        </el-image>
                                        <el-image v-else
                                                  :src="require('../assets/images/gofree.jpg')"
                                                  fit="cover">
                                        </el-image>
                                    </el-col>
                                </el-row>
                            </div>
                        </el-aside>
                    </el-container>
                </div>
            </el-scrollbar>
        </el-aside>
        <!--拉动线-->
        <el-aside width="4px">
            <borderLine @widthChange="noteListWidthChange"/>
        </el-aside>
    </el-container>
</template>

<script>
    import borderLine from "./BorderLine"
    import noteSort from "./NoteSort"
    import cascader from "./Cascader"

    export default {
        name: "noteList",
        components: {
            borderLine,
            noteSort,
            cascader
        },
        data() {
            return {
                enterIndex: 0,
                noteListWidth: this.$store.state.sortWay.listWidth, // 笔记列表的初始宽度
                navMax: 800,
                navMin: 150,
                searchValue: '',
                isSortShow: false,
                iconMouseLeave: false,  // 鼠标是否离开了图标区域
                sortPanelMouseLeave: true,// 鼠标是否离开了排序面板区域
                lastTime: 0, //定时器的初始值
                widthLastTime: 0,
            }
        },

        methods: {
            /*清空废纸篓*/
            clearAllWasteNotes() {

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

            /*恢复所有删除的笔记*/
            recoverAllNote() {
                /*1.修改 note push到 所有笔记和 todo 对应的笔记本中*/
                this.$store.state.currentNote.wastepaper = false
                this.$store.state.notes.unshift(...this.$store.state.currentNoteList)

                /*4.后台 将 wastepaper设置为 false*/
                this.https.recoverAllNotes().then(({data}) => {
                    console.log("恢复所有删除的笔记 成功", data);
                })

                /*2.将 当前笔记移出  */
                this.$store.state.currentNoteList = []
                this.$store.state.wastepaperNotesList = []


                //  4.更新笔记数量的显示  延时加载
                setTimeout(() => {
                    this.https.getNoteBooksTree().then(({data}) => {
                        this.$store.state.noteBooksTree = data.data
                        this.tool.addNoteCount(this.$store.state.noteBooksTree)
                    })
                }, 1000)

            },
            /*控制列表颜色*/
            getBgColor(index) {
                /* 若当前 index 被选中 则直接返回选中颜色 进入就返回 hover颜色 其他情况就都返回白色(背景遮挡色)*/
                if (this.$store.state.currentIndex == index) return "#E6E6E6"
                if (this.enterIndex == index) return "#EDF6FD"
                return "#ffffff"
            },
            getTagList(note) {
                let dynamicTags = []
                note.tagList.forEach((tag) => dynamicTags.push(tag.title))
                return dynamicTags.toString()
            },
            noteClick(note, index) {
                /*页面显示*/
                if (this.$store.state.isSearchMode) {
                    if (this.$store.state.isTitleEditMode) this.$store.state.isTitleEditMode = false
                    if (this.$store.state.isContentEditMode) this.$store.state.isContentEditMode = false
                }
                this.$store.state.currentNote = note
                this.$store.state.currentIndex = index
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

            handleChange(value) {
                console.log(value);
            },
            noteListWidthChange(movement) {
                this.noteListWidth -= movement
                if (this.noteListWidth < this.navMin || this.noteListWidth > this.navMax) {
                    this.noteListWidth = this.noteListWidth > this.navMin ? this.navMax : this.navMin
                }
                if (this.widthLastTime == 0) {
                    this.exeWidthChange(this.noteListWidth)
                } else {
                    clearTimeout(this.widthLastTime)
                    this.exeWidthChange(this.noteListWidth)
                }
            },
            exeWidthChange(listWidth) {
                this.widthLastTime = setTimeout(() => {
                    this.https.updateSortWay({id: 1, listWidth: listWidth}).then(({data}) => {
                        console.log(data)
                    })
                }, 2000)
            },
            switchToSearchMode() {
                this.$store.state.isSearchMode = !this.$store.state.isSearchMode
                this.$store.state.isTitleEditMode = !this.$store.state.isTitleEditMode
                this.$store.state.isContentEditMode = !this.$store.state.isContentEditMode
            },

            /*标签的搜索*/
            querySearch(queryString, cb) {
                let searchHistroy = this.$store.state.searchHistroy;
                let results = queryString ? searchHistroy.filter(this.createFilter(queryString)) : searchHistroy;
                // 调用 callback 返回建议列表的数据
                cb(results);
            },
            /*创建忽略大小写的过滤器*/
            createFilter(queryString) {
                return (h) => {
                    return (h.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0);
                };
            },
            handleSelect(item) {
                this.searchValue = item.value
            },
            search(searchValue) {
                //  1.根据内容进行全局搜索,并高亮返回  2.点击列表可进行预览 3.点击笔记 进行修改
                console.log("搜索字段 ", searchValue);
                // 切换到搜索模式
                if (!this.$store.state.isSearchMode) this.switchToSearchMode()
                let queryData = {
                    "query": {
                        "bool": {
                            "must": [ //排除逻辑删除的笔记
                                {
                                    "match": {
                                        "status": false
                                    }
                                },
                                {
                                    "match": {
                                        "wastepaper": false
                                    }
                                },
                            ],
                            "should": [
                                {
                                    "match": {
                                        "title": searchValue
                                    }
                                },
                                {
                                    "match": {
                                        "content": searchValue
                                    }
                                },
                                {
                                    "wildcard": {
                                        "title": "*" + searchValue + "*"
                                    }
                                },
                                {
                                    "wildcard": {
                                        "content": "*" + searchValue + "*"
                                    }
                                }
                            ]
                        }
                    },
                    "highlight": {
                        "pre_tags": ["<font style=\"background:yellow\" color=\"red\">"],
                        "post_tags": ["</font>"],
                        "fields": {
                            "title": {},
                            "content": {}
                        }
                    }
                }//搜索所有笔记
                 // 指定笔记本进行搜索
                if (searchValue.length > 0) {
                    this.https.searchNoteByWords(queryData).then((data) => {
                        let result = data.data.hits.hits
                        if (result.length > 0) {
                            // 封装 currentNotes  直接利用查询到的数据进行封装note
                            let searchNotes = []
                            result.forEach(item => {
                                // 覆盖
                                if (item.highlight) {
                                    item._source.title = item.highlight.title ? item.highlight.title[0] : item._source.title;
                                    item._source.content = item.highlight.content ? item.highlight.content[0] : item._source.content;
                                    item._source.id = item._id
                                    searchNotes.push(item._source)
                                }
                            })
                            // console.log(searchNotes)
                            /*封装tags 便于list显示*/
                            searchNotes.forEach((s) => {
                                s.tagList = []
                                if (s.tag_uid) {
                                    let tagIds = s.tag_uid.split(",")
                                    let tagList = []
                                    tagIds.forEach((t) => {
                                        if (t.length > 0) tagList.push(this.$store.state.tags.filter((tag) => tag.id == t)[0])
                                    })
                                    s.tagList = tagList
                                }
                                /*封装字段*/
                                s.createTime = s.create_time.replace("+08:00", " ").replace("T", " ")
                                s.updateTime = s.update_time.replace("+08:00", " ").replace("T", " ")
                            })
                            /* this.$store.state.searchNotesList = searchNotes*/
                            this.tool.getDateTimes(searchNotes, this.$store.state.sortWay);
                            // console.log("搜索结果数组长度", searchNotes.length);
                            // 封装搜索结果
                            this.$store.state.currentNoteList = searchNotes

                            if (searchNotes.length > 0) {
                                this.$store.state.currentNote = searchNotes[0]
                            }
                        }
                    });
                }
                // 字段为空时展示所有笔记
                else {
                    this.switchToSearchMode()
                    // 显示当前笔记本中的所有笔记
                    this.$store.state.currentIndex = 0
                    this.$store.state.currentNoteList = this.$store.state.currentNoteBookNoteList
                    this.$store.state.currentNote = this.$store.state.currentNoteBookNoteList[0]
                }
            },
            exefunc(searchValue) {
                this.lastTime = setTimeout(() => {
                    if (searchValue) this.https.insertSearchWords({keyword: searchValue}).then(({data}) => {
                        console.log('保存搜索字段到数据库', searchValue, data)
                    })
                }, 2000)
            }
        }
        ,
        watch: {
            /*监听用户停止输入*/
            searchValue(searchValue) {
                this.search(searchValue)
                if (this.lastTime == 0) {
                    this.exefunc(searchValue)
                } else {
                    clearTimeout(this.lastTime)
                    this.exefunc(searchValue)
                }
            }
            ,
        }
    }
</script>

<style>
    /*排序栏的样式*/
    .listTitle {
        margin: 5px 0px;
        display: flex;
        justify-content: flex-start; /*主轴上靠左 flex-start*/
        flex-wrap: wrap; /*不换行*/
        justify-content: space-between; /**无法实现开头和结尾对其 *!*/
        align-items: center; /*侧轴上居中*/
        /*width: 600px; !*适配手机预览*!*/
        width: 100%;
    }

    .innerCenter {
        /*background: aqua;*/
        display: flex;
        justify-content: center; /*主轴上居中*/
        align-items: center; /*侧轴上居中*/
    }

    /*笔记列表中的内容样式  多行截断  (超级简化版)*/
    .more-line {
        font-size: mini;
        display: -webkit-box !important;
        overflow: hidden;
        text-overflow: ellipsis;
        word-break: break-all;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
    }


    /*笔记列表中的标题样式*/
    .titleInList {
        /*width: 300px;*/
        overflow: hidden;
        /*文本不会换行*/
        white-space: nowrap;
        /*当文本溢出包含元素时，以省略号表示超出的文本*/
        text-overflow: ellipsis;
    }

    /* 美化列表的滚动条样式*/
    .page-scroll {
        height: 100%;
        width: 100%;
    }

    .page-scroll .el-scrollbar__wrap {
        overflow-x: hidden;
    }
    /*以免出现丑陋的滑动条*/
    .el-scrollbar__wrap{
        margin-right: -200px !important;
    }

    /*给排序图标按钮指定大小*/
    .sortButton .el-button.is-round {
        border-radius: 55px;
        padding: 2px 7px;
    }

    /*搜索图标的大小*/
    .search .el-input__inner {
        height: 22px !important;
    }

    .search .el-input__icon {
        line-height: 22px;
    }


    /*搜索图标字体*/
    .el-input__icon {
        line-height: 20px;
    }

    /*控制搜索框的间隔*/
    .el-row {
        margin-bottom: 0px !important;
    }

    #noteList .el-aside {
        background-color: #ffffff !important;
    }

    .el-container {
        display: flex;
        flex-direction: row;
        flex: 0;
        flex-basis: auto;
        box-sizing: border-box;
        min-width: 0;

    }

    .el-main {
        display: block;
        flex: 1;
        flex-basis: auto;
        overflow: auto;
        box-sizing: border-box;
        padding: 0px;
    }
</style>