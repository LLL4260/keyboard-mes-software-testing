# API 接口文档

## 1. 通用约定

| 项 | 契约 |
| --- | --- |
| Base URL | `http://localhost:8088/api` |
| 请求体 | `application/json` |
| 字段命名 | lowerCamelCase |
| 日期 | `yyyy-MM-dd` |
| 日期时间 | `yyyy-MM-ddTHH:mm:ss` |
| 成功判断 | `code === 200` |

统一响应：

```json
{
  "code": 200,
  "msg": "成功",
  "data": {}
}
```

| code | 含义 |
| --- | --- |
| `200` | 成功 |
| `400` | 参数或业务错误 |
| `401` | 未登录 |
| `403` | 无权限 |
| `500` | 系统或持久化失败 |

## 2. 认证接口

| 功能 | 方法 | 路径 | 请求体 |
| --- | --- | --- | --- |
| 登录 | `POST` | `/auth/login` | `{ "employeeNo": "U001", "password": "123456" }` |
| 当前用户 | `GET` | `/auth/current` | 无 |
| 退出 | `POST` | `/auth/logout` | 无 |

## 3. CRUD 模板

| 功能 | 方法 | 路径 |
| --- | --- | --- |
| 列表 | `GET` | `/{resource}/list` |
| 新增 | `POST` | `/{resource}/add` |
| 详情 | `GET` | `/{resource}/{id}` |
| 更新 | `PUT` | `/{resource}/{id}` |
| 兼容更新 | `POST` | `/{resource}/update` |
| 删除 | `DELETE` | `/{resource}/{id}` |

新增不传 `id`、`createTime`、`updateTime`。更新优先传完整对象。

## 4. 资源契约

| resource | 表 | 字段 |
| --- | --- | --- |
| `sysUser` | `sys_user` | `id`, `employeeNo`, `passwordHash`, `name`, `roleCode`, `roleName`, `department`, `position`, `phone`, `skillLevel`, `status`, `createTime`, `updateTime` |
| `productModel` | `product_model` | `id`, `modelCode`, `modelName`, `category`, `configurationDesc`, `firmwareVersion`, `status`, `createTime`, `updateTime`, `remark` |
| `material` | `material` | `id`, `materialCode`, `materialName`, `specification`, `materialType`, `unit`, `supplier`, `barcode`, `status`, `createTime`, `updateTime` |
| `productBom` | `product_bom` | `id`, `productModelId`, `bomVersion`, `materialId`, `quantity`, `unit`, `sequenceNo`, `effectiveDate`, `expireDate`, `status`, `remark` |
| `processRoute` | `process_route` | `id`, `productModelId`, `routeVersion`, `sequenceNo`, `processCode`, `processName`, `processType`, `stationCode`, `stationName`, `standardHours`, `qualityGate`, `inspectionType`, `inspectionConfig`, `sopFile`, `status` |
| `productionOrder` | `production_order` | `id`, `orderNo`, `productModelId`, `bomVersion`, `routeVersion`, `batchNo`, `quantity`, `plannedStartTime`, `plannedEndTime`, `deliveryDate`, `lineName`, `source`, `priority`, `plannerId`, `materialReadyStatus`, `status`, `createTime`, `updateTime` |
| `productionTask` | `production_task` | `id`, `taskNo`, `orderId`, `processRouteId`, `sequenceNo`, `stationCode`, `assignedUserId`, `plannedQuantity`, `completedQuantity`, `defectQuantity`, `abnormalType`, `abnormalDesc`, `abnormalSolution`, `status`, `startTime`, `finishTime`, `remark` |
| `workReport` | `work_report` | `id`, `reportNo`, `taskId`, `orderId`, `productSn`, `operatorId`, `stationCode`, `reportQuantity`, `qualifiedQuantity`, `defectQuantity`, `actualHours`, `defectReason`, `reportTime`, `status` |
| `inspectionRecord` | `inspection_record` | `id`, `inspectionNo`, `inspectionType`, `taskId`, `orderId`, `productSn`, `processRouteId`, `standardSnapshot`, `inspectorId`, `sourceType`, `equipmentNo`, `inspectionTime`, `result`, `measuredData`, `defectReason`, `handlingMethod`, `approvalStatus`, `remark` |
| `reworkOrder` | `rework_order` | `id`, `reworkNo`, `sourceInspectionId`, `orderId`, `taskId`, `productSn`, `defectReason`, `reworkRequirement`, `assigneeId`, `deadline`, `status`, `repairAction`, `replacedMaterial`, `repairHours`, `repairResult`, `repairTime`, `recheckInspectionId`, `createTime` |

## 5. 元信息接口

| 功能 | 方法 | 路径 |
| --- | --- | --- |
| 健康检查 | `GET` | `/health` |
| API 清单 | `GET` | `/endpoints` |
| 静态契约 | `GET` | `http://localhost:8088/api-contract.json` |

`/endpoints` 和 `api-contract.json` 均包含通用 CRUD 操作与 `businessActions` 业务动作清单。

## 6. 生产业务接口

| 功能 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 任务开工 | `POST` | `/productionTask/{id}/start` | 设置任务为生产中，并补实际开始时间 |
| 任务暂停 | `POST` | `/productionTask/{id}/pause` | 设置任务为暂停 |
| 任务完工 | `POST` | `/productionTask/{id}/finish` | 设置任务为已完工，并补实际完成时间 |
| 工单生成任务 | `POST` | `/productionOrder/{id}/generateTasks` | 按工单绑定的工艺路线生成生产任务 |
| 报工提交 | `POST` | `/workReport/submit` | 保存报工并联动更新任务数量、任务状态和工单状态 |
| 移动报工候选项 | `GET` | `/workReport/options` | 返回可报工任务编号、关联工单编号、工位和剩余数量 |
| 质检提交 | `POST` | `/inspectionRecord/submit` | 保存质检；不合格或需返修时自动生成返修单 |
| 返修处理 | `POST` | `/reworkOrder/{id}/repair` | 记录维修措施、用料、工时和维修结果，状态进入待复检 |
| 返修复检 | `POST` | `/reworkOrder/{id}/recheck` | 保存复检记录；复检通过后关闭返修并恢复任务/工单状态 |
| 产品追溯 | `GET` | `/trace/{productSn}` | 按产品 SN 返回工单、任务、报工、检验、返修记录 |
| 生产概览报表 | `GET` | `/report/overview` | 返回目标数量、报工数量、合格率、不良数、工位概览 |
| 质量与产量分析报表 | `GET` | `/report/quality?period=week&productModelId=1` | 按本周/本月及可选产品型号返回产量趋势、型号构成、质量指标和缺陷分布 |
| 移动报工 | `POST` | `/workReport/submit` | 小程序轻量报工表单调用业务提交接口 |
| 移动质检 | `POST` | `/inspectionRecord/submit` | 小程序轻量质检表单调用业务提交接口 |
| 移动返修处理 | `POST` | `/reworkOrder/{id}/repair` | 小程序轻量返修表单记录维修结果 |

质量与产量分析参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| `period` | 否 | `week`（默认）或 `month`；周报按日、月报按自然周汇总 |
| `productModelId` | 否 | 产品型号 ID；不传时统计全部产品并返回型号产量构成 |

产品追溯返回：

```json
{
  "productSn": "SN-001",
  "orders": [],
  "tasks": [],
  "reports": [],
  "inspections": [],
  "reworks": []
}
```

## 7. 角色权限

后端已在 `AuthInterceptor` 按 `roleCode`、资源和动作限制访问。管理员拥有全部权限；普通岗位只允许访问本岗位业务范围。`sysUser` 只允许管理员维护，非管理员访问会返回 `403`。

| 角色 | 允许范围 |
| --- | --- |
| `admin` | 全部资源、全部业务动作、系统用户维护 |
| `planner` | 产品、物料、BOM、工艺路线、生产工单、生产任务、产品追溯、轻量报表 |
| `operator` | 生产任务、报工记录、产品追溯、轻量报表 |
| `inspector` | 检验记录、返修工单、产品追溯、轻量报表 |
| `repair` | 返修工单、检验记录只读、产品追溯、轻量报表 |
