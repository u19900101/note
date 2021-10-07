let str = '笔记2-内<font style="background:yellow" color="red">s</font>'
console.log(str);
str = str.replace(/<font style="background:yellow" color="red">/gi,"").replace(/<\/font>/gi,"")
console.log(str);
// let str="Visit Microsoft! Visit Microsoft!";
// let n=str.replace(/i/gi,"好");
// console.log(n);
