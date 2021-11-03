<template>

    <div>
        <!--搜索入口-->
        <el-input
                placeholder="搜索"
                v-model="searchValue"
                clearable>
        </el-input>
    </div>
</template>

<script>
    export default {
        name: "noteBookInfo",
        data() {
            return {
                isSortShow: false,
                searchValue:''
            }
        },
    }
</script>

<style scoped>

</style>