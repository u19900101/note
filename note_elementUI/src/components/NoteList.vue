<template>
    <el-container>
        <!-- -->
        <el-aside :style="{width: noteListWidth + 'px'}">

            <!--当前笔记本 笔记排序按钮-->
            <el-row>
                <el-col :span="16" style="text-align: center"> 当前笔记本</el-col>
                <el-col :span="8" style="text-align: right;">
                    <div class="sortButton">
                        <el-button round>
                            <i class="el-icon-sort" @click="sortClick(-1)"></i>
                        </el-button>
                    </div>
                </el-col>

            </el-row>

            <!--笔记排序 级联面板-->
            <cascader :isSortShow="isSortShow"
                      @sortClick="sortClick"
                      :style="{'margin-left': (noteListWidth-30) + 'px'}">
            </cascader>

            <!--搜索入口-->
            <el-row class="search">
                <el-input
                        placeholder="搜索"
                        v-model="searchValue"
                        prefix-icon="el-icon-search"
                        clearable>
                </el-input>
            </el-row>

            <!--笔记列表-->
            <el-container>
                <el-aside style="height: 500px;" :style="{width: noteListWidth + 'px'}">
                    <el-scrollbar class="page-scroll">
                        <div v-for="(v,index) in new Array(15)">
                            <!-- type="flex" 为了让图片居中-->
                            <el-row style="border-bottom: solid;padding-left: 5px" type="flex">
                                <!--标题  标签  内容-->
                                <el-col :span="16">
                                    <el-row>
                                        <div class='titleInList'>
                                             标题 - {{index}}
                                        </div>
                                    </el-row>
                                    <el-row>
                                        <div class="contentInList">
                                            <span style="color: #49a2de">标签 - {{index}}</span>
                                            内容- {{index}}Lorem ipsum dolor sit amet consectetur adipisicing elit. Consequatur, minus fugit in perspiciatis内容sssssss
                                        </div>
                                    </el-row>
                                    <el-row>
                                        <span style="font-size: small;color: #49a2de">日期：2021.10.31</span>
                                    </el-row>
                                </el-col>
                                <!--图片-->
                                <el-col :span="8" class="innerCenter">
                                    <el-image
                                            :src="require('../assets/images/gofree.jpg')"
                                            fit="cover">
                                    </el-image>
                                </el-col>
                            </el-row>
                        </div>
                    </el-scrollbar>

                </el-aside>
            </el-container>

        </el-aside>
        <!--拉动线-->
        <el-aside width="4px">
            <borderLine @widthChange="noteListWidthChange"/>
        </el-aside>
    </el-container>

</template>

<script>
    import borderLine from "./BorderLine"
    import noteSort from "./NoteSort"

    import cascader from "./Cascader"

    export default {
        name: "noteList",
        components: {
            borderLine,
            noteSort,
            cascader
        },
        data() {
            return {
                fits: ['fill', 'contain', 'cover', 'none', 'scale-down'],
                noteListWidth: 240, // 笔记列表的初始宽度
                navMax: 240,
                navMin: 150,
                searchValue: '',
                isSortShow: false,
                value: 'zhinan',
                isMouseleaveSortArea: false,  // 鼠标是否离开了排序区域
                options: [
                    {
                        value: 'zhinan',
                        label: '指南ssssssssss',
                        children: [
                            {
                                value: 'zhinan',
                                label: '指南ssssssssss',
                            }
                        ]
                    }, {
                        value: 'zujian',
                        label: '组件sssssssssssss',
                    }, {
                        value: 'ziyuan',
                        label: '资源ssssssssssssssss',
                    }]
            }
        },
        methods: {

            sortClick(sortType) {
                this.isSortShow = !this.isSortShow
                console.log('sortType is ', sortType)  // sortType 为 -1 时 进行关闭操作
            },

            handleChange(value) {
                console.log(value);
            },
            noteListWidthChange(movement) {
                this.noteListWidth -= movement
                if (this.noteListWidth < this.navMin || this.noteListWidth > this.navMax) {
                    this.noteListWidth = this.noteListWidth > this.navMin ? this.navMax : this.navMin
                }
            },
        }
    }
</script>

<style>
    /*笔记列表中图片 居中 */
    .innerCenter{
        /*background: aqua;*/
        display:flex;
        justify-content:center;/*主轴上居中*/
        align-items:center;/*侧轴上居中*/
    }

    /*笔记列表中的内容样式  多行截断*/
    .contentInList {
        font-size: small;
        position: relative;
      /*  line-height: 20px;*/
        height: 35px;
        overflow: hidden;
    }
    .contentInList::after {
        content: "...";
        font-size: small;
        background: #ffffff;  /*加背景覆盖源文本*/
        position: absolute;
        bottom: 5px;
        right: 5px;
    }

    /*笔记列表中的标题样式*/
    .titleInList {
        /*width: 300px;*/
        overflow: hidden;
        /*文本不会换行*/
        white-space: nowrap;
        /*当文本溢出包含元素时，以省略号表示超出的文本*/
        text-overflow: ellipsis;
    }

    /* 美化列表的滚动条样式*/
    .page-scroll {
        height: 100%;
        width: 100%;
    }
    .page-scroll .el-scrollbar__wrap {
        overflow-x: hidden;
    }


    /*给排序图标按钮指定大小*/
    .sortButton .el-button.is-round {
        border-radius: 55px;
        padding: 2px 7px;
    }

    /*搜索图标的大小*/
    .search .el-input__inner {
        height: 22px !important;
    }

    .search .el-input__icon {
        line-height: 22px;
    }


    /*搜索图标字体*/
    .el-input__icon {
        line-height: 20px;
    }

    /*控制搜索框的间隔*/
    .el-row {
        margin-bottom: 0px !important;
    }

    .el-aside {
        background: #ffffff !important;
    }

    .el-container {
        display: flex;
        flex-direction: row;
        flex: 0;
        flex-basis: auto;
        box-sizing: border-box;
        min-width: 0;

    }

    .el-main {
        display: block;
        flex: 1;
        flex-basis: auto;
        overflow: auto;
        box-sizing: border-box;
        padding: 0px;
    }
</style>