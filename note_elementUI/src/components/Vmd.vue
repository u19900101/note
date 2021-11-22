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
                toolbarConfig: {
                    pin: true
                },
                cache: {
                    enable: false
                },
                after: () => {
                    this.contentEditor.setValue(this.$store.state.currentNote.content)
                },
                input(content) {
                    // 没有标题,默认提取前10个字符为标题'
                    let title = content.substring(0, 10)
                    console.log('kkk',title)
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