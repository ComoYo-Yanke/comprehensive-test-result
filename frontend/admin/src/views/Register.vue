<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <h2 class="title">员工注册</h2>
      <el-form :model="form">
        <el-form-item><el-input v-model="form.username" placeholder="工号" /></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" show-password /></el-form-item>
        <el-form-item><el-input v-model="form.name" placeholder="姓名" /></el-form-item>
        <el-form-item><el-input v-model="form.phone" placeholder="电话" /></el-form-item>
        <el-form-item><el-input v-model="form.number" placeholder="身份证号" /></el-form-item>
        <el-form-item>
          <el-select v-model="form.sex" placeholder="性别" style="width: 100%">
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="form.role" placeholder="角色" style="width: 100%">
            <el-option label="教师" :value="1" />
            <el-option label="领导" :value="2" />
            <el-option label="辅导员" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" placeholder="验证码" maxlength="4" class="captcha-input" />
            <img v-if="captchaImg" :src="'data:image/png;base64,' + captchaImg" class="captcha-img"
              title="看不清？点击刷新" @click="refreshCaptcha" />
            <el-icon v-else class="captcha-loading" @click="refreshCaptcha"><Refresh /></el-icon>
          </div>
        </el-form-item>
        <el-button type="primary" class="login-btn" :loading="loading" @click="doRegister">注册</el-button>
        <el-button class="login-btn" style="margin-left: 0; margin-top: 10px" @click="$router.push('/login')">返回登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'

const router = useRouter()
const loading = ref(false)
const captchaImg = ref('')
const form = reactive({ username: '', password: '', name: '', phone: '', number: '', sex: 1, role: 1, captchaId: '', captchaCode: '' })

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

async function doRegister() {
  if (!form.username || !form.password || !form.name) {
    ElMessage.warning('请填写工号、密码、姓名')
    return
  }
  if (!form.captchaCode) {
    ElMessage.warning('请输入验证码')
    return
  }
  loading.value = true
  try {
    await authApi.register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    // 验证码一次性使用，失败后刷新
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
  margin-bottom: 20px;
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
</style>
