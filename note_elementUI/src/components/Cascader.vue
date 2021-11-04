<template>
    <div v-show="isSortShow">
        <!-- value  初始绑定的值-->
        <el-cascader-panel :options="options"
                           :props="props"
                           v-model="selected"

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
                selected: ['0'],
                props: {multiple: true}, // 开启多选
                /*{
                        value: '1',
                        label: '笔记排序方式',
                        children: [
                            { label: '大小', value:'1-1',},
                            { label: '位置', value:'1-2'}]
                    },*/
                options: this.getOptions(),
                lastCheckNodes: ['0'],
            }
        },
        props: ['isSortShow'],
        methods: {
            getOptions() {
                return [
                    {
                        value: '0',
                        label: '创建时间',
                        disabled: false,

                    }, {
                        value: '1',
                        label: '更新时间',
                        disabled: false,
                    },
                    {
                        value: '2',
                        label: '位置',
                        disabled: false,

                    }, {
                        value: '3',
                        label: '倒序排列',
                    }]
            },
            // 节点的互斥多选
            sortChanged(node) {
                // 将node 变为 array
                let lastValues = []
                this.lastCheckNodes.forEach((v) => {lastValues.push(v)})
                let currentValues = []
                node.forEach((v) => {currentValues.push(v[0])})
                console.log(currentValues, lastValues)

                //新增
                if(currentValues.length > lastValues.length){
                    //差集 找到新增的节点
                    let addIndex = currentValues.filter(function (v) {return lastValues.indexOf(v) == -1})[0]
                    console.log('addIndex ',addIndex)
                    let selected = []
                    // 将新增的选中放入数组中
                    selected.push(addIndex)
                    // 判断当前选中(本次选中或者 历史已选中) 中是否包含 逆序 这一项
                    if(currentValues.includes('3') && addIndex!= '3') selected.push('3')
                    // 若当前选中为 逆序 则 将将之前已经选中的前三项 加入
                    if( addIndex == '3') selected.push(lastValues[0])

                    // 记录本次选中  更新 当前页面选中
                    this.lastCheckNodes = selected
                    this.selected =  selected
                }else { //减少
                    this.lastCheckNodes = currentValues
                    this.selected =  currentValues
                }
                // console.log('selected is ',this.selected)
            },

        },

        created() {

        }
    }
</script>

<style scoped>

</style>