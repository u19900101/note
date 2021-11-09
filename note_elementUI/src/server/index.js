import axios from 'axios'
import Vue from 'vue'
import {data} from "autoprefixer";

const vm = axios.create({
    baseURL: 'http://admin.note.com',
    // 解决请求参数 方法2：传递到后端事为空
    // headers: {'Content-Type': 'application/x-www-form-urlencoded'}
});

/*
* http://admin.note.com/api/admin/sortWay/allSortWay
* */
/**笔记本*/
export function getNotebooks() {
    // return vm.get('/api/admin/notebook')
    // 直接访问网关目录下的 静态资源用作测试
    return vm.get('/api/admin/noteBook/allNoteBooks')
}

export function getNoteBooksTree() {
    return vm.get('/api/admin/noteBook/noteBooksTree')
}

export function updateNotebook(data) {
    return vm.post('/api/admin/noteBook/updateNotebook', data)
}


/**笔记*/
export function getNotes() {
    return vm.get('/api/admin/note/allNotes')
}
export function getWastepaperNotes() {
    return vm.get('/api/admin/note/getWastepaperNotes')
}
// 更新笔记的标题和内容
export function updateNote(data) {
    return vm.post('/api/admin/note/update', data)
}

export function deleteNote(data) {
    return vm.post('/api/admin/note/deleteNote', data)
}
// 清空废纸篓
export function clearAllWasteNotes() {
    return vm.post('/api/admin/note/clearAllWasteNotes')
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

/**排序方式*/
export function getSortWay() {
    return vm.get('/api/admin/sortWay/allSortWay')
}
/** ps 导航栏和笔记列表的宽度 */
export function updateSortWay(data) {
    return vm.post('/api/admin/sortWay/updateSortWay', data)
}




export default {
    install(Vue) {
        Vue.prototype.https = {
            getNotebooks,
            updateNotebook,
            getNoteBooksTree,
            getTags,
            updateTag,
            getNotes,
            updateNote,
            insertNote,
            searchNoteByWords,

            getSortWay,
            updateSortWay,

            deleteNote,
            getWastepaperNotes,
            clearAllWasteNotes,
        }
    }
}
