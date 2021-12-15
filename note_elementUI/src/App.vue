<template>
    <div id="app">
        <div class="box">
            <!--<home/>-->
         <timeline style="margin-bottom: 20px;width: 3000px"></timeline>
            <!--   <noteMap style="height:70% "></noteMap>-->



        </div>
    </div>
</template>


<script>

    import home from "./components/Home"
    // import timeLine from "./components/cb/LeafTimeLine"

    import noteMap from './components/NoteMap'
    import timeline from './components/TimeLine'
    export default {
        name: 'App',
        components: {
            home,noteMap,timeline
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
