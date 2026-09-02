<template>
  <el-card>
    <div class="toolbar">
      <el-form inline>
        <el-form-item><el-input v-model="query.name" placeholder="班级名称" clearable style="width: 150px" /></el-form-item>
        <el-form-item>
          <el-select v-model="query.schoolId" placeholder="所属学院" clearable filterable style="width: 150px" @change="query.majorId = null">
            <el-option v-for="s in schools" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="query.majorId" placeholder="所属专业" clearable filterable style="width: 150px">
            <el-option v-for="m in majorQueryOptions" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="精确选择" clearable style="width: 110px">
            <el-option label="在读" :value="1" /><el-option label="撤班" :value="2" /><el-option label="已毕业" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
      <el-button type="success" @click="openAdd">新增班级</el-button>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="班级名称" min-width="140" />
      <el-table-column prop="schoolName" label="所属学院" width="140" />
      <el-table-column prop="majorName" label="所属专业" width="140" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ { 1: '在读', 2: '撤班', 3: '已毕业' }[row.status] || '-' }}</template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="160" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next"
      :total="total" v-model:current-page="query.page" @current-change="load" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑班级' : '新增班级'" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="所属学院" required>
          <el-select v-model="form.schoolId" placeholder="请选择学院" filterable style="width: 100%" @change="onSchoolChange">
            <el-option v-for="s in schools" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属专业" required>
          <el-select v-model="form.majorId" placeholder="请选择专业" filterable style="width: 100%" @change="onMajorChange">
            <el-option v-for="m in majorDialogOptions" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="在读" :value="1" /><el-option label="撤班" :value="2" /><el-option label="已毕业" :value="3" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { employeeApi, optionApi } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const query = reactive({ page: 1, size: 10, name: '', schoolId: null, majorId: null, status: null })
const form = reactive({})

// 下拉数据源
const schools = ref([])
const majorsAll = ref([])

const toNum = v => (v === '' || v === null || v === undefined) ? null : Number(v)
// 查询栏专业：可按所选学院联动过滤
const majorQueryOptions = computed(() => majorsAll.value.filter(m => !query.schoolId || m.schoolId === query.schoolId))
// 弹窗专业：仅展示所选学院下的专业
const majorDialogOptions = computed(() => majorsAll.value.filter(m => !form.schoolId || m.schoolId === form.schoolId))

async function loadOptions() {
  const [s, m] = await Promise.all([optionApi.schools(), optionApi.majors()])
  schools.value = s || []
  majorsAll.value = m || []
}
async function load() {
  loading.value = true
  try {
    const data = await employeeApi.clazzs(query)
    list.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}
function search() { query.page = 1; load() }
function reset() {
  Object.assign(query, { page: 1, size: 10, name: '', schoolId: null, majorId: null, status: null })
  load()
}
function openAdd() {
  Object.keys(form).forEach(k => delete form[k])
  form.schoolId = null
  form.majorId = null
  dialogVisible.value = true
}
function openEdit(row) { Object.assign(form, row); dialogVisible.value = true }
// 更换学院时清空已选专业，防止跨学院误选
function onSchoolChange() { form.majorId = null }
// 选择专业后自动带出所属学院（防止未选学院时直接选专业）
function onMajorChange() {
  const major = majorsAll.value.find(m => m.id === form.majorId)
  if (major && major.schoolId) form.schoolId = major.schoolId
}
async function save() {
  if (!form.name) {
    ElMessage.warning('请填写班级名称')
    return
  }
  if (!form.schoolId) {
    ElMessage.warning('请选择所属学院')
    return
  }
  if (!form.majorId) {
    ElMessage.warning('请选择所属专业')
    return
  }
  form.schoolId = toNum(form.schoolId)
  form.majorId = toNum(form.majorId)
  if (form.id) await employeeApi.updateClazz(form)
  else await employeeApi.addClazz(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}
async function del(row) {
  await ElMessageBox.confirm(`确认删除班级 ${row.name}？`, '提示', { type: 'warning' })
  await employeeApi.deleteClazz(row.id)
  ElMessage.success('已删除')
  load()
}
onMounted(() => { load(); loadOptions() })
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 14px; }
</style>
