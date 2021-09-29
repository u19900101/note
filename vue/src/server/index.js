import axios from 'axios'
import Vue from 'vue'
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

export function getNotes(){
  // return vm.get('/api/admin/notebook')
  // 直接访问网关目录下的 静态资源用作测试
  return vm.get('/api/admin/note/allNotes')
}

export function getTags(){
  return vm.get('/tag.json')
}

export function noteEditChange(data){
  // 方法2
  // return vm.post('/api/admin/note/update',qs.stringify(data))
  return vm.post('/api/admin/note/update',data)
}

export default {
  install(Vue){
     Vue.prototype.https = {
       getNotebooks,
       getNotes,
       getTags,
       noteEditChange
     }
  }
}
