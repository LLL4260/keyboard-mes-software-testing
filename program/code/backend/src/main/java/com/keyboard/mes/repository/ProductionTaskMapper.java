package com.keyboard.mes.repository;

import com.keyboard.mes.entity.ProductionTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 生产任务表数据访问接口。
 */
@Repository
@Mapper
public interface ProductionTaskMapper {

    ArrayList<ProductionTask> productionTaskList();

    int insert(ProductionTask productionTask);

    ProductionTask getById(@Param("id") Long id);

    int update(ProductionTask productionTask);

    int delete(@Param("id") Long id);
}
