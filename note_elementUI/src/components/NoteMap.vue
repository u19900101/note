<template>
    <el-container>
        <el-header style="padding-top: 10px;">
            <timeline style="margin-bottom: 20px;"></timeline>
            <imageInfo v-if="showImageInfo"></imageInfo>
        </el-header>
        <el-main style="padding: 10px;height: 300px">
            <el-switch
                    style="float: right;"
                    v-model="mapStyle"
                    inactive-color="#13ce66"
                    active-icon-class="iconfont icon-moon"
                    inactive-icon-class="iconfont icon-sun"
                    active-color="#000000">
            </el-switch>
            <div id="container"></div>
        </el-main>
        <div style="position:absolute;display:flex;bottom: 72px; margin-left: 20px;">

            <!--展示图片-->
            <div v-for="(image,indexInner) in this.$store.state.dayImages">
                <imageDetail :image-scale="'100px'"
                             :img="image"
                             :index-inner="indexInner"
                             :images = "$store.state.dayImages"
                             @getShowImageInfo="getShowImageInfo"
                />
            </div>
        </div>
    </el-container>
</template>

<script>
    import timeline from './TimeLine'
    import imageDetail from './ImageDetail'
    import imageInfo from './ImageInfo'
    export default {
        name: "NoteMap",
        components: {timeline,imageDetail,imageInfo},
        data() {
            return {
                /*高德地图相关*/
                marker: '',
                map: '',
                layer: '',
                position: [],
                makerClick: false,
                mapStyle: this.$store.state.sortWay.maptheme,//地图的主题
                title: '',
                createTime: '',
                dayImages: this.$store.state.fileList.slice(0, 5), //当天相关所有照片
                showImageInfo :false,
            }
        },
        computed:{
        },
        methods: {
            /*初始化地图*/
            initMap() {
                let data = [...this.$store.state.notes]
                this.position = [data[0].lnglat.split(',')[0], data[0].lnglat.split(',')[1]]
                this.title = data[0].title
                this.createTime = data[0].createTime
                let marker, map = new AMap.Map("container", {
                    resizeEnable: true,
                    mapStyle: "amap://styles/" + (this.$store.state.sortWay.maptheme ? 'grey' : 'normal'),
                    center: this.position,
                    zoom: 13,
                });

                map.addControl(new AMap.MapType({
                    defaultType: this.$store.state.sortWay.maptype ? 1 : 0 //0代表默认，1代表卫星
                }));

                //给切换图层添加事件
                let domImageMask = document.querySelector(".amap-maptype-con");
                domImageMask.addEventListener("click", () => {
                    console.log('切换图层')
                    this.$store.state.sortWay.maptype = !this.$store.state.sortWay.maptype
                    /*写进数据库*/
                    this.https.updateSortWay({id: 1, maptype: this.$store.state.sortWay.maptype}).then(({data}) => {
                        console.log('更新数据库中的图层')
                    })
                });

                this.map = map
                this.addMarker()
                this.updateContent()

                /*给地图绑定缩放事件*/
                map.on('zoomchange', this.mapZoom);
                /*添加历史坐标*/
                this.initMakers(data)
            },

            /*地图跳转到某点显示*/
            toPoint(lng, lat, title, createTime) {
                this.makerClick = true
                this.position = [lng, lat]
                this.updateMapCenter(this.position)
                this.updateContent(title, createTime)
            },
            // 实例化点标记
            addMarker() {
                let marker = new AMap.Marker({
                    icon: "//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-red.png",
                    position: this.position,
                    map: this.map
                });
                let vm = this
                marker.on('mouseover', function (ev) {
                    // console.log('xxxx mouseover', ev,vm.title, vm.createTime)
                    vm.updateContent(vm.title, vm.createTime)
                });
                this.marker = marker
            },
            updateContent(title, createTime) {
                if (!this.marker) {
                    return;
                }
                // 自定义点标记内容
                let markerContent = document.createElement("div");
                markerContent.setAttribute('color', '#000000');
                markerContent.setAttribute('background', '#139a39');
                // 点标记中的图标
                let markerImg = document.createElement("img");
                markerImg.className = "markerlnglat";
                markerImg.src = "//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-red.png";
                markerImg.setAttribute('width', '25px');
                markerImg.setAttribute('height', '34px');
                markerContent.appendChild(markerImg);

                // 点标记中的文本
                let markerContentDiv = document.createElement("div");

                markerContentDiv.style = 'width: 800px;font-size: 25px;line-height: 30px;' +
                    'margin-left: -234px;margin-top: -171px;' +
                    'color:#000000; display:' +
                    ' -webkit-box !important;overflow: hidden;text-overflow: ellipsis;word-break: break-all;-webkit-box-orient: vertical;\n' +
                    '-webkit-line-clamp: 4; '
                /*时间题头*/
                let timeInfoDiv = document.createElement("div");
                timeInfoDiv.innerHTML = '' + createTime;
                timeInfoDiv.style = 'width: 200px;font-size: 20px;background-color: #dcdfe6;border: 1px solid #D7DADC;border-radius: 5px;'
                markerContentDiv.appendChild(timeInfoDiv);

                /*多行截断的内容*/
                let markerSpan = document.createElement("span");
                markerSpan.innerHTML = '' + title;
                markerSpan.style = 'background-color: #ffffff;color:#000000;border: 1px solid #D7DADC;border-radius: 5px;'

                let vm = this
                markerSpan.addEventListener("click", function (e) {
                    vm.mapToNote(title, createTime)
                });

                markerContentDiv.appendChild(markerSpan);
                markerContent.appendChild(markerContentDiv);

                this.marker.setContent(markerContent); //更新点标记内容
            },
            /*点击地图上显示的标题 跳转到笔记列表*/
            mapToNote(title, createTime) {
                console.log('kkk', title, createTime)
                let note = this.$store.state.notes.filter((n, index) => {
                    if (n.createTime == createTime && n.title == title) {
                        this.$store.state.currentIndex = index
                    }
                    return n.createTime == createTime && n.title == title
                })[0]
                this.$store.state.currentNote = note
                /*定位列表中的位置*/
                this.$store.state.fromCalender = true
                this.$router.push({name: 'notepage'})
            },
            updateMapCenter() {
                this.clearMarker()
                // this.position = [this.position[0] + 0.0001, this.position[1] + 0.0001]
                this.addMarker()
                this.map.setCenter(this.position); //设置地图中心点
            },
            // 清除 marker
            clearMarker() {
                if (this.marker) {
                    this.marker.setMap(null);
                    this.marker = null;
                }
            },
            /*地图的缩放事件*/
            mapZoom() {
                /*1.清除当前标题内容*/
                this.marker.setContent('')
                this.addMarker()
            },
            /*初始化地图上的点 缩放时要重新定位*/
            initMakers() {
                let layer = new Loca.PointLayer({
                    eventSupport: true,
                    map: this.map
                });
                let vm = this
                layer.on('mouseenter', function (ev) {
                    // console.log('layer mouseenter', ev,ev.rawData.title)
                    /*点击的时候不重复执行*/
                    if (!vm.makerClick) {
                         vm.updateContent(ev.rawData.title,ev.rawData.createTime)
                    }

                    vm.makerClick = false
                    /*消除笔记点击进入控制的定位*/
                    vm.$store.state.noteClickLocation = false
                });

                layer.on('click', function (ev) {
                    vm.makerClick = true
                    vm.title = ev.rawData.title
                    vm.createTime = ev.rawData.createTime
                    // console.log('click', ev.rawData)
                    vm.position = ev.lnglat
                    vm.updateMapCenter(vm.position)
                    vm.updateContent()
                    /*同步时间轴的位置*/
                    vm.$bus.$emit('setDateIndex', ev.rawData.title, ev.rawData.createTime)
                });
                let notes = this.$store.state.notes /*diarymapData.diarymapData*/
                layer.setData(notes, {
                    lnglat: function (data) {
                        /*  let item = data.value;
                        return [item.lng, item.lat];*/
                        let item = data.value.lnglat.split(',');
                        return [Number(item[0]), Number(item[1])];
                    }
                });
                layer.setOptions({
                    style: {
                        radius: 4,
                        color: '#07E8E4',
                        borderColor: '#07E8E4',
                        borderWidth: 1.5,
                        opacity: 0.8
                    },
                    selectStyle: {
                        radius: 14,
                        color: '#FFF684'
                    }
                });
                layer.render();
                this.layer = layer
            },
            getShowImageInfo(showImageInfo){
                this.showImageInfo =  showImageInfo
            },
        },
        mounted() {
            this.initMap()
            this.$bus.$on('toPoint', this.toPoint)
        },
        created() {
            // console.log('map created ...')
        },
        destroyed() {
            this.map.off('zoomchange', this.mapZoom);
        },
        watch: {
            mapStyle(flag) {
                /*更新数据库中的样式*/
                this.https.updateSortWay({id: 1, maptheme: flag}).then(({data}) => {
                    console.log('更新数据库中的样式')
                })
                this.map.setMapStyle("amap://styles/" + (flag ? 'grey' : 'normal'));
            }
        }
    }
</script>

<style scoped>
    .amap-icon img,
    .amap-marker-content img {
        width: 25px;
        height: 34px;
    }

    .marker {
        position: absolute;
        top: -20px;
        right: -118px;
        color: #fff;
        padding: 4px 10px;
        box-shadow: 1px 1px 1px rgba(10, 10, 10, .2);
        white-space: nowrap;
        font-size: 12px;
        font-family: "";
        background-color: #25A5F7;
        border-radius: 3px;
    }

    .input-card {
        width: 18rem;
        z-index: 170;
    }

    .input-card .btn {
        margin-right: .8rem;
    }

    .input-card .btn:last-child {
        margin-right: 0;
    }

    #container {
        height: 95%;
        width: 100%;
        margin: 0;
    }
</style>