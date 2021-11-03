<template>
    <el-container>

        <el-header>
            <!--标题-->
            <el-row>
                <el-input
                        size="small"
                        placeholder="笔记标题"
                        v-model="title">
                </el-input>
            </el-row>
            <!--笔记本 标签 收藏 删除-->
            <el-row class="toolLeft"> <!--toolLeft 让笔记图标在div中居中-->
                <!--笔记本和图标放一起-->
                <div class="toolLeft">
                    <i class="el-icon-notebook-2"></i>
                    <!--让下拉框随着内容的变化动态的改变宽带 字体的大小是 12 英文和中文的宽度不一样 粗略的进行计算 -->
                    <el-select v-model="value" filterable placeholder="请选择"
                               size="small"
                               :style="{width: value.label.length*12 + 50+ 'px'}">
                        <el-option
                                v-for="item in options"
                                :key="item.value"
                                :label="item.label"
                                :value="{ value: item.value, label: item.label }">
                        </el-option>
                    </el-select>
                </div>

                <!--收藏-->
                <el-tooltip class="item"
                            :content="star ? '取消收藏': '收藏'"
                            placement="bottom"
                            :hide-after=800>
                    <!--给图标单独设置大小 采用 i 设置时会影响全局-->
                    <i @click="starClick" :class="star ? 'el-icon-star-on': 'el-icon-star-off' "
                       style="margin-left: 10px;font-size: 25px"></i>
                </el-tooltip>


                <el-tooltip class="item"
                            content="删除笔记"
                            placement="bottom"
                            :hide-after=800>
                    <i class="el-icon-delete" @click="deleteClick" style="margin-left: 10px;font-size: 25px"></i>

                </el-tooltip>

                <!--标签-->
                <noteTag style="margin-left: 10px" ref="noteTag"></noteTag>

            </el-row>
        </el-header>

        <!--内容-->
        <el-main>
            <div @drop="handleTargetDrop"
                 @dragover="handleTargetDragOver"
                 style="height: 100%">
                <el-input type="textarea"
                          maxlength="1000000"
                          show-word-limit
                          v-model='content'
                > 笔记内容
                </el-input>
            </div>


        </el-main>
    </el-container>
</template>

<script>
    import noteTag from "./NoteTag"

    export default {
        name: "Note",
        components: {
            noteTag,
        },
        data() {
            return {
                title: '',
                content: '',
                star: false, //当前日记是否已收藏
                options: [
                    {
                        value: '1',
                        label: '我的抗战ing'
                    }, {
                        value: '2',
                        label: '我的抗战2'
                    }, {
                        value: '3',
                        label: '蚵仔煎'
                    },],
                value: {
                    value: '1',
                    label: '我的抗战ing'
                },
            }
        },
        methods: {
            handleTargetDragOver(e) {
                let firstLevelId = this.tool.getfirstLevelId(this.$store.state.currentNode)
                // 判断节点是否为标签子的节点
                if (firstLevelId == 4) {
                    e.preventDefault(); // 使该区域允许释放
                }

            },
            handleTargetDrop() {
                console.log('在内容区放置了当前拖拽标签')
                let {id, label} = this.$store.state.currentNode.data
                // 触发 noteTag中的添加标签 事件
                console.log(this.$store.state.currentNode)
                this.$refs.noteTag.addTag(label);
            },

            starClick() {
                this.star = !this.star
                this.$message({
                    message: this.star ? '已收藏' : '取消收藏',
                    type: this.star ? 'success' : 'error',
                    duration: 500,  //显示时间, 毫秒。设为 0 则不会自动关闭 默认3000
                    center: true
                });
            },
            deleteClick() {
                console.log('deleteClick...')
                this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    center: true
                }).then(() => {
                    this.$message({
                        type: 'success',
                        message: '删除成功!',
                        duration: 500,
                    });
                }).catch(() => {
                    this.$message({
                        type: 'info',
                        message: '已取消删除',
                        duration: 500,
                    });
                });
            }

        }
    }
</script>

<style>

    /*自定义提示框的宽度*/
    .el-message {
        min-width: 176px !important;
    }

    /* 让工具栏在同一行展示*/
    .toolLeft {
        display: flex;
        justify-content: flex-start; /*主轴上靠左*/
        /*justify-content: space-between !important;*/ /*space-between 无法实现开头和结尾对其 */
        align-items: center; /*侧轴上居中*/
    }


    .el-main {
        /*17*/
        padding: 80px 20px 0px 20px !important;
    }

    /*这只内容区域高度占满*/
    .el-textarea__inner,
    .el-textarea {
        height: 100% !important;
    }
</style>