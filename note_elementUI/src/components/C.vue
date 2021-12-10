<template>
    <Calendar ref="calendar" v-model="selectedDate">
       <!-- <template slot="comment">
            &lt;!&ndash;定义自定义自定义自定义&ndash;&gt;
            &lt;!&ndash;自定义自定义自定义自定义自定义自定义自定义自定义自定义自定义自定义自定义自定义自定义自定义自定义自定义自定义自定义&ndash;&gt;
        </template>-->
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
