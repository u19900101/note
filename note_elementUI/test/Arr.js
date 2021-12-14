let arr = [9,10]
let res = {}
for (let i =1;i<arr.length;i++) {
    console.log(i,arr[i])
    res[parseInt(i)] = arr[i]
}
console.log('res',res)