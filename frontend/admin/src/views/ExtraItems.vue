<template>
  <el-card>
    <el-tabs v-model="tab">
      <el-tab-pane label="全部加分项" name="all" />
      <el-tab-pane label="本班加分项" name="myclass" />
    </el-tabs>

    <el-form inline>
      <el-form-item label="名称"><el-input v-model="query.name" placeholder="模糊搜索" clearable style="width: 140px" /></el-form-item>
      <el-form-item label="原因"><el-input v-model="query.reason" placeholder="模糊搜索" clearable style="width: 150px" /></el-form-item>
      <el-form-item v-if="tab === 'all'" label="学生">
        <el-select v-model="query.studentId" placeholder="精确选择" filterable remote clearable
          :remote-method="searchStudents" :loading="studentLoading" style="width: 200px">
          <el-option v-for="s in studentOptions" :key="s.id" :label="s.name + '（' + s.username + '）'" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="精确选择" clearable style="width: 120px">
          <el-option label="未审核" :value="1" /><el-option label="审核通过" :value="2" /><el-option label="审核不通过" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="name" label="名称" min-width="150" />
      <el-table-column prop="studentUsername" label="学号" width="110" />
      <el-table-column prop="studentName" label="姓名" width="100" />
      <el-table-column prop="score" label="分数" width="80" />
      <el-table-column label="佐证" width="90">
        <template #default="{ row }">
          <el-image v-if="row.evidence" :src="row.evidence" :preview-src-list="[row.evidence]" style="width: 40px; height: 40px" fit="cover" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ { 1: '未审核', 2: '通过', 3: '不通过' }[row.status] || '-' }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" min-width="120" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" size="small" type="primary" @click="openReview(row)">审核</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next"
      :total="total" v-model:current-page="query.page" @current-change="load" />

    <el-dialog v-model="reviewVisible" title="审核加分项" width="440px">
      <p>学生：{{ current.studentName || '-' }}（{{ current.studentUsername || '-' }}）</p>
      <p>加分项：{{ current.name }}</p>
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
const tab = ref('all')
// 学生远程搜索（姓名/学号模糊查询）
const studentOptions = ref([])
const studentLoading = ref(false)
const query = reactive({ page: 1, size: 10, status: null, name: '', reason: '', studentId: null })
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
    const api = tab.value === 'all' ? employeeApi.extraItems : employeeApi.myClassExtraItems
    const data = await api(query)
    list.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}
function search() { query.page = 1; load() }
function reset() {
  Object.assign(query, { page: 1, size: 10, status: null, name: '', reason: '', studentId: null })
  load()
}
// 切换到本班时不筛选具体学生
watch(tab, () => { query.page = 1; query.studentId = null; load() })

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
  const api = tab.value === 'all' ? employeeApi.reviewExtraItem : employeeApi.reviewMyClassExtraItem
  await api(current.value.id, reviewForm)
  ElMessage.success('审核完成')
  reviewVisible.value = false
  load()
}
onMounted(() => { load(); searchStudents('') })
</script>
