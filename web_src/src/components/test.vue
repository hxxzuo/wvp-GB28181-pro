<template>
  <div class="check-schedule">
    <el-card>
      <div slot="header" class="clearfix">
        <span>检查计划管理</span>
        <el-button
          type="primary"
          size="mini"
          @click="openDialog('add')">新增计划
        </el-button>
      </div>
      <el-table
        :data="scheduleList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="计划名" />
        <el-table-column prop="period" label="执行周期" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '未启用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="text" @click="openDialog('edit', scope.row)">编辑</el-button>
            <el-button type="text" style="color: red" @click="deleteSchedule(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑/新增对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible">
      <el-form :model="formData" :rules="rules" ref="form">
        <el-form-item label="计划名" prop="name">
          <el-input v-model="formData.name" />
        </el-form-item>
        <el-form-item label="执行周期" prop="period">
          <cron-generator v-model="formData.period" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">未启用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="检查内容" prop="checkOptions">
          <el-checkbox
            v-model="formData.check_light"
            :true-label="1"
            :false-label="0"
            @change="handleCheckChange('light')"
          >灯光检查</el-checkbox>
          <el-checkbox
            v-model="formData.check_color"
            :true-label="1"
            :false-label="0"
            @change="handleCheckChange('color')"
            :disabled="formData.check_light === 1"
          >颜色检查</el-checkbox>
          <el-checkbox
            v-model="formData.check_structure"
            :true-label="1"
            :false-label="0"
            @change="handleCheckChange('structure')"
            :disabled="formData.check_light === 1"
          >结构检查</el-checkbox>
        </el-form-item>
        <el-form-item label="航标站列表" prop="administrators">
          <el-select v-model="formData.administrators" multiple filterable>
            <el-option
              v-for="admin in adminOptions"
              :key="admin.value"
              :label="admin.label"
              :value="admin.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
// 引入 Cron 表达式生成器
import CronGenerator from 'vue-cron-generator';
import 'vue-cron-generator/dist/vcg.css';

export default {
  components: { CronGenerator },
  data() {
    return {
      scheduleList: [], // 数据列表
      adminOptions: [
        { value: 'station1', label: '航标站1' },
        { value: 'station2', label: '航标站2' },
        { value: 'station3', label: '航标站3' },
      ], // 航标站选项
      dialogVisible: false,
      dialogTitle: '新增计划',
      formData: {
        id: null,
        name: '',
        period: '',
        status: 1,
        check_light: 0,
        check_color: 0,
        check_structure: 0,
        administrators: [],
      },
      rules: {
        name: [{ required: true, message: '请输入计划名', trigger: 'blur' }],
        period: [{ required: true, message: '请选择执行周期', trigger: 'blur' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }],
        administrators: [{ required: true, message: '请选择航标站', trigger: 'change' }],
      },
    };
  },
  methods: {
    // 获取检查计划数据
    async fetchScheduleList() {
      const response = await fetch('/api/check_schedule');
      this.scheduleList = await response.json();
    },
    // 打开新增/编辑对话框
    openDialog(type, row = null) {
      if (type === 'add') {
        this.dialogTitle = '新增计划';
        this.formData = { id: null, name: '', period: '', status: 1, check_light: 0, check_color: 0, check_structure: 0, administrators: [] };
      } else if (type === 'edit') {
        this.dialogTitle = '编辑计划';
        this.formData = { ...row };
      }
      this.dialogVisible = true;
    },
    // 检查互斥逻辑
    handleCheckChange(type) {
      if (type === 'light' && this.formData.check_light === 1) {
        this.formData.check_color = 0;
        this.formData.check_structure = 0;
      }
    },
    // 提交表单
    async submitForm() {
      this.$refs.form.validate(async valid => {
        if (!valid) return;
        const method = this.formData.id ? 'PUT' : 'POST';
        const url = this.formData.id
          ? `/api/check_schedule/${this.formData.id}`
          : '/api/check_schedule';
        await fetch(url, {
          method,
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(this.formData),
        });
        this.dialogVisible = false;
        this.fetchScheduleList();
      });
    },
    // 删除检查计划
    async deleteSchedule(id) {
      await fetch(`/api/check_schedule/${id}`, { method: 'DELETE' });
      this.fetchScheduleList();
    },
  },
  mounted() {
    this.fetchScheduleList();
  },
};
</script>

<style scoped>
.check-schedule {
  padding: 20px;
}
</style>
