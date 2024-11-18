<template>
  <div id="channelList" style="width: 100%">
    <div class="page-header">
      <div class="page-title">
        <el-button icon="el-icon-back" size="mini" style="font-size: 20px; color: #000;" type="text"
                   @click="showDevice"></el-button>
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
        <el-table size="medium" ref="channelListTable" :data="deviceChannelList" :height="winHeight" style="width: 100%"
                  header-row-class-name="table-header">
          <el-table-column prop="name" label="名称" min-width="100">
          </el-table-column>
          <el-table-column prop="address" label="地址" min-width="100">
          </el-table-column>
          <el-table-column prop="type" label="类型" min-width="100">
          </el-table-column>
          <el-table-column label="经纬度" min-width="100">
            <template slot-scope="scope">
              <span size="medium"
                    v-if="scope.row.longitude && scope.row.latitude">{{ scope.row.longitude }}<br/>{{ scope.row.latitude }}</span>
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
          <el-table-column label="操作" min-width="340" fixed="right">
            <template slot-scope="scope">
              <el-divider direction="vertical"></el-divider>
              <el-button size="medium" icon="el-icon-s-open" type="text"
                         @click="changeSubchannel(scope.row)">查询航标附近摄像头
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
      deviceService: new DeviceService(),
      deviceChannelList: [],
      updateLooper: 0, //数据刷新轮训标志
      searchSrt: null,
      type: null,
      winHeight: window.innerHeight - 200,
      currentPage: 1,
      count: 15,
      total: 0,
      isLoging: false,
      showTree: false,
      loadSnap: {},
      ptzTypes: {
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
    initData: function () {
      this.getDeviceChannelList();
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
      this.getDeviceChannelList();
    },
    getDeviceChannelList: function () {
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
          that.deviceChannelList = res.data.list;
          that.$nextTick(() => {
            that.$refs.channelListTable.doLayout();
          })
        }

      }).catch(function (error) {
        console.log(error);
      });
    },
    showDevice: function () {
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
  }
};
</script>

<style>

</style>
