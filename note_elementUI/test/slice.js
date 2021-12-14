// let arr = [1, 2, 3, 4, 5, 6];
// // arr.splice(0,1,"memeda")
// console.log(arr.slice(1))
// console.log(arr); // [4, 5, 6]


let array = [{id:1},{id:2},{id:3}];
let n = [{id:111},{id:2},{id:3},{id:1},{id:2},{id:3}]
/*替换*/
// array.splice(0, array.length, ...n);

/*插入*/
array.splice(1, 0, 'memmeda');

console.log(array)
