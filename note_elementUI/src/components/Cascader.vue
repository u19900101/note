<template>
    <div v-show="isSortShow">
        <!-- value  初始绑定的值-->
        <el-cascader-panel :options="options"
                           :props="props"
                           v-model="currentSortWays"

                           style="position: absolute;z-index: 1990;background: #f2f2f2"
                           @change="sortChanged"
                           ref="panel"
        ></el-cascader-panel>
    </div>
</template>

<script>

    export default {
        name: "cascader",
        data() {
            return {
                currentSortWays: this.getLastCheckNodes(),
                props: {multiple: true}, // 开启多选

                options: this.getOptions(),
                lastCheckNodes: this.getLastCheckNodes(),  // 上次(初始)选中排序方式
                sortWay: this.$store.state.sortWay, // 数据库中的排序方式
            }
        },
        props: ['isSortShow'],
        methods: {
            getLastCheckNodes(){
                let sortWays = this.$store.state.sortWay
                let lastCheckNodes = []

                if(sortWays.createTime) lastCheckNodes.push('0')
                if(sortWays.updateTime) lastCheckNodes.push('1')
                if(sortWays.location) lastCheckNodes.push('2')
                if(sortWays.reverse) lastCheckNodes.push('3')
                // console.log(lastCheckNodes)
                return lastCheckNodes
            },
            getOptions() {
                return [
                    {
                        value: '0',
                        label: '创建时间',
                    }, {
                        value: '1',
                        label: '更新时间',
                    },
                    {
                        value: '2',
                        label: '位置',
                    }, {
                        value: '3',
                        label: '倒序排列',
                    }]
            },
            // 节点的互斥多选  0-创建时间  1-更新时间  2-位置  3-倒序排列
            sortChanged(node) {
                // 将node 变为 array
                let lastValues = []
                this.lastCheckNodes.forEach((v) => {lastValues.push(v)})
                let currentValues = []
                node.forEach((v) => {currentValues.push(v[0])})
                // console.log(currentValues, lastValues)

                //新增
                let addIndex = null
                let isReverseChanged = false //记录逆序方式是否发生了改变
                let isSortWayChange = true
                if (currentValues.length > lastValues.length) {
                    //差集 找到新增的节点
                    addIndex = currentValues.filter(function (v) {return lastValues.indexOf(v) == -1})[0]
                    // console.log('addIndex ',addIndex)
                    let currentSortWays = []
                    // 将新增的选中放入数组中
                    currentSortWays.push(addIndex)
                    // 判断当前选中(本次选中或者 历史已选中) 中是否包含 逆序 这一项
                    if (currentValues.includes('3') && addIndex != '3') currentSortWays.push('3')
                    // 若当前选中为 逆序 则 将将之前已经选中的前三项 加入
                    if (addIndex == '3') {
                        isReverseChanged = true
                        currentSortWays.push(lastValues[0])
                    }

                    // 记录本次选中  更新 当前页面选中
                    this.lastCheckNodes = currentSortWays
                    this.currentSortWays = currentSortWays
                } else { //减少
                    // 点击了 逆序
                    if( this.lastCheckNodes.includes('3') && !currentValues.includes('3')){
                        console.log('取消了逆序...')
                        isReverseChanged = true
                        this.lastCheckNodes = currentValues
                        this.currentSortWays = currentValues
                    }else { // 点击了除 倒序外的其他 lastCheckNodes 不更新 继续保持选中状态
                        console.log('点击了其他 lastCheckNodes 不更新',this.lastCheckNodes)
                        this.currentSortWays = lastValues
                        this.lastCheckNodes = lastValues
                        isSortWayChange = false
                    }
                }
                // console.log('当前排序方式：', this.currentSortWays)
                // 根据排序方式 更新笔记的顺序
                if(isSortWayChange)  this.updateSortWay(isReverseChanged)
            },
            updateSortWay(isReverseChanged){
                let sortWay = {
                    createTime: this.currentSortWays.includes('0'),
                    updateTime: this.currentSortWays.includes('1'),
                    location: this.currentSortWays.includes('2'),
                    reverse: this.currentSortWays.includes('3')
                }

                // 是否进行逆序
                if (isReverseChanged) {
                    console.log('倒序...')
                    this.$store.state.currentNoteList.reverse()
                }else  if (sortWay.createTime || sortWay.updateTime) {
                    this.tool.sortWay(this.$store.state.currentNoteList, sortWay);
                }
                // 修改全局变量 联动noteList变化
                this.$store.state.sortWay = sortWay
                this.https.updateSortWay(sortWay).then(({data}) => {
                    console.log(data)
                })
            },

        },

        created() {

        }
    }
</script>

<style scoped>

</style>