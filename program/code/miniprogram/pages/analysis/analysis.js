const api = require('../../utils/api');

Page({
  data: {
    currentTab: 'week',
    periodLabel: '本周',
    dateRange: '',
    productModels: [{ id: '', label: '全部产品' }],
    productModelIndex: 0,
    productModelId: '',
    productModelName: '全部产品',
    totalQuantity: 0,
    qualifiedQuantity: 0,
    defectQuantity: 0,
    qualityRate: 0,
    defectTypes: [],
    trendData: [],
    productBreakdown: []
  },

  onShow() {
    this.loadQuality();
  },

  loadQuality() {
    const requestToken = Date.now();
    this.requestToken = requestToken;
    const { currentTab, productModelId } = this.data;
    const modelQuery = productModelId ? `&productModelId=${encodeURIComponent(productModelId)}` : '';
    wx.showLoading({ title: '加载报表...' });
    api.get(`/report/quality?period=${currentTab}${modelQuery}`)
      .then((data) => {
        if (this.requestToken === requestToken) {
          this.setData(this.normalizeQuality(data || {}));
        }
      })
      .catch(() => {
        if (this.requestToken === requestToken) {
          this.setData(this.demoData(currentTab, productModelId));
        }
      })
      .finally(() => {
        if (this.requestToken === requestToken) {
          wx.hideLoading();
        }
      });
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    if (tab === this.data.currentTab) {
      return;
    }
    this.setData({ currentTab: tab }, () => this.loadQuality());
  },

  selectProductModel(e) {
    const index = Number(e.detail.value || 0);
    const selected = this.data.productModels[index] || this.data.productModels[0];
    this.setData({
      productModelIndex: index,
      productModelId: selected.id || '',
      productModelName: selected.label || '全部产品'
    }, () => this.loadQuality());
  },

  normalizeQuality(data) {
    const colors = ['bg-red', 'bg-orange', 'bg-blue', 'bg-green'];
    const apiModels = (data.productModels || []).map((item) => ({
      id: String(item.id),
      label: item.label || `${item.modelCode || ''} ${item.modelName || ''}`.trim()
    }));
    const productModels = [{ id: '', label: '全部产品' }, ...apiModels];
    const selectedId = data.productModelId ? String(data.productModelId) : '';
    const productModelIndex = Math.max(0, productModels.findIndex((item) => item.id === selectedId));
    const defectTypes = (data.defectTypes || []).map((item, index) => ({
      name: item.name,
      count: Number(item.count || 0),
      percent: Number(item.percent || 0),
      color: colors[index % colors.length]
    }));
    return {
      periodLabel: data.periodLabel || ({ day: '本日', month: '本月' }[this.data.currentTab] || '本周'),
      dateRange: this.dateRange(data.periodStart, data.periodEnd),
      productModels,
      productModelIndex,
      productModelId: selectedId,
      productModelName: data.productModelName || productModels[productModelIndex].label,
      totalQuantity: Number(data.totalQuantity || 0),
      qualifiedQuantity: Number(data.qualifiedQuantity || 0),
      defectQuantity: Number(data.defectQuantity || 0),
      qualityRate: Number(data.qualityRate || 0),
      defectTypes,
      trendData: this.normalizeTrend(data.trendData || []),
      productBreakdown: (data.productBreakdown || []).map((item) => ({
        id: item.productModelId,
        label: `${item.modelCode || ''} ${item.modelName || ''}`.trim(),
        category: item.category || '未分类',
        quantity: Number(item.quantity || 0),
        percent: Number(item.percent || 0)
      }))
    };
  },

  normalizeTrend(items) {
    const max = Math.max(...items.map((item) => Number(item.quantity || 0)), 1);
    return items.map((item) => {
      const qty = Number(item.quantity || 0);
      return {
        day: item.label || String(item.date || '').slice(5),
        qty,
        height: qty === 0 ? 6 : Math.max(18, Math.round((qty / max) * 140))
      };
    });
  },

  dateRange(start, end) {
    if (!start || !end) {
      return '';
    }
    return `${String(start).slice(5)} 至 ${String(end).slice(5)}`;
  },

  demoData(period, productModelId) {
    const models = [
      { id: '', label: '全部产品' },
      { id: '1', label: 'KB-ALPHA-87 · Alpha 87' },
      { id: '2', label: 'KB-PRO-104 · Pro 104' },
      { id: '3', label: 'KB-MINI-68 · Mini 68' }
    ];
    const selectedIndex = Math.max(0, models.findIndex((item) => item.id === String(productModelId || '')));
    const ratios = { '1': 0.42, '2': 0.34, '3': 0.24 };
    const ratio = productModelId ? (ratios[String(productModelId)] || 0.3) : 1;
    const source = period === 'day'
      ? [{ day: '07-17', qty: 1320 }]
      : period === 'month'
      ? [
          { day: '第1周', qty: 6800 },
          { day: '第2周', qty: 7200 },
          { day: '第3周', qty: 7500 },
          { day: '第4周', qty: 8100 },
          { day: '第5周', qty: 3200 }
        ]
      : [
          { day: '07-13', qty: 1090 },
          { day: '07-14', qty: 1250 },
          { day: '07-15', qty: 1180 },
          { day: '07-16', qty: 1320 },
          { day: '07-17', qty: 0 },
          { day: '07-18', qty: 0 },
          { day: '07-19', qty: 0 }
        ];
    const trendData = this.normalizeTrend(source.map((item) => ({
      label: item.day,
      quantity: Math.round(item.qty * ratio)
    })));
    const totalQuantity = trendData.reduce((sum, item) => sum + item.qty, 0);
    const defectQuantity = Math.round(totalQuantity * 0.025);
    const qualifiedQuantity = totalQuantity - defectQuantity;
    return {
      periodLabel: ({ day: '本日', month: '本月' }[period] || '本周'),
      dateRange: period === 'day' ? '07-17' : (period === 'month' ? '07-01 至 07-31' : '07-13 至 07-19'),
      productModels: models,
      productModelIndex: selectedIndex,
      productModelId: productModelId || '',
      productModelName: models[selectedIndex].label,
      totalQuantity,
      qualifiedQuantity,
      defectQuantity,
      qualityRate: totalQuantity ? Number((qualifiedQuantity * 100 / totalQuantity).toFixed(1)) : 0,
      defectTypes: [
        { name: 'PCB轴座虚焊/漏焊', count: 12, percent: 40, color: 'bg-red' },
        { name: '卫星轴杂音/卡顿', count: 9, percent: 30, color: 'bg-orange' },
        { name: '键帽外壳划伤', count: 6, percent: 20, color: 'bg-blue' },
        { name: 'RGB灯珠不亮', count: 3, percent: 10, color: 'bg-green' }
      ],
      trendData,
      productBreakdown: productModelId ? [] : [
        { id: 1, label: 'KB-ALPHA-87 Alpha 87', category: '87键', quantity: Math.round(totalQuantity * 0.42), percent: 42 },
        { id: 2, label: 'KB-PRO-104 Pro 104', category: '104键', quantity: Math.round(totalQuantity * 0.34), percent: 34 },
        { id: 3, label: 'KB-MINI-68 Mini 68', category: '68键', quantity: Math.round(totalQuantity * 0.24), percent: 24 }
      ]
    };
  }
});
