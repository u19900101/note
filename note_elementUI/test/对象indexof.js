let obj = [{id:1,name:'jj'},{id:1,name:'jj'}]
let obj2 = {id:1,name:'jj'}
// console.log(obj.indexOf(obj2)) //false

let obj3 = JSON.parse(JSON.stringify(obj))
obj[0].id = 'kkk'
console.log(obj3)
