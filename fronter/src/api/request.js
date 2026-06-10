import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

request.interceptors.response.use(
  (response) => {
    const { data } = response
    if (data.code !== 1) {
      if (!response.config?.silent) {
        ElMessage.error(data.msg || '请求失败')
      }
      return Promise.reject(new Error(data.msg || '请求失败'))
    }
    return data
  },
  (error) => {
    const msg = error.response?.data?.msg || error.message || '网络错误'
    if (!error.config?.silent) {
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  },
)

export default request
