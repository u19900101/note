import axios from 'axios'
import Vue from 'vue'

const vm = axios.create({
   baseURL:'http://localhost:8600'
});

export function getList(){
   return vm.get('/note.json')
}

export default {
  install(Vue){
     Vue.prototype.https = {
        getList,
     }
  }
}
