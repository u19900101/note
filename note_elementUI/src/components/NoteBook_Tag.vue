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
                    sortable
                    width="180">
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
                    sortable
                    width="180">
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
                if (!this.titleOnEdit) {
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
