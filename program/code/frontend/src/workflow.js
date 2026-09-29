export const workflowEntries = [
  { key: 'order', title: '生产工单', role: '计划员 / 主管', resource: 'productionOrder', text: '创建工单、维护齐套与工单状态' },
  { key: 'task', title: '生产任务', role: '主管 / 操作员', resource: 'productionTask', text: '查看任务、维护开工与完工状态' },
  { key: 'report', title: '工位报工', role: '操作员', resource: 'workReport', text: '按任务录入报工数量和不良原因' },
  { key: 'inspection', title: '质量检验', role: '质检员', resource: 'inspectionRecord', text: '记录过程检、终检、复检和判定' },
  { key: 'rework', title: '返修处理', role: '维修员', resource: 'reworkOrder', text: '查看返修单并登记维修结果' },
  { key: 'dashboard', title: '可视化报表', role: '主管 / 计划 / 质检', type: 'dashboard', text: '集中查看产量、质量、工位与趋势图表' },
  { key: 'trace', title: '产品追溯', role: '主管 / 质检 / 售后', type: 'trace', text: '按产品 SN 串联报工、质检、返修记录' }
];

export const traceResourceNames = ['workReport', 'inspectionRecord', 'reworkOrder', 'productionTask', 'productionOrder'];
