package com.keyboard.mes.service;

import com.keyboard.mes.entity.ProductionTask;

import java.util.ArrayList;

/**
 * 生产任务表业务接口。
 */
public interface ProductionTaskService {

    ArrayList<ProductionTask> productionTaskList();

    boolean save(ProductionTask productionTask);

    ProductionTask getProductionTaskById(Long id);

    boolean delete(Long id);

    boolean update(ProductionTask productionTask);

    ProductionTask start(Long id);

    ProductionTask pause(Long id);

    ProductionTask finish(Long id);
}
