<template>
    <div>
        <el-tooltip class="item"
                    content= '新建'
                    placement="bottom">
            <el-button type="text" @click="insertItem" size="small" style="margin-left: 10px;padding: 0px">
                <i class="el-icon-circle-plus"  style="font-size: 25px"></i>
            </el-button>
        </el-tooltip>


        <el-table
                :data="$store.state.tableData"
                @row-dblclick="rowDblclick"
                @cell-click="cellClick"
                style="width: 100%;margin-bottom: 20px;"
                row-key="id"
                border
                stripe
                highlight-current-row
                :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
            <el-table-column
                    prop="title"
                    label="名称"
                    sortable>
                <template slot-scope="scope">
                    <span class="editCell" style="width:120px;"
                          v-show="!titleOnEdit || (editId != scope.row.id)">
                        {{scope.row.title}}
                    </span>
                    <el-input
                            :id="scope.row.id+'title'"
                            v-show="titleOnEdit && (editId == scope.row.id)"
                            @input="titleChanged"
                            @blur="titleOnEdit = false"
                            v-model="scope.row.title">
                    </el-input>
                </template>
            </el-table-column>
            <el-table-column
                    prop="noteCount"
                    label="笔记数量"
                    sortable>
            </el-table-column>
            <el-table-column
                    prop="createTime"
                    label="创建时间"
                    sortable>
            </el-table-column>
            <el-table-column
                    prop="updateTime"
                    label="更新日期"
                    sortable>
            </el-table-column>
            <el-table-column
                    prop="delete"
                    label="操作">
                <template slot-scope="scope">
                   <el-button type="danger" size="small" @click="deleteItem(scope.row.id)">删除</el-button>
                </template>
            </el-table-column>
        </el-table>
    </div>
</template>
<script>
    let dayjs = require('dayjs');
    export default {
        name: "noteBook_tag",
        data() {
            return {
                titleOnEdit: false,//控制显隐
                editId: -1 //当前编辑行index
            }
        },
        methods: {
            deleteItem(id){
                console.log( 'deleteItem...',id)
                this.$confirm('此操作将该笔记组移入到废纸篓是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    center: true
                }).then(() => {
                    /*2.操作数据库 将包含该 id的所有笔记都移动到废纸篓 同时删除该id对应的笔记本 */
                    if (this.$store.state.tableData == this.$store.state.noteBooksTreePure) {
                        this.https.deleteNotebook({id:id}).then(({data}) => {
                            this.https.getNotebooks().then(({data}) => {
                                this.$store.state.noteBooks = data.data[0]
                                /*默认初始化选择所有笔记*/
                                this.$store.state.currentNoteBook = data.data[0][0]
                                /*1.更新 $store.state.tableData*/
                                this.$store.state.noteBooksTreePure = JSON.parse(JSON.stringify(data.data[1]))
                                this.$store.state.tableData = this.$store.state.noteBooksTreePure

                                this.tool.addNoteCount(data.data[1])
                                this.$store.state.noteBooksTree = data.data[1]

                                /*2.更新废纸篓*/
                                this.https.getWastepaperNotes().then(({data}) => {
                                    this.$store.state.wastepaperNotesList = data.data
                                })
                            })
                        })
                    }
                    /*删除标签*/
                    else {
                        this.https.deleteTag({id:id}).then(({data}) => {
                            this.https.getTags().then(({data}) => {
                                /* 将标签数据 封装上笔记的数量*/
                                this.$store.state.tagsTree = data.data[0];
                                this.$store.state.tagsTreePure = JSON.parse(JSON.stringify(data.data[0]));
                                this.$store.state.tableData = this.$store.state.tagsTreePure
                                this.$store.state.tags = data.data[1];
                                this.tool.addNoteCount(this.$store.state.tagsTree)
                            })
                        })
                    }
                    this.$message({type: 'success', message: '成功!', duration: 1000,});
                }).catch(() => {
                    this.$message({
                        type: 'info',
                        message: '已取消',
                        duration: 1000,
                    });
                });
            },
            insertItem(){
                this.$prompt('请输入名称', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    inputPattern: /[\w]+/,
                    inputErrorMessage: '不能为空'
                }).then(({ value }) => {
                    if (this.$store.state.tableData == this.$store.state.noteBooksTreePure) {
                        this.https.insertNoteBook({title:value}).then(({data}) => {
                            /*修改页面*/
                            this.$store.state.noteBooks.push(data.data)
                            /*给当前的列表*/
                            data.data.children = []
                            this.$store.state.noteBooksTreePure.push(data.data)
                            let temp = JSON.parse(JSON.stringify(data.data))
                            temp.title += ' ('+temp.noteCount + ')'
                            /*手动封装子节点*/
                            temp.children = []
                            this.$store.state.noteBooksTree.push(temp)
                        })
                    }else {
                        this.https.insertTag({title:value}).then(({data}) => {
                            /*修改页面*/
                            this.$store.state.tags.push(data.data)
                            /*给当前的列表*/
                            data.data.children = []
                            this.$store.state.tagsTreePure.push(data.data)
                            let temp = JSON.parse(JSON.stringify(data.data))
                            temp.title += ' ('+temp.noteCount + ')'
                            /*手动封装子节点*/
                            temp.children = []
                            this.$store.state.tagsTree.push(temp)
                        })
                    }
                    this.$message({
                        type: 'success',
                        message: '新建成功'
                    });
                }).catch(() => {
                    this.$message({
                        type: 'info',
                        message: '取消'
                    });
                });
            },
            rowDblclick(row, column, event) {
                /*处于编辑模式时双击不跳转*/
                if (!this.titleOnEdit && column.property != 'delete') {
                    this.$store.state.listAndNoteShow = true
                    if (this.$store.state.tableData == this.$store.state.noteBooksTreePure) {
                        this.$bus.$emit('initCurrentNoteListByName', "noteBookNameId", row.id)
                    } else {
                        let tagNodeData = this.getTagNodeDataById(row.id, this.$store.state.tagsTree)
                        this.$bus.$emit('initTagNotesListByTagNode', tagNodeData)
                    }
                }
            },
            cellClick(row, column, cell, event) {
                this.editId = row.id
                this.titleOnEdit = column.property == 'title'
                if(this.titleOnEdit){
                    setTimeout(()=>{
                        document.getElementById(row.id + 'title').focus();
                    },100)
                }
            },
            /*名称的修改*/
            titleChanged(title) {
                if (title) {
                    /*笔记本名称修改*/
                    if (this.$store.state.tableData == this.$store.state.noteBooksTreePure) {
                        this.https.updateNotebook({currentId: this.editId, title: title}).then(({data}) => {
                            console.log('更新笔记本名称成功', data)
                            /*更新tree*/
                            let currentNoteBook = this.getTagNodeDataById(this.editId, this.$store.state.noteBooksTree)
                            currentNoteBook.title = currentNoteBook.title.replace(currentNoteBook.title.split(" ")[0], title)
                            /*更新noteBooks*/
                            this.$store.state.noteBooks.filter((n) => n.id == this.editId)[0].title = title
                        })
                    } else {/*标签名称修改*/
                        this.https.updateTag({currentId: this.editId, title: title}).then(({data}) => {
                            /*更新Tag tree*/
                            console.log('更新标签名称成功', data)
                            let currentTag = this.getTagNodeDataById(this.editId, this.$store.state.tagsTree)
                            currentTag.title = currentTag.title.replace(currentTag.title.split(" ")[0], title)
                            /*更新tags*/
                            this.$store.state.tags.filter((n) => n.id == this.editId)[0].title = title
                        })
                    }
                    /*更新时间*/
                    this.$store.state.tableData.filter((n) => n.id == this.editId)[0].updateTime = dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss')
                }

            },
            getTagNodeDataById(tagId, tagTreeData) {
                for (let t of tagTreeData) {
                    if (t.id == tagId) return t
                    if (t.children.length > 0) this.getTagNodeDataById(tagId, t.children)
                }
            },
        }
    }
</script>

<style scoped>
    .editCell:hover {
        cursor: pointer;
    }
</style>
