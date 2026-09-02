import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 统一的 axios 实例
const request = axios.create({
  baseURL: '/api/v1',
  timeout: 10000
})

// 请求拦截：附加 token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

// 响应拦截：统一处理 {code, msg, data}
request.interceptors.response.use(
  res => {
    const data = res.data
    if (data.code === 1) {
      return data.data
    }
    // 未登录 / 无权限
    if (data.code === 401 || data.code === 403) {
      ElMessage.error(data.msg || '登录已失效')
      localStorage.removeItem('token')
      router.push('/login')
      return Promise.reject(new Error(data.msg))
    }
    ElMessage.error(data.msg || '请求失败')
    return Promise.reject(new Error(data.msg))
  },
  err => {
    ElMessage.error('网络错误，请稍后重试')
    return Promise.reject(err)
  }
)

export default request
