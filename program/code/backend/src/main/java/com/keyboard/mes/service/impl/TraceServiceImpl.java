package com.keyboard.mes.service.impl;

import com.keyboard.mes.dto.TraceResult;
import com.keyboard.mes.entity.InspectionRecord;
import com.keyboard.mes.entity.ProductionOrder;
import com.keyboard.mes.entity.ProductionTask;
import com.keyboard.mes.entity.ReworkOrder;
import com.keyboard.mes.entity.WorkReport;
import com.keyboard.mes.exception.BusinessException;
import com.keyboard.mes.repository.InspectionRecordMapper;
import com.keyboard.mes.repository.ProductionOrderMapper;
import com.keyboard.mes.repository.ProductionTaskMapper;
import com.keyboard.mes.repository.ReworkOrderMapper;
import com.keyboard.mes.repository.WorkReportMapper;
import com.keyboard.mes.service.TraceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 产品追溯业务实现。
 *
 * @author Keyboard MES项目组
 */
@Service
public class TraceServiceImpl implements TraceService {

    private final WorkReportMapper workReportMapper;
    private final InspectionRecordMapper inspectionRecordMapper;
    private final ReworkOrderMapper reworkOrderMapper;
    private final ProductionTaskMapper productionTaskMapper;
    private final ProductionOrderMapper productionOrderMapper;

    public TraceServiceImpl(WorkReportMapper workReportMapper,
                            InspectionRecordMapper inspectionRecordMapper,
                            ReworkOrderMapper reworkOrderMapper,
                            ProductionTaskMapper productionTaskMapper,
                            ProductionOrderMapper productionOrderMapper) {
        this.workReportMapper = workReportMapper;
        this.inspectionRecordMapper = inspectionRecordMapper;
        this.reworkOrderMapper = reworkOrderMapper;
        this.productionTaskMapper = productionTaskMapper;
        this.productionOrderMapper = productionOrderMapper;
    }

    @Override
    public TraceResult getTraceByProductSn(String productSn) {
        if (!StringUtils.hasText(productSn)) {
            throw new BusinessException("请输入产品 SN");
        }
        String normalizedSn = productSn.trim();
        List<WorkReport> reports = workReportMapper.findByProductSn(normalizedSn);
        List<InspectionRecord> inspections = inspectionRecordMapper.findByProductSn(normalizedSn);
        List<ReworkOrder> reworks = reworkOrderMapper.findByProductSn(normalizedSn);

        Set<Long> taskIds = new LinkedHashSet<>();
        Set<Long> orderIds = new LinkedHashSet<>();
        collectIds(reports, taskIds, orderIds);
        collectIds(inspections, taskIds, orderIds);
        collectIds(reworks, taskIds, orderIds);

        List<ProductionTask> tasks = filterTasks(taskIds);
        tasks.stream().map(ProductionTask::getOrderId).filter(Objects::nonNull).forEach(orderIds::add);

        // 追溯以产品 SN 为入口，先拿业务记录，再反查任务和工单，前端可按业务或时间线展示。
        TraceResult result = new TraceResult();
        result.setProductSn(normalizedSn);
        result.setReports(reports);
        result.setInspections(inspections);
        result.setReworks(reworks);
        result.setTasks(tasks);
        result.setOrders(filterOrders(orderIds));
        return result;
    }

    private void collectIds(List<?> records, Set<Long> taskIds, Set<Long> orderIds) {
        for (Object record : records) {
            if (record instanceof WorkReport item) {
                addIfPresent(taskIds, item.getTaskId());
                addIfPresent(orderIds, item.getOrderId());
            } else if (record instanceof InspectionRecord item) {
                addIfPresent(taskIds, item.getTaskId());
                addIfPresent(orderIds, item.getOrderId());
            } else if (record instanceof ReworkOrder item) {
                addIfPresent(taskIds, item.getTaskId());
                addIfPresent(orderIds, item.getOrderId());
            }
        }
    }

    private void addIfPresent(Set<Long> ids, Long id) {
        if (id != null) {
            ids.add(id);
        }
    }

    private List<ProductionTask> filterTasks(Set<Long> taskIds) {
        if (taskIds.isEmpty()) {
            return new ArrayList<>();
        }
        return productionTaskMapper.productionTaskList()
                .stream()
                .filter(task -> taskIds.contains(task.getId()))
                .toList();
    }

    private List<ProductionOrder> filterOrders(Set<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return new ArrayList<>();
        }
        return productionOrderMapper.productionOrderList()
                .stream()
                .filter(order -> orderIds.contains(order.getId()))
                .toList();
    }
}
