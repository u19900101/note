import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter);

import Home from '@/components/Home'
// import Edit from '@/components/Edit'
const Edit = () => import('@/components/Edit');

let routes = [
  {
    path:'/home/:id?',
    component:Home,
  },
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
