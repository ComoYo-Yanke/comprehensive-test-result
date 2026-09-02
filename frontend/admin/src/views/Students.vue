<template>
  <el-card>
    <div class="toolbar">
      <el-form inline>
        <el-form-item label="姓名"><el-input v-model="query.name" placeholder="模糊搜索" clearable style="width: 130px" /></el-form-item>
        <el-form-item label="学号"><el-input v-model="query.username" placeholder="模糊搜索" clearable style="width: 130px" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="query.phone" placeholder="模糊搜索" clearable style="width: 130px" /></el-form-item>
        <el-form-item label="学院">
          <el-select v-model="query.schoolId" placeholder="精确选择" clearable filterable style="width: 150px" @change="onQuerySchoolChange">
            <el-option v-for="s in schools" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="query.clazzId" placeholder="精确选择" clearable filterable style="width: 150px">
            <el-option v-for="c in queryClazzOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.role" placeholder="精确选择" clearable style="width: 130px">
            <el-option label="普通学生" :value="1" /><el-option label="学生会" :value="2" />
            <el-option label="社团" :value="3" /><el-option label="外国留学生" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="精确选择" clearable style="width: 110px">
            <el-option label="在读" :value="1" /><el-option label="毕业" :value="2" />
            <el-option label="开除" :value="3" /><el-option label="休学" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
      <el-button type="success" @click="openAdd">新增学生</el-button>
    </div>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="username" label="学号" width="110" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="clazzName" label="班级" width="110" />
      <el-table-column prop="schoolName" label="学院" width="120" />
      <el-table-column prop="majorNames" label="专业" min-width="140" />
      <el-table-column label="角色" width="110">
        <template #default="{ row }">{{ roleText(row.role) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">{{ statusText(row.status) }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="电话" width="120" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="resetPwd(row)">重置密码</el-button>
          <el-button size="small" type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next, sizes"
      :total="total" v-model:current-page="query.page" v-model:page-size="query.size" @current-change="load" @size-change="load" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑学生' : '新增学生'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="学号" required><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="姓名" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.sex" style="width: 100%"><el-option label="男" :value="1" /><el-option label="女" :value="2" /></el-select>
        </el-form-item>
        <el-form-item label="身份证号"><el-input v-model="form.number" /></el-form-item>
        <el-form-item label="学院">
          <el-select v-model="form.schoolId" placeholder="请选择学院" filterable clearable style="width: 100%"
            @change="onSchoolChange">
            <el-option v-for="s in schools" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="form.clazzId" placeholder="请选择班级" filterable clearable style="width: 100%"
            @change="onClazzChange">
            <el-option v-for="c in clazzOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业">
          <el-select v-model="majorIds" placeholder="请选择专业（可多选）" multiple filterable style="width: 100%">
            <el-option v-for="m in majorOptions" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="入学年月"><el-input v-model="form.enrollTime" placeholder="2022-09" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="普通学生" :value="1" /><el-option label="学生会" :value="2" />
            <el-option label="社团" :value="3" /><el-option label="外国留学生" :value="4" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { employeeApi, optionApi } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const query = reactive({ page: 1, size: 10, name: '', username: '', phone: '', schoolId: null, clazzId: null, role: null, status: null })
const form = reactive({})

// 下拉数据源
const schools = ref([])
const majorsAll = ref([])
const clazzsAll = ref([])
const majorIds = ref([])

const toNum = v => (v === '' || v === null || v === undefined) ? null : Number(v)
// 按所选学院过滤班级/专业（弹窗）
const clazzOptions = computed(() => clazzsAll.value.filter(c => !form.schoolId || c.schoolId === form.schoolId))
const majorOptions = computed(() => majorsAll.value.filter(m => !form.schoolId || m.schoolId === form.schoolId))
// 查询栏班级：随所选学院联动
const queryClazzOptions = computed(() => clazzsAll.value.filter(c => !query.schoolId || c.schoolId === query.schoolId))

function roleText(r) { return { 1: '普通学生', 2: '学生会', 3: '社团', 4: '外国留学生' }[r] || '-' }
function statusText(s) { return { 1: '在读', 2: '毕业', 3: '开除', 4: '休学' }[s] || '-' }

async function loadOptions() {
  const [s, m, c] = await Promise.all([optionApi.schools(), optionApi.majors(), optionApi.clazzs()])
  schools.value = s || []
  majorsAll.value = m || []
  clazzsAll.value = c || []
}

async function load() {
  loading.value = true
  try {
    const data = await employeeApi.students(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function search() { query.page = 1; load() }
function reset() {
  Object.assign(query, { page: 1, size: 10, name: '', username: '', phone: '', schoolId: null, clazzId: null, role: null, status: null })
  load()
}
// 查询栏更换学院时清空班级
function onQuerySchoolChange() { query.clazzId = null }
function openAdd() {
  Object.keys(form).forEach(k => delete form[k])
  form.schoolId = null
  form.sex = 1
  form.role = 1
  form.status = 1
  majorIds.value = []
  dialogVisible.value = true
}
function openEdit(row) {
  Object.assign(form, row)
  majorIds.value = row.majorIds ? row.majorIds.slice() : []
  dialogVisible.value = true
}

// 更换学院时清空已选班级/专业，防止跨学院误选
function onSchoolChange() {
  form.clazzId = null
  majorIds.value = []
}
// 选择班级后自动带出所属学院
function onClazzChange() {
  const clazz = clazzsAll.value.find(c => c.id === form.clazzId)
  if (clazz && clazz.schoolId) {
    form.schoolId = clazz.schoolId
  }
}

async function save() {
  if (!form.username || !form.name) {
    ElMessage.warning('请填写学号和姓名')
    return
  }
  form.schoolId = toNum(form.schoolId)
  form.clazzId = toNum(form.clazzId)
  // 始终回传专业列表（为空则清除已关联专业）
  const params = { majorIds: majorIds.value.map(n => Number(n)) }
  if (form.id) {
    await employeeApi.updateStudent(form, params)
  } else {
    await employeeApi.addStudent(form, params)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function resetPwd(row) {
  await ElMessageBox.confirm('确认重置该学生密码为默认密码（身份证后6位）？', '提示', { type: 'warning' })
  await employeeApi.resetStudentPwd(row.id)
  ElMessage.success('已重置')
}

async function del(row) {
  await ElMessageBox.confirm(`确认删除学生 ${row.name}？`, '提示', { type: 'warning' })
  await employeeApi.deleteStudent(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(() => { load(); loadOptions() })
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 14px;
}
</style>
