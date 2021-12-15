<template>
    <div>
        <div id="info">
            <h1 id="day"></h1>
            <h2 id="title"></h2>
            <ul id="displayed-list"></ul>
        </div>
        <div id="map" style="width: 50%;"></div>
    </div>

</template>

<script>
    import jsonData from '../../data/traceData.json'
    export default {
        name: "timeLine",
        methods:{
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

            timeLineView(data) {
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
            },
        },
        mounted() {
            // console.log(jsonData)
            this.timeLineView(jsonData)

        }
    }
</script>

<style scoped>
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

</style>