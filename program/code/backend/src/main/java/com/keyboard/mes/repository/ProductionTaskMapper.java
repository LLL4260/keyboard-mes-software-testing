package com.keyboard.mes.repository;

import com.keyboard.mes.entity.ProductionTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 生产任务表数据访问接口。
 *
 * @author Keyboard MES项目组
 */
@Repository
@Mapper
public interface ProductionTaskMapper {

    /**
     * 查询生产任务列表。
     *
     * @return 生产任务列表
     */
    ArrayList<ProductionTask> productionTaskList();

    /**
     * 新增生产任务。
     *
     * @param productionTask 生产任务数据
     * @return 受影响的记录数
     */
    int insert(ProductionTask productionTask);

    /**
     * 按主键查询生产任务。
     *
     * @param id 记录主键
     * @return 查询或处理后的生产任务
     */
    ProductionTask getById(@Param("id") Long id);

    /**
     * 更新生产任务。
     *
     * @param productionTask 生产任务数据
     * @return 受影响的记录数
     */
    int update(ProductionTask productionTask);

    /**
     * 按主键删除生产任务。
     *
     * @param id 记录主键
     * @return 受影响的记录数
     */
    int delete(@Param("id") Long id);
}
