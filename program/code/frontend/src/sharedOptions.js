import { markRaw } from 'vue';
import { ALL_ACCESS, emptyAccess, FIELD_WRITE_ACCESS, ROLE_ACCESS } from './access.js';
import { ApiClient, DEFAULT_API_BASE } from './api.js';
import { dictionaries, moduleGroups, relationLabelFields, resourceMap, resources } from './domain.js';
import { workflowEntries } from './workflow.js';

const STORAGE_KEY = 'keyboardMesApiBase';
const OPERATION_LOG_KEY = 'keyboardMesOperationLogs';
const OPERATION_LOG_LIMIT = 20;
const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];
const COPYABLE_FIELD_NAMES = new Set([
  'employeeNo', 'modelCode', 'materialCode', 'processCode', 'stationCode',
  'orderNo', 'taskNo', 'reportNo', 'inspectionNo', 'reworkNo', 'productSn'
]);
const client = new ApiClient(localStorage.getItem(STORAGE_KEY) || DEFAULT_API_BASE);

export const appState = {
  resources,
  moduleGroups: moduleGroups.map((group) => ({
    ...group,
    resources: group.names.map((name) => resourceMap[name]).filter(Boolean)
  })),
  currentResource: resourceMap.productionOrder || resources[0],
  activeView: 'manage',
  activeWorkflowKey: 'order',
  workflowEntries,
  records: [],
  pageSize: 20,
  pageSizeOptions: PAGE_SIZE_OPTIONS,
  currentPage: 1,
  sortField: '',
  sortDirection: 'asc',
  selectedRecord: null,
  detailDialogOpen: false,
  selectedRecordIds: [],
  batchStatusValue: '',
  statusFilterValue: '',
  mineOnly: false,
  attentionFilter: '',
  fieldFilter: null,
  pendingListFilter: null,
  recordDraft: {},
  draftBaseline: null,
  relationCache: {},
  searchKeyword: '',
  productModelFilter: '',
  authUser: null,
  loginForm: { employeeNo: '', password: '' },
  traceForm: { productSn: '' },
  traceGroups: [],
  traceSortMode: 'business',
  traceSearched: false,
  orderReport: null,
  reportOverview: null,
  reportQuality: null,
  dashboardFilter: { period: 'week', productModelId: '' },
  todoItems: [],
  todoLoadedAt: null,
  operationLogs: [],
  backendStatus: 'checking',
  backendStatusText: '正在确认生产系统',
  statusText: '正在确认登录状态',
  statusType: '',
  toastMessage: '',
  toastVisible: false,
  toastTimer: null,
  dashboardLayoutFrame: null,
  dashboardResizeObserver: null,
  loading: { login: false, records: false, save: false, delete: false, trace: false, taskAction: false, orderReport: false, dashboard: false, todos: false, batch: false }
};

export const sharedComputed = {
  businessDate() {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
  },
  currentGroupTitle() {
    const group = this.visibleModuleGroups.find((item) => item.resources.some((resource) => resource.name === this.currentResource?.name));
    return group ? group.title : '业务模块';
  },
  viewEyebrow() {
    if (this.activeView === 'trace') {
      return '产品追溯';
    }
    if (this.activeView === 'dashboard') {
      return '生产报表';
    }
    if (this.activeView === 'operations') {
      return '操作记录';
    }
    return this.currentGroupTitle;
  },
  viewTitle() {
    if (this.activeView === 'trace') {
      return '产品追溯';
    }
    if (this.activeView === 'dashboard') {
      return '可视化报表';
    }
    if (this.activeView === 'operations') {
      return '最近操作日志';
    }
    return this.currentResource ? this.currentResource.title : '业务模块';
  },
  viewDescription() {
    if (this.activeView === 'trace') {
      return '按产品 SN 查询生产、报工、检验、返修全过程';
    }
    if (this.activeView === 'dashboard') {
      return '集中查看产量进度、质量分布、工位产出和近日报工趋势';
    }
    if (this.activeView === 'operations') {
      return '查看当前账号最近 20 条业务操作';
    }
    return this.currentResource ? this.currentResource.description : '请选择一个业务模块';
  },
  visibleModuleGroups() {
    return this.moduleGroups
      .map((group) => ({ ...group, resources: group.resources.filter((resource) => this.canOpenResource(resource.name)) }))
      .filter((group) => group.resources.length > 0);
  },
  visibleWorkflowEntries() {
    return this.workflowEntries.filter((entry) => this.canOpenWorkflow(entry));
  },
  formFields() {
    return (this.currentResource?.fields || []).filter((field) => !field.hidden);
  },
  visibleFields() {
    if (!this.currentResource) {
      return [];
    }
    return this.currentResource.visible
      .map((name) => this.formFields.find((field) => field.name === name))
      .filter(Boolean);
  },
  requiredFieldCount() {
    return this.formFields.filter((field) => field.required && !this.isReadonlyField(field)).length;
  },
  canCreateRecord() {
    return Boolean(this.currentResource) && !this.currentResource.restrictedCreate && this.canPerform('create', this.currentResource.name);
  },
  canUpdateRecord() {
    return Boolean(this.currentResource) && this.canPerform('update', this.currentResource.name);
  },
  canDeleteRecord() {
    return Boolean(this.currentResource) && this.canPerform('delete', this.currentResource.name);
  },
  canExportRecords() {
    return this.activeView === 'manage'
      && Boolean(this.currentResource)
      && this.canPerform('export', this.currentResource.name)
      && this.canReadResource(this.currentResource.name)
      && !this.loading.records
      && this.filteredRecords.length > 0;
  },
  exportFields() {
    return this.formFields.filter((field) => !field.sensitive);
  },
  selectedRecordSet() {
    return new Set(this.selectedRecordIds.map(String));
  },
  selectedRecords() {
    return this.records.filter((record) => record?.id !== null && record?.id !== undefined && this.selectedRecordSet.has(String(record.id)));
  },
  selectedRecordCount() {
    return this.selectedRecords.length;
  },
  allVisibleRecordsSelected() {
    const ids = this.paginatedRecords.map((record) => this.recordKey(record)).filter(Boolean);
    return ids.length > 0 && ids.every((id) => this.selectedRecordSet.has(id));
  },
  tableColspan() {
    return Math.max(this.visibleFields.length + 1, 1);
  },
  batchStatusField() {
    return this.formFields.find((field) => field.name === 'status' && field.dict) || null;
  },
  batchStatusOptions() {
    return this.batchStatusField ? this.dictionaryOptions(this.batchStatusField.dict) : [];
  },
  canBatchExportRecords() {
    return this.selectedRecordCount > 0
      && Boolean(this.currentResource)
      && this.canPerform('export', this.currentResource.name)
      && this.canReadResource(this.currentResource.name);
  },
  canBatchDeleteRecords() {
    return this.selectedRecordCount > 0
      && Boolean(this.currentResource)
      && this.canDeleteRecord
      && !this.loading.delete
      && !this.loading.batch;
  },
  canBatchUpdateStatus() {
    return this.selectedRecordCount > 0
      && Boolean(this.currentResource)
      && Boolean(this.batchStatusField)
      && this.canUpdateRecord
      && this.canWriteResourceField(this.currentResource.name, this.batchStatusField)
      && this.batchStatusValue !== ''
      && !this.loading.batch;
  },
  todoSummaryText() {
    if (this.loading.todos) {
      return '正在刷新待办';
    }
    const total = this.visibleTodoItems.reduce((sum, item) => sum + Number(item.count || 0), 0);
    return total > 0 ? `当前待处理 ${total} 项` : '当前暂无待处理事项';
  },
  visibleTodoItems() {
    return this.todoItems;
  },
  recentOperationLogs() {
    return this.operationLogs.slice(0, OPERATION_LOG_LIMIT);
  },
  statusFilterField() {
    const preferredNames = ['status', 'approvalStatus', 'result', 'materialReadyStatus'];
    return preferredNames
      .map((name) => this.visibleFields.find((field) => field.name === name && field.dict) || this.formFields.find((field) => field.name === name && field.dict))
      .find(Boolean) || null;
  },
  statusFilterOptions() {
    return this.statusFilterField ? this.dictionaryOptions(this.statusFilterField.dict) : [];
  },
  mineFilterField() {
    const ownerFields = {
      productionOrder: 'plannerId',
      productionTask: 'assignedUserId',
      workReport: 'operatorId',
      inspectionRecord: 'inspectorId',
      reworkOrder: 'assigneeId'
    };
    const fieldName = ownerFields[this.currentResource?.name];
    return fieldName && this.formFields.some((field) => field.name === fieldName) ? fieldName : '';
  },
  mineFilterLabel() {
    const labels = {
      productionOrder: '我的工单',
      productionTask: '我的任务',
      workReport: '我的报工',
      inspectionRecord: '我的检验',
      reworkOrder: '我的返修'
    };
    return labels[this.currentResource?.name] || '我的';
  },
  canUseMineFilter() {
    return Boolean(this.authUser?.id && this.mineFilterField);
  },
  overdueRecordCount() {
    return this.records.filter((record) => this.isRecordOverdue(record)).length;
  },
  canUseOverdueFilter() {
    return ['productionOrder', 'reworkOrder'].includes(this.currentResource?.name) && this.overdueRecordCount > 0;
  },
  hasActiveListFilter() {
    return Boolean(this.statusFilterValue || this.mineOnly || this.attentionFilter || this.fieldFilter);
  },
  showQuickFilterBar() {
    return this.statusFilterOptions.length > 0 || this.canUseMineFilter || this.canUseOverdueFilter || this.hasActiveListFilter;
  },
  firstVisibleFieldName() {
    return this.visibleFields[0]?.name || '';
  },
  saveDisabled() {
    if (!this.currentResource) {
      return true;
    }
    if (!this.selectedRecord && this.canSubmitCurrentDraft) {
      return false;
    }
    return this.selectedRecord ? !this.canUpdateRecord : !this.canCreateRecord;
  },
  canSubmitCurrentDraft() {
    return ['workReport', 'inspectionRecord'].includes(this.currentResource?.name)
      && this.canPerform('submit', this.currentResource.name);
  },
  primaryActionText() {
    if (!this.selectedRecord && this.currentResource?.name === 'workReport' && this.canSubmitCurrentDraft) {
      return '提交报工';
    }
    if (!this.selectedRecord && this.currentResource?.name === 'inspectionRecord' && this.canSubmitCurrentDraft) {
      return '提交质检';
    }
    return '保存';
  },
  primaryActionLabel() {
    if (!this.loading.save) {
      return this.primaryActionText;
    }
    return this.canSubmitCurrentDraft ? '提交中' : '保存中';
  },
  refreshActionLabel() {
    if (this.activeView === 'dashboard') {
      return this.loading.dashboard ? '刷新中' : '刷新';
    }
    if (this.activeView === 'operations') {
      return '刷新';
    }
    return this.loading.records ? '刷新中' : '刷新';
  },
  deleteActionLabel() {
    return this.loading.delete ? '删除中' : '删除';
  },
  canGenerateTasks() {
    return this.currentResource?.name === 'productionOrder'
      && Boolean(this.selectedRecord?.id)
      && this.canPerform('generateTasks', 'productionOrder');
  },
  showOrderTrackingReport() {
    return this.currentResource?.name === 'productionOrder' && Boolean(this.selectedRecord?.id);
  },
  orderReportCards() {
    const report = this.orderReport || {};
    return [
      { label: '工单数量', value: this.numberText(report.targetQuantity) },
      { label: '成品完成', value: this.numberText(report.completedQuantity) },
      { label: '合格数', value: this.numberText(report.qualifiedQuantity) },
      { label: '不良数', value: this.numberText(report.defectQuantity) },
      { label: '完成率', value: this.percentText(report.progressRate) },
      { label: '合格率', value: this.percentText(report.qualityRate) }
    ];
  },
  orderReportProgressWidth() {
    const rate = Number(this.orderReport?.progressRate || 0);
    return `${Math.max(0, Math.min(100, rate))}%`;
  },
  orderReportTasks() {
    return this.orderReport?.tasks || [];
  },
  orderReportTodos() {
    return this.orderReport?.todos || [];
  },
  dashboardCards() {
    const overview = this.reportOverview || {};
    const quality = this.reportQuality || {};
    const periodLabel = quality.periodLabel || ({ day: '本日', month: '本月' }[this.dashboardFilter.period] || '本周');
    const modelCount = this.dashboardFilter.productModelId
      ? (Number(quality.totalQuantity || 0) > 0 ? 1 : 0)
      : (quality.productBreakdown || []).length;
    return [
      { label: `${periodLabel}总产量`, value: this.numberText(quality.totalQuantity), note: quality.productModelName || '全部产品', tone: 'primary' },
      { label: '合格产量', value: this.numberText(quality.qualifiedQuantity), note: `合格率 ${this.percentText(quality.qualityRate)}`, tone: 'success' },
      { label: '不良数量', value: this.numberText(quality.defectQuantity), note: '当前筛选范围', tone: 'danger' },
      { label: '覆盖型号', value: this.numberText(modelCount), note: this.dashboardFilter.productModelId ? '单一型号' : '有产量型号', tone: 'neutral' },
      { label: '生产中任务', value: this.numberText(overview.runningTasks), note: '全局实时状态', tone: 'warning' },
      { label: '待处理返修', value: this.numberText(overview.pendingReworks), note: '全局实时状态', tone: 'danger' }
    ];
  },
  dashboardPeriodLabel() {
    return this.reportQuality?.periodLabel || ({ day: '本日', month: '本月' }[this.dashboardFilter.period] || '本周');
  },
  dashboardDateRange() {
    const start = this.reportQuality?.periodStart;
    const end = this.reportQuality?.periodEnd;
    return start && end ? `${start} 至 ${end}` : '等待报表数据';
  },
  dashboardProductModels() {
    return this.reportQuality?.productModels || [];
  },
  dashboardSelectedProductLabel() {
    return this.reportQuality?.productModelName || '全部产品';
  },
  dashboardProgressRate() {
    return this.boundedPercent(this.reportOverview?.progressRate);
  },
  dashboardProgressStyle() {
    const rate = this.dashboardProgressRate;
    return {
      background: `conic-gradient(var(--primary) 0 ${rate}%, #e2e8f0 ${rate}% 100%)`
    };
  },
  dashboardQualityRate() {
    return this.boundedPercent(this.reportQuality?.qualityRate ?? this.reportOverview?.qualityRate);
  },
  dashboardQualityStyle() {
    const rate = this.dashboardQualityRate;
    return {
      background: `conic-gradient(var(--success) 0 ${rate}%, #e2e8f0 ${rate}% 100%)`
    };
  },
  dashboardQualitySegments() {
    const quality = this.reportQuality || {};
    const qualified = Math.max(0, Number(quality.qualifiedQuantity || 0));
    const defect = Math.max(0, Number(quality.defectQuantity || 0));
    const reportQuantity = Math.max(0, Number(quality.totalQuantity || 0));
    const pending = Math.max(0, reportQuantity - qualified - defect);
    const total = Math.max(reportQuantity, qualified + defect, 1);
    return [
      { label: '合格', value: qualified, width: `${this.boundedPercent(qualified * 100 / total)}%`, className: 'qualified' },
      { label: '不良', value: defect, width: `${this.boundedPercent(defect * 100 / total)}%`, className: 'defect' },
      { label: '其他', value: pending, width: `${this.boundedPercent(pending * 100 / total)}%`, className: 'unfinished' }
    ];
  },
  workstationBars() {
    const rows = this.reportOverview?.workstations || [];
    const maxQuantity = Math.max(1, ...rows.map((item) => Number(item.reportQuantity || 0)));
    return rows.map((item) => ({
      key: item.name,
      label: item.name,
      value: Number(item.reportQuantity || 0),
      defect: Number(item.defectQuantity || 0),
      width: `${this.boundedPercent(Number(item.reportQuantity || 0) * 100 / maxQuantity)}%`
    }));
  },
  defectTypeBars() {
    const rows = this.reportQuality?.defectTypes || [];
    const maxCount = Math.max(1, ...rows.map((item) => Number(item.count || 0)));
    return rows.map((item) => ({
      key: item.name,
      label: item.name,
      value: Number(item.count || 0),
      percent: this.percentText(item.percent),
      width: `${this.boundedPercent(Number(item.count || 0) * 100 / maxCount)}%`
    }));
  },
  productModelBars() {
    const rows = this.reportQuality?.productBreakdown || [];
    return rows.map((item) => ({
      key: item.productModelId,
      label: [item.modelCode, item.modelName].filter(Boolean).join(' · '),
      category: item.category || '未分类',
      value: Number(item.quantity || 0),
      percent: this.percentText(item.percent),
      width: `${this.boundedPercent(item.percent)}%`
    }));
  },
  trendBars() {
    const rows = this.reportQuality?.trendData || [];
    const maxQuantity = Math.max(1, ...rows.map((item) => Number(item.quantity || 0)));
    return rows.map((item) => ({
      key: `${item.date}-${item.label || ''}`,
      label: item.label || String(item.date || '').slice(5) || '-',
      value: Number(item.quantity || 0),
      height: `${Math.max(8, this.boundedPercent(Number(item.quantity || 0) * 100 / maxQuantity))}%`
    }));
  },
  canRunTaskAction() {
    return this.currentResource?.name === 'productionTask'
      && Boolean(this.selectedRecord?.id)
      && this.canPerform('taskAction', 'productionTask');
  },
  canRepairCurrentRework() {
    return this.currentResource?.name === 'reworkOrder'
      && Boolean(this.selectedRecord?.id)
      && this.canPerform('repair', 'reworkOrder');
  },
  canRecheckCurrentRework() {
    return this.currentResource?.name === 'reworkOrder'
      && Boolean(this.selectedRecord?.id)
      && this.canPerform('recheck', 'reworkOrder');
  },
  selectedSummary() {
    if (!this.selectedRecord?.id) {
      return '未选择';
    }
    const fields = relationLabelFields[this.currentResource?.name] || this.currentResource?.visible || [];
    const text = fields
      .map((name) => this.selectedRecord[name])
      .filter((value) => value !== null && value !== undefined && value !== '')
      .join(' / ');
    return text ? `#${this.selectedRecord.id} ${text}` : `#${this.selectedRecord.id}`;
  },
  supportsProductModelFilter() {
    return ['productBom', 'processRoute'].includes(this.currentResource?.name);
  },
  productModelFilterOptions() {
    const models = this.relationCache.productModel || [];
    const modelById = new Map(models.map((model) => [String(model.id), model]));
    const modelIds = [...new Set(this.records.map((record) => record.productModelId).filter(Boolean).map(String))];
    return modelIds.map((value) => {
      const model = modelById.get(value);
      const label = model
        ? [model.modelCode, model.modelName].filter(Boolean).join(' · ')
        : `产品型号 #${value}`;
      return { value, label };
    }).sort((left, right) => left.label.localeCompare(right.label, 'zh-CN', { numeric: true }));
  },
  filteredRecords() {
    const keyword = this.searchKeyword.trim().toLowerCase();
    const selectedProductModel = this.productModelFilter;
    const statusFieldName = this.statusFilterField?.name;
    const statusValue = this.statusFilterValue;
    const fieldFilter = this.fieldFilter;
    const fieldFilterValues = fieldFilter?.values ? new Set(fieldFilter.values.map(String)) : null;
    const mineField = this.mineFilterField;
    const userId = this.authUser?.id ? String(this.authUser.id) : '';
    return this.records.filter((record) => {
      const matchesProductModel = !this.supportsProductModelFilter
        || !selectedProductModel
        || String(record.productModelId) === selectedProductModel;
      const matchesKeyword = !keyword || JSON.stringify(record).toLowerCase().includes(keyword);
      const matchesStatus = !statusFieldName || statusValue === '' || String(record[statusFieldName]) === String(statusValue);
      const matchesFieldFilter = !fieldFilterValues || fieldFilterValues.has(String(record[fieldFilter.field]));
      const matchesMine = !this.mineOnly || !mineField || String(record[mineField]) === userId;
      const matchesAttention = !this.attentionFilter || (this.attentionFilter === 'overdue' && this.isRecordOverdue(record));
      return matchesProductModel && matchesKeyword && matchesStatus && matchesFieldFilter && matchesMine && matchesAttention;
    });
  },
  sortedRecords() {
    if (!this.sortField) {
      return this.filteredRecords;
    }
    const field = this.visibleFields.find((item) => item.name === this.sortField);
    if (!field) {
      return this.filteredRecords;
    }
    const direction = this.sortDirection === 'desc' ? -1 : 1;
    return this.filteredRecords
      .map((record, index) => ({ record, index }))
      .sort((left, right) => {
        const compared = this.compareRecordValues(field, left.record, right.record);
        return compared === 0 ? left.index - right.index : compared * direction;
      })
      .map((entry) => entry.record);
  },
  totalPages() {
    return Math.max(1, Math.ceil(this.filteredRecords.length / this.pageSize));
  },
  paginatedRecords() {
    const page = Math.min(this.currentPage, this.totalPages);
    const start = (page - 1) * this.pageSize;
    return this.sortedRecords.slice(start, start + this.pageSize);
  },
  paginationLabel() {
    if (this.filteredRecords.length === 0) {
      return '共 0 条';
    }
    const page = Math.min(this.currentPage, this.totalPages);
    const start = (page - 1) * this.pageSize + 1;
    const end = Math.min(page * this.pageSize, this.filteredRecords.length);
    return `${start}-${end} / 共 ${this.filteredRecords.length} 条`;
  },
  canPreviousPage() {
    return this.currentPage > 1;
  },
  canNextPage() {
    return this.currentPage < this.totalPages;
  },
  hasUnsavedChanges() {
    return this.activeView === 'manage'
      && this.draftBaseline !== null
      && this.serializeDraft() !== this.draftBaseline;
  },
  traceIsEmpty() {
    return this.traceSearched && this.traceGroups.every((group) => group.items.length === 0);
  },
  traceDisplayGroups() {
    return this.traceGroups.map((group) => ({
      ...group,
      items: this.sortTraceItems(group.resourceName, group.items)
    }));
  },
  traceTimelineItems() {
    return this.traceDisplayGroups
      .flatMap((group) => group.items.map((item) => ({
        key: `${group.resourceName}-${item.id}`,
        groupTitle: group.title,
        resourceName: group.resourceName,
        item,
        fields: group.fields,
        time: this.traceRecordTimestamp(group.resourceName, item),
        timeText: this.traceRecordTimeText(group.resourceName, item)
      })))
      .sort((left, right) => left.time - right.time || Number(left.item.id || 0) - Number(right.item.id || 0));
  },
  currentUserName() {
    return this.authUser?.name || this.authUser?.employeeNo || '未登录';
  },
  currentUserRoleText() {
    return this.authUser?.roleName || this.authUser?.roleCode || '未登录';
  },
  currentUserDepartmentText() {
    return this.authUser?.department || '未分配部门';
  }
};

export const sharedMethods = {
  normalizedRole() {
    return String(this.authUser?.roleCode || '').trim().toLowerCase();
  },
  currentAccess() {
    return ROLE_ACCESS[this.normalizedRole()] || emptyAccess;
  },
  allows(scope, name) {
    if (!this.authUser) {
      return false;
    }
    const allowed = this.currentAccess()[scope] || [];
    return allowed === ALL_ACCESS || allowed.includes(name);
  },
  canOpenResource(name) {
    return this.allows('menu', name);
  },
  canReadResource(name) {
    return this.allows('read', name);
  },
  canUseWorkflow(key) {
    return this.allows('workflows', key);
  },
  canOpenWorkflow(entry) {
    return this.canUseWorkflow(entry.key) && (!entry.resource || this.canOpenResource(entry.resource));
  },
  canPerform(action, resourceName) {
    return this.allows(action, resourceName);
  },
  canWriteField(field) {
    if (!this.currentResource || field.hidden || field.readonly || ['id', 'createTime', 'updateTime'].includes(field.name)) {
      return false;
    }
    if (field.sensitive && this.selectedRecord) {
      return false;
    }
    if (this.selectedRecord && !this.canUpdateRecord) {
      return false;
    }
    if (!this.selectedRecord && !this.canCreateRecord) {
      return false;
    }
    const fieldAccess = FIELD_WRITE_ACCESS[this.normalizedRole()];
    if (fieldAccess === ALL_ACCESS) {
      return true;
    }
    const allowedFields = fieldAccess?.[this.currentResource.name];
    return allowedFields === ALL_ACCESS || Boolean(allowedFields?.includes(field.name));
  },
  canWriteResourceField(resourceName, field) {
    if (!resourceName || !field || field.hidden || field.readonly || field.sensitive || ['id', 'createTime', 'updateTime'].includes(field.name)) {
      return false;
    }
    const fieldAccess = FIELD_WRITE_ACCESS[this.normalizedRole()];
    if (fieldAccess === ALL_ACCESS) {
      return true;
    }
    const allowedFields = fieldAccess?.[resourceName];
    return allowedFields === ALL_ACCESS || Boolean(allowedFields?.includes(field.name));
  },
  firstAllowedResource() {
    const group = this.visibleModuleGroups.find((item) => item.resources.length > 0);
    return group?.resources[0] || null;
  },
  defaultRoute() {
    const firstWorkflow = this.visibleWorkflowEntries[0];
    if (firstWorkflow?.type === 'dashboard') {
      return { name: 'dashboard' };
    }
    if (firstWorkflow?.type === 'trace') {
      return { name: 'trace' };
    }
    if (firstWorkflow?.resource) {
      return { name: 'work', params: { resourceName: firstWorkflow.resource } };
    }
    const firstResource = this.firstAllowedResource();
    return firstResource ? { name: 'work', params: { resourceName: firstResource.name } } : { name: 'login' };
  },
  async checkBackendConnection() {
    this.backendStatus = 'checking';
    this.backendStatusText = '正在确认生产系统';
    try {
      await client.health();
      this.backendStatus = 'ok';
      this.backendStatusText = '生产系统可用';
    } catch (error) {
      this.backendStatus = 'warn';
      this.backendStatusText = '生产系统暂不可用';
      this.setStatus('生产系统暂不可用', 'warn');
    }
  },
  async loadCurrentUser() {
    try {
      this.authUser = await client.currentUser();
      this.loadOperationLogs();
      this.setStatus('系统就绪', 'ok');
    } catch (error) {
      this.authUser = null;
      this.operationLogs = [];
      this.setStatus(error.code === 401 ? '请先登录' : error.message, 'warn');
    }
  },
  async login() {
    if (!this.loginForm.employeeNo || !this.loginForm.password) {
      this.showToast('请输入工号和密码');
      return;
    }
    this.loading.login = true;
    try {
      const data = await client.login(this.loginForm);
      client.setSessionId(data.sessionId);
      this.authUser = data.user;
      this.loadOperationLogs();
      this.loginForm.password = '';
      this.setStatus('登录成功', 'ok');
      this.showToast('登录成功');
      await this.$router.replace(this.defaultRoute());
    } catch (error) {
      this.setStatus(error.message, 'warn');
      this.showToast(error.message);
    } finally {
      this.loading.login = false;
    }
  },
  async logout() {
    if (!this.confirmDiscardChanges()) {
      return;
    }
    try {
      await client.logout();
    } catch (error) {
      // 即使退出请求失败，也清理页面状态，避免继续留在工作台。
    }
    client.setSessionId(null);
    this.authUser = null;
    this.records = [];
    this.todoItems = [];
    this.operationLogs = [];
    this.resetListFilters();
    this.clearBatchSelection();
    this.clearSelection();
    this.setStatus('已退出登录', 'warn');
    await this.$router.replace({ name: 'login' });
  },
  async ensureLoggedIn() {
    if (!this.authUser) {
      await this.loadCurrentUser();
    }
    if (!this.authUser) {
      await this.$router.replace({ name: 'login' });
      return false;
    }
    return true;
  },
  async selectResource(name, navigate = true) {
    const nextResource = resourceMap[name];
    if (!nextResource) {
      return;
    }
    if (!this.canOpenResource(name)) {
      this.showToast('当前角色不能进入该模块');
      if (navigate) {
        await this.$router.replace(this.defaultRoute());
      }
      return;
    }
    if (navigate) {
      await this.$router.push({ name: 'work', params: { resourceName: name } });
      return;
    }
    this.currentResource = nextResource;
    this.activeView = 'manage';
    this.activeWorkflowKey = this.workflowEntries.find((entry) => entry.resource === name)?.key || '';
    const pendingFilter = this.pendingListFilter?.resourceName === name ? this.pendingListFilter : null;
    this.pendingListFilter = null;
    this.searchKeyword = '';
    this.productModelFilter = '';
    this.currentPage = 1;
    this.resetListFilters();
    this.clearBatchSelection();
    this.clearSelection();
    await this.loadRelationsForResource();
    await this.loadRecords();
    if (pendingFilter) {
      this.applyListFilter(pendingFilter);
    }
    await this.loadTodayTodos();
  },
  async openWorkflow(entry) {
    if (!this.canOpenWorkflow(entry)) {
      this.showToast('当前角色不能进入该业务');
      return;
    }
    if (entry.type === 'dashboard') {
      await this.$router.push({ name: 'dashboard' });
      return;
    }
    if (entry.type === 'trace') {
      await this.$router.push({ name: 'trace' });
      return;
    }
    await this.selectResource(entry.resource);
  },
  async syncRoute() {
    const ok = await this.ensureLoggedIn();
    if (!ok) {
      return;
    }
    if (this.$route.name === 'trace') {
      if (!this.canUseWorkflow('trace')) {
        this.showToast('当前角色不能进入产品追溯');
        await this.$router.replace(this.defaultRoute());
        return;
      }
      this.activeView = 'trace';
      this.activeWorkflowKey = 'trace';
      this.traceSearched = false;
      this.traceGroups = [];
      return;
    }
    if (this.$route.name === 'dashboard') {
      if (!this.canUseWorkflow('dashboard')) {
        this.showToast('当前角色不能进入可视化报表');
        await this.$router.replace(this.defaultRoute());
        return;
      }
      this.activeView = 'dashboard';
      this.activeWorkflowKey = 'dashboard';
      this.records = [];
      this.clearSelection();
      await this.loadDashboardReports();
      return;
    }
    if (this.$route.name === 'operations') {
      this.activeView = 'operations';
      this.activeWorkflowKey = 'operations';
      this.records = [];
      this.clearSelection();
      this.clearBatchSelection();
      this.loadOperationLogs();
      this.setStatus('操作日志已更新', 'ok');
      return;
    }
    const routeResource = this.$route.params.resourceName;
    const nextName = routeResource || this.firstAllowedResource()?.name;
    if (!nextName) {
      this.currentResource = null;
      this.records = [];
      this.activeWorkflowKey = '';
      this.setStatus('当前账号暂无可用业务', 'warn');
      return;
    }
    if (!this.canOpenResource(nextName)) {
      this.showToast('当前角色不能进入该模块');
      await this.$router.replace(this.defaultRoute());
      return;
    }
    await this.selectResource(nextName, false);
  },
  async refreshCurrentView() {
    if (this.activeView === 'dashboard') {
      await this.loadDashboardReports();
      return;
    }
    if (this.activeView === 'operations') {
      this.loadOperationLogs();
      this.setStatus('操作日志已更新', 'ok');
      return;
    }
    await this.loadRecords();
    await this.loadTodayTodos();
  },
  async loadRecords() {
    if (!this.currentResource || !this.authUser || this.activeView !== 'manage') {
      return;
    }
    if (!this.canReadResource(this.currentResource.name)) {
      this.records = [];
      this.setStatus('当前角色不能查看该模块', 'warn');
      return;
    }
    this.loading.records = true;
    try {
      const data = await client.list(this.currentResource);
      this.records = Array.isArray(data) ? data : [];
      this.pruneBatchSelection();
      this.currentPage = Math.min(this.currentPage, Math.max(1, Math.ceil(this.filteredRecords.length / this.pageSize)));
      this.setStatus('记录已更新', 'ok');
    } catch (error) {
      this.records = [];
      this.handleRequestError(error);
    } finally {
      this.loading.records = false;
    }
  },
  goToPage(page) {
    const nextPage = Number(page);
    if (!Number.isFinite(nextPage)) {
      return;
    }
    this.currentPage = Math.max(1, Math.min(Math.trunc(nextPage), this.totalPages));
  },
  previousPage() {
    this.goToPage(this.currentPage - 1);
  },
  nextPage() {
    this.goToPage(this.currentPage + 1);
  },
  recordKey(record) {
    return record?.id === null || record?.id === undefined ? '' : String(record.id);
  },
  isRecordChecked(record) {
    const key = this.recordKey(record);
    return Boolean(key) && this.selectedRecordSet.has(key);
  },
  toggleRecordSelection(record) {
    const key = this.recordKey(record);
    if (!key) {
      return;
    }
    const selected = new Set(this.selectedRecordIds.map(String));
    if (selected.has(key)) {
      selected.delete(key);
    } else {
      selected.add(key);
    }
    this.selectedRecordIds = [...selected];
  },
  toggleCurrentPageSelection() {
    const ids = this.paginatedRecords.map((record) => this.recordKey(record)).filter(Boolean);
    if (ids.length === 0) {
      return;
    }
    const selected = new Set(this.selectedRecordIds.map(String));
    const shouldClear = ids.every((id) => selected.has(id));
    ids.forEach((id) => {
      if (shouldClear) {
        selected.delete(id);
      } else {
        selected.add(id);
      }
    });
    this.selectedRecordIds = [...selected];
  },
  clearBatchSelection() {
    this.selectedRecordIds = [];
    this.batchStatusValue = '';
  },
  pruneBatchSelection() {
    const existingIds = new Set(this.records.map((record) => this.recordKey(record)).filter(Boolean));
    this.selectedRecordIds = this.selectedRecordIds.filter((id) => existingIds.has(String(id)));
  },
  resetListFilters() {
    this.statusFilterValue = '';
    this.mineOnly = false;
    this.attentionFilter = '';
    this.fieldFilter = null;
    this.sortField = '';
    this.sortDirection = 'asc';
  },
  onListFilterChanged() {
    this.currentPage = 1;
    this.clearBatchSelection();
  },
  setStatusFilter(value) {
    this.statusFilterValue = value;
    this.fieldFilter = null;
    this.onListFilterChanged();
  },
  toggleMineFilter() {
    this.mineOnly = !this.mineOnly;
    this.onListFilterChanged();
  },
  toggleAttentionFilter(name) {
    this.attentionFilter = this.attentionFilter === name ? '' : name;
    this.fieldFilter = null;
    this.onListFilterChanged();
  },
  applyListFilter(filter) {
    this.resetListFilters();
    if (!filter) {
      this.onListFilterChanged();
      return;
    }
    if (filter.field) {
      this.fieldFilter = {
        field: filter.field,
        values: [].concat(filter.values ?? filter.value ?? []).map(String),
        label: filter.label || '待办筛选'
      };
    }
    if (filter.status !== undefined && filter.status !== null) {
      this.statusFilterValue = String(filter.status);
    }
    if (filter.attention) {
      this.attentionFilter = filter.attention;
    }
    this.onListFilterChanged();
  },
  clearListFilters() {
    this.resetListFilters();
    this.onListFilterChanged();
  },
  toggleSort(field) {
    if (!field?.name) {
      return;
    }
    if (this.sortField === field.name) {
      if (this.sortDirection === 'asc') {
        this.sortDirection = 'desc';
      } else {
        this.sortField = '';
        this.sortDirection = 'asc';
      }
    } else {
      this.sortField = field.name;
      this.sortDirection = 'asc';
    }
    this.currentPage = 1;
  },
  sortAriaValue(field) {
    if (this.sortField !== field?.name) {
      return 'none';
    }
    return this.sortDirection === 'desc' ? 'descending' : 'ascending';
  },
  compareRecordValues(field, leftRecord, rightRecord) {
    const leftRaw = leftRecord?.[field.name];
    const rightRaw = rightRecord?.[field.name];
    const leftEmpty = leftRaw === null || leftRaw === undefined || leftRaw === '';
    const rightEmpty = rightRaw === null || rightRaw === undefined || rightRaw === '';
    if (leftEmpty || rightEmpty) {
      return leftEmpty === rightEmpty ? 0 : leftEmpty ? 1 : -1;
    }
    if (['Integer', 'Long', 'BigDecimal'].includes(field.type)) {
      return Number(leftRaw) - Number(rightRaw);
    }
    if (['LocalDate', 'LocalDateTime'].includes(field.type)) {
      const leftTime = new Date(leftRaw).getTime();
      const rightTime = new Date(rightRaw).getTime();
      if (!Number.isNaN(leftTime) && !Number.isNaN(rightTime)) {
        return leftTime - rightTime;
      }
    }
    const leftText = this.displayValue(field, leftRaw);
    const rightText = this.displayValue(field, rightRaw);
    return String(leftText).localeCompare(String(rightText), 'zh-CN', { numeric: true, sensitivity: 'base' });
  },
  onPageSizeChange() {
    const normalized = Number(this.pageSize);
    this.pageSize = PAGE_SIZE_OPTIONS.includes(normalized) ? normalized : 20;
    this.currentPage = 1;
  },
  isCopyableField(field, record) {
    const value = record?.[field?.name];
    return COPYABLE_FIELD_NAMES.has(field?.name)
      && value !== null
      && value !== undefined
      && value !== '';
  },
  async copyCellValue(field, record) {
    const text = String(record?.[field?.name] ?? '').trim();
    if (!text) {
      return;
    }
    try {
      if (!navigator.clipboard?.writeText) {
        throw new Error('clipboard unavailable');
      }
      await navigator.clipboard.writeText(text);
    } catch (error) {
      const input = document.createElement('textarea');
      input.value = text;
      input.setAttribute('readonly', '');
      input.style.position = 'fixed';
      input.style.opacity = '0';
      document.body.appendChild(input);
      input.select();
      document.execCommand('copy');
      document.body.removeChild(input);
    }
    this.showToast(`已复制${field.label}`);
  },
  operationLogStorageKey() {
    const userKey = this.authUser?.employeeNo || this.authUser?.id || 'anonymous';
    return `${OPERATION_LOG_KEY}:${userKey}`;
  },
  loadOperationLogs() {
    if (!this.authUser) {
      this.operationLogs = [];
      return;
    }
    try {
      const rows = JSON.parse(localStorage.getItem(this.operationLogStorageKey()) || '[]');
      this.operationLogs = Array.isArray(rows) ? rows.slice(0, OPERATION_LOG_LIMIT) : [];
    } catch (error) {
      this.operationLogs = [];
    }
  },
  persistOperationLogs() {
    if (!this.authUser) {
      return;
    }
    try {
      localStorage.setItem(this.operationLogStorageKey(), JSON.stringify(this.operationLogs.slice(0, OPERATION_LOG_LIMIT)));
    } catch (error) {
      // 日志只用于前端展示，存储失败不能影响业务操作。
    }
  },
  addOperationLog({ action, resourceName = this.currentResource?.name, record = null, recordId = null, summary = '', count = null, tone = '' }) {
    if (!this.authUser || !action) {
      return;
    }
    const resource = resourceMap[resourceName] || this.currentResource || {};
    const normalizedCount = Number(count || 0);
    const entry = {
      id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
      action,
      resourceName: resource.name || resourceName || '',
      resourceTitle: resource.title || '业务记录',
      recordId: recordId || record?.id || '',
      summary: summary || this.operationRecordSummary(resource.name || resourceName, record),
      count: normalizedCount > 0 ? normalizedCount : null,
      actor: this.currentUserName,
      time: new Date().toISOString(),
      tone: tone || this.operationTone(action)
    };
    this.operationLogs = [entry, ...this.operationLogs].slice(0, OPERATION_LOG_LIMIT);
    this.persistOperationLogs();
  },
  operationRecordSummary(resourceName, record) {
    if (!record) {
      return resourceMap[resourceName]?.title || '业务记录';
    }
    const fields = relationLabelFields[resourceName] || resourceMap[resourceName]?.visible || [];
    const text = fields
      .map((name) => record[name])
      .filter((value) => value !== null && value !== undefined && value !== '')
      .join(' / ');
    if (record.id) {
      return text ? `#${record.id} ${text}` : `#${record.id}`;
    }
    return text || resourceMap[resourceName]?.title || '业务记录';
  },
  operationTone(action) {
    if (action.includes('删除')) {
      return 'danger';
    }
    if (action.includes('新增') || action.includes('提交') || action.includes('完工') || action.includes('复检')) {
      return 'success';
    }
    if (action.includes('暂停') || action.includes('维修') || action.includes('返修') || action.includes('批量改')) {
      return 'warning';
    }
    if (action.includes('导出')) {
      return 'neutral';
    }
    return 'primary';
  },
  operationTimeText(value) {
    if (!value) {
      return '-';
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return String(value).replace('T', ' ').slice(5, 16);
    }
    const pad = (number) => String(number).padStart(2, '0');
    return `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
  },
  exportCurrentRecords() {
    if (!this.canExportRecords) {
      this.showToast(this.filteredRecords.length === 0 ? '当前没有可导出的记录' : '当前角色不能导出该模块');
      return;
    }
    this.downloadExcelRecords(this.filteredRecords);
  },
  batchExportSelectedRecords() {
    if (!this.canBatchExportRecords) {
      this.showToast(this.selectedRecordCount === 0 ? '请先勾选记录' : '当前角色不能导出该模块');
      return;
    }
    this.downloadExcelRecords(this.selectedRecords, '选中');
  },
  downloadExcelRecords(records, scopeLabel = '') {
    if (!Array.isArray(records) || records.length === 0) {
      this.showToast('当前没有可导出的记录');
      return;
    }
    const workbook = this.buildExcelWorkbook(records, scopeLabel);
    const blob = new Blob([workbook], { type: 'application/vnd.ms-excel;charset=utf-8;' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = this.exportFileName(scopeLabel);
    link.style.display = 'none';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    this.showToast(`已导出 ${records.length} 条记录`);
    this.addOperationLog({
      action: scopeLabel ? '导出选中' : '导出',
      count: records.length,
      summary: `${this.currentResource?.title || '业务记录'} ${records.length} 条`
    });
  },
  buildExcelWorkbook(records = this.filteredRecords, scopeLabel = '') {
    const title = [this.currentResource?.title || '业务记录', scopeLabel].filter(Boolean).join('-');
    const colGroup = this.exportFields
      .map((field) => `<col style="width:${this.exportColumnWidth(field)}px">`)
      .join('');
    const header = this.exportFields
      .map((field) => `<th>${this.escapeHtml(field.label)}</th>`)
      .join('');
    const body = records
      .map((record) => `<tr>${this.exportFields.map((field) => this.excelCell(field, record[field.name])).join('')}</tr>`)
      .join('');
    return `\uFEFF<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40">
<head>
  <meta charset="UTF-8">
  <style>
    table { border-collapse: collapse; font-family: "Microsoft YaHei", Arial, sans-serif; font-size: 11pt; }
    th { background: #edf2f7; font-weight: 700; }
    th, td { border: 1px solid #d9e0e8; padding: 6px 8px; white-space: nowrap; }
    .date-cell { mso-number-format:"yyyy\\/m\\/d\\ h\\:mm\\:ss"; }
    .day-cell { mso-number-format:"yyyy\\/m\\/d"; }
    .text-cell { mso-number-format:"\\@"; }
  </style>
</head>
<body>
  <table>
    <caption>${this.escapeHtml(title)}</caption>
    <colgroup>${colGroup}</colgroup>
    <thead><tr>${header}</tr></thead>
    <tbody>${body}</tbody>
  </table>
</body>
</html>`;
  },
  exportColumnWidth(field) {
    if (field.type === 'LocalDateTime') {
      return 180;
    }
    if (field.type === 'LocalDate') {
      return 120;
    }
    if (field.type === 'Text') {
      return 260;
    }
    if (field.relation) {
      return 190;
    }
    return 135;
  },
  excelCell(field, value) {
    const text = this.exportCellValue(field, value);
    const className = field.type === 'LocalDateTime'
      ? 'date-cell'
      : field.type === 'LocalDate'
        ? 'day-cell'
        : 'text-cell';
    return `<td class="${className}">${this.escapeHtml(this.safeSpreadsheetText(text))}</td>`;
  },
  exportCellValue(field, value) {
    if (value === null || value === undefined || value === '') {
      return '';
    }
    if (['LocalDate', 'LocalDateTime'].includes(field.type)) {
      return this.exportDateValue(field, value);
    }
    const text = this.displayValue(field, value);
    return text === '-' ? '' : text;
  },
  exportDateValue(field, value) {
    const text = String(value ?? '').trim().replace('T', ' ');
    if (!text) {
      return '';
    }
    const match = text.match(/^(\d{4})-(\d{2})-(\d{2})(?:\s+(\d{2}):(\d{2})(?::(\d{2}))?)?/);
    if (!match) {
      return text;
    }
    const [, year, month, day, hour = '00', minute = '00', second = '00'] = match;
    if (field.type === 'LocalDate') {
      return `${year}/${month}/${day}`;
    }
    return `${year}/${month}/${day} ${hour}:${minute}:${second}`;
  },
  safeSpreadsheetText(value) {
    const text = String(value ?? '').replace(/\r\n/g, '\n').replace(/\r/g, '\n');
    return /^[\s]*[=+\-@]/.test(text) ? `'${text}` : text;
  },
  escapeHtml(value) {
    return String(value ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  },
  exportFileName(scopeLabel = '') {
    const now = new Date();
    const pad = (value) => String(value).padStart(2, '0');
    const stamp = [
      now.getFullYear(),
      pad(now.getMonth() + 1),
      pad(now.getDate()),
      pad(now.getHours()),
      pad(now.getMinutes()),
      pad(now.getSeconds())
    ].join('');
    const title = [this.currentResource?.title || '业务记录', scopeLabel].filter(Boolean).join('-');
    return `Keyboard-MES-${title}-${stamp}.xls`;
  },
  async setDashboardPeriod(period) {
    if (!['day', 'week', 'month'].includes(period) || this.dashboardFilter.period === period) {
      return;
    }
    this.dashboardFilter.period = period;
    await this.loadDashboardReports();
  },
  async loadDashboardReports() {
    if (!this.authUser) {
      return;
    }
    this.loading.dashboard = true;
    try {
      const [overview, quality] = await Promise.all([
        client.reportOverview(),
        client.reportQuality(this.dashboardFilter)
      ]);
      this.reportOverview = overview || {};
      this.reportQuality = quality || {};
      this.setStatus('报表已更新', 'ok');
    } catch (error) {
      this.reportOverview = null;
      this.reportQuality = null;
      this.handleRequestError(error);
      this.showToast(error.message);
    } finally {
      this.loading.dashboard = false;
      this.$nextTick(() => {
        this.observeDashboardPanels();
        this.scheduleDashboardLayout();
      });
    }
  },
  initializeDashboardLayout() {
    if (typeof ResizeObserver === 'function') {
      this.dashboardResizeObserver = markRaw(new ResizeObserver(() => this.scheduleDashboardLayout()));
    }
    window.addEventListener('resize', this.scheduleDashboardLayout);
    this.$nextTick(() => {
      this.observeDashboardPanels();
      this.scheduleDashboardLayout();
    });
  },
  destroyDashboardLayout() {
    window.removeEventListener('resize', this.scheduleDashboardLayout);
    window.cancelAnimationFrame(this.dashboardLayoutFrame);
    this.dashboardResizeObserver?.disconnect();
    this.dashboardResizeObserver = null;
  },
  observeDashboardPanels() {
    if (!this.dashboardResizeObserver) {
      return;
    }
    this.dashboardResizeObserver.disconnect();
    this.$el?.querySelectorAll('.dashboard-panel').forEach((panel) => {
      this.dashboardResizeObserver.observe(panel);
    });
  },
  scheduleDashboardLayout() {
    window.cancelAnimationFrame(this.dashboardLayoutFrame);
    this.dashboardLayoutFrame = window.requestAnimationFrame(() => this.layoutDashboardPanels());
  },
  layoutDashboardPanels() {
    const grid = this.$el?.querySelector('.dashboard-grid');
    if (!grid) {
      return;
    }
    const styles = window.getComputedStyle(grid);
    const rowHeight = Number.parseFloat(styles.gridAutoRows) || 6;
    const rowGap = Number.parseFloat(styles.rowGap) || 0;
    grid.querySelectorAll('.dashboard-panel').forEach((panel) => {
      panel.style.gridRowEnd = 'auto';
      const panelStyles = window.getComputedStyle(panel);
      const contentHeight = panel.scrollHeight
        + (Number.parseFloat(panelStyles.borderTopWidth) || 0)
        + (Number.parseFloat(panelStyles.borderBottomWidth) || 0);
      const rowSpan = Math.max(1, Math.ceil((contentHeight + rowGap) / (rowHeight + rowGap)));
      panel.style.gridRowEnd = `span ${rowSpan}`;
    });
  },
  async loadRelationsForResource(force = false) {
    const relationNames = [...new Set(this.formFields.flatMap((field) => [field.relation, field.versionSource]).filter(Boolean))]
      .filter((name) => this.canReadResource(name));
    await Promise.all(relationNames.map((name) => this.loadRelation(name, force)));
  },
  async loadRelation(name, force = false) {
    if ((!force && this.relationCache[name]) || !resourceMap[name] || !this.authUser || !this.canReadResource(name)) {
      return;
    }
    try {
      const data = await client.list(resourceMap[name]);
      this.relationCache[name] = Array.isArray(data) ? data : [];
    } catch (error) {
      this.relationCache[name] = [];
    }
  },
  async refreshAfterMutation() {
    this.relationCache = {};
    await this.loadRelationsForResource(true);
    await this.loadRecords();
    this.clearBatchSelection();
    await this.loadTodayTodos();
  },
  async loadTodayTodos() {
    if (!this.authUser) {
      this.todoItems = [];
      return;
    }
    const names = ['productionOrder', 'productionTask', 'inspectionRecord', 'reworkOrder']
      .filter((name) => resourceMap[name] && this.canReadResource(name));
    if (names.length === 0) {
      this.todoItems = [];
      return;
    }
    this.loading.todos = true;
    try {
      const entries = await Promise.all(names.map(async (name) => {
        if (this.activeView === 'manage' && this.currentResource?.name === name) {
          return [name, this.records];
        }
        try {
          const data = await client.list(resourceMap[name]);
          return [name, Array.isArray(data) ? data : []];
        } catch (error) {
          if (error.code === 401) {
            throw error;
          }
          return [name, []];
        }
      }));
      this.todoItems = this.buildTodayTodos(Object.fromEntries(entries));
      this.todoLoadedAt = new Date().toISOString();
    } catch (error) {
      this.todoItems = [];
      this.handleRequestError(error);
    } finally {
      this.loading.todos = false;
    }
  },
  buildTodayTodos(source) {
    const items = [];
    const countBy = (records, fieldName, values) => {
      const expected = new Set([].concat(values).map(String));
      return (records || []).filter((record) => expected.has(String(record[fieldName]))).length;
    };
    const add = (resourceName, title, count, tone, desc, filter = null) => {
      if (!this.canOpenResource(resourceName) || !this.canReadResource(resourceName)) {
        return;
      }
      if (Number(count || 0) <= 0) {
        return;
      }
      items.push({
        key: `${resourceName}-${title}`,
        resourceName,
        title,
        count,
        tone,
        desc,
        filter: filter ? { ...filter, label: title } : null
      });
    };
    const orders = source.productionOrder || [];
    const tasks = source.productionTask || [];
    const inspections = source.inspectionRecord || [];
    const reworks = source.reworkOrder || [];

    add('productionOrder', '超期工单', orders.filter((record) => this.isResourceRecordOverdue('productionOrder', record)).length, 'danger', '交付或计划时间已超期', { attention: 'overdue' });
    add('productionOrder', '待配置工单', countBy(orders, 'status', 0), 'neutral', '需要完成版本、数量与产线配置', { status: 0 });
    add('productionOrder', '未齐套工单', orders.filter((record) => String(record.materialReadyStatus) === '0' && !['4', '5'].includes(String(record.status))).length, 'danger', '需要处理物料齐套', { field: 'materialReadyStatus', values: [0] });
    add('productionTask', '待开工任务', countBy(tasks, 'status', 0), 'neutral', '等待工位开工', { status: 0 });
    add('productionTask', '生产中任务', countBy(tasks, 'status', 1), 'primary', '正在执行的工序', { status: 1 });
    add('productionTask', '待质检任务', countBy(tasks, 'status', 4), 'warning', '需要质检确认', { status: 4 });
    add('productionTask', '异常任务', countBy(tasks, 'status', 5), 'danger', '需要处理异常', { status: 5 });
    add('inspectionRecord', '待审批检验', countBy(inspections, 'approvalStatus', 0), 'warning', '需要确认检验处置', { field: 'approvalStatus', values: [0] });
    add('reworkOrder', '超期返修', reworks.filter((record) => this.isResourceRecordOverdue('reworkOrder', record)).length, 'danger', '返修截止时间已超期', { attention: 'overdue' });
    add('reworkOrder', '待分派返修', countBy(reworks, 'status', 0), 'danger', '需要分派维修责任人', { status: 0 });
    add('reworkOrder', '返修处理中', countBy(reworks, 'status', 1), 'warning', '需要记录维修措施', { status: 1 });
    add('reworkOrder', '待复检返修', countBy(reworks, 'status', 2), 'warning', '需要提交复检结果', { status: 2 });
    return items.slice(0, 8);
  },
  async openTodo(todo) {
    if (!todo?.resourceName || !this.canOpenResource(todo.resourceName)) {
      return;
    }
    const filter = todo.filter ? { ...todo.filter, resourceName: todo.resourceName } : null;
    if (this.activeView === 'manage' && this.currentResource?.name === todo.resourceName) {
      this.applyListFilter(filter);
      return;
    }
    this.pendingListFilter = filter;
    await this.selectResource(todo.resourceName);
  },
  async batchUpdateStatus() {
    if (!this.canBatchUpdateStatus) {
      this.showToast(this.selectedRecordCount === 0 ? '请先勾选记录' : '当前角色不能批量修改状态');
      return;
    }
    const field = this.batchStatusField;
    const nextOption = this.batchStatusOptions.find((option) => String(option.value) === String(this.batchStatusValue));
    const nextLabel = nextOption?.label || this.batchStatusValue;
    const records = this.selectedRecords;
    const ok = window.confirm(`确认将 ${records.length} 条${this.currentResource.title}状态改为「${nextLabel}」？`);
    if (!ok) {
      return;
    }
    const nextValue = ['Integer', 'Long'].includes(field.type)
      ? Number.parseInt(this.batchStatusValue, 10)
      : this.batchStatusValue;
    this.loading.batch = true;
    try {
      for (const record of records) {
        await client.update(this.currentResource, record.id, { ...record, id: record.id, [field.name]: nextValue });
      }
      this.showToast(`已更新 ${records.length} 条记录`);
      this.addOperationLog({
        action: '批量改状态',
        count: records.length,
        summary: `${this.currentResource.title} 改为「${nextLabel}」`
      });
      this.clearSelection();
      await this.refreshAfterMutation();
    } catch (error) {
      this.handleRequestError(error);
      this.showToast(error.message);
    } finally {
      this.loading.batch = false;
    }
  },
  async batchDeleteRecords() {
    if (!this.canBatchDeleteRecords) {
      this.showToast(this.selectedRecordCount === 0 ? '请先勾选记录' : '当前角色不能删除该模块记录');
      return;
    }
    const records = this.selectedRecords;
    const ok = window.confirm(`确认删除 ${records.length} 条${this.currentResource.title}？`);
    if (!ok) {
      return;
    }
    this.loading.delete = true;
    try {
      for (const record of records) {
        await client.delete(this.currentResource, record.id);
      }
      this.showToast(`已删除 ${records.length} 条记录`);
      this.addOperationLog({
        action: '批量删除',
        count: records.length,
        summary: `${this.currentResource.title} ${records.length} 条`
      });
      this.clearSelection();
      await this.refreshAfterMutation();
    } catch (error) {
      this.handleRequestError(error);
      this.showToast(error.message);
    } finally {
      this.loading.delete = false;
    }
  },
  relationOptions(field) {
    const records = this.relationCache[field.relation] || [];
    const options = records.map((record) => ({ value: String(record.id), label: this.relationLabel(field.relation, record) }));
    if (field.relation === 'sysUser' && this.authUser?.id && !options.some((option) => option.value === String(this.authUser.id))) {
      options.unshift({ value: String(this.authUser.id), label: `#${this.authUser.id} ${this.authUser.employeeNo || ''} ${this.authUser.name || ''}`.trim() });
    }
    return options;
  },
  relationLabel(resourceName, record) {
    const fields = relationLabelFields[resourceName] || ['id'];
    const text = fields.map((name) => record[name]).filter((value) => value !== null && value !== undefined && value !== '').join(' / ');
    return text ? `#${record.id} ${text}` : `#${record.id}`;
  },
  dictionaryOptions(name) {
    const dict = dictionaries[name] || {};
    return Object.entries(dict).map(([value, label]) => ({ value, label }));
  },
  isVersionSelectField(field) {
    return Boolean(field.versionSource && field.versionField);
  },
  versionOptions(field) {
    // 工单版本必须来自当前产品型号已维护的 BOM/工艺路线，避免手写不存在的版本。
    const records = this.relationCache[field.versionSource] || [];
    const productModelId = this.recordDraft.productModelId;
    const values = records
      .filter((record) => !productModelId || String(record.productModelId) === String(productModelId))
      .map((record) => record[field.versionField])
      .filter((value) => value !== null && value !== undefined && value !== '');
    const currentValue = this.recordDraft[field.name];
    if (currentValue !== null && currentValue !== undefined && currentValue !== '') {
      values.push(currentValue);
    }
    return [...new Set(values.map(String))]
      .sort((left, right) => left.localeCompare(right, 'zh-CN', { numeric: true }))
      .map((value) => ({ value, label: value }));
  },
  selectRecord(record) {
    if (String(this.selectedRecord?.id ?? '') === String(record?.id ?? '')) {
      return;
    }
    if (!this.confirmDiscardChanges()) {
      return;
    }
    this.selectedRecord = record;
    this.recordDraft = this.createDraft(record);
    this.captureDraftBaseline();
    this.detailDialogOpen = true;
    if (this.currentResource?.name === 'productionOrder' && record?.id) {
      this.loadOrderTrackingReport(record.id);
      return;
    }
    this.orderReport = null;
  },
  clearSelection() {
    this.selectedRecord = null;
    this.recordDraft = this.createDraft({});
    this.captureDraftBaseline();
    this.orderReport = null;
    this.detailDialogOpen = false;
  },
  openCreateDialog() {
    if (!this.confirmDiscardChanges()) {
      return;
    }
    this.clearSelection();
    this.detailDialogOpen = true;
  },
  closeDetailDialog() {
    if (!this.confirmDiscardChanges()) {
      return;
    }
    this.clearSelection();
  },
  handleDialogKeydown(event) {
    if (event.key === 'Escape' && this.detailDialogOpen) {
      this.closeDetailDialog();
    }
  },
  requestClearSelection() {
    if (!this.confirmDiscardChanges()) {
      return;
    }
    this.clearSelection();
  },
  serializeDraft() {
    return JSON.stringify(this.formFields.map((field) => {
      const value = this.recordDraft?.[field.name];
      return [field.name, typeof value === 'string' ? value.trim() : value ?? null];
    }));
  },
  captureDraftBaseline() {
    this.draftBaseline = this.serializeDraft();
  },
  confirmDiscardChanges() {
    if (!this.hasUnsavedChanges) {
      return true;
    }
    const confirmed = window.confirm('当前单据有未保存的修改，确定放弃修改吗？');
    if (confirmed) {
      this.captureDraftBaseline();
    }
    return confirmed;
  },
  handleBeforeUnload(event) {
    if (!this.hasUnsavedChanges) {
      return;
    }
    event.preventDefault();
    event.returnValue = '';
  },
  async loadOrderTrackingReport(orderId) {
    if (!orderId) {
      this.orderReport = null;
      return;
    }
    this.loading.orderReport = true;
    try {
      this.orderReport = await client.orderTracking(orderId);
    } catch (error) {
      this.orderReport = null;
      this.handleRequestError(error);
      this.showToast(error.message);
    } finally {
      this.loading.orderReport = false;
    }
  },
  createDraft(record) {
    const draft = {};
    this.formFields.forEach((field) => {
      draft[field.name] = this.toInputValue(record[field.name], field);
    });
    this.applyDefaultDraft(draft, record);
    return draft;
  },
  applyDefaultDraft(draft, record) {
    if (record?.id || !this.currentResource) {
      return;
    }
    const userId = this.authUser?.id ? String(this.authUser.id) : '';
    if (this.currentResource.name === 'sysUser') {
      draft.passwordHash ||= '123456';
      draft.status ||= '1';
    }
    if (this.currentResource.name === 'workReport') {
      draft.reportNo ||= `WR-${Date.now()}`;
      draft.reportTime ||= this.nowInputValue();
      draft.operatorId ||= userId;
      draft.defectQuantity ||= '0';
      draft.status ||= '1';
    }
    if (this.currentResource.name === 'inspectionRecord') {
      draft.inspectionNo ||= `QC-${Date.now()}`;
      draft.inspectionType ||= 'process';
      draft.sourceType ||= 'manual';
      draft.inspectionTime ||= this.nowInputValue();
      draft.inspectorId ||= userId;
      draft.result ||= '1';
      draft.approvalStatus ||= '0';
    }
    if (this.currentResource.name === 'reworkOrder') {
      draft.assigneeId ||= userId;
      draft.status ||= '2';
      draft.repairTime ||= this.nowInputValue();
    }
  },
  async saveRecord() {
    if (!this.currentResource) {
      return;
    }
    if (this.selectedRecord?.id && !this.canUpdateRecord) {
      this.showToast('当前角色不能修改该模块');
      return;
    }
    if (!this.selectedRecord && !this.canCreateRecord && !this.canSubmitCurrentDraft) {
      this.showToast('当前角色不能新增该模块');
      return;
    }
    this.loading.save = true;
    try {
      if (this.selectedRecord?.id) {
        const payload = this.buildUpdatePayload();
        const logRecord = { ...this.selectedRecord, ...payload };
        await client.update(this.currentResource, this.selectedRecord.id, payload);
        this.showToast('更新成功');
        this.addOperationLog({ action: '更新', record: logRecord });
      } else if (this.canSubmitCurrentDraft) {
        const payload = this.readDraft();
        await this.submitBusinessRecord(payload);
        this.addOperationLog({
          action: this.currentResource.name === 'workReport' ? '提交报工' : '提交质检',
          record: payload
        });
      } else {
        const payload = this.readDraft();
        const created = await client.create(this.currentResource, payload);
        this.showToast('新增成功');
        this.addOperationLog({ action: '新增', record: created || payload });
      }
      this.clearSelection();
      await this.refreshAfterMutation();
    } catch (error) {
      this.handleRequestError(error);
      this.showToast(error.message);
    } finally {
      this.loading.save = false;
    }
  },
  async submitBusinessRecord(payload) {
    if (this.currentResource.name === 'workReport') {
      await client.submitWorkReport(payload);
      this.showToast('报工提交成功');
      return;
    }
    if (this.currentResource.name === 'inspectionRecord') {
      await client.submitInspection(payload);
      this.showToast('质检提交成功');
    }
  },
  async deleteRecord() {
    if (!this.currentResource || !this.selectedRecord?.id) {
      return;
    }
    if (!this.canDeleteRecord) {
      this.showToast('当前角色不能删除该模块记录');
      return;
    }
    const ok = window.confirm(`确认删除 ${this.currentResource.title} #${this.selectedRecord.id}？`);
    if (!ok) {
      return;
    }
    this.loading.delete = true;
    try {
      const deletedRecord = this.selectedRecord;
      await client.delete(this.currentResource, this.selectedRecord.id);
      this.showToast('删除成功');
      this.addOperationLog({ action: '删除', record: deletedRecord });
      this.clearSelection();
      await this.refreshAfterMutation();
    } catch (error) {
      this.handleRequestError(error);
      this.showToast(error.message);
    } finally {
      this.loading.delete = false;
    }
  },
  async runTaskAction(action) {
    if (!this.currentResource || this.currentResource.name !== 'productionTask' || !this.selectedRecord?.id) {
      this.showToast('请先选择生产任务');
      return;
    }
    if (!this.canRunTaskAction) {
      this.showToast('当前角色不能操作生产任务');
      return;
    }
    this.loading.taskAction = true;
    try {
      const taskRecord = this.selectedRecord;
      await client.taskAction(this.selectedRecord.id, action);
      this.showToast('任务状态已更新');
      const actionLabel = { start: '开工', pause: '暂停', finish: '完工' }[action] || '任务操作';
      this.addOperationLog({ action: actionLabel, resourceName: 'productionTask', record: taskRecord });
      await this.refreshAfterMutation();
    } catch (error) {
      this.handleRequestError(error);
      this.showToast(error.message);
    } finally {
      this.loading.taskAction = false;
    }
  },
  async generateTasksForOrder() {
    if (!this.canGenerateTasks) {
      this.showToast('请先选择可操作的生产工单');
      return;
    }
    this.loading.taskAction = true;
    try {
      const orderRecord = this.selectedRecord;
      const tasks = await client.generateTasks(this.selectedRecord.id);
      const count = Array.isArray(tasks) ? tasks.length : 0;
      this.showToast(count > 0 ? `已生成 ${count} 个生产任务` : '任务已生成');
      this.addOperationLog({
        action: '生成任务',
        resourceName: 'productionOrder',
        record: orderRecord,
        count,
        summary: count > 0 ? `${this.operationRecordSummary('productionOrder', orderRecord)} / ${count} 个任务` : this.operationRecordSummary('productionOrder', orderRecord)
      });
      await this.refreshAfterMutation();
      await this.loadOrderTrackingReport(this.selectedRecord.id);
    } catch (error) {
      this.handleRequestError(error);
      this.showToast(error.message);
    } finally {
      this.loading.taskAction = false;
    }
  },
  async repairCurrentRework() {
    if (!this.canRepairCurrentRework) {
      this.showToast('请先选择可处理的返修单');
      return;
    }
    this.loading.taskAction = true;
    try {
      const reworkRecord = this.selectedRecord;
      await client.repairRework(this.selectedRecord.id, this.readDraft());
      this.showToast('返修处理已保存');
      this.addOperationLog({ action: '记录维修', resourceName: 'reworkOrder', record: reworkRecord });
      await this.refreshAfterMutation();
    } catch (error) {
      this.handleRequestError(error);
      this.showToast(error.message);
    } finally {
      this.loading.taskAction = false;
    }
  },
  async recheckCurrentRework() {
    if (!this.canRecheckCurrentRework) {
      this.showToast('请先选择可复检的返修单');
      return;
    }
    const passed = window.confirm('复检结果是否合格？确定=合格，取消=不合格');
    const remark = window.prompt('请输入复检备注', passed ? '返修复检通过' : '返修复检未通过');
    this.loading.taskAction = true;
    try {
      const reworkRecord = this.selectedRecord;
      await client.recheckRework(this.selectedRecord.id, {
        inspectionNo: `RQ-${Date.now()}`,
        inspectorId: this.authUser?.id || null,
        sourceType: 'manual',
        inspectionTime: this.nowInputValue(),
        result: passed ? 1 : 0,
        measuredData: JSON.stringify({ pass: passed }),
        approvalStatus: 0,
        remark: remark || ''
      });
      this.showToast(passed ? '返修复检通过' : '复检未通过，返修单已退回');
      this.addOperationLog({
        action: passed ? '返修复检通过' : '返修复检未通过',
        resourceName: 'reworkOrder',
        record: reworkRecord,
        tone: passed ? 'success' : 'danger'
      });
      await this.refreshAfterMutation();
    } catch (error) {
      this.handleRequestError(error);
      this.showToast(error.message);
    } finally {
      this.loading.taskAction = false;
    }
  },
  async searchTrace() {
    if (!this.canUseWorkflow('trace')) {
      this.showToast('当前角色不能进入产品追溯');
      return;
    }
    const keyword = this.traceForm.productSn.trim();
    if (!keyword) {
      this.showToast('请输入产品 SN');
      return;
    }
    this.loading.trace = true;
    this.traceSearched = true;
    try {
      const data = await client.trace(keyword);
      this.traceGroups = this.normalizeTraceGroups(data);
      this.setStatus('追溯查询完成', 'ok');
    } catch (error) {
      this.traceGroups = [];
      this.handleRequestError(error);
      this.showToast(error.message);
    } finally {
      this.loading.trace = false;
    }
  },
  normalizeTraceGroups(data) {
    const safeData = data || {};
    return [
      { resourceName: 'productionOrder', title: '生产工单', items: safeData.orders || [], fields: this.traceFields('productionOrder', ['orderNo', 'quantity', 'lineName', 'plannedStartTime', 'status']) },
      { resourceName: 'productionTask', title: '生产任务', items: safeData.tasks || [], fields: this.traceFields('productionTask', ['taskNo', 'sequenceNo', 'stationCode', 'startTime', 'finishTime', 'status']) },
      { resourceName: 'workReport', title: '报工记录', items: safeData.reports || [], fields: this.traceFields('workReport', ['reportNo', 'reportQuantity', 'qualifiedQuantity', 'defectQuantity', 'reportTime']) },
      { resourceName: 'inspectionRecord', title: '检验记录', items: safeData.inspections || [], fields: this.traceFields('inspectionRecord', ['inspectionNo', 'inspectionType', 'result', 'inspectionTime']) },
      { resourceName: 'reworkOrder', title: '返修记录', items: safeData.reworks || [], fields: this.traceFields('reworkOrder', ['reworkNo', 'status', 'repairResult', 'repairTime']) }
    ];
  },
  traceRecordTimeFields(resourceName) {
    // 不同业务表的时间字段名称不同，统一映射后才能按完整生产过程排序。
    const fields = {
      productionOrder: ['plannedStartTime', 'createTime', 'plannedEndTime', 'deliveryDate'],
      productionTask: ['startTime', 'finishTime'],
      workReport: ['reportTime'],
      inspectionRecord: ['inspectionTime'],
      reworkOrder: ['repairTime', 'createTime', 'deadline']
    };
    return fields[resourceName] || ['createTime', 'updateTime'];
  },
  traceRecordRawTime(resourceName, item) {
    return this.traceRecordTimeFields(resourceName)
      .map((fieldName) => item?.[fieldName])
      .find((value) => value !== null && value !== undefined && value !== '');
  },
  traceRecordTimestamp(resourceName, item) {
    const value = this.traceRecordRawTime(resourceName, item);
    if (!value) {
      return Number.MAX_SAFE_INTEGER;
    }
    const parsed = new Date(String(value).replace(' ', 'T')).getTime();
    return Number.isNaN(parsed) ? Number.MAX_SAFE_INTEGER : parsed;
  },
  traceRecordTimeText(resourceName, item) {
    const value = this.traceRecordRawTime(resourceName, item);
    return value ? String(value).replace('T', ' ') : '无时间';
  },
  sortTraceItems(resourceName, items) {
    return [...(items || [])].sort((left, right) => (
      this.traceRecordTimestamp(resourceName, left) - this.traceRecordTimestamp(resourceName, right)
      || Number(left.id || 0) - Number(right.id || 0)
    ));
  },
  buildUpdatePayload() {
    const draft = this.readDraft();
    const payload = { ...(this.selectedRecord || {}) };

    // 后端 update 入口启用了实体校验，更新时需要保留只读/受限字段中的必填值。
    Object.entries(draft).forEach(([key, value]) => {
      payload[key] = value;
    });
    if (this.selectedRecord?.id) {
      payload.id = this.selectedRecord.id;
    }
    return payload;
  },
  readDraft() {
    const data = {};
    (this.currentResource?.fields || []).forEach((field) => {
      if (field.hidden || this.isReadonlyField(field)) {
        return;
      }
      if (field.sensitive && this.selectedRecord?.id) {
        return;
      }
      const rawValue = this.recordDraft[field.name];
      const value = typeof rawValue === 'string' ? rawValue.trim() : rawValue;
      if (value === '' || value === undefined) {
        data[field.name] = null;
        return;
      }
      if (['Integer', 'Long'].includes(field.type)) {
        data[field.name] = Number.parseInt(value, 10);
        return;
      }
      if (field.type === 'BigDecimal') {
        data[field.name] = Number.parseFloat(value);
        return;
      }
      data[field.name] = value;
    });
    if (this.selectedRecord?.id) {
      data.id = this.selectedRecord.id;
    }
    return data;
  },
  inputType(field) {
    if (field.type === 'Text') {
      return 'textarea';
    }
    if (field.type === 'LocalDate') {
      return 'date';
    }
    if (field.type === 'LocalDateTime') {
      return 'datetime-local';
    }
    if (['Integer', 'Long', 'BigDecimal'].includes(field.type)) {
      return 'number';
    }
    return 'text';
  },
  isReadonlyField(field) {
    return !this.canWriteField(field);
  },
  recordRowClass(record) {
    const tone = this.recordTone(record);
    return [
      {
        selected: this.selectedRecord && this.selectedRecord.id === record.id,
        checked: this.isRecordChecked(record),
        overdue: this.isRecordOverdue(record)
      },
      tone ? `row-${tone}` : ''
    ];
  },
  recordTone(record) {
    if (this.isRecordOverdue(record)) {
      return 'danger';
    }
    const tones = this.visibleFields
      .filter((field) => field.dict)
      .map((field) => this.statusTone(field, record[field.name]))
      .filter((tone) => tone && tone !== 'neutral');
    return ['danger', 'warning', 'primary', 'success'].find((tone) => tones.includes(tone)) || 'neutral';
  },
  recordBadges(record, field) {
    if (field?.name !== this.firstVisibleFieldName) {
      return [];
    }
    return this.isRecordOverdue(record) ? [{ label: '超期', className: 'overdue' }] : [];
  },
  isRecordOverdue(record) {
    return this.isResourceRecordOverdue(this.currentResource?.name, record);
  },
  isResourceRecordOverdue(resourceName, record) {
    if (!record) {
      return false;
    }
    if (resourceName === 'productionOrder') {
      if (['4', '5'].includes(String(record.status))) {
        return false;
      }
      return this.isDeadlineExpired(record.deliveryDate || record.plannedEndTime);
    }
    if (resourceName === 'reworkOrder') {
      if (['3', '4'].includes(String(record.status))) {
        return false;
      }
      return this.isDeadlineExpired(record.deadline);
    }
    return false;
  },
  isDeadlineExpired(value) {
    if (!value) {
      return false;
    }
    const text = String(value).trim().replace(' ', 'T');
    const dateOnly = text.match(/^(\d{4})-(\d{2})-(\d{2})$/);
    const deadline = dateOnly
      ? new Date(`${dateOnly[1]}-${dateOnly[2]}-${dateOnly[3]}T23:59:59`).getTime()
      : new Date(text).getTime();
    return Number.isFinite(deadline) && deadline < Date.now();
  },
  statusTagClass(field, value) {
    return ['status-tag', `status-${this.statusTone(field, value)}`];
  },
  statusTone(field, value) {
    if (value === null || value === undefined || value === '') {
      return 'neutral';
    }
    const key = String(value);
    const toneMaps = {
      enabledStatus: { 0: 'neutral', 1: 'success' },
      priority: { 0: 'neutral', 1: 'warning' },
      materialReady: { 0: 'danger', 1: 'success' },
      orderStatus: { 0: 'neutral', 1: 'primary', 2: 'primary', 3: 'warning', 4: 'success', 5: 'neutral' },
      taskStatus: { 0: 'neutral', 1: 'primary', 2: 'warning', 3: 'success', 4: 'warning', 5: 'danger' },
      reportStatus: { 0: 'neutral', 1: 'success', 2: 'warning' },
      inspectionResult: { 0: 'danger', 1: 'success', 2: 'warning', 3: 'danger' },
      approvalStatus: { 0: 'warning', 1: 'success', 2: 'danger' },
      reworkStatus: { 0: 'danger', 1: 'warning', 2: 'warning', 3: 'success', 4: 'neutral' },
      abnormalType: { 1: 'danger', 2: 'danger', 3: 'danger', 4: 'danger' },
      yesNo: { 0: 'neutral', 1: 'success' }
    };
    return toneMaps[field?.dict]?.[key] || 'neutral';
  },
  displayValue(field, value) {
    if (value === null || value === undefined || value === '') {
      return '-';
    }
    if (field.dict) {
      return dictionaries[field.dict]?.[value] || String(value);
    }
    if (field.relation) {
      const match = (this.relationCache[field.relation] || []).find((record) => String(record.id) === String(value));
      return match ? this.relationLabel(field.relation, match) : `#${value}`;
    }
    if (field.type === 'LocalDate') {
      return this.displayDateValue(field, value);
    }
    if (field.type === 'LocalDateTime') {
      return this.displayDateValue(field, value);
    }
    return String(value);
  },
  displayDateValue(field, value) {
    const text = String(value ?? '').trim().replace('T', ' ');
    if (!text) {
      return '-';
    }
    const match = text.match(/^(\d{4})-(\d{2})-(\d{2})(?:\s+(\d{2}):(\d{2})(?::(\d{2}))?)?/);
    if (!match) {
      return text;
    }
    const [, year, month, day, hour = '00', minute = '00', second] = match;
    if (field.type === 'LocalDate') {
      return `${year}-${month}-${day}`;
    }
    return `${year}-${month}-${day} ${hour}:${minute}${second ? `:${second}` : ''}`;
  },
  numberText(value) {
    if (value === null || value === undefined || value === '') {
      return '-';
    }
    const number = Number(value);
    return Number.isFinite(number) ? number.toLocaleString('zh-CN') : String(value);
  },
  percentText(value) {
    const number = Number(value || 0);
    return `${Number.isFinite(number) ? number.toFixed(1) : '0.0'}%`;
  },
  boundedPercent(value) {
    const number = Number(value || 0);
    return Number.isFinite(number) ? Math.max(0, Math.min(100, number)) : 0;
  },
  toTimeValue(value) {
    if (!value) {
      return Number.MAX_SAFE_INTEGER;
    }
    const parsed = new Date(String(value).replace(' ', 'T')).getTime();
    return Number.isNaN(parsed) ? Number.MAX_SAFE_INTEGER : parsed;
  },
  shortDateTime(value) {
    if (!value) {
      return '-';
    }
    const text = String(value).replace('T', ' ');
    return text.length > 16 ? text.slice(5, 16) : text;
  },
  traceFields(resourceName, names) {
    const fields = resourceMap[resourceName]?.fields || [];
    return names.map((name) => fields.find((field) => field.name === name) || { name, label: name });
  },
  traceValue(field, value) {
    if (!field?.name) {
      return value === null || value === undefined || value === '' ? '-' : String(value).replace('T', ' ');
    }
    return this.displayValue(field, value);
  },
  toInputValue(value, field = {}) {
    if (value === null || value === undefined) {
      return '';
    }
    if (field.type === 'LocalDateTime' && typeof value === 'string' && value.includes('T')) {
      return value.slice(0, 16);
    }
    return String(value);
  },
  nowInputValue() {
    const date = new Date();
    const pad = (value) => String(value).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
  },
  normalizeErrorMessage(error) {
    if (error?.code === 'NETWORK_ERROR') {
      return error.message;
    }
    return error?.message || '请求失败，请稍后重试';
  },
  handleRequestError(error) {
    const message = this.normalizeErrorMessage(error);
    error.message = message;
    if (error.code === 401) {
      this.authUser = null;
      this.setStatus('请先登录', 'warn');
      this.$router.replace({ name: 'login' });
      return;
    }
    if (error.code === 403) {
      this.setStatus(message || '当前角色不能执行该操作', 'warn');
      return;
    }
    this.setStatus(message, 'warn');
  },
  setStatus(text, type) {
    this.statusText = text;
    this.statusType = type || '';
  },
  showToast(message) {
    this.toastMessage = message;
    this.toastVisible = true;
    window.clearTimeout(this.toastTimer);
    this.toastTimer = window.setTimeout(() => {
      this.toastVisible = false;
    }, 2200);
  }
};

export const sharedOptions = {
  data() {
    return appState;
  },
  computed: sharedComputed,
  methods: sharedMethods
};
