<template>
    <div>
        <!--  default-expand-all -->
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
            rowDblclick(row, column, event) {
                /*处于编辑模式时双击不跳转*/
                if (!this.titleOnEdit) {
                    this.$store.state.listAndNoteShow = true
                    if (this.$store.state.tableData == this.$store.state.noteBooksTreePure) {
                        this.$bus.$emit('clickNoteBook', "noteBookNameId", row.id)
                    } else {
                        let tagNodeData = this.getTagNodeDataById(row.id, this.$store.state.tagsTree)
                        this.$bus.$emit('initTagNotesListByTagNode', tagNodeData)
                    }
                }
            },
            cellClick(row, column, cell, event) {
                console.log(row.id, column.property)
                this.editId = row.id
                this.titleOnEdit = column.property == 'title'
                setTimeout(() => {
                    document.getElementById(row.id + 'title').focus();
                }, 100)
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
