import { apiClient } from './client.js'

export const matchesApi = {
  async mine() {
    return (await apiClient.get('/matches/my')).data
  },
  async detail(id) {
    const response = (await apiClient.get(`/matches/${id}`)).data
    return response.match
  },
  async accept(id) { return (await apiClient.post(`/matches/${id}/accept`)).data.match },
  async reject(id) { return (await apiClient.post(`/matches/${id}/reject`)).data.match },
}
