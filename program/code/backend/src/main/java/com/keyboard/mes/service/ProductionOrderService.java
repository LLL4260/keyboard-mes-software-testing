package com.keyboard.mes.service;

import com.keyboard.mes.entity.ProductionOrder;
import com.keyboard.mes.entity.ProductionTask;

import java.util.ArrayList;

/**
 * 生产工单表业务接口。
 *
 * @author Keyboard MES项目组
 */
public interface ProductionOrderService {

    /**
     * 查询生产工单列表。
     *
     * @return 生产工单列表
     */
    ArrayList<ProductionOrder> productionOrderList();

    /**
     * 保存生产工单。
     *
     * @param productionOrder 生产工单数据
     * @return 操作是否成功
     */
    boolean save(ProductionOrder productionOrder);

    /**
     * 按主键查询生产工单。
     *
     * @param id 记录主键
     * @return 查询或处理后的生产工单
     */
    ProductionOrder getProductionOrderById(Long id);

    /**
     * 按主键删除生产工单。
     *
     * @param id 记录主键
     * @return 操作是否成功
     */
    boolean delete(Long id);

    /**
     * 更新生产工单。
     *
     * @param productionOrder 生产工单数据
     * @return 操作是否成功
     */
    boolean update(ProductionOrder productionOrder);

    /**
     * 根据工单生成生产任务。
     *
     * @param id 记录主键
     * @return 生产工单列表
     */
    ArrayList<ProductionTask> generateTasks(Long id);
}
