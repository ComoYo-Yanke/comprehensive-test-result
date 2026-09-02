<template>
  <div>
    <el-card>
      <!-- 筛选条件 -->
      <el-form inline>
        <el-form-item label="名称">
          <el-input v-model="query.name" placeholder="活动名称" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="学院">
          <el-select v-model="query.schoolId" placeholder="精确选择(校级不选)" clearable filterable style="width: 160px">
            <el-option v-for="s in schoolOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" placeholder="全部" clearable style="width: 130px">
            <el-option label="校级思想" :value="1" />
            <el-option label="校级文体" :value="2" />
            <el-option label="院级思想" :value="3" />
            <el-option label="院级文体" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
            <el-option label="举办中" :value="2" />
            <el-option label="已结束" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="人数">
          <el-select v-model="query.full" placeholder="不限" clearable style="width: 110px">
            <el-option label="已满" :value="1" />
            <el-option label="未满" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="参加">
          <el-select v-model="query.joined" placeholder="不限" clearable style="width: 110px">
            <el-option label="已参加" :value="1" />
            <el-option label="未参加" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button v-if="canCreate" type="success" @click="openCreate">申请创建活动</el-button>
        </el-form-item>
      </el-form>

      <!-- 活动列表 -->
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="name" label="活动名称" min-width="150" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">{{ typeText(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="schoolName" label="创办学院" width="120">
          <template #default="{ row }">{{ row.schoolName || '校级' }}</template>
        </el-table-column>
        <el-table-column label="人数" width="90">
          <template #default="{ row }">{{ row.joinedCount }}/{{ row.limitNum }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">{{ statusText(row.status) }}</template>
        </el-table-column>
        <el-table-column label="时间" min-width="170">
          <template #default="{ row }">{{ fmt(row.startTime) }} ~ {{ fmt(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="我的状态" width="130">
          <template #default="{ row }">
            <el-tag v-if="row.joined" type="success">已参加({{ row.myScore }}分)</el-tag>
            <el-tag v-else type="info">未参加</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="detail(row)">详情</el-button>
            <el-button size="small" type="primary" :disabled="row.joined" @click="join(row)">报名</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 14px; justify-content: flex-end"
        layout="total, prev, pager, next, sizes"
        :total="total"
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        @current-change="load"
        @size-change="load"
      />
    </el-card>

    <!-- 活动详情 -->
    <el-dialog v-model="detailVisible" title="活动详情" width="520px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="名称">{{ current.name }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ typeText(current.type) }}</el-descriptions-item>
        <el-descriptions-item label="创办学院">{{ current.schoolName || '校级' }}</el-descriptions-item>
        <el-descriptions-item label="人数">{{ current.joinedCount }}/{{ current.limitNum }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ fmt(current.startTime) }} ~ {{ fmt(current.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ current.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="我的状态">
          <el-tag v-if="current.joined" type="success">已参加，可得 {{ current.myScore }} 分</el-tag>
          <el-tag v-else type="info">未参加</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" :disabled="current.joined" @click="join(current)">报名参加</el-button>
      </template>
    </el-dialog>

    <!-- 创建活动 -->
    <el-dialog v-model="createVisible" title="申请创建活动" width="520px">
      <el-form :model="createForm" label-width="90px">
        <el-form-item label="名称" required><el-input v-model="createForm.name" /></el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="createForm.type" style="width: 100%" @change="onTypeChange">
            <el-option label="校级思想" :value="1" />
            <el-option label="校级文体" :value="2" />
            <el-option label="院级思想" :value="3" />
            <el-option label="院级文体" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="创办学院">
          <el-select v-model="createSchoolId" placeholder="请选择创办学院（校级可不选）" clearable filterable
            :disabled="isSchoolLevel" style="width: 100%">
            <el-option v-for="s in schoolOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
          <div v-if="isSchoolLevel" class="form-tip">校级活动无需指定学院</div>
        </el-form-item>
        <el-form-item label="限制人数" required>
          <el-input-number v-model="createForm.limitNum" :min="1" />
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker v-model="createForm.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker v-model="createForm.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="负责老师">
          <el-select v-model="teacherIds" placeholder="请选择负责老师（可多选）" multiple filterable style="width: 100%">
            <el-option v-for="t in teacherOptions" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="createForm.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { studentApi } from '../api'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const detailVisible = ref(false)
const createVisible = ref(false)
const current = ref({})
// 下拉数据源与已选项
const schoolOptions = ref([])
const teacherOptions = ref([])
const createSchoolId = ref(null)
const teacherIds = ref([])

const query = reactive({ page: 1, size: 10, name: '', type: null, schoolId: null, status: null, full: null, joined: null })

const createForm = reactive({
  name: '', type: 1, schoolId: null, limitNum: 20, startTime: '', endTime: '', description: '', empInChargeIds: []
})

// 校级类型（思想/文体）无需指定学院
const isSchoolLevel = computed(() => createForm.type === 1 || createForm.type === 2)

// 学生会(2)/社团(3)成员可创建活动
const canCreate = computed(() => {
  const role = userStore.userInfo?.role
  return role === 2 || role === 3
})

function typeText(t) {
  return { 1: '校级思想', 2: '校级文体', 3: '院级思想', 4: '院级文体' }[t] || '-'
}
function statusText(s) {
  return { 2: '举办中', 5: '举办中', 6: '已结束' }[s] || '审核通过'
}
function fmt(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : '-'
}

async function load() {
  loading.value = true
  try {
    const data = await studentApi.activities(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  load()
}
function reset() {
  Object.assign(query, { page: 1, size: 10, name: '', type: null, schoolId: null, status: null, full: null, joined: null })
  load()
}

function detail(row) {
  current.value = row
  detailVisible.value = true
}

async function join(row) {
  try {
    await studentApi.joinActivity(row.id)
    ElMessage.success('报名成功')
    detailVisible.value = false
    load()
  } catch (e) {
    // 已提示
  }
}

function onTypeChange() {
  // 切换为校级时清空学院选择
  if (isSchoolLevel.value) createSchoolId.value = null
}

async function loadOptions() {
  if (schoolOptions.value.length > 0) return
  const data = await studentApi.activityOptions()
  schoolOptions.value = data.schools || []
  teacherOptions.value = data.teachers || []
}

async function openCreate() {
  Object.keys(createForm).forEach(k => delete createForm[k])
  Object.assign(createForm, {
    name: '', type: 1, schoolId: null, limitNum: 20, startTime: '', endTime: '', description: '', empInChargeIds: []
  })
  createSchoolId.value = null
  teacherIds.value = []
  await loadOptions()
  createVisible.value = true
}

async function submitCreate() {
  if (!createForm.name || !createForm.limitNum || !createForm.startTime || !createForm.endTime) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (!isSchoolLevel.value && !createSchoolId.value) {
    ElMessage.warning('院级活动请选择创办学院')
    return
  }
  // 校级活动不关联学院；选择学院则回填其 id
  createForm.schoolId = createSchoolId.value ? Number(createSchoolId.value) : null
  createForm.empInChargeIds = teacherIds.value.map(n => Number(n))
  await studentApi.createActivity(createForm)
  ElMessage.success('提交成功，等待审核')
  createVisible.value = false
  load()
}

onMounted(() => { load(); loadOptions() })
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 18px;
}
</style>
