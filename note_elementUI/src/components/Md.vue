<template>
    <div id="editormdid">
        <textarea v-model="$store.state.currentNote.content"></textarea>
    </div>
</template>

<script>
    export default {
        name: 'Editor',
        data() {
            return {
                instance: null,
                init: 1 //初始化
            };
        },
        methods: {
            initEditor() {
                let vm = this
                this.instance = window.editormd("editormdid", {
                    height: 500,
                    emoji: true,
                    path: 'editor.md/lib/',
                    editorTheme: "pastel-on-dark",
                    codeFold: true,
                    //syncScrolling : false,
                    saveHTMLToTextarea: true,    // 保存 HTML 到 Textarea
                    searchReplace: true,
                    // watch : false,            // 关闭实时预览
                    htmlDecode: true,            // 开启 HTML 标签解析，为了安全性，默认不开启
                    // htmlDecode: "style,script,iframe|on*",            // 开启 HTML 标签解析，为了安全性，默认不开启
                    //toolbar  : false,             //关闭工具栏
                    //previewCodeHighlight : false, // 关闭预览 HTML 的代码块高亮，默认开启
                    taskList: true,
                    tocm: true,                 // Using [TOCM]
                    tex: true,                   // 开启科学公式TeX语言支持，默认关闭
                    flowChart: true,             // 开启流程图支持，默认关闭
                    sequenceDiagram: true,       // 开启时序/序列图支持，默认关闭,
                    //dialogLockScreen : false,   // 设置弹出层对话框不锁屏，全局通用，默认为true
                    //dialogShowMask : false,     // 设置弹出层对话框显示透明遮罩层，全局通用，默认为true
                    //dialogDraggable : false,    // 设置弹出层对话框不可拖动，全局通用，默认为true
                    //dialogMaskOpacity : 0.4,    // 设置透明遮罩层的透明度，全局通用，默认值为0.1
                    //dialogMaskBgColor : "#000", // 设置透明遮罩层的背景颜色，全局通用，默认为#fff
                    imageUpload: true,
                    imageFormats: ["jpg", "jpeg", "gif", "png", "bmp", "webp", "mp4"],
                    imageUploadURL: "./static/php/upload.php",
                    onload: function () {
                        //手动操作让其达到全屏预览
                        this.watch()
                        this.previewed();//首先对上一次的清屏
                        this.previewing();//全屏预览
                    },
                    onchange() {
                        // 解决初次加载时执行change事件
                        // console.log('onchange...', vm.init)
                        if ( vm.init> 2) {
                            vm.https.updateNote({
                                id: vm.$store.state.currentNote.id,
                                content: this.getMarkdown()
                            }).then(({data}) => {
                                console.log("修改数据库成功", data);
                            })
                        }
                        vm.init += 1
                    }
                });
            },
        },
        watch: {
            '$store.state.currentNote'() {
                // console.log('this.init = true')
                this.init = 1
                this.initEditor();
            }
        },
        mounted() {
            this.initEditor();
        },
    };
</script>