<template>
  <div class="selList">
    <legend class="sortingWay">排序方式</legend>

    <ul class="sortlist">
      <li v-for="(item,index) in sortdata"
          :key="item.id"
          :class="[index === state ? 'action' : '']"
          @mouseover="overHander($event,index)"
          @click="sortClick(item,index)"
      >
        {{ item.title }}
      </li>
    </ul>
    <div class="MenuDivider"></div>
    <div class="viewsel" @click.stop>
      <legend>查看选项</legend>
      <div v-for="item in lookDate"
           :key="item.id"
           :class="item.checked ? 's-pit' : 's-pit n-pit'"
           @click="showHander(item)"
      >
        {{ item.title }}
      </div>
    </div>
  </div>
</template>

<script>
let lookDate = [
  {
    title: '显示图片',
    checked: true,
    id: 1,
  },
  {
    title: '显示文字',
    checked: true,
    id: 2
  }
];
import sortData from '@/assets/js/sortdata'

export default {
  name: "yx--select-sort",
  data() {
    return {
      sortdata: sortData,
      sortwaykk: "createLatest", //默认排序方式
      state: 1,
      lookDate,
    }
  },
  methods: {
    overHander(ev, index) {
      if (this.state === index) {
        if (ev.target.nodeName === 'LI') {
          let target = ev.target;
          target.classList.add('item')
        }
      }
    },

    // 显示文字
    showHander(obj) {
      if (obj.id === 2) {
        obj.checked = !obj.checked;
        // 同步vuex状态
        this.$store.commit('changeShowHander', {
          st: obj.checked,
        })
      }
    },

    // 更换时间排序日期
    sortClick(item, index) {
      if (index !== this.state) {
        this.state = index;
        // 使用 getters 更改排序方式
        // this.$store.getters.getNoteListSortWay(item.way);
        // 根据排序方式 更新笔记的顺序
        // 笔记本列表选项排序
        this.sortWay(this.$store.state.noteModule.currentNoteList,item.way);

        // 更新成功后 将事件传入父组件 关闭显示框
        // this.hideSelectSort()
        this.$emit('hideSelectSort')
        // this.$store.state.noteModule.notes =
        // this.$store.state.noteModule.currentNoteList

      }
    }
  }

}
</script>

<style scoped>
.action {
  background: url("../../assets/images/duigou.png") no-repeat 173px 12px;
}

.sortlist li:hover {
  background: #2dbe60;
  color: #ffffff;
}

.sortlist li:hover.item {
  background: url("../../assets/images/duigouhover.png") no-repeat 173px 12px #2dbe60;
}

.s-pit:hover {
  color: #ffffff;
  background: url("../../assets/images/checkebox2.png") no-repeat 162px 4px #2dbe60;
}

.n-pit:hover {
  color: #ffffff;
  background: url("../../assets/images/xuankuangweixuanzhong.png") no-repeat 162px 4px #2dbe60;
}

</style>
