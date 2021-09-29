<template>
      <div class="yx-nt-tag" v-if="$store.state.noteTagState">
        <div class="bijibenInfo">
          <h2>标签</h2>
          <div class="message">
            <input type="text" class="messageValue" placeholder="查找标签" v-focus v-model="searchTag">
          </div>
        </div>
        <div class="tag-cont">
          <div class="nt-tag">
            <div class="sp-z"></div>

            <div class="tag-page" v-for="(item,index) in findTagList" :key="item.id">
              <!--展示标签-->
              <div class="changeedit"
                   @mouseover="overHander(item)"
                   @mouseout="outHander"
              >
                  <div class="s-tag" title="进入标签笔记" @click="JoinTagNotes(item)">
                    <span class="z-cont">{{item.title}}</span><span class="t-number">{{item.noteCount}}</span>
                  </div>
                <!--v-if="index===state"-->
                  <div class="nt-func" v-if="item === state">
                    <span class="edittag" title="编辑标签" @click.stop="editTagHander(item,index)"></span>
                    <span class="deletetag" title="删除标签" @click.stop="deleteTagHander(item)"></span>
                  </div>
              </div>
              <div class="edit-tag" v-show="item.tag === editContShow">
                <input type="text" class="editVal"
                       v-model="editValue"
                       @blur.stop="blurHander(item)"
                       ref="editVal"
                >
                <div class="saveedit"></div>
              </div>
            </div>

              <!--当标签为空的时候-->
              <div class="xJtagPic" v-if="!this.$store.state.allTags.length">
                <div class="xJn-cont">
                  <div class="ico"></div>
                  <p style="font-weight: 700">点击笔记,去新建个自己的专属标签吧!</p>
                  <p class="tipmecces">添加标签,查找更容易</p>
                </div>
              </div>
<!--            {{filterstag}}-->
          </div>
        </div>
      </div>
</template>

<script>
  export default {
      data(){
        return {
           currentTags: this.$store.state.allTags,
           state:-1, //删除,编辑下标
           editContShow:'', //用来和输入的input框控制显隐
           editValue:'',  //和编辑标签进行双向数据绑定
           searchTag:'', //双向绑定搜索的标签
        }
      },
    methods:{
      overHander(index){
         this.state = index;
      },
      outHander(){
        this.state = -1;
      },
      //进入标签笔记
      JoinTagNotes(tag){
         this.state = -1;
         this.$store.commit('joinTagNotes',tag); //根据当前标签找到笔记对象
         this.$router.push({
           path:'/home/1111111',
         });
        // 如果当搜索框显示的时候,再提交commit关闭
        if(this.$store.state.searchBox){
          this.$store.commit('hideSearchShow')
        }
        // 进入标签隐藏第几阶段笔记组件信息
        this.$store.commit('closeJdShowTag');
        // 如果点击进入标签,预防当前状态处于查看快捷方式
        this.$store.commit('noteListTrue'); //margin-left为原始值,笔记本列表显示
        this.$store.commit('closeHander'); //显示展开图标
      },
      //编辑标签
      editTagHander(obj,index){
          // 将被编辑的对象的tag内容同步在当前组件的editValue中,和input进行双向数据绑定
          this.editValue = obj.tag; //用来和input双向数据绑定的
          this.editContShow = obj.tag; //用tag控制显示隐藏
          //修正
          let bl = this.$refs.editVal;
          this.$nextTick(function(){
            bl[index].focus()
          })
      },
      // 失去焦点事件
      blurHander(obj){
        // 判断空否
         if(this.editValue !== this.editContShow){
             if(this.editValue.trim()){
               //提交commit 同步最新数据
               this.$store.commit('changeEditHander',{
                 tag:obj.tag,
                 val:this.editValue,
               })
             }
             else{
               this.editValue = this.editContShow;
             }
         }
         // 如果在不做任何修改的情况下,隐藏
         else{
            this.editContShow = '';
         }
      },
      //删除标签
      deleteTagHander(obj){
        this.$store.commit('deleteTagHander',obj.tag)
      },
    },
    computed:{

      //   // 过滤完成的标签
      //   filterstag(){
      //
      //     // 从数据库中取出所有的tag
      //     let allTags = this.$store.state.allTags;
      //     // tag:newArr[i].tag,
      //     //   len:count,
      //     //   id:Math.random(),
      //     // 将所有的笔记tag列表,同步到vuex状态中
      //     // this.$store.commit('setAllTags',allTags);
      //     // // 将标签数据同步在editValue
      //     // this.currentTags = allTags;
      //     return allTags
      // },
      findTagList(){

          if(this.searchTag == undefined || this.searchTag.length == 0){
            return this.currentTags
          }else {
            return this.currentTags.filter(item => {
              return item.title.match(this.searchTag)
            })
          }

      }
    },
    // 要在Tag组件侦听标签组件的变化,如果显示就像vuex中同步标签数据。
    watch:{
       '$store.state.noteTagState':function(){
          if(this.$store.state.noteTagState){
            // this.$store.commit('filterstag',this.$store.state.allTags);
            this.currentTags = this.$store.state.allTags;
            // filterstag()
          }
       }
    },
    directives: {
      focus: {
        // 指令的定义
        inserted: function (el) {
          el.focus()
        }
      }
    }
  }
</script>

<style scoped>

</style>
