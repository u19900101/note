
//https://segmentfault.com/a/1190000039778492 讲解到位
let k2 = [{id:1,name:"name1"},{id:1,name:"name2"},{id:1,name:"name3"}]
const array1 = [1, 4, 9, 16];
const map1 = k2.map(x => x.name);
const map2 = array1.map(x => x*2);
// console.log(map1,map2) //[ 'name1', 'name2', 'name3' ] [ 2, 8, 18, 32 ]

let arr = [2, 3, 5, 7]

/*arr.map(function(element, index, array){  }, this);  this可以当参数进行传递*/
let marr = arr.map(function(element, index, array){
    /*console.log(element);
    console.log(index);
    console.log(array);*/
    return element + this;
}, 80);
console.log(marr)


//不传initialValue值
arr = [1,2,3];
// arr = arr.reduce(function(pre,cur,index,arr){return pre+cur});
arr = arr.reduce((pre,cur,index,arr) =>pre+cur);
console.log(arr)