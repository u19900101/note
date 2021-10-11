let newNote = {
  "id": 1,
  "pid": 1,
  "title": "",
  "status": false,
  "summary": "",
  "createTime": 1,
  "updateTime": "",
  "remindTime": "",
  "content": "",
  "tagUid": "",
  "mediaUid": "",
  "star": false,
  "tagList": [],
  "mediaList": []
}
let s = JSON.stringify(newNote)
console.log(s)

console.log(JSON.parse(s));
