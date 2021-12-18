//https://segmentfault.com/a/1190000039310119
let data = [{id:1,name:'kk'}]


let obj2 = require('lodash').cloneDeep(data);
// let obj2 = [...data]

let inputTag = data;
let r = []
let tree = []
r.push(inputTag);
tree.push(inputTag);
r[0][0].name = "改变"
console.log(r,tree,obj2)
