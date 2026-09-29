package com.keyboard.mes.service;

import com.keyboard.mes.entity.ProductionOrder;
import com.keyboard.mes.entity.ProductionTask;

import java.util.ArrayList;

/**
 * 生产工单表业务接口。
 */
public interface ProductionOrderService {

    ArrayList<ProductionOrder> productionOrderList();

    boolean save(ProductionOrder productionOrder);

    ProductionOrder getProductionOrderById(Long id);

    boolean delete(Long id);

    boolean update(ProductionOrder productionOrder);

    ArrayList<ProductionTask> generateTasks(Long id);
}
