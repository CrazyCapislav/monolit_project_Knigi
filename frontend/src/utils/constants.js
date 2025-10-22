// API Base URL configuration
// In production (Docker), nginx proxies /api/* to backend
// In development, connect directly to backend on localhost:8080
export const API_BASE_URL = process.env.REACT_APP_API_URL || 
  (process.env.NODE_ENV === 'production' 
    ? '/api/v1' 
    : 'http://localhost:8080/api/v1');

export const BOOK_CONDITIONS = {
  NEW: 'Новая',
  GOOD: 'Хорошее',
  FAIR: 'Удовлетворительное',
  BAD: 'Плохое'
};

export const REQUEST_STATUSES = {
  PENDING: 'В ожидании',
  APPROVED: 'Одобрено',
  REJECTED: 'Отклонено',
  AVAILABLE: 'Доступна',
  EXCHANGED: 'Обменена'
};

export const ROUTES = {
  HOME: '/',
  BOOKS: '/books',
  MY_BOOKS: '/my-books',
  EXCHANGES: '/exchanges',
  PUBLICATIONS: '/publications',
  PROFILE: '/profile',
  GENRES: '/genres',
  LOGIN: '/login',
  REGISTER: '/register'
};
