import { API_BASE_URL } from '../utils/constants';


const toSnakeCase = (obj) => {
  if (obj === null || typeof obj !== 'object' || obj instanceof Date) {
    return obj;
  }

  if (Array.isArray(obj)) {
    return obj.map(toSnakeCase);
  }

  return Object.keys(obj).reduce((result, key) => {
    const snakeKey = key.replace(/[A-Z]/g, letter => `_${letter.toLowerCase()}`);
    result[snakeKey] = toSnakeCase(obj[key]);
    return result;
  }, {});
};

const toCamelCase = (obj) => {
  if (obj === null || typeof obj !== 'object' || obj instanceof Date) {
    return obj;
  }

  if (Array.isArray(obj)) {
    return obj.map(toCamelCase);
  }

  return Object.keys(obj).reduce((result, key) => {
    const camelKey = key.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
    result[camelKey] = toCamelCase(obj[key]);
    return result;
  }, {});
};

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

  let bodyToSend = options.body;
  if (options.body && typeof options.body === 'string') {
    try {
      const parsed = JSON.parse(options.body);
      bodyToSend = JSON.stringify(toSnakeCase(parsed));
    } catch (e) {
    }
  }

  const config = {
    ...options,
    body: bodyToSend,
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

    if (response.status === 204) {
      return null;
    }

    const data = await response.json();
    return toCamelCase(data);
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
};

export const getTotalCount = (headers) => {
  return headers.get('X-Total-Count') ? parseInt(headers.get('X-Total-Count')) : 0;
};
