import { apiClient } from './client.js'

export const tradeRoomsApi = {
  async list() { return (await apiClient.get('/trade-rooms')).data },
  async detail(id) { return (await apiClient.get(`/trade-rooms/${id}`)).data },
  async messages(id, params = {}) { return (await apiClient.get(`/trade-rooms/${id}/messages`, { params })).data },
  async markRead(id) { return (await apiClient.patch(`/trade-rooms/${id}/read`)).data },
  async complete(id) { return (await apiClient.post(`/trade-rooms/${id}/complete`)).data },
}
