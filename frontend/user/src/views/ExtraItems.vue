<template>
  <div>
    <el-card>
      <div class="toolbar">
        <span class="page-title">我的综测加分项</span>
        <el-button type="primary" @click="addVisible = true">添加加分项</el-button>
      </div>
      <el-form inline style="margin-bottom: 12px">
        <el-form-item label="名称"><el-input v-model="nameFilter" placeholder="模糊搜索" clearable style="width: 150px" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="statusFilter" placeholder="精确选择" clearable style="width: 120px">
            <el-option label="未审核" :value="1" /><el-option label="审核通过" :value="2" /><el-option label="审核不通过" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="score" label="分数" width="90" />
        <el-table-column label="佐证" width="120">
          <template #default="{ row }">
            <el-image v-if="row.evidence" :src="row.evidence" :preview-src-list="[row.evidence]" style="width: 40px; height: 40px" fit="cover" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ auditText(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="不通过原因" min-width="150">
          <template #default="{ row }">{{ row.reason || '-' }}</template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">{{ fmt(row.createTime) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next"
        :total="total" v-model:current-page="page" @current-change="load" />
    </el-card>

    <el-dialog v-model="addVisible" title="添加综测加分项" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分数" required><el-input-number v-model="form.score" :min="0" :precision="1" /></el-form-item>
        <el-form-item label="佐证图片">
          <el-upload :http-request="doUpload" :show-file-list="false">
            <el-button>上传图片</el-button>
          </el-upload>
          <el-image v-if="form.evidence" :src="form.evidence" style="width: 60px; height: 60px; margin-top: 6px" fit="cover" />
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">提交（将通知辅导员）</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { studentApi, fileApi } from '../api'

const list = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const addVisible = ref(false)
// 筛选条件：名称模糊、状态精确
const nameFilter = ref('')
const statusFilter = ref(null)
const form = reactive({ name: '', score: 0, evidence: '', description: '' })

function auditText(s) {
  return { 1: '未审核', 2: '审核通过', 3: '审核不通过' }[s] || '-'
}
function fmt(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : '-'
}

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: 10, status: statusFilter.value, name: nameFilter.value || undefined }
    const data = await studentApi.extraItems(params)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}
function search() { page.value = 1; load() }
function reset() { nameFilter.value = ''; statusFilter.value = null; page.value = 1; load() }

async function doUpload(option) {
  const fd = new FormData()
  fd.append('file', option.file)
  const url = await fileApi.upload(fd)
  form.evidence = url
  ElMessage.success('上传成功')
}

async function submit() {
  if (!form.name) {
    ElMessage.warning('请填写名称')
    return
  }
  await studentApi.addExtraItem(form)
  ElMessage.success('提交成功')
  addVisible.value = false
  Object.assign(form, { name: '', score: 0, evidence: '', description: '' })
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
