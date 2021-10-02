let arr = new Array(3)
arr[0] = "ZhangQian"
arr[1] = "LinFang"
arr[2] = "HaiKun"

console.log(arr);// ["ZhangQian","LinFang","HaiKun"]
console.log(arr.unshift("C"));// 4
console.log(arr);// ["C","ZhangQian","LinFang","HaiKun"]
console.log(arr.unshift("A","B"));// 6
console.log(arr);// ["A","B","C","ZhangQian","LinFang","HaiKun"]
