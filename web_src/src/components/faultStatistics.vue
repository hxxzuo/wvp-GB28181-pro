<template>
  <div id="channelList" style="width: 100%">
    <div class="page-header">
      <div class="page-title">
        <el-button icon="el-icon-back" size="mini" style="font-size: 20px; color: #000;" type="text"
                   @click="show"></el-button>
        <el-divider direction="vertical"></el-divider>
        故障统计
      </div>
      <div class="page-header-btn">
        <div style="display: inline;">
          名称搜索:
          <el-input @input="search" style="margin-right: 1rem; width: auto;" size="mini" placeholder="关键字"
                    prefix-icon="el-icon-search" v-model="searchSrt" clearable></el-input>

        </div>
        <el-button icon="el-icon-refresh-right" circle size="mini" @click="refresh()"></el-button>
        <el-button type="primary" size="mini" @click="openAddDialog">新增故障</el-button>
      </div>
    </div>
    <el-container v-loading="isLoading" style="height: 82vh;">
      <el-main style="padding: 5px;">
        <el-table size="medium" ref="scheduleTable" :data="scheduleList" :height="winHeight" style="width: 100%"
                  header-row-class-name="table-header">
          <el-table-column prop="id" label="id" min-width="100"></el-table-column>
          <el-table-column prop="name" label="基站名称" min-width="100"></el-table-column>
          <el-table-column prop="faultStartTime" label="故障开始时间" min-width="100"></el-table-column>
          <el-table-column prop="faultEndTime" label="故障结束时间" min-width="100"></el-table-column>
          <el-table-column label="操作" min-width="200" fixed="right">
            <template slot-scope="scope">
              <el-button size="mini" @click="openEditDialog(scope.row)">编辑</el-button>
              <el-button size="mini" type="danger" @click="deleteSchedule(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          style="text-align: right"
          @size-change="handleSizeChange"
          @current-change="currentChange"
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next"
          :total="total">
        </el-pagination>
      </el-main>
    </el-container>

    <!-- 新增/编辑计划弹窗 -->
    <el-dialog :title="isEdit ? '编辑' : '新增'" :visible.sync="dialogVisible" width="500px">
      <el-form ref="scheduleForm" :model="currentSchedule" :rules="rules" label-width="100px">
        <el-form-item label="id" prop="id">
          <el-input v-model="currentSchedule.id" placeholder="" disabled></el-input>
        </el-form-item>
        <el-form-item label="基站名称" prop="name">
          <el-input v-model="currentSchedule.name" placeholder="请输入名称"></el-input>
        </el-form-item>
        <el-form-item label="故障开始时间" prop="period">
          <el-input v-model="currentSchedule.faultStartTime" placeholder="请输入开始时间"></el-input>
        </el-form-item>
        <el-form-item label="故障结束时间" prop="period">
          <el-input v-model="currentSchedule.faultEndTime" placeholder="请输入结束时间"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSchedule">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import moment from "moment/moment";

export default {
  name: 'checkSchedule',
  data() {
    return {
      scheduleList: [],
      searchSrt: null,
      winHeight: window.innerHeight - 200,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      isLoading: false,
      dialogVisible: false, // 控制新增/编辑弹窗的显示
      isEdit: false, // 是否为编辑模式
      currentSchedule: { // 当前编辑/新增的计划表单模型
        id: null,
        name: '',
        faultStartTime: '',
        faultEndTime: '',
      },
      rules: { // 表单验证规则
        name: [{required: true, message: '请输入计划名称', trigger: 'blur'}],
        faultStartTime: [{required: true, message: '请输入开始时间', trigger: 'blur'}],
      },
      adminOptions: [
        { value: '广州航标管理站', label: '广州航标管理站' },
        { value: '盐田航标管理站', label: '盐田航标管理站' },
        { value: '蛇口航标管理站', label: '蛇口航标管理站' },
        { value: '南沙航标管理站', label: '南沙航标管理站' },
        { value: '江门航标管理站', label: '江门航标管理站' },
        { value: '阳江航标管理站', label: '阳江航标管理站' },
      ],
    };
  },
  methods: {
    initData() {
      this.getScheduleList();
    },
    getScheduleList() {
      this.isLoading = true;
      this.$axios({
        method: 'post',
        url: '/api/v1/device/faultList',
        data: {
          page: this.currentPage,
          count: this.pageSize,
          name: this.searchSrt,
        },
      }).then((res) => {
        if (res.status === 200) {
          this.scheduleList = res.data.list;
          this.total = res.data.total;
          this.$nextTick(() => {
            this.$refs.scheduleTable.doLayout();
          });
        }
      }).catch((err) => {
        console.error(err);
      }).finally(() => {
        this.isLoading = false;
      });
    },
    currentChange(val) {
      this.currentPage = val;
      this.initData();
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.initData();
    },
    search() {
      this.currentPage = 1;
      this.initData();
    },
    refresh() {
      this.searchSrt = '';
      this.initData();
    },
    show() {
      this.initParam();
      this.initData();
    },
    deleteSchedule(row) {
      this.$confirm('确认删除该?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        this.$axios({
          method: 'delete',
          url: `/api/v1/device/fault/delete/${row.id}`,
        }).then(() => {
          this.$message.success('删除成功');
          this.initData();
        }).catch((err) => {
          console.error(err);
          this.$message.error('删除失败');
        });
      }).catch(() => {
        this.$message.info('取消删除');
      });
    },
    openAddDialog() {
      this.resetCurrentSchedule();
      this.isEdit = false;
      this.dialogVisible = true;
    },
    openEditDialog(row) {
      this.currentSchedule = {...row};
      this.currentSchedule.administrators = typeof this.currentSchedule.administrators === 'string'
        ? this.currentSchedule.administrators.split(',')
        : Array.isArray(this.currentSchedule.administrators)
          ? this.currentSchedule.administrators
          : []
      this.isEdit = true;
      this.dialogVisible = true;
    },
    saveSchedule() {
      this.$refs.scheduleForm.validate((valid) => {
        if (valid) {
          console.log(this.currentSchedule)
          const form = this.currentSchedule
          if (Array.isArray(form.administrators)){
            form.administrators = form.administrators.join(',')
            console.log(this.currentSchedule.administrators)
          }

          const url = this.isEdit
            ? `/api/v1/device/fault/update/${this.currentSchedule.id}`
            : '/api/v1/device/fault/add';
          const method = this.isEdit ? 'put' : 'post';

          this.$axios({
            method,
            url,
            data: form,
          }).then(() => {
            this.$message.success(this.isEdit ? '更新成功' : '新增成功');
            this.dialogVisible = false;
            this.initData();
          }).catch((err) => {
            console.error(err);
            this.$message.error(this.isEdit ? '更新失败' : '新增失败');
          });
        }
      });
    },
    resetCurrentSchedule() {
      this.currentSchedule = {
        id: null,
        name: '',
        fault_start_time: '',
        fault_end_time: '',
      };
    }

  },
  mounted() {
    this.initData();
    if (this.isEdit) {

    }
  },
};
</script>

<style>
/* 样式根据需求调整 */
</style>
