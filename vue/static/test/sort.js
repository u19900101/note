// 直接改变数组的排序
let fruits = ["1", "300", "45", "5"];
fruits.sort(function (a,b){
   return b-a
});
console.log(fruits);
