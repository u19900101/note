<template>
    <div v-show="isSortShow">
        <!-- value  初始绑定的值-->
        <el-cascader-panel :options="options"
                           :props="props"
                           value="1"
                           style="position: absolute;z-index: 1990;background: #f2f2f2"
                           @change="sortChanged"
        ></el-cascader-panel>
    </div>
</template>

<script>

    export default {
        name: "cascader",
        data(){
            return{
                props: { multiple: true }, // 开启多选
                /*{
                        value: '1',
                        label: '笔记排序方式',
                        children: [
                            { label: '大小', value:'1-1',},
                            { label: '位置', value:'1-2'}]
                    },*/
                options: [
                     {
                        value:'2',
                        label: '创建时间',

                    }, {
                        value:'3',
                        label: '更新时间',
                    },
                    {
                        value:'4',
                        label: '位置',

                    }, {
                        value:'5',
                        label: '倒序排列',
                    }]
            }
        },
        props:['isSortShow'],
        methods:{
            sortChanged(node){
                // node 对应的value值
                console.log(node[node.length-1])
                this.$emit("sortClick",node[node.length - 1])
            },

        }
    }
</script>

<style scoped>

</style>