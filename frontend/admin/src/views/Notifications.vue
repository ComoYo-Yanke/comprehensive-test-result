<template>
  <el-card>
    <template #header>我的通知</template>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column prop="content" label="内容" min-width="260" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.isRead === 0" type="danger">未读</el-tag>
          <el-tag v-else type="info">已读</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="170">
        <template #default="{ row }">{{ fmt(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.isRead === 0" size="small" @click="read(row)">标记已读</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 14px; justify-content: flex-end" layout="total, prev, pager, next"
      :total="total" v-model:current-page="page" @current-change="load" />
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { employeeApi } from '../api'

const list = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

function fmt(t) { return t ? t.replace('T', ' ').substring(0, 16) : '-' }

async function load() {
  loading.value = true
  try {
    const data = await employeeApi.notifications({ page: page.value, size: 10 })
    list.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}
async function read(row) {
  await employeeApi.readNotification(row.id)
  ElMessage.success('已标记')
  load()
}
onMounted(load)
</script>
