<template>
  <div id="channelList" style="width: 100%">
    <div class="page-header">
      <div class="page-title">
        <el-button icon="el-icon-back" size="mini" style="font-size: 20px; color: #000;" type="text"
                   @click="show"></el-button>
        <el-divider direction="vertical"></el-divider>
        巡视计划
      </div>
      <div class="page-header-btn">
        <div style="display: inline;">
          名称搜索:
          <el-input @input="search" style="margin-right: 1rem; width: auto;" size="mini" placeholder="关键字"
                    prefix-icon="el-icon-search" v-model="searchSrt" clearable></el-input>

        </div>
        <el-button icon="el-icon-refresh-right" circle size="mini" @click="refresh()"></el-button>
        <el-button type="primary" size="mini" @click="openAddDialog">新增计划</el-button>
      </div>
    </div>
    <el-container v-loading="isLoading" style="height: 82vh;">
      <el-main style="padding: 5px;">
        <el-table size="medium" ref="scheduleTable" :data="scheduleList" :height="winHeight" style="width: 100%"
                  header-row-class-name="table-header">
          <el-table-column prop="id" label="id" min-width="100"></el-table-column>
          <el-table-column prop="name" label="计划名称" min-width="100"></el-table-column>
          <el-table-column prop="period" label="执行周期" min-width="100">
            <template slot-scope="scope">
              <span>{{ scope.row.period }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="administrators" label="航标站" min-width="100">
            <template slot-scope="scope">
              <span>{{ scope.row.administrators.split(',') }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" min-width="100">
            <template slot-scope="scope">
              <el-tag type="success" v-if="scope.row.status === 1">启用</el-tag>
              <el-tag type="danger" v-else>禁用</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级" min-width="100"></el-table-column>
          <el-table-column prop="checkStructure" label="检查结构" min-width="100">
            <template slot-scope="scope">
              <el-tag type="success" v-if="scope.row.checkStructure === 1">是</el-tag>
              <el-tag type="danger" v-else>否</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="checkColor" label="检查涂色" min-width="100">
            <template slot-scope="scope">
              <el-tag type="success" v-if="scope.row.checkColor === 1">是</el-tag>
              <el-tag type="danger" v-else>否</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="checkLight" label="检查灯光" min-width="100">
            <template slot-scope="scope">
              <el-tag type="success" v-if="scope.row.checkLight === 1">是</el-tag>
              <el-tag type="danger" v-else>否</el-tag>
            </template>
          </el-table-column>
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
    <el-dialog :title="isEdit ? '编辑计划' : '新增计划'" :visible.sync="dialogVisible" width="500px">
      <el-form ref="scheduleForm" :model="currentSchedule" :rules="rules" label-width="100px">
        <el-form-item label="id" prop="id">
          <el-input v-model="currentSchedule.id" placeholder="" disabled></el-input>
        </el-form-item>
        <el-form-item label="计划名称" prop="name">
          <el-input v-model="currentSchedule.name" placeholder="请输入计划名称"></el-input>
        </el-form-item>
        <el-form-item label="执行周期" prop="period">
          <el-input v-model="currentSchedule.period" placeholder="请输入执行周期"></el-input>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input v-model="currentSchedule.priority" placeholder="请输入计划优先级"
                    @input="currentSchedule.priority = parseInt(currentSchedule.priority) || 0"></el-input>
        </el-form-item>
        <el-form-item label="航标站列表" prop="administrators">
          <el-select v-model="currentSchedule.administrators" multiple filterable>
            <el-option
              v-for="admin in adminOptions"
              :key="admin.value"
              :label="admin.label"
              :value="admin.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="currentSchedule.status" placeholder="请选择状态">
            <el-option label="启用" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="检查内容" prop="checkOptions">
          <el-checkbox
            v-model="currentSchedule.checkLight"
            :true-label="1"
            :false-label="0"
            @change="handleCheckChange('light')"
          >灯光检查</el-checkbox>
          <el-checkbox
            v-model="currentSchedule.checkColor"
            :true-label="1"
            :false-label="0"
            @change="handleCheckChange('color')"
            :disabled="currentSchedule.checkLight === 1"
          >颜色检查</el-checkbox>
          <el-checkbox
            v-model="currentSchedule.checkStructure"
            :true-label="1"
            :false-label="0"
            @change="handleCheckChange('structure')"
            :disabled="currentSchedule.checkLight === 1"
          >结构检查</el-checkbox>
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
        period: '',
        administrators: [],
        status: 0,
        checkColor: 0,
        checkStructure: 0,
        checkLight: 0,
        priority: 0,
      },
      rules: { // 表单验证规则
        name: [{required: true, message: '请输入计划名称', trigger: 'blur'}],
        period: [{required: true, message: '请输入执行周期', trigger: 'blur'}],
        // administrators: [{required: true, message: '请输入航标站', trigger: 'blur'}],
        status: [{required: true, message: '请选择状态', trigger: 'change'}],
        priority: [{required: true, message: '请输入优先级', trigger: 'blur'}],
        checkOptions: [
          {
            validator: (rule, value, callback) => {
              if (
                !this.currentSchedule.checkLight &&
                !this.currentSchedule.checkColor &&
                !this.currentSchedule.checkStructure
              ) {
                callback(new Error('至少选择一种检查内容'));
              } else {
                callback();
              }
            },
            trigger: 'change',
          },
        ],
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
    // 检查互斥逻辑
    handleCheckChange(type) {
      if (type === 'light' && this.currentSchedule.checkLight === 1) {
        this.currentSchedule.checkColor = 0;
        this.currentSchedule.checkStructure = 0;
      }
    },
    getScheduleList() {
      this.isLoading = true;
      this.$axios({
        method: 'post',
        url: '/api/v1/device/schedule/list',
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
      this.$confirm('确认删除该计划?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        this.$axios({
          method: 'delete',
          url: `/api/v1/device/schedule/delete/${row.id}`,
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
            ? `/api/v1/device/schedule/update/${this.currentSchedule.id}`
            : '/api/v1/device/schedule/add';
          const method = this.isEdit ? 'put' : 'post';

          this.$axios({
            method,
            url,
            data: form,
          }).then(() => {
            this.$message.success(this.isEdit ? '更新计划成功' : '新增计划成功');
            this.dialogVisible = false;
            this.initData();
          }).catch((err) => {
            console.error(err);
            this.$message.error(this.isEdit ? '更新计划失败' : '新增计划失败');
          });
        }
      });
    },
    resetCurrentSchedule() {
      this.currentSchedule = {
        id: null,
        name: '',
        period: '',
        administrators: [],
        status: null,
        checkLight: 0,
        checkColor: 0,
        checkStructure: 0
      };
    }

  },
  mounted() {
    this.initData();
    if (this.isEdit) {
      this.currentSchedule.administrators = this.currentSchedule.administrators
        ? this.currentSchedule.administrators.split(',') // 转为数组
        : [];
    }
  },
};
</script>

<style>
/* 样式根据需求调整 */
</style>
