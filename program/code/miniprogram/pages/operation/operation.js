const api = require('../../utils/api');

Page({
  data: {
    mode: 'report',
    currentUser: null,
    currentUserName: '未登录',
    currentUserRole: '请先登录',
    inspectionResultOptions: [
      { label: '合格', value: 1 },
      { label: '不合格', value: 0 },
      { label: '让步接收', value: 2 },
      { label: '报废', value: 3 }
    ],
    handlingMethodOptions: [
      { label: '不处理', value: '' },
      { label: '返修', value: 1 },
      { label: '报废', value: 2 },
      { label: '让步接收', value: 3 }
    ],
    reworkStatusOptions: [
      { label: '返修中', value: 1 },
      { label: '待复检', value: 2 }
    ],
    inspectionResultIndex: 0,
    handlingMethodIndex: 0,
    reworkStatusIndex: 1,
    inspectionResultLabel: '合格',
    handlingMethodLabel: '不处理',
    reworkStatusLabel: '待复检',
    reportOptions: [],
    reportOrderOptions: [],
    reportTaskOptions: [],
    reportOrderIndex: 0,
    reportTaskIndex: 0,
    reportOrderLabel: '请选择工单编号',
    reportTaskLabel: '请选择任务编号',
    reportTaskHint: '',
    inspectionOptions: [],
    inspectionOrderOptions: [],
    inspectionTaskOptions: [],
    inspectionOrderIndex: 0,
    inspectionTaskIndex: 0,
    inspectionOrderLabel: '请选择工单编号',
    inspectionTaskLabel: '请选择任务编号',
    reworkOptions: [],
    reworkIndex: 0,
    reworkLabel: '请选择返修单编号',
    report: {
      reportNo: '',
      taskId: '',
      orderId: '',
      productSn: '',
      operatorId: '',
      stationCode: '',
      reportQuantity: '',
      qualifiedQuantity: '',
      defectQuantity: '0',
      defectReason: ''
    },
    inspection: {
      inspectionNo: '',
      inspectionType: 'process',
      orderId: '',
      taskId: '',
      productSn: '',
      inspectorId: '',
      result: '1',
      defectReason: '',
      handlingMethod: ''
    },
    rework: {
      id: '',
      assigneeId: '',
      status: '2',
      repairAction: '',
      repairResult: '',
      replacedMaterial: '',
      repairHours: ''
    }
  },

  onShow() {
    this.hydrateCurrentUser();
    this.loadReportOptions();
    this.loadInspectionOptions();
    this.loadReworkOptions();
  },

  loadReportOptions() {
    api.get('/workReport/options')
      .then((records) => {
        const reportOptions = Array.isArray(records) ? records : [];
        const orderMap = {};
        reportOptions.forEach((item) => {
          orderMap[String(item.orderId)] = {
            orderId: item.orderId,
            orderNo: item.orderNo
          };
        });
        this.setData({
          reportOptions,
          reportOrderOptions: Object.keys(orderMap).map((key) => orderMap[key]),
          reportTaskOptions: reportOptions
        });
      })
      .catch(() => {
        this.setData({ reportOptions: [], reportOrderOptions: [], reportTaskOptions: [] });
      });
  },

  loadInspectionOptions() {
    api.get('/inspectionRecord/options')
      .then((records) => {
        const inspectionOptions = Array.isArray(records) ? records : [];
        const orderMap = {};
        inspectionOptions.forEach((item) => {
          orderMap[String(item.orderId)] = { orderId: item.orderId, orderNo: item.orderNo };
        });
        this.setData({
          inspectionOptions,
          inspectionOrderOptions: Object.keys(orderMap).map((key) => orderMap[key]),
          inspectionTaskOptions: inspectionOptions
        });
      })
      .catch(() => {
        this.setData({ inspectionOptions: [], inspectionOrderOptions: [], inspectionTaskOptions: [] });
      });
  },

  loadReworkOptions() {
    api.get('/reworkOrder/list')
      .then((records) => {
        const reworkOptions = (Array.isArray(records) ? records : [])
          .filter((item) => [0, 1, 2].includes(Number(item.status)))
          .sort((left, right) => String(left.reworkNo || '').localeCompare(String(right.reworkNo || '')));
        this.setData({ reworkOptions });
      })
      .catch(() => this.setData({ reworkOptions: [] }));
  },

  hydrateCurrentUser() {
    const app = getApp();
    const currentUser = (app.globalData && app.globalData.userInfo) || wx.getStorageSync('MES_USER') || null;
    if (!currentUser) {
      this.setData({
        currentUser: null,
        currentUserName: '未登录',
        currentUserRole: '请先登录'
      });
      return;
    }
    // 现场端只记录当前登录人的 ID，姓名和角色用于页面提示。
    this.setData({
      currentUser,
      currentUserName: currentUser.name || currentUser.employeeNo || '已登录',
      currentUserRole: currentUser.roleName || currentUser.roleCode || '现场人员',
      'report.operatorId': this.data.report.operatorId || String(currentUser.id || ''),
      'inspection.inspectorId': this.data.inspection.inspectorId || String(currentUser.id || ''),
      'rework.assigneeId': this.data.rework.assigneeId || String(currentUser.id || '')
    });
  },

  switchMode(e) {
    this.setData({ mode: e.currentTarget.dataset.mode });
  },

  updateReport(e) {
    const field = e.currentTarget.dataset.field;
    const value = e.detail.value;
    this.setData({ [`report.${field}`]: value });
    
    if (field === 'reportQuantity' || field === 'defectQuantity') {
      const reportQty = this.toNumber(this.data.report.reportQuantity);
      const defectQty = this.toNumber(this.data.report.defectQuantity);
      const qualifiedQty = Math.max(0, reportQty - defectQty);
      this.setData({ 'report.qualifiedQuantity': String(qualifiedQty) });
    }
  },

  selectReportOrder(e) {
    const index = Number(e.detail.value || 0);
    const selected = this.data.reportOrderOptions[index];
    if (!selected) {
      return;
    }
    const reportTaskOptions = this.data.reportOptions.filter((item) => String(item.orderId) === String(selected.orderId));
    this.setData({
      reportOrderIndex: index,
      reportOrderLabel: selected.orderNo,
      reportTaskOptions,
      reportTaskIndex: 0,
      reportTaskLabel: '请选择任务编号',
      reportTaskHint: '',
      'report.orderId': String(selected.orderId),
      'report.taskId': '',
      'report.stationCode': ''
    });
  },

  selectReportTask(e) {
    const index = Number(e.detail.value || 0);
    const selected = this.data.reportTaskOptions[index];
    if (!selected) {
      return;
    }
    const orderIndex = Math.max(0, this.data.reportOrderOptions.findIndex((item) => String(item.orderId) === String(selected.orderId)));
    this.setData({
      reportTaskIndex: index,
      reportTaskLabel: selected.taskNo,
      reportOrderIndex: orderIndex,
      reportOrderLabel: selected.orderNo,
      reportTaskHint: `工位 ${selected.stationCode || '未分配'} · 剩余 ${Number(selected.remainingQuantity || 0)}`,
      'report.taskId': String(selected.taskId),
      'report.orderId': String(selected.orderId),
      'report.stationCode': selected.stationCode || ''
    });
  },

  selectInspectionOrder(e) {
    const index = Number(e.detail.value || 0);
    const selected = this.data.inspectionOrderOptions[index];
    if (!selected) {
      return;
    }
    const inspectionTaskOptions = this.data.inspectionOptions.filter((item) => String(item.orderId) === String(selected.orderId));
    this.setData({
      inspectionOrderIndex: index,
      inspectionOrderLabel: selected.orderNo,
      inspectionTaskOptions,
      inspectionTaskIndex: 0,
      inspectionTaskLabel: '请选择任务编号',
      'inspection.orderId': String(selected.orderId),
      'inspection.taskId': ''
    });
  },

  selectInspectionTask(e) {
    const index = Number(e.detail.value || 0);
    const selected = this.data.inspectionTaskOptions[index];
    if (!selected) {
      return;
    }
    const orderIndex = Math.max(0, this.data.inspectionOrderOptions.findIndex((item) => String(item.orderId) === String(selected.orderId)));
    this.setData({
      inspectionTaskIndex: index,
      inspectionTaskLabel: selected.taskNo,
      inspectionOrderIndex: orderIndex,
      inspectionOrderLabel: selected.orderNo,
      'inspection.taskId': String(selected.taskId),
      'inspection.orderId': String(selected.orderId)
    });
  },

  selectReworkOrder(e) {
    const index = Number(e.detail.value || 0);
    const selected = this.data.reworkOptions[index];
    if (!selected) {
      return;
    }
    this.setData({
      reworkIndex: index,
      reworkLabel: selected.reworkNo,
      'rework.id': String(selected.id)
    });
  },

  updateInspection(e) {
    this.setData({ [`inspection.${e.currentTarget.dataset.field}`]: e.detail.value });
  },

  updateRework(e) {
    this.setData({ [`rework.${e.currentTarget.dataset.field}`]: e.detail.value });
  },

  selectInspectionResult(e) {
    const index = Number(e.detail.value);
    const option = this.data.inspectionResultOptions[index];
    this.setData({
      inspectionResultIndex: index,
      inspectionResultLabel: option.label,
      'inspection.result': String(option.value)
    });
  },

  selectHandlingMethod(e) {
    const index = Number(e.detail.value);
    const option = this.data.handlingMethodOptions[index];
    this.setData({
      handlingMethodIndex: index,
      handlingMethodLabel: option.label,
      'inspection.handlingMethod': option.value === '' ? '' : String(option.value)
    });
  },

  selectReworkStatus(e) {
    const index = Number(e.detail.value);
    const option = this.data.reworkStatusOptions[index];
    this.setData({
      reworkStatusIndex: index,
      reworkStatusLabel: option.label,
      'rework.status': String(option.value)
    });
  },

  scanProductSn() {
    wx.scanCode({
      onlyFromCamera: false,
      success: (res) => {
        const value = res.result || '';
        if (!value) {
          return;
        }
        if (this.data.mode === 'inspection') {
          this.setData({ 'inspection.productSn': value });
          return;
        }
        this.setData({ 'report.productSn': value });
      },
      fail: () => wx.showToast({ title: '未获取到扫码结果', icon: 'none' })
    });
  },

  submitCurrent() {
    if (this.data.mode === 'report') {
      this.submitReport();
      return;
    }
    if (this.data.mode === 'inspection') {
      this.submitInspection();
      return;
    }
    this.submitRework();
  },

  submitReport() {
    const form = this.data.report;
    const reportQuantity = this.toNumber(form.reportQuantity);
    const qualifiedQuantity = this.toNumber(form.qualifiedQuantity);
    const defectQuantity = this.toNumber(form.defectQuantity);
    const operatorId = this.optionalNumber(form.operatorId) || this.currentUserId();
    if (!form.taskId || !form.orderId || !operatorId) {
      wx.showToast({ title: '请补齐报工必填项', icon: 'none' });
      return;
    }
    if (reportQuantity <= 0 || qualifiedQuantity + defectQuantity > reportQuantity) {
      wx.showToast({ title: '请检查报工数量', icon: 'none' });
      return;
    }
    this.submit('/workReport/submit', {
      reportNo: form.reportNo || this.generateNo('WR'),
      taskId: this.toNumber(form.taskId),
      orderId: this.toNumber(form.orderId),
      productSn: form.productSn,
      operatorId,
      stationCode: form.stationCode,
      reportQuantity,
      qualifiedQuantity,
      defectQuantity,
      defectReason: form.defectReason,
      reportTime: this.now(),
      status: 1
    }, '报工已提交', () => this.resetReport());
  },

  submitInspection() {
    const form = this.data.inspection;
    const result = this.toNumber(form.result);
    const inspectorId = this.optionalNumber(form.inspectorId) || this.currentUserId();
    if (!form.orderId || !inspectorId) {
      wx.showToast({ title: '请补齐质检必填项', icon: 'none' });
      return;
    }
    if (result === 0 && !form.defectReason) {
      wx.showToast({ title: '请填写不合格原因', icon: 'none' });
      return;
    }
    this.submit('/inspectionRecord/submit', {
      inspectionNo: form.inspectionNo || this.generateNo('QC'),
      inspectionType: form.inspectionType,
      taskId: this.optionalNumber(form.taskId),
      orderId: this.toNumber(form.orderId),
      productSn: form.productSn,
      inspectorId,
      sourceType: 'manual',
      inspectionTime: this.now(),
      result,
      defectReason: form.defectReason,
      handlingMethod: this.optionalNumber(form.handlingMethod),
      approvalStatus: 0
    }, '质检已记录', () => this.resetInspection());
  },

  submitRework() {
    const form = this.data.rework;
    if (!form.id) {
      wx.showToast({ title: '请选择返修单编号', icon: 'none' });
      return;
    }
    const payload = {
      id: this.toNumber(form.id),
      assigneeId: this.optionalNumber(form.assigneeId),
      status: this.toNumber(form.status),
      repairAction: form.repairAction,
      repairResult: form.repairResult,
      replacedMaterial: form.replacedMaterial,
      repairHours: form.repairHours ? Number(form.repairHours) : null,
      repairTime: this.now()
    };
    if (!payload.assigneeId) {
      wx.showToast({ title: '请填写维修员', icon: 'none' });
      return;
    }
    if (!payload.repairAction || !payload.repairResult) {
      wx.showToast({ title: '请填写维修措施和结果', icon: 'none' });
      return;
    }
    wx.showLoading({ title: '提交中...' });
    api.post(`/reworkOrder/${payload.id}/repair`, payload)
      .then(() => {
        wx.showToast({ title: '返修已更新', icon: 'success' });
        this.resetRework();
        this.loadReworkOptions();
      })
      .catch((error) => wx.showToast({ title: error.message || '提交失败', icon: 'none' }))
      .finally(() => wx.hideLoading());
  },

  submit(path, payload, message, afterSuccess) {
    wx.showLoading({ title: '提交中...' });
    api.post(path, payload)
      .then(() => {
        wx.showToast({ title: message, icon: 'success' });
        if (afterSuccess) {
          afterSuccess();
        }
      })
      .catch((error) => wx.showToast({ title: error.message || '提交失败', icon: 'none' }))
      .finally(() => wx.hideLoading());
  },

  toNumber(value) {
    return Number(value || 0);
  },

  optionalNumber(value) {
    return value === '' || value === null || value === undefined ? null : Number(value);
  },

  currentUserId() {
    return this.data.currentUser && this.data.currentUser.id ? Number(this.data.currentUser.id) : null;
  },

  generateNo(prefix) {
    const date = new Date();
    const pad = (value) => String(value).padStart(2, '0');
    return `${prefix}-${date.getFullYear()}${pad(date.getMonth() + 1)}${pad(date.getDate())}${pad(date.getHours())}${pad(date.getMinutes())}${pad(date.getSeconds())}`;
  },

  resetReport() {
    this.setData({
      report: {
        reportNo: '',
        taskId: this.data.report.taskId,
        orderId: this.data.report.orderId,
        productSn: '',
        operatorId: String(this.currentUserId() || ''),
        stationCode: this.data.report.stationCode,
        reportQuantity: '',
        qualifiedQuantity: '',
        defectQuantity: '0',
        defectReason: ''
      }
    });
  },

  resetInspection() {
    this.setData({
      inspectionResultIndex: 0,
      handlingMethodIndex: 0,
      inspectionResultLabel: '合格',
      handlingMethodLabel: '不处理',
      inspection: {
        inspectionNo: '',
        inspectionType: 'process',
        orderId: this.data.inspection.orderId,
        taskId: this.data.inspection.taskId,
        productSn: '',
        inspectorId: String(this.currentUserId() || ''),
        result: '1',
        defectReason: '',
        handlingMethod: ''
      }
    });
  },

  resetRework() {
    this.setData({
      reworkStatusIndex: 1,
      reworkStatusLabel: '待复检',
      reworkIndex: 0,
      reworkLabel: '请选择返修单编号',
      rework: {
        id: '',
        assigneeId: String(this.currentUserId() || ''),
        status: '2',
        repairAction: '',
        repairResult: '',
        replacedMaterial: '',
        repairHours: ''
      }
    });
  },

  now() {
    const date = new Date();
    const pad = (value) => String(value).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
  }
});
