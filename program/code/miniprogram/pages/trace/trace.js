Page({
  data: {
    snCode: '',
    traceMode: 'timeline',
    traceList: [],
    traceGroups: [],
    showModal: false,
    modalData: {}
  },
  onSnInput(e) {
    this.setData({ snCode: e.detail.value });
  },
  switchMode(e) {
    this.setData({ traceMode: e.currentTarget.dataset.mode });
  },
  scanCode() {
    wx.scanCode({
      onlyFromCamera: false,
      success: (res) => {
        this.setData({ snCode: res.result });
        this.searchTrace();
      }
    });
  },
  searchTrace() {
    const snCode = String(this.data.snCode || '').trim();
    if (!snCode) {
      return wx.showToast({ title: '请输入或扫描SN', icon: 'none' });
    }
    this.setData({ snCode });
    const api = require('../../utils/api');
    wx.showLoading({ title: '查询中...' });
    api.get(`/trace/${encodeURIComponent(snCode)}`)
      .then((data) => {
        const { traceList, traceGroups } = this.formatTraceData(data);
        this.setData({ 
          traceList: traceList.length ? traceList : this.demoTraceList(),
          traceGroups: traceGroups.length ? traceGroups : this.demoTraceGroups()
        });
        wx.showToast({ title: '查询成功', icon: 'success' });
      })
      .catch(() => {
        this.setData({ 
          traceList: this.demoTraceList(),
          traceGroups: this.demoTraceGroups()
        });
        wx.showToast({ title: '演示数据', icon: 'none' });
      })
      .finally(() => {
        wx.hideLoading();
      });
  },

  formatTraceData(data) {
    const items = [];
    const groups = {
      orders: { title: '生产工单', items: [] },
      tasks: { title: '生产任务', items: [] },
      reports: { title: '报工记录', items: [] },
      inspections: { title: '检验记录', items: [] },
      reworks: { title: '返修工单', items: [] }
    };

    (data.orders || []).forEach((item) => {
      const record = {
        key: `order-${item.id}`,
        id: item.id,
        orderNo: item.orderNo,
        time: this.displayTime(item.updateTime || item.createTime),
        nodeName: `生产工单 ${item.orderNo || ''}`,
        operator: `计划员：${item.plannerId || '-'}`,
        result: `状态：${item.status || '-'}`,
        status: item.status
      };
      items.push(record);
      groups.orders.items.push(record);
    });

    (data.tasks || []).forEach((item) => {
      const record = {
        key: `task-${item.id}`,
        id: item.id,
        taskNo: item.taskNo,
        time: this.displayTime(item.finishTime || item.startTime),
        nodeName: `生产任务 ${item.taskNo || ''}`,
        operator: `工位：${item.stationCode || '-'}`,
        result: `状态：${item.status || '-'}`,
        stationCode: item.stationCode,
        status: item.status
      };
      items.push(record);
      groups.tasks.items.push(record);
    });

    (data.reports || []).forEach((item) => {
      const record = {
        key: `report-${item.id}`,
        id: item.id,
        reportNo: item.reportNo,
        time: this.displayTime(item.reportTime),
        nodeName: `报工 ${item.reportNo || ''}`,
        operator: `操作员：${item.operatorId || '-'}`,
        result: `合格 ${item.qualifiedQuantity || 0}，不良 ${item.defectQuantity || 0}`,
        qualifiedQuantity: item.qualifiedQuantity,
        defectQuantity: item.defectQuantity,
        defectReason: item.defectReason
      };
      items.push(record);
      groups.reports.items.push(record);
    });

    (data.inspections || []).forEach((item) => {
      const record = {
        key: `inspection-${item.id}`,
        id: item.id,
        inspectionNo: item.inspectionNo,
        time: this.displayTime(item.inspectionTime),
        nodeName: `检验 ${item.inspectionNo || ''}`,
        operator: `检验员：${item.inspectorId || '-'}`,
        result: `结果：${item.result || '-'}`,
        defectReason: item.defectReason,
        handlingMethod: item.handlingMethod
      };
      items.push(record);
      groups.inspections.items.push(record);
    });

    (data.reworks || []).forEach((item) => {
      const record = {
        key: `rework-${item.id}`,
        id: item.id,
        reworkNo: item.reworkNo,
        time: this.displayTime(item.repairTime || item.createTime),
        nodeName: `返修 ${item.reworkNo || ''}`,
        operator: `维修员：${item.assigneeId || '-'}`,
        result: item.repairResult || `状态：${item.status || '-'}`,
        repairAction: item.repairAction,
        repairResult: item.repairResult,
        replacedMaterial: item.replacedMaterial,
        repairHours: item.repairHours,
        status: item.status
      };
      items.push(record);
      groups.reworks.items.push(record);
    });

    const sortedList = items
      .filter((item) => item.time)
      .sort((a, b) => String(b.time).localeCompare(String(a.time)));
    
    sortedList.forEach((item, index) => {
      item.isActive = index === 0;
    });

    const traceGroups = Object.values(groups).filter((g) => g.items.length > 0);

    return { traceList: sortedList, traceGroups };
  },

  showDetail(e) {
    this.setData({
      showModal: true,
      modalData: e.currentTarget.dataset.item || {}
    });
  },

  closeModal() {
    this.setData({ showModal: false, modalData: {} });
  },

  stopPropagation() {},

  displayTime(value) {
    return value ? String(value).replace('T', ' ').slice(0, 19) : '';
  },

  demoTraceList() {
    return [
      { 
        key: '1', isActive: true,
        time: '2026-07-14 09:30:15', 
        nodeName: 'FQC 终检', 
        operator: '质检员-王五', 
        result: '合格通过',
        inspectionNo: 'INS-20260714-001'
      },
      { 
        key: '2', isActive: false,
        time: '2026-07-13 16:45:20', 
        nodeName: '车间组装', 
        operator: '组装工-李四', 
        result: '组装完成',
        taskNo: 'TASK-20260713-005',
        stationCode: 'S02'
      },
      { 
        key: '3', isActive: false,
        time: '2026-07-13 14:10:05', 
        nodeName: '仓库备料', 
        operator: '仓管员-张三', 
        result: '已发料',
        taskNo: 'TASK-20260713-001',
        stationCode: 'S01'
      },
      { 
        key: '4', isActive: false,
        time: '2026-07-13 10:00:00', 
        nodeName: '生产工单下发', 
        operator: '计划员-赵六', 
        result: '排产成功',
        orderNo: 'PO-20260713-001',
        status: '生产中'
      }
    ];
  },

  demoTraceGroups() {
    return [
      {
        title: '生产工单',
        items: [
          { 
            key: 'g1',
            id: 'PO-20260713-001',
            nodeName: 'PO-20260713-001',
            operator: '计划员：赵六',
            result: '状态：生产中',
            orderNo: 'PO-20260713-001',
            time: '2026-07-13 10:00:00',
            status: '生产中'
          }
        ]
      },
      {
        title: '生产任务',
        items: [
          { 
            key: 'g2',
            id: 'TASK-20260713-005',
            nodeName: 'TASK-20260713-005',
            operator: '工位：S02',
            result: '状态：已完成',
            taskNo: 'TASK-20260713-005',
            time: '2026-07-13 16:45:20',
            stationCode: 'S02',
            status: '已完成'
          },
          { 
            key: 'g3',
            id: 'TASK-20260713-001',
            nodeName: 'TASK-20260713-001',
            operator: '工位：S01',
            result: '状态：已完成',
            taskNo: 'TASK-20260713-001',
            time: '2026-07-13 14:10:05',
            stationCode: 'S01',
            status: '已完成'
          }
        ]
      },
      {
        title: '检验记录',
        items: [
          { 
            key: 'g4',
            id: 'INS-20260714-001',
            nodeName: 'INS-20260714-001',
            operator: '检验员：王五',
            result: '结果：合格',
            inspectionNo: 'INS-20260714-001',
            time: '2026-07-14 09:30:15',
            defectReason: '-',
            handlingMethod: '-'
          }
        ]
      }
    ];
  }
});
