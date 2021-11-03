<template>
    <div>
        <el-tag
                :key="tag"
                v-for="tag in dynamicTags"
                closable
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
        <el-button v-else class="button-new-tag" size="small" @click="showInput">+ New Tag</el-button>
    </div>
</template>

<script>

    export default {
        name: "NoteTag",
        data() {
            return {
                dynamicTags: ['标签一', '标签二', '标签三'],
                inputVisible: false,
                inputValue: '',
                restaurants: [],
                state1: '',
            };
        },
        methods: {
            querySearch(queryString, cb) {
                var restaurants = this.restaurants;
                var results = queryString ? restaurants.filter(this.createFilter(queryString)) : restaurants;
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
            handleClose(tag) {
                this.dynamicTags.splice(this.dynamicTags.indexOf(tag), 1);
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