<template>
    <div class="order" style="margin-right: 10px">
        <div id="vditor"></div>
    </div>
</template>
<script>
    import Vditor from "vditor"
    import "vditor/dist/index.css"

    export default {
        data() {
            return {
                contentEditor: "",
                mdImageUploadLastTime : 0,
            }
        },
        mounted() {
            let vm = this
            vm.editortheme = this.$store.state.sortWay.editortheme
            vm.contentTheme = this.$store.state.sortWay.contenttheme
            this.contentEditor = new Vditor("vditor", {
                toolbar: [
                    'emoji', 'br', 'bold', '|', 'line','code-theme' ,
                    //自定义按钮
                    {
                        name: 'sponsor',
                        tipPosition: 's',
                        tip: '切换主题',
                        className: 'right',
                        icon: '<svg t="1589994565028" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2808" width="32" height="32"><path d="M506.6 423.6m-29.8 0a29.8 29.8 0 1 0 59.6 0 29.8 29.8 0 1 0-59.6 0Z" fill="#0F0F0F" p-id="2809"></path><path d="M717.8 114.5c-83.5 0-158.4 65.4-211.2 122-52.7-56.6-127.7-122-211.2-122-159.5 0-273.9 129.3-273.9 288.9C21.5 562.9 429.3 913 506.6 913s485.1-350.1 485.1-509.7c0.1-159.5-114.4-288.8-273.9-288.8z" fill="#FAFCFB" p-id="2810"></path><path d="M506.6 926c-22 0-61-20.1-116-59.6-51.5-37-109.9-86.4-164.6-139-65.4-63-217.5-220.6-217.5-324 0-81.4 28.6-157.1 80.6-213.1 53.2-57.2 126.4-88.8 206.3-88.8 40 0 81.8 14.1 124.2 41.9 28.1 18.4 56.6 42.8 86.9 74.2 30.3-31.5 58.9-55.8 86.9-74.2 42.5-27.8 84.3-41.9 124.2-41.9 79.9 0 153.2 31.5 206.3 88.8 52 56 80.6 131.7 80.6 213.1 0 103.4-152.1 261-217.5 324-54.6 52.6-113.1 102-164.6 139-54.8 39.5-93.8 59.6-115.8 59.6zM295.4 127.5c-72.6 0-139.1 28.6-187.3 80.4-47.5 51.2-73.7 120.6-73.7 195.4 0 64.8 78.3 178.9 209.6 305.3 53.8 51.8 111.2 100.3 161.7 136.6 56.1 40.4 88.9 54.8 100.9 54.8s44.7-14.4 100.9-54.8c50.5-36.3 108-84.9 161.7-136.6 131.2-126.4 209.6-240.5 209.6-305.3 0-74.9-26.2-144.2-73.7-195.4-48.2-51.9-114.7-80.4-187.3-80.4-61.8 0-127.8 38.5-201.7 117.9-2.5 2.6-5.9 4.1-9.5 4.1s-7.1-1.5-9.5-4.1C423.2 166 357.2 127.5 295.4 127.5z" fill="#141414" p-id="2811"></path><path d="M353.9 415.6m-33.8 0a33.8 33.8 0 1 0 67.6 0 33.8 33.8 0 1 0-67.6 0Z" fill="#0F0F0F" p-id="2812"></path><path d="M659.3 415.6m-33.8 0a33.8 33.8 0 1 0 67.6 0 33.8 33.8 0 1 0-67.6 0Z" fill="#0F0F0F" p-id="2813"></path><path d="M411.6 538.5c0 52.3 42.8 95 95 95 52.3 0 95-42.8 95-95v-31.7h-190v31.7z" fill="#5B5143" p-id="2814"></path><path d="M506.6 646.5c-59.6 0-108-48.5-108-108v-31.7c0-7.2 5.8-13 13-13h190.1c7.2 0 13 5.8 13 13v31.7c0 59.5-48.5 108-108.1 108z m-82-126.7v18.7c0 45.2 36.8 82 82 82s82-36.8 82-82v-18.7h-164z" fill="#141414" p-id="2815"></path><path d="M450.4 578.9a54.7 27.5 0 1 0 109.4 0 54.7 27.5 0 1 0-109.4 0Z" fill="#EA64F9" p-id="2816"></path><path d="M256 502.7a32.1 27.5 0 1 0 64.2 0 32.1 27.5 0 1 0-64.2 0Z" fill="#EFAFF9" p-id="2817"></path><path d="M703.3 502.7a32.1 27.5 0 1 0 64.2 0 32.1 27.5 0 1 0-64.2 0Z" fill="#EFAFF9" p-id="2818"></path></svg>',
                        click () {
                            vm.editortheme = !vm.editortheme
                            vm.contentEditor.setTheme(vm.editortheme ? "light": "dark")
                            vm.https.updateSortWay({
                                id: "1", //固定的id
                                editortheme: vm.editortheme
                            }).then(({data}) => {
                                console.log("保存主题成功",data)
                            })
                        },
                    },
                    {
                        name: 'contentTheme',
                        tipPosition: 's',
                        tip: '切换内容主题',
                        className: 'right',
                        icon: '<svg t="1589994565028" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2808" width="32" height="32"><path d="M506.6 423.6m-29.8 0a29.8 29.8 0 1 0 59.6 0 29.8 29.8 0 1 0-59.6 0Z" fill="#0F0F0F" p-id="2809"></path><path d="M717.8 114.5c-83.5 0-158.4 65.4-211.2 122-52.7-56.6-127.7-122-211.2-122-159.5 0-273.9 129.3-273.9 288.9C21.5 562.9 429.3 913 506.6 913s485.1-350.1 485.1-509.7c0.1-159.5-114.4-288.8-273.9-288.8z" fill="#FAFCFB" p-id="2810"></path><path d="M506.6 926c-22 0-61-20.1-116-59.6-51.5-37-109.9-86.4-164.6-139-65.4-63-217.5-220.6-217.5-324 0-81.4 28.6-157.1 80.6-213.1 53.2-57.2 126.4-88.8 206.3-88.8 40 0 81.8 14.1 124.2 41.9 28.1 18.4 56.6 42.8 86.9 74.2 30.3-31.5 58.9-55.8 86.9-74.2 42.5-27.8 84.3-41.9 124.2-41.9 79.9 0 153.2 31.5 206.3 88.8 52 56 80.6 131.7 80.6 213.1 0 103.4-152.1 261-217.5 324-54.6 52.6-113.1 102-164.6 139-54.8 39.5-93.8 59.6-115.8 59.6zM295.4 127.5c-72.6 0-139.1 28.6-187.3 80.4-47.5 51.2-73.7 120.6-73.7 195.4 0 64.8 78.3 178.9 209.6 305.3 53.8 51.8 111.2 100.3 161.7 136.6 56.1 40.4 88.9 54.8 100.9 54.8s44.7-14.4 100.9-54.8c50.5-36.3 108-84.9 161.7-136.6 131.2-126.4 209.6-240.5 209.6-305.3 0-74.9-26.2-144.2-73.7-195.4-48.2-51.9-114.7-80.4-187.3-80.4-61.8 0-127.8 38.5-201.7 117.9-2.5 2.6-5.9 4.1-9.5 4.1s-7.1-1.5-9.5-4.1C423.2 166 357.2 127.5 295.4 127.5z" fill="#141414" p-id="2811"></path><path d="M353.9 415.6m-33.8 0a33.8 33.8 0 1 0 67.6 0 33.8 33.8 0 1 0-67.6 0Z" fill="#0F0F0F" p-id="2812"></path><path d="M659.3 415.6m-33.8 0a33.8 33.8 0 1 0 67.6 0 33.8 33.8 0 1 0-67.6 0Z" fill="#0F0F0F" p-id="2813"></path><path d="M411.6 538.5c0 52.3 42.8 95 95 95 52.3 0 95-42.8 95-95v-31.7h-190v31.7z" fill="#5B5143" p-id="2814"></path><path d="M506.6 646.5c-59.6 0-108-48.5-108-108v-31.7c0-7.2 5.8-13 13-13h190.1c7.2 0 13 5.8 13 13v31.7c0 59.5-48.5 108-108.1 108z m-82-126.7v18.7c0 45.2 36.8 82 82 82s82-36.8 82-82v-18.7h-164z" fill="#141414" p-id="2815"></path><path d="M450.4 578.9a54.7 27.5 0 1 0 109.4 0 54.7 27.5 0 1 0-109.4 0Z" fill="#EA64F9" p-id="2816"></path><path d="M256 502.7a32.1 27.5 0 1 0 64.2 0 32.1 27.5 0 1 0-64.2 0Z" fill="#EFAFF9" p-id="2817"></path><path d="M703.3 502.7a32.1 27.5 0 1 0 64.2 0 32.1 27.5 0 1 0-64.2 0Z" fill="#EFAFF9" p-id="2818"></path></svg>',
                        click () {
                            vm.contentTheme = !vm.contentTheme
                            vm.contentEditor.setTheme(vm.editortheme ? "light": "dark",vm.contentTheme ? "light": "dark")
                            vm.https.updateSortWay({
                                id: "1", //固定的id
                                contenttheme: vm.contentTheme
                            }).then(({data}) => {
                                console.log("保存主题成功",data)
                            })
                        },
                    },
                    'upload',
                    ],
                upload: {
                    accept: 'image/*,.mp3, .wav, .rar,video/*,audio/*,text/*',
                    max: 1024 * 1024 * 1024,
                    multiple: true, //上传文件是否为多个
                    handler(file) {  //自定义上传，当发生错误时返回错误信息
                        for (let i in file) {
                            let formData = new FormData()
                            formData.append('file', file[i])
                            let request = new XMLHttpRequest()
                            // 图片上传路径
                            request.open('POST', "http://lpgogo.top/api/admin/file/uploadFileAndInsert")
                            request.onload = vm.onloadCallback
                            request.send(formData)
                        }
                    },
                },
                toolbarConfig: {
                    pin: true, //pin	是否固定工具栏	false
                    // hide: true, //是否隐藏工具栏	false
                },
                counter: {enable: true, type: 'text'}, //'markdown', 'text'
                cache: {
                    enable: false
                },
                after: () => {
                    this.contentEditor.setValue(this.$store.state.currentNote.content)
                    /*设置主题*/
                    this.contentEditor.setTheme(this.$store.state.sortWay.editortheme ?"light": "dark",
                        this.$store.state.sortWay.contenttheme ? "light": "dark",
                        "dark")
                    /*控制边距 todo*/
                    /*let lastPixelRatio = window.devicePixelRatio;
                    window.addEventListener('resize', function () {
                        let currentPixelRatio = window.devicePixelRatio;
                        if (currentPixelRatio !== lastPixelRatio) {
                            console.log('页面缩放变化了',document.body.clientWidth -  vm.$store.state.sortWay.navWidth - vm.$store.state.sortWay.listWidth - 100);
                            // vm.contentEditor.vditor.options.width = document.body.clientWidth -  100 - vm.$store.state.sortWay.navWidth - vm.$store.state.sortWay.listWidth + 'px'
                            vm.contentEditor.vditor.options.width = 500 + 'px'
                        }
                        lastPixelRatio = currentPixelRatio;
                    });*/
                },
                /*监听输入框发生变化时保存修改*/
                input(content) {
                    if( content && (content != '\n')){
                        vm.$store.state.currentNote.content = content
                        // 没有标题,默认提取前10个字符为标题'
                        let title = content.substring(0, content.indexOf('\n') + 1)
                        vm.$store.state.currentNote.title = title.replace("#",'')

                        let i = content.indexOf('\n') + 1; //去掉标题
                        let replaceAll = content.substring(i).replaceAll("\\<img.*?\\>|\\<video.*?video\\>|\\n","");
                        let end = 50 > replaceAll.length ? replaceAll.length : 50;
                        let summary = replaceAll.substring(0, end);
                        vm.$store.state.currentNote.summary = summary;

                        let note = {
                            id: vm.$store.state.currentNote.id,
                            content: content,
                            title: title ? title: "#"
                        }
                        // 提取标题
                        let re = /# .+?\n\n/;
                        if (content.match(re)) {
                            // console.log(content.match(re)[0].substring(2))
                            title = content.match(re)[0].substring(2)
                            note.title = title.replace("\n\n", "")
                        }
                        vm.https.updateNote(note).then(({data}) => {
                            console.log("修改数据库成功", data);
                        })
                    }

                },
                width: "80%",  /*编辑器总宽度，支持 %*/
            })
        },
       /* computed:{
            getMdWidth(){
                console.log('kkk   ',this.clientWidth)
                return  this.clientWidth - this.$store.state.sortWay.navWidth - this.$store.state.sortWay.listWidth
            },
        },*/
        methods: {
            /*上传成功后的回调*/
            onloadCallback(oEvent) {
                const currentTarget = oEvent.currentTarget
                if (currentTarget.status !== 200) {
                    return this.$message({
                        type: 'error',
                        message: currentTarget.status + ' ' + currentTarget.statusText
                    })
                }
                let resp = JSON.parse(currentTarget.response)
                let mdStr = ''
                if (resp.code == 'error') {
                    this.$message({type: 'error', message: '上传失败!', duration: 1000,});
                }
                else {
                    let i = resp.data
                    let reg_img=/.+\.(jpg|jpeg|gif|bmp|png)$/;
                    let reg_video=/.+\.(avi|wmv|mpeg|mp4|m4v|mov|asf|flv|f4v|rmvb|rm|3gp|vob)$/;
                    if(reg_img.test(i.title)){  // 缩放图片
                        mdStr += `\n\n&lt;img src="${i.url}" alt = "${i.title}" style="zoom:30%;"/&gt\n\n`
                    }else if(reg_video.test(i.title)){  // 上传视频文件
                        mdStr += `\n\n&lt;video controls preload="auto" src="${i.url}">${i.title}&lt;/video>\n\n`
                    }else{    // 其他格式文件，可以提供下载
                        mdStr += `![${i.title}](${i.url})\n\n`
                    }
                }
                this.contentEditor.insertValue(mdStr);
                /*使用定时器对图片列表进行更新操作*/
                this.tool.setTimeoutUpdate(this.updateImageList, this.mdImageUploadLastTime)
            },
            updateImageList(){
                this.mdImageUploadLastTime = setTimeout(() => {
                    /*更新所有图片的数据 涉及到排序所以直接请求服务器*/
                    this.https.getFiles().then(({data}) => {
                        this.$store.state.fileList = data.data;
                    })
                    this.$message({type: 'success', message: '上传成功!', duration: 1000,});
                }, 2000)
            }
        },
        watch: {
            '$store.state.currentNote'() {
                let content = this.$store.state.currentNote.content
                this.contentEditor.setValue((content && (content != '\n')) ? content: "# 标题\n内容" )
                this.contentEditor.setTheme(this.editortheme ? "light": "dark",this.contentTheme ? "light": "dark","dark")
                //清空撤销和重做记录栈
                this.contentEditor.clearStack()
            }
        },
    }
</script>
<style scoped>
    /*控制内容的边距 todo 无法控制边距*/
   /* .vditor-reset{
        padding: 10px 38.5px !important;
    }*/
    /*解决滑动内容时出现的条纹阴影bug*/
    .vditor {
        display: flex;
        flex-direction: column;
        border: -7px solid var(--border-color) !important;
    }

</style>