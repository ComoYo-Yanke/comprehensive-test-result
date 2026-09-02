<template>
  <el-card>
    <template #header>我的违规记录</template>
    <el-form inline style="margin-bottom: 12px">
      <el-form-item label="原因"><el-input v-model="reasonFilter" placeholder="模糊搜索" clearable style="width: 160px" /></el-form-item>
      <el-form-item label="处分">
        <el-select v-model="punishmentFilter" placeholder="精确选择" clearable style="width: 130px">
          <el-option label="无" value="无" /><el-option label="通报批评" value="通报批评" /><el-option label="其他" value="其他" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
    </el-form>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="reason" label="原因" min-width="180" />
      <el-table-column prop="score" label="扣分" width="90" />
      <el-table-column prop="punishment" label="处分" width="110" />
      <el-table-column label="记录时间" width="170">
        <template #default="{ row }">{{ fmt(row.createTime) }}</template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next"
      :total="total" v-model:current-page="page" @current-change="load" />
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { studentApi } from '../api'

const list = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
// 筛选条件：原因模糊、处分精确
const reasonFilter = ref('')
const punishmentFilter = ref(null)

function fmt(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : '-'
}

async function load() {
  loading.value = true
  try {
    const data = await studentApi.penalties({
      page: page.value, size: 10,
      reason: reasonFilter.value || undefined,
      punishment: punishmentFilter.value ?? undefined
    })
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}
function search() { page.value = 1; load() }
function reset() { reasonFilter.value = ''; punishmentFilter.value = null; page.value = 1; load() }

onMounted(load)
</script>
