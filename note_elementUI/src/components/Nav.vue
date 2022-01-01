<template>
    <el-container>
        <!--点击逻辑
    点击上级菜单时 1.显示对应菜单的笔记(标签) 2.不展开子项目
    点击 三角符号时 收起或折叠子项 accordion   default-expand-all   :expand-on-click-node=false-->
        <el-aside :style="{width: navWidth + 'px'}">
            <el-scrollbar class="page-scroll">
                <el-tree :data="data"
                         ref="mytree"
                         node-key="id"
                         :default-expanded-keys="[$store.state.expandedKeyId]"
                         :highlight-current="true"
                         @node-click="handleNodeClick"
                         @node-expand="handleNodeExpand"
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
                        <!--右键修改笔记本名称   -->
                         <div v-if="node.data.isEdit" @click.stop class="editNoteBookName">
                              <input v-focus
                                     v-model=node.data.title
                                     @blur.stop="NodeBlur(node, data)"
                                     @keyup.enter="NodeBlur(node, data)"
                                     size="small"
                                     placeholder="请输入名称"></input>
                         </div>
                         <span v-else :style="{'font-size': node.level == 1 ? '20px':'14px'}" style="margin-left: 5px">{{ data.title }}</span>
                    </span>
                </el-tree>
            </el-scrollbar>
        </el-aside>
        <!--右键菜单-->
        <div v-show="showContextmenu"
             @mouseleave="menuMouseLeave = true"
             @mouseenter="menuMouseLeave = false"
             :style="{left:menuLeft + 'px',top:menuTop + 'px'}" style="position: absolute;z-index: 2001">
            <el-menu
                    default-active="2"
                    class="el-menu-vertical-demo"
                    background-color="#545c64"
                    text-color="#fff"
                    active-text-color="#ffd04b">
                <el-menu-item index="2" @click="reNameNode"
                              :disabled="menuEnable()">
                    <i class="el-icon-edit"></i>
                    <span slot="title">重命名</span>
                </el-menu-item>
                <el-menu-item index="3" @click="deleteNode"
                              :disabled="menuEnable()">
                    <i class="el-icon-delete"></i>
                    <span slot="title">删除</span>
                </el-menu-item>
                <el-menu-item index="4" @click="addChildNode">
                    <i class="el-icon-folder-add"></i>
                    <span slot="title">添加子项目</span>
                </el-menu-item>
                <el-menu-item v-if="menuEnable()" index="5" @click="manage">
                    <i class="el-icon-s-tools"></i>
                    <span slot="title">管理</span>
                </el-menu-item>
            </el-menu>
        </div>
        <!--导航栏宽度可拖拽组件-->
        <el-aside width="4px">
            <borderLine @widthChange="widthChange"/>
        </el-aside>
    </el-container>
</template>

<script>
    import borderLine from "./BorderLine";
    /*深拷贝*/
    let _ = require('lodash')
    export default {
        name: "tree",
        components: {
            borderLine,
        },
        data() {
            return {
                noteCountTemp: '', //保存 修改笔记本名称时的笔记数量
                navWidth: this.$store.state.sortWay.navWidth, // 导航栏的初始宽度
                navMax: 800,
                navMin: 150,
                //收藏-1 笔记-2 笔记本-3 标签-4  废纸篓-5  新建笔记-6
                icons: ['el-icon-star-on', 'el-icon-document', 'el-icon-notebook-2', 'el-icon-discount', 'el-icon-delete', 'el-icon-circle-plus-outline'],
                defaultProps: {
                    children: 'children',
                    label: 'title'
                },
                lastTime: 0, // 定时器的初始值
                showContextmenu: false,// 右键显示菜单
                menuLeft: 0, //菜单定位
                menuTop: 0,
                currentNode: {data: {id: -1}}, //当前节点
                menuMouseLeave: true, //
                currentFirstLevelNodeId: -1,//当前一级节点的id
            };
        },
        methods: {
            /* 右键  传递给 data 属性的数组中该节点所对应的对象、节点对应的 Node、节点组件本身。*/
            handleNodeContextmenu(e, data, node) {
                console.log('右键节点', node.data.id, node)
                this.currentNode = node
                /*右键节点同时展开该节点*/
                this.$store.state.expandedKeyId = node.data.id
                /*右键非一级节点时 显示右键菜单  增删改*/
                if (node.level != 1 || ['noteBooks', 'allTags', 'tagImages'].indexOf(node.data.id) != -1) {
                    let charL = data.title.replace(/[^\x00-\xff]/g, '**').length;
                    this.menuLeft = charL * 6 + 70 //菜单定位
                    this.menuTop = e.clientY + 168 < document.body.clientHeight ? e.clientY : document.body.clientHeight - 168 //菜单定位
                    this.showContextmenu = true
                    // 面板出现就开始监控鼠标按下  利用延时解决mousedown事件覆盖click事件
                    setTimeout(() => {
                        document.addEventListener('mousedown', this.mouseDown)
                    }, 200);
                }
            },
            /*节点点击  传递给 data 属性的数组中该节点所对应的对象、节点对应的 Node、节点组件本身。*/
            handleNodeClick(data, node, e) {
                /*  收藏-1 全部笔记-2 笔记本-3 标签-4 废纸篓-5 新建-6 */
                /*关闭搜索模式*/
                this.currentNode = node
                this.$store.state.listTitle = data.title.split(" ")[0]
                if (this.$store.state.isSearchMode) this.turnOffSearchMode()
                /*默认文件视图模式关闭*/
                switch (data.id) {
                    case 'insertNote':
                        this.toPage('notepage')
                        this.insertNote();
                        break;
                    case 'allStarNotes': // 收藏笔记
                        this.initCurrentNoteListByName("starNotesList", 1);
                        break;
                    case 'calendar': // 日历视图
                        this.toPage('calendar')
                        break;
                    case 'noteBooks': //'笔记本'
                        break;
                    case 'allNotes'://所有笔记
                        this.$store.state.currentNoteBook = this.$store.state.noteBooks.filter((n) => n.id == 0)[0]
                        this.initCurrentNoteListByName("notes", 0);
                        break;
                    case 'allTags': //笔记标签
                        this.toPage('notepage')
                        this.$store.state.currentNoteBook = this.$store.state.noteBooks.filter((n) => n.id == 11)[0]
                        this.initTagNotesListByTagNode(data)
                        break;
                    case 'wastePaper':
                        this.$store.state.currentNoteBook = this.$store.state.noteBooks.filter((n) => n.id == 2)[0]
                        this.initCurrentNoteListByName("wastepaperNotesList", 2);
                        break; /*'废纸篓' */
                    case 'images': /*todo 优化结构*/
                        // this.$store.state.currentNoteList =  this.$store.state.fileList
                        this.initCurrentNoteListByName("fileList", 8);
                        break; /* 文件 */
                    case 'starImages':
                        // this.$store.state.currentNoteList =  this.$store.state.fileList
                        this.initCurrentNoteListByName("fileList", 9);
                        break; /* 收藏的图片 */
                    /*case 'tagImages':
                        console.log('tagImages ...')
                        break; /!* 收藏的图片 *!/*/
                    case 'recycleBin':
                        // this.$store.state.currentNoteList =  this.$store.state.fileList
                        this.initCurrentNoteListByName("fileList", 10);
                        break; /* 图片回收站 */
                    case 'map':
                        this.toPage('map')
                        break; /* 地图模式 */
                    /*人物*/
                    case 'person': //人物 id=14
                        this.toPage('person')
                        //点击人物时重新请求数据
                        this.$store.state.currentNoteBook = this.$store.state.noteBooks.filter((n) => n.id == 14)[0]
                        /*人脸数据*/
                        this.https.getPersons().then(({data}) => {
                            this.$store.state.persons = data.data
                        })
                        break;
                    default:
                        /*区分是 点击的是笔记本 还是 标签*/
                        let firstLevelTitle = this.getfirstLevelTitle(node)
                        if (firstLevelTitle == '笔记本') {
                            this.toPage('notepage')
                            this.initCurrentNoteListByName("noteBookNameId", data.id);
                        } else if (firstLevelTitle == '标签') {
                            this.toPage('notepage')
                            this.initTagNotesListByTagNode(data)
                        } else { // 图片标签
                            this.toPage('imageList')
                            // this.$store.state.currentImageUrlList = [1, 2, 3]
                            this.initImageListByTagNode(data)
                        }
                }
            },
            /*初始化笔记列表和笔记本*/
            initCurrentNoteListByName(currentNoteBookName, noteBookId) {
                //笔记本
                if (currentNoteBookName == "noteBookNameId") {
                    // console.log('noteBookId is ', noteBookId);
                    /*获取所有子笔记*/
                    if(noteBookId.indexOf('_notebook') == -1)noteBookId = noteBookId+ '_notebook'
                    let parentIds = this.getChildrenIds(noteBookId, this.$store.state.noteBooksTree)
                    this.$store.state.currentNoteBookNoteList = this.$store.state.notes.filter((n) => parentIds.includes(n.pid + '_notebook'))
                    this.$store.state.currentNoteBook = this.$store.state.noteBooks.filter((n) => (n.id + '_notebook') == noteBookId)[0]
                    this.$store.state.currentNoteList = this.$store.state.currentNoteBookNoteList
                } else { //其他一级标题  如 wastepaperNotesList
                    if (noteBookId == 8 || noteBookId == 9 || noteBookId == 10) {
                        this.toPage('imageList')
                        let dayImages = []
                        switch (noteBookId) {
                            case 8:
                                dayImages = this.$store.state.fileList;
                                break;
                            case 9:
                                dayImages = this.$store.state.starImageList;
                                break;
                            case 10:
                                dayImages = this.$store.state.wastepaperPictureList;
                                break;
                        }
                        /*初始化笔记本*/
                        this.$store.state.currentNoteBook = this.$store.state.noteBooks.filter((n) => n.id == noteBookId)[0]
                        dayImages = this.tool.groupImages("day", dayImages)
                        this.$store.state.currentImageList = dayImages
                        /*初始化预览 给占个位 不然第一次点击时不出现大图*/
                        // this.$store.state.currentImageUrlList = [1, 2, 3]
                    } else {
                        this.toPage('notepage')
                        this.$store.state.currentNoteBookNoteList = this.$store.state[currentNoteBookName]
                        this.$store.state.currentNoteList = this.$store.state[currentNoteBookName]
                    }
                }
                if (this.$store.state.currentNoteList.length > 0) {
                    this.$store.state.currentNote = this.$store.state.currentNoteList[0]
                    this.$store.state.currentIndex = 0
                }
            },
            /*初始化 图片列表 */
            initImageListByTagNode(tagNodeData) {
                /*视图控制*/
                let dayImages = this.tool.groupImages("day", this.getImageListByTagNode(tagNodeData))
                this.$store.state.currentImageList = dayImages

                /*3.初始化 currentNote currentNoteBook*/
                if (this.$store.state.currentImageList.length > 0) {
                    this.$store.state.currentImage = this.$store.state.currentImageList[0].images[0]
                    this.$store.state.currentIndex = 0
                }
                /*初始化笔记本 便于展开tree*/
                this.$store.state.currentNoteBook = this.$store.state.noteBooks.filter((n) => n.id == 12)[0]
            },

            initTagNotesListByTagNode(tagNodeData) {
                /*3.初始化 currentNoteList */
                this.$store.state.currentNoteList = this.getNoteListByTagNode(tagNodeData)

                /*3.初始化 currentNote currentNoteBook*/
                if (this.$store.state.currentNoteList.length > 0) {
                    this.$store.state.currentNote = this.$store.state.currentNoteList[0]
                    this.$store.state.currentIndex = 0
                }
            },


            /*以列表的方式对 笔记本 标签 图片标签进行管理*/
            manage() {
                this.showContextmenu = false //隐藏菜单
                this.$router.push({name: 'noteBook_tag'})
                if (this.currentNode.data.id == 'noteBooks') {
                    this.$store.state.tableData = this.$store.state.noteBooksTreePure
                } else if (this.currentNode.data.id == 'allTags') {
                    this.$store.state.tableData = this.$store.state.tagsTreePure
                } else if (this.currentNode.data.id == 'tagImages') {
                    console.log('on the way')
                }

            },
            /*删除节点*/
            deleteNode() {
                /*1.页面变化*/
                /*获取当前被选中节点的 data，若没有节点被选中则返回 null*/
                this.$refs.mytree.remove(this.currentNode.data)
                this.showContextmenu = false
                /*2.修改数据库 返回imagetree 更新tree*/
                let deleteTargetId = this.currentNode.data.id.split('_')[0]
                /*删除笔记标签*/
                if (this.currentFirstLevelNodeId == 11) {
                    this.$store.state.expandedKeyId = 'allTags'
                    this.https.deleteTag({id: deleteTargetId}).then(({data}) => {
                        /* 将标签数据 封装上笔记的数量*/
                        this.$store.state.tagsTree = data.data[0];
                        this.$store.state.tagsTreePure = JSON.parse(JSON.stringify(data.data[0]));
                        this.$store.state.tableData = this.$store.state.tagsTreePure
                        this.$store.state.tags = data.data[1];
                        this.tool.addNoteCount(this.$store.state.tagsTree, 'noteTag')
                    })
                } else if (this.currentFirstLevelNodeId == 12) {
                    this.$store.state.expandedKeyId = 'tagImages'
                    this.https.deleteImageTag({id: deleteTargetId}).then(({data}) => {
                        /* 将标签数据 封装上笔记的数量*/
                        this.$store.state.imageTagsTree = data.data[0];
                        // this.$store.state.imageTags = JSON.parse(JSON.stringify(data.data[0]));
                        // this.$store.state.tableData = this.$store.state.tagsTreePure
                        this.$store.state.imageTags = data.data[1];
                        this.tool.addNoteCount(this.$store.state.imageTagsTree, 'imageTag')
                    })
                } else if (this.currentFirstLevelNodeId == 13) {
                    this.$store.state.expandedKeyId = 'noteBooks'
                    /*2.删除笔记本 将包含该 id的所有笔记都移动到废纸篓 同时删除该id对应的笔记本 */
                    this.https.deleteNotebook({id: deleteTargetId}).then(({data}) => {
                        this.$store.state.noteBooks = data.data[0]
                        /*默认初始化选择所有笔记*/
                        this.$store.state.currentNoteBook = data.data[0][0]
                        /*1.更新 $store.state.tableData*/
                        this.$store.state.noteBooksTreePure = JSON.parse(JSON.stringify(data.data[1]))
                        this.$store.state.tableData = this.$store.state.noteBooksTreePure

                        this.tool.addNoteCount(data.data[1], 'notebook')
                        this.$store.state.noteBooksTree = data.data[1]

                        /*2.更新废纸篓*/
                        this.https.getWastepaperNotes().then(({data}) => {
                            this.$store.state.wastepaperNotesList = data.data
                        })
                        console.log('删除笔记本成功',)
                    })
                }
                this.$message({type: 'success', message: '成功!', duration: 1000,});

            },
            /*对节点重命名*/
            reNameNode() {
                this.showContextmenu = false //隐藏菜单
                this.noteCountTemp = this.currentNode.data.title.split(" ")[1]
                this.currentNode.data.title = this.currentNode.data.title.split(" ")[0]
                this.currentNode.data.isEdit = true
            },

            /*添加子节点*/
            addChildNode() {
                let pid = this.currentNode.data.id.split('_')[0]
                if (['noteBooks', 'allTags', 'tagImages'].indexOf(pid) != -1) {
                    pid = 0
                }

                let firstLevelTitle = this.getfirstLevelTitle(this.currentNode)
                switch (firstLevelTitle) {
                    case  '笔记本':
                        this.https.insertNoteBook({pid: pid, title: '', noteCount: 0}).then(({data}) => {
                            let inputTag = data.data;
                            /*要进行深拷贝*/
                            this.$store.state.noteBooks.push(_.cloneDeep(inputTag));
                            this.addChildNodeFunc(inputTag)
                        })
                        break;
                    case  '标签':
                        this.https.insertTag({pid: pid, title: '', noteCount: 0}).then(({data}) => {
                            let inputTag = data.data;
                            /*要进行深拷贝*/
                            this.$store.state.tags.push(_.cloneDeep(inputTag));
                            this.addChildNodeFunc(inputTag)
                        })
                        break;
                    case  '图片标签':
                        this.https.insertImageTag({pid: pid, title: '', noteCount: 0}).then(({data}) => {
                            let inputTag = data.data;
                            /*要进行深拷贝*/
                            this.$store.state.imageTags.push(_.cloneDeep(inputTag));
                            this.addChildNodeFunc(inputTag)
                        })
                        break;
                }
            },
            addChildNodeFunc(inputTag) {
                this.showContextmenu = false
                this.$refs.mytree.append(inputTag, this.currentNode)
                this.showContextmenu = false
                this.noteCountTemp = '(0)'
                inputTag.isEdit = true
                this.currentNode = {data: inputTag}
            },
            // 当鼠标离开排序区(图标区 + 排序面板区 )后 点击任意位置 排序框消失
            mouseDown() {
                if (this.menuMouseLeave) {
                    this.showContextmenu = false
                    document.removeEventListener('mousedown', this.mouseDown)
                    // console.log('remove mouseDown EventListener ')
                }
            },

            /*节点失去焦点时  修改节点名称*/
            NodeBlur(node, data) {
                console.log(data.title, ' 节点失去焦点')
                if (data.isEdit) {
                    let title = data.title
                    let currentId = data.id.split('_')[0]
                    data.title = title + " " + this.noteCountTemp
                    data.isEdit = false
                    let target
                    /*区分是 点击的是笔记本 还是 标签*/
                    let firstLevelTitle = this.getfirstLevelTitle(node)
                    switch (firstLevelTitle) {
                        case  '笔记本':
                            /*判断标签内容是否发生了改变*/
                            target = this.$store.state.noteBooks.filter((t) => t.id == currentId)[0].title;
                            if (target != title) {
                                this.https.updateNotebook({currentId: currentId, title: title}).then(({data}) => {
                                    console.log('更新笔记本名称成功', data)
                                    /*更新noteBooks*/
                                    this.$store.state.noteBooks.filter((n) => n.id == currentId)[0].title = title
                                })
                            }
                            break;
                        case  '标签':
                            target = this.$store.state.tags.filter((t) => t.id == currentId)[0].title;
                            if (target != title) {
                                this.https.updateTag({currentId: currentId, title: title}).then(({data}) => {
                                    /*更新Tag tree*/
                                    console.log('更新标签名称成功', data)
                                    /*更新tags*/
                                    this.$store.state.tags.filter((n) => n.id == currentId)[0].title = title
                                })
                            }
                            break;
                        case  '图片标签':
                            target = this.$store.state.imageTags.filter((t) => t.id == currentId)[0].title;
                            if (target != title) {
                                this.https.updateImageTags({currentId: currentId, title: title}).then(({data}) => {
                                    /*更新Tag tree*/
                                    console.log('更新图片标签名称成功', data)
                                    /*更新tags*/
                                    this.$store.state.imageTags.filter((n) => n.id == currentId)[0].title = title
                                })
                            }
                            break;
                    }
                }
            },
            /*节点展开*/
            handleNodeExpand(data, node, e) {

                if (node.level == 1) {
                    if (node.data.id == "tagImages") {
                        this.currentFirstLevelNodeId = 12
                    } else if (node.data.id == "noteBooks") {
                        this.currentFirstLevelNodeId = 13
                    } else if (node.data.id == "allTags") {
                        this.currentFirstLevelNodeId = 11
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
                    //  选择笔this.$store.state.noteBooks.filter((n) => n.id == 12)[0]记本的情况下 需要对notes 进行追加，因为此时 currentNoteList 指向为当前笔记本
                    //    3.将新建笔记加入notes中
                    if (this.$store.state.currentNoteBook.id > 2) {
                        this.$store.state.notes.unshift(newNote)
                    }
                    //  4.更新笔记数量的显示
                    this.https.getNoteBooksTree().then(({data}) => {
                        this.$store.state.noteBooksTree = data.data
                        this.tool.addNoteCount(this.$store.state.noteBooksTree, 'notebook')
                    })
                })
            },

            /*获取拖拽节点的 {preId, currentId, nextId}*/
            getIds(draggingNode) {
                let [preId, currentId, nextId, currentIndex] = ["0", "0", "0", "0"]
                let brotherNotes = this.$refs.mytree.getNode(draggingNode.data.id).parent.data.children
                /* console.log(brotherNotes)*/
                if (brotherNotes.length == 1) {
                    currentId = brotherNotes[0].id
                    // console.log('只有一个节点：', 0, brotherNotes[0].id, 0)
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
                        // console.log('中间：', brotherNotes[currentIndex - 1].id, brotherNotes[currentIndex].id, brotherNotes[currentIndex + 1].id)
                        preId = brotherNotes[currentIndex - 1].id
                        nextId = brotherNotes[currentIndex + 1].id
                    } else if (currentIndex == 0) { //当拖到第一个时
                        // console.log('第一：', 0, brotherNotes[currentIndex].id, brotherNotes[currentIndex + 1].id)
                        nextId = brotherNotes[currentIndex + 1].id
                    } else {     //当拖到最后一个时
                        // console.log('最后：', brotherNotes[currentIndex - 1].id, brotherNotes[currentIndex].id, 0)
                        preId = brotherNotes[currentIndex - 1].id
                    }
                }
                return {preId, currentId, nextId}
            },
            /*控制菜单项的显示*/
            menuEnable() {
                return ['noteBooks', 'allTags', 'tagImages'].indexOf(this.currentNode.data.id) != -1
            },
            handleDrop(draggingNode, dropNode, dropType, ev) {
                // console.log(dropNode.data.title, dropNode.data.id)
                // 无法直接获取拖拽完成后的node父id，才用使用id获取当前节点的方式，间接获取pid，用于更新
                let currentParentNode = this.$refs.mytree.getNode(draggingNode.data.id).parent
                // console.log('tree drag end: ', draggingNode.data.title, '--> ', currentParentNode.data.title);
                let {preId, currentId, nextId} = this.getIds(draggingNode)
                currentId = currentId.split("_")[0]
                nextId = nextId.split("_")[0]
                preId = preId.split("_")[0]

                // 区分是 笔记本节点拖动 和 标签节点拖动
                let firstLevelTitle = this.getfirstLevelTitle(dropNode)
                /*在树内拖拽时才进行数据更新*/
                if (ev.clientX < this.$store.state.sortWay.navWidth) {
                    let obj = {
                        preId: eval(preId),
                        currentId: eval(currentId),
                        nextId: eval(nextId),// 当拖拽到一级节点下时  给pid 赋值为 0 与数据库保持一致
                        pid: eval(currentParentNode.level == 1 ? 0 : currentParentNode.data.id.split('_')[0]),
                        oldPid: draggingNode.data.pid
                    }
                    if (firstLevelTitle == '笔记本') {
                        console.log('noteBook...', firstLevelTitle);
                        this.https.updateNotebook(obj).then(({data}) => {
                            console.log('更新笔记本成功', data)
                            /*更新tree*/
                            /*更新列表*/
                            this.$store.state.noteBooksTreePure = JSON.parse(JSON.stringify(data.data))
                            this.$store.state.tableData = this.$store.state.noteBooksTreePure

                            this.$store.state.noteBooksTree = data.data
                            this.tool.addNoteCount(this.$store.state.noteBooksTree, 'notebook')
                        })
                    } else if (firstLevelTitle == '标签') {  // 拖拽标签 新旧两个分支 全父节点更新
                        this.https.updateTag(obj).then(({data}) => {
                            /*更新Tag tree*/
                            console.log('更新标签成功', data)
                            /*更新列表*/
                            this.$store.state.tagsTreePure = JSON.parse(JSON.stringify(data.data))
                            this.$store.state.tableData = this.$store.state.tagsTreePure

                            this.$store.state.tagsTree = data.data
                            this.tool.addNoteCount(this.$store.state.tagsTree, 'noteTag')
                        })
                    } else if (firstLevelTitle == '图片标签') {  // 拖拽图片标签 新旧两个分支 全父节点更新
                        this.https.updateImageTags(obj).then(({data}) => {
                            /*更新Tag tree*/
                            console.log('更新标签成功', data)
                            /*更新列表*/
                            /*  this.$store.state.tagsTreePure = JSON.parse(JSON.stringify(data.data))
                              this.$store.state.tableData = this.$store.state.tagsTreePure*/
                            this.$store.state.imageTagsTree = data.data
                            this.tool.addNoteCount(this.$store.state.imageTagsTree, 'imageTag')
                        })
                    }
                } else { //不在树内  手动恢复树的形状(可能出现的节点逃逸)
                    if (firstLevelTitle == '笔记本') {
                        /*更新tree*/
                        this.https.getNoteBooksTree().then(({data}) => {
                            this.$store.state.noteBooksTree = data.data
                            this.tool.addNoteCount(this.$store.state.noteBooksTree, 'notebook')
                        })
                    } else {
                        /*更新Tag tree*/
                        this.https.getTagsTree().then(({data}) => {
                            this.$store.state.tagsTree = data.data
                            this.tool.addNoteCount(this.$store.state.tagsTree, 'imageTag')
                        })

                    }
                }
                /*手动展开当前节点*/
                this.$store.state.expandedKeyId = dropNode.data.id
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
                /*    console.log(draggingNodeAncestors == dropNodeAncestors ? 'yes': 'no')*/
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
                if (treeNode.children && treeNode.children.length > 0) {
                    treeNode.children.forEach((n) => {
                        this.getChildrenNotes(n, res)
                    })
                }
                res.push(treeNode.id)

            },

            getTagChildrenIds(tagIds, data) {
                tagIds.push(data.id)
                if (data.children && data.children.length > 0) {
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
                            return tagIds.indexOf(v.id + '_noteTag') != -1
                        })
                        return resIds.length > 0
                    }
                    return false
                })
            },
            getImageListByTagNode(tagNodeData) {
                /*1.查找当前标签的所有子标签*/
                let tagIds = []
                this.getTagChildrenIds(tagIds, tagNodeData)

                /*2.找到 包含tagIds 的所有笔记*/
                let res = this.$store.state.fileList.filter((n) => {
                    /*判断两者的tagList是否有交集*/
                    if (n.tagList.length > 0) {
                        let resIds = n.tagList.filter((v) => {
                                return tagIds.indexOf(v.id + '_imageTag') != -1
                            }
                        )
                        return resIds.length > 0
                    }
                    return false
                })
                return res
            },

            getTagNodeDataById(tagId, tagTreeData) {
                for (let t of tagTreeData) {
                    if (t.id == tagId) return t
                    if (t.children.length > 0) return this.getTagNodeDataById(tagId, t.children)
                }
            },
            getNodeCountByTagId(tagId, tagTree) {
                let currentTagNode = this.getTagNodeDataById(tagId, tagTree)
                return this.getNoteListByTagNode(currentTagNode).length
            },
            turnOffSearchMode() {
                this.$store.state.isSearchMode = false
                this.$store.state.isTitleEditMode = true
                this.$store.state.isContentEditMode = true
            },
            /*导航栏宽度可拖拽组件*/
            widthChange(movement) {
                this.navWidth -= movement
                if (this.navWidth < this.navMin || this.navWidth > this.navMax) {
                    this.navWidth = this.navWidth > this.navMin ? this.navMax : this.navMin
                }
                /*将导航栏的宽度写进数据库*/

                if (this.lastTime == 0) {
                    this.exeWidthChange(this.navWidth)
                } else {
                    clearTimeout(this.lastTime)
                    this.exeWidthChange(this.navWidth)
                }
            },
            exeWidthChange(navWidth) {
                this.lastTime = setTimeout(() => {
                    this.https.updateSortWay({id: 1, navWidth: navWidth}).then(({data}) => {
                        console.log(data)
                    })
                }, 2000)
            },
            toPage(routeName) {
                /* if(routeName == 'imageList'){
                     this.$router.history.current.name != 'imageList' ? this.$router.push({name:'imageList'}) : ''
                 }else  if(routeName == 'notepage'){
                     this.$router.history.current.name != 'notepage' ? this.$router.push({name:'notepage'}) : ''
                 }*/
                this.$router.history.current.name != routeName ? this.$router.push({name: routeName}) : ''
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
                            id: 'calendar',
                            title: '日历'
                        },
                        /*地图*/
                        {
                            id: 'map',
                            title: '地图 (' + (this.$store.state.notes.length +  this.$store.state.fileList.length) + ')'
                        },
                        /*人物*/
                        {
                            id: 'person',
                            title: '人物 ('+this.$store.state.persons.length+')'
                        },
                        {
                            id: 'allNotes',
                            title: '全部笔记 (' + this.$store.state.notes.length +')'
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
                            children: this.$store.state.tagsTree
                        },
                        /*废纸篓*/
                        {
                            id: 'wastePaper',
                            title: '废纸篓 (' + this.$store.state.wastepaperNotesList.length + ')'
                        },
                        /*图片和视频*/
                        {
                            id: 'images',
                            title: '图片和视频 (' + this.$store.state.fileList.length + ')'
                        },
                        /*图片和视频*/
                        {
                            id: 'starImages',
                            title: '我的收藏 (' + this.$store.state.starImageList.length + ')'
                        },
                        /*图片标签*/
                        {
                            id: 'tagImages',
                            title: '图片标签',
                            children: this.$store.state.imageTagsTree
                        },
                        /*图片和视频*/
                        {
                            id: 'recycleBin',
                            title: '回收站 (' + this.$store.state.wastepaperPictureList.length + ')'
                        },

                    ]
                },
                set: function (newValue) {

                }
            }
        },
        created() {

        },
        mounted() {
            this.$bus.$on('initCurrentNoteListByName', this.initCurrentNoteListByName)
            this.$bus.$on('initTagNotesListByTagNode', this.initTagNotesListByTagNode)
            this.$bus.$on('getTagNodeDataById', this.getTagNodeDataById)
        },
        //注册一个局部的自定义指令 v-focus
        directives: {
            focus: {
                inserted: function (el) {
                    el.focus();
                }
            }
        }
    };
</script>

<style>

    /*右键菜单样式*/
    .el-menu-item, .el-submenu__title {
        height: 40px !important;
        line-height: 40px !important;
    }

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

    .editNoteBookName .el-input__inner {
        background-color: #2a333c;
        color: #ffffff;
    }

</style>