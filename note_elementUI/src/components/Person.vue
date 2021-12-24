<template>
    <div class="imgItem">
        <!--{{persons}}-->
        <!--横线-->
        <!-- <div    :style="{left: getFace(p).faceLocations[0],top: getFace(p).faceLocations[1]}"
                 style="height: 1px;width: 50px;background-color: #c70a0a;position: absolute;z-index: 10;
        "></div>
         &lt;!&ndash;竖线&ndash;&gt;
         <div style="height: 50px;width: 1px;background-color: #c70a0a;
              position: absolute;
              z-index: 10;
              left: 80px;
              top: 156px;"></div>-->
        <!-- <canvas id="myCanvas" style="border:1px solid red;">
             这是浏览器不支持canvas时展示的信息
         </canvas>
         <el-button @click="draw">kkk</el-button>-->
        <div v-for="person in persons" style="position: relative">
            <div @mouseover="faceId = person.faceUrls[0]"
                 @mouseleave="editPersonName = false"
                 style="display:flex;flex-direction: column;align-items: center;
            width: 200px;height: 260px;margin-left: 10px;border: 1px solid red">
                <el-image
                        :style="{width: imageScale,height: imageScale,boxShadow: faceId == person.faceUrls[0]?'0 0 5px 3px #999':''}"
                        style="border-radius: 50%;"
                        @click="personClick(person)"
                        :src="person.faceUrls[0]"
                        fit="cover">
                </el-image>


                <el-input v-if = "faceId == person.faceUrls[0] && editPersonName"
                          style="width: 150px;padding-left: 26px;font-size:20px;font-weight:bold;"
                          placeholder="添加姓名"
                          v-model="person.name == '添加姓名'?'':person.name"
                          clearable>
                </el-input>
                <strong v-else style="margin-top: 10px;font-size: 25px;"
                        @click="editPersonName = true">{{person.name}}</strong>


                <span style="margin-top: 5px"> {{ person.pictureList.length}} 照片</span>
            </div>
        </div>

    </div>

</template>

<script>

    export default {
        name: "Person",
        components: {},
        data() {
            return {
                imageScale: '200px',
                persons: this.$store.state.persons,
                faceId: 0,
                editPersonName: false,
            }
        },
        methods: {
            personClick(p) {

            },
            /*获取人物组第一张本人在照片中的face信息(定位和关键点)*/
            getFace(p) {
                let personId = p.id;
                let faceUrls = []
                for (let picture of p.pictureList) {
                    for (let f of picture.faceList) {
                        if (f.faceNameId == personId) {
                            // console.log(f)
                            // let faceLocations = f.faceLocations.replaceAll("\\[|\\]","").replaceAll(",","px")
                            // return faceLocations
                            faceUrls.push(f.url)
                        }
                    }
                }
                // console.log(personId,faceUrls)
                return faceUrls
            },
            /*绘制矩形框*/
            draw() {
                let canvasId = "myCanvas";
                let faceNum = 1;
                let rects = [[0, 0, 100, 100]];
                let points = "";
                let faceNamesList = "";
                let srcImgPath = this.persons[0].pictureList[0].url;
                // let face_paths = data.face_paths;
                this.canvasPart(canvasId, srcImgPath, rects, points, faceNum, faceNamesList);
                // showFace(face_paths)
            },
            showFace(face_paths) {
                console.log(face_paths);
                for (let i = 0; i < face_paths.length; i++) {
                    console.log(face_paths[i]);
                    let html = '<img src = "' + face_paths[i] + '" width="80">';
                    $("#right").append(html);
                }
            },
            canvasPart(canvasId, srcImgPath, rects, points, faceNum, faceNamesList) {

                let c = document.getElementById(canvasId);
                let ctx = c.getContext("2d");
                let img = new Image();
                // 这里可以放 图片路径 "./test.jpg"  || base64图片 || 图片链接
                img.src = srcImgPath;
                img.onload = function () {
                    // 设置图片在canvas上 前面两个0,0是边距, 后面是宽高
                    // 让绘图自适应 canvas 尺寸
                    let img_w = img.width;
                    let img_h = img.height;
                    let ch = 500;
                    let scale = ch / img_h;
                    // console.log(rects,"图片高度",img_h,"图片宽度",img_w,);
                    // console.log("画布高度",ch,"画布宽度",img_w*scale,"缩放比例：",scale);
                    $("#" + canvasId).attr("height", ch);
                    $("#" + canvasId).attr("width", img_w * scale);
                    // console.log("cw,ch: ",img_w/img_h*800,ch,"img_w,img_h: ",img_w,img_h);

                    ctx.drawImage(img, 0, 0, img_w / img_h * ch, ch);

                    // 添加文字 后面两个数字是坐标
                    ctx.font = "30px sans-serif";
                    ctx.fillStyle = '#fc0000';
                    ctx.fillText("face Num :" + faceNum, 10, 50);

                    for (let i = 0; i < rects.length; i++) {
                        // 画矩形 前两个数字是坐标, 后面是矩形的宽高 fillRect是填充的
                        ctx.lineWidth = 3;
                        ctx.strokeStyle = '#64e204';
                        ctx.strokeRect(rects[i][0] * scale, rects[i][1] * scale, rects[i][2] * scale, rects[i][3] * scale);
                        // console.log("绘制矩形框：",rects[i][0]*scale, rects[i][1]*scale, rects[i][2]*scale, rects[i][3]*scale)
                        // 显示人名
                        ctx.font = "20px sans-serif";
                        ctx.fillStyle = '#000000';
                        // ctx.fillText(faceNamesList[i], rects[i][0] * scale, rects[i][1] * scale - 10);
                        //  画圈
                        let r = 1;
                        /* for (let j = 0; j < points[0].length; j++) {
                             // ctx.strokeStyle = '#03e2db';
                             ctx.lineWidth = 1;
                             ctx.beginPath();
                             ctx.arc(points[i][j][0] * scale, points[i][j][1] * scale, r, 0, Math.PI * 2, true);
                             // ctx.stroke();// 空心圆
                             ctx.fillStyle = "#69fcd5";
                             ctx.fill();//画实心圆

                         }*/
                    }
                }
            },
        },
        created() {
            /*给人脸封装picture*/
            for (let person of this.$store.state.persons) {
                if (person.pictureUid.length > 1) {
                    person.pictureList = []
                    person.faceUrls = []
                    let pictureIds = person.pictureUid.split(",")
                    for (let pictureId of pictureIds) {
                        let image = this.$store.state.fileList.filter((i) => i.id == pictureId)[0]
                        if (image) {
                            /*将照片列表封装到person中*/
                            person.pictureList.push(image)
                            /*将人脸url封装到person中*/
                            for (let f of image.faceList) {
                                if (f.faceNameId == person.id) {
                                    person.faceUrls.push(f.url)
                                }
                            }
                        }
                    }
                    // console.log(person)
                }
            }
        }
    }
</script>

<style scoped>
    .imgItem {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
    }
</style>