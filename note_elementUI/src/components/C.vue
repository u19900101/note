<template>
    <Calendar ref="calendar" v-model="selectedDate">
        <template slot="comment">
            自定义
        </template>
    </Calendar>
</template>

<script>
    import Calendar from "./Calendar"
    export default {
        name:'C',
        components: { Calendar },

        data() {
            return {
                selectedDate: new Date()
            }
        }
    }
</script>
