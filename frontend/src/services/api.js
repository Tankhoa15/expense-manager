import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const authService = {
  login: (email, password) => api.post('/auth/login', { email, password }),
  register: (data) => api.post('/auth/register', data),
  getMe: () => api.get('/auth/me'),
}

export const dashboardService = {
  get: () => api.get('/dashboard'),
}

export const monitorService = {
  getOverview: () => api.get('/monitor/overview'),
  getRecentActivity: () => api.get('/monitor/recent'),
  getStatistics: () => api.get('/monitor/statistics'),
  getTrend: (days) => api.get('/monitor/trend', { params: { days } }),
}

export const moneySourceService = {
  getAll: () => api.get('/money-sources'),
  getById: (id) => api.get(`/money-sources/${id}`),
  create: (data) => api.post('/money-sources', data),
  update: (id, data) => api.put(`/money-sources/${id}`, data),
  delete: (id) => api.delete(`/money-sources/${id}`),
}

export const monthlyBalanceService = {
  getAll: () => api.get('/monthly-balances'),
  getCurrent: () => api.get('/monthly-balances/current'),
  createOrUpdate: (data) => api.post('/monthly-balances', data),
}

export const categoryService = {
  getAll: () => api.get('/categories'),
  getByType: (type) => api.get(`/categories?type=${type}`),
  getById: (id) => api.get(`/categories/${id}`),
  create: (data) => api.post('/categories', data),
  update: (id, data) => api.put(`/categories/${id}`, data),
  delete: (id) => api.delete(`/categories/${id}`),
}

export const transactionService = {
  getAll: (page = 0, size = 20) => api.get('/transactions', { params: { page, size } }),
  getPending: (page = 0, size = 20) => api.get('/transactions/pending', { params: { page, size } }),
  getByDateRange: (startDate, endDate) => api.get('/transactions/date-range', { params: { startDate, endDate } }),
  getById: (id) => api.get(`/transactions/${id}`),
  create: (data) => api.post('/transactions', data),
  confirm: (id) => api.post(`/transactions/${id}/confirm`),
  cancel: (id) => api.post(`/transactions/${id}/cancel`),
  delete: (id) => api.delete(`/transactions/${id}`),
}

export default api
