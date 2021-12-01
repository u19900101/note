<template>
    <div id="app">
        <div class="box">
            <home/>
        </div>
    </div>
</template>

<script>

    import home from "./components/Home"

    export default {
        name: 'App',
        components: {
            home
        },
    }
</script>

<style>
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
