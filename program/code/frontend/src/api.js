export const DEFAULT_API_BASE = 'http://localhost:8088/api';

export class ApiClient {
  constructor(base = DEFAULT_API_BASE) {
    this.base = base;
    this.sessionId = sessionStorage.getItem('MES_SESSION_ID');
  }

  setBase(base) {
    this.base = (base || DEFAULT_API_BASE).replace(/\/$/, '');
  }

  setSessionId(sessionId) {
    this.sessionId = sessionId;
    if (sessionId) {
      sessionStorage.setItem('MES_SESSION_ID', sessionId);
    } else {
      sessionStorage.removeItem('MES_SESSION_ID');
    }
  }

  url(path) {
    return `${this.base.replace(/\/$/, '')}${path}`;
  }

  async request(path, options = {}) {
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (this.sessionId) {
      headers['Authorization'] = `Bearer ${this.sessionId}`;
    }
    let response = null;
    try {
      response = await fetch(this.url(path), {
        headers,
        ...options
      });
    } catch (error) {
      const networkError = new Error(`无法连接后端服务：${this.base}`);
      networkError.code = 'NETWORK_ERROR';
      throw networkError;
    }

    let result = null;
    try {
      result = await response.json();
    } catch (error) {
      throw new Error(`系统响应异常：HTTP ${response.status}`);
    }

    if (response.status === 401 || result.code === 401) {
      const error = new Error(result.msg || '请先登录');
      error.code = 401;
      throw error;
    }

    if (response.status === 403 || result.code === 403) {
      const error = new Error(result.msg || '当前角色不能执行该操作');
      error.code = 403;
      throw error;
    }

    if (result.code !== 200) {
      const error = new Error(result.msg || `业务请求失败：HTTP ${response.status}`);
      error.code = result.code || response.status;
      throw error;
    }

    return result.data;
  }

  login(payload) {
    return this.request('/auth/login', { method: 'POST', body: JSON.stringify(payload) });
  }

  currentUser() {
    return this.request('/auth/current');
  }

  logout() {
    return this.request('/auth/logout', { method: 'POST' });
  }

  health() {
    return this.request('/health');
  }

  trace(productSn) {
    return this.request(`/trace/${encodeURIComponent(productSn)}`);
  }

  orderTracking(orderId) {
    return this.request(`/report/order/${encodeURIComponent(orderId)}`);
  }

  reportOverview() {
    return this.request('/report/overview');
  }

  reportQuality(filters = {}) {
    const params = new URLSearchParams();
    params.set('period', filters.period || 'week');
    if (filters.productModelId) {
      params.set('productModelId', filters.productModelId);
    }
    return this.request(`/report/quality?${params.toString()}`);
  }

  list(resource) {
    return this.request(`${resource.path.replace('/api', '')}/list`);
  }

  create(resource, payload) {
    return this.request(`${resource.path.replace('/api', '')}/add`, { method: 'POST', body: JSON.stringify(payload) });
  }

  update(resource, id, payload) {
    return this.request(`${resource.path.replace('/api', '')}/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
  }

  delete(resource, id) {
    return this.request(`${resource.path.replace('/api', '')}/${id}`, { method: 'DELETE' });
  }

  taskAction(id, action) {
    return this.request(`/productionTask/${id}/${action}`, { method: 'POST' });
  }

  generateTasks(orderId) {
    return this.request(`/productionOrder/${orderId}/generateTasks`, { method: 'POST' });
  }

  submitWorkReport(payload) {
    return this.request('/workReport/submit', { method: 'POST', body: JSON.stringify(payload) });
  }

  submitInspection(payload) {
    return this.request('/inspectionRecord/submit', { method: 'POST', body: JSON.stringify(payload) });
  }

  repairRework(id, payload) {
    return this.request(`/reworkOrder/${id}/repair`, { method: 'POST', body: JSON.stringify(payload) });
  }

  recheckRework(id, payload) {
    return this.request(`/reworkOrder/${id}/recheck`, { method: 'POST', body: JSON.stringify(payload) });
  }
}
