import axios from 'axios'

export const TOKEN_KEY = 'valueswap_token'

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      window.dispatchEvent(new Event('valueswap:unauthorized'))
    }
    return Promise.reject(error)
  },
)

export function apiErrorMessage(error, fallback) {
  return error.response?.data?.message || fallback
}
