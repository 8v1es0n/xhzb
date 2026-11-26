<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-number primary">{{ stats.total }}</div>
          <div class="stat-label">值班总人次</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-number success">{{ stats.normal }}</div>
          <div class="stat-label">正常值班</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-number warning">{{ stats.rest }}</div>
          <div class="stat-label">调休</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-number danger">{{ stats.leave }}</div>
          <div class="stat-label">请假</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-number info">{{ stats.nurseCount }}</div>
          <div class="stat-label">参与护理员</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 工具栏 -->
    <el-row :gutter="10" class="mb8 mt16">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Calendar" @click="viewMode = 'week'":type="viewMode === 'week' ? '' : 'info'">本周</el-button>
        <el-button type="primary" plain icon="Calendar" @click="viewMode = 'month'" :type="viewMode === 'month' ? '' : 'info'">本月</el-button>
      </el-col>
      <el-col :span="1.5" :offset="viewMode === 'week' ? 10 : 8">
        <el-button type="success" plain icon="RefreshRight" circle @click="loadBoardData" title="刷新" />
      </el-col>
    </el-row>

    <!-- 日历看板 -->
    <div class="board-title">值班人员分布 <span class="sub-title">{{ viewMode === 'week' ? '本周排班概览' : '本月排班概览' }}</span></div>

    <div v-loading="loading" class="calendar-board">
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
  </div>
</template>

<script setup name="DutyBoard">
import { listDutyRoster } from "@/api/duty/roster";
import { listUser } from "@/api/system/user";

const { proxy } = getCurrentInstance();

const loading = ref(false);
const viewMode = ref('week');
const dayList = ref([]);
const nurseList = ref([]);
const selectedDate = ref('');
const detailVisible = ref(false);
const selectedRosters = ref([]);
const detailLoading = ref(false);

const stats = reactive({ total: 0, normal: 0, rest: 0, leave: 0, nurseCount: 0 });

function weekDayName(wd) { return ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][wd]; }
function statusText(s) { return { '0': '正常', '1': '调休', '2': '请假' }[s] || s; }
function statusTagType(s) { return { '0': 'success', '1': 'warning', '2': 'danger' }[s] || 'info'; }

/** 生成日期列表 */
function generateDays() {
  const now = new Date();
  const days = [];
  let start, count;

  if (viewMode.value === 'week') {
    const dayOfWeek = now.getDay() || 7;
    start = new Date(now);
    start.setDate(now.getDate() - dayOfWeek + 1); // 周一
    count = 7;
  } else {
    start = new Date(now.getFullYear(), now.getMonth(), 1);
    count = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate();
  }

  for (let i = 0; i < count; i++) {
    const d = new Date(start);
    d.setDate(start.getDate() + i);
    const dateStr = formatDate(d);
    days.push({
      dayNum: d.getDate(),
      dateStr,
      isToday: isSameDay(d, now),
      weekDay: d.getDay(),
      rosters: []
    });
  }
  dayList.value = days;
}

function formatDate(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${dd}`;
}

function isSameDay(a, b) {
  return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
}

/** 加载看板数据 */
async function loadBoardData() {
  loading.value = true;
  generateDays();

  // 获取日期范围
  if (dayList.value.length === 0) { loading.value = false; return; }

  const startDate = dayList.value[0].dateStr;
  const endDate = dayList.value[dayList.value.length - 1].dateStr;

  try {
    const res = await listDutyRoster({ dutyDateStart: startDate, dutyDateEnd: endDate });
    const allRosters = res.rows || [];

    // 重置统计
    stats.total = allRosters.length;
    stats.normal = 0; stats.rest = 0; stats.leave = 0;
    const nurseSet = new Set();

    // 按日期分组
    dayList.value.forEach(day => { day.rosters = []; });
    allRosters.forEach(r => {
      const idx = dayList.value.findIndex(d => d.dateStr === r.dutyDate);
      if (idx >= 0) {
        dayList.value[idx].rosters.push(r);
      }
      nurseSet.add(r.userId);
      if (r.status === '0') stats.normal++;
      else if (r.status === '1') stats.rest++;
      else if (r.status === '2') stats.leave++;
    });
    stats.nurseCount = nurseSet.size;
  } catch (e) {
    console.error(e);
  }
  loading.value = false;
}

/** 选择某天查看详情 */
function selectDate(day) {
  selectedDate.value = day.dateStr;
  selectedRosters.value = day.rosters || [];
  detailVisible.value = true;
}

// 获取护理员列表（备用）
listUser({ status: '0' }).then(res => { nurseList.value = res.rows || []; });

// 初始化
watch(viewMode, () => loadBoardData());
loadBoardData();
</script>

<style scoped>
.stat-row { margin-bottom: 16px; }
.stat-card { text-align: center; padding: 8px 0; }
.stat-number { font-size: 32px; font-weight: bold; line-height: 1.2; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.primary { color: #409EFF; }
.success { color: #67C23A; }
.warning { color: #E6A23C; }
.danger { color: #F56C6C; }
.info { color: #909399; }
.mt16 { margin-top: 16px; }
.board-title { font-size: 18px; font-weight: bold; margin: 16px 0 12px; }
.sub-title { font-size: 13px; font-weight: normal; color: #909399; margin-left: 8px; }
.day-col { margin-bottom: 16px; }
.day-card { border: 2px solid #EBEEF5; border-radius: 8px; overflow: hidden; transition: all 0.3s; cursor: pointer; background: #fff; }
.day-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); border-color: #409EFF; }
.day-card.is-today { border-color: #67C23A; background: #f0fdf4; }
.day-card.is-selected { border-color: #409EFF; background: #ecf5ff; }
.day-header { padding: 10px 12px; display: flex; align-items: center; gap: 6px; flex-wrap: wrap; background: #fafafa; border-bottom: 1px solid #EBEEF5; }
.day-num { font-size: 20px; font-weight: bold; color: #303133; min-width: 28px; }
.day-date { font-size: 11px; color: #909399; }
.day-body { padding: 10px 12px; min-height: 80px; }
.no-data { text-align: center; color: #C0C4CC; padding: 20px 0; font-size: 13px; }
.roster-list { display: flex; flex-direction: column; gap: 8px; }
.roster-item { display: flex; align-items: center; gap: 8px; padding: 4px 0; }
.roster-avatar { background: #409EFF; color: #fff; font-size: 12px; flex-shrink: 0; }
.roster-info { flex: 1; min-width: 0; }
.roster-name { font-size: 13px; font-weight: 500; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.roster-shift { font-size: 11px; color: #909399; }
.roster-status { flex-shrink: 0; }
</style>
