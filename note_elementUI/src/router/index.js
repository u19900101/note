// 该文件专门用于创建整个应用的路由器
import VueRouter from 'vue-router'
//引入组件
import  home from '../components/Home'
import ImageList from "../components/ImageList";
import NoteBook_Tag from "../components/NoteBook_Tag";
import Notepage from "../components/Notepage";
import Calendar from "../components/Calendar";
import  C from "../components/C";


//创建并暴露一个路由器
export default new VueRouter({
    routes: [
        {
            path: '/',
            component: home,
        },
        {   name: 'imageList',
            path:'/imageList',
            components: {
                imageList: ImageList,
            },
        },
        {
            name: 'noteBook_tag',
            path:'/noteBook_tag',
            components: {
                noteBook_tag: NoteBook_Tag,
            },
        },
        {
            name: 'notepage',
            path:'/notepage',
            components: {
                notepage: Notepage,
            },
        },

        {
            name: 'calendar',
            path:'/calendar',
            components: {
                imageList: C,
            },
        },
    ]
})
