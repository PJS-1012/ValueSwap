import { apiClient } from './client.js'

export const authApi = {
  async login(payload) {
    return (await apiClient.post('/auth/login', payload)).data
  },
  async signup(payload) {
    return (await apiClient.post('/auth/signup', payload)).data
  },
  async me() {
    return (await apiClient.get('/auth/me')).data
  },
}
