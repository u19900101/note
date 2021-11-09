let a = [1,2,3,4,5]
let b = [2,4,6,8,10]


//差集
/*let d = a.filter(function(v){ return b.indexOf(v) == -1 })
console.log(d)*/

//交集
let d = a.filter(function(v){ return b.indexOf(v) != -1 })
console.log(d)