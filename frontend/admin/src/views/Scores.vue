<template>
  <el-card>
    <el-tabs v-model="tab">
      <el-tab-pane label="待审核" name="pending" />
      <el-tab-pane label="已审核通过" name="approved" />
    </el-tabs>

    <el-form inline>
      <el-form-item label="学生">
        <el-select v-model="query.studentId" placeholder="精确选择" filterable remote clearable
          :remote-method="searchStudents" :loading="studentLoading" style="width: 210px">
          <el-option v-for="s in studentOptions" :key="s.id" :label="s.name + '（' + s.username + '）'" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="学年">
        <el-input-number v-model="query.year" :min="2000" :max="2100" controls-position="right" placeholder="精确" style="width: 130px" />
      </el-form-item>
      <el-form-item label="学期">
        <el-select v-model="query.semester" placeholder="精确选择" clearable style="width: 110px">
          <el-option label="春季" :value="1" /><el-option label="秋季" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="studentUsername" label="学号" width="110" />
      <el-table-column prop="studentName" label="姓名" width="100" />
      <el-table-column prop="year" label="年份" width="80" />
      <el-table-column label="学期" width="80">
        <template #default="{ row }">{{ row.semester === 1 ? '春季' : '秋季' }}</template>
      </el-table-column>
      <el-table-column prop="activityScore" label="活动分" width="80" />
      <el-table-column prop="extraScore" label="加分" width="80" />
      <el-table-column prop="penaltyScore" label="扣分" width="80" />
      <el-table-column prop="score" label="总成绩" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">{{ { 1: '未审核', 2: '通过', 3: '不通过' }[row.status] || '-' }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" min-width="120" />
      <el-table-column v-if="tab === 'pending'" label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openReview(row)">审核</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next"
      :total="total" v-model:current-page="query.page" @current-change="load" />

    <el-dialog v-model="reviewVisible" title="审核综测成绩" width="440px">
      <p>学生：{{ current.studentName || '-' }}（{{ current.studentUsername || '-' }}），总成绩：{{ current.score }}</p>
      <el-form label-width="90px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="reviewForm.approve">
            <el-radio :value="true">通过</el-radio>
            <el-radio :value="false">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!reviewForm.approve" label="原因" required>
          <el-input v-model="reviewForm.reason" type="textarea" placeholder="不通过需填写原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { employeeApi } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const reviewVisible = ref(false)
const current = ref({})
const tab = ref('pending')
// 学生远程搜索（姓名/学号模糊查询）
const studentOptions = ref([])
const studentLoading = ref(false)
const query = reactive({ page: 1, size: 10, studentId: null, year: null, semester: null })
const reviewForm = reactive({ approve: true, reason: '' })

async function searchStudents(keyword) {
  studentLoading.value = true
  try {
    const data = await employeeApi.students({ page: 1, size: 20, name: keyword || '' })
    studentOptions.value = data.records || []
  } finally {
    studentLoading.value = false
  }
}

async function load() {
  loading.value = true
  try {
    const api = tab.value === 'pending' ? employeeApi.pendingScores : employeeApi.approvedScores
    const data = await api(query)
    list.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}
function search() { query.page = 1; load() }
function reset() {
  Object.assign(query, { page: 1, size: 10, studentId: null, year: null, semester: null })
  load()
}
watch(tab, () => { query.page = 1; load() })

function openReview(row) {
  current.value = row
  reviewForm.approve = true
  reviewForm.reason = ''
  reviewVisible.value = true
}
async function submitReview() {
  if (!reviewForm.approve && !reviewForm.reason) {
    ElMessage.warning('请填写不通过原因')
    return
  }
  await employeeApi.reviewScore(current.value.id, reviewForm)
  ElMessage.success('审核完成')
  reviewVisible.value = false
  load()
}
onMounted(() => { load(); searchStudents('') })
</script>
