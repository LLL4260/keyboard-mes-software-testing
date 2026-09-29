const DEFAULT_API_BASE = 'http://localhost:8088/api';

function apiBase() {
  const app = getApp();
  return (app.globalData && app.globalData.apiBase) || DEFAULT_API_BASE;
}

function request(path, options = {}) {
  const cookie = wx.getStorageSync('MES_COOKIE') || '';
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${apiBase()}${path}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(cookie ? { Cookie: cookie } : {})
      },
      success(res) {
        const setCookie = res.header['Set-Cookie'] || res.header['set-cookie'];
        if (setCookie) {
          wx.setStorageSync('MES_COOKIE', setCookie);
        }

        const body = res.data || {};
        if (res.statusCode === 401 || body.code === 401) {
          wx.showToast({ title: body.msg || '请先登录', icon: 'none', duration: 2000 });
          reject(new Error(body.msg || '请先登录'));
          return;
        }
        if (res.statusCode === 403 || body.code === 403) {
          wx.showToast({ title: body.msg || '无权限访问', icon: 'none', duration: 2000 });
          reject(new Error(body.msg || '无权限访问'));
          return;
        }
        if (body.code !== 200) {
          wx.showToast({ title: body.msg || '请求失败', icon: 'none', duration: 2000 });
          reject(new Error(body.msg || `请求失败：HTTP ${res.statusCode}`));
          return;
        }
        resolve(body.data);
      },
      fail(error) {
        wx.showToast({ title: '网络连接失败', icon: 'none', duration: 2000 });
        reject(error);
      }
    });
  });
}

function get(path) {
  return request(path);
}

function post(path, data) {
  return request(path, { method: 'POST', data });
}

function put(path, data) {
  return request(path, { method: 'PUT', data });
}

module.exports = {
  DEFAULT_API_BASE,
  get,
  post,
  put,
  request
};
