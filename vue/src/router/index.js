import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter);

import Home from '@/components/Home'
import noteList from "../func/note/noteList";
import noteBookList from "../func/note/noteBookList";
import noteBookInfo from "../func/note/noteBookInfo";
import note from "../func/note/note";
import search from "../func/search/search";
import searchResultList from "../func/search/searchResultList";
import searchResultItem from "../func/search/searchResultItem";

const Edit = () => import('@/components/Edit');

let routes = [

  {
    path: '/home',
    name: 'home',
    component: Home,
    children: [
      {
        path: 'noteList/:notes/:noteBookTagName',
        name: 'noteList',
        component: noteList,
        children: [{
          path: 'note1/:note',
          name: 'note1',
          components: {
            note1: note
          },
        }]
      },
      {
        path: 'noteBookList',
        name: 'noteBookList',
        component: noteBookList,
        children: [{
          path: 'noteList/:notes/:noteBookTagName',
          name: 'noteListInNoteBook',
          component: noteList,
          children: [{
            path: 'note2/:note',
            name: 'note2',
            components: {
              note1: note
            }
          }]
        }]
      }
    ]
  },
  {
    path: '/search',
    name: 'search',
    component: search,
    children: [
      {
        path: 'searchResultList/:notes/:noteBookTagName',
        name: 'searchResultList',
        component: searchResultList,
        children: [{
          path: 'note3/:note',
          name: 'searchResultItem',
          components: {
            searchResultItem: searchResultItem
          },
        }]
      }
    ]
  },
  {
    // 新建笔记
    path: '/createNote',
    component: Edit,
  },
  // {
  //   path: '*',
  //   redirect: '/home'
  // }
];

let router = new VueRouter({
  mode: 'history',
  routes,
});

export default router;
