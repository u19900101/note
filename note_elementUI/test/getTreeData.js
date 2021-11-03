/**一种愚蠢的实现**/

let res = '{"data":[{"id":2,"pid":0,"level":1,"title":"我的抗战-1","summary":"笔记本1-简介","createTime":"2020-10-13 18:01:00","updateTime":"2020-12-26 19:24:33","noteCount":2,"imgUrl":"1.jpg","status":false},{"id":3,"pid":0,"level":1,"title":"数据库-2","summary":"笔记本2-简介","createTime":"2020-10-13 18:01:00","updateTime":"2020-12-26 19:24:33","noteCount":2,"imgUrl":"1.jpg","status":false},{"id":0,"pid":0,"level":1,"title":"所有笔记","summary":"所有笔记","createTime":"2021-10-27 06:40:58","updateTime":"2021-10-27 06:41:03","noteCount":0,"imgUrl":"","status":false},{"id":1,"pid":0,"level":1,"title":"我的收藏","summary":"收藏笔记","createTime":"2021-10-10 08:00:00","updateTime":"2021-10-10 08:00:00","noteCount":1,"imgUrl":"","status":false},{"id":4,"pid":3,"level":2,"title":"mysql","summary":"","createTime":"2021-10-10 08:00:00","updateTime":"2021-10-10 08:00:00","noteCount":0,"imgUrl":"","status":false},{"id":5,"pid":3,"level":2,"title":"mongoDb","summary":"","createTime":"2021-10-10 08:00:00","updateTime":"2021-10-10 08:00:00","noteCount":0,"imgUrl":"","status":false},{"id":6,"pid":4,"level":3,"title":"mysql子菜单","summary":"","createTime":"2021-10-10 08:00:00","updateTime":"2021-10-10 08:00:00","noteCount":0,"imgUrl":"","status":false}],"code":"success"}'
res = JSON.parse(res).data;
let notes = []
//最大深度
let maxLevel = res[res.length - 1].level
let level = 1
while (level <= maxLevel) {
    for (const item of res) {
        /* 首先遍历一级节点*/
        if (item.level == level) {
            if (item.level == 1) {
                notes.push({id: item.id, label: item.title, pid: item.pid, level: item.level})
            } else {
                notes.forEach((note) => {
                    let hasChildren = false
                    if (level >= 3) {
                        let count = 2
                        while (count < level) {
                            if (note['childern']) {
                                note = note.childern
                                hasChildren = true
                            }
                            count +=1
                        }
                    }
                    if(hasChildren){
                        note.forEach((note) =>{
                            // 只在当前层级的上一级找
                            if (note.level == level - 1 && note.id == item.pid) {
                                if (note['childern']) {
                                    note.childern.push({id: item.id, label: item.title, pid: item.pid, level: item.level})
                                } else {
                                    note['childern'] = [{id: item.id, label: item.title, pid: item.pid, level: item.level}]
                                }
                            }
                        })
                    }else {
                        if (note.level == level - 1 && note.id == item.pid) {
                            if (note['childern']) {
                                note.childern.push({id: item.id, label: item.title, pid: item.pid, level: item.level})
                            } else {
                                note['childern'] = [{id: item.id, label: item.title, pid: item.pid, level: item.level}]
                            }
                        }
                    }
                })
            }
        }
    }
    level += 1
}
console.log(notes)