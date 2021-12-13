<template>
    <div id="app">
        <div class="box">
            <!--<home/>-->
            <!-- <div id="info">
                 <h1 id="day"></h1>
                 <h2 id="title"></h2>
                 <ul id="displayed-list"></ul>
             </div>
             <div id="map"></div>-->
            <div id="container" tabindex="0"></div>

        </div>
    </div>
</template>


<script>

    import home from "./components/Home"
    import jsonData from './data/traceData.json'
    import diarymapData from './data/diarymapData.json'

    export default {
        name: 'App',
        components: {
            home
        },
        data() {
            return {}
        },
        methods: {
            /*地图的更新*/
            updateList(timeline, map) {
                console.log('updateList')
                let displayed = timeline.getLayers();
                let list = document.getElementById("displayed-list");
                let day = document.getElementById("day");
                let title = document.getElementById("title");
                list.innerHTML = "";
                displayed.forEach(function (quake) {
                    day.innerHTML = quake.feature.properties.day;
                    title.innerHTML = quake.feature.properties.title;
                    // 实时更新地图
                    // map.panTo(new L.LatLng(quake.feature.geometry.coordinates[1], quake.feature.geometry.coordinates[0]));
                    // map.center = new L.LatLng(quake.feature.geometry.coordinates[1], quake.feature.geometry.coordinates[0])
                });

            },

            eqfeed_callback(data) {
                /*初始化*/
                // 改用高德地图的图层   /* // 'http://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}',
                // 'http://www.google.cn/maps/vt?lyrs=m@189&gl=cn&x={x}&y={y}&z={z}',
                // 谷歌卫星地图
                //   'http://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}',*/
                let osm = L.tileLayer('http://webst0{s}.is.autonavi.com/appmaptile?style=6&x={x}&y={y}&z={z}',
                    {
                        subdomains: ['1', '2', '3', '4'],
                        minZoom: 1,
                        maxZoom: 19
                    });
                let map = L.map("map", {
                    layers: [osm],
                    center: new L.LatLng(18.692222222222227, 109.18),
                    zoom: 3,
                    maxBounds: [
                        [90, -180],
                        [-90, 180],
                    ],
                });

                let getInterval = function (quake) {
                    // earthquake data only has a time, so we'll use that as a "start"
                    // and the "end" will be that + some value based on magnitude
                    // 18000000 = 30 minutes, so a quake of magnitude 5 would show on the
                    // map for 150 minutes or 2.5 hours
                    return {
                        start: quake.properties.time,
                        // 修改持续的时间片为 1h
                        end: quake.properties.time + quake.properties.mag * 1800000 * 2 * 10,
                    };
                };
                let timelineControl = L.timelineSliderControl({
                    formatOutput: function (date) {
                        return new Date(date).toLocaleDateString();
                    },
                });
                let timeline = L.timeline(data, {
                    getInterval: getInterval,
                    pointToLayer: function (data, latlng) {
                        let hue_min = 120;
                        let hue_max = 0;
                        let hue =
                            (data.properties.mag / 10) * (hue_max - hue_min) + hue_min;
                        return L.circleMarker(latlng, {
                            // 通过持续的时间来控制 半径的大小
                            radius: data.properties.mag * 3,
                            color: "hsl(" + hue + ", 100%, 50%)",
                            fillColor: "hsl(" + hue + ", 100%, 50%)",
                        }).bindPopup(
                            // '<a href="' + data.properties.url + '">click for more info</a>'
                            '<h3>' + data.properties.day + '</h3>' +
                            '<h1>' + data.properties.title + '</h1>'
                        );
                    },
                });

                console.log('timeline', timeline, 'timelineControl is ', typeof timelineControl)
                /*将事件滑块加入到map中*/
                timelineControl.addTo(map);
                timelineControl.addTimelines(timeline);
                /*将坐标点加入到地图*/
                timeline.addTo(map);
                let vm = this
                timeline.on("change", function (e) {
                    vm.updateList(e.target, map);
                });
                this.updateList(timeline, map);
            }
        },
        mounted() {
            // console.log(jsonData)
            // this.eqfeed_callback(jsonData)

            console.log('kkk',diarymapData,earthQuake)
            var map = new AMap.Map('container', {
                mapStyle: 'amap://styles/db9efe6a1745ac24b7269b862f359536',
                // mapStyle: 'amap://styles/twilight',
                pitch: 10,  //设置地图俯仰角
                resizeEnable: true,
                center: [114.3330555556,30.6697222222],
                zoom: 15,
                features: ['bg', 'road'],
                viewMode: '3D'
            });
            map.addControl(new AMap.MapType({
                defaultType: 1 //0代表默认，1代表卫星
            }));
            var layer = new Loca.PointLayer({
                eventSupport: true,
                map: map
            });

            layer.on('mousemove', function (ev) {
                // 事件类型
                var type = ev.type;
                // 当前元素的原始数据
                var rawData = ev.rawData;
                // 原始鼠标事件
                var originalEvent = ev.originalEvent;

                openInfoWin(map, originalEvent, {
                    // '名称': rawData.lng,
                    // '位置': rawData.lat,
                    '时间': rawData.day,
                    '干了啥': rawData.title,
                });
            });

            layer.on('mouseleave', function (ev) {
                closeInfoWin();
            });

            layer.setData(diarymapData.diarymapData, {
                lnglat: function (data) {
                    var item = data.value;
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

            layer.render();
        }
    }
</script>

<style>
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

    #info {
        position: fixed;
        top: 0;
        left: 0;
        bottom: 0;
        width: 20vw;
        padding: 1em;
    }

    #map {
        position: fixed;
        top: 0;
        left: 20vw;
        bottom: 0;
        right: 0;
    }

    .leaflet-bottom.leaflet-left {
        width: 100%;
    }

    .leaflet-control-container .leaflet-timeline-controls {
        box-sizing: border-box;
        width: 100%;
        margin: 0;
        margin-bottom: 15px;
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
