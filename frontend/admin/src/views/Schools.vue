<template>
  <el-card>
    <div class="toolbar">
      <el-form inline>
        <el-form-item label="名称"><el-input v-model="query.name" placeholder="模糊搜索" clearable style="width: 150px" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="精确选择" clearable style="width: 110px">
            <el-option label="正常" :value="1" /><el-option label="解散" :value="2" /><el-option label="未启用" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
      <el-button type="success" @click="openAdd">新增学院</el-button>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="学院名称" min-width="160" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ { 1: '正常', 2: '解散', 3: '未启用' }[row.status] || '-' }}</template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="180" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next"
      :total="total" v-model:current-page="query.page" @current-change="load" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑学院' : '新增学院'" width="420px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="正常" :value="1" /><el-option label="解散" :value="2" /><el-option label="未启用" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { employeeApi } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const query = reactive({ page: 1, size: 10, name: '', status: null })
const form = reactive({})

async function load() {
  loading.value = true
  try {
    const data = await employeeApi.schools(query)
    list.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}
function search() { query.page = 1; load() }
function reset() { Object.assign(query, { page: 1, size: 10, name: '', status: null }); load() }
function openAdd() { Object.keys(form).forEach(k => delete form[k]); dialogVisible.value = true }
function openEdit(row) { Object.assign(form, row); dialogVisible.value = true }
async function save() {
  if (form.id) await employeeApi.updateSchool(form)
  else await employeeApi.addSchool(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}
async function del(row) {
  await ElMessageBox.confirm(`确认删除学院 ${row.name}？`, '提示', { type: 'warning' })
  await employeeApi.deleteSchool(row.id)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 14px; }
</style>
