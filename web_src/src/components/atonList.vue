<template>
  <div id="channelList" style="width: 100%">
    <div class="page-header">
      <div class="page-title">
        <el-button icon="el-icon-back" size="mini" style="font-size: 20px; color: #000;" type="text"
                   @click="showAton"></el-button>
        <el-divider direction="vertical"></el-divider>
        航标列表
      </div>
      <div class="page-header-btn">
        <div style="display: inline;">
          名称搜索:
          <el-input @input="search" style="margin-right: 1rem; width: auto;" size="mini" placeholder="关键字"
                    prefix-icon="el-icon-search" v-model="searchSrt" clearable></el-input>

          种类:
          <el-select size="mini" @change="search" style="width: 8rem; margin-right: 1rem;" v-model="type"
                     placeholder="请选择"
                     default-first-option>
            <el-option label="全部" value=""></el-option>
            <el-option label="AIS航标" value="AIS航标"></el-option>
            <el-option label="AIS基站" value="AIS基站"></el-option>
            <el-option label="AIS中心" value="AIS中心"></el-option>
            <el-option label="大型显示灯牌" value="大型显示灯牌"></el-option>
            <el-option label="导标" value="导标"></el-option>
            <el-option label="灯船" value="灯船"></el-option>
            <el-option label="灯浮标" value="灯浮标"></el-option>
            <el-option label="灯塔" value="灯塔"></el-option>
            <el-option label="灯桩" value="灯桩"></el-option>
            <el-option label="电雾号" value="电雾号"></el-option>
            <el-option label="管线标" value="管线标"></el-option>
            <el-option label="雷达应答器" value="雷达应答器"></el-option>
            <el-option label="雷达指向标" value="雷达指向标"></el-option>
            <el-option label="立标" value="立标"></el-option>
            <el-option label="桥梁标志" value="桥梁标志"></el-option>
          </el-select>
        </div>
        <el-button icon="el-icon-refresh-right" circle size="mini" @click="refresh()"></el-button>
      </div>
    </div>
    <el-container v-loading="isLoging" style="height: 82vh;">
      <el-main style="padding: 5px;">
        <el-table size="medium" ref="channelListTable" :data="atonList" :height="winHeight" style="width: 100%"
                  header-row-class-name="table-header">
          <el-table-column prop="id" label="id" min-width="100">
          </el-table-column>
          <el-table-column prop="name" label="名称" min-width="100">
          </el-table-column>
          <el-table-column prop="address" label="地址" min-width="100">
          </el-table-column>
          <el-table-column prop="type" label="类型" min-width="100">
          </el-table-column>
          <el-table-column label="经纬度" min-width="100">
            <template slot-scope="scope">
              <span size="medium"
                    v-if="scope.row.longitude && scope.row.latitude">{{ scope.row.longitude.toFixed(6) }}<br/>{{
                  scope.row.latitude.toFixed(6)
                }}</span>
              <span size="medium" v-if="!scope.row.longitude || !scope.row.latitude">无</span>
            </template>
          </el-table-column>
          <el-table-column prop="attribute" label="航标性质" min-width="100">
          </el-table-column>
          <el-table-column prop="administer" label="管辖单位" min-width="100">
          </el-table-column>
          <el-table-column prop="maintenance" label="维护单位" min-width="100">
          </el-table-column>
          <el-table-column prop="belong" label="所属单位" min-width="100">
          </el-table-column>
          <el-table-column prop="waters" label="水域" min-width="100">
          </el-table-column>
          <el-table-column label="操作" min-width="240" fixed="right">
            <template slot-scope="scope">
              <el-divider direction="vertical"></el-divider>
              <!-- 将按钮作为 popover 的触发元素 -->
              <el-popover
                ref="popover"
                placement="right"
                width="1000"
                trigger="click"
              >
                <div v-if="popoverContent">
                  <el-table :data="popoverContent" style=" max-height: 400px; overflow: auto;">
                    <el-table-column prop="gbName" label="名称" width="300px"></el-table-column>
                    <el-table-column prop="gbPtzType" label="类型" show-overflow-tooltip>
                      <template slot-scope="scope">
                        {{ typeMapping(scope.row.gbPtzType) }}
                      </template>
                    </el-table-column>
                    <el-table-column prop="toAtonDistance" label="距离(米)" show-overflow-tooltip></el-table-column>
                    <el-table-column prop="relativeAtonDirection" label="相对航标方位"
                                     show-overflow-tooltip></el-table-column>
                    <el-table-column prop="atonPresetLocation" label="摄像头预设点"
                                     show-overflow-tooltip></el-table-column>
                    <el-table-column label="操作" show-overflow-tooltip>
                      <template slot-scope="scope">
                        <el-button type="text" @click="play(scope.row)">播放</el-button>
                        <el-button type="text" @click="openPresetDialog(scope.row)">标记预设点</el-button>
                      </template>
                    </el-table-column>
                  </el-table>

                </div>
                <el-button
                  slot="reference"
                  size="medium"
                  icon="el-icon-s-open"
                  type="text"
                  @click="checkAtonCameraList(scope.row)"
                >
                  查询附近摄像头
                </el-button>
              </el-popover>
              <el-button
                size="medium"
                icon="el-icon-s-open"
                type="text"
                @click="locate(scope.row)"
              >
                地图定位
              </el-button>
            </template>


          </el-table-column>
        </el-table>
        <el-pagination
          style="text-align: right"
          @size-change="handleSizeChange"
          @current-change="currentChange"
          :current-page="currentPage"
          :page-size="count"
          :page-sizes="[15, 25, 35, 50]"
          layout="total, sizes, prev, pager, next"
          :total="total">
        </el-pagination>
      </el-main>
    </el-container>
    <devicePlayer ref="devicePlayer"></devicePlayer>
    <!-- 弹出对话框 -->
    <el-dialog
      title="标记预设点"
      :visible.sync="presetDialogVisible"
      width="400px"
      @close="resetPresetDialog">
      <div>
        <el-form :model="presetForm" label-width="80px">
          <el-form-item label="预设点">
            <el-input v-model="presetForm.presetLocation" placeholder="请输入预设点"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
                      <el-button @click="resetPresetDialog">取消</el-button>
                      <el-button type="primary" @click="submitPreset">确认</el-button>
                    </span>
    </el-dialog>
  </div>
</template>

<script>
import devicePlayer from './dialog/devicePlayer.vue'
import uiHeader from '../layout/UiHeader.vue'
import DeviceService from "./service/DeviceService";
import DeviceTree from "./common/DeviceTree";
import ChannelEdit from "./ChannelEdit";

export default {
  name: 'aton',
  components: {
    devicePlayer,
    uiHeader,
    DeviceTree,
    ChannelEdit,
  },
  data() {
    return {
      presetDialogVisible: false, // 控制对话框显示
      presetForm: {
        atonId: null,
        presetLocation: '', // 预设点
        rowData: null
      },
      popoverContent: null, // 存储弹出框内容
      cameraTotal: 0,
      deviceService: new DeviceService(),
      atonList: [],
      updateLooper: 0, //数据刷新轮训标志
      searchSrt: null,
      type: null,
      winHeight: window.innerHeight - 200,
      currentPage: 1,
      count: 15,
      total: 0,
      checkRadius: 500,
      isLoging: false,
      showTree: false,
      loadSnap: {},
      ptzType: {
        0: "未知",
        1: "球机",
        2: "半球",
        3: "固定枪机",
        4: "遥控枪机"
      }
    };
  },

  mounted() {
    this.initData();
  },
  destroyed() {
    clearTimeout(this.updateLooper);
  },
  methods: {
    // 显示标记预设点对话框
    openPresetDialog(row) {
      console.log(row)
      this.presetForm.rowData = row; // 保存当前行数据
      this.presetForm.presetLocation = ''; // 清空输入框
      this.presetDialogVisible = true; // 显示对话框
    },
    // 重置对话框数据
    resetPresetDialog() {
      this.presetForm.atonId = '';
      this.presetForm.presetLocation = '';
      this.presetForm.rowData = '';
      this.presetDialogVisible = false
    },
    typeMapping(value) {
      return this.ptzType[value] || '未知类型';
    },
    initData: function () {
      this.getAtonList();
    },
    initParam: function () {
      this.currentPage = 1;
      this.count = 15;
    },
    currentChange: function (val) {
      this.currentPage = val;
      this.initData();
    },
    handleSizeChange: function (val) {
      this.count = val;
      this.getAtonList();
    },
    getAtonList: function () {
      let that = this;
      this.$axios({
        method: 'post',
        url: `/api/v1/device/atonlist`,
        data: {
          page: that.currentPage,
          count: that.count,
          type: that.type,
          name: that.searchSrt,
        }
      }).then(function (res) {
        console.log(res)
        if (res.status === 200) {
          that.total = res.data.total;
          that.atonList = res.data.list;
          that.$nextTick(() => {
            that.$refs.channelListTable.doLayout();
          })
        }
      }).catch(function (error) {
        console.log(error);
      });
    },
    showAton: function () {
      this.initParam();
      this.initData();
    },
    search: function () {
      this.currentPage = 1;
      this.total = 0;
      this.initData();
    },
    refresh: function () {
      this.initParam();
      this.initData();
    },
    submitPreset: function () {
      console.info("submitPreset:"+JSON.stringify(this.presetForm.rowData))
      this.$axios({
        method: 'post',
        url: this.presetForm.rowData.atonPresetLocationId === null? '/api/v1/device/addSubmitPreset':'/api/v1/device/updateSubmitPreset',
        data: {
          deviceId: this.presetForm.rowData.gbParentId,
          channelId: this.presetForm.rowData.gbDeviceId,
          atonId: this.presetForm.atonId,
          presetLocation: this.presetForm.presetLocation,
          id: this.presetForm.rowData.atonPresetLocationId
        }
      }).then(function (res) {

      }).catch(function (e) {
        console.error(e)
      });
    },
    play: function (channel) {
      console.info("play: " + JSON.stringify(channel))
      let deviceId = channel.parentId;
      let channelId = channel.deviceId;
      console.log("通知设备推流1：" + deviceId + " : " + channelId);
      let that = this;
      this.$axios({
        method: 'get',
        url: '/api/play/start/' + deviceId + '/' + channelId
      }).then(function (res) {
        if (res.data.code === 0) {
          that.$refs.devicePlayer.openDialog("media", deviceId, channelId, {
            streamInfo: res.data.data,
            hasAudio: channel.hasAudio
          });
        } else {
          console.log("play error")
        }
      }).catch(function (e) {
        console.error(e)
      });
    },
    checkAtonCameraList: function (row) {
      this.presetForm.atonId = row.id;
      this.popoverContent = null;
      console.log("checkAtonCameraList")
      this.$axios({
        method: 'post',
        url: `/api/v1/device/checkatoncameralist`,
        data: {
          atonId: row.id,
          name: row.name,
          radius: this.checkRadius
        }
      }).then((res) => {
        console.log(res)
        if (res.status === 200) {
          this.popoverContent = res.data.list;
        }
      }).catch(function (error) {
        console.log(error);
      });

    }
  }
};
</script>

<style>

</style>
