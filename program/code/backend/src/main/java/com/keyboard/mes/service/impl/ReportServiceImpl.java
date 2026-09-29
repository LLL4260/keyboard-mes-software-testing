package com.keyboard.mes.service.impl;

import com.keyboard.mes.entity.InspectionRecord;
import com.keyboard.mes.entity.ProductModel;
import com.keyboard.mes.entity.ProductionOrder;
import com.keyboard.mes.entity.ProductionTask;
import com.keyboard.mes.entity.ReworkOrder;
import com.keyboard.mes.entity.WorkReport;
import com.keyboard.mes.exception.BusinessException;
import com.keyboard.mes.repository.InspectionRecordMapper;
import com.keyboard.mes.repository.ProductModelMapper;
import com.keyboard.mes.repository.ProductionOrderMapper;
import com.keyboard.mes.repository.ProductionTaskMapper;
import com.keyboard.mes.repository.ReworkOrderMapper;
import com.keyboard.mes.repository.WorkReportMapper;
import com.keyboard.mes.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 轻量报表业务实现。
 *
 * <p>当前不新增统计表，直接从已有业务表聚合，满足 Web 和小程序展示页使用。</p>
 */
@Service
public class ReportServiceImpl implements ReportService {

    private final ProductionOrderMapper productionOrderMapper;
    private final ProductModelMapper productModelMapper;
    private final ProductionTaskMapper productionTaskMapper;
    private final WorkReportMapper workReportMapper;
    private final InspectionRecordMapper inspectionRecordMapper;
    private final ReworkOrderMapper reworkOrderMapper;

    public ReportServiceImpl(ProductionOrderMapper productionOrderMapper,
                             ProductModelMapper productModelMapper,
                             ProductionTaskMapper productionTaskMapper,
                             WorkReportMapper workReportMapper,
                             InspectionRecordMapper inspectionRecordMapper,
                             ReworkOrderMapper reworkOrderMapper) {
        this.productionOrderMapper = productionOrderMapper;
        this.productModelMapper = productModelMapper;
        this.productionTaskMapper = productionTaskMapper;
        this.workReportMapper = workReportMapper;
        this.inspectionRecordMapper = inspectionRecordMapper;
        this.reworkOrderMapper = reworkOrderMapper;
    }

    @Override
    public Map<String, Object> overview() {
        List<ProductionOrder> orders = productionOrderMapper.productionOrderList();
        List<ProductionTask> tasks = productionTaskMapper.productionTaskList();
        List<WorkReport> reports = workReportMapper.workReportList();
        List<InspectionRecord> inspections = inspectionRecordMapper.inspectionRecordList();
        List<ReworkOrder> reworks = reworkOrderMapper.reworkOrderList();

        int targetQuantity = orders.stream().mapToInt(order -> value(order.getQuantity())).sum();
        int reportQuantity = reports.stream().mapToInt(report -> value(report.getReportQuantity())).sum();
        int qualifiedQuantity = reports.stream().mapToInt(report -> value(report.getQualifiedQuantity())).sum();
        int defectQuantity = reports.stream().mapToInt(report -> value(report.getDefectQuantity())).sum();
        long runningTasks = tasks.stream().filter(task -> Integer.valueOf(1).equals(task.getStatus())).count();
        long pendingReworks = reworks.stream().filter(item -> item.getStatus() != null && item.getStatus() < 3).count();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("targetQuantity", targetQuantity);
        data.put("reportQuantity", reportQuantity);
        data.put("qualifiedQuantity", qualifiedQuantity);
        data.put("defectQuantity", defectQuantity);
        data.put("progressRate", percent(reportQuantity, targetQuantity));
        data.put("qualityRate", percent(qualifiedQuantity, reportQuantity));
        data.put("runningTasks", runningTasks);
        data.put("inspectionCount", inspections.size());
        data.put("pendingReworks", pendingReworks);
        data.put("workstations", workstationStats(reports));
        return data;
    }

    @Override
    public Map<String, Object> quality(String period, Long productModelId) {
        String normalizedPeriod = normalizePeriod(period);
        LocalDate today = LocalDate.now();
        LocalDate periodStart = switch (normalizedPeriod) {
            case "day" -> today;
            case "month" -> YearMonth.from(today).atDay(1);
            default -> today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        };
        LocalDate periodEnd = switch (normalizedPeriod) {
            case "day" -> periodStart.plusDays(1);
            case "month" -> YearMonth.from(today).plusMonths(1).atDay(1);
            default -> periodStart.plusDays(7);
        };

        List<ProductModel> productModels = productModelMapper.productModelList()
                .stream()
                .filter(model -> Integer.valueOf(1).equals(model.getStatus()))
                .sorted(Comparator.comparing(ProductModel::getModelCode, Comparator.nullsLast(String::compareTo)))
                .toList();
        ProductModel selectedModel = null;
        if (productModelId != null) {
            selectedModel = productModelMapper.getById(productModelId);
            if (selectedModel == null) {
                throw new BusinessException("产品型号不存在");
            }
        }

        Map<Long, ProductionOrder> orderMap = productionOrderMapper.productionOrderList()
                .stream()
                .collect(Collectors.toMap(ProductionOrder::getId, Function.identity(), (left, right) -> left));
        List<WorkReport> reports = workReportMapper.workReportList()
                .stream()
                .filter(report -> !Integer.valueOf(0).equals(report.getStatus()))
                .filter(report -> inPeriod(report.getReportTime(), periodStart, periodEnd))
                .filter(report -> matchesProductModel(report.getOrderId(), productModelId, orderMap))
                .toList();
        List<InspectionRecord> inspections = inspectionRecordMapper.inspectionRecordList()
                .stream()
                .filter(record -> inPeriod(record.getInspectionTime(), periodStart, periodEnd))
                .filter(record -> matchesProductModel(record.getOrderId(), productModelId, orderMap))
                .toList();

        Map<String, Integer> defectMap = new LinkedHashMap<>();
        reports.stream()
                .filter(report -> value(report.getDefectQuantity()) > 0)
                .forEach(report -> addDefect(defectMap, report.getDefectReason(), value(report.getDefectQuantity())));
        inspections.stream()
                .filter(record -> record.getResult() != null && record.getResult() == 0)
                .forEach(record -> addDefect(defectMap, record.getDefectReason(), 1));

        Map<LocalDate, Integer> trendMap = new TreeMap<>();
        for (LocalDate date = periodStart; date.isBefore(periodEnd); date = date.plusDays(1)) {
            trendMap.put(date, 0);
        }
        reports.stream()
                .forEach(report -> trendMap.merge(report.getReportTime().toLocalDate(), value(report.getReportQuantity()), Integer::sum));

        int totalQuantity = reports.stream().mapToInt(report -> value(report.getReportQuantity())).sum();
        int qualifiedQuantity = reports.stream().mapToInt(report -> value(report.getQualifiedQuantity())).sum();
        int defectQuantity = reports.stream().mapToInt(report -> value(report.getDefectQuantity())).sum();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("period", normalizedPeriod);
        data.put("periodLabel", switch (normalizedPeriod) {
            case "day" -> "本日";
            case "month" -> "本月";
            default -> "本周";
        });
        data.put("periodStart", periodStart);
        data.put("periodEnd", periodEnd.minusDays(1));
        data.put("productModelId", productModelId);
        data.put("productModelName", selectedModel == null ? "全部产品" : productModelLabel(selectedModel));
        data.put("productModels", productModels.stream().map(this::productModelOption).toList());
        data.put("totalQuantity", totalQuantity);
        data.put("qualifiedQuantity", qualifiedQuantity);
        data.put("defectQuantity", defectQuantity);
        data.put("qualityRate", percent(qualifiedQuantity, totalQuantity));
        data.put("defectTypes", defectTypes(defectMap));
        data.put("trendData", "month".equals(normalizedPeriod)
                ? monthlyTrendData(trendMap, periodStart, periodEnd)
                : dailyTrendData(trendMap));
        data.put("productBreakdown", productBreakdown(reports, orderMap, productModels, totalQuantity));
        return data;
    }

    @Override
    public Map<String, Object> orderTracking(Long orderId) {
        ProductionOrder order = productionOrderMapper.getById(orderId);
        if (order == null) {
            throw new BusinessException("生产工单不存在");
        }

        List<ProductionTask> tasks = productionTaskMapper.productionTaskList()
                .stream()
                .filter(task -> Objects.equals(task.getOrderId(), orderId))
                .sorted(Comparator.comparing(task -> value(task.getSequenceNo())))
                .toList();
        List<WorkReport> reports = workReportMapper.workReportList()
                .stream()
                .filter(report -> Objects.equals(report.getOrderId(), orderId))
                .filter(report -> !Integer.valueOf(0).equals(report.getStatus()))
                .sorted(Comparator.comparing(WorkReport::getReportTime, Comparator.nullsFirst(Comparator.naturalOrder())).reversed())
                .toList();
        List<ReworkOrder> reworks = reworkOrderMapper.reworkOrderList()
                .stream()
                .filter(rework -> Objects.equals(rework.getOrderId(), orderId))
                .toList();

        int targetQuantity = value(order.getQuantity());
        int reportedQuantity = reports.stream().mapToInt(report -> value(report.getReportQuantity())).sum();
        int reportedQualifiedQuantity = reports.stream().mapToInt(report -> value(report.getQualifiedQuantity())).sum();
        int reportedDefectQuantity = reports.stream().mapToInt(report -> value(report.getDefectQuantity())).sum();

        ProductionTask lastTask = tasks.stream()
                .max(Comparator.comparing(task -> value(task.getSequenceNo())))
                .orElse(null);
        List<WorkReport> finalReports = lastTask == null ? List.of() : reports.stream()
                .filter(report -> Objects.equals(report.getTaskId(), lastTask.getId()))
                .toList();

        // 多工序工单不能把每道工序完工数相加，否则会重复计算；成品完成数取最后一道工序进度。
        int completedQuantity = lastTask == null ? reportedQuantity : value(lastTask.getCompletedQuantity());
        int qualifiedQuantity = finalQuantity(finalReports, lastTask, reportedQualifiedQuantity, true);
        int defectQuantity = finalQuantity(finalReports, lastTask, reportedDefectQuantity, false);

        long finishedTaskCount = tasks.stream().filter(task -> Integer.valueOf(3).equals(task.getStatus())).count();
        long runningTaskCount = tasks.stream().filter(task -> Integer.valueOf(1).equals(task.getStatus())).count();
        long waitingInspectionTaskCount = tasks.stream().filter(task -> Integer.valueOf(4).equals(task.getStatus())).count();
        long pendingReworkCount = reworks.stream().filter(rework -> rework.getStatus() != null && rework.getStatus() < 3).count();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("order", order);
        data.put("targetQuantity", targetQuantity);
        data.put("completedQuantity", completedQuantity);
        data.put("finishedProductQuantity", completedQuantity);
        data.put("qualifiedQuantity", qualifiedQuantity);
        data.put("defectQuantity", defectQuantity);
        data.put("reportedQuantity", reportedQuantity);
        data.put("reportedQualifiedQuantity", reportedQualifiedQuantity);
        data.put("reportedDefectQuantity", reportedDefectQuantity);
        data.put("progressRate", percent(Math.min(completedQuantity, targetQuantity), targetQuantity));
        data.put("qualityRate", percent(qualifiedQuantity, Math.max(completedQuantity, qualifiedQuantity + defectQuantity)));
        data.put("taskCount", tasks.size());
        data.put("finishedTaskCount", finishedTaskCount);
        data.put("runningTaskCount", runningTaskCount);
        data.put("waitingInspectionTaskCount", waitingInspectionTaskCount);
        data.put("pendingReworkCount", pendingReworkCount);
        data.put("tasks", tasks.stream().map(this::taskTrackingItem).toList());
        data.put("recentReports", reports.stream().limit(8).map(this::reportTrackingItem).toList());
        data.put("todos", orderTodos(order, tasks, reworks, completedQuantity));
        return data;
    }

    private int finalQuantity(List<WorkReport> finalReports, ProductionTask lastTask, int fallbackQuantity, boolean qualified) {
        if (lastTask == null) {
            return fallbackQuantity;
        }
        if (!finalReports.isEmpty()) {
            return finalReports.stream()
                    .mapToInt(report -> qualified ? value(report.getQualifiedQuantity()) : value(report.getDefectQuantity()))
                    .sum();
        }
        int completedQuantity = value(lastTask.getCompletedQuantity());
        int defectQuantity = value(lastTask.getDefectQuantity());
        return qualified ? Math.max(0, completedQuantity - defectQuantity) : defectQuantity;
    }

    private Map<String, Object> taskTrackingItem(ProductionTask task) {
        int plannedQuantity = value(task.getPlannedQuantity());
        int completedQuantity = value(task.getCompletedQuantity());
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", task.getId());
        item.put("taskNo", task.getTaskNo());
        item.put("sequenceNo", task.getSequenceNo());
        item.put("stationCode", StringUtils.hasText(task.getStationCode()) ? task.getStationCode() : "未分配工位");
        item.put("plannedQuantity", plannedQuantity);
        item.put("completedQuantity", completedQuantity);
        item.put("defectQuantity", value(task.getDefectQuantity()));
        item.put("remainingQuantity", Math.max(0, plannedQuantity - completedQuantity));
        item.put("progressRate", percent(Math.min(completedQuantity, plannedQuantity), plannedQuantity));
        item.put("status", task.getStatus());
        item.put("statusText", taskStatusText(task.getStatus()));
        item.put("startTime", task.getStartTime());
        item.put("finishTime", task.getFinishTime());
        return item;
    }

    private Map<String, Object> reportTrackingItem(WorkReport report) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", report.getId());
        item.put("reportNo", report.getReportNo());
        item.put("taskId", report.getTaskId());
        item.put("stationCode", StringUtils.hasText(report.getStationCode()) ? report.getStationCode() : "未分配工位");
        item.put("reportQuantity", value(report.getReportQuantity()));
        item.put("qualifiedQuantity", value(report.getQualifiedQuantity()));
        item.put("defectQuantity", value(report.getDefectQuantity()));
        item.put("reportTime", report.getReportTime());
        return item;
    }

    private List<Map<String, Object>> orderTodos(ProductionOrder order,
                                                 List<ProductionTask> tasks,
                                                 List<ReworkOrder> reworks,
                                                 int completedQuantity) {
        List<Map<String, Object>> todos = new ArrayList<>();
        if (Integer.valueOf(0).equals(order.getMaterialReadyStatus())) {
            todos.add(todo("warning", "物料齐套待确认", "当前工单物料未齐套，开工前需要确认缺料风险。", "productionOrder", order.getId()));
        }
        if (tasks.isEmpty()) {
            todos.add(todo("warning", "待生成生产任务", "当前工单还没有生产任务，请先按工艺路线生成任务。", "productionOrder", order.getId()));
        }
        if (order.getPlannedEndTime() != null
                && order.getPlannedEndTime().isBefore(LocalDateTime.now())
                && completedQuantity < value(order.getQuantity())) {
            todos.add(todo("danger", "工单已超计划时间", "计划结束时间已过，当前成品完成数仍低于工单数量。", "productionOrder", order.getId()));
        }
        if (order.getDeliveryDate() != null
                && order.getDeliveryDate().isBefore(LocalDate.now())
                && completedQuantity < value(order.getQuantity())) {
            todos.add(todo("danger", "交付日期已逾期", "交付日期已过，建议优先协调生产和质检资源。", "productionOrder", order.getId()));
        }
        tasks.stream()
                .filter(task -> Integer.valueOf(0).equals(task.getStatus()))
                .forEach(task -> todos.add(todo("info", "工序待开工", taskTodoMessage(task, "仍未开工。"), "productionTask", task.getId())));
        tasks.stream()
                .filter(task -> Integer.valueOf(2).equals(task.getStatus()))
                .forEach(task -> todos.add(todo("warning", "工序已暂停", taskTodoMessage(task, "处于暂停状态，需要确认是否继续生产。"), "productionTask", task.getId())));
        tasks.stream()
                .filter(task -> Integer.valueOf(4).equals(task.getStatus()))
                .forEach(task -> todos.add(todo("warning", "工序待质检", taskTodoMessage(task, "已完工并进入待质检状态。"), "productionTask", task.getId())));
        tasks.stream()
                .filter(task -> Integer.valueOf(5).equals(task.getStatus()))
                .forEach(task -> todos.add(todo("danger", "工序异常待处理", taskTodoMessage(task, "存在异常，需要填写处理措施或协调责任人。"), "productionTask", task.getId())));
        reworks.stream()
                .filter(rework -> rework.getStatus() != null && rework.getStatus() < 3)
                .forEach(rework -> todos.add(todo("warning", "返修待处理", reworkTodoMessage(rework), "reworkOrder", rework.getId())));
        if (todos.isEmpty()) {
            todos.add(todo("success", "暂无待办", "当前工单没有明显阻塞事项。", "productionOrder", order.getId()));
        }
        return todos;
    }

    private Map<String, Object> todo(String level, String title, String message, String resource, Long recordId) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("level", level);
        item.put("title", title);
        item.put("message", message);
        item.put("resource", resource);
        item.put("recordId", recordId);
        return item;
    }

    private String taskTodoMessage(ProductionTask task, String suffix) {
        String taskNo = StringUtils.hasText(task.getTaskNo()) ? task.getTaskNo() : "任务" + task.getId();
        return taskNo + "（工序" + value(task.getSequenceNo()) + "，" + taskStatusText(task.getStatus()) + "）" + suffix;
    }

    private String reworkTodoMessage(ReworkOrder rework) {
        String reworkNo = StringUtils.hasText(rework.getReworkNo()) ? rework.getReworkNo() : "返修单" + rework.getId();
        return reworkNo + " 当前状态为" + reworkStatusText(rework.getStatus()) + "。";
    }

    private String taskStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待开工";
            case 1 -> "生产中";
            case 2 -> "暂停";
            case 3 -> "已完工";
            case 4 -> "待质检";
            case 5 -> "异常";
            default -> "未知";
        };
    }

    private String reworkStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待分派";
            case 1 -> "返修中";
            case 2 -> "待复检";
            case 3 -> "完成";
            case 4 -> "关闭";
            default -> "未知";
        };
    }

    private List<Map<String, Object>> workstationStats(List<WorkReport> reports) {
        Map<String, Map<String, Object>> stationMap = new LinkedHashMap<>();
        for (WorkReport report : reports) {
            String stationCode = StringUtils.hasText(report.getStationCode()) ? report.getStationCode() : "未分配工位";
            Map<String, Object> station = stationMap.computeIfAbsent(stationCode, key -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", key);
                item.put("reportQuantity", 0);
                item.put("defectQuantity", 0);
                item.put("status", "正常");
                return item;
            });
            int reportQuantity = (Integer) station.get("reportQuantity") + value(report.getReportQuantity());
            int defectQuantity = (Integer) station.get("defectQuantity") + value(report.getDefectQuantity());
            station.put("reportQuantity", reportQuantity);
            station.put("defectQuantity", defectQuantity);
            station.put("status", defectQuantity > 0 ? "关注" : "正常");
        }
        return new ArrayList<>(stationMap.values());
    }

    private List<Map<String, Object>> defectTypes(Map<String, Integer> defectMap) {
        int total = defectMap.values().stream().mapToInt(Integer::intValue).sum();
        return defectMap.entrySet()
                .stream()
                .sorted((left, right) -> right.getValue().compareTo(left.getValue()))
                .limit(8)
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", entry.getKey());
                    item.put("count", entry.getValue());
                    item.put("percent", percent(entry.getValue(), total));
                    return item;
                })
                .toList();
    }

    private String normalizePeriod(String period) {
        String normalized = StringUtils.hasText(period) ? period.trim().toLowerCase() : "week";
        if (!"day".equals(normalized) && !"week".equals(normalized) && !"month".equals(normalized)) {
            throw new BusinessException("统计周期仅支持 day、week 或 month");
        }
        return normalized;
    }

    private boolean inPeriod(LocalDateTime time, LocalDate start, LocalDate end) {
        if (time == null) {
            return false;
        }
        LocalDate date = time.toLocalDate();
        return !date.isBefore(start) && date.isBefore(end);
    }

    private boolean matchesProductModel(Long orderId,
                                        Long productModelId,
                                        Map<Long, ProductionOrder> orderMap) {
        ProductionOrder order = orderMap.get(orderId);
        return order != null
                && (productModelId == null || Objects.equals(order.getProductModelId(), productModelId));
    }

    private Map<String, Object> productModelOption(ProductModel model) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", model.getId());
        item.put("modelCode", model.getModelCode());
        item.put("modelName", model.getModelName());
        item.put("category", model.getCategory());
        item.put("label", productModelLabel(model));
        return item;
    }

    private String productModelLabel(ProductModel model) {
        if (StringUtils.hasText(model.getModelCode()) && StringUtils.hasText(model.getModelName())) {
            return model.getModelCode() + " · " + model.getModelName();
        }
        return StringUtils.hasText(model.getModelName()) ? model.getModelName() : model.getModelCode();
    }

    private List<Map<String, Object>> productBreakdown(List<WorkReport> reports,
                                                       Map<Long, ProductionOrder> orderMap,
                                                       List<ProductModel> productModels,
                                                       int totalQuantity) {
        Map<Long, Integer> quantityMap = new LinkedHashMap<>();
        for (WorkReport report : reports) {
            ProductionOrder order = orderMap.get(report.getOrderId());
            if (order != null && order.getProductModelId() != null) {
                quantityMap.merge(order.getProductModelId(), value(report.getReportQuantity()), Integer::sum);
            }
        }
        Map<Long, ProductModel> modelMap = productModels.stream()
                .collect(Collectors.toMap(ProductModel::getId, Function.identity(), (left, right) -> left));
        return quantityMap.entrySet()
                .stream()
                .sorted((left, right) -> right.getValue().compareTo(left.getValue()))
                .map(entry -> {
                    ProductModel model = modelMap.get(entry.getKey());
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("productModelId", entry.getKey());
                    item.put("modelCode", model == null ? "型号#" + entry.getKey() : model.getModelCode());
                    item.put("modelName", model == null ? "未维护型号" : model.getModelName());
                    item.put("category", model == null ? null : model.getCategory());
                    item.put("quantity", entry.getValue());
                    item.put("percent", percent(entry.getValue(), totalQuantity));
                    return item;
                })
                .toList();
    }

    private List<Map<String, Object>> dailyTrendData(Map<LocalDate, Integer> trendMap) {
        return trendMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("date", entry.getKey());
                    item.put("label", String.format("%02d-%02d", entry.getKey().getMonthValue(), entry.getKey().getDayOfMonth()));
                    item.put("quantity", entry.getValue());
                    return item;
                })
                .toList();
    }

    private List<Map<String, Object>> monthlyTrendData(Map<LocalDate, Integer> trendMap,
                                                       LocalDate periodStart,
                                                       LocalDate periodEnd) {
        int bucketCount = (periodEnd.minusDays(1).getDayOfMonth() + 6) / 7;
        List<Map<String, Object>> items = new ArrayList<>();
        for (int index = 0; index < bucketCount; index++) {
            LocalDate bucketStart = periodStart.plusDays(index * 7L);
            LocalDate bucketEnd = bucketStart.plusDays(7);
            int quantity = trendMap.entrySet()
                    .stream()
                    .filter(entry -> !entry.getKey().isBefore(bucketStart) && entry.getKey().isBefore(bucketEnd))
                    .mapToInt(Map.Entry::getValue)
                    .sum();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", bucketStart);
            item.put("label", "第" + (index + 1) + "周");
            item.put("quantity", quantity);
            items.add(item);
        }
        return items;
    }

    private void addDefect(Map<String, Integer> defectMap, String reason, int count) {
        String key = StringUtils.hasText(reason) ? reason : "未填写原因";
        defectMap.merge(key, count, Integer::sum);
    }

    private int value(Integer number) {
        return number == null ? 0 : number;
    }

    private BigDecimal percent(int numerator, int denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 1, RoundingMode.HALF_UP);
    }
}
