// let a = 1
// a= "an"
// let b = 2
// b = 'nn'
// console.log(typeof (a*b))
// console.log(typeof  NaN)
// console.log(0.1+0.2)  //0.30000000000000004
// console.log(typeof null)
// console.log(typeof object)
//
// console.log(1 + 2 + "3");  // 33
// console.log('1' + 2 + 3);  // 123
// console.log('1' - 1 + 3);  // 3
//
// let d = 20
// d = d++
// console.log(d);  // 20
// console.log(5 && 16) // 6 结果为true时 返回后面一个

// let obj = new Object();
// obj["123"] = 123
// console.log(obj)

function MyClass(){
  // // function kk(){}
  // let kk =1;
  // return kk
}
let c = new MyClass()







console.log(MyClass.prototype)  //MyClass {}
console.log(MyClass.__proto__)  //[Function]
console.log(Object.prototype)   // {}
console.log(Object.__proto__)   // [Function]
console.log(c.__proto__)        //MyClass {}
console.log(c.__proto__.prototype)  //undefined
