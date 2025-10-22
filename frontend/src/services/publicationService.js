import { apiRequest } from './api';

export const publicationService = {
  async getPublications(page = 0, size = 20) {
    return await apiRequest(`/publications?page=${page}&size=${size}`);
  },

  async createPublication(publicationData) {
    return await apiRequest('/publications', {
      method: 'POST',
      body: JSON.stringify(publicationData)
    });
  },

  async approvePublication(id) {
    return await apiRequest(`/publications/${id}/approve`, {
      method: 'POST'
    });
  },

  async rejectPublication(id) {
    return await apiRequest(`/publications/${id}/reject`, {
      method: 'POST'
    });
  }
};
