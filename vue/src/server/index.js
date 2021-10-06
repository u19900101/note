import axios from 'axios'
import Vue from 'vue'
import {data} from "autoprefixer";
// import qs from 'qs'
const vm = axios.create({
    baseURL:'http://admin.note.com',
    // 解决请求参数 方法2：传递到后端事为空
    // headers: {'Content-Type': 'application/x-www-form-urlencoded'}
});

export function getNotebooks(){
   // return vm.get('/api/admin/notebook')
   // 直接访问网关目录下的 静态资源用作测试
   return vm.get('/api/admin/noteBook/allNoteBooks')
}

/* note CRUD */
export function getNotes(){
  return vm.get('/api/admin/note/allNotes')
}

export function insertNote(data){
  return vm.post('/api/admin/note/insert',data)
}

export function getTags(){
  return vm.get('/tag.json')
}

// 更新笔记的标题和内容
export function updateNote(data){

  return vm.post('/api/admin/note/update',data)
}

// 使用es 搜索笔记
export function searchNoteByWords(data){
  return vm.post('/es/search',data)
}


export default {
  install(Vue){
     Vue.prototype.https = {
       getNotebooks,
       getNotes,
       getTags,
       updateNote,
       insertNote,
       searchNoteByWords
     }
  }
}
