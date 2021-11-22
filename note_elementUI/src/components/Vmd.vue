<template>
    <div class="order">
        <div id="vditor"></div>
    </div>
</template>
<script>
    import Vditor from "vditor"
    import "vditor/dist/index.css"
    export default {
        data(){
            return{
                contentEditor:""
            }
        },
        mounted(){
            let vm = this
            this.contentEditor = new Vditor("vditor",{
                height:670,
                toolbarConfig:{
                    pin:true
                },
                cache:{
                    enable:false
                },
                after:()=>{
                    this.contentEditor.setValue(this.$store.state.currentNote.content)
                },
                input(value){
                    vm.https.updateNote({
                        id: vm.$store.state.currentNote.id,
                        content: value
                    }).then(({data}) => {
                        console.log("修改数据库成功", data);
                    })
                }
            })
        },
        methods:{

        },
        watch: {
            '$store.state.currentNote'() {
                this.contentEditor.setValue(this.$store.state.currentNote.content)
            }
        },
    }
</script>
<style scoped>
</style>