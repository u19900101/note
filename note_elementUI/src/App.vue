<template>
  <div id="app">
    <div class="box">
      <home/>
      <!--侧栏100%-->
     <!-- <el-container>
        <el-aside width="200px">Aside</el-aside>
        <el-main>Main</el-main>
      </el-container>-->
    </div>
  </div>
</template>

<script>

import home from "./components/Home"
export default {
  name: 'App',
  components: {
    home
  }
}
</script>

<style>
  html,body {
    margin: 0;
    padding: 0;
    height: 100%;
  }
  #app,.box{
    height: 100%;
  }
  .el-container {
    height: 100%;
  }

  /*.el-aside {
    background-color: #D3DCE6;
    color: #333;
    text-align: center;
    line-height: 200px;
  }

  .el-main {
    background-color: #E9EEF3;
    color: #333;
    text-align: center;
    line-height: 160px;
  }*/
</style>
