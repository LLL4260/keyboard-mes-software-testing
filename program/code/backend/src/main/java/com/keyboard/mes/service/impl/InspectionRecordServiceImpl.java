package com.keyboard.mes.service.impl;

import com.keyboard.mes.entity.InspectionRecord;
import com.keyboard.mes.entity.ProductionOrder;
import com.keyboard.mes.entity.ProductionTask;
import com.keyboard.mes.entity.ReworkOrder;
import com.keyboard.mes.exception.BusinessException;
import com.keyboard.mes.repository.InspectionRecordMapper;
import com.keyboard.mes.repository.ProductionOrderMapper;
import com.keyboard.mes.repository.ProductionTaskMapper;
import com.keyboard.mes.repository.ReworkOrderMapper;
import com.keyboard.mes.service.InspectionRecordService;
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
 * 检验记录表业务实现。
 */
@Service
public class InspectionRecordServiceImpl implements InspectionRecordService {

    @Autowired
    private InspectionRecordMapper inspectionRecordMapper;

    @Autowired
    private ProductionTaskMapper productionTaskMapper;

    @Autowired
    private ProductionOrderMapper productionOrderMapper;

    @Autowired
    private ReworkOrderMapper reworkOrderMapper;

    @Override
    public ArrayList<InspectionRecord> inspectionRecordList() {
        return inspectionRecordMapper.inspectionRecordList();
    }

    @Override
    public List<Map<String, Object>> inspectionOptions() {
        Map<Long, ProductionOrder> orderMap = productionOrderMapper.productionOrderList()
                .stream()
                .collect(Collectors.toMap(ProductionOrder::getId, Function.identity(), (left, right) -> left));
        return productionTaskMapper.productionTaskList()
                .stream()
                .filter(task -> task.getId() != null && task.getOrderId() != null)
                .filter(task -> Integer.valueOf(3).equals(task.getStatus()) || Integer.valueOf(4).equals(task.getStatus()))
                .filter(task -> orderMap.containsKey(task.getOrderId()))
                .sorted(Comparator.comparing(ProductionTask::getTaskNo, Comparator.nullsLast(String::compareTo)))
                .map(task -> inspectionOption(task, orderMap.get(task.getOrderId())))
                .toList();
    }

    private Map<String, Object> inspectionOption(ProductionTask task, ProductionOrder order) {
        Map<String, Object> option = new LinkedHashMap<>();
        option.put("taskId", task.getId());
        option.put("taskNo", task.getTaskNo());
        option.put("orderId", order.getId());
        option.put("orderNo", order.getOrderNo());
        option.put("stationCode", task.getStationCode());
        return option;
    }

    @Override
    public boolean save(InspectionRecord inspectionRecord) {
        int num = inspectionRecordMapper.insert(inspectionRecord);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public InspectionRecord getInspectionRecordById(Long id) {
        return inspectionRecordMapper.getById(id);
    }

    @Override
    public boolean delete(Long id) {
        int num = inspectionRecordMapper.delete(id);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean update(InspectionRecord inspectionRecord) {
        int num = inspectionRecordMapper.update(inspectionRecord);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InspectionRecord submit(InspectionRecord inspectionRecord) {
        prepareInspection(inspectionRecord);
        ProductionTask task = inspectionRecord.getTaskId() == null ? null : productionTaskMapper.getById(inspectionRecord.getTaskId());
        if (inspectionRecord.getTaskId() != null && task == null) {
            throw new BusinessException("生产任务不存在");
        }
        ProductionOrder order = requireOrder(inspectionRecord.getOrderId());
        if (task != null && !Objects.equals(task.getOrderId(), order.getId())) {
            throw new BusinessException("质检任务与工单不匹配");
        }
        if (inspectionRecordMapper.insert(inspectionRecord) <= 0) {
            throw new BusinessException("质检记录保存失败");
        }
        // 不合格或选择返修处置时自动生成返修单，保证质检问题进入闭环。
        if (isFailed(inspectionRecord)) {
            createReworkOrder(inspectionRecord);
            if (task != null) {
                task.setStatus(5);
                productionTaskMapper.update(task);
            }
        } else if (task != null) {
            task.setStatus(3);
            if (task.getFinishTime() == null) {
                task.setFinishTime(LocalDateTime.now());
            }
            productionTaskMapper.update(task);
        }
        refreshOrderStatus(order.getId());
        return inspectionRecordMapper.getById(inspectionRecord.getId());
    }

    private void prepareInspection(InspectionRecord inspection) {
        if (!StringUtils.hasText(inspection.getInspectionNo())) {
            inspection.setInspectionNo("QC-" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(inspection.getInspectionType())) {
            inspection.setInspectionType("process");
        }
        if (!StringUtils.hasText(inspection.getSourceType())) {
            inspection.setSourceType("manual");
        }
        if (inspection.getInspectionTime() == null) {
            inspection.setInspectionTime(LocalDateTime.now());
        }
        if (inspection.getApprovalStatus() == null) {
            inspection.setApprovalStatus(0);
        }
    }

    private boolean isFailed(InspectionRecord inspection) {
        return Integer.valueOf(0).equals(inspection.getResult()) || Integer.valueOf(1).equals(inspection.getHandlingMethod());
    }

    private void createReworkOrder(InspectionRecord inspection) {
        ReworkOrder rework = new ReworkOrder();
        rework.setReworkNo("RW-" + inspection.getInspectionNo());
        rework.setSourceInspectionId(inspection.getId());
        rework.setOrderId(inspection.getOrderId());
        rework.setTaskId(inspection.getTaskId());
        rework.setProductSn(inspection.getProductSn());
        rework.setDefectReason(inspection.getDefectReason());
        rework.setReworkRequirement("质检不合格，需返修复检");
        rework.setStatus(0);
        rework.setCreateTime(LocalDateTime.now());
        if (reworkOrderMapper.insert(rework) <= 0) {
            throw new BusinessException("自动生成返修单失败");
        }
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
        boolean hasAbnormal = tasks.stream().anyMatch(item -> Integer.valueOf(5).equals(item.getStatus()));
        boolean waitingInspection = tasks.stream().anyMatch(item -> Integer.valueOf(4).equals(item.getStatus()));
        boolean allDone = tasks.stream().allMatch(item -> Integer.valueOf(3).equals(item.getStatus()));
        order.setStatus(hasAbnormal ? 2 : (allDone ? 4 : (waitingInspection ? 3 : 2)));
        productionOrderMapper.update(order);
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
}
