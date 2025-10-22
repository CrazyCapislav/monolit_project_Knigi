import { API_BASE_URL } from '../utils/constants';

const getHeaders = (userId) => {
  const headers = {
    'Content-Type': 'application/json'
  };

  if (userId) {
    headers['X-User-Id'] = userId;
  }

  return headers;
};

export const apiRequest = async (endpoint, options = {}) => {
  const userId = localStorage.getItem('userId');
  const url = `${API_BASE_URL}${endpoint}`;

  const config = {
    ...options,
    headers: {
      ...getHeaders(userId),
      ...options.headers
    }
  };

  try {
    const response = await fetch(url, config);

    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `HTTP error! status: ${response.status}`);
    }

    // Для 204 No Content не пытаемся парсить JSON
    if (response.status === 204) {
      return null;
    }

    return await response.json();
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
};

export const getTotalCount = (headers) => {
  return headers.get('X-Total-Count') ? parseInt(headers.get('X-Total-Count')) : 0;
};
