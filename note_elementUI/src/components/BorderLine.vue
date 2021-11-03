<template>
    <div class="x-handle"
         @mousedown="mouseDown"
         @mouseleave="isTipShow = false"
         @mouseenter = "isTipShow = true"
         unselectable="on"
         onselectstart="return false"
         style="-moz-user-select: none; -webkit-user-select: none; -ms-user-select: none;"
        :style="{'background':isTipShow?'#db2828':''}"
        >

    </div>

</template>

<script>
    export default {
        name: "BorderLine",
        data () {
            return {
                lastX: '',
                isTipShow: false
            }
        },
        methods: {
            mouseLeave(){

            },
            mouseEnter(){
                // console.log('mouseEnter...')
                this.isTipShow = true
            },
            mouseDown (event) {
                // console.log('mouseDown...')
                this.isTipShow = false
                document.addEventListener('mousemove', this.mouseMove)
                this.lastX = event.screenX
            },
            mouseMove (event) {
                // console.log('mouseMove...')
                this.$emit('widthChange', this.lastX - event.screenX)
                this.lastX = event.screenX
            },
            mouseUp () {
                // console.log('mouseUp...')
                this.lastX = ''
                document.removeEventListener('mousemove', this.mouseMove)
            }
        },
        created () {
            // console.log('created...')
            document.addEventListener('mouseup', this.mouseUp)
        },
        destroyed () {
            // console.log('destroyed...')
            document.removeEventListener('mouseup', this.mouseUp)
        }

    }
</script>

<style scoped>
    .x-handle{
        background: #4a5d6b;
        width: 4px;
        height: 100%;
        display: flex;
    }
</style>