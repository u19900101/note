<template>
    <el-container class="nav">
        <!--点击逻辑
    点击上级菜单时 1.显示对应菜单的笔记(标签) 2.不展开子项目
    点击 三角符号时 收起或折叠子项-->
        <el-aside :style="{width: navWidth + 'px'}">
            <el-tree :data="data"
                     ref="mytree"
                    node-key="id"
                    accordion
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

    export default {
        name: "tree",
        components: {
            borderLine,
        },
        data() {

            return {
                navWidth: 150, // 导航栏的初始宽度
                noteListWidth: 240, // 笔记列表的初始宽度
                navMax: 240,
                navMin: 150,
                //收藏-1 笔记-2 笔记本-3 标签-4  废纸篓-5  新建笔记-6
                firstLevelIds: [1, 2, 3, 4, 5, 6],  //一级菜单id，不允许拖动
                icons: ['el-icon-star-on', 'el-icon-document', 'el-icon-notebook-2', 'el-icon-discount', 'el-icon-delete','el-icon-circle-plus-outline'],
                data: [
                    {
                        id: 6,
                        title: '新建笔记'
                    },
                    {
                        id: 1,
                        title: '收藏'
                    }, {
                        id: 2,
                        title: '全部笔记'
                    },
                    /*笔记本*/
                    {
                        id: 3,
                        title: '笔记本',
                        children: this.getNotebooks()
                    },
                    /*标签*/
                    {
                        id: 4,
                        title: '标签',
                        children: [{
                            id: 41,
                            title: '二级 3-1'
                        }, {
                            id: 42,
                            title: '二级 3-2',
                            children: [{
                                id: 43,
                                title: '三级 3-2-1'
                            }, {
                                id: 44,
                                title: '三级 3-2-2'
                            }, {
                                id: 45,
                                title: '三级 3-2-3'
                            }]
                        }]
                    },
                    /*废纸篓*/
                    {
                        id: 5,
                        title: '废纸篓'
                    },

                ],

                defaultProps: {
                    children: 'children',
                    label: 'title'
                }
            };
        },
        methods: {
            /* 构造树形data*/
            getNotebooks(){
                let noteBooks = this.$store.state.noteBooks
                // console.log('构造树形data',noteBooks)
                return noteBooks
            },
            widthChange(movement) {
                this.navWidth -= movement
                if (this.navWidth < this.navMin || this.navWidth > this.navMax) {
                    this.navWidth = this.navWidth > this.navMin ? this.navMax : this.navMin
                }
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
                console.log('clicked Node', data.title);
            },
            handleDragStart(node, ev) {
                console.log('drag start', node);
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
            handleDragEnd(draggingNode, dropNode, dropType, ev) {
                // 无法直接获取拖拽完成后的node父id，才用使用id获取当前节点的方式，间接获取pid，用于更新
                let currentParentNode = this.$refs.mytree.getNode(draggingNode.data.id).parent.data
                console.log('tree drag end: ', draggingNode.data.title,'--> ',currentParentNode.title);
                if(currentParentNode.id == 3){
                //    变化到数据库中的id为 0
                    currentParentNode.id  = 0
                }
                this.https.updateNotebook({id: draggingNode.data.id, pid: currentParentNode.id}).then(({data}) => {
                    console.log('更新笔记本成功', data)
                })
            },
            handleDrop(draggingNode, dropNode, dropType, ev) { // dropType -- inner

                // console.log('tree drop: ', draggingNode.data.id ,'--> ',dropNode.data.id);
                // console.log('tree drop: ', draggingNode.data.title ,'--> ',dropNode.data.title);


            },
            allowDrop(draggingNode, dropNode, type) {
                // 找到 level = 1，比较两者的 一级节点id是否相等
                let draggingNodeAncestors = this.getfirstLevelId(draggingNode)
                let dropNodeAncestors = this.getfirstLevelId(dropNode)
                // 只有两者 所属同一 一级菜单时才能放下
                return draggingNodeAncestors == dropNodeAncestors
            },
            allowDrag(draggingNode) {
                // 每次拖拽之前进行判断
                // 禁止对 一级的四个节点进行拖动
                return !this.firstLevelIds.includes(draggingNode.data.id);
            },
            getfirstLevelId(node) {
                if (node.level != 1) {
                    this.getfirstLevelId(node.parent)
                } else {
                    return node.id
                }
            }
        },
        created() {
            this.getNotebooks()
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

    /* 节点选中时的背景色*/
    .el-tree-node:focus > .el-tree-node__content {
        background-color: #4a5d6b !important;
    }

    /* 鼠标悬浮时的背景色*/
    .el-tree-node__content:hover {
        background-color: #354553 !important;
    }

    .el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content {
        background-color: rgba(16, 15, 14, 0.53) !important;
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