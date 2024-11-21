<template>
  <div id="devicePosition" style="width: 100vw; height: 91vh;">
    <el-container v-if="onOff" style="height: 91vh;" v-loading="isLoging">
      <el-aside width="400px" style="background-color: #ffffff">
        <DeviceTree ref="deviceTree" :clickEvent="clickEvent" :contextMenuEvent="contextmenuEventHandler"></DeviceTree>
      </el-aside>
      <el-main style="height: 91vh; padding: 0">
        <MapComponent ref="map" @showAllCameraLocation-event="showAllCameraLocation"
                      @showAllAtonLocation-event="showAllAtonLocation" :parentData="this.layer"></MapComponent>
      </el-main>
    </el-container>
    <div v-if="!onOff" style="width: 100%; height:100%; text-align: center; line-height: 5rem">
      <p>地图功能已关闭</p>
    </div>
    <div ref="infobox" v-if="channel != null ">
      <div v-if="channel != null" class="infobox-content">
        <el-descriptions class="margin-top" :title="channel.gbName" :column="1" :colon="true" size="mini"
                         :labelStyle="labelStyle">
          <el-descriptions-item label="平台编号">{{ channel["gbParentId"] }}</el-descriptions-item>
          <el-descriptions-item label="通道编号">{{ channel["gbDeviceId"] }}</el-descriptions-item>
          <el-descriptions-item label="型号">{{ channel["gbModel"] }}</el-descriptions-item>
          <el-descriptions-item label="经纬度">{{ channel["gbLongitude"] }},{{
              channel["gbLatitude"]
            }}
          </el-descriptions-item>
          <el-descriptions-item label="生产厂商">{{ channel["gbManufacturer"] }}</el-descriptions-item>
          <el-descriptions-item label="安装地址">{{ channel["gbAddress"] }}</el-descriptions-item>
          <el-descriptions-item label="网络地址">{{
              channel["gbIpAddress"] + ":" + channel["gbPort"]
            }}
          </el-descriptions-item>
          <!--          <el-descriptions-item label="安装地址">{{ channel.address == null ? '未知' : channel.address }}</el-descriptions-item>-->
          <el-descriptions-item label="云台类型">{{ ptzTypeMapping[channel["gbPtzType"]] }}</el-descriptions-item>
          <el-descriptions-item label="通道状态">
            <el-tag size="small" v-if="channel['gbStatus'] === 'ON'">在线</el-tag>
            <el-tag type="danger" size="small" v-if="channel['gbStatus'] === 'OFF'">离线</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div style="padding-top: 10px">
          <el-button v-bind:disabled="channel['gbStatus'] !== 'ON'" type="primary" size="small" title="播放"
                     icon="el-icon-video-play" @click="play(channel)">播放
          </el-button>
          <!--          <el-button type="primary" size="small" title="编辑位置" icon="el-icon-edit"-->
          <!--                     @click="edit(channel)"></el-button>-->
          <!--          <el-button type="primary" size="small" title="轨迹查询" icon="el-icon-map-location"-->
          <!--                     @click="getTrace(channel)"></el-button>-->
        </div>
        <span class="infobox-close el-icon-close" @click="closeInfoBox()"></span>
      </div>
    </div>
    <div ref="atoninfobox" v-if="aton != null ">
      <div v-if="aton != null" class="infobox-content">
        <el-descriptions class="margin-top" :title="aton.name" :column="1" :colon="true" size="mini"
                         :labelStyle="labelStyle">
          <el-descriptions-item label="地址">{{ aton["address"] }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ aton["type"] }}</el-descriptions-item>
          <el-descriptions-item label="航标性质">{{ aton["attribute"] }}</el-descriptions-item>
          <el-descriptions-item label="经纬度">{{ aton["longitude"] }},{{ aton["latitude"] }}</el-descriptions-item>
          <el-descriptions-item label="管辖单位">{{ aton["administer"] }}</el-descriptions-item>
          <el-descriptions-item label="维护单位">{{ aton["maintenance"] }}</el-descriptions-item>
          <el-descriptions-item label="所属单位">{{ aton["belong"] }}</el-descriptions-item>
          <el-descriptions-item label="水域">{{ aton["waters"] }}</el-descriptions-item>
        </el-descriptions>
        <span class="infobox-close el-icon-close" @click="closeAtonInfoBox()"></span>
      </div>
    </div>
    <devicePlayer ref="devicePlayer"></devicePlayer>
    <queryTrace ref="queryTrace"></queryTrace>
  </div>
</template>

<script>
import MapComponent from "./common/MapComponent.vue";
import DeviceService from "./service/DeviceService";
import DeviceTree from "./common/DeviceTree";
import channelMapInfobox from "./dialog/channelMapInfobox";
import devicePlayer from './dialog/devicePlayer.vue'
import queryTrace from './dialog/queryTrace.vue'
import {createEmpty, extend} from "ol/extent";

export default {
  name: "map",
  components: {
    MapComponent,
    DeviceTree,
    channelMapInfobox,
    devicePlayer,
    queryTrace,
  },
  data() {
    return {
      //onOff: typeof window.mapParam !== "undefined" && window.mapParam.enable,
      onOff: true,
      deviceService: new DeviceService(),
      layer: null,
      atonLayer: null,
      lineLayer: null,
      channel: null,
      aton: null,
      device: null,
      infoBoxId: null,
      atonInfoBoxId: null,
      labelStyle: {
        width: "52px"
      },

      isLoging: false,
      longitudeStr: "gbLongitude",
      latitudeStr: "gbLatitude",

      ptzTypeMapping: {
        1: '球机',
        2: '半球',
        3: '固定枪机',
        4: '遥控枪机',
        5: '遥控半球',
        6: '多目设备的全景/拼接通道',
        7: '多目设备的分割通道'
      }
    };

  },
  created() {
    if (this.$route.query.deviceId) {
      console.log(this.$route.query.deviceId)
      // this.$refs.deviceTree.openByDeivceId(this.$route.query.deivceId)
      setTimeout(() => { // 延迟以等待地图加载完成 TODO 后续修改为通过是实际这；状态加回调完成
        this.deviceService.getAllChannel(false, false, this.$route.query.deviceId, this.channelsHandler)
      }, 1000)
    }
    console.log("window.mapParam " + window.mapParam.coordinateSystem)
    if (window.mapParam.coordinateSystem == "GCJ-02") {
      this.longitudeStr = "longitudeGcj02";
      this.latitudeStr = "latitudeGcj02";
    } else if (window.mapParam.coordinateSystem == "WGS84") {
      this.longitudeStr = "longitudeWgs84";
      this.latitudeStr = "latitudeWgs84";
    } else {
      this.longitudeStr = "gbLongitude";
      this.latitudeStr = "gbLatitude";
    }
  },
  destroyed() {

  },
  methods: {
    clickEvent: function (channelId) {
      this.$axios({
        method: 'get',
        url: `/api/common/channel/one`,
        params: {
          id: channelId,
        }
      }).then((res) => {
        if (res.data.code === 0) {
          console.log(res.data.data)
          console.log("119")
          if (!res.data.data.gbLongitude || !res.data.data.gbLatitude) {
            this.$message.error({
              showClose: true,
              message: "位置信息不存在"
            })
          } else {
            if (this.layer != null) {
              this.$refs.map.removeLayer(this.layer);
            }
            this.closeInfoBox()
            this.layer = this.$refs.map.addLayer([{
              position: [res.data.data.gbLongitude, res.data.data.gbLatitude],
              image: {
                src: this.getImageByChannel(res.data.data),
                anchor: [0.5, 1]
              },
              data: res.data.data
            }], this.featureClickEvent)
            this.$refs.map.panTo([res.data.data.gbLongitude, res.data.data.gbLatitude], mapParam.maxZoom)
          }
        }

      }).catch(function (error) {
        console.log(error);
      });
    },
    showAllCameraLocation: function () {
      if (this.layer != null) {
        this.$refs.map.removeLayer(this.layer);
        this.layer = null
        return
      }

      this.$axios({
        method: 'get',
        url: `/api/device/query/devices/allChannel`,
        params: {}
      }).then((res) => {
        if (res.data.code === 0) {
          console.log("172")
          if (this.layer != null) {
            this.$refs.map.removeLayer(this.layer);
          }
          this.closeInfoBox()
          const channels = res.data.data.list;
          const layerData = channels.map((channel) => ({
            position: [channel.gbLongitude, channel.gbLatitude],
            image: {
              src: this.getImageByChannel(channel),
              anchor: [0.5, 1],
            },
            data: channel, // 保存当前元素信息
            label: {
              text: channel.gbName, // 显示名称
              offset: [0, -30], // 文字偏移，确保在图标上方
              font: '12px Calibri', // 字体样式
              fill: 'black', // 文字颜色
              stroke: 'white', // 边框颜色
              strokeWidth: 2, // 边框宽度
            }
          }));

          // 添加所有元素图层
          this.layer = this.$refs.map.addLayer(layerData, this.featureClickEvent);

          // const extent = createEmpty(); // 初始化空边界
          // channels.forEach((channel) => {
          //   const coord = [channel.gbLongitude, channel.gbLatitude]; // OpenLayers 使用 [lon, lat]
          //   extend(extent, coord); // 扩展边界范围
          // });
          // this.$refs.map.getView().fit(extent, { padding: [50, 50] });
        }
      }).catch(function (error) {
        console.log(error);
      });
    },
    showAllAtonLocation: function () {
      if (this.atonLayer != null) {
        this.$refs.map.removeLayer(this.atonLayer);
        this.atonLayer = null
        return
      }
      this.$axios({
        method: 'post',
        url: `/api/v1/device/atonlist`,
        data: {
          page: 1,
          count: 1500,
        }
      }).then((res) => {
        console.log(res)
        if (res.status === 200) {
          if (this.atonLayer != null) {
            this.$refs.map.removeLayer(this.atonLayer);
          }
          this.closeInfoBox()
          const atons = res.data.list;
          const layerData = atons.map((aton) => ({
            position: [aton.longitude, aton.latitude],
            image: {
              src: this.getImageByAton(aton),
              anchor: [0.5, 1],
            },
            data: aton, // 保存当前元素信息
            label: {
              text: aton.name, // 显示名称
              offset: [0, -30], // 文字偏移，确保在图标上方
              font: '12px Calibri', // 字体样式
              fill: 'black', // 文字颜色
              stroke: 'white', // 边框颜色
              strokeWidth: 1, // 边框宽度
            }
          }));

          this.atonLayer = this.$refs.map.addLayer(layerData, this.atonFeatureClickEvent);
        }
      }).catch(function (error) {
        console.log(error);
      });
    },
    contextmenuEventHandler: function (device, event, data, isCatalog) {
      console.log(device)
      console.log("149")
      this.device = device;
      if (data.channelId && !isCatalog) {
        // 点击通道
        this.$contextmenu({
          items: [
            {
              label: "播放",
              icon: "el-icon-video-play",
              disabled: device.online === 0,
              onClick: () => {
                this.play(data);
              }
            },
            {
              label: "编辑位置",
              icon: "el-icon-edit",
              disabled: false,
              onClick: () => {
                this.edit(data)
              }
            },
            {
              label: "轨迹查询",
              icon: "el-icon-map-location",
              disabled: false,
              onClick: () => {
                this.getTrace(data)
              }
            }
          ],
          event, // 鼠标事件信息
          customClass: "custom-class", // 自定义菜单 class
          zIndex: 3000, // 菜单样式 z-index
        });
      } else {
        if (typeof data.channelId === "undefined") {
          this.deviceOrSubChannelMenu(event, data)
        } else {
          // TODO 子目录暂时不支持查询他下面所有设备, 支持支持查询直属于这个目录的设备
          this.deviceOrSubChannelMenu(event, data)
        }

      }

    },
    deviceOrSubChannelMenu: function (event, data) {
      // 点击设备
      this.$contextmenu({
        items: [
          {
            label: "定位",
            icon: "el-icon-s-promotion",
            disabled: false,
            onClick: () => {
              if (!data.channelId) {
                this.deviceService.getAllChannel(false, false, data.deviceId, this.channelsHandler)
              }
              if (data.channelId && data.subCount > 0) {
                // 点击子目录
                this.deviceService.getAllSubChannel(false, data.deviceId, data.channelId, this.channelsHandler)
              }
            }
          },
          {
            label: "查询轨迹",
            icon: "el-icon-map-location",
            disabled: false,
            onClick: () => {
              this.getTrace(data)
            }
          }
        ],
        event, // 鼠标事件信息
        customClass: "custom-class", // 自定义菜单 class
        zIndex: 3000, // 菜单样式 z-index
      });

    },
    channelsHandler: function (channels) {
      console.log(2)
      console.log("229")
      if (channels.length > 0) {
        this.clean()
        this.closeInfoBox()
        let params = [];

        for (let i = 0; i < channels.length; i++) {
          let longitude = channels[i][this.longitudeStr];
          let latitude = channels[i][this.latitudeStr];
          if (longitude * latitude === 0) {
            continue;
          }
          let item = {
            position: [longitude, latitude],
            image: {
              src: this.getImageByChannel(channels[i]),
              anchor: [0.5, 1]
            },
            data: channels[i]
          }
          params.push(item);
        }
        console.log(3)

        this.layer = this.$refs.map.addLayer(params, this.featureClickEvent)
        console.log(4)
        if (params.length === 1) {
          this.$refs.map.panTo([channels[0][this.longitudeStr], channels[0][this.latitudeStr]], mapParam.maxZoom)
        } else if (params.length > 1) {
          this.$refs.map.fit(this.layer)
        } else {
          this.$message.error({
            showClose: true,
            message: "未获取到位置信息"
          })
        }
      } else {
        this.$message.error({
          showClose: true,
          message: "未获取到位置信息"
        })
      }
    },
    getImageByChannel: function (channel) {
      let src = "static/images/gis/camera.png"
      switch (channel.gbPtzType) {
        case 1:
          if (channel.gbStatus === "ON") {
            src = "static/images/gis/camera1.png"
          } else {
            src = "static/images/gis/camera1-offline.png"
          }
          break;
        case 2:
          if (channel.gbStatus === "ON") {
            src = "static/images/gis/camera2.png"
          } else {
            src = "static/images/gis/camera2-offline.png"
          }
          break;
        case 3:
          if (channel.gbStatus === "ON") {
            src = "static/images/gis/camera3.png"
          } else {
            src = "static/images/gis/camera3-offline.png"
          }
          break;
        default:
          if (channel.gbStatus === "ON") {
            src = "static/images/gis/camera.png"
          } else {
            src = "static/images/gis/camera-offline.png"
          }
      }
      return src;
    },
    getImageByAton: function (aton) {
      let src = "static/images/gis/camera.png"
      switch (aton.type) {
        case 1:
            src = "static/images/gis/camera1-offline.png"
          break;
        case 2:
            src = "static/images/gis/camera2-offline.png"
          break;
        case 3:
            src = "static/images/gis/camera3-offline.png"
          break;
        default:
            src = "static/images/aton/1.ico"
      }
      return src;
    },
    featureClickEvent: function (channels) {
      this.closeInfoBox()
      if (channels.length > 0) {
        this.channel = channels[0]
      }
      console.log("featureClickEvent:" + JSON.stringify(this.channel))
      this.$nextTick(() => {
        let position = [this.channel[this.longitudeStr], this.channel[this.latitudeStr]];
        this.infoBoxId = this.$refs.map.openInfoBox(position, this.$refs.infobox, [0, -50])
      })
    },
    atonFeatureClickEvent: function (atons) {
      this.closeAtonInfoBox()
      if (atons.length > 0) {
        this.aton = atons[0]
      }
      console.log("atonFeatureClickEvent:" + JSON.stringify(this.aton))
      this.$nextTick(() => {
        let position = [this.aton['longitude'], this.aton['latitude']];
        this.atonInfoBoxId = this.$refs.map.openInfoBox(position, this.$refs.atoninfobox, [0, -50])
      })
    },
    closeInfoBox: function () {
      if (this.infoBoxId != null) {
        this.$refs.map.closeInfoBox(this.infoBoxId)
      }
    },
    closeAtonInfoBox: function () {
      if (this.atonInfoBoxId != null) {
        this.$refs.map.closeInfoBox(this.atonInfoBoxId)
      }
    },
    play: function (channel) {
      console.info("play: " + JSON.stringify(channel))
      let deviceId = channel.gbParentId;
      this.isLoging = true;
      let channelId = channel.gbDeviceId;
      console.log("通知设备推流1：" + deviceId + " : " + channelId);
      let that = this;
      this.$axios({
        method: 'get',
        url: '/api/play/start/' + deviceId + '/' + channelId
      }).then(function (res) {
        that.isLoging = false;
        if (res.data.code === 0) {
          that.$refs.devicePlayer.openDialog("media", deviceId, channelId, {
            streamInfo: res.data.data,
            hasAudio: channel.hasAudio
          });

        } else {
          that.$message.error(res.data.msg);
        }
      }).catch(function (e) {
      });
    },
    edit: function (data) {
      this.$message.warning({
        showClose: true,
        message: "暂不支持"
      })
    },
    getTrace: function (data) {
      // this.$message.warning('暂不支持');
      this.clean()
      this.$refs.queryTrace.openDialog(data, (channelPositions) => {
        console.log("getTrace")
        console.log(channelPositions)
        if (channelPositions.length === 0) {
          this.$message.info({
            showClose: true,
            message: "未查询到轨迹信息"
          })
        } else {
          let positions = [];
          for (let i = 0; i < channelPositions.length; i++) {
            if (channelPositions[i][this.longitudeStr] * channelPositions[i][this.latitudeStr] > 0) {
              positions.push([channelPositions[i][this.longitudeStr], channelPositions[i][this.latitudeStr]])
            }

          }
          if (positions.length === 0) {
            this.$message.info({
              showClose: true,
              message: "未查询到轨迹信息"
            })
            return;
          }
          this.lineLayer = this.$refs.map.addLineLayer(positions)
          this.$refs.map.fit(this.lineLayer)
        }
      })
    },
    clean: function () {
      if (this.lineLayer != null) {
        this.$refs.map.removeLayer(this.lineLayer)
      }
      if (this.infoBoxId != null) {
        this.$refs.map.closeInfoBox(this.infoBoxId)
      }
      if (this.layer != null) {
        this.$refs.map.removeLayer(this.layer)
      }
    }
  },

};
</script>

<style>
.infobox-content {
  width: 260px;
  background-color: #FFFFFF;
  padding: 5px;
  border-radius: 10px;
  border: 1px solid #e2e2e2;
}

.infobox-content::after {
  position: absolute;
  bottom: -11px;
  left: 130px;
  display: block;
  content: "";
  width: 16px;
  height: 16px;
  background: url('~@static/images/arrow.png') no-repeat center;
}

.infobox-close {
  position: absolute;
  right: 1rem;
  top: 1rem;
  color: #000000;
  cursor: pointer
}

.el-descriptions__title {
  font-size: 1rem;
  font-weight: 700;
  padding: 20px 20px 0px 23px;
  text-align: center;
  width: 100%;
}
</style>
