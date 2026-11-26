<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="班次名称" prop="shiftName">
        <el-input v-model="queryParams.shiftName" placeholder="请输入班次名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['nursing:shift:add']">新增班次</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['nursing:shift:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" type="index" width="60" />
      <el-table-column label="班次名称" align="center" prop="shiftName" min-width="120" />
      <el-table-column label="开始时间" align="center" prop="startTime" width="100" />
      <el-table-column label="结束时间" align="center" prop="endTime" width="100" />
      <el-table-column label="是否跨天" align="center" prop="isNextDay" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.isNextDay === 1 ? 'warning' : 'info'">{{ scope.row.isNextDay === 1 ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sortOrder" width="70" />
      <el-table-column label="备注" align="center" prop="remark" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['nursing:shift:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['nursing:shift:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/修改班次对话框 -->
    <el-dialog :title="title" v-model="open" width="550px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="班次名称" prop="shiftName">
          <el-input v-model="form.shiftName" placeholder="请输入班次名称（如：白班、夜班）" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker v-model="form.startTime" placeholder="选择开始时间" value-format="HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker v-model="form.endTime" placeholder="选择结束时间" value-format="HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="是否跨天" prop="isNextDay">
          <el-radio-group v-model="form.isNextDay">
            <el-radio :label="0">否</el-radio>
            <el-radio :label="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" controls-position="right" style="width: 200px" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ShiftConfig">
import { listShiftConfig, getShiftConfig, addShiftConfig, updateShiftConfig, delShiftConfig } from "@/api/duty/shift";

const { proxy } = getCurrentInstance();

const configList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    shiftName: undefined
  },
  rules: {
    shiftName: [{ required: true, message: "班次名称不能为空", trigger: "blur" }],
    startTime: [{ required: true, message: "请选择开始时间", trigger: "change" }],
    endTime: [{ required: true, message: "请选择结束时间", trigger: "change" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listShiftConfig(queryParams.value).then(response => {
    configList.value = response.rows;
    loading.value = false;
  });
}

function cancel() { open.value = false; reset(); }

function reset() {
  form.value = { shiftId: undefined, shiftName: undefined, startTime: undefined, endTime: undefined, isNextDay: 0, sortOrder: 0, remark: undefined };
  proxy.resetForm("formRef");
}

function handleQuery() { getList(); }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery(); }

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.shiftId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function handleAdd() { reset(); open.value = true; title.value = "新增班次配置"; }

function handleUpdate(row) {
  reset();
  const shiftId = row.shiftId || ids.value[0];
  getShiftConfig(shiftId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改班次配置";
  });
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.shiftId !== undefined) {
        updateShiftConfig(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); });
      } else {
        addShiftConfig(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); });
      }
    }
  });
}

function handleDelete(row) {
  const shiftIds = row.shiftId || ids.value;
  proxy.$modal.confirm('是否确认删除班次配置？').then(() => delShiftConfig(shiftIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {});
}

getList();
</script>
