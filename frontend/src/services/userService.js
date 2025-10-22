import { apiRequest } from './api';

export const userService = {
  async register(userData) {
    return await apiRequest('/users', {
      method: 'POST',
      body: JSON.stringify(userData)
    });
  },

  async getCurrentUser() {
    return await apiRequest('/users/me');
  }
};
