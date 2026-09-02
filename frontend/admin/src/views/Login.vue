<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <h2 class="title">学生综测统计系统</h2>
      <p class="subtitle">管理端登录</p>
      <el-form :model="form" @keyup.enter="doLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="请输入工号" size="large">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password>
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" placeholder="验证码" size="large" maxlength="4" class="captcha-input" />
            <img v-if="captchaImg" :src="'data:image/png;base64,' + captchaImg" class="captcha-img"
              title="看不清？点击刷新" @click="refreshCaptcha" />
            <el-icon v-else class="captcha-loading" @click="refreshCaptcha"><Refresh /></el-icon>
          </div>
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="doLogin">登录</el-button>
      </el-form>
      <div class="extra">
        <el-button link type="primary" @click="$router.push('/register')">注册账号</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const captchaImg = ref('')
const form = reactive({ username: '', password: '', captchaId: '', captchaCode: '' })

async function refreshCaptcha() {
  try {
    const data = await authApi.captcha()
    form.captchaId = data.captchaId
    captchaImg.value = data.imgBase64
    form.captchaCode = ''
  } catch (e) {
    // 已提示
  }
}

async function doLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入工号和密码')
    return
  }
  if (!form.captchaCode) {
    ElMessage.warning('请输入验证码')
    return
  }
  loading.value = true
  try {
    const data = await authApi.login(form)
    userStore.setLogin(data.token, data)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    // 验证码一次性使用，失败后无论何种原因都刷新图片
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(refreshCaptcha)
</script>

<style scoped>
.login-wrap {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #303f9f, #409eff);
}
.login-card {
  width: 380px;
  padding: 10px 20px;
}
.title {
  text-align: center;
  margin-bottom: 4px;
}
.subtitle {
  text-align: center;
  color: #999;
  margin: 0 0 20px;
}
.captcha-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}
.captcha-input {
  flex: 1;
}
.captcha-img {
  height: 40px;
  width: 110px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
}
.captcha-loading {
  font-size: 22px;
  color: #999;
  cursor: pointer;
  margin-left: auto;
}
.login-btn {
  width: 100%;
}
.extra {
  text-align: center;
  margin-top: 10px;
}
</style>
