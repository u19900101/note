<template>
    <div>

        <el-tree :data="data"
                 :props="defaultProps"
                 @node-click="handleNodeClick"
                 accordion
        ></el-tree>
    </div>
</template>

<script>
    export default {
        name: 'noteSort',
        data() {
            return {
                data: [
                    {
                    label: '笔记排序方式',
                    children: [
                        { label: '大小'},
                        { label: '位置'}]
                },
                    {
                        label: '创建时间',

                    }, {
                        label: '更新时间',
                    },
                    {
                        label: '位置',

                    }, {
                        label: '倒序排列',
                    }],
                defaultProps: {
                    children: 'children',
                    label: 'label'
                }
            };
        },
        methods: {
            handleNodeClick(data) {
                console.log('clicked Node', data.label);
            }
        }
    };
</script>