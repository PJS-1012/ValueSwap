import { apiClient } from './client.js'

export const postsApi = {
  async list() {
    return (await apiClient.get('/posts')).data
  },
  async detail(id) {
    return (await apiClient.get(`/posts/${id}`)).data
  },
  async create(payload) {
    return (await apiClient.post('/posts', payload)).data
  },
  async mine() {
    return (await apiClient.get('/posts/my')).data
  },
  async cancel(id) {
    return (await apiClient.delete(`/posts/${id}`)).data
  },
}
