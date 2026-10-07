package com.keyboard.mes.service;

import com.keyboard.mes.entity.ProductionTask;

import java.util.ArrayList;

/**
 * 生产任务表业务接口。
 *
 * @author Keyboard MES项目组
 */
public interface ProductionTaskService {

    /**
     * 查询生产任务列表。
     *
     * @return 生产任务列表
     */
    ArrayList<ProductionTask> productionTaskList();

    /**
     * 保存生产任务。
     *
     * @param productionTask 生产任务数据
     * @return 操作是否成功
     */
    boolean save(ProductionTask productionTask);

    /**
     * 按主键查询生产任务。
     *
     * @param id 记录主键
     * @return 查询或处理后的生产任务
     */
    ProductionTask getProductionTaskById(Long id);

    /**
     * 按主键删除生产任务。
     *
     * @param id 记录主键
     * @return 操作是否成功
     */
    boolean delete(Long id);

    /**
     * 更新生产任务。
     *
     * @param productionTask 生产任务数据
     * @return 操作是否成功
     */
    boolean update(ProductionTask productionTask);

    /**
     * 启动生产任务。
     *
     * @param id 记录主键
     * @return 查询或处理后的生产任务
     */
    ProductionTask start(Long id);

    /**
     * 暂停生产任务。
     *
     * @param id 记录主键
     * @return 查询或处理后的生产任务
     */
    ProductionTask pause(Long id);

    /**
     * 完成生产任务。
     *
     * @param id 记录主键
     * @return 查询或处理后的生产任务
     */
    ProductionTask finish(Long id);
}
