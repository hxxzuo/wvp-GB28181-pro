<template>
  <div id="mapContainer" ref="mapContainer" style="width: 100%;height: 100%;">
    <button @click="switchSource('onlineMap')">在线地图</button>
    <button @click="switchSource('offlineMap')">离线地图</button>
    <button @click="$emit('showAllCameraLocation-event', '')">展示/隐藏所有摄像头</button>
    <button @click="$emit('showAllAtonLocation-event', '')">展示/隐藏所有航标</button>
    <div class="mapInfo">
      <p>缩放等级: {{ Math.round(zoomLevel) }}  经纬度: {{ mouseCoordinates.lon.toFixed(4) }},{{ mouseCoordinates.lat.toFixed(4) }}</p>
    </div>

  </div>

</template>

<script>
import 'ol/ol.css';
import Map from 'ol/Map';
import OSM from 'ol/source/OSM';
import XYZ from 'ol/source/XYZ';
import VectorSource from 'ol/source/Vector';
import Tile from 'ol/layer/Tile';
import VectorLayer from 'ol/layer/Vector';
import Style from 'ol/style/Style';
import Stroke from 'ol/style/Stroke';
import Icon from 'ol/style/Icon';
import Text from 'ol/style/Text';
import View from 'ol/View';
import Feature from 'ol/Feature';
import Overlay from 'ol/Overlay';
import {Point, LineString} from 'ol/geom';
import {get as getProj, fromLonLat, toLonLat} from 'ol/proj';
import {ZoomSlider, Zoom, FullScreen} from 'ol/control';
import {containsCoordinate} from 'ol/extent';
import { watch } from 'vue';
import {v4}  from 'uuid'
let olMap = null;

export default {
    name: 'MapComponent',
    props: ['parentData'],
    data() {
        return {
          zoomLevel: 10,
          mouseCoordinates: { lon: 0, lat: 0 },
          tileLayer: null,
          onLineTileLayerSource : null,
          offlineTileLayerSource : null,
          view: null
        };
    },
    created(){
      this.$nextTick(() => {
        setTimeout(()=>{
          this.init()
        }, 100)
      })

    },
    mounted () {
    },
    methods: {
      init(){
        let center = fromLonLat([116.41020, 39.915119]);
        if (mapParam.center) {
          center = fromLonLat(mapParam.center);
        }
        this.view = new View({
          center: center,
          zoom:  mapParam.zoom || 10,
          projection: this.projection,
          maxZoom: mapParam.maxZoom || 19,
          minZoom: mapParam.minZoom || 1,
        });

        if (mapParam.tilesUrl) {
          this.onLineTileLayerSource =
              new XYZ({
              projection: getProj("EPSG:3857"),
              wrapX: false,
              tileSize: 256 || mapParam.tileSize,
              url: mapParam.tilesUrl
            })
        }
        {
          this.offlineTileLayerSource = new XYZ({
              projection: getProj("EPSG:3857"),
              wrapX: false,
              tileSize: 256 || mapParam.tileSize,
              url: '/static/tile/tiles/{z}/{y}/{x}.png'
            })

        }

        // this.onLineTileLayerSource.getSource().on('tileloaderror', this.handleTileLoadError);

        this.tileLayer = new Tile({
          source: this.onLineTileLayerSource? this.onLineTileLayerSource: this.offlineTileLayerSource,
        })

        olMap = new Map({
          target: this.$refs.mapContainer, // 容器ID
          layers:  [this.tileLayer], // 默认图层
          view: this.view,  // 视图
          controls:[   // 控件
            // new ZoomSlider(),
            new Zoom(),
            new FullScreen(),
          ] ,
        })

        // 监听地图的移动事件，更新缩放等级
        olMap.getView().on('change:resolution', () => {
          this.zoomLevel = olMap.getView().getZoom();
        });

        // 监听地图的鼠标移动事件，更新鼠标光标所在经纬度
        olMap.on('pointermove', (event) => {
          const [lon, lat] = toLonLat(olMap.getEventCoordinate(event.originalEvent));
          this.mouseCoordinates = { lon, lat };
        });

        olMap.on('moveend', () => {
          const zoomLevel = olMap.getView().getZoom(); // 获取当前缩放级别
            parentData.setStyle((style) => {
            console.log("moveend")
            const shouldShowText = zoomLevel > 10; // 控制文字显示的缩放级别
            if (!shouldShowText){
              return style
            }
            if (style.text!= null){
              style.text = null
            }
            return style
          });
        })
      },
      switchSource(sourceType) {
        this.tileLayer.setSource(sourceType==='onlineMap'? this.onLineTileLayerSource: this.offlineTileLayerSource);
        if (sourceType === 'offlineMap'){
          this.view.setMaxZoom(14)
          this.view.setCenter(fromLonLat([113.32020, 23.115119]))
        }else {
          this.view.setMaxZoom(19)
        }
      },
      handleTileLoadError(event) {
        console.log('在线地图加载失败，尝试切换到离线地图...');
        this.switchSource('offlineMap')
      },
      setCenter(point){

      },
      zoomIn(zoom){

      },
      zoomOut(zoom){

      },
      centerAndZoom(point,zoom,callback){
        var zoom_ = olMap.getView().getZoom();
        zoom = zoom|| zoom_;
        var duration = 600;
        olMap.getView().setCenter(fromLonLat(point))
        olMap.getView().animate({
          zoom: zoom ,
          duration: duration
        });
      },
      panTo(point, zoom){
        let duration = 800;

        olMap.getView().cancelAnimations()
        olMap.getView().animate({
          center: fromLonLat(point),
          duration: duration
        });
        if (!containsCoordinate(olMap.getView().calculateExtent(), fromLonLat(point))) {
          olMap.getView().animate({
            zoom: olMap.getView().getZoom() - 1,
            duration: duration / 2
          }, {
            zoom: zoom || olMap.getView().getZoom(),
            duration: duration / 2
          });
        }

      },
      fit(layer){
        let extent = layer.getSource().getExtent();
        if (extent) {
          olMap.getView().fit(extent,{
            duration : 600,
            padding: [100, 100, 100, 100]
          });
        }


      },
      openInfoBox(position, content, offset){
        let id = v4()
        // let infoBox = document.createElement("div");
        // infoBox.innerHTML = content ;
        // infoBox.setAttribute("infoBoxId", id)
        let overlay = new Overlay({
          id:id,
          autoPan:true,
          autoPanAnimation:{
            duration: 250
          },
          element: content,
          positioning:"bottom-center",
          offset:offset,
          // className:overlayStyle.className
        });
        olMap.addOverlay(overlay);
        overlay.setPosition(fromLonLat(position));
        return id;
      },
      closeInfoBox(id){
        olMap.getOverlayById(id).setPosition(undefined)
        // olMap.removeOverlay(olMap.getOverlayById(id))
      },
      /**
       * 添加图层
       * @param data
       * [
       *     {
       *
       *         position: [119.1212,45,122],
       *             image: {
       *                 src:"/images/123.png",
       *                 anchor: [0.5, 0.5]
       *
       *             }
       *     }
       *
       * ]
       */
      addLayer(data, clickEvent){
        let style = new Style();
        if (data.length > 0) {
          let features = [];
          for (let i = 0; i < data.length; i++) {
            let feature = new Feature(new Point(fromLonLat(data[i].position)));
            feature.customData = data[i].data;
            let iconStyle = new Icon({
              anchor: data[i].image.anchor,
              crossOrigin: 'Anonymous',
              src: data[i].image.src,
            });

            let textStyle = new Text({
              text: data[i].label.text || '',
              offsetX: data[i].label.offset[0] || 0,
              offsetY: data[i].label.offset[1] || -30,
              font: data[i].label.font || '12px Arial',
              // fill: new Fill({ color: data[i].label.fill || 'black' }),
              stroke: new Stroke({ color: data[i].label.stroke || 'white', width: data[i].label.strokeWidth || 2 }),
            });

            let featureStyle = new Style({
              image: iconStyle, text: textStyle,
            });
            feature.setStyle(featureStyle)
            features.push(feature);
          }
          let source = new VectorSource();
          source.addFeatures(features);
          let vectorLayer = new VectorLayer({
            source:source,
            style:style,
            renderMode:"vector",
            declutter: false
          })
          olMap.addLayer(vectorLayer)

          if (typeof clickEvent == "function") {
            console.log("clickEvent")
            olMap.on("click", (event)=>{
              vectorLayer.getFeatures(event.pixel).then((features)=>{
                if (features.length > 0) {
                  let items = []
                  for (let i = 0; i < features.length; i++) {
                    items.push(features[i].customData)
                  }
                  clickEvent(items)
                }
              })
            })
          }
          return vectorLayer;
        }
      },
      removeLayer(layer){
        olMap.removeLayer(layer)
      },

      addLineLayer(positions) {
        if (positions.length > 0) {
          let points = [];
          for (let i = 0; i < positions.length; i++) {
            points.push(fromLonLat(positions[i]));
          }
          let line = new LineString(points)
          let lineFeature = new Feature(line);
          lineFeature.setStyle(new Style({
            stroke: new Stroke({
              width: 4 ,
              color: "#0c6d6a",
            })
          }))
          let source = new VectorSource();
          source.addFeature(lineFeature);
          let vectorLayer = new VectorLayer({
            source: source,
          })
          olMap.addLayer(vectorLayer)
          return vectorLayer;
        }
      }
    },
    destroyed() {
      // if (this.jessibuca) {
      //   this.jessibuca.destroy();
      // }
      // this.playing = false;
      // this.loaded = false;
      // this.performance = "";
    },
}
</script>

<style>
.mapInfo {
  position: absolute;
  bottom: 10px;
  left: 35%;
  transform: translateX(-35%);
  background-color: rgba(255, 255, 255, 0.1);
  padding: 2px 3px;
  border-radius: 2px;
  font-size: 13px;
  z-index: 10; /* 确保 .mapInfo 在地图上方 */
}
</style>
