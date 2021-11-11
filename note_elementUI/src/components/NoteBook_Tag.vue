<template>
    <div>
        <!--  default-expand-all -->
        <el-table
                :data="$store.state.tableData"
                @row-dblclick = "rowDblclick"
                style="width: 100%;margin-bottom: 20px;"
                row-key="id"
                border
                :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
            <el-table-column
                    prop="title"
                    label="名称"
                    sortable
                    width="180">
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
    export default {
        name: "noteBook_tag",
        methods:{
            rowDblclick(row, column, event){
                this.$store.state.listAndNoteShow = true
                if( this.$store.state.tableData == this.$store.state.noteBooksTreePure){
                    this.$bus.$emit('clickNoteBook', "noteBookNameId", row.id)
                }else {
                    let tagNodeData = this.getTagNodeDataById(row.id,this.$store.state.tagsTree)
                    this.$bus.$emit('initTagNotesListByTagNode', tagNodeData)
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
