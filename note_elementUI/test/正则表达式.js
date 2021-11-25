let re = /#.+?\n\n/;
let str = '#hello \n\nhuxiao6  #hello \n\n';

// if(str.match(re)) console.log(str.match(re)[0])

let s = "1.jpg 2.png 3.jpeg 4.gif"
s = ".avi .wmv .mpeg .mp4 .m4v .mov .asf .flv .f4v .rmvb .rm .3gp .vob"

let reg_img=/.+\.(jpg|jpeg|gif|bmp|png)$/;
let reg_video=/.+\.(avi|wmv|mpeg|mp4|m4v|mov|asf|flv|f4v|rmvb|rm|3gp|vob)$/;
console.log(reg_img.test(s))
console.log(reg_video.test('sjsjsjsj\jjkkkk1.mp4'))
