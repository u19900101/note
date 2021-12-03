let obj = [{id:1,name:'jj'},{id:1,name:'jj'}]
let obj2 = {id:1,name:'jj'}
// console.log(obj.indexOf(obj2)) //false

let obj3 = JSON.parse(JSON.stringify(obj))
obj[0].id = 'kkk'
// console.log(obj3)

const array1 = new Array(5).fill(false);

// fill with 0 from position 2 until position 4
// console.log(array1.fill(0, 2, 4));
// // expected output: [1, 2, 0, 0]
//
// // fill with 5 from position 1
// console.log(array1.fill(5, 1));
// expected output: [1, 5, 5, 5]

console.log(array1);

