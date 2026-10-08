package com.keyboard.mes.service.impl;

import com.keyboard.mes.entity.ProductionTask;
import com.keyboard.mes.entity.ProcessRoute;
import com.keyboard.mes.exception.BusinessException;
import com.keyboard.mes.repository.ProcessRouteMapper;
import com.keyboard.mes.repository.ProductionTaskMapper;
import com.keyboard.mes.service.ProductionTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * 生产任务表业务实现。
 *
 * @author Keyboard MES项目组
 */
@Service
public class ProductionTaskServiceImpl implements ProductionTaskService {

    @Autowired
    private ProductionTaskMapper productionTaskMapper;

    @Autowired
    private ProcessRouteMapper processRouteMapper;

    @Override
    public ArrayList<ProductionTask> productionTaskList() {
        return productionTaskMapper.productionTaskList();
    }

    @Override
    public boolean save(ProductionTask productionTask) {
        int num = productionTaskMapper.insert(productionTask);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public ProductionTask getProductionTaskById(Long id) {
        return productionTaskMapper.getById(id);
    }

    @Override
    public boolean delete(Long id) {
        int num = productionTaskMapper.delete(id);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean update(ProductionTask productionTask) {
        int num = productionTaskMapper.update(productionTask);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public ProductionTask start(Long id) {
        ProductionTask task = requireTask(id);
        task.setStatus(1);
        if (task.getStartTime() == null) {
            task.setStartTime(LocalDateTime.now());
        }
        updateOrThrow(task);
        return productionTaskMapper.getById(id);
    }

    @Override
    public ProductionTask pause(Long id) {
        ProductionTask task = requireTask(id);
        task.setStatus(2);
        updateOrThrow(task);
        return productionTaskMapper.getById(id);
    }

    @Override
    public ProductionTask finish(Long id) {
        ProductionTask task = requireTask(id);
        int completed = task.getCompletedQuantity() == null ? 0 : task.getCompletedQuantity();
        int planned = task.getPlannedQuantity() == null ? Integer.valueOf(0) : task.getPlannedQuantity();
        if (completed < planned) {
            throw new BusinessException("任务累计完成数（" + completed + "）未达到计划数（" + planned + "），不能完工");
        }
        if (isQualityGate(task)) {
            task.setStatus(4);
            task.setFinishTime(LocalDateTime.now());
        } else {
            task.setStatus(3);
            task.setFinishTime(LocalDateTime.now());
        }
        if (task.getCompletedQuantity() == null) {
            task.setCompletedQuantity(task.getPlannedQuantity() == null ? Integer.valueOf(0) : task.getPlannedQuantity());
        }
        if (task.getDefectQuantity() == null) {
            task.setDefectQuantity(0);
        }
        updateOrThrow(task);
        return productionTaskMapper.getById(id);
    }

    private boolean isQualityGate(ProductionTask task) {
        if (task.getProcessRouteId() == null) {
            return false;
        }
        ProcessRoute route = processRouteMapper.getById(task.getProcessRouteId());
        return route != null && Integer.valueOf(1).equals(route.getQualityGate());
    }

    private ProductionTask requireTask(Long id) {
        ProductionTask task = productionTaskMapper.getById(id);
        if (task == null) {
            throw new BusinessException("生产任务不存在");
        }
        return task;
    }

    private void updateOrThrow(ProductionTask task) {
        if (productionTaskMapper.update(task) <= 0) {
            throw new BusinessException("生产任务状态更新失败");
        }
    }
}
