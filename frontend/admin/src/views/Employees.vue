<template>
  <el-card>
    <div class="toolbar">
      <el-form inline>
        <el-form-item label="姓名"><el-input v-model="query.name" placeholder="模糊搜索" clearable style="width: 130px" /></el-form-item>
        <el-form-item label="工号"><el-input v-model="query.username" placeholder="模糊搜索" clearable style="width: 130px" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="query.phone" placeholder="模糊搜索" clearable style="width: 130px" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.role" placeholder="精确选择" clearable style="width: 120px">
            <el-option label="教师" :value="1" /><el-option label="领导" :value="2" />
            <el-option label="辅导员" :value="3" /><el-option label="管理员" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="精确选择" clearable style="width: 110px">
            <el-option label="在职" :value="1" /><el-option label="离职" :value="2" /><el-option label="停用" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
      <el-button type="success" @click="openAdd">新增员工</el-button>
    </div>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="username" label="工号" width="110" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">{{ roleText(row.role) }}</template>
      </el-table-column>
      <el-table-column prop="schoolNames" label="任职学院" min-width="140" />
      <el-table-column prop="clazzName" label="管理班级" width="110" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">{{ statusText(row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next, sizes"
      :total="total" v-model:current-page="query.page" v-model:page-size="query.size" @current-change="load" @size-change="load" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑员工' : '新增员工'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="工号" required><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="姓名" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="身份证号"><el-input v-model="form.number" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.sex" style="width: 100%"><el-option label="男" :value="1" /><el-option label="女" :value="2" /></el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="教师" :value="1" /><el-option label="领导" :value="2" />
            <el-option label="辅导员" :value="3" /><el-option label="管理员" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="任职学院">
          <el-select v-model="schoolIds" placeholder="请选择任职学院（可多选）" multiple filterable style="width: 100%">
            <el-option v-for="s in schools" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="管理班级">
          <el-select v-model="form.clazzId" placeholder="辅导员管理的班级" filterable clearable style="width: 100%">
            <el-option v-for="c in clazzsAll" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
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
import { employeeApi, optionApi } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const query = reactive({ page: 1, size: 10, name: '', username: '', phone: '', role: null, status: null })
const form = reactive({})

// 下拉数据源
const schools = ref([])
const clazzsAll = ref([])
const schoolIds = ref([])

const toNum = v => (v === '' || v === null || v === undefined) ? null : Number(v)

function roleText(r) { return { 1: '教师', 2: '领导', 3: '辅导员', 4: '管理员' }[r] || '-' }
function statusText(s) { return { 1: '在职', 2: '离职', 3: '停用' }[s] || '-' }

async function loadOptions() {
  const [s, c] = await Promise.all([optionApi.schools(), optionApi.clazzs()])
  schools.value = s || []
  clazzsAll.value = c || []
}

async function load() {
  loading.value = true
  try {
    const data = await employeeApi.employees(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}
function search() { query.page = 1; load() }
function reset() {
  Object.assign(query, { page: 1, size: 10, name: '', username: '', phone: '', role: null, status: null })
  load()
}
function openAdd() {
  Object.keys(form).forEach(k => delete form[k])
  form.sex = 1
  form.role = 1
  form.status = 1
  schoolIds.value = []
  dialogVisible.value = true
}
function openEdit(row) {
  Object.assign(form, row)
  schoolIds.value = row.schoolIds ? row.schoolIds.slice() : []
  dialogVisible.value = true
}

async function save() {
  if (!form.username || !form.name) {
    ElMessage.warning('请填写工号和姓名')
    return
  }
  form.clazzId = toNum(form.clazzId)
  // 始终回传学院列表（为空则清除已关联学院）
  const params = { schoolIds: schoolIds.value.map(n => Number(n)) }
  if (form.id) {
    await employeeApi.updateEmployee(form, params)
  } else {
    await employeeApi.addEmployee(form, params)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}
async function del(row) {
  await ElMessageBox.confirm(`确认删除员工 ${row.name}？`, '提示', { type: 'warning' })
  await employeeApi.deleteEmployee(row.id)
  ElMessage.success('已删除')
  load()
}
onMounted(() => { load(); loadOptions() })
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 14px; }
</style>
