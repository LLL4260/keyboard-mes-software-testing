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
import com.keyboard.mes.service.ReworkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 返修工单表业务实现。
 */
@Service
public class ReworkOrderServiceImpl implements ReworkOrderService {

    @Autowired
    private ReworkOrderMapper reworkOrderMapper;

    @Autowired
    private InspectionRecordMapper inspectionRecordMapper;

    @Autowired
    private ProductionTaskMapper productionTaskMapper;

    @Autowired
    private ProductionOrderMapper productionOrderMapper;

    @Override
    public ArrayList<ReworkOrder> reworkOrderList() {
        return reworkOrderMapper.reworkOrderList();
    }

    @Override
    public boolean save(ReworkOrder reworkOrder) {
        int num = reworkOrderMapper.insert(reworkOrder);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public ReworkOrder getReworkOrderById(Long id) {
        return reworkOrderMapper.getById(id);
    }

    @Override
    public boolean delete(Long id) {
        int num = reworkOrderMapper.delete(id);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean update(ReworkOrder reworkOrder) {
        int num = reworkOrderMapper.update(reworkOrder);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReworkOrder repair(Long id, ReworkOrder input) {
        ReworkOrder rework = requireRework(id);
        rework.setAssigneeId(input.getAssigneeId());
        rework.setRepairAction(input.getRepairAction());
        rework.setReplacedMaterial(input.getReplacedMaterial());
        rework.setRepairHours(input.getRepairHours());
        rework.setRepairResult(input.getRepairResult());
        rework.setRepairTime(input.getRepairTime() == null ? LocalDateTime.now() : input.getRepairTime());
        rework.setStatus(input.getStatus() == null ? 2 : input.getStatus());
        updateOrThrow(rework);
        return reworkOrderMapper.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReworkOrder recheck(Long id, InspectionRecord inspectionRecord) {
        ReworkOrder rework = requireRework(id);
        prepareRecheck(inspectionRecord, rework);
        if (inspectionRecordMapper.insert(inspectionRecord) <= 0) {
            throw new BusinessException("复检记录保存失败");
        }
        rework.setRecheckInspectionId(inspectionRecord.getId());
        boolean passed = Integer.valueOf(1).equals(inspectionRecord.getResult());
        rework.setStatus(passed ? 3 : 1);
        updateOrThrow(rework);
        // 复检通过才关闭相关任务并刷新工单状态；未通过则回到返修中继续处理。
        if (passed) {
            finishRelatedTask(rework);
            refreshOrderStatus(rework.getOrderId());
        }
        return reworkOrderMapper.getById(id);
    }

    private void prepareRecheck(InspectionRecord inspection, ReworkOrder rework) {
        if (!StringUtils.hasText(inspection.getInspectionNo())) {
            inspection.setInspectionNo("RQ-" + System.currentTimeMillis());
        }
        inspection.setInspectionType("recheck");
        inspection.setTaskId(rework.getTaskId());
        inspection.setOrderId(rework.getOrderId());
        inspection.setProductSn(rework.getProductSn());
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

    private ReworkOrder requireRework(Long id) {
        if (id == null) {
            throw new BusinessException("返修单不能为空");
        }
        ReworkOrder rework = reworkOrderMapper.getById(id);
        if (rework == null) {
            throw new BusinessException("返修单不存在");
        }
        return rework;
    }

    private void updateOrThrow(ReworkOrder rework) {
        if (reworkOrderMapper.update(rework) <= 0) {
            throw new BusinessException("返修单更新失败");
        }
    }

    private void finishRelatedTask(ReworkOrder rework) {
        if (rework.getTaskId() == null) {
            return;
        }
        ProductionTask task = productionTaskMapper.getById(rework.getTaskId());
        if (task == null) {
            return;
        }
        task.setStatus(3);
        if (task.getFinishTime() == null) {
            task.setFinishTime(LocalDateTime.now());
        }
        if (productionTaskMapper.update(task) <= 0) {
            throw new BusinessException("返修复检后任务状态更新失败");
        }
    }

    private void refreshOrderStatus(Long orderId) {
        if (orderId == null) {
            return;
        }
        ProductionOrder order = productionOrderMapper.getById(orderId);
        if (order == null) {
            return;
        }
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
}
