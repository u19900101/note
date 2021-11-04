import axios from 'axios'
import Vue from 'vue'
import {data} from "autoprefixer";

const vm = axios.create({
    baseURL: 'http://admin.note.com',
    // 解决请求参数 方法2：传递到后端事为空
    // headers: {'Content-Type': 'application/x-www-form-urlencoded'}
});

/*
* http://admin.note.com/api/admin/note/allNotes
* */
/**笔记本*/
export function getNotebooks() {
    // return vm.get('/api/admin/notebook')
    // 直接访问网关目录下的 静态资源用作测试
    return vm.get('/api/admin/noteBook/allNoteBooks')
}

export function updateNotebook(data) {
    return vm.post('/api/admin/noteBook/updateNotebook', data)
}


/**笔记*/
export function getNotes() {
    return vm.get('/api/admin/note/allNotes')
}
// 更新笔记的标题和内容
export function updateNote(data) {
    return vm.post('/api/admin/note/update', data)
}

export function deleteNote(data) {
    return vm.post('/api/admin/note/delete', data)
}

// 使用es 搜索笔记
export function searchNoteByWords(data) {
    return vm.post('/es/search', data)
}
export function insertNote(data) {
    return vm.post('/api/admin/note/insert', data)
}

/**标签**/
export function getTags() {
    return vm.get('/api/admin/tag/allTags')
}
export function updateTag(data) {
    return vm.post('/api/admin/tag/updateTag', data)
}


export default {
    install(Vue) {
        Vue.prototype.https = {
            getNotebooks,
            updateNotebook,
            getTags,
            updateTag,
            getNotes,
            updateNote,
            insertNote,
            searchNoteByWords,
            deleteNote
        }
    }
}
