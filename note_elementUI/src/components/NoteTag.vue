<template>
    <div>
        <el-tag
                :key="tag.id"
                size="small"
                v-for="tag in $store.state.currentNote.tagList"
                :closable="$store.state.currentNote.wastepaper ? false : true"
                :disable-transitions="false"
                @close="handleClose(tag)">
            {{tag.title}}
        </el-tag>
        <!--显示可输入的框-->
        <el-autocomplete
                size="mini"
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
        <el-tag
                @click="showInput"
                size="small"
                :closable="false"
                :disable-transitions="false">
            +
        </el-tag>
    </div>
</template>

<script>
    export default {
        name: "NoteTag",
        data() {
            return {
                inputVisible: false,
                inputValue: '',
                state1: '',
            };
        },
        computed: {
            /*候选标签*/
            candidateTags() {
                /*封装数据*/
                let tags = []
                let candidateTags = this.$store.state.tags.filter((t) => {
                    for (let n of this.$store.state.currentNote.tagList) {
                        if (n.id == t.id) return false
                    }
                    return true
                }) /* { "value": "xxx", "id": "xx" },*/
                candidateTags.forEach((n) => tags.push({value: n.title, id: n.id}))
                return tags
            }
        },
        methods: {
            /*移除笔记中的某一标签*/
            handleClose(tag) {
                /*修改页面*/
                this.$store.state.currentNote.tagList = this.$store.state.currentNote.tagList.filter((n) => n.title != tag.title)
                /*更新数据库*/
                /*更新笔记*/
                this.updateData(tag,'remove')
            },

            /*标签的搜索*/
            querySearch(queryString, cb) {
                let tags = this.candidateTags;
                let results = queryString ? tags.filter(this.createFilter(queryString)) : tags;
                // 调用 callback 返回建议列表的数据
                cb(results);
            },
            /*创建忽略大小写的过滤器*/
            createFilter(queryString) {
                return (h) => {
                    return (h.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0);
                };
            },

            handleSelect(item) {
                this.inputValue = item.value
                this.handleInputConfirm(item)
            },

            showInput() {
                this.inputVisible = true;
                this.$nextTick(_ => {
                    this.$refs.saveTagInput.$refs.input.focus();
                });
            },
            /*从标签栏拖拽添加标签*/
            addTag(tag) {
                /*不包含时才进行添加*/
                if(!this.$store.state.currentNote.tagUid.includes(tag.id + ",")){
                    this.handleInputConfirm({id:tag.id,value:tag.title.split(" ")[0]})
                }
            },

            /*添加标签确认提交*/
            handleInputConfirm(inputTag) {
                /*1.新建标签 按下回车键进行的新建标签*/
                if (inputTag.type == "keyup") {
                    // 判断当前标签库是否存在同名标签(区分大小写)
                    let inputValue = this.inputValue;
                    if (inputValue) {
                        inputTag = this.$store.state.tags.filter((n) => n.title == inputValue)[0]
                        if (!inputTag) {  // 1.存在，直接 加入到 当前笔记的tagList中
                            // 2.不存在 新建标签项 将返回的新标签加入
                            this.https.insertTag({title: inputValue, noteCount: 1}).then(({data}) => {
                                inputTag = data.data;
                                this.$store.state.currentNote.tagList.push(inputTag)
                                /*3.后台数据更新*/
                                this.updateData(inputTag,'add')
                            })
                        }else { //存在异步的情况
                            this.$store.state.currentNote.tagList.push(inputTag)
                        }
                    }
                } else {  /*2.添加标签已经存在的标签*/
                    /*1.更新页面*/
                    this.$store.state.currentNote.tagList.push({id: inputTag.id, title: inputTag.value})
                    this.updateData(inputTag,'add')
                }

                this.inputVisible = false;
                this.inputValue = '';
            },
            /* 手动添加失去焦点事件  因为 blur事件的优先级高于 @select，前者会使后者失效*/
            inputBlur() {
                setTimeout(() => {
                    console.log('失去焦点...')
                    if (this.inputValue) {
                        this.handleInputConfirm()
                    } else {
                        this.inputVisible = false;
                    }
                }, 500)
            },

            /*笔记标签数据更新*/
            updateData(inputTag,type) {
                /*添加标签*/
                let tagUid = this.$store.state.currentNote.tagUid + inputTag.id + ','
                /*移除标签*/
                if(type == 'remove'){
                    tagUid = this.$store.state.currentNote.tagUid.replace(inputTag.id + ",","")
                }

                this.https.updateNote({id: this.$store.state.currentNote.id, tagUid: tagUid}).then(({data}) => {
                    console.log("给笔记添加标签 ", data);
                    /*删除笔记中某一标签*/
                    /*oldPid = 0 使其中一个不更新*/
                    this.https.updateTag({
                        pid: inputTag.id,
                        oldPid: 0
                    }).then(({data}) => {
                        /*更新Tag tree*/
                        this.$store.state.tagsTree = data.data
                        this.tool.addNoteCount(this.$store.state.tagsTree,'noteTag')
                        this.$store.state.expandedKeyId = 'allTags'
                    })
                })
            }
        }
    }
</script>

<style>
    .el-tag + .el-tag {
        margin-left: 10px;
    }

    .input-new-tag {
        /*  width: 90px !important;*/ /* 控制宽度 避免被标题栏的宽度覆盖*/
        margin-left: 10px;
        vertical-align: bottom;
    }
</style>