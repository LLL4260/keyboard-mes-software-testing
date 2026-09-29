Page({
  data: {
    currentUserName: '当前用户',
    loggingOut: false,
    targetQuantity: 1200,
    reportQuantity: 856,
    qualityRate: 98.5,
    defectQuantity: 18,
    progressRate: 71.3,
    runningTasks: 0,
    pendingReworks: 0,
    workstations: []
  },

  onShow() {
    const user = wx.getStorageSync('MES_USER') || {};
    this.setData({
      currentUserName: user.realName || user.username || user.userNo || '当前用户'
    });
    this.refreshData();
  },

  logout() {
    if (this.data.loggingOut) {
      return;
    }
    wx.showModal({
      title: '退出登录',
      content: '确认退出当前账号吗？',
      confirmText: '退出',
      confirmColor: '#d94f4f',
      success: (result) => {
        if (!result.confirm) {
          return;
        }
        this.performLogout();
      }
    });
  },

  async performLogout() {
    const api = require('../../utils/api');
    const app = getApp();
    this.setData({ loggingOut: true });
    wx.showLoading({ title: '正在退出...' });
    try {
      await api.post('/auth/logout', {});
    } catch (error) {
      // 服务端不可用时仍清理本地登录态，避免账号继续留在设备上。
    } finally {
      wx.removeStorageSync('MES_COOKIE');
      wx.removeStorageSync('MES_USER');
      app.globalData.userInfo = null;
      wx.hideLoading();
      this.setData({ loggingOut: false });
      wx.reLaunch({ url: '/pages/login/login' });
    }
  },

  refreshData() {
    const api = require('../../utils/api');
    wx.showLoading({ title: '同步数据...' });
    api.get('/report/overview')
      .then((data) => {
        this.setData({
          targetQuantity: data.targetQuantity || 0,
          reportQuantity: data.reportQuantity || 0,
          qualityRate: data.qualityRate || 0,
          defectQuantity: data.defectQuantity || 0,
          progressRate: data.progressRate || 0,
          runningTasks: data.runningTasks || 0,
          pendingReworks: data.pendingReworks || 0,
          workstations: this.formatWorkstations(data.workstations || [])
        });
      })
      .catch(() => {
        this.setData(this.demoData());
      })
      .finally(() => {
        wx.hideLoading();
      });
  },

  formatWorkstations(items) {
    if (items.length === 0) {
      return this.demoData().workstations;
    }
    return items.map((item) => ({
      name: item.name,
      reportQuantity: item.reportQuantity || 0,
      defectQuantity: item.defectQuantity || 0,
      status: item.status || '正常'
    }));
  },

  demoData() {
    return {
      targetQuantity: 1200,
      reportQuantity: 856,
      qualityRate: 98.5,
      defectQuantity: 18,
      progressRate: 71.3,
      runningTasks: 4,
      pendingReworks: 2,
      workstations: [
        { name: 'ST-01 总装工位', reportQuantity: 320, defectQuantity: 0, status: '正常' },
        { name: 'ST-02 键帽装配', reportQuantity: 290, defectQuantity: 0, status: '正常' },
        { name: 'ST-03 焊接组装', reportQuantity: 246, defectQuantity: 5, status: '关注' },
        { name: 'ST-04 FQC 终检', reportQuantity: 210, defectQuantity: 1, status: '关注' }
      ]
    };
  }
});
