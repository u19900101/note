function getBt(str){
    let char = str.replace(/[^\x00-\xff]/g, '**');
    return char.length;
}

console.log(getBt('中文ss')) //6