<template>
  <el-card>
    <div class="toolbar">
      <el-form inline>
        <el-form-item label="姓名"><el-input v-model="query.name" placeholder="模糊搜索" clearable style="width: 130px" /></el-form-item>
        <el-form-item label="原因"><el-input v-model="query.reason" placeholder="模糊搜索" clearable style="width: 160px" /></el-form-item>
        <el-form-item label="学生">
          <el-select v-model="query.studentId" placeholder="精确选择" filterable remote clearable
            :remote-method="searchStudents" :loading="studentLoading" style="width: 200px">
            <el-option v-for="s in studentOptions" :key="s.id" :label="s.name + '（' + s.username + '）'" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
      <el-button type="danger" @click="openAdd">添加违规记录</el-button>
    </div>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="name" label="学生" min-width="140" />
      <el-table-column prop="reason" label="原因" min-width="180" />
      <el-table-column prop="score" label="扣分" width="80" />
      <el-table-column prop="punishment" label="处分" width="110" />
      <el-table-column label="时间" width="170">
        <template #default="{ row }">{{ fmt(row.createTime) }}</template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next"
      :total="total" v-model:current-page="query.page" @current-change="load" />

    <el-dialog v-model="addVisible" title="添加违规记录" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="学生" required>
          <el-select v-model="form.studentId" placeholder="输入姓名/学号搜索学生" filterable remote clearable
            :remote-method="searchStudents" :loading="studentLoading" style="width: 100%" @change="onStudentChange">
            <el-option v-for="s in studentOptions" :key="s.id" :label="s.name + '（' + s.username + '）'" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因"><el-input v-model="form.reason" type="textarea" /></el-form-item>
        <el-form-item label="扣分" required><el-input-number v-model="form.score" :min="0" :precision="1" /></el-form-item>
        <el-form-item label="处分">
          <el-select v-model="form.punishment" style="width: 100%">
            <el-option label="无" value="无" /><el-option label="通报批评" value="通报批评" /><el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">提交</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { employeeApi } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const addVisible = ref(false)
// 学生远程搜索（姓名/学号模糊查询）
const studentOptions = ref([])
const studentLoading = ref(false)
const query = reactive({ page: 1, size: 10, studentId: null, name: '', reason: '' })
const form = reactive({ studentId: null, name: '', reason: '', score: 0, punishment: '无' })

function fmt(t) { return t ? t.replace('T', ' ').substring(0, 16) : '-' }

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
    const data = await employeeApi.penalties(query)
    list.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}
function search() { query.page = 1; load() }
function reset() {
  Object.assign(query, { page: 1, size: 10, studentId: null, name: '', reason: '' })
  load()
}
function openAdd() {
  Object.assign(form, { studentId: null, name: '', reason: '', score: 0, punishment: '无' })
  searchStudents('')
  addVisible.value = true
}
// 选中学生后自动带出其姓名（违规记录快照该姓名）
function onStudentChange() {
  const s = studentOptions.value.find(x => x.id === form.studentId)
  form.name = s ? s.name : ''
}
async function submit() {
  if (!form.studentId) {
    ElMessage.warning('请选择学生')
    return
  }
  await employeeApi.addPenalty(form)
  ElMessage.success('添加成功')
  addVisible.value = false
  load()
}
onMounted(() => { load(); searchStudents('') })
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 14px; }
</style>
