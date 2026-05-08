// src/services/api.js
import axios from 'axios';

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

// ── Auth token injection ───────────────────────────────────────
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('dsr_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// ── Global error handling ─────────────────────────────────────
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('dsr_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ── DSR Requests ──────────────────────────────────────────────
export const dsrApi = {
  getAll: (params = {}) =>
    api.get('/dsr', { params }).then((r) => r.data),

  search: (params = {}) =>
    api.get('/dsr/search', { params }).then((r) => r.data),

  getById: (id) =>
    api.get(`/dsr/${id}`).then((r) => r.data),

  create: (data) =>
    api.post('/dsr', data).then((r) => r.data),

  update: (id, data) =>
    api.put(`/dsr/${id}`, data).then((r) => r.data),

  delete: (id) =>
    api.delete(`/dsr/${id}`).then((r) => r.data),

  getStats: () =>
    api.get('/dsr/stats').then((r) => r.data),

  export: (status) => {
    const params = status ? { status } : {};
    return api.get('/dsr/export', { params, responseType: 'blob' });
  },

  uploadFile: (id, file) => {
    const form = new FormData();
    form.append('file', file);
    return api.post(`/dsr/${id}/upload`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then((r) => r.data);
  },
};

// ── Auth ──────────────────────────────────────────────────────
export const authApi = {
  login: (credentials) =>
    api.post('/auth/login', credentials).then((r) => r.data),

  register: (data) =>
    api.post('/auth/register', data).then((r) => r.data),

  refresh: () =>
    api.post('/auth/refresh').then((r) => r.data),
};

export default api;
