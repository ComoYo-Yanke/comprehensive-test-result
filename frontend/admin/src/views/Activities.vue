<template>
  <el-card>
    <div class="toolbar">
      <el-form inline>
        <el-form-item label="名称"><el-input v-model="query.name" placeholder="模糊搜索" clearable style="width: 150px" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" placeholder="精确选择" clearable style="width: 120px">
            <el-option label="校级思想" :value="1" /><el-option label="校级文体" :value="2" />
            <el-option label="院级思想" :value="3" /><el-option label="院级文体" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院">
          <el-select v-model="query.schoolId" placeholder="精确选择(校级不选)" clearable filterable style="width: 150px">
            <el-option v-for="s in schools" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="精确选择" clearable style="width: 120px">
            <el-option label="未审核" :value="1" /><el-option label="审核通过" :value="2" />
            <el-option label="审核不通过" :value="3" /><el-option label="已取消" :value="4" />
            <el-option label="举办中" :value="5" /><el-option label="已结束" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="name" label="活动名称" min-width="150" />
      <el-table-column label="类型" width="110">
        <template #default="{ row }">{{ { 1: '校级思想', 2: '校级文体', 3: '院级思想', 4: '院级文体' }[row.type] || '-' }}</template>
      </el-table-column>
      <el-table-column prop="schoolName" label="学院" width="120">
        <template #default="{ row }">{{ row.schoolName || '校级' }}</template>
      </el-table-column>
      <el-table-column label="人数" width="90">
        <template #default="{ row }">{{ row.joinedCount }}/{{ row.limitNum }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ statusText(row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" size="small" type="primary" @click="openReview(row)">审核</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next"
      :total="total" v-model:current-page="query.page" @current-change="load" />

    <el-dialog v-model="reviewVisible" title="审核活动" width="440px">
      <p>活动：{{ current.name }}</p>
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { employeeApi, optionApi } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const reviewVisible = ref(false)
const current = ref({})
const query = reactive({ page: 1, size: 10, name: '', type: null, schoolId: null, status: null })
const reviewForm = reactive({ approve: true, reason: '' })
// 学院下拉数据源
const schools = ref([])

async function loadSchools() {
  schools.value = (await optionApi.schools()) || []
}

function statusText(s) {
  return { 1: '未审核', 2: '审核通过', 3: '审核不通过', 4: '已取消', 5: '举办中', 6: '已结束' }[s] || '-'
}

async function load() {
  loading.value = true
  try {
    const data = await employeeApi.activities(query)
    list.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}
function search() { query.page = 1; load() }
function reset() {
  Object.assign(query, { page: 1, size: 10, name: '', type: null, schoolId: null, status: null })
  load()
}
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
  await employeeApi.reviewActivity(current.value.id, reviewForm)
  ElMessage.success('审核完成')
  reviewVisible.value = false
  load()
}
onMounted(() => { load(); loadSchools() })
</script>

<style scoped>
.toolbar { margin-bottom: 14px; }
</style>
