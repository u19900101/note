<template>
  <div>
    <div class="defdelete main" title="删除笔记" @click.stop="delNoteHandel()"></div>
    <div class="delpage" v-if="isDeleteShow">
      <div class="delbox">
        <div class="title">
          <img src="@/assets/images/112.png" alt="">
          <div class="miaoshu">删除笔记</div>
        </div>
<!--        继续将删除的数据和逻辑交给上游页面进行处理  解耦 编辑和搜索模式删除后的列表更新问题-->
        <div class="n-note">
          确定删除<strong style="font-weight: normal">
          <slot name="deleteTitle"></slot>
          </strong>吗?
        </div>
        <div class="btn clearfix" @click="isDeleteShow = false">
          <span class="calcel GJDCG5COCC" >取消</span>
<!--          监听子组件点击了删除-->
            <slot name="exeDelete" ></slot>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
// 布完局了
export default {
  data() {
    return {
      isDeleteShow: false
    }
  },
  methods: {
    delNoteHandel() {
      console.log('delNoteId index is ',this.$store.state.noteModule.currentIndex);
      this.isDeleteShow = true
    },

  },
  watch:{
    // 侦听路由对象变化
    $route() {



    },
  }
}
</script>

<style scoped>
.delpage {
  position: absolute;
  left: 50px;
  top: 80px;
  z-index: 1006;
  width: 400px;
  height: 25%;

  background: #ffffff;
  cursor: default;
}

.delpage .delbox {
  width: 400px;
  height: 50%;
  margin: auto;
  font-family: gotham, helvetica, arial, sans-serif;
}

.delpage .title {
  text-align: center;
  /*padding-top: 100px;*/
  box-sizing: border-box;
  color: #878787;
}

.delpage .miaoshu {
  line-height: 31px;
  font-size: 13px;
  letter-spacing: 1px;
  border-bottom: 1px solid #d9d9d9;
  width: 124px;
  margin: 16px auto 0;
  padding-bottom: 4px;
}

.delpage .n-note {
  font-size: 32px;
  color: #383838;
  text-align: center;
  font-family: caecilia, times, serif;
}

.delpage .btn {
  text-align: center;
}

.delpage .calcel {
  background: #ececec;
  color: #606060;
  border: 1px solid #d9d9d9;
}

.delpage .calcel:hover {
  background: rgb(217, 217, 217);
}

.delpage .isdel {
  background: rgb(40, 169, 85);
  border: 1px solid #23944b;
  color: #ffffff;
}

.delpage .isdel:hover {
  background: #23944b;
}

.delpage .GJDCG5COCC {
  cursor: pointer;
  border-radius: 5px;
  display: inline-block;
  margin: 0 4px;
  width: 168px;
  height: 31px;
  line-height: 31px;
  font-family: gotham, helvetica, arial, sans-serif;
  font-size: 13px;
}
</style>
