<template>
    <div id="app">
        <div class="box">
            <home/>
            <!--<hTimeLine></hTimeLine>-->
            <!--<noteMap style="height:70% "></noteMap>-->

        </div>
    </div>
</template>


<script>

    import home from "./components/Home"
    // import timeLine from "./components/cb/LeafTimeLine"
    import hTimeLine from "./components/HTimeLine"
    import noteMap from './components/NoteMap'

    export default {
        name: 'App',
        components: {
            home,noteMap,hTimeLine
        },

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
