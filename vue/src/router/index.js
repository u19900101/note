import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter);

import Home from '@/components/Home'
import noteList from "../func/note/noteList";
import note from "../func/note/note";
const Edit = () => import('@/components/Edit');

let routes = [
  {
    path:'/home/:id?',
    component:Home,
  },
  // {
  //   path:'/noteList/:id?',
  //   component:noteList,
  // },
  {
    path:'/edit',
    component:Edit,
  },
  {
    path:'*',
    redirect:'/home/1'
  }
];

let router = new VueRouter({
   mode:'history',
   routes,
});

export default router;
