<template>
  <div class="app-container">
    <el-form ref="queryRef" :inline="true" :model="query">
      <el-form-item label="等级名称">
        <el-input v-model="query.name" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="护理计划">
        <el-select v-model="query.lplanId" clearable>
          <el-option v-for="plan in plans" :key="plan.id" :label="plan.planName" :value="plan.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['nursing:level:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['nursing:level:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="rows" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" prop="id" width="80" align="center" />
      <el-table-column label="等级名称" prop="name" />
      <el-table-column label="护理计划" prop="planName" />
      <el-table-column label="护理费用" prop="fee" width="120" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="等级说明" prop="description" />
      <el-table-column label="操作" width="140" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['nursing:level:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['nursing:level:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" @pagination="getList" />

    <el-dialog v-model="open" :title="title" width="560px" append-to-body>
      <el-form ref="levelRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="等级名称" prop="name"><el-input v-model="form.name" maxlength="50" placeholder="请输入等级名称" /></el-form-item>
        <el-form-item label="护理计划" prop="lplanId">
          <el-select v-model="form.lplanId" filterable placeholder="请选择启用的护理计划">
            <el-option v-for="plan in plans" :key="plan.id" :label="plan.planName" :value="plan.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="护理费用" prop="fee"><el-input-number v-model="form.fee" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="状态" prop="status"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" /></el-form-item>
        <el-form-item label="等级说明"><el-input v-model="form.description" type="textarea" maxlength="255" placeholder="请输入等级说明" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" maxlength="255" placeholder="请输入备注" /></el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="NursingLevel">
import { listLevels, getLevel, saveLevel, delLevels, enabledPlans } from '@/api/nursing/manage'

const { proxy } = getCurrentInstance()
const rows = ref([])
const plans = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  query: {
    pageNum: 1,
    pageSize: 10,
    name: undefined,
    lplanId: undefined
  },
  rules: {
    name: [{ required: true, message: '等级名称不能为空', trigger: 'blur' }],
    lplanId: [{ required: true, message: '护理计划不能为空', trigger: 'change' }],
    fee: [{ required: true, message: '护理费用不能为空', trigger: 'blur' }],
    status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
  }
})

const { query, form, rules } = toRefs(data)

/** 查询护理等级列表 */
function getList() {
  loading.value = true
  listLevels(query.value).then(response => {
    rows.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 查询启用状态的护理计划 */
function getEnabledPlans() {
  enabledPlans().then(response => {
    plans.value = response.data || []
  })
}

/** 查询按钮操作 */
function handleQuery() {
  query.value.pageNum = 1
  getList()
}

/** 重置查询条件 */
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

/** 表格选择项变化 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 表单重置 */
function reset() {
  form.value = {
    id: null,
    name: null,
    lplanId: null,
    fee: 0,
    status: 1,
    description: null,
    remark: null
  }
  proxy.resetForm('levelRef')
}

/** 新增护理等级 */
function handleAdd() {
  reset()
  getEnabledPlans()
  open.value = true
  title.value = '添加护理等级'
}

/** 修改护理等级 */
function handleUpdate(row) {
  reset()
  getEnabledPlans()
  getLevel(row.id || ids.value[0]).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改护理等级'
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 提交护理等级表单 */
function submitForm() {
  proxy.$refs.levelRef.validate(valid => {
    if (!valid) return
    saveLevel(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.id ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}

/** 删除护理等级 */
function handleDelete(row) {
  const deleteIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除护理等级编号为“' + deleteIds + '”的数据项？').then(() => {
    return delLevels(deleteIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getEnabledPlans()
getList()
</script>
