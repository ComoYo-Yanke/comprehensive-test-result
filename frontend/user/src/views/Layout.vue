<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="brand">学生综测统计系统</div>
      <el-menu mode="horizontal" :default-active="$route.path" router class="nav">
        <el-menu-item index="/activities">活动</el-menu-item>
        <el-menu-item index="/extra-items">综测加分</el-menu-item>
        <el-menu-item index="/scores">综测成绩</el-menu-item>
        <el-menu-item index="/penalties">违规记录</el-menu-item>
        <el-menu-item index="/notifications">通知</el-menu-item>
        <el-menu-item index="/profile">个人信息</el-menu-item>
      </el-menu>
      <div class="user">
        <span>{{ userStore.userInfo?.name || '' }}</span>
        <el-button link type="danger" @click="logout">退出</el-button>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
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

// 建立 WebSocket 连接，接收实时通知
function connectWs() {
  const token = localStorage.getItem('token')
  if (!token) return
  const proto = location.protocol === 'https:' ? 'wss' : 'ws'
  ws = new WebSocket(`${proto}://${location.host}/ws?token=${token}`)
  ws.onmessage = e => {
    try {
      const msg = JSON.parse(e.data)
      ElNotification({ title: msg.title, message: msg.content, type: 'info', duration: 6000 })
    } catch (err) {
      // 忽略非 JSON 消息
    }
  }
  ws.onclose = () => {
    // 断线重连
    setTimeout(connectWs, 5000)
  }
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
.header {
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
}
.brand {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
  margin-right: 30px;
  white-space: nowrap;
}
.nav {
  flex: 1;
  border-bottom: none;
}
.user {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
