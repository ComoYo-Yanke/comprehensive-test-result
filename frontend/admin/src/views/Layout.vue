<template>
  <el-container class="layout">
    <el-aside width="200px" class="aside">
      <div class="brand">综测管理系统</div>
      <el-menu :default-active="$route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="/dashboard">数据统计</el-menu-item>
        <el-menu-item index="/students">学生管理</el-menu-item>
        <el-menu-item index="/employees">员工管理</el-menu-item>
        <el-menu-item index="/schools">学院管理</el-menu-item>
        <el-menu-item index="/majors">专业管理</el-menu-item>
        <el-menu-item index="/clazzs">班级管理</el-menu-item>
        <el-menu-item index="/activities">活动审核</el-menu-item>
        <el-menu-item index="/extra-items">加分审核</el-menu-item>
        <el-menu-item index="/scores">综测审核</el-menu-item>
        <el-menu-item index="/penalties">违规记录</el-menu-item>
        <el-menu-item index="/notifications">通知</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ userStore.userInfo?.name || '' }}</span>
        <el-button link type="danger" @click="logout">退出</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
let ws = null

function connectWs() {
  const token = localStorage.getItem('token')
  if (!token) return
  const proto = location.protocol === 'https:' ? 'wss' : 'ws'
  ws = new WebSocket(`${proto}://${location.host}/ws?token=${token}`)
  ws.onmessage = e => {
    try {
      const msg = JSON.parse(e.data)
      ElNotification({ title: msg.title, message: msg.content, type: 'warning', duration: 8000 })
    } catch (err) {
      // 忽略
    }
  }
  ws.onclose = () => setTimeout(connectWs, 5000)
}

function logout() {
  userStore.logout()
  router.push('/login')
}

onMounted(connectWs)
onBeforeUnmount(() => ws && ws.close())
</script>

<style scoped>
.layout {
  height: 100vh;
}
.aside {
  background: #304156;
}
.brand {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-weight: bold;
}
.aside :deep(.el-menu) {
  border-right: none;
}
.header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 14px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
}
</style>
