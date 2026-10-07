package com.keyboard.mes.service.impl;

import com.keyboard.mes.entity.WorkReport;
import com.keyboard.mes.entity.ProcessRoute;
import com.keyboard.mes.entity.ProductionOrder;
import com.keyboard.mes.entity.ProductionTask;
import com.keyboard.mes.exception.BusinessException;
import com.keyboard.mes.repository.ProcessRouteMapper;
import com.keyboard.mes.repository.ProductionOrderMapper;
import com.keyboard.mes.repository.ProductionTaskMapper;
import com.keyboard.mes.repository.WorkReportMapper;
import com.keyboard.mes.service.WorkReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 报工记录表业务实现。
 *
 * @author Keyboard MES项目组
 */
@Service
public class WorkReportServiceImpl implements WorkReportService {

    @Autowired
    private WorkReportMapper workReportMapper;

    @Autowired
    private ProductionTaskMapper productionTaskMapper;

    @Autowired
    private ProductionOrderMapper productionOrderMapper;

    @Autowired
    private ProcessRouteMapper processRouteMapper;

    @Override
    public ArrayList<WorkReport> workReportList() {
        return workReportMapper.workReportList();
    }

    @Override
    public List<Map<String, Object>> reportOptions() {
        Map<Long, ProductionOrder> orderMap = productionOrderMapper.productionOrderList()
                .stream()
                .collect(Collectors.toMap(ProductionOrder::getId, Function.identity(), (left, right) -> left));
        return productionTaskMapper.productionTaskList()
                .stream()
                .filter(task -> task.getId() != null && task.getOrderId() != null)
                .filter(task -> Integer.valueOf(0).equals(task.getStatus()) || Integer.valueOf(1).equals(task.getStatus()))
                .filter(task -> orderMap.containsKey(task.getOrderId()))
                .sorted(Comparator.comparing(ProductionTask::getTaskNo, Comparator.nullsLast(String::compareTo)))
                .map(task -> reportOption(task, orderMap.get(task.getOrderId())))
                .toList();
    }

    private Map<String, Object> reportOption(ProductionTask task, ProductionOrder order) {
        Map<String, Object> option = new LinkedHashMap<>();
        option.put("taskId", task.getId());
        option.put("taskNo", task.getTaskNo());
        option.put("orderId", order.getId());
        option.put("orderNo", order.getOrderNo());
        option.put("stationCode", task.getStationCode());
        option.put("plannedQuantity", value(task.getPlannedQuantity()));
        option.put("completedQuantity", value(task.getCompletedQuantity()));
        option.put("remainingQuantity", Math.max(0, value(task.getPlannedQuantity()) - value(task.getCompletedQuantity())));
        return option;
    }

    @Override
    public boolean save(WorkReport workReport) {
        int num = workReportMapper.insert(workReport);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public WorkReport getWorkReportById(Long id) {
        return workReportMapper.getById(id);
    }

    @Override
    public boolean delete(Long id) {
        int num = workReportMapper.delete(id);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean update(WorkReport workReport) {
        int num = workReportMapper.update(workReport);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkReport submit(WorkReport workReport) {
        ProductionTask task = requireTask(workReport.getTaskId());
        ProductionOrder order = requireOrder(workReport.getOrderId());
        if (!Objects.equals(task.getOrderId(), order.getId())) {
            throw new BusinessException("报工任务与工单不匹配");
        }
        prepareReport(workReport, task);
        validateReportQuantity(workReport, task);
        if (workReportMapper.insert(workReport) <= 0) {
            throw new BusinessException("报工保存失败");
        }
        // 报工是任务进度的来源，保存成功后立即回写任务完工数和不良数。
        int completedQuantity = value(task.getCompletedQuantity()) + value(workReport.getReportQuantity());
        int defectQuantity = value(task.getDefectQuantity()) + value(workReport.getDefectQuantity());
        task.setCompletedQuantity(completedQuantity);
        task.setDefectQuantity(defectQuantity);
        if (task.getStartTime() == null) {
            task.setStartTime(LocalDateTime.now());
        }
        if (completedQuantity >= value(task.getPlannedQuantity())) {
            task.setFinishTime(LocalDateTime.now());
            // 质检关口工序完工后进入待质检，普通工序直接完成。
            task.setStatus(isQualityGate(task) ? 4 : 3);
        } else {
            task.setStatus(1);
        }
        if (productionTaskMapper.update(task) <= 0) {
            throw new BusinessException("生产任务进度更新失败");
        }
        refreshOrderStatus(order.getId());
        return workReportMapper.getById(workReport.getId());
    }

    private void prepareReport(WorkReport report, ProductionTask task) {
        if (!StringUtils.hasText(report.getReportNo())) {
            report.setReportNo("WR-" + System.currentTimeMillis());
        }
        if (report.getReportTime() == null) {
            report.setReportTime(LocalDateTime.now());
        }
        if (report.getStatus() == null) {
            report.setStatus(1);
        }
        if (!StringUtils.hasText(report.getStationCode())) {
            report.setStationCode(task.getStationCode());
        }
        if (report.getReportQuantity() == null) {
            report.setReportQuantity(0);
        }
        if (report.getQualifiedQuantity() == null) {
            report.setQualifiedQuantity(Math.max(0, value(report.getReportQuantity()) - value(report.getDefectQuantity())));
        }
        if (report.getDefectQuantity() == null) {
            report.setDefectQuantity(0);
        }
    }

    private void validateReportQuantity(WorkReport report, ProductionTask task) {
        int reportQuantity = value(report.getReportQuantity());
        int qualified = value(report.getQualifiedQuantity());
        int defect = value(report.getDefectQuantity());
        if (reportQuantity <= 0) {
            throw new BusinessException("报工数量必须大于 0");
        }
        if (qualified < 0 || defect < 0) {
            throw new BusinessException("合格数和不良数不得为负");
        }
        if (qualified + defect != reportQuantity) {
            throw new BusinessException("合格数与不良数之和必须等于报工数量");
        }
        int remaining = value(task.getPlannedQuantity()) - value(task.getCompletedQuantity());
        if (reportQuantity > remaining) {
            throw new BusinessException("报工数量超过任务剩余数量（剩余 " + remaining + "）");
        }
    }

    private boolean isQualityGate(ProductionTask task) {
        if (task.getProcessRouteId() == null) {
            return false;
        }
        ProcessRoute route = processRouteMapper.getById(task.getProcessRouteId());
        return route != null && Integer.valueOf(1).equals(route.getQualityGate());
    }

    private void refreshOrderStatus(Long orderId) {
        ProductionOrder order = requireOrder(orderId);
        List<ProductionTask> tasks = productionTaskMapper.productionTaskList()
                .stream()
                .filter(item -> Objects.equals(item.getOrderId(), orderId))
                .toList();
        if (tasks.isEmpty()) {
            return;
        }
        boolean allDone = tasks.stream().allMatch(item -> Integer.valueOf(3).equals(item.getStatus()));
        boolean waitingInspection = tasks.stream().anyMatch(item -> Integer.valueOf(4).equals(item.getStatus()));
        order.setStatus(allDone ? 4 : (waitingInspection ? 3 : 2));
        productionOrderMapper.update(order);
    }

    private ProductionTask requireTask(Long id) {
        if (id == null) {
            throw new BusinessException("生产任务不能为空");
        }
        ProductionTask task = productionTaskMapper.getById(id);
        if (task == null) {
            throw new BusinessException("生产任务不存在");
        }
        return task;
    }

    private ProductionOrder requireOrder(Long id) {
        if (id == null) {
            throw new BusinessException("生产工单不能为空");
        }
        ProductionOrder order = productionOrderMapper.getById(id);
        if (order == null) {
            throw new BusinessException("生产工单不存在");
        }
        return order;
    }

    private int value(Integer number) {
        return number == null ? 0 : number;
    }
}
