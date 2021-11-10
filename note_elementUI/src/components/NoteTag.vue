<template>
    <div>
        <el-tag
                :key="tag"
                v-for="tag in dynamicTags"
                :closable = "$store.state.currentNote.wastepaper ? false : true"
                :disable-transitions="false"
                @close="handleClose(tag)">
            {{tag}}
        </el-tag>
        <!--显示可输入的框-->
        <el-autocomplete
                size="small"
                v-if="inputVisible"
                ref="saveTagInput"
                class="input-new-tag"
                v-model="inputValue"
                :fetch-suggestions="querySearch"
                placeholder="请输入内容"
                @blur="inputBlur"
                @select="handleSelect"
                @keyup.enter.native="handleInputConfirm"
        ></el-autocomplete>

        <!--新增标签按钮-->
        <el-button v-else :disabled="$store.state.currentNote.wastepaper" class="button-new-tag" size="small" @click="showInput">+ New Tag</el-button>
    </div>
</template>

<script>

    export default {
        name: "NoteTag",
        data() {
            return {
                inputVisible: false,
                inputValue: '',
                restaurants: [],
                state1: '',
            };
        },
        computed:{
            dynamicTags(){
                let dynamicTags = []
                this.$store.state.currentNote.tagList.forEach((tag)=>dynamicTags.push(tag.title))
                return dynamicTags
            },
        },
        methods: {
            /*移除笔记中的某一标签*/
            handleClose(tagName) {
                /*修改页面*/
                let  tagId = this.$store.state.currentNote.tagList.filter((n) => n.title == tagName)[0].id

                this.$store.state.currentNote.tagList = this.$store.state.currentNote.tagList.filter((n) => n.title != tagName)
                /*更新数据库*/
                /*将封装tagList为idStr*/
                let tagUid = ""
                if(this.$store.state.currentNote.tagList.length > 0){
                    this.$store.state.currentNote.tagList.forEach((n) => tagUid += n.id + ',')
                }

                /*更新笔记*/
                this.https.updateNote({id: this.$store.state.currentNote.id, tagUid: tagUid}).then(({data}) => {
                    console.log("移除笔记中的某一标签 ", data);
                    /*删除笔记中某一标签*/
                    /*oldPid = 0 使其中一个不更新*/
                    this.https.updateTag({pid: tagId,
                        oldPid: 0}).then(({data}) => {
                        /*更新Tag tree*/
                        this.$store.state.tags = data.data
                        this.tool.addNoteCount(this.$store.state.tags)
                    })
                })
            },

            querySearch(queryString, cb) {
                let restaurants = this.restaurants;
                let results = queryString ? restaurants.filter(this.createFilter(queryString)) : restaurants;
                // 调用 callback 返回建议列表的数据
                cb(results);
            },
            createFilter(queryString) {
                return (restaurant) => {
                    return (restaurant.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0);
                };
            },
            loadAll() {
                return [
                    { "value": "三全鲜食（北新泾店）", "address": "长宁区新渔路144号" },
                    { "value": "Hot honey 首尔炸鸡（仙霞路）", "address": "上海市长宁区淞虹路661号" },
                ];
            },

            handleSelect(item) {
                this.inputValue = item.value
                this.handleInputConfirm()
            },

            showInput() {
                this.inputVisible = true;
                this.$nextTick(_ => {
                    this.$refs.saveTagInput.$refs.input.focus();
                });
            },

            addTag(inputValue){
                console.log('addTag...')
                this.dynamicTags.push(inputValue);
            },
            handleInputConfirm() {
                console.log('handleInputConfirm....')
                let inputValue = this.inputValue;
                if (inputValue) {
                    this.dynamicTags.push(inputValue);
                }
                this.inputVisible = false;
                this.inputValue = '';
            },
            /* 手动添加失去焦点事件  因为 blur事件的优先级高于 @select，前者会使后者失效*/
            inputBlur(){
                setTimeout(()=>{
                    console.log('失去焦点...')
                    if(this.inputValue) {
                        this.handleInputConfirm()
                    } else {
                        this.inputVisible = false;
                    }
                },500)
            }
        },

        mounted() {
            this.restaurants = this.loadAll();
        }
    }
</script>

<style>
    .el-tag + .el-tag {
        margin-left: 10px;
    }
    .button-new-tag {
        width: 90px;
        margin-left: 10px;
        height: 32px;
        line-height: 30px;
        padding-top: 0;
        padding-bottom: 0;
    }
    .input-new-tag {
      /*  width: 90px !important;*/ /* 控制宽度 避免被标题栏的宽度覆盖*/
        margin-left: 10px;
        vertical-align: bottom;
    }
</style>