<template>
    <div>
        <el-container v-if="$store.state.currentNoteList.length > 0" :style="{height :$store.state.clientH + 'px'}" >
            <!--标题 & 工具栏--> <!--$router.history.current.name != 'imageList'-->
            <el-header v-if="$router.history.current.name == 'notepage'" style="padding: 0 5px;">
                <!--笔记本 标签 收藏 删除  日期 位置-->
                <el-row class="toolLeft"> <!--toolLeft 让笔记图标在div中居中-->
                    <!--笔记本和图标放一起-->
                    <i class="el-icon-notebook-2"></i>
                    <!--让下拉框随着内容的变化动态的改变宽带 字体的大小是 12 英文和中文的宽度不一样 粗略的进行计算 -->
                    <el-select v-model="currentNoteBook" filterable placeholder="请选择"
                               size="mini"
                               :disabled="$store.state.currentNote.wastepaper"
                               :style="{width: getBt(currentNoteBook.label)*6 + 70+ 'px'}">
                        <!--slice(3) 过滤掉 数据库中 所有笔记，收藏，废纸篓三项-->
                        <el-option
                                v-for="item in $store.state.noteBooks.slice(3)"
                                :key="item.id"
                                :label="item.title"
                                :value="{ value: item.id, label: item.title }">
                        </el-option>
                    </el-select>

                    <!--收藏-->
                    <el-tooltip class="item"
                                :disabled="$store.state.currentNote.wastepaper"
                                :content="star ? '取消收藏': '收藏'"
                                placement="bottom">
                        <!--给图标单独设置大小 采用 i 设置时会影响全局-->
                        <el-button :disabled="$store.state.currentNote.wastepaper" size="mini"
                                   style="margin-left: 10px;padding: 0px">
                            <i @click="starClick" :class="star ? 'el-icon-star-on': 'el-icon-star-off' "
                               style="font-size: 25px"></i>
                        </el-button>
                    </el-tooltip>

                    <!--删除笔记-->
                    <el-tooltip class="item"
                                :content="$store.state.currentNote.wastepaper ? '彻底删除笔记' : '移入到废纸篓'"
                                placement="bottom">
                        <el-button size="mini" style="margin-left: 10px;padding: 0px">
                            <i class="el-icon-delete" @click="deleteClick" style="font-size: 25px"></i>
                        </el-button>
                    </el-tooltip>

                    <!--标签-->
                    <noteTag style="margin-left: 10px" ref="noteTag"></noteTag>

                    <!--恢复删除的笔记-->
                    <el-button @click="recoverNote" size="mini" type="primary" round
                               v-if="$store.state.currentNote.wastepaper"
                               style="margin-left: 10px;">
                        恢复笔记
                    </el-button>


                    <!--日期 -->
                    <div class="mydate" style="margin-left: 10px">
                        <el-date-picker
                                ref="timePicker"
                                style="width: 150px;"
                                v-model="createTime"
                                editable
                                size="mini"
                                type="datetime"
                                placeholder="选择日期时间"
                                :default-time="createTime.split(' ')[0]">
                        </el-date-picker>
                    </div>

                    <!--位置-->
                    <div style="margin-left: 10px">
                        <i class="el-icon-location-information"></i>
                        <a href="#">{{location}}</a>
                    </div>
                </el-row>
            </el-header>

            <el-main style="padding: 7px">
                <el-scrollbar class="page-scroll">
                    <vmd></vmd>
                </el-scrollbar>
            </el-main>
        </el-container>
        <div v-else style="text-align: center;width: 100%;">
            <h1>空空如也 </h1>
        </div>
    </div>
</template>

<script>
    import noteTag from "./NoteTag"
    import vmd from "./Vmd"
    let dayjs = require('dayjs');

    export default {
        name: "Note",
        components: {
            noteTag,
            vmd
        },
        data() {
            return {

            }
        },

        computed: {
            title: {
                get: function () {
                    return this.$store.state.currentNote.title
                },
                set: function (newValue) {
                    this.$store.state.currentNote.title = newValue
                    this.https.updateNote({id: this.$store.state.currentNote.id, title: newValue}).then(({data}) => {
                        console.log("修改数据库成功", data);
                    })
                }
            },
            content: {
                get: function () {
                    return this.$store.state.currentNote.content
                },
                set: function (newValue) {
                    this.$store.state.currentNote.content = newValue
                    this.https.updateNote({id: this.$store.state.currentNote.id, content: newValue}).then(({data}) => {
                        console.log("修改数据库成功", data);
                    })
                }
            },

            tagList: {
                get: function () {
                    let dynamicTags = []
                    this.$store.state.currentNote.tagList.forEach((tag) => dynamicTags.push(tag.title))
                    return dynamicTags
                },
                set: function (newValue) {
                    // this.$store.state.currentNote.content = newValue
                    console.log('tagList..undone ', newValue)
                }
            },

            star: {
                get: function () {
                    return this.$store.state.currentNote.star
                },
                set: function (newValue) {
                    this.$store.state.currentNote.star = newValue
                    this.https.updateNote({id: this.$store.state.currentNote.id, star: newValue}).then(({data}) => {
                        console.log("修改数据库成功", data);
                    })
                }
            },
            currentNoteBook: {
                get: function () {
                    let currentNoteBook = this.$store.state.noteBooks.filter((n) => n.id == this.$store.state.currentNote.pid)[0]
                    return {value: currentNoteBook.id, label: currentNoteBook.title}
                },
                set: function (currentNoteBook) {
                    // 更新笔记本的id  {"value": 4,"label": "数据库"}
                    this.$store.state.currentNote.pid = currentNoteBook.value

                    this.https.updateNote({
                        id: this.$store.state.currentNote.id,
                        pid: currentNoteBook.value
                    }).then(({data}) => {
                        console.log("移动笔记本成功", data);
                        // 进行全量更新树  级联更新自己和新笔记本 显示的笔记数量
                        // 放弃局部更新树(遍历判断树的次数更频繁)
                        this.$store.state.noteBooksTree = data.data
                        this.tool.addNoteCount(this.$store.state.noteBooksTree,'notebook')
                    })
                }
            },
            createTime: {
                get: function () {
                    return this.$store.state.currentNote.createTime
                },
                set: function (newValue) {
                    let formatTime = dayjs(newValue).format('YYYY-MM-DD HH:mm:ss')
                    this.$store.state.currentNote.createTime = formatTime
                    /*收起时间选择面板*/
                    this.foldTimePicker()
                    console.log('更新笔记创建时间', this.$store.state.currentNote.title, formatTime)
                    this.https.updateNote({
                        id: this.$store.state.currentNote.id,
                        createTime: newValue
                    }).then(({data}) => {
                        console.log("修改数据库成功", data);
                    })
                }
            },
            location: {
                get: function () {
                    // console.log(this.$store.state.currentNote)
                    return this.$store.state.currentNote.location
                },
                set: function (newValue) {
                    this.$store.state.currentNote.location = newValue
                    // console.log(newValue)
                    /* this.https.updateNote({id: this.$store.state.currentNote.id, createTime: newValue}).then(({data}) => {
                         console.log("修改数据库成功", data);
                     })*/
                }
            },
        },
        methods: {
            /*收起时间选择面板*/
            foldTimePicker(){
                this.$refs['timePicker'].pickerVisible = false
            },

            /*获取带有中文字符的长度  一个中文的宽度对应两个英文的宽度*/
            getBt(str) {
                let char = str.replace(/[^\x00-\xff]/g, '**');
                return char.length;
            },

            handleTargetDragOver(e) {
                let firstLevelId = this.tool.getfirstLevelId(this.$store.state.currentNode)
                // 判断节点是否为标签子的节点
                if (firstLevelId == "allTags") {
                    e.preventDefault(); // 使该区域允许释放
                }

            },
            handleTargetDrop() {
                console.log('在内容区放置了当前拖拽标签')
                let tag = this.$store.state.currentNode.data
                // 触发 noteTag中的添加标签 事件
                // console.log(this.$store.state.currentNode)
                this.$refs.noteTag.addTag(tag);
            },

            starClick() {
                this.star = !this.star
                this.$message({
                    message: this.star ? '已收藏' : '取消收藏',
                    type: this.star ? 'success' : 'info',
                    duration: 1000,  //显示时间, 毫秒。设为 0 则不会自动关闭 默认3000
                    center: true
                });
                /*更新收藏笔记的数据*/
                if (this.star) {
                    this.$store.state.starNotesList.push(this.$store.state.currentNote)
                    /* 2.更新所有笔记*/
                    this.$store.state.notes.forEach((n) => {
                        if (n.id == this.$store.state.currentNote.id) n.star = true
                    })

                } else {
                    this.removeCurrentNoteByTypeName('starNotesList')
                    /* 2.更新所有笔记*/
                    this.$store.state.notes.forEach((n) => {
                        if (n.id == this.$store.state.currentNote.id) n.star = false
                    })
                }

            },
            removeCurrentNoteByTypeName(typeName) {
                this.$store.state[typeName] = this.$store.state[typeName].filter((n) => n.id != this.$store.state.currentNote.id)
            },
            deleteClick() {
                console.log('deleteClick...')
                let msg = this.$store.state.currentNote.wastepaper ? '彻底删除' : '移入到废纸篓'
                this.$confirm('此操作将该笔记' + msg + ', 是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    center: true
                }).then(() => {
                    this.deleteNote()
                    this.$message({type: 'success', message: '成功!', duration: 1000,});
                }).catch(() => {
                    this.$message({
                        type: 'info',
                        message: '已取消',
                        duration: 1000,
                    });
                });
            },

            deleteNote() {
                // 2.1 数据库
                // 	    2.1.1 将 数据库中该note的 status 字段改为 1 表示逻辑删除
                //   	2.1.2 修改该笔记所包含的所有tag数量都 -1
                //      2.1.3 修改该笔记所在笔记本数量 -1
                // 2.2 页面变化
                //  	2.2.1 设置成功后 将该 note从currentNotes列表和note列表中移除
                //  	2.2.2 noteBook中也删除相应的笔记
                //      2.2.3 当前的note页面显示 栈顶笔记
                this.https.deleteNote({id: this.$store.state.currentNote.id}).then(({data}) => {
                    console.log("删除成功", data);
                    /*更新笔记本中笔记的数量 更新树形展示*/
                    this.$store.state.noteBooksTree = data.data
                    this.tool.addNoteCount(data.data,'notebook')

                    /* 1.更新所有笔记*/
                    this.removeCurrentNoteByTypeName('notes')

                    /* 2.更新 logicDeletedNotesList*/
                    if (!this.$store.state.currentNote.wastepaper) {//从列表移入到废纸篓
                        this.$store.state.currentNote.wastepaper = true
                        this.$store.state.wastepaperNotesList.unshift(this.$store.state.currentNote)
                    }

                    /* 3.修改当前选中的笔记*/
                    this.$store.state.currentIndex = 0
                    this.$store.state.currentNoteList.splice(this.$store.state.currentIndex, 1)
                    if (this.$store.state.currentNoteList.length > 0) {
                        this.$store.state.currentNote = this.$store.state.currentNoteList[0]
                    }
                })
            },
            /*恢复删除的笔记*/
            recoverNote() {
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
                        this.tool.addNoteCount(this.$store.state.noteBooksTree,'notebook')
                    })
                }, 1000)

            },

            replaceHeightLight(str) {
                return str.replace(/<font style="background:yellow" color="red">/gi, "").replace(/<\/font>/gi, "")
            },
            titleClick() {
                this.$store.state.currentNote.title = this.replaceHeightLight(this.$store.state.currentNote.title)
                this.$store.state.isTitleEditMode = true
            },
            contentClick() {
                this.$store.state.currentNote.content = this.replaceHeightLight(this.$store.state.currentNote.content)
                this.$store.state.isContentEditMode = true
            },
        },
    }
</script>

<style>
    .mydate .el-date-editor.el-input{
        width: 150px !important;
    }

    /*自定义提示框的宽度*/
    .el-message {
        min-width: 176px !important;
    }

    /* 让工具栏在同一行展示*/
    .toolLeft {
        margin-top: 10px;
        display: flex;
        justify-content: flex-start; /*主轴上靠左 flex-start*/
       /* flex-wrap: nowrap; !*不换行*!*/
        /*justify-content: space-between; !*无法实现开头和结尾对其 *!*/
        align-items: center; /*侧轴上居中*/
        /*width: 600px; !*适配手机预览*!*/
        width: 100%;
    }
    /*这只内容区域高度占满*/
    .el-textarea__inner,
    .el-textarea {
        height: 100% !important;
    }
</style>