<template>
    <div>
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
        <el-tabs v-model="activeName" @tab-click="handleClick">
            <el-tab-pane label="人物" name="first">人物
                <!--使用draggable组件-->
                <draggable v-model="persons"  chosenClass="chosen" forceFallback="true" group="people" animation="1000"
                           :disabled="persons.length < 2"
                           @start="onStart" @end="onEnd">
                    <transition-group class="imgItem">
                        <div v-for="person in persons" :key="person.id" style="position: relative">
                            <div @mouseover="currentPerson = person"
                                 @mouseleave="editPersonName = false"
                                 style="display:flex;flex-direction: column;align-items: center;
            width: 200px;margin-left: 10px;border: 1px solid red">
                                <el-image
                                        :style="{width: imageScale,height: imageScale,
                        boxShadow: currentPerson.id == person.id?'0 0 5px 3px #999':''}"
                                        style="border-radius: 50%;"
                                        @click.stop="personClick(person)"
                                        :src="person.faceList[0].url"
                                        fit="cover">
                                </el-image>


                                <el-input v-if="currentPerson.id == person.id && editPersonName"
                                          style="width: 150px;padding-left: 26px;font-size:20px;font-weight:bold;"
                                          placeholder="添加姓名"
                                          v-model="currentPersonName"
                                          clearable>
                                </el-input>
                                <strong v-else style="margin-top: 10px;font-size: 25px;"
                                        @click="editPersonName = true">{{person.name}}</strong>
                                <span style="margin-top: 5px"> {{ person.pictureList.length}} 照片</span>
                            </div>
                        </div>
                    </transition-group>
                </draggable>
            </el-tab-pane>
            <el-tab-pane label="人脸管理" name="second">
                人脸管理
                <div v-for="person in persons" :key="person.id" style="position: relative">
                    <!--名称显示-->
                    <el-input v-if="currentPerson.id == person.id && editPersonName"
                              style="width: 150px;padding-left: 26px;font-size:20px;font-weight:bold;"
                              placeholder="添加姓名"
                              v-model="currentPersonName"
                              clearable>
                    </el-input>
                    <strong v-else style="margin-top: 10px;font-size: 25px;"
                            @click="editPersonName = true">{{person.name}} id {{person.id}}</strong>
                    <span style="margin-top: 5px"> {{ person.pictureList.length}} 照片</span><br>
                <!--    {{  person.faceUrls}}-->
                  <div class="imgItem"
                         style="margin-left: 10px;border: 1px solid red">
                      <div  v-for="face in person.faceList">
                          <div style="display: flex;flex-direction: column"  @mouseover="currentPerson = person">
                              <el-image
                                      @mouseover="currentFace = face"
                                      @mouseleave="editPersonName = false"
                                      :style="{width: imageScale,height: imageScale,
                        boxShadow: currentFace.id == face.id?'0 0 5px 3px #999':''}"
                                      style="border-radius: 50%;"
                                      @click.stop="personClick(person)"
                                      :src="face.url"
                                      fit="cover">
                              </el-image>
                              <div>
                                  <el-button @click="deleteFace(face)">移除</el-button>
                                  <el-button>添加到</el-button>
                              </div>

                          </div>

                      </div>

                    </div>
                </div>
            </el-tab-pane>

        </el-tabs>
    </div>

</template>

<script>
    //导入draggable组件
    import draggable from 'vuedraggable'

    export default {
        name: "Person",
        components: {draggable},
        data() {
            return {
                imageScale: '200px',
                editPersonName: false,
                currentPerson: this.$store.state.persons[0],
                currentFace: {},
                personNameLastTime: 0,//修改名称定时器
                activeName: 'second',

            }
        },
        computed: {
            currentPersonName: {
                get: function () {
                    let name = this.currentPerson.name
                    return name == '添加姓名' ? '' : name
                },
                set: function (newPersonName) {
                    /*1.更新页面*/
                    this.currentPerson.name = newPersonName
                    if (newPersonName.length > 0) {
                        /*2.修改到数据库*/
                        this.tool.setTimeoutUpdate(this.updatePersonName, this.personNameLastTime)
                    }
                }
            },
            persons() {
                /*给人脸封装picture*/
              /*  let persons = []
                for (let person of this.$store.state.persons) {
                    if (person.pictureUid.length > 1) {
                        person.pictureList = []
                        person.faceUrls = []
                        let pictureIds = person.pictureUid.split(",")
                        for (let pictureId of pictureIds) {
                            let image = this.$store.state.fileList.filter((i) => i.id == pictureId)[0]
                            /!*将照片列表和faceUrl封装到person中*!/
                            if (image) {
                                person.pictureList.push(image)
                                /!*将人脸url封装到person中*!/
                                for (let f of image.faceList) {
                                    if (f.faceNameId == person.id) {
                                        person.faceUrls.push(f.url)
                                    }
                                }
                            }
                        }
                        if (person.faceUrls.length > 0) {
                            persons.push(person)
                        }
                    }
                }
                this.$store.state.personNum = persons.length
                return persons*/
                return this.$store.state.persons
            }
        },
        methods: {
            /*删除不需要的人脸*/
            deleteFace(face){
                /*1.从face表中删除*/
                /*2.从服务器中删除*/
                /*3.更新相关联的 person、picture表*/
                this.https.deleteFace(face).then(({data}) => {
                    console.log("成功删除人脸", data);
                })
                /*4.页面更新*/
                if(this.currentPerson.faceList.length ==1){
                    this.$store.state.persons = this.persons.filter(p => p.id != this.currentPerson.id)
                }else {
                    this.currentPerson.faceList = this.currentPerson.faceList.filter(f => f.id != face.id)
                }

            },
            handleClick(tab, event) {
                // console.log(tab, event);
            },
            //开始拖拽事件
            onStart() {
            },
            //拖拽结束事件
            onEnd(e) {
                /*此时数组已发生改变*/
                let fromIndex = e.newIndex
                let toIndex = e.oldIndex < e.newIndex ? e.newIndex - 1 : e.newIndex + 1
                console.log(fromIndex, toIndex)

                this.$confirm("<h2>此操作将合并</h2>" +
                    "<img style='border-radius: 50%;width:100px;height:100px' src=" + this.persons[fromIndex].faceUrls[0] + " >"
                    + "<img style='border-radius: 50%;width:100px;height:100px' src=" + this.persons[toIndex].faceUrls[0] + " >" +
                    "<h3>是否继续?</h3>", '提示', {
                    dangerouslyUseHTMLString: true,
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    center: true
                }).then(() => {
                    /*合并相同的面孔*/
                    this.mergePerson(fromIndex, toIndex)
                    this.$message({type: 'success', message: '成功!', duration: 1000,});
                }).catch((e) => {
                    console.log(e)
                    this.$message({
                        type: 'info',
                        message: '取消',
                        duration: 1000,
                    });
                });
            },
            updatePersonName() {
                this.personNameLastTime = setTimeout(() => {
                    let p = this.currentPerson
                    this.https.updatePersonName({
                        id: this.currentPerson.id,
                        name: this.currentPerson.name
                    }).then(({data}) => {
                        console.log("修改人物名称成功", data);
                    })
                }, 2000)
            },
            /*跳转到照片列表页面*/
            personClick(p) {
                this.$store.state.listTitle = p.name
                let dayImages = p.pictureList
                dayImages = this.tool.groupImages("day", dayImages)
                this.$store.state.currentImageList = dayImages
                /*保存当前人物的照片列表 用于删除时的页面更新*/
                this.$store.state.currentPerson = p
                this.$router.push({name: 'imageList'})
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
            mergePerson(fromIndex, toIndex) {
                /*合并*/
                let pictureList = [...this.$store.state.persons[fromIndex].pictureList, ...this.$store.state.persons[toIndex].pictureList]
                /*去重 todo 优化*/
                /*更新数据库 1.修改所有fromId的 faceNameId为 to 2.删除from 3.增加to的数量*/
                this.https.mergePerson({
                    fromIndex: this.persons[fromIndex].id,
                    toIndex: this.persons[toIndex].id
                }).then(({data}) => {
                    console.log("合并人物成功", data);
                })
                pictureList = pictureList.filter((item, index) => pictureList.indexOf(item) === index);
                this.persons[toIndex].pictureList = pictureList
                this.persons.splice(fromIndex, 1)


            }
        },
        created() {

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