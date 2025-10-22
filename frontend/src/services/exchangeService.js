import { apiRequest } from './api';

export const exchangeService = {
  async getExchanges(page = 0, size = 20) {
    return await apiRequest(`/exchanges?page=${page}&size=${size}`);
  },

  async createExchange(exchangeData) {
    return await apiRequest('/exchanges', {
      method: 'POST',
      body: JSON.stringify(exchangeData)
    });
  },

  async acceptExchange(id) {
    return await apiRequest(`/exchanges/${id}/accept`, {
      method: 'POST'
    });
  }
};
