import axios from 'axios'
import Vue from 'vue'
import {data} from "autoprefixer";

const vm = axios.create({
    // baseURL: 'http://47.101.137.245',//上线
    // baseURL: 'http://192.168.56.10', //本地
    baseURL: 'http://lpgogo.top', //本地虚拟机 192.168.56.1
    // baseURL: 'http://jingjingmemeda.ltd',
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

export function insertNoteBook(data) {
    return vm.post('/api/admin/noteBook/insertNoteBook', data)
}

export function deleteNotebook(data) {
    return vm.post('/api/admin/noteBook/deleteNotebook', data)
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

// 恢复所有删除的笔记
export function recoverAllNotes() {
    return vm.post('/api/admin/note/recoverAllNotes')
}

export function insertNote(data) {
    return vm.post('/api/admin/note/insert', data)
}

/**搜索笔记**/
export function getSearchHistroy() {
    return vm.get('/api/es/getSearchHistroy')
}

export function searchNoteByWords(data) {
    return vm.post('/api/es/search', data)
}

export function insertSearchWords(data) {
    return vm.post('/api/es/insertSearchWords', data)
}



/**标签**/
export function getTags() {
    return vm.get('/api/admin/tag/allTags')
}

export function getTagsTree() {
    return vm.get('/api/admin/tag/tagsTree')
}

export function updateTag(data) {
    return vm.post('/api/admin/tag/updateTag', data)
}

export function insertTag(data) {
    return vm.post('/api/admin/tag/insert', data)
}

export function deleteTag(data) {
    return vm.post('/api/admin/tag/deleteTag', data)
}

/**排序方式*/
export function getSortWay() {
    return vm.get('/api/admin/sortWay/allSortWay')
}
/** 导航栏和笔记列表的宽度 */
export function updateSortWay(data) {
    return vm.post('/api/admin/sortWay/updateSortWay', data)
}


/*文件*/
export function getFiles() {
    return vm.get('/api/admin/file/allFiles')
}

// 更新照片的收藏
export function updateImage(data) {
    return vm.post('/api/admin/file/update', data)
}

export function deleteImage(data) {
    return vm.post('/api/admin/file/deleteImage', data)
}

export function deleteImageBatch(data) { //idList
    return vm.post('/api/admin/file/deleteImageBatch', data)
}
export function getWastepaperPictureList() {
    return vm.get('/api/admin/file/getWastepaperPictureList')
}

export function recoverAllPictures() {
    return vm.post('/api/admin/file/recoverAllPictures')
}




export default {
    install(Vue) {
        Vue.prototype.https = {
            getNotebooks,
            updateNotebook,
            insertNoteBook,
            getNoteBooksTree,
            deleteNotebook,
            getTags,
            updateTag,
            getNotes,
            updateNote,
            insertNote,
            getSearchHistroy,
            searchNoteByWords,
            insertSearchWords,
            getSortWay,
            updateSortWay,

            deleteNote,
            getWastepaperNotes,
            clearAllWasteNotes,recoverAllNotes,
            /*tag*/
            insertTag,
            deleteTag,
            getTagsTree,
            /*file*/
            getFiles,
            updateImage,
            deleteImage,
            getWastepaperPictureList,recoverAllPictures,deleteImageBatch
        }
    }
}
