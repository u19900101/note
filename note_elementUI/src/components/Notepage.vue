<template>
    <div :style="{height :$store.state.clientH}">
        <el-container>
            <!--笔记列表-->
            <el-aside class="widthSyncChild" :style="{height :$store.state.clientH}">
                <noteList/>
            </el-aside>
            <!--笔记内容-->
            <note></note>
        </el-container>
    </div>
</template>

<script>
    import note from "./Note";
    import noteList from "./NoteList";
    export default {
        name: "Notepage",
        components:{
            noteList, note,
        }
    }
</script>

<style>
    .widthSyncChild {
        width: auto !important
    }


    .el-container {
        display: flex;
        flex-direction: row;
        flex: 0;
        flex-basis: auto;
        box-sizing: border-box;
        min-width: 0;
    }
</style>