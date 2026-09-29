import { traceResourceNames } from './workflow.js';

export const ALL_ACCESS = '*';

export const emptyAccess = Object.freeze({
  menu: [],
  read: [],
  workflows: [],
  create: [],
  update: [],
  delete: [],
  generateTasks: [],
  submit: [],
  taskAction: [],
  repair: [],
  recheck: [],
  export: []
});

export const ROLE_ACCESS = Object.freeze({
  admin: {
    menu: ALL_ACCESS,
    read: ALL_ACCESS,
    workflows: ALL_ACCESS,
    create: ALL_ACCESS,
    update: ALL_ACCESS,
    delete: ALL_ACCESS,
    generateTasks: ALL_ACCESS,
    submit: ALL_ACCESS,
    taskAction: ALL_ACCESS,
    repair: ALL_ACCESS,
    recheck: ALL_ACCESS,
    export: ALL_ACCESS
  },
  planner: {
    menu: ['productModel', 'material', 'productBom', 'processRoute', 'productionOrder', 'productionTask'],
    read: ['productModel', 'material', 'productBom', 'processRoute', 'productionOrder', 'productionTask', ...traceResourceNames],
    workflows: ['order', 'task', 'dashboard', 'trace'],
    create: ['productModel', 'material', 'productBom', 'processRoute', 'productionOrder', 'productionTask'],
    update: ['productModel', 'material', 'productBom', 'processRoute', 'productionOrder', 'productionTask'],
    delete: ['productModel', 'material', 'productBom', 'processRoute', 'productionOrder', 'productionTask'],
    generateTasks: ['productionOrder'],
    submit: [],
    taskAction: ['productionTask'],
    repair: [],
    recheck: [],
    export: ['productModel', 'material', 'productBom', 'processRoute', 'productionOrder', 'productionTask']
  },
  operator: {
    menu: ['productionTask', 'workReport'],
    read: ['processRoute', 'productionOrder', 'productionTask', 'workReport', ...traceResourceNames],
    workflows: ['task', 'report', 'dashboard', 'trace'],
    create: ['workReport'],
    update: ['productionTask', 'workReport'],
    delete: [],
    generateTasks: [],
    submit: ['workReport'],
    taskAction: ['productionTask'],
    repair: [],
    recheck: [],
    export: ['productionTask', 'workReport']
  },
  inspector: {
    menu: ['inspectionRecord', 'reworkOrder'],
    read: ['processRoute', 'productionOrder', 'productionTask', 'inspectionRecord', 'reworkOrder', ...traceResourceNames],
    workflows: ['inspection', 'rework', 'dashboard', 'trace'],
    create: ['inspectionRecord', 'reworkOrder'],
    update: ['inspectionRecord', 'reworkOrder'],
    delete: [],
    generateTasks: [],
    submit: ['inspectionRecord'],
    taskAction: [],
    repair: ['reworkOrder'],
    recheck: ['reworkOrder'],
    export: ['inspectionRecord', 'reworkOrder']
  },
  repair: {
    menu: ['reworkOrder', 'inspectionRecord'],
    read: ['productionOrder', 'productionTask', 'inspectionRecord', 'reworkOrder', ...traceResourceNames],
    workflows: ['rework', 'dashboard', 'trace'],
    create: [],
    update: ['reworkOrder'],
    delete: [],
    generateTasks: [],
    submit: [],
    taskAction: [],
    repair: ['reworkOrder'],
    recheck: ['reworkOrder'],
    export: ['reworkOrder', 'inspectionRecord']
  }
});

export const FIELD_WRITE_ACCESS = Object.freeze({
  admin: ALL_ACCESS,
  planner: ALL_ACCESS,
  inspector: ALL_ACCESS,
  operator: {
    productionTask: ['completedQuantity', 'defectQuantity', 'abnormalType', 'abnormalDesc', 'abnormalSolution', 'status', 'startTime', 'finishTime', 'remark'],
    workReport: ALL_ACCESS
  },
  repair: {
    reworkOrder: ['status', 'repairAction', 'replacedMaterial', 'repairHours', 'repairResult', 'repairTime', 'recheckInspectionId']
  }
});
