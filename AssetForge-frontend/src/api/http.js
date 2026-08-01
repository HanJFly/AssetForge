import axios from 'axios'
import { ElMessage } from 'element-plus'

const baseURL = import.meta.env.DEV
  ? '/api'
  : (import.meta.env.VITE_API_BASE_URL || '/api')

const http = axios.create({
  baseURL,
  timeout: 30000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('assetforge-token')
  if (token) {
    config.headers.token = token
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (typeof payload?.code !== 'undefined' && payload.code !== 200) {
      ElMessage.error(payload.msg || '请求失败')
      return Promise.reject(payload)
    }
    return payload
  },
  (error) => {
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default http
