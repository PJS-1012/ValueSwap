import { apiClient } from './client.js'

export const notificationsApi = {
  async list() {
    return (await apiClient.get('/notifications')).data
  },
  async markRead(id) {
    return (await apiClient.patch(`/notifications/${id}/read`)).data
  },
}
