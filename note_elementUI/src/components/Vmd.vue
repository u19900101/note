<template>
    <div class="order">
        <div id="vditor"></div>
    </div>
</template>
<script>
    import Vditor from "vditor"
    import "vditor/dist/index.css"
    export default {
        data() {
            return {
                contentEditor: ""
            }
        },
        mounted() {
            let vm = this
            this.contentEditor = new Vditor("vditor", {
                height: 670,
                toolbar: [
                    'emoji', 'br', 'bold', '|', 'line','code-theme' , 'content-theme'],
                toolbarConfig: {
                    pin: true, //pin	是否固定工具栏	false
                    // hide: true, //是否隐藏工具栏	false
                },
                counter: {enable: true, type: 'text'}, //'markdown', 'text'
                preview: {
                    theme:
                        {current : "dark"} //, dark light  const v = this.contentEditor  v.setTheme("dark","dark","dark")
                },
                cache: {
                    enable: false
                },
                after: () => {
                    this.contentEditor.setValue(this.$store.state.currentNote.content)
                },
                input(content) {
                    vm.$store.state.currentNote.content = content
                    // 没有标题,默认提取前10个字符为标题'
                    let title = content.substring(0, 10)
                    let note = {
                        id: vm.$store.state.currentNote.id,
                        content: content,
                        title: title
                    }
                    // 提取标题
                    let re = /# .+?\n\n/;
                    if (content.match(re)) {
                        // console.log(content.match(re)[0].substring(2))
                        title = content.match(re)[0].substring(2)
                        note.title = title.replace("\n\n", "")
                    }
                    vm.https.updateNote(note).then(({data}) => {
                        console.log("修改数据库成功", data);
                    })
                }
            })
        },
        methods: {},
        watch: {
            '$store.state.currentNote'() {
                this.contentEditor.setValue(this.$store.state.currentNote.content)
            }
        },
    }
</script>
<style scoped>
</style>