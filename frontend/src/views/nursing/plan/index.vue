<template>
  <div class="app-container">
    <el-form :inline="true" :model="query">
      <el-form-item label="计划名称">
        <el-input v-model="query.planName" clearable @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="edit()" v-hasPermi="['nursing:plan:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="!selected.length" @click="remove()" v-hasPermi="['nursing:plan:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="load" />
    </el-row>

    <el-table v-loading="loading" :data="rows" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" prop="id" width="80" align="center" />
      <el-table-column label="计划名称" prop="planName" />
      <el-table-column label="排序号" prop="sortNo" width="90" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" />
      <el-table-column label="操作" width="140" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="edit(scope.row)" v-hasPermi="['nursing:plan:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="remove(scope.row)" v-hasPermi="['nursing:plan:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" @pagination="load" />

    <el-dialog v-model="open" :title="form.id ? '修改护理计划' : '添加护理计划'" width="900px" append-to-body>
      <el-form ref="planRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="计划名称" prop="planName"><el-input v-model="form.planName" maxlength="255" placeholder="请输入计划名称" /></el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="排序号"><el-input-number v-model="form.sortNo" :min="0" /></el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" maxlength="255" placeholder="请输入备注" /></el-form-item>
      </el-form>

      <el-row class="mb8"><el-button type="primary" plain icon="Plus" @click="addProject">添加护理项目</el-button></el-row>
      <el-table :data="form.projects">
        <el-table-column label="护理项目" min-width="160">
          <template #default="scope">
            <el-select v-model="scope.row.projectId" filterable placeholder="请选择项目">
              <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="执行时间段" min-width="160">
          <template #default="scope"><el-input v-model="scope.row.executeTime" placeholder="如 08:00-09:00" /></template>
        </el-table-column>
        <el-table-column label="执行周期" width="120">
          <template #default="scope">
            <el-select v-model="scope.row.executeCycle">
              <el-option label="天" :value="0" /><el-option label="周" :value="1" /><el-option label="月" :value="2" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="执行频次" width="150">
          <template #default="scope"><el-input-number v-model="scope.row.executeFrequency" :min="1" /></template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="scope"><el-button link type="primary" @click="removeProject(scope.$index)">移除</el-button></template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer"><el-button type="primary" @click="submit">确 定</el-button><el-button @click="cancel">取 消</el-button></div>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { listPlans, getPlan, savePlan, delPlans, listProjects } from '@/api/nursing/manage'

const { proxy } = getCurrentInstance()
const query = ref({ pageNum: 1, pageSize: 10 })
const rows = ref([])
const projects = ref([])
const total = ref(0)
const loading = ref(false)
const open = ref(false)
const showSearch = ref(true)
const selected = ref([])
const form = ref({})
const rules = {
  planName: [{ required: true, message: '计划名称不能为空', trigger: 'blur' }]
}

/** 查询护理计划列表 */
function load() {
  loading.value = true
  listPlans(query.value).then(response => {
    rows.value = response.rows
    total.value = response.total
  }).finally(() => {
    loading.value = false
  })
}

/** 重置查询条件 */
function resetQuery() {
  query.value = { pageNum: 1, pageSize: 10 }
  load()
}

/** 记录表格选中项 */
function handleSelectionChange(selection) {
  selected.value = selection.map(item => item.id)
}

/** 新增或修改护理计划 */
function edit(row) {
  listProjects().then(response => {
    projects.value = response.data || []
  })
  if (row) {
    getPlan(row.id).then(response => {
      form.value = response.data
      open.value = true
    })
  } else {
    form.value = { status: 1, projects: [] }
    open.value = true
  }
}

/** 添加护理项目配置 */
function addProject() {
  form.value.projects.push({
    projectId: null,
    executeTime: '',
    executeCycle: 0,
    executeFrequency: 1
  })
}

/** 移除护理项目配置 */
function removeProject(index) {
  form.value.projects.splice(index, 1)
}

/** 取消编辑 */
function cancel() {
  open.value = false
  form.value = {}
}

/** 保存护理计划 */
function submit() {
  proxy.$refs.planRef.validate(valid => {
    if (!valid) return
    const projectIds = form.value.projects.map(item => item.projectId)
    const hasInvalidProject = form.value.projects.some(item =>
      !item.projectId || !item.executeTime || !item.executeFrequency
    )
    if (hasInvalidProject || new Set(projectIds).size !== projectIds.length) {
      proxy.$modal.msgError('请完整填写项目配置，且项目不能重复')
      return
    }
    savePlan(form.value).then(() => {
      proxy.$modal.msgSuccess('保存成功')
      open.value = false
      load()
    })
  })
}

/** 删除护理计划 */
function remove(row) {
  const ids = row?.id || selected.value
  proxy.$modal.confirm('是否确认删除所选护理计划？').then(() => {
    return delPlans(ids)
  }).then(() => {
    load()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

load()
</script>
