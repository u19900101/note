function getTagNodeDataById(tagId, tagTreeData) {
    for (let t of tagTreeData) {
        if (t.id == tagId) return t
        if (t.children.length > 0) return getTagNodeDataById(tagId, t.children)
    }
}

let tree = [{id:1,children:[]},{id:2,children:[{id:6},{id:7},{id:8}]}]
let kk = getTagNodeDataById(6, tree)
console.log(kk);