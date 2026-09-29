App({
  onLaunch() {
    const storedUser = wx.getStorageSync('MES_USER');
    if (storedUser) {
      this.globalData.userInfo = storedUser;
    }
  },
  globalData: {
    apiBase: 'http://localhost:8088/api',
    userInfo: null
  }
});
