<template>
    <el-container>
        <!-- -->
        <el-aside :style="{width: noteListWidth + 'px'}">

            <!--当前笔记本 笔记排序按钮-->
            <el-row>
                <el-col :span="16" style="text-align: center"> 当前笔记本</el-col>
                <el-col :span="8" style="text-align: right;">
                    <div class="sortButton" >
                        <el-button round
                                   @click="sortClick(-1)"
                                   @mouseleave.native="iconMouseLeave = true"
                                   @mouseenter.native="iconMouseLeave = false">
                            <i class="el-icon-sort" ></i>
                        </el-button>
                    </div>
                </el-col>
            </el-row>

            <!--笔记排序 级联面板-->
            <cascader :isSortShow="isSortShow"
                      @sortClick="sortClick"
                      @mouseleave.native="sortPanelMouseLeave = true"
                      @mouseenter.native="sortPanelMouseLeave = false"
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
                        <div v-for="(note,index) in this.$store.state.currentNoteList">
                            <!-- type="flex" 为了让图片居中-->
                            <el-row style="border-bottom: solid;padding-left: 5px" type="flex">
                                <!--标题  标签  内容-->
                                <el-col :span="16">
                                    <el-row>
                                        <div class='titleInList'>
                                             标题 - {{note.title}}
                                        </div>
                                    </el-row>
                                    <el-row>
                                        <div class="contentInList">
                                            <span style="color: #49a2de">标签 - {{note.tagList}}</span>
                                            内容- {{note.content}}Lorem ipsum dolor sit amet consectetur adipisicing elit. Consequatur, minus fugit in perspiciatis内容sssssss
                                        </div>
                                    </el-row>
                                    <el-row>
                                        <!--根据排序方式来决定显示的时间类型 note.createTime -->
                                        <span style="font-size: small;color: #49a2de">{{note.updateTime}}</span>
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
                iconMouseLeave: false,  // 鼠标是否离开了图标区域
                sortPanelMouseLeave: true,// 鼠标是否离开了排序面板区域
            }
        },
        methods: {
            // 当鼠标离开排序区(图标区 + 排序面板区 )后 点击任意位置 排序框消失
            mouseDown(){
                if(this.iconMouseLeave && this.sortPanelMouseLeave){
                    console.log('离开')
                    this.sortClick(-1)
                }else {
                    console.log('进入')
                }
            },

            sortClick(sortType) {
                this.isSortShow = !this.isSortShow
                console.log('sortType is ', sortType,this.isSortShow)  // sortType 为 -1 时 进行关闭操作
                // 排序面板出现就开始监控鼠标按下时间
                if(this.isSortShow){
                   document.addEventListener('mousedown', this.mouseDown)
               }else {
                    document.removeEventListener('mousedown', this.mouseDown)
                }
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