import { apiRequest } from './api';


export const bookService = {
  async getMyBooks() {
    return await apiRequest('/books/mine');
  },

  async getBooks(page = 0, size = 20) {
    return await apiRequest(`/books?page=${page}&size=${size}`);
  },

  async getBooksFeed(after = null, limit = 20) {
    const params = after ? `?after=${after}&limit=${limit}` : `?limit=${limit}`;
    return await apiRequest(`/books/feed${params}`);
  },

  async createBook(bookData) {
    return await apiRequest('/books', {
      method: 'POST',
      body: JSON.stringify(bookData)
    });
  },

  async getBookById(id) {
    return await apiRequest(`/books/${id}`);
  }
};
