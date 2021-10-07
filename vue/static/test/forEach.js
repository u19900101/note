// let a = [1,2,3,4,5]

let a = {"_source" : {
    "pid" : "2",
    "title" : "笔记-2",
    "content" : "笔记2-内s",
    "status" : false,
    "summary" : "笔记2-简介",
    "create_time" : "2021-09-11T11:01:00+00:00",
    "update_time" : "2021-10-03T02:12:28+00:00",
    "remind_time" : "2021-09-24T17:01:55+00:00",
    "tag_uid" : "tag_id_2",
    "media_uid" : "4.jpg",
    "star" : false
  },}
// 这种情况数据不会发生改变
// a.forEach((item) => {
//   item = item * 2
// })

// 这种情况就会发生改变
a._source.forEach((item, index, arr) => {
  console.log(item)
})

