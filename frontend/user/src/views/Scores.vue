<template>
  <div>
    <el-card>
      <div class="toolbar">
        <span class="page-title">我的综测成绩</span>
        <el-button type="primary" @click="compute">计算综测成绩</el-button>
      </div>
      <el-form inline style="margin-bottom: 12px">
        <el-form-item label="学年">
          <el-input-number v-model="yearFilter" :min="2000" :max="2100" controls-position="right" placeholder="精确" style="width: 130px" />
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="semesterFilter" placeholder="精确选择" clearable style="width: 110px">
            <el-option label="春季" :value="1" /><el-option label="秋季" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="statusFilter" placeholder="精确选择" clearable style="width: 120px">
            <el-option label="未审核" :value="1" /><el-option label="审核通过" :value="2" /><el-option label="审核不通过" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="year" label="年份" width="80" />
        <el-table-column label="学期" width="90">
          <template #default="{ row }">{{ row.semester === 1 ? '春季' : '秋季' }}</template>
        </el-table-column>
        <el-table-column prop="activityScore" label="活动分" width="90" />
        <el-table-column prop="extraScore" label="其他加分" width="90" />
        <el-table-column prop="penaltyScore" label="违规扣分" width="90" />
        <el-table-column prop="score" label="总成绩" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ auditText(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="不通过原因" min-width="150">
          <template #default="{ row }">{{ row.reason || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 3" size="small" type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next"
        :total="total" v-model:current-page="page" @current-change="load" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { studentApi } from '../api'

const list = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
// 筛选条件：学年/学期/状态均精确匹配
const yearFilter = ref(null)
const semesterFilter = ref(null)
const statusFilter = ref(null)

function auditText(s) {
  return { 1: '未审核', 2: '审核通过', 3: '审核不通过' }[s] || '-'
}

async function load() {
  loading.value = true
  try {
    const params = {
      page: page.value, size: 10,
      status: statusFilter.value ?? undefined,
      year: yearFilter.value ?? undefined,
      semester: semesterFilter.value ?? undefined
    }
    const data = await studentApi.scores(params)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}
function search() { page.value = 1; load() }
function reset() {
  yearFilter.value = null
  semesterFilter.value = null
  statusFilter.value = null
  page.value = 1
  load()
}

async function compute() {
  await ElMessageBox.confirm('将根据你已参加并通过审核的活动、已通过加分项及违规记录计算综测成绩，是否继续？', '计算综测', {
    type: 'warning'
  })
  try {
    await studentApi.computeScore()
    ElMessage.success('计算成功，已提交审核')
    load()
  } catch (e) {
    // 已提示（如存在未审核/已达上限）
  }
}

async function del(row) {
  await studentApi.deleteScore(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.page-title {
  font-weight: bold;
}
</style>
