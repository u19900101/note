<template>
    <el-container class="nav">
        <!--点击逻辑
    点击上级菜单时 1.显示对应菜单的笔记(标签) 2.不展开子项目
    点击 三角符号时 收起或折叠子项 accordion -->
        <el-aside :style="{width: navWidth + 'px'}">
            <el-tree :data="data"
                     ref="mytree"
                     node-key="id"
                     default-expand-all
                     :expand-on-click-node=false
                     :highlight-current="true"
                     @node-click="handleNodeClick"
                     @node-drag-start="handleDragStart"
                     @node-drag-enter="handleDragEnter"
                     @node-drag-leave="handleDragLeave"
                     @node-drag-over="handleDragOver"
                     @node-drag-end="handleDragEnd"
                     @node-drop="handleDrop"
                     @node-contextmenu=handleNodeContextmenu
                     draggable
                     :allow-drop="allowDrop"
                     :allow-drag="allowDrag">
                <!-- 自定义节点-->
                <span class="custom-tree-node" slot-scope="{ node, data }">
                    <!-- 给一级节点 设置自定义图标-->
                    <!-- 收藏-1 笔记-2 笔记本-3 标签-4 废纸篓-5 新建-6-->
                     <i v-if="node.level == 1" :class=icons[data.id-1]></i>
                    <!-- 动态设置一级标题和子节点的字体大小-->
                    <!--不能对 node在此处实行插值查看-->
                    <span :style="{'font-size': node.level == 1 ? '20px':'14px'}" style="margin-left: 5px">{{ data.title }}</span>
                </span>
            </el-tree>
        </el-aside>

        <!--导航栏宽度可拖拽组件-->
        <el-aside width="4px">
            <borderLine @widthChange="widthChange"/>
        </el-aside>
    </el-container>


</template>

<script>
    import borderLine from "./BorderLine";
    import {getNoteBooksTree} from "../server";

    export default {
        name: "tree",
        components: {
            borderLine,
        },
        data() {

            return {
                navWidth: this.$store.state.sortWay.navWidth, // 导航栏的初始宽度
                navMax: 800,
                navMin: 150,
                //收藏-1 笔记-2 笔记本-3 标签-4  废纸篓-5  新建笔记-6
                icons: ['el-icon-star-on', 'el-icon-document', 'el-icon-notebook-2', 'el-icon-discount', 'el-icon-delete', 'el-icon-circle-plus-outline'],

                defaultProps: {
                    children: 'children',
                    label: 'title'
                }
            };
        },
        methods: {

            /*导航栏宽度可拖拽组件*/
            widthChange(movement) {
                this.navWidth -= movement
                if (this.navWidth < this.navMin || this.navWidth > this.navMax) {
                    this.navWidth = this.navWidth > this.navMin ? this.navMax : this.navMin
                }
                /*将导航栏的宽度写进数据库*/
                this.https.updateSortWay({id: 1, navWidth: this.navWidth}).then(({data}) => {
                    console.log(data)
                })
            },
            /*
            * event、传递给 data 属性的数组中该节点所对应的对象、节点对应的 Node、节点组件本身。*/
            handleNodeContextmenu(e, data, node) {
                console.log('右键点击了节点', node.data.title)
            },
            /*
            * 传递给 data 属性的数组中该节点所对应的对象、节点对应的 Node、节点组件本身。
            * */
            handleNodeClick(data, node, e) {
                /*  收藏-1 全部笔记-2 笔记本-3 标签-4 废纸篓-5 新建-6 */
                // console.log('clicked Node',data.id,data.title);
                switch (data.id) {
                    case 'insertNote':
                        this.insertNote();
                        break;
                    case 'allStarNotes':
                        this.initCurrentNoteListByName("starNotesList", 1);
                        break; // 收藏笔记
                    case 'noteBooks': //'笔记本'
                    case 'allNotes':
                        this.initCurrentNoteListByName("notes", 0);
                        break;//所有笔记
                    case 'allTags':
                        console.log('标签');
                        break;
                    case 'wastePaper':
                        this.initCurrentNoteListByName("wastepaperNotesList", 2);
                        break; /*'废纸篓' */
                    default:
                        /*区分是 点击的是笔记本 还是 标签*/
                        let firstLevelTitle = this.getfirstLevelTitle(node)
                        if (firstLevelTitle == '笔记本') {
                            this.initCurrentNoteListByName("noteBookNameId", data.id);
                        } else {

                            this.initTagNotesListByTagNode(data)
                        }

                }
            },
            insertNote() {
                console.log('insertNote')
                /* 若当前笔记 所有笔记、收藏、废纸篓 则指定 id = 3 (我的抗战)笔记本为本次默认笔记本*/
                let pid = this.$store.state.currentNoteBook.id
                if (pid < 2) pid = 3
                this.https.insertNote({'pid': pid}).then(({data}) => {
                    let newNote = data.data;
                    console.log("新建笔记 ", data);
                    // 2.修改 currentNotes,将新建置顶
                    this.$store.state.currentNoteList.unshift(newNote)
                    this.$store.state.currentNote = newNote
                    this.$store.state.currentIndex = 0  // 让新建的笔记处于选中状态


                    // 修改相应笔记本的笔记数量  和树形列表的显示
                    this.$store.state.noteBooks.filter(item => item.id == pid)[0].noteCount += 1
                    // 树形  更新本节点和所有父节点的数量
                    //  选择笔记本的情况下 需要对notes 进行追加，因为此时 currentNoteList 指向为当前笔记本
                    //    3.将新建笔记加入notes中
                    if (this.$store.state.currentNoteBook.id > 2) {
                        this.$store.state.notes.unshift(newNote)
                    }
                    //  4.更新笔记数量的显示
                    this.https.getNoteBooksTree().then(({data}) => {
                        this.$store.state.noteBooksTree = data.data
                        this.tool.addNoteCount(this.$store.state.noteBooksTree)
                    })
                })
            },
            /*初始化笔记列表和笔记本*/
            initCurrentNoteListByName(currentNoteBookName, noteBookId) {

                if (currentNoteBookName == "noteBookNameId") {
                    // console.log('noteBookId is ', noteBookId);
                    /*获取所有子笔记*/
                    let parentIds = this.getChildrenIds(noteBookId, this.$store.state.noteBooksTree)
                    // console.log('parentIds are ',parentIds)
                    this.$store.state.currentNoteList = this.$store.state.notes.filter((n) => parentIds.includes(n.pid))
                } else {
                    this.$store.state.currentNoteList = this.$store.state[currentNoteBookName]
                }
                if (this.$store.state.currentNoteList.length > 0) {
                    this.$store.state.currentNote = this.$store.state.currentNoteList[0]
                    this.$store.state.currentIndex = 0
                }
                this.$store.state.currentNoteBook = this.$store.state.noteBooks.filter((n) => n.id == noteBookId)[0]
            },
            handleDragStart(node, ev) {
                // console.log('drag start', node);
                //    将当前拖拽节点设置为全局变量，以便在内容区能取值
                this.$store.state.currentNode = node
            },
            handleDragEnter(draggingNode, dropNode, ev) {
                // console.log('tree drag enter: ',draggingNode.data.title ,'--> ',dropNode.data.title);
            },
            handleDragLeave(draggingNode, dropNode, ev) {
                // console.log('tree drag leave: ', draggingNode.data.title ,'--> ',dropNode.data.title);
            },
            handleDragOver(draggingNode, dropNode, ev) {
                // console.log('tree drag over: ', draggingNode.data.title ,'--> ',dropNode.data.title);
            },

            /*获取拖拽节点的 {preId, currentId, nextId}*/
            getIds(draggingNode) {
                let [preId, currentId, nextId, currentIndex] = [0, 0, 0, 0]
                let brotherNotes = this.$refs.mytree.getNode(draggingNode.data.id).parent.data.children
                /* console.log(brotherNotes)*/
                if (brotherNotes.length == 1) {
                    currentId = brotherNotes[0].id
                    console.log('只有一个节点：', 0, brotherNotes[0].id, 0)
                } else {
                    // 获取相邻的两个节点
                    brotherNotes.forEach((item, index, array) => {
                        if (item.id == draggingNode.data.id) {
                            currentIndex = index
                        }
                    })
                    currentId = brotherNotes[currentIndex].id
                    //节点放置的位置
                    if (currentIndex > 0 && currentIndex < brotherNotes.length - 1) {
                        // console.log( '中间：',brotherNotes[currentIndex - 1].title, brotherNotes[currentIndex].title, brotherNotes[currentIndex +1].title)
                        console.log('中间：', brotherNotes[currentIndex - 1].id, brotherNotes[currentIndex].id, brotherNotes[currentIndex + 1].id)
                        preId = brotherNotes[currentIndex - 1].id
                        nextId = brotherNotes[currentIndex + 1].id
                    } else if (currentIndex == 0) { //当拖到第一个时
                        console.log('第一：', 0, brotherNotes[currentIndex].id, brotherNotes[currentIndex + 1].id)
                        nextId = brotherNotes[currentIndex + 1].id
                    } else {     //当拖到最后一个时
                        console.log('最后：', brotherNotes[currentIndex - 1].id, brotherNotes[currentIndex].id, 0)
                        preId = brotherNotes[currentIndex - 1].id
                    }
                }
                return {preId, currentId, nextId}
            },
            handleDragEnd(draggingNode, dropNode, dropType, ev) {
                // 无法直接获取拖拽完成后的node父id，才用使用id获取当前节点的方式，间接获取pid，用于更新
                let currentParentNode = this.$refs.mytree.getNode(draggingNode.data.id).parent
                // console.log('tree drag end: ', draggingNode.data.title, '--> ', currentParentNode.data.title);

                let {preId, currentId, nextId} = this.getIds(draggingNode)

                // 区分是 笔记本节点拖动 和 标签节点拖动
                let firstLevelTitle = this.getfirstLevelTitle(dropNode)

                let obj = {
                    preId,
                    currentId,
                    nextId,// 当拖拽到一级节点下时  给pid 赋值为 0 与数据库保持一致
                    pid: currentParentNode.level == 1 ? 0 : currentParentNode.data.id,
                    oldPid: draggingNode.data.pid
                }
                // 笔记本 - 3
                if (firstLevelTitle == '笔记本') {
                    console.log('noteBook...', firstLevelTitle);
                    this.https.updateNotebook(obj).then(({data}) => {
                        console.log('更新笔记本成功', data)
                        /*更新tree*/
                        this.$store.state.noteBooksTree = data.data
                        this.tool.addNoteCount(this.$store.state.noteBooksTree)
                    })
                } else {  // 拖拽标签 新旧两个分支 全父节点更新
                    console.log('tag...');

                   /* /!*获取当前父节点的笔记数量*!/
                    if (obj.pid != 0) {
                        obj.NoteCount = this.getNodeCountByTagId(obj.pid, this.$store.state.tags)
                    }

                    /!*获取原父节点的笔记数量*!/
                    if (obj.oldPid != 0) {
                        obj.oldNoteCount = this.getNodeCountByTagId(obj.oldPid, this.$store.state.tags)
                    }

                    console.log(obj)*/

                     this.https.updateTag(obj).then(({data}) => {
                         /*更新Tag tree*/
                         this.$store.state.tags = data.data
                         this.tool.addNoteCount(this.$store.state.tags)
                     })
                }


            },
            handleDrop(draggingNode, dropNode, dropType, ev) { // dropType -- inner

                // console.log('tree drop: ', draggingNode.data.id ,'--> ',dropNode.data.id);
                // console.log('tree drop: ', draggingNode.data.title ,'--> ',dropNode.data.title);


            },
            allowDrop(draggingNode, dropNode, type) {
                /*console.log('allowDrop...',draggingNode, dropNode)*/
                // 不能移动到一级目录
                if (dropNode.level == 1) {
                    return false
                }
                // 只能在同一个一级目录中进行移动
                // 找到 level = 1，比较两者的 一级节点id是否相等
                let draggingNodeAncestors = this.getfirstLevelId(draggingNode)
                let dropNodeAncestors = this.getfirstLevelId(dropNode)

                return draggingNodeAncestors == dropNodeAncestors
            },
            allowDrag(draggingNode) {
                // 每次拖拽之前进行判断  禁止对 一级的四个节点进行拖动
                return draggingNode.level != 1;
            },
            getfirstLevelId(node) {
                if (node.level != 1) {
                    return this.getfirstLevelId(node.parent)
                } else {
                    return node.id
                }
            },
            getfirstLevelTitle(node) {
                if (node.level != 1) {
                    return this.getfirstLevelTitle(node.parent)
                } else {
                    return node.data.title
                }
            },

            //遍历树 找到所有子id
            getChildrenIds(noteBookId, pData) {
                for (let n of pData) {
                    /*在自身节点中找*/
                    if (n.id == noteBookId) {
                        let res = []
                        this.getChildrenNotes(n, res)
                        return res
                    }
                    /*在子节点中找*/
                    if (n.children.length > 0) {
                        let res = this.getChildrenIds(noteBookId, n.children)
                        if (res) return res
                    }
                }
            },
            getChildrenNotes(treeNode, res) {
                if (treeNode.children.length > 0) {
                    treeNode.children.forEach((n) => {
                        this.getChildrenNotes(n, res)
                    })
                }
                res.push(treeNode.id)

            },
            initTagNotesListByTagNode(tagNodeData) {

                /*3.初始化 currentNoteList */
                this.$store.state.currentNoteList = this.getNoteListByTagNode(tagNodeData)

                /*3.初始化 currentNote currentNoteBook*/
                if (this.$store.state.currentNoteList.length > 0) {
                    this.$store.state.currentNote = this.$store.state.currentNoteList[0]
                    this.$store.state.currentIndex = 0
                    this.$store.state.currentNoteBook = this.$store.state.noteBooks.filter((n) => n.id == this.$store.state.currentNote.pid)[0]
                }
            },
            getTagChildrenIds(tagIds, data) {
                tagIds.push(data.id)
                if (data.children.length > 0) {
                    data.children.forEach((n) => {
                        this.getTagChildrenIds(tagIds, n)
                    })
                }
            },
            getNoteListByTagNode(tagNodeData) {
                /*1.查找当前标签的所有子标签*/
                let tagIds = []
                this.getTagChildrenIds(tagIds, tagNodeData)

                /*2.找到 包含tagIds 的所有笔记*/
                return this.$store.state.notes.filter((n) => {
                    /*判断两者的tagList是否有交集*/
                    if (n.tagList.length > 0) {
                        let resIds = n.tagList.filter(function (v) {
                            return tagIds.indexOf(v.id) != -1
                        })
                        return resIds.length > 0
                    }
                    return false
                })
            },

            getTagNodeDataById(tagId, tagTreeData) {
                for (let t of tagTreeData) {
                    if (t.id == tagId) return t
                    if (t.children.length > 0) this.getTagNodeDataById(tagId, t.children)
                }
            },
            getNodeCountByTagId(tagId, tagTree) {
                let currentTagNode = this.getTagNodeDataById(tagId, tagTree)
                return this.getNoteListByTagNode(currentTagNode).length
            }
        },
        computed: {
            data: {
                get: function () {
                    return [
                        {
                            id: 'insertNote',
                            title: '新建笔记'
                        },
                        {
                            id: 'allStarNotes',
                            title: '收藏 (' + this.$store.state.starNotesList.length + ')'
                        },
                        {
                            id: 'allNotes',
                            title: '全部笔记 (' + this.$store.state.notes.length + ')'
                        },
                        /*笔记本*/
                        {
                            id: 'noteBooks',
                            title: '笔记本',
                            children: this.$store.state.noteBooksTree
                        },
                        /*标签*/
                        {
                            id: 'allTags',
                            title: '标签',
                            children: this.$store.state.tags
                        },
                        /*废纸篓*/
                        {
                            id: 'wastePaper',
                            title: '废纸篓 (' + this.$store.state.wastepaperNotesList.length + ')'
                        },
                    ]
                },
                set: function (newValue) {

                }
            }
        },
        created() {

        }
    };
</script>

<style>
    .nav {
        background: #2a333c;
        height: 100%;
    }

    .el-tree {
        background: #2a333c !important; /* 整体的背景色*/
        color: #ffffff !important; /*字体颜色*/
        active-text-color: "#ffd04b" !important;
        height: 100%;
    }

    .el-tree-node__label {
        font-size: 20px;
    }

    /* 树背景色*/
    .el-tree-node:focus > .el-tree-node__content {
        background-color: #4a5d6b !important;
    }

    /* 鼠标悬浮时的背景色*/
    .el-tree-node__content:hover {
        background-color: #354553 !important;
    }

    /* 节点选中时的背景色*/
    .el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content {
        background-color: rgba(212, 119, 27, 0.53) !important;
    }

    /* 一级节点的高度*/
    .el-tree-node__content {
        height: 39px !important;
    }

    /* 二级节点的高度*/
    .el-tree-node__children .el-tree-node__content {
        height: 23px !important;
    }

</style>