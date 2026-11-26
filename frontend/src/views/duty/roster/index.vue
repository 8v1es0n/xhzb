<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="border-card" class="duty-tabs">
      <!-- Tab1: 值班排班 -->
      <el-tab-pane label="值班排班" name="roster">
        <el-form :model="rosterQuery" ref="rosterQueryRef" :inline="true" v-show="showRosterSearch" label-width="90px">
          <el-form-item label="值班日期" prop="dutyDate">
            <el-date-picker v-model="rosterQuery.dutyDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item label="护理员" prop="userId">
            <el-select v-model="rosterQuery.userId" placeholder="请选择护理员" clearable style="width: 200px">
              <el-option v-for="item in nurseList" :key="item.userId" :label="item.nickName" :value="item.userId" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="rosterQuery.status" placeholder="请选择状态" clearable style="width: 150px">
              <el-option label="正常" value="0" /><el-option label="调休" value="1" /><el-option label="请假" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleRosterQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetRosterQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleRosterAdd" v-hasPermi="['nursing:duty:add']">新增排班</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Delete" :disabled="rosterMultiple" @click="handleRosterBatchDel" v-hasPermi="['nursing:duty:remove']">批量删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="View" @click="handleCurrentDuty">当前在岗</el-button>
          </el-col>
        </el-row>

        <el-table v-loading="rosterLoading" :data="rosterList" @selection-change="handleRosterSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="排班ID" align="center" prop="rosterId" width="80" />
          <el-table-column label="护理员" align="center" prop="userName" min-width="100" />
          <el-table-column label="值班日期" align="center" prop="dutyDate" width="120" />
          <el-table-column label="班次" align="center" prop="shiftName" width="120" />
          <el-table-column label="状态" align="center" prop="status" width="80">
            <template #default="scope">
              <el-tag v-if="scope.row.status === '0'" type="success">正常</el-tag>
              <el-tag v-else-if="scope.row.status === '1'" type="warning">调休</el-tag>
              <el-tag v-else-if="scope.row.status === '2'" type="danger">请假</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="备注" align="center" prop="remark" min-width="150" show-overflow-tooltip />
          <el-table-column label="创建时间" align="center" prop="createTime" width="160">
            <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleRosterUpdate(scope.row)" v-hasPermi="['nursing:duty:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleRosterDelete(scope.row)" v-hasPermi="['nursing:duty:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="rosterTotal > 0" :total="rosterTotal" v-model:page="rosterQuery.pageNum" v-model:limit="rosterQuery.pageSize" @pagination="getRosterList" />

        <!-- 新增/修改排班对话框 -->
        <el-dialog :title="rosterTitle" v-model="rosterOpen" width="520px" append-to-body>
          <el-form ref="rosterFormRef" :model="rosterForm" :rules="rosterRules" label-width="100px">
            <el-form-item label="护理员" prop="userId">
              <el-select v-model="rosterForm.userId" placeholder="请选择护理员" filterable style="width: 100%">
                <el-option v-for="item in nurseList" :key="item.userId" :label="item.nickName" :value="item.userId" />
              </el-select>
            </el-form-item>
            <el-form-item label="值班日期" prop="dutyDate">
              <el-date-picker v-model="rosterForm.dutyDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
            <el-form-item label="班次" prop="shiftId">
              <el-select v-model="rosterForm.shiftId" placeholder="请选择班次" style="width: 100%">
                <el-option v-for="item in shiftList" :key="item.shiftId" :label="item.shiftName" :value="item.shiftId" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="rosterForm.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="正常" value="0" /><el-option label="调休" value="1" /><el-option label="请假" value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="rosterForm.remark" type="textarea" placeholder="请输入备注" :rows="3" />
            </el-form-item>
          </el-form>
          <template #footer>
            <div class="dialog-footer">
              <el-button type="primary" @click="submitRosterForm">确 定</el-button>
              <el-button @click="rosterOpen = false">取 消</el-button>
            </div>
          </template>
        </el-dialog>

        <!-- 当前在岗弹窗 -->
        <el-dialog title="当前在岗人员" v-model="currentDialogVisible" width="600px" append-to-body>
          <el-empty v-if="currentDutyList.length === 0" description="暂无在岗人员" />
          <el-table v-else :data="currentDutyList" border>
            <el-table-column label="姓名" align="center" prop="userName" />
            <el-table-column label="班次" align="center" prop="shiftName" />
            <el-table-column label="值班日期" align="center" prop="dutyDate" />
            <el-table-column label="状态" align="center"><template #default><el-tag type="success">在岗</el-tag></template></el-table-column>
          </el-table>
        </el-dialog>
      </el-tab-pane>

      <!-- Tab2: 班次配置 -->
      <el-tab-pane label="班次配置" name="shift">
        <el-form :model="shiftQuery" ref="shiftQueryRef" :inline="true" v-show="showShiftSearch">
          <el-form-item label="班次名称" prop="shiftName">
            <el-input v-model="shiftQuery.shiftName" placeholder="请输入班次名称" clearable style="width: 200px" @keyup.enter="handleShiftQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleShiftQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetShiftQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleShiftAdd" v-hasPermi="['nursing:shift:add']">新增班次</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="shiftMultiple" @click="handleShiftDelete" v-hasPermi="['nursing:shift:remove']">删除</el-button>
          </el-col>
        </el-row>

        <el-table v-loading="shiftLoading" :data="configList" @selection-change="handleShiftSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序号" align="center" type="index" width="60" />
          <el-table-column label="班次名称" align="center" prop="shiftName" min-width="120" />
          <el-table-column label="开始时间" align="center" prop="startTime" width="110" />
          <el-table-column label="结束时间" align="center" prop="endTime" width="110" />
          <el-table-column label="是否跨天" align="center" prop="isNextDay" width="95">
            <template #default="scope"><el-tag :type="scope.row.isNextDay === 1 ? 'warning' : 'info'">{{ scope.row.isNextDay === 1 ? '是' : '否' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="排序" align="center" prop="sortOrder" width="70" />
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleShiftUpdate(scope.row)" v-hasPermi="['nursing:shift:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleShiftDelete(scope.row)" v-hasPermi="['nursing:shift:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 新增/修改班次对话框 -->
        <el-dialog :title="shiftTitle" v-model="shiftOpen" width="550px" append-to-body>
          <el-form ref="shiftFormRef" :model="shiftForm" :rules="shiftRules" label-width="100px">
            <el-form-item label="班次名称" prop="shiftName">
              <el-input v-model="shiftForm.shiftName" placeholder="如：白班、夜班、午班" />
            </el-form-item>
            <el-form-item label="开始时间" prop="startTime">
              <el-time-picker v-model="shiftForm.startTime" placeholder="选择开始时间" value-format="HH:mm:ss" style="width: 100%" />
            </el-form-item>
            <el-form-item label="结束时间" prop="endTime">
              <el-time-picker v-model="shiftForm.endTime" placeholder="选择结束时间" value-format="HH:mm:ss" style="width: 100%" />
            </el-form-item>
            <el-form-item label="是否跨天" prop="isNextDay">
              <el-radio-group v-model="shiftForm.isNextDay">
                <el-radio :label="0">否</el-radio><el-radio :label="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="shiftForm.sortOrder" :min="0" :max="999" controls-position="right" style="width: 200px" />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="shiftForm.remark" type="textarea" placeholder="请输入备注" :rows="3" />
            </el-form-item>
          </el-form>
          <template #footer>
            <div class="dialog-footer">
              <el-button type="primary" @click="submitShiftForm">确 定</el-button>
              <el-button @click="shiftOpen = false">取 消</el-button>
            </div>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- Tab3: 值班看板 -->
      <el-tab-pane label="值班看板" name="board">
        <!-- 统计卡片 -->
        <el-row :gutter="16" class="stat-row mb16">
          <el-col :xs="12" :sm="6" :md="4">
            <el-card shadow="hover" class="stat-card text-center">
              <div class="stat-number primary">{{ stats.total }}</div>
              <div class="stat-label">值班总人次</div>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="6" :md="4">
            <el-card shadow="hover" class="stat-card text-center">
              <div class="stat-number success">{{ stats.normal }}</div>
              <div class="stat-label">正常值班</div>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="6" :md="4">
            <el-card shadow="hover" class="stat-card text-center">
              <div class="stat-number warning">{{ stats.rest }}</div>
              <div class="stat-label">调休</div>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="6" :md="4">
            <el-card shadow="hover" class="stat-card text-center">
              <div class="stat-number danger">{{ stats.leave }}</div>
              <div class="stat-label">请假</div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="24" :md="4">
            <el-card shadow="hover" class="stat-card text-center">
              <div class="stat-number info">{{ stats.nurseCount }}</div>
              <div class="stat-label">参与护理员</div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 切换按钮 -->
        <el-row :gutter="10" class="mb16">
          <el-col :span="6">
            <el-button-group>
              <el-button :type="viewMode === 'week' ? 'primary' : ''" @click="switchView('week')">本周</el-button>
              <el-button :type="viewMode === 'month' ? 'primary' : ''" @click="switchView('month')">本月</el-button>
            </el-button-group>
          </el-col>
          <el-col :span="18" style="text-align:right">
            <el-button type="success" plain icon="RefreshRight" circle @click="loadBoardData" title="刷新" />
          </el-col>
        </el-row>

        <!-- 日历看板 -->
        <div v-loading="boardLoading" class="calendar-board">
          <el-row :gutter="12">
            <el-col :xs="24" :sm="12" :md="8" :lg="viewMode === 'week' ? 6 : 4" v-for="(day, index) in dayList" :key="index" class="day-col">
              <div class="day-card" :class="{ 'is-today': day.isToday, 'is-selected': selectedDate === day.dateStr }" @click="selectDate(day)">
                <div class="day-header">
                  <span class="day-num">{{ day.dayNum }}</span>
                  <span class="day-date">{{ day.dateStr }}</span>
                  <el-tag size="small" :type="day.weekDay === 0 || day.weekDay === 6 ? 'warning' : 'info'">{{ weekDayName(day.weekDay) }}</el-tag>
                  <el-tag v-if="day.isToday" size="small" type="success">今天</el-tag>
                </div>
                <div class="day-body">
                  <div v-if="!day.rosters || day.rosters.length === 0" class="no-data">— 无排班 —</div>
                  <div v-else class="roster-list">
                    <div v-for="(r, ri) in day.rosters" :key="ri" class="roster-item">
                      <el-avatar :size="28" class="roster-avatar">{{ (r.userName || '?').charAt(0) }}</el-avatar>
                      <div class="roster-info">
                        <div class="roster-name">{{ r.userName }}</div>
                        <div class="roster-shift">{{ r.shiftName }}</div>
                      </div>
                      <el-tag size="small" :type="statusTagType(r.status)" class="roster-status">{{ statusText(r.status) }}</el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 选中日期详情 -->
        <el-dialog :title="'「' + selectedDate + '」 排班详情'" v-model="detailVisible" width="650px" append-to-body>
          <el-table :data="selectedRosters" border v-loading="detailLoading">
            <el-table-column label="护理员" align="center" prop="userName" />
            <el-table-column label="班次" align="center" prop="shiftName" />
            <el-table-column label="状态" align="center" prop="status">
              <template #default="scope"><el-tag :type="statusTagType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag></template>
            </el-table-column>
            <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
          </el-table>
        </el-dialog>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup name="DutyManage">
import { listDutyRoster, getDutyRoster, addDutyRoster, updateDutyRoster, delDutyRoster, getCurrentDutyUsers } from "@/api/duty/roster";
import { listShiftConfig, allShifts, getShiftConfig, addShiftConfig, updateShiftConfig, delShiftConfig } from "@/api/duty/shift";
import { listUser } from "@/api/system/user";

const { proxy } = getCurrentInstance();

// ==================== 公共数据 ====================
const activeTab = ref("roster");
const nurseList = ref([]);
const shiftList = ref([]);

// ==================== Tab1: 值班排班 ====================
const rosterLoading = ref(true);
const showRosterSearch = ref(true);
const rosterList = ref([]);
const rosterIds = ref([]);
const rosterMultiple = ref(true);
const rosterTotal = ref(0);
const rosterTitle = ref("");
const rosterOpen = ref(false);
const currentDialogVisible = ref(false);
const currentDutyList = ref([]);

const rosterQuery = reactive({ pageNum: 1, pageSize: 10, dutyDate: undefined, userId: undefined, status: undefined });
const rosterForm = ref({});
const rosterRules = {
  userId: [{ required: true, message: "请选择护理员", trigger: "change" }],
  dutyDate: [{ required: true, message: "请选择值班日期", trigger: "change" }],
  shiftId: [{ required: true, message: "请选择班次", trigger: "change" }],
  status: [{ required: true, message: "请选择状态", trigger: "change" }]
};

function getRosterList() {
  rosterLoading.value = true;
  listDutyRoster(rosterQuery).then(res => { rosterList.value = res.rows; rosterTotal.value = res.total; rosterLoading.value = false; });
}
function handleRosterQuery() { rosterQuery.pageNum = 1; getRosterList(); }
function resetRosterQuery() { proxy.resetForm("rosterQueryRef"); handleRosterQuery(); }
function handleRosterSelectionChange(s) { rosterIds.value = s.map(i => i.rosterId); rosterMultiple.value = !s.length; }

function handleRosterAdd() { rosterForm.value = { userId: undefined, dutyDate: undefined, shiftId: undefined, status: "0", remark: undefined }; rosterOpen.value = true; rosterTitle.value = "新增排班"; }
function handleRosterUpdate(row) {
  getDutyRoster(row.rosterId).then(res => { rosterForm.value = res.data; rosterOpen.value = true; rosterTitle.value = "修改排班"; });
}
function submitRosterForm() {
  proxy.$refs["rosterFormRef"].validate(v => {
    if (v) {
      const fn = rosterForm.value.rosterId !== undefined ? updateDutyRoster : addDutyRoster;
      fn(rosterForm.value).then(() => { proxy.$modal.msgSuccess(rosterForm.value.rosterId ? "修改成功" : "新增成功"); rosterOpen.value = false; getRosterList(); });
    }
  });
}
function handleRosterDelete(row) {
  const ids = row.rosterId || rosterIds.value;
  proxy.$modal.confirm("是否确认删除？").then(() => delDutyRoster(ids)).then(() => { getRosterList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {});
}
function handleRosterBatchDel() {
  if (!rosterIds.value.length) return;
  proxy.$modal.confirm("确认删除选中的 " + rosterIds.value.length + " 条？").then(() => delDutyRoster(rosterIds.value.join(","))).then(() => { getRosterList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {});
}
function handleCurrentDuty() {
  getCurrentDutyUsers().then(res => { currentDutyList.value = res.data || []; currentDialogVisible.value = true; });
}

// ==================== Tab2: 班次配置 ====================
const shiftLoading = ref(true);
const showShiftSearch = ref(true);
const configList = ref([]);
const shiftIds = ref([]);
const shiftMultiple = ref(true);
const shiftTitle = ref("");
const shiftOpen = ref(false);

const shiftQuery = reactive({ shiftName: undefined });
const shiftForm = ref({});
const shiftRules = {
  shiftName: [{ required: true, message: "班次名称不能为空", trigger: "blur" }],
  startTime: [{ required: true, message: "请选择开始时间", trigger: "change" }],
  endTime: [{ required: true, message: "请选择结束时间", trigger: "change" }]
};

function getShiftList() {
  shiftLoading.value = true;
  listShiftConfig(shiftQuery).then(res => { configList.value = res.rows; shiftLoading.value = false; });
}
function handleShiftQuery() { getShiftList(); }
function resetShiftQuery() { proxy.resetForm("shiftQueryRef"); getShiftList(); }
function handleShiftSelectionChange(s) { shiftIds.value = s.map(i => i.shiftId); shiftMultiple.value = !s.length; }

function handleShiftAdd() { shiftForm.value = { shiftName: undefined, startTime: undefined, endTime: undefined, isNextDay: 0, sortOrder: 0, remark: undefined }; shiftOpen.value = true; shiftTitle.value = "新增班次配置"; }
function handleShiftUpdate(row) {
  getShiftConfig(row.shiftId).then(res => { shiftForm.value = res.data; shiftOpen.value = true; shiftTitle.value = "修改班次配置"; });
}
function submitShiftForm() {
  proxy.$refs["shiftFormRef"].validate(v => {
    if (v) {
      const fn = shiftForm.value.shiftId !== undefined ? updateShiftConfig : addShiftConfig;
      fn(shiftForm.value).then(() => { proxy.$modal.msgSuccess(shiftForm.value.shiftId ? "修改成功" : "新增成功"); shiftOpen.value = false; getShiftList(); getShiftOptions(); });
    }
  });
}
function handleShiftDelete(row) {
  const ids = row.shiftId || shiftIds.value;
  proxy.$modal.confirm("是否确认删除？").then(() => delShiftConfig(ids)).then(() => { getShiftList(); getShiftOptions(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {});
}

// ==================== Tab3: 值班看板 ====================
const boardLoading = ref(false);
const viewMode = ref("week");
const dayList = ref([]);
const selectedDate = ref("");
const detailVisible = ref(false);
const selectedRosters = ref([]);
const detailLoading = ref(false);

const stats = reactive({ total: 0, normal: 0, rest: 0, leave: 0, nurseCount: 0 });

function weekDayName(wd) { return ["周日","周一","周二","周三","周四","周五","周六"][wd]; }
function statusText(s) { return {"0":"正常","1":"调休","2":"请假"}[s]||s; }
function statusTagType(s) { return {"0":"success","1":"warning","2":"danger"}[s]||"info"; }

function switchView(mode) { viewMode.value = mode; loadBoardData(); }

function generateDays() {
  const now = new Date();
  let start, count;
  if (viewMode.value === "week") {
    const d = now.getDay() || 7;
    start = new Date(now); start.setDate(now.getDate() - d + 1);
    count = 7;
  } else {
    start = new Date(now.getFullYear(), now.getMonth(), 1);
    count = new Date(now.getFullYear(), now.getMonth()+1, 0).getDate();
  }
  const days = [];
  for (let i=0;i<count;i++) {
    const dd=new Date(start);dd.setDate(start.getDate()+i);
    days.push({dayNum:dd.getDate(),dateStr:`${dd.getFullYear()}-${String(dd.getMonth()+1).padStart(2,'0')}-${String(dd.getDate()).padStart(2,'0')}`,isToday:isSameDay(dd,now),weekDay:dd.getDay(),rosters:[]});
  }
  dayList.value=days;
}

function isSameDay(a,b){return a.getFullYear()===b.getFullYear()&&a.getMonth()===b.getMonth()&&a.getDate()===b.getDate();}
async function loadBoardData(){
  boardLoading.value=true;
  generateDays();
  if(!dayList.value.length){boardLoading.value=false;return;}
  const sd=dayList.value[0].dateStr,ed=dayList.value[dayList.value.length-1].dateStr;
  try{
    const res=await listDutyRoster({dutyDateStart:sd,dutyDateEnd:ed});
    const all=res.rows||[];
    stats.total=all.length;stats.normal=0;stats.rest=0;stats.leave=0;const ns=new Set();
    dayList.value.forEach(d=>{d.rosters=[];});
    all.forEach(r=>{const idx=dayList.value.findIndex(d=>d.dateStr===r.dutyDate);if(idx>=0){dayList.value[idx].rosters.push(r);}ns.add(r.userId);if(r.status==='0')stats.normal++;else if(r.status==='1')stats.rest++;else if(r.status==='2')stats.leave++;});
    stats.nurseCount=ns.size;
  }catch(e){console.error(e);}
  boardLoading.value=false;
}
function selectDate(day){selectedDate.value=day.dateStr;selectedRosters.value=day.rosters||[];detailVisible.value=true;}

// ==================== 公共方法 ====================
function getNurseList(){listUser({status:'0'}).then(res=>{nurseList.value=res.rows||[];});}
function getShiftOptions(){allShifts().then(res=>{shiftList.value=res.data||[];});}

getNurseList();
getShiftOptions();
getRosterList();
getShiftList();
watch(activeTab,(v)=>{if(v==='board')loadBoardData();});
</script>

<style scoped>
.duty-tabs >>> .el-tabs__header { margin-bottom: 16px; }
.stat-row .text-center { text-align: center; }
.stat-card { padding: 8px 4px; }
.stat-number { font-size: 28px; font-weight: bold; line-height: 1.3; }
.stat-label { font-size: 13px; color: #909399; margin-top: 2px; }
.primary { color: #409EFF; } .success { color: #67C23A; } .warning { color: #E6A23C; } .danger { color: #F56C6C; } .info { color: #909399; }
.mb16 { margin-bottom: 16px; }
.calendar-board { min-height: 300px; }
.day-col { margin-bottom: 14px; }
.day-card { border: 2px solid #EBEEF5; border-radius: 8px; overflow: hidden; transition: all 0.25s; cursor: pointer; background: #fff; }
.day-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); border-color: #409EFF; }
.day-card.is-today { border-color: #67C23A; background: #f0fdf4; }
.day-card.is-selected { border-color: #409EFF; background: #ecf5ff; }
.day-header { padding: 8px 10px; display: flex; align-items: center; gap: 6px; flex-wrap: wrap; background: #fafafa; border-bottom: 1px solid #EBEEF5; }
.day-num { font-size: 18px; font-weight: bold; color: #303133; min-width: 26px; }
.day-date { font-size: 11px; color: #909399; }
.day-body { padding: 8px 10px; min-height: 70px; }
.no-data { text-align: center; color: #C0C4CC; padding: 16px 0; font-size: 13px; }
.roster-list { display: flex; flex-direction: column; gap: 7px; }
.roster-item { display: flex; align-items: center; gap: 7px; padding: 3px 0; }
.roster-avatar { background: #409EFF; color: #fff; font-size: 11px; flex-shrink: 0; }
.roster-info { flex: 1; min-width: 0; }
.roster-name { font-size: 13px; font-weight: 500; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.roster-shift { font-size: 11px; color: #909399; }
.roster-status { flex-shrink: 0; }
.mb8 { margin-bottom: 8px; }
</style>