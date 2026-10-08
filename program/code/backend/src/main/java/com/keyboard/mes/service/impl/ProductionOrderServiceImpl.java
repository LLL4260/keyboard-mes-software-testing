package com.keyboard.mes.service.impl;

import com.keyboard.mes.entity.ProductionOrder;
import com.keyboard.mes.entity.ProductionTask;
import com.keyboard.mes.entity.ProcessRoute;
import com.keyboard.mes.exception.BusinessException;
import com.keyboard.mes.repository.ProcessRouteMapper;
import com.keyboard.mes.repository.ProductionOrderMapper;
import com.keyboard.mes.repository.ProductionTaskMapper;
import com.keyboard.mes.service.ProductionOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 生产工单表业务实现。
 *
 * @author Keyboard MES项目组
 */
@Service
public class ProductionOrderServiceImpl implements ProductionOrderService {

    @Autowired
    private ProductionOrderMapper productionOrderMapper;

    @Autowired
    private ProcessRouteMapper processRouteMapper;

    @Autowired
    private ProductionTaskMapper productionTaskMapper;

    @Override
    public ArrayList<ProductionOrder> productionOrderList() {
        return productionOrderMapper.productionOrderList();
    }

    @Override
    public boolean save(ProductionOrder productionOrder) {
        int num = productionOrderMapper.insert(productionOrder);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public ProductionOrder getProductionOrderById(Long id) {
        return productionOrderMapper.getById(id);
    }

    @Override
    public boolean delete(Long id) {
        int num = productionOrderMapper.delete(id);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean update(ProductionOrder productionOrder) {
        int num = productionOrderMapper.update(productionOrder);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArrayList<ProductionTask> generateTasks(Long id) {
        ProductionOrder order = productionOrderMapper.getById(id);
        if (order == null) {
            throw new BusinessException("生产工单不存在");
        }
        List<ProductionTask> existedTasks = productionTaskMapper.productionTaskList()
                .stream()
                .filter(task -> Objects.equals(task.getOrderId(), id))
                .collect(Collectors.toList());
        if (!existedTasks.isEmpty()) {
            return new ArrayList<>(existedTasks);
        }
        // 按工单绑定的产品型号和路线版本生成任务，避免不同版本工艺混用。
        List<ProcessRoute> routes = processRouteMapper.processRouteList()
                .stream()
                .filter(route -> Objects.equals(route.getProductModelId(), order.getProductModelId()))
                .filter(route -> Objects.equals(route.getRouteVersion(), order.getRouteVersion()))
                .filter(route -> route.getStatus() == null || route.getStatus() == 1)
                .sorted(Comparator.comparing(ProcessRoute::getSequenceNo))
                .collect(Collectors.toList());
        if (routes.isEmpty()) {
            throw new BusinessException("未找到匹配的启用工艺路线");
        }
        ArrayList<ProductionTask> tasks = new ArrayList<>();
        for (ProcessRoute route : routes) {
            // 每一道启用工序对应一张生产任务，任务顺序直接沿用工艺路线顺序。
            ProductionTask task = new ProductionTask();
            task.setTaskNo(buildTaskNo(order, route));
            task.setOrderId(order.getId());
            task.setProcessRouteId(route.getId());
            task.setSequenceNo(route.getSequenceNo());
            task.setStationCode(route.getStationCode());
            task.setPlannedQuantity(order.getQuantity());
            task.setCompletedQuantity(0);
            task.setDefectQuantity(0);
            task.setStatus(0);
            task.setRemark("由工单生成");
            if (productionTaskMapper.insert(task) <= 0) {
                throw new BusinessException("生成生产任务失败");
            }
            tasks.add(task);
        }
        order.setStatus(1);
        productionOrderMapper.update(order);
        return tasks;
    }

    private String buildTaskNo(ProductionOrder order, ProcessRoute route) {
        String orderNo = StringUtils.hasText(order.getOrderNo()) ? order.getOrderNo() : String.valueOf(order.getId());
        Integer sequenceNo = route.getSequenceNo() == null ? Integer.valueOf(0) : route.getSequenceNo();
        return "TASK-" + orderNo + "-" + String.format("%03d", sequenceNo);
    }
}
