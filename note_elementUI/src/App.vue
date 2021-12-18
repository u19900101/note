<template>
    <div id="app">
        <div class="box">
            <home/>
        </div>
    </div>
</template>


<script>

    import home from "./components/Home"
    import hTimeLine from "./components/HTimeLine"
    import noteMap from './components/NoteMap'
    import ElImageViewer from "element-ui/packages/image/src/image-viewer";

    export default {
        name: 'App',
        components: {
            home, noteMap, hTimeLine,ElImageViewer
        },
        data() {
            return {

            }

        },
        computed: {

        },
        methods:{

        },
        created() {

        },
        mounted() {

        }

    }
</script>

<style>

    html,
    body {
        margin: 0;
        padding: 0;
        font-family: "Open Sans", sans-serif;
    }


    html, body {
        margin: 0;
        padding: 0;
        height: 100%;
    }

    #app, .box {
        height: 100%;
    }

    .el-container {
        height: 100%;
    }
</style>
