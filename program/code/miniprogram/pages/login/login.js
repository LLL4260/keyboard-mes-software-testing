Page({
  data: {
    username: 'U001',
    password: '123456',
    loading: false
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value });
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value });
  },

  handleLogin() {
    const { username, password } = this.data;
    if (!username || !password) {
      return wx.showToast({ title: '请输入账号和密码', icon: 'error' });
    }

    const api = require('../../utils/api');
    this.setData({ loading: true });
    wx.showLoading({ title: '登录中...' });
    api.post('/auth/login', { employeeNo: username, password })
      .then((data) => {
        const app = getApp();
        app.globalData.userInfo = data.user;
        wx.setStorageSync('MES_USER', data.user);
        wx.showToast({ title: '登录成功', icon: 'success' });
        setTimeout(() => {
          wx.switchTab({ url: '/pages/dashboard/dashboard' });
        }, 500);
      })
      .catch((error) => {
        wx.showToast({ title: error.message || '登录失败', icon: 'none' });
      })
      .finally(() => {
        this.setData({ loading: false });
        wx.hideLoading();
      });
  }
});
