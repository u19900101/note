let a = [{"id":1,"name":"name1"},{"id":2,"name":"name2"}]
let b = a.filter(item => item.id == 1)[0];
let c = a.concat()
c[0].name = "1000"
console.log(b,a,c);

let array = [{"id":1,"name":"name1"},{"id":2,"name":"name2"}];
let copyArray = array.concat();
copyArray[0].name = "100";
console.log(array); // [1, 2, 3, 4]
console.log(copyArray); // [100, 2, 3, 4]
