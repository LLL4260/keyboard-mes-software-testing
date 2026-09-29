export const moduleGroups = [
  { title: '基础资料', names: ['sysUser', 'productModel', 'material', 'productBom', 'processRoute'] },
  { title: '生产执行', names: ['productionOrder', 'productionTask', 'workReport'] },
  { title: '质量返修', names: ['inspectionRecord', 'reworkOrder'] }
];

export const dictionaries = {
  enabledStatus: { 0: '停用', 1: '启用' },
  roleCode: { admin: '管理员', planner: '计划员', operator: '操作员', inspector: '质检员', repair: '返修员' },
  roleName: { 系统管理员: '系统管理员', 计划员: '计划员', 操作员: '操作员', 质检员: '质检员', 返修员: '返修员' },
  skillLevel: { L1: 'L1 初级', L2: 'L2 熟练', L3: 'L3 资深' },
  materialType: { 1: '零部件', 2: '辅料', 3: '半成品', 4: '成品' },
  unit: { pcs: 'pcs', set: 'set', box: 'box', kg: 'kg', m: 'm' },
  processType: { 1: '组装', 2: '检测', 3: '包装', 4: '返修' },
  yesNo: { 0: '否', 1: '是' },
  inspectionType: { process: '过程检', final: '终检', recheck: '复检', AOI: 'AOI', ICT: 'ICT', FCT: 'FCT' },
  dataSource: { manual: '人工录入', equipment: '设备采集' },
  orderSource: { ERP: 'ERP', manual: '手工创建' },
  lineName: { 一号装配线: '一号装配线', 二号装配线: '二号装配线', 三号装配线: '三号装配线', 四号矮轴线: '四号矮轴线', 试产线: '试产线' },
  priority: { 0: '普通', 1: '加急' },
  materialReady: { 0: '未齐套', 1: '齐套' },
  orderStatus: { 0: '待配置', 1: '已接收', 2: '生产中', 3: '待终检', 4: '完成', 5: '关闭' },
  taskStatus: { 0: '待开工', 1: '生产中', 2: '暂停', 3: '已完工', 4: '待质检', 5: '异常' },
  abnormalType: { 1: '缺料', 2: '设备', 3: '质量', 4: '其他' },
  reportStatus: { 0: '撤销', 1: '有效', 2: '待审核' },
  inspectionResult: { 0: '不合格', 1: '合格', 2: '让步接收', 3: '报废' },
  handlingMethod: { 1: '返修', 2: '报废', 3: '让步接收' },
  approvalStatus: { 0: '无需/待审', 1: '通过', 2: '驳回' },
  reworkStatus: { 0: '待分派', 1: '返修中', 2: '待复检', 3: '完成', 4: '关闭' }
};

const field = (name, label, type = 'String', options = {}) => ({ name, label, type, ...options });
const id = () => field('id', 'ID', 'Long', { readonly: true });
const createTime = () => field('createTime', '创建时间', 'LocalDateTime', { readonly: true });
const updateTime = () => field('updateTime', '更新时间', 'LocalDateTime', { readonly: true });

export const resources = [
  {
    name: 'sysUser', title: '人员岗位', icon: '人', table: 'sys_user', path: '/api/sysUser', description: '维护工号、岗位、班组和在岗状态。',
    visible: ['id', 'employeeNo', 'name', 'roleName', 'department', 'status'],
    fields: [id(), field('employeeNo', '工号', 'String', { required: true }), field('passwordHash', '密码', 'String', { required: true, sensitive: true }), field('name', '姓名', 'String', { required: true }), field('roleCode', '岗位类型', 'String', { required: true, dict: 'roleCode' }), field('roleName', '岗位名称', 'String', { required: true, dict: 'roleName' }), field('department', '部门/班组'), field('position', '岗位'), field('phone', '手机号'), field('skillLevel', '技能等级', 'String', { dict: 'skillLevel' }), field('status', '状态', 'Integer', { required: true, dict: 'enabledStatus' }), createTime(), updateTime()]
  },
  {
    name: 'productModel', title: '产品型号', icon: '型', table: 'product_model', path: '/api/productModel', description: '维护键盘产品型号、配置摘要和固件版本。',
    visible: ['id', 'modelCode', 'modelName', 'category', 'firmwareVersion', 'status'],
    fields: [id(), field('modelCode', '型号编码', 'String', { required: true }), field('modelName', '型号名称', 'String', { required: true }), field('category', '产品类别'), field('configurationDesc', '配置摘要', 'Text'), field('firmwareVersion', '固件版本'), field('status', '状态', 'Integer', { required: true, dict: 'enabledStatus' }), createTime(), updateTime(), field('remark', '备注')]
  },
  {
    name: 'material', title: '物料管理', icon: '料', table: 'material', path: '/api/material', description: '维护零部件、辅料、半成品和成品物料。',
    visible: ['id', 'materialCode', 'materialName', 'materialType', 'unit', 'status'],
    fields: [id(), field('materialCode', '物料编码', 'String', { required: true }), field('materialName', '物料名称', 'String', { required: true }), field('specification', '规格型号'), field('materialType', '物料类型', 'Integer', { required: true, dict: 'materialType' }), field('unit', '计量单位', 'String', { dict: 'unit' }), field('supplier', '供应商/来源'), field('barcode', '物料条码'), field('status', '状态', 'Integer', { required: true, dict: 'enabledStatus' }), createTime(), updateTime()]
  },
  {
    name: 'productBom', title: '产品 BOM', icon: 'B', table: 'product_bom', path: '/api/productBom', description: '维护产品型号与物料用量关系。',
    visible: ['id', 'productModelId', 'bomVersion', 'materialId', 'quantity', 'status'],
    fields: [id(), field('productModelId', '产品型号', 'Long', { required: true, relation: 'productModel' }), field('bomVersion', 'BOM 版本', 'String', { required: true }), field('materialId', '物料', 'Long', { required: true, relation: 'material' }), field('quantity', '标准用量', 'BigDecimal', { required: true }), field('unit', '计量单位', 'String', { dict: 'unit' }), field('sequenceNo', '行序号', 'Integer'), field('effectiveDate', '生效日期', 'LocalDate'), field('expireDate', '失效日期', 'LocalDate'), field('status', '状态', 'Integer', { required: true, dict: 'enabledStatus' }), field('remark', '备注')]
  },
  {
    name: 'processRoute', title: '工艺路线', icon: '艺', table: 'process_route', path: '/api/processRoute', description: '维护产品工序、工位、工时和质检节点。',
    visible: ['id', 'productModelId', 'routeVersion', 'sequenceNo', 'processName', 'processType', 'status'],
    fields: [id(), field('productModelId', '产品型号', 'Long', { required: true, relation: 'productModel' }), field('routeVersion', '路线版本', 'String', { required: true }), field('sequenceNo', '工序顺序', 'Integer', { required: true }), field('processCode', '工序编码', 'String', { required: true }), field('processName', '工序名称', 'String', { required: true }), field('processType', '工序类型', 'Integer', { required: true, dict: 'processType' }), field('stationCode', '默认工位编码'), field('stationName', '默认工位名称'), field('standardHours', '标准工时', 'BigDecimal'), field('qualityGate', '质检节点', 'Integer', { required: true, dict: 'yesNo' }), field('inspectionType', '检验类型', 'String', { dict: 'inspectionType' }), field('inspectionConfig', '检验配置', 'Text'), field('sopFile', 'SOP 文件'), field('status', '状态', 'Integer', { required: true, dict: 'enabledStatus' })]
  },
  {
    name: 'productionOrder', title: '生产工单', icon: '单', table: 'production_order', path: '/api/productionOrder', description: '维护生产计划、批次、数量、物料齐套和工单状态。',
    visible: ['id', 'orderNo', 'productModelId', 'quantity', 'lineName', 'materialReadyStatus', 'status'],
    fields: [id(), field('orderNo', '工单编号', 'String', { required: true }), field('productModelId', '产品型号', 'Long', { required: true, relation: 'productModel' }), field('bomVersion', 'BOM 版本', 'String', { required: true, versionSource: 'productBom', versionField: 'bomVersion' }), field('routeVersion', '路线版本', 'String', { required: true, versionSource: 'processRoute', versionField: 'routeVersion' }), field('batchNo', '生产批次'), field('quantity', '工单数量', 'Integer', { required: true }), field('plannedStartTime', '计划开始', 'LocalDateTime'), field('plannedEndTime', '计划结束', 'LocalDateTime'), field('deliveryDate', '交付日期', 'LocalDate'), field('lineName', '计划产线/班组', 'String', { dict: 'lineName' }), field('source', '来源', 'String', { dict: 'orderSource' }), field('priority', '优先级', 'Integer', { required: true, dict: 'priority' }), field('plannerId', '计划员', 'Long', { relation: 'sysUser' }), field('materialReadyStatus', '物料齐套', 'Integer', { required: true, dict: 'materialReady' }), field('status', '工单状态', 'Integer', { required: true, dict: 'orderStatus' }), createTime(), updateTime()]
  },
  {
    name: 'productionTask', title: '生产任务', icon: '任', table: 'production_task', path: '/api/productionTask', description: '维护工单下发到工序、工位、人员和完工情况。',
    visible: ['id', 'taskNo', 'orderId', 'sequenceNo', 'plannedQuantity', 'completedQuantity', 'status'],
    fields: [id(), field('taskNo', '任务单号', 'String', { required: true }), field('orderId', '生产工单', 'Long', { required: true, relation: 'productionOrder' }), field('processRouteId', '工艺步骤', 'Long', { required: true, relation: 'processRoute' }), field('sequenceNo', '工序顺序', 'Integer', { required: true }), field('stationCode', '执行工位'), field('assignedUserId', '指派人员', 'Long', { relation: 'sysUser' }), field('plannedQuantity', '计划数量', 'Integer', { required: true }), field('completedQuantity', '完工数量', 'Integer', { required: true }), field('defectQuantity', '不良数量', 'Integer', { required: true }), field('abnormalType', '异常类型', 'Integer', { dict: 'abnormalType' }), field('abnormalDesc', '异常描述', 'Text'), field('abnormalSolution', '处理措施', 'Text'), field('status', '任务状态', 'Integer', { required: true, dict: 'taskStatus' }), field('startTime', '实际开始', 'LocalDateTime'), field('finishTime', '实际完成', 'LocalDateTime'), field('remark', '备注')]
  },
  {
    name: 'workReport', title: '报工记录', icon: '报', table: 'work_report', path: '/api/workReport', description: '记录任务报工数量、合格数量、不良数量和工时。',
    visible: ['id', 'reportNo', 'taskId', 'orderId', 'reportQuantity', 'qualifiedQuantity', 'status'],
    fields: [id(), field('reportNo', '报工单号', 'String', { required: true }), field('taskId', '生产任务', 'Long', { required: true, relation: 'productionTask' }), field('orderId', '生产工单', 'Long', { required: true, relation: 'productionOrder' }), field('productSn', '产品 SN'), field('operatorId', '操作员', 'Long', { required: true, relation: 'sysUser' }), field('stationCode', '报工工位'), field('reportQuantity', '报工数量', 'Integer', { required: true }), field('qualifiedQuantity', '合格数量', 'Integer', { required: true }), field('defectQuantity', '不良数量', 'Integer', { required: true }), field('actualHours', '实际工时', 'BigDecimal'), field('defectReason', '不良原因'), field('reportTime', '报工时间', 'LocalDateTime', { required: true }), field('status', '状态', 'Integer', { required: true, dict: 'reportStatus' })]
  },
  {
    name: 'inspectionRecord', title: '检验记录', icon: '检', table: 'inspection_record', path: '/api/inspectionRecord', description: '记录过程检、终检、复检或设备检测结果。',
    visible: ['id', 'inspectionNo', 'inspectionType', 'orderId', 'productSn', 'result', 'approvalStatus'],
    fields: [id(), field('inspectionNo', '检验单号', 'String', { required: true }), field('inspectionType', '检验类型', 'String', { required: true, dict: 'inspectionType' }), field('taskId', '生产任务', 'Long', { relation: 'productionTask' }), field('orderId', '生产工单', 'Long', { required: true, relation: 'productionOrder' }), field('productSn', '产品 SN'), field('processRouteId', '工艺步骤', 'Long', { relation: 'processRoute' }), field('standardSnapshot', '检验标准快照', 'Text'), field('inspectorId', '检验员', 'Long', { relation: 'sysUser' }), field('sourceType', '数据来源', 'String', { required: true, dict: 'dataSource' }), field('equipmentNo', '检测设备编号'), field('inspectionTime', '检验时间', 'LocalDateTime', { required: true }), field('result', '检验结果', 'Integer', { required: true, dict: 'inspectionResult' }), field('measuredData', '实测数据', 'Text'), field('defectReason', '不合格原因'), field('handlingMethod', '处置方式', 'Integer', { dict: 'handlingMethod' }), field('approvalStatus', '审批状态', 'Integer', { required: true, dict: 'approvalStatus' }), field('remark', '备注')]
  },
  {
    name: 'reworkOrder', title: '返修工单', icon: '修', table: 'rework_order', path: '/api/reworkOrder', description: '维护由检验记录触发的返修要求、责任人、措施和复检。',
    visible: ['id', 'reworkNo', 'sourceInspectionId', 'orderId', 'assigneeId', 'status', 'repairTime'],
    fields: [id(), field('reworkNo', '返修单号', 'String', { required: true }), field('sourceInspectionId', '来源检验', 'Long', { required: true, relation: 'inspectionRecord' }), field('orderId', '生产工单', 'Long', { required: true, relation: 'productionOrder' }), field('taskId', '来源任务', 'Long', { relation: 'productionTask' }), field('productSn', '产品 SN'), field('defectReason', '不合格原因'), field('reworkRequirement', '返修要求', 'Text'), field('assigneeId', '维修责任人', 'Long', { relation: 'sysUser' }), field('deadline', '截止时间', 'LocalDateTime'), field('status', '返修状态', 'Integer', { required: true, dict: 'reworkStatus' }), field('repairAction', '维修措施', 'Text'), field('replacedMaterial', '更换物料', 'Text'), field('repairHours', '维修工时', 'BigDecimal'), field('repairResult', '维修结果'), field('repairTime', '维修完成时间', 'LocalDateTime'), field('recheckInspectionId', '复检记录', 'Long', { relation: 'inspectionRecord' }), createTime()]
  }
];

export const resourceMap = Object.fromEntries(resources.map((resource) => [resource.name, resource]));

export const relationLabelFields = {
  sysUser: ['employeeNo', 'name'],
  productModel: ['modelCode', 'modelName'],
  material: ['materialCode', 'materialName'],
  productBom: ['bomVersion', 'materialId'],
  processRoute: ['routeVersion', 'processName'],
  productionOrder: ['orderNo', 'lineName'],
  productionTask: ['taskNo', 'stationCode'],
  workReport: ['reportNo', 'productSn'],
  inspectionRecord: ['inspectionNo', 'productSn'],
  reworkOrder: ['reworkNo', 'productSn']
};
