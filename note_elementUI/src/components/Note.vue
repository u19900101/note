<template>
    <el-container>

        <el-header>
            <!--标题-->
            <el-row>
                <el-input
                        size="small"
                        placeholder="请输入笔记标题"
                        v-model="title">
                </el-input>
            </el-row>
            <!--笔记本 标签 收藏 删除-->
            <el-row class="toolLeft"> <!--toolLeft 让笔记图标在div中居中-->
                <!--笔记本和图标放一起-->
                <div class="toolLeft">
                    <i class="el-icon-notebook-2"></i>
                    <!--让下拉框随着内容的变化动态的改变宽带 字体的大小是 12 英文和中文的宽度不一样 粗略的进行计算 -->
                    <el-select v-model="currentNoteBook" filterable placeholder="请选择"
                               size="small"
                               :style="{width: getBt(currentNoteBook.label)*6 + 70+ 'px'}">
                        <el-option
                                v-for="item in notebooks"
                                :key="item.value"
                                :label="item.label"
                                :value="{ value: item.value, label: item.label }">
                        </el-option>
                    </el-select>
                </div>

                <!--收藏-->
                <el-tooltip class="item"
                            :content="star ? '取消收藏': '收藏'"
                            placement="bottom"
                            :hide-after=800>
                    <!--给图标单独设置大小 采用 i 设置时会影响全局-->
                    <i @click="starClick" :class="star ? 'el-icon-star-on': 'el-icon-star-off' "
                       style="margin-left: 10px;font-size: 25px"></i>
                </el-tooltip>


                <el-tooltip class="item"
                            content="删除笔记"
                            placement="bottom"
                            :hide-after=800>
                    <i class="el-icon-delete" @click="deleteClick" style="margin-left: 10px;font-size: 25px"></i>

                </el-tooltip>

                <!--标签-->
                <noteTag style="margin-left: 10px" ref="noteTag"></noteTag>

            </el-row>
        </el-header>

        <!--内容-->
        <el-main>
            <div @drop="handleTargetDrop"
                 @dragover="handleTargetDragOver"
                 style="height: 100%">
                <el-input type="textarea"
                          maxlength="1000000"
                          show-word-limit
                          v-model='content'
                > 笔记内容
                </el-input>
            </div>


        </el-main>
    </el-container>
</template>

<script>
    import noteTag from "./NoteTag"

    export default {
        name: "Note",
        components: {
            noteTag,
        },
        data() {
            return {
                notebooks: this.getNoteBooks(),
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
                    let currentNoteBook = this.notebooks.filter((n) => n.value == this.$store.state.currentNote.pid)[0]
                    // console.log('currentNoteBook is ',currentNoteBook)
                    return currentNoteBook
                },
                set: function (newValue) {
                    // 更新笔记本的id
                    this.$store.state.currentNote.pid = newValue.value
                    console.log("set currentNoteBook...", newValue);

                    this.$store.state.currentNote.star = newValue
                    this.https.updateNote({
                        id: this.$store.state.currentNote.id,
                        pid: newValue.value
                    }).then(({data}) => {
                        console.log("修改数据库成功", data);
                    })
                }
            },
        },
        methods: {
            /*获取带有中文字符的长度  一个中文的宽度对应两个英文的宽度*/
            getBt(str) {
                let char = str.replace(/[^\x00-\xff]/g, '**');
                return char.length;
            },
            // 将树形的noteBook变为一条条普通noteBook，不进行级联选择
            fromTreeToNormal(treeData, normalData) {
                treeData.forEach((item) => {
                    normalData.push(item)
                    if (item.children.length > 0) {
                        return this.fromTreeToNormal(item.children, normalData)
                    }
                })
            },

            getNoteBooks() {
                let notebooksNormal = []
                let notebooks = []
                this.fromTreeToNormal(this.$store.state.noteBooks, notebooksNormal)

                notebooksNormal.forEach((noteBook) => {
                    notebooks.push({
                        value: noteBook.id,
                        label: noteBook.title
                    })
                })
                return notebooks
            },
            handleTargetDragOver(e) {
                let firstLevelId = this.tool.getfirstLevelId(this.$store.state.currentNode)
                // 判断节点是否为标签子的节点
                if (firstLevelId == 4) {
                    e.preventDefault(); // 使该区域允许释放
                }

            },
            handleTargetDrop() {
                console.log('在内容区放置了当前拖拽标签')
                let {id, label} = this.$store.state.currentNode.data
                // 触发 noteTag中的添加标签 事件
                console.log(this.$store.state.currentNode)
                this.$refs.noteTag.addTag(label);
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
                if(this.star){
                    this.$store.state.starNoteList.push(this.$store.state.currentNote)
                    /* 2.更新所有笔记*/
                    this.$store.state.notes.forEach((n) => {
                        if(n.id == this.$store.state.currentNote.id) n.star = true
                    })

                }else {
                    this.removeCurrentNoteByTypeName('starNoteList')
                    /* 2.更新所有笔记*/
                    this.$store.state.notes.forEach((n) => {
                        if(n.id == this.$store.state.currentNote.id) n.star = false
                    })
                }

            },
            removeCurrentNoteByTypeName(typeName){
                this.$store.state[typeName] = this.$store.state[typeName].filter((n) => n.id != this.$store.state.currentNote.id)
            },
            deleteClick() {
                console.log('deleteClick...')
                this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    center: true
                }).then(() => {
                    this.logicDeleteNote()
                    this.$message({type: 'success',message: '删除成功!',duration: 1000,});
                }).catch(() => {
                    this.$message({
                        type: 'info',
                        message: '已取消删除',
                        duration: 1000,
                    });
                });
            },
            logicDeleteNote(){
                // 2.1 数据库
                // 	    2.1.1 将 数据库中该note的 status 字段改为 1 表示逻辑删除
                //   	2.1.2 修改该笔记所包含的所有tag数量都 -1
                //      2.1.3 修改该笔记所在笔记本数量 -1
                // 2.2 页面变化
                //  	2.2.1 设置成功后 将该 note从currentNotes列表和note列表中移除
                //  	2.2.2 noteBook中也删除相应的笔记
                //      2.2.3 当前的note页面显示 栈顶笔记
                this.https.logicDeleteNote({id: this.$store.state.currentNote.id}).then(({data}) => {
                    console.log("逻辑删除成功  ", data);
                    /* 1.更新所有笔记*/
                    this.removeCurrentNoteByTypeName('notes')

                    /* 2.修改当前选中的笔记*/
                    this.$store.state.currentIndex = 0
                    this.$store.state.currentNoteList.splice(this.$store.state.currentIndex, 1)
                    if(this.$store.state.currentNoteList.length > 0) {
                        this.$store.state.currentNote = this.$store.state.currentNoteList[0]
                    }else {
                        this.$store.state.currentNote = {}
                    }
                })
                //  更新笔记本中笔记的数量
                /*this.$store.state.noteBookModule.noteBooks.filter((item) => item.id == this.pid)[0].noteCount -= 1
                if(this.$store.state.noteBookModule.currentNoteBook.id != 0){
                    this.updateNotes()
                }*/
            }
        },

    }
</script>

<style>

    /*自定义提示框的宽度*/
    .el-message {
        min-width: 176px !important;
    }

    /* 让工具栏在同一行展示*/
    .toolLeft {
        display: flex;
        justify-content: flex-start; /*主轴上靠左*/
        /*justify-content: space-between !important;*/ /*space-between 无法实现开头和结尾对其 */
        align-items: center; /*侧轴上居中*/
    }


    .el-main {
        /*17*/
        padding: 80px 20px 0px 20px !important;
    }

    /*这只内容区域高度占满*/
    .el-textarea__inner,
    .el-textarea {
        height: 100% !important;
    }
</style>