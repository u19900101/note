let a = [1,2,3,4,5]
// 这种情况数据不会发生改变
// a.forEach((item) => {
//   item = item * 2
// })

// 这种情况就会发生改变
a.forEach((item, index, arr) => {
  arr[index] = item * 2
})
console.log(a)
