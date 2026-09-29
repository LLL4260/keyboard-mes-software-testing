<template>
    <section class="app-shell">
      <aside class="sidebar">
        <div class="brand"><img class="brand-mark" src="/assets/keyboard-mark.svg" alt="Keyboard MES"><div><strong>键盘装配 MES</strong><span>生产制造执行系统</span></div></div>
        <div class="operator-card"><span>当前用户</span><strong>{{ currentUserName }}</strong><small>{{ currentUserRoleText }} · {{ currentUserDepartmentText }}</small></div>
        <section class="sidebar-todo-panel" aria-labelledby="sidebarTodoTitle">
          <div class="sidebar-todo-head">
            <div><h2 id="sidebarTodoTitle">今日待办</h2><span>{{ todoSummaryText }}</span></div>
            <button type="button" class="sidebar-icon-button" :class="{ spinning: loading.todos }" :disabled="loading.todos" aria-label="刷新待办" @click="loadTodayTodos">
              <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M21 12a9 9 0 0 1-15.3 6.4L4 16"/><path d="M3 12A9 9 0 0 1 18.3 5.6L20 8"/><path d="M4 16v4h4"/><path d="M20 8V4h-4"/></svg>
            </button>
          </div>
          <div v-if="visibleTodoItems.length" class="sidebar-todo-list">
            <button v-for="todo in visibleTodoItems" :key="todo.key" type="button" class="sidebar-todo-item" :class="todo.tone" @click="openTodo(todo)">
              <span>{{ todo.title }}</span><strong>{{ numberText(todo.count) }}</strong>
            </button>
          </div>
          <div v-else class="sidebar-todo-empty">{{ loading.todos ? '刷新中' : '暂无待办' }}</div>
        </section>
        <nav class="module-nav" aria-label="业务模块">
          <section v-for="group in visibleModuleGroups" :key="group.title" class="nav-group">
            <div class="nav-group-title">{{ group.title }}</div>
            <button v-for="resource in group.resources" :key="resource.name" type="button" class="module-button" :class="{ active: currentResource && currentResource.name === resource.name && activeView === 'manage' }" @click="selectResource(resource.name)">
              <span class="module-icon"><NavIcon :name="resource.name" /></span><span>{{ resource.title }}</span>
            </button>
          </section>
          <section class="nav-group">
            <div class="nav-group-title">分析看板</div>
            <button v-if="canUseWorkflow('dashboard')" type="button" class="module-button" :class="{ active: activeView === 'dashboard' }" @click="$router.push({ name: 'dashboard' })">
              <span class="module-icon"><NavIcon name="dashboard" /></span><span>可视化报表</span>
            </button>
            <button v-if="canUseWorkflow('trace')" type="button" class="module-button" :class="{ active: activeView === 'trace' }" @click="$router.push({ name: 'trace' })">
              <span class="module-icon"><NavIcon name="trace" /></span><span>产品追溯</span>
            </button>
            <button type="button" class="module-button" :class="{ active: activeView === 'operations' }" @click="$router.push({ name: 'operations' })">
              <span class="module-icon"><NavIcon name="operations" /></span>
              <span>操作日志</span>
            </button>
          </section>
        </nav>
      </aside>
      <main class="workspace">
        <header class="topbar">
          <section class="title-block"><p class="eyebrow">{{ viewEyebrow }}</p><h1>{{ viewTitle }}</h1><p>{{ viewDescription }}</p></section>
          <section class="runtime-strip" aria-label="生产现场状态"><div><span>业务日期</span><strong>{{ businessDate }}</strong></div><div><span>运行状态</span><strong>{{ statusText }}</strong></div><div><span>登录用户</span><strong>{{ currentUserName }}</strong></div></section>
        </header>
        <section class="command-bar" aria-live="polite"><div class="status-pill"><span :class="['status-dot', statusType]"></span><span>{{ viewDescription }}</span></div><div class="action-controls"><button type="button" @click="refreshCurrentView" :disabled="loading.records || loading.dashboard || activeView === 'trace'">{{ refreshActionLabel }}</button><button type="button" class="ghost-button" @click="logout">退出</button></div></section>
        <section v-if="activeView !== 'operations'" class="workflow-strip" aria-label="业务工作台"><button v-for="entry in visibleWorkflowEntries" :key="entry.key" type="button" class="workflow-card" :class="{ active: activeWorkflowKey === entry.key }" @click="openWorkflow(entry)"><span>{{ entry.title }}</span><strong>{{ entry.role }}</strong><small>{{ entry.text }}</small></button></section>
        <section v-if="activeView === 'trace'" class="panel trace-panel" aria-labelledby="traceTitle">
          <div class="panel-header"><div><h2 id="traceTitle">产品追溯</h2><span>按产品 SN 查询报工、质检、返修全过程</span></div></div>
          <form class="trace-search" @submit.prevent="searchTrace"><input v-model.trim="traceForm.productSn" type="search" placeholder="输入产品 SN，例如 SN-ALPHA87-0715-001"><button type="submit" class="primary-action" :disabled="loading.trace">{{ loading.trace ? '查询中' : '查询' }}</button></form>
          <div class="trace-mode-switch" role="group" aria-label="追溯排序方式"><button type="button" :class="{ active: traceSortMode === 'business' }" @click="traceSortMode = 'business'">按业务分组</button><button type="button" :class="{ active: traceSortMode === 'timeline' }" @click="traceSortMode = 'timeline'">按时间线</button></div>
          <div v-if="traceIsEmpty" class="trace-empty">未找到相关追溯记录</div>
          <div v-else-if="traceSortMode === 'business'" class="trace-groups"><section v-for="group in traceDisplayGroups" :key="group.title" class="trace-group"><h3>{{ group.title }} <span>{{ group.items.length }}</span></h3><div v-if="group.items.length === 0" class="trace-empty-line">暂无记录</div><article v-for="item in group.items" :key="group.title + '-' + item.id" class="trace-record"><strong>记录ID {{ item.id }}</strong><dl><template v-for="field in group.fields" :key="field.name"><dt>{{ field.label }}</dt><dd>{{ traceValue(field, item[field.name]) }}</dd></template></dl></article></section></div>
          <div v-else class="trace-timeline"><article v-for="(entry, index) in traceTimelineItems" :key="entry.key" class="trace-timeline-item"><div class="timeline-marker">{{ index + 1 }}</div><div class="timeline-card"><header><strong>{{ entry.groupTitle }}</strong><span>{{ entry.timeText }}</span></header><small>记录ID {{ entry.item.id }}</small><dl><template v-for="field in entry.fields" :key="field.name"><dt>{{ field.label }}</dt><dd>{{ traceValue(field, entry.item[field.name]) }}</dd></template></dl></div></article></div>
        </section>
        <section v-else-if="activeView === 'dashboard'" class="report-dashboard" aria-labelledby="dashboardTitle">
          <section class="dashboard-toolbar">
            <div class="dashboard-filter-copy">
              <p>生产分析</p>
              <h2 id="dashboardTitle">{{ dashboardSelectedProductLabel }}</h2>
              <span>{{ dashboardDateRange }}</span>
            </div>
            <div class="dashboard-filter-controls">
              <div class="period-switch" role="group" aria-label="统计周期">
                <button type="button" :class="{ active: dashboardFilter.period === 'day' }" @click="setDashboardPeriod('day')">本日</button>
                <button type="button" :class="{ active: dashboardFilter.period === 'week' }" @click="setDashboardPeriod('week')">本周</button>
                <button type="button" :class="{ active: dashboardFilter.period === 'month' }" @click="setDashboardPeriod('month')">本月</button>
              </div>
              <label class="model-select">
                <span>产品型号</span>
                <select v-model="dashboardFilter.productModelId" @change="loadDashboardReports">
                  <option value="">全部产品</option>
                  <option v-for="model in dashboardProductModels" :key="model.id" :value="String(model.id)">{{ model.label }}</option>
                </select>
              </label>
              <button type="button" class="ghost-button dashboard-refresh" :disabled="loading.dashboard" @click="loadDashboardReports">{{ loading.dashboard ? '加载中' : '刷新报表' }}</button>
            </div>
          </section>

          <section class="dashboard-metrics" aria-label="生产报表指标">
            <article v-for="card in dashboardCards" :key="card.label" class="dashboard-metric" :class="card.tone">
              <span>{{ card.label }}</span>
              <strong>{{ card.value }}</strong>
              <small>{{ card.note }}</small>
            </article>
          </section>

          <section class="dashboard-grid">
            <section class="panel dashboard-panel dashboard-trend-panel">
              <div class="panel-header"><div><h2>{{ dashboardPeriodLabel }}产量趋势</h2><span>{{ dashboardSelectedProductLabel }} · {{ dashboardFilter.period === 'day' ? '当日报工产量' : `按${dashboardFilter.period === 'month' ? '周' : '日'}汇总报工产量` }}</span></div><strong class="panel-total">{{ numberText(reportQuality?.totalQuantity) }} 把</strong></div>
              <div v-if="trendBars.length" class="trend-bars"><article v-for="bar in trendBars" :key="bar.key" class="trend-bar"><div class="trend-bar-track"><span :style="{ height: bar.height }"></span></div><strong>{{ numberText(bar.value) }}</strong><small>{{ bar.label }}</small></article></div><div v-else class="tracking-empty compact">暂无趋势数据</div>
            </section>

            <section class="panel dashboard-panel dashboard-model-panel">
              <div class="panel-header"><div><h2>型号产量构成</h2><span>{{ dashboardFilter.productModelId ? '当前选择的单一型号' : '各产品型号产量与占比' }}</span></div></div>
              <div class="bar-list model-bar-list"><article v-for="item in productModelBars" :key="item.key" class="bar-row model"><header><div><strong>{{ item.label }}</strong><small>{{ item.category }}</small></div><span>{{ numberText(item.value) }} / {{ item.percent }}</span></header><div class="bar-track"><span :style="{ width: item.width }"></span></div></article><div v-if="productModelBars.length === 0" class="tracking-empty compact">当前周期暂无型号产量</div></div>
            </section>

            <section class="panel dashboard-panel dashboard-defect-panel">
              <div class="panel-header"><div><h2>缺陷类型排行</h2><span>当前周期与产品范围内的不良原因</span></div></div>
              <div class="bar-list"><article v-for="item in defectTypeBars" :key="item.key" class="bar-row defect"><header><strong>{{ item.label }}</strong><span>{{ numberText(item.value) }} / {{ item.percent }}</span></header><div class="bar-track"><span :style="{ width: item.width }"></span></div></article><div v-if="defectTypeBars.length === 0" class="tracking-empty compact">当前范围暂无不良记录</div></div>
            </section>

            <section class="panel dashboard-panel dashboard-quality-panel">
              <div class="panel-header"><div><h2>当前筛选状态</h2><span>{{ dashboardPeriodLabel }} · {{ dashboardSelectedProductLabel }}</span></div></div>
              <div class="dashboard-donuts"><article class="progress-donut-block"><div class="donut-chart quality-donut" :style="dashboardQualityStyle"><div><strong>{{ percentText(dashboardQualityRate) }}</strong><span>合格率</span></div></div><small>{{ dashboardPeriodLabel }}合格 / 总产量</small></article></div><div class="quality-summary"><header><strong>质量构成</strong><span>{{ percentText(dashboardQualityRate) }}</span></header><div class="quality-stack"><span v-for="segment in dashboardQualitySegments" :key="segment.label" :class="segment.className" :style="{ width: segment.width }"></span></div><div class="quality-legend"><span v-for="segment in dashboardQualitySegments" :key="'dashboard-' + segment.label"><i :class="segment.className"></i>{{ segment.label }} {{ numberText(segment.value) }}</span></div></div>
            </section>

            <section class="panel dashboard-panel dashboard-workstation-panel">
              <div class="panel-header"><div><h2>工位产出</h2><span>全局实时工位报工统计</span></div></div>
              <div class="bar-list"><article v-for="item in workstationBars" :key="item.key" class="bar-row"><header><strong>{{ item.label }}</strong><span>{{ numberText(item.value) }} / 不良 {{ numberText(item.defect) }}</span></header><div class="bar-track"><span :style="{ width: item.width }"></span></div></article><div v-if="workstationBars.length === 0" class="tracking-empty compact">暂无工位数据</div></div>
            </section>
          </section>
        </section>
        <section v-else-if="activeView === 'operations'" class="operation-log-page" aria-labelledby="operationLogTitle">
          <section class="operation-log-toolbar">
            <div>
              <p>操作记录</p>
              <h2 id="operationLogTitle">最近操作日志</h2>
              <span>当前账号最近 20 条业务操作</span>
            </div>
            <button type="button" class="ghost-button" @click="loadOperationLogs">刷新</button>
          </section>
          <section v-if="recentOperationLogs.length" class="operation-log-list" aria-label="最近操作列表">
            <article v-for="entry in recentOperationLogs" :key="entry.id" class="operation-log-item" :class="entry.tone">
              <div class="operation-log-time"><strong>{{ operationTimeText(entry.time) }}</strong><span>{{ entry.actor }}</span></div>
              <div class="operation-log-main">
                <header><strong>{{ entry.action }}</strong><span>{{ entry.resourceTitle }}<em v-if="entry.count"> {{ entry.count }} 条</em></span></header>
                <p>{{ entry.summary }}</p>
              </div>
            </article>
          </section>
          <section v-else class="operation-log-empty">暂无操作记录</section>
        </section>
        <section v-else>
          <section class="overview-strip" aria-label="当前模块概览"><div class="overview-item"><span>当前记录</span><strong>{{ filteredRecords.length }}</strong></div><div class="overview-item"><span>填报项</span><strong>{{ formFields.length }}</strong></div><div class="overview-item"><span>必填项</span><strong>{{ requiredFieldCount }}</strong></div><div class="overview-item overview-summary"><span>选中单据</span><strong>{{ selectedSummary }}</strong></div></section>
          <section class="content-grid">
            <section class="panel table-panel" aria-labelledby="tableTitle">
              <div class="panel-header">
                <div><h2 id="tableTitle">业务列表</h2><span>{{ currentResource ? currentResource.title : '-' }}</span></div>
                <div class="table-actions" :class="{ 'has-product-filter': supportsProductModelFilter }">
                  <label v-if="supportsProductModelFilter" class="product-model-filter">
                    <select v-model="productModelFilter" aria-label="产品型号" @change="currentPage = 1">
                      <option value="">全部产品型号</option>
                      <option v-for="option in productModelFilterOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
                    </select>
                  </label>
                  <input v-model.trim="searchKeyword" @input="currentPage = 1" type="search" placeholder="搜索当前列表">
                  <button type="button" class="ghost-button" :disabled="!canExportRecords" @click="exportCurrentRecords">导出</button>
                  <button type="button" class="secondary-button" :disabled="!canCreateRecord" @click="openCreateDialog">新增</button>
                </div>
              </div>
              <div v-if="showQuickFilterBar" class="quick-filter-bar">
                <div v-if="statusFilterOptions.length" class="quick-filter-group" role="group" :aria-label="statusFilterField.label">
                  <span class="quick-filter-label">{{ statusFilterField.label }}</span>
                  <button type="button" class="quick-filter-button" :class="{ active: !statusFilterValue && !fieldFilter }" @click="setStatusFilter('')">全部</button>
                  <button v-for="option in statusFilterOptions" :key="option.value" type="button" class="quick-filter-button" :class="{ active: String(statusFilterValue) === String(option.value) && !fieldFilter }" @click="setStatusFilter(String(option.value))">{{ option.label }}</button>
                </div>
                <button v-if="fieldFilter" type="button" class="quick-filter-button active" @click="clearListFilters">{{ fieldFilter.label }}</button>
                <button v-if="canUseMineFilter" type="button" class="quick-filter-button" :class="{ active: mineOnly }" @click="toggleMineFilter">{{ mineFilterLabel }}</button>
                <button v-if="canUseOverdueFilter" type="button" class="quick-filter-button danger" :class="{ active: attentionFilter === 'overdue' }" @click="toggleAttentionFilter('overdue')">超期 {{ overdueRecordCount }}</button>
                <button v-if="hasActiveListFilter" type="button" class="quick-filter-clear" @click="clearListFilters">清除</button>
              </div>
              <div v-if="selectedRecordCount" class="batch-bar">
                <strong>已选 {{ selectedRecordCount }} 条</strong>
                <label v-if="batchStatusOptions.length" class="batch-status-select">
                  <span>状态</span>
                  <select v-model="batchStatusValue">
                    <option value="">选择状态</option>
                    <option v-for="option in batchStatusOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
                  </select>
                </label>
                <button type="button" class="secondary-button" :disabled="!canBatchUpdateStatus" @click="batchUpdateStatus">{{ loading.batch ? '更新中' : '批量改状态' }}</button>
                <button type="button" class="ghost-button" :disabled="!canBatchExportRecords" @click="batchExportSelectedRecords">导出选中</button>
                <button v-if="canDeleteRecord" type="button" class="danger" :disabled="!canBatchDeleteRecords" @click="batchDeleteRecords">{{ loading.delete ? '删除中' : '批量删除' }}</button>
                <button type="button" class="ghost-button" @click="clearBatchSelection">取消选择</button>
              </div>
              <div class="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th class="select-column">
                        <input type="checkbox" :checked="allVisibleRecordsSelected" :disabled="paginatedRecords.length === 0" aria-label="选择当前页" @change="toggleCurrentPageSelection">
                      </th>
                      <th v-for="field in visibleFields" :key="field.name" :aria-sort="sortAriaValue(field)">
                        <button type="button" class="sort-header" :class="{ active: sortField === field.name }" :aria-label="`按${field.label}排序`" @click="toggleSort(field)">
                          <span>{{ field.label }}</span>
                          <svg viewBox="0 0 20 20" aria-hidden="true">
                            <path class="sort-up" d="m6 8 4-4 4 4" />
                            <path class="sort-down" d="m6 12 4 4 4-4" />
                          </svg>
                        </button>
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    <template v-if="loading.records">
                      <tr v-for="row in 5" :key="'loading-' + row" class="skeleton-row">
                        <td class="select-column"><span class="skeleton-line tiny"></span></td>
                        <td v-for="field in visibleFields" :key="field.name"><span class="skeleton-line"></span></td>
                      </tr>
                    </template>
                    <tr v-else-if="filteredRecords.length === 0" class="empty-row"><td :colspan="tableColspan">暂无数据</td></tr>
                    <tr v-for="record in paginatedRecords" :key="record.id || JSON.stringify(record)" :class="recordRowClass(record)" @click="selectRecord(record)">
                      <td class="select-column" @click.stop>
                        <input type="checkbox" :checked="isRecordChecked(record)" :aria-label="`选择记录 ${record.id || ''}`" @change="toggleRecordSelection(record)">
                      </td>
                      <td v-for="field in visibleFields" :key="field.name" :title="displayValue(field, record[field.name])">
                        <span v-if="field.dict" :class="statusTagClass(field, record[field.name])">{{ displayValue(field, record[field.name]) }}</span>
                        <span v-else class="cell-value">
                          <span>{{ displayValue(field, record[field.name]) }}</span>
                          <button v-if="isCopyableField(field, record)" type="button" class="cell-copy-button" :title="`复制${field.label}`" :aria-label="`复制${field.label}`" @click.stop="copyCellValue(field, record)">
                            <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="9" y="9" width="11" height="11" rx="2"/><path d="M15 9V6a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h3"/></svg>
                          </button>
                        </span>
                        <span v-for="badge in recordBadges(record, field)" :key="badge.label" class="record-badge" :class="badge.className">{{ badge.label }}</span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div v-if="!loading.records && filteredRecords.length" class="pagination-bar"><span>{{ paginationLabel }}</span><label class="page-size-control"><span>每页</span><select v-model.number="pageSize" aria-label="每页显示条数" @change="onPageSizeChange"><option v-for="size in pageSizeOptions" :key="size" :value="size">{{ size }} 条</option></select></label><div class="pagination-actions"><button type="button" class="page-button" :disabled="!canPreviousPage" @click="previousPage">上一页</button><strong>第 {{ Math.min(currentPage, totalPages) }} / {{ totalPages }} 页</strong><button type="button" class="page-button" :disabled="!canNextPage" @click="nextPage">下一页</button></div></div>
            </section>
            
            <div v-if="detailDialogOpen" class="detail-dialog-backdrop" role="presentation" @click.self="closeDetailDialog">
              <section class="panel form-panel detail-dialog" role="dialog" aria-modal="true" aria-labelledby="formTitle"><div class="panel-header"><div><h2 id="formTitle">{{ selectedRecord ? '单据维护' : '新增单据' }}</h2><span>{{ selectedSummary }}</span></div><button type="button" class="dialog-close-button" aria-label="关闭详情" title="关闭" @click="closeDetailDialog">×</button></div>
<div v-if="currentResource && currentResource.name === 'productionOrder'" class="task-actions single-action"><button type="button" class="secondary-button" :disabled="!canGenerateTasks || loading.taskAction" @click="generateTasksForOrder">{{ loading.taskAction ? '生成中' : '生成任务' }}</button></div><div v-if="showOrderTrackingReport" class="order-tracking"><div class="tracking-header"><div><strong>工单数据统计</strong><span>{{ loading.orderReport ? '正在刷新工单进度' : '完成数、合格数与待办提醒' }}</span></div><button type="button" class="ghost-button" :disabled="loading.orderReport" @click="loadOrderTrackingReport(selectedRecord.id)">刷新</button></div><div v-if="loading.orderReport" class="tracking-loading"><span class="skeleton-line"></span><span class="skeleton-line"></span><span class="skeleton-line"></span></div><template v-else-if="orderReport"><div class="tracking-cards"><div v-for="card in orderReportCards" :key="card.label" class="tracking-card"><span>{{ card.label }}</span><strong>{{ card.value }}</strong></div></div><div class="tracking-progress"><div class="progress-track"><span :style="{ width: orderReportProgressWidth }"></span></div><small>成品完成进度按最后一道工序计算，避免多工序重复计数。</small></div><div class="todo-list"><article v-for="todo in orderReportTodos" :key="todo.title + todo.recordId" class="todo-item" :class="todo.level"><span>{{ todo.title }}</span><p>{{ todo.message }}</p></article></div><div class="task-progress-list"><article v-for="task in orderReportTasks" :key="task.id" class="task-progress-row"><header><strong>{{ task.taskNo }}</strong><span>{{ task.statusText }}</span></header><div class="task-progress-meta"><span>工序 {{ task.sequenceNo }}</span><span>{{ task.completedQuantity }}/{{ task.plannedQuantity }}</span><span>不良 {{ task.defectQuantity }}</span></div><div class="progress-track slim"><span :style="{ width: Math.max(0, Math.min(100, Number(task.progressRate || 0))) + '%' }"></span></div></article><div v-if="orderReportTasks.length === 0" class="tracking-empty">暂无生产任务，请先生成任务。</div></div></template><div v-else class="tracking-empty">选择工单后查看工单数据统计。</div></div><div v-if="currentResource && currentResource.name === 'productionTask'" class="task-actions"><button type="button" class="secondary-button" :disabled="!canRunTaskAction || loading.taskAction" @click="runTaskAction('start')">{{ loading.taskAction ? '处理中' : '开工' }}</button><button type="button" class="secondary-button" :disabled="!canRunTaskAction || loading.taskAction" @click="runTaskAction('pause')">{{ loading.taskAction ? '处理中' : '暂停' }}</button><button type="button" class="secondary-button" :disabled="!canRunTaskAction || loading.taskAction" @click="runTaskAction('finish')">{{ loading.taskAction ? '处理中' : '完工' }}</button></div><div v-if="currentResource && currentResource.name === 'reworkOrder'" class="task-actions rework-actions"><button type="button" class="secondary-button" :disabled="!canRepairCurrentRework || loading.taskAction" @click="repairCurrentRework">{{ loading.taskAction ? '保存中' : '记录维修' }}</button><button type="button" class="secondary-button" :disabled="!canRecheckCurrentRework || loading.taskAction" @click="recheckCurrentRework">{{ loading.taskAction ? '提交中' : '返修复检' }}</button></div><form class="record-form" @submit.prevent="saveRecord"><div v-for="field in formFields" :key="field.name" class="field" :class="{ readonly: isReadonlyField(field) }"><label :for="'field-' + field.name"><span>{{ field.label }}</span><small>{{ field.required ? '必填' : '可选' }}</small></label><select v-if="field.relation" :id="'field-' + field.name" v-model="recordDraft[field.name]" :required="field.required" :disabled="isReadonlyField(field)"><option value="">请选择{{ field.label }}</option><option v-for="option in relationOptions(field)" :key="option.value" :value="option.value">{{ option.label }}</option></select><select v-else-if="isVersionSelectField(field)" :id="'field-' + field.name" v-model="recordDraft[field.name]" :required="field.required" :disabled="isReadonlyField(field)"><option value="">请选择{{ field.label }}</option><option v-for="option in versionOptions(field)" :key="option.value" :value="option.value">{{ option.label }}</option></select><select v-else-if="field.dict" :id="'field-' + field.name" v-model="recordDraft[field.name]" :required="field.required" :disabled="isReadonlyField(field)"><option value="">请选择{{ field.label }}</option><option v-for="option in dictionaryOptions(field.dict)" :key="option.value" :value="option.value">{{ option.label }}</option></select><textarea v-else-if="inputType(field) === 'textarea'" :id="'field-' + field.name" v-model="recordDraft[field.name]" :required="field.required && !isReadonlyField(field)" :disabled="isReadonlyField(field)"></textarea><input v-else :id="'field-' + field.name" v-model="recordDraft[field.name]" :type="inputType(field)" :required="field.required && !isReadonlyField(field)" :disabled="isReadonlyField(field)"><div v-if="field.hint" class="hint">{{ field.hint }}</div></div></form><div class="form-actions"><button type="button" class="primary-action" @click="saveRecord" :disabled="loading.save || saveDisabled">{{ primaryActionLabel }}</button><button type="button" class="danger" :disabled="!selectedRecord || !selectedRecord.id || loading.delete || !canDeleteRecord" @click="deleteRecord">{{ deleteActionLabel }}</button></div></section>
            </div>
          </section>
        </section>
      </main>
      <div class="toast" :class="{ show: toastVisible }" role="status" aria-live="polite">{{ toastMessage }}</div>
    </section>
  
</template>

<script>
import { sharedOptions } from '../sharedOptions.js';
import NavIcon from '../components/NavIcon.vue';

export default {
  ...sharedOptions,
  components: { NavIcon },
  async mounted() {
    this.initializeDashboardLayout();
    window.addEventListener('beforeunload', this.handleBeforeUnload);
    window.addEventListener('keydown', this.handleDialogKeydown);
    await this.syncRoute();
  },
  beforeUnmount() {
    window.removeEventListener('beforeunload', this.handleBeforeUnload);
    window.removeEventListener('keydown', this.handleDialogKeydown);
    this.destroyDashboardLayout();
  },
  beforeRouteUpdate(to, from, next) {
    if (this.confirmDiscardChanges()) {
      next();
    } else {
      next(false);
    }
  },
  beforeRouteLeave(to, from, next) {
    if (this.confirmDiscardChanges()) {
      next();
    } else {
      next(false);
    }
  },
  watch: {
    '$route.fullPath'() {
      this.syncRoute();
    }
  }
};
</script>
