import { apiRequest } from './api';

export const genreService = {
  async getGenres() {
    return await apiRequest('/genres');
  },

  async createGenre(genreData) {
    return await apiRequest('/genres', {
      method: 'POST',
      body: JSON.stringify(genreData)
    });
  },

  async deleteGenre(id) {
    return await apiRequest(`/genres/${id}`, {
      method: 'DELETE'
    });
  }
};
