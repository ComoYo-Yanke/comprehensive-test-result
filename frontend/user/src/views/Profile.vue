<template>
  <div>
    <el-card>
      <template #header>个人信息</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学号">{{ profile.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ profile.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ sexText(profile.sex) }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ profile.number }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ profile.clazzName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ profile.schoolName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ profile.majorNames || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入学时间">{{ profile.enrollTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ profile.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ profile.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ profile.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ profile.description || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 16px">
      <template #header>修改信息（仅电话/邮箱/密码/描述）</template>
      <el-form :model="form" label-width="80px" style="max-width: 480px">
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="原密码"><el-input v-model="form.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="form.newPassword" type="password" show-password /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { studentApi } from '../api'

const profile = ref({})
const form = reactive({ phone: '', email: '', description: '', oldPassword: '', newPassword: '' })

function sexText(sex) {
  return sex === 1 ? '男' : sex === 2 ? '女' : '-'
}

async function load() {
  profile.value = await studentApi.profile()
  form.phone = profile.value.phone
  form.email = profile.value.email
  form.description = profile.value.description
}

async function save() {
  await studentApi.updateProfile(form)
  ElMessage.success('修改成功')
  form.oldPassword = ''
  form.newPassword = ''
  load()
}

onMounted(load)
</script>
