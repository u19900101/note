<template>
    <div id="app">
        <div class="box">
            <!--<home/>-->
            <!--<timeLine/>-->

            <div style="z-index: 3000;margin-left: 50px;">
                <span class="demonstration">{{date}}</span>
                <el-button @click="dateIndex -= 1"> 前一日</el-button>
                <el-button @click="play">{{isPlay ? '播放':'暂停'}}</el-button>
                <el-button @click="dateIndex += 1"> 后一日</el-button>
                <el-slider v-model="dateIndex"
                           :max=lenOfArr
                           :marks="timeLineMarkers"
                           @change="timelineChange"
                           :format-tooltip="formatTooltip"></el-slider>
            </div>
            <!--<div id="container" tabindex="0" style="height: 80%"></div>-->
        </div>
    </div>
</template>


<script>

    import home from "./components/Home"
    import timeLine from "./components/cb/TimeLine"
    // import diarymapData from './data/diarymapData_bak.json'
    import diarymapData from './data/diarymapData.json'

    export default {
        name: 'App',
        components: {
            home, timeLine
        },
        computed: {
            date() {
                console.log('date')
                let k = this.dateArr[this.dateIndex]
                return this.dateArr[this.dateIndex].day
            },
            lenOfArr() {
                return this.dateArr.length - 1
            },
           /* marks() {
                let res = {}
                for (let i = 1; i < this.dateArr.length - 1; i++) {
                    res[i] = {
                        style: {
                            color: '#000000',
                            fontSize: '10px'
                        },
                        label: '|'
                    }
                }
                res[0] = {
                    style: {
                        color: '#000000',
                    },
                    label: this.dateArr[0].day
                }
                res[this.dateArr.length - 1] = {
                    style: {
                        color: '#000000',
                    },
                    label: this.dateArr[this.dateArr.length - 1].day
                }
                return res
            },*/
            /*dateArr() {
                let data = diarymapData.diarymapData
                fillAbsentDate(data.diarymapData)
                let data = {
                    "diarymapData": [
                        {
                            "lng": 110.2738888889,
                            "lat": 20.0225,
                            "day": "2017\/01\/01 22:44",
                            "title": "报道第一天 感觉非常的差 光头振威"
                        }, {
                            "lng": 110.2730555556,
                            "lat": 20.0233333333,
                            "day": "2017\/01\/05 13:09",
                            "title": "2  集齐五福了 体检，整理东西，太多太杂 很想很想离开到处在咨询 当兵的人不要听回家的歌"
                        },
                        {"lng": 110.2730555556, "lat": 20.0233333333, "day": "2017\/01\/09 12:00", "title": null},
                        {
                            "lng": 110.2719444444,
                            "lat": 20.0233333333,
                            "day": "2017\/01\/10 13:07",
                            "title": "4.心态调整了一些了，搬家了，换了一群班长朋友"
                        }, {
                            "lng": 110.2780555556,
                            "lat": 20.0194444444,
                            "day": "2017\/01\/12 14:41",
                            "title": "5.我决定了，我要走"
                        }, {
                            "lng": 110.2619444444,
                            "lat": 20.025,
                            "day": "2017\/01\/25 22:03",
                            "title": "6一百八十度的大转弯，感谢小姨，感谢老姐"
                        }, {
                            "lng": 110.2719444444,
                            "lat": 20.0233333333,
                            "day": "2017\/01\/26 23:07",
                            "title": "7.心态调整好了，动员，布置会场"
                        }, {
                            "lng": 110.2719444444,
                            "lat": 20.0233333333,
                            "day": "2017\/01\/27 23:06",
                            "title": "8 大年三十 第一次在budui过年，很想家，但是这里也很热闹"
                        }, {
                            "lng": 110.2719444444,
                            "lat": 20.0233333333,
                            "day": "2017\/01\/28 22:20",
                            "title": "9大年初一 政委零点过来贺喜拜年 游园会三等奖，打够级，添民哥过生日"
                        }, {
                            "lng": 110.2730555556,
                            "lat": 20.0236111111,
                            "day": "2017\/01\/29 22:00",
                            "title": "10 初二，外出采购，海滩玩航拍认识一个开直升机的"
                        },
                    ]
                }
               /!* let res = {}
                for (let i = 1; i < this.dateArr.length - 1; i++) {
                    res[i] = {
                        style: {
                            color: '#000000',
                            fontSize: '10px'
                        },
                        label: '|'
                    }
                }
                res[0] = {
                    style: {
                        color: '#000000',
                    },
                    label: this.dateArr[0].day
                }
                res[this.dateArr.length - 1] = {
                    style: {
                        color: '#000000',
                    },
                    label: this.dateArr[this.dateArr.length - 1].day
                }*!/
            }*/

        },
        data() {
            return {
                /*高德地图相关*/
                marker: '',
                map: '',
                position: [114.3330555556, 30.6697222222],
                makerClick: false,
                // dateArr: ['2000-01-05', '2001-02-14', '2002-10-31', '2003-12-22', '2004-07-09'],
                dateArr: [],
                timeLineMarkers : [],
                dateIndex: 0,
                isPlay: true,
                timeIndex: 0,//定时器
            }
        },
        methods: {
            formatDate(date) {
                let month = '' + (date.getMonth() + 1),
                    day = '' + date.getDate(),
                    year = date.getFullYear();
                if (month.length < 2) month = '0' + month;
                if (day.length < 2) day = '0' + day;

                return [year, month, day].join('/');
            },

            fillAbsentDate() {
                // let dateArr = diarymapData.diarymapData.slice(100,200)
                let dateArr = diarymapData.diarymapData
                let marker = {}
                let curday = dateArr[0].day.substring(0, 10)
                for (let i = 0; i < dateArr.length - 1; i++) {
                    /*补全缺失的日期*/
                    let nextDay = dateArr[i + 1].day.substring(0, 10)
                    let cd = new Date(curday)
                    let nd = new Date(nextDay)
                    let detalDay = (nd - cd) / 86400000
                    /*插入缺失的天数*/
                    marker[i] = {
                        style: {
                            color: '#000000',
                            fontSize: '10px'
                        },
                        label: '|'
                    }
                    if (detalDay > 1) {
                        for (let j = 0; j < detalDay - 1; j++) {
                            let temp = new Date(cd)
                            let dateTime = new Date(temp.setDate(cd.getDate() + j + 1));
                            dateArr.splice(i + 1 + j, 0, {
                                "day": this.formatDate(dateTime),
                            })
                            marker[i + 1 + j] = {
                                style: {
                                    color: '#000000',
                                    fontSize: '10px'
                                },
                                label: ''
                            }
                        }
                        i += detalDay - 1
                    }
                    curday = nextDay
                }
                /*覆盖*/
                marker[0] = {
                    style: {
                        color: '#000000',
                    },
                    label: dateArr[0].day
                }
                marker[dateArr.length - 1] = {
                    style: {
                        color: '#000000',
                    },
                    label: dateArr[dateArr.length - 1].day
                }
                this.dateArr = dateArr
                this.timeLineMarkers = marker
            },


            play() {
                /*暂停*/
                if (!this.isPlay) {
                    this.isPlay = true
                    clearInterval(this.timeIndex)
                } else { /*播放*/
                    this.isPlay = false
                    /*在末尾点击播放*/
                    if (this.dateIndex >= this.dateArr.length - 1) {
                        this.dateIndex = 0
                    }
                    this.timeIndex = setInterval(() => {
                        // console.log('setTimeout',this.dateIndex,this.dateArr.length)
                        this.dateIndex += 1
                        if (this.dateIndex >= this.dateArr.length - 1) {
                            this.isPlay = true
                            clearInterval(this.timeIndex)
                        }
                    }, 500)
                }
            },
            timelineChange(value) {
                console.log(value, this.dateIndex)
            },
            formatTooltip(val) {
                if (!val) {
                    val = 0
                }
                return this.dateArr[val].title;
            },
            gdMap() { /*[114.3330555556,30.6697222222]*/
                // console.log('kkk',diarymapData,earthQuake)
                // let map = new AMap.Map('container', {
                //
                //     mapStyle: 'amap://styles/db9efe6a1745ac24b7269b862f359536',
                //     // mapStyle: 'amap://styles/twilight',
                //     // pitch: 10,  //设置地图俯仰角
                //     // resizeEnable: true,
                //     center: position,
                //     zoom: 20,
                //     features: ['bg', 'road'],
                //     // viewMode: '3D'
                // });
                // map.addControl(new AMap.MapType({
                //     // defaultType: 1 //0代表默认，1代表卫星
                // }));
                let layer = new Loca.PointLayer({
                    eventSupport: true,
                    map: this.map
                });

                layer.on('mousemove', function (ev) {
                    // 事件类型
                    let type = ev.type;
                    // 当前元素的原始数据
                    let rawData = ev.rawData;
                    // 原始鼠标事件
                    let originalEvent = ev.originalEvent;

                    openInfoWin(vm.map, originalEvent, {
                        // '名称': rawData.lng,
                        // '位置': rawData.lat,
                        '时间': rawData.day,
                        '干了啥': rawData.title,
                    });
                });

                layer.on('mouseleave', function (ev) {
                    closeInfoWin();
                });

                let vm = this
                layer.on('click', function (ev) {
                    console.log('click', ev.rawData)
                    let position = [ev.rawData.lng + 1, ev.rawData.lat]

                });


                layer.setData(diarymapData.diarymapData, {
                    lnglat: function (data) {
                        let item = data.value;
                        return [item.lng, item.lat];
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
                // this.initPage(map,position,title)
                layer.render();
            },
            initPage(map, position, title) { /*[116.305285, 39.904989]*/

                AMapUI.loadUI(['overlay/SimpleMarker'], function (SimpleMarker) {
                    //启动页面
                    // vm.initPage(SimpleMarker,map,position,title);
                    //创建SimpleMarker实例
                    new SimpleMarker({
                        //前景文字
                        iconLabel: {
                            // innerHTML: '<h1 style="margin-left: 50px;margin-top: 0px;">'+title+'</h1>', //设置文字内容 <i>'+ title+'</i>
                            innerHTML: '<div style="width: 200px;line-height: 25px;">' + title + '</div>', //设置文字内容 <i>'+ title+'</i>
                            style: {
                                color: '#000000' //设置文字颜色
                            }
                        },

                        //图标主题
                        iconTheme: 'fresh',
                        //背景图标样式
                        iconStyle: 'red',

                        //...其他Marker选项...，不包括content
                        map: map,
                        position: position
                    });
                });

            },
            // 实例化点标记
            addMarker(position) {
                this.marker = new AMap.Marker({
                    icon: "//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-red.png",
                    position: position,
                });
                this.marker.setMap(this.map);
            },

            updateContent(content, timeInfo) {

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

                markerContentDiv.style = 'width: 800px;font-size: 30px;line-height: 40px;' +
                    'margin-left: -234px;margin-top: -171px;' +
                    'color:#000000; display:' +
                    ' -webkit-box !important;overflow: hidden;text-overflow: ellipsis;word-break: break-all;-webkit-box-orient: vertical;\n' +
                    '-webkit-line-clamp: 4; '
                let timeInfoDiv = document.createElement("div");
                timeInfoDiv.innerHTML = '' + timeInfo;
                timeInfoDiv.style = 'width: 200px;font-size: 20px;background-color: #dcdfe6;border: 1px solid #D7DADC;border-radius: 5px;'
                markerContentDiv.appendChild(timeInfoDiv);


                let markerSpan = document.createElement("span");
                markerSpan.innerHTML = '' + content;
                markerSpan.style = 'background-color: #ffffff;color:#000000;border: 1px solid #D7DADC;border-radius: 5px;'
                // markerSpan.setAttribute(' background', '#000000');
                /*top:-85px;left:-500px*/
                markerContentDiv.appendChild(markerSpan);
                markerContent.appendChild(markerContentDiv);

                this.marker.setContent(markerContent); //更新点标记内容
            },
            updateMapCenter() {
                this.clearMarker()
                // this.position = [this.position[0] + 0.0001, this.position[1] + 0.0001]
                this.addMarker(this.position)
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
                this.addMarker(this.position)
            },
        },
        mounted() {

            // console.log(jsonData)
            // this.timeLineView(jsonData)

            // this.gdMap([114.3330555556,30.6697222222],'memeda')
            // this.gdMap([114.3330555556,30.6697222222],'2memeda')
            //引入SimpleMarker，loadUI的路径参数为模块名中 'ui/' 之后的部分
            /*let marker, map = new AMap.Map("container", {
                resizeEnable: true,
                mapStyle: "amap://styles/grey",
                center: this.position,
                zoom: 13,
            });
            this.marker = marker
            this.map = map

            /!*给地图绑定缩放事件*!/
            map.on('zoomchange', this.mapZoom);
            this.addMarker(this.position)
            this.updateContent('jjjj','2021.12.14 12:00:00')
            /!*添加历史坐标*!/
            let layer = new Loca.PointLayer({
                eventSupport: true,
                map: map
            });

            layer.on('mouseenter', function (ev) {
                console.log('mouseenter',ev,)
                /!*点击的时候不重复执行*!/
                if(!vm.makerClick){
                    vm.clearMarker()
                    vm.position = [ev.rawData.lng, ev.rawData.lat]
                    vm.addMarker(vm.position)
                    vm.updateContent(ev.rawData.title,ev.rawData.day)
                }
                vm.makerClick = false
            });

            layer.on('mouseleave', function (ev) {
                /!*关闭显示的信息*!/
                // vm.mapZoom();
            });

            let vm = this
            layer.on('click', function (ev) {
                vm.makerClick = true
                console.log('click', ev.rawData)
                vm.position = [ev.rawData.lng, ev.rawData.lat]
                vm.updateMapCenter(vm.position)
                vm.updateContent(ev.rawData.title,ev.rawData.day)
            });


            layer.setData(diarymapData.diarymapData, {
                lnglat: function (data) {
                    let item = data.value;
                    return [item.lng, item.lat];
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
            // this.initPage(map,position,title)
            layer.render();*/
        },
        created() {
            console.log('mounted')
            this.fillAbsentDate();
        },
        destroyed() {
            // this.map.off('zoomchange', this.mapZoom);
        }
    }
</script>

<style>
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
        height: 100%;
        width: 100%;
        margin: 0;
    }

    html,
    body {
        margin: 0;
        padding: 0;
        font-family: "Open Sans", sans-serif;
    }


    html, body {
        margin: 0;
        padding: 0;
        height: 100%;
    }

    #app, .box {
        height: 100%;
    }

    .el-container {
        height: 100%;
    }
</style>
