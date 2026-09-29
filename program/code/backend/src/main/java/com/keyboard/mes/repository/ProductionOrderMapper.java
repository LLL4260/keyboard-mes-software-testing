package com.keyboard.mes.repository;

import com.keyboard.mes.entity.ProductionOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 生产工单表数据访问接口。
 */
@Repository
@Mapper
public interface ProductionOrderMapper {

    ArrayList<ProductionOrder> productionOrderList();

    int insert(ProductionOrder productionOrder);

    ProductionOrder getById(@Param("id") Long id);

    int update(ProductionOrder productionOrder);

    int delete(@Param("id") Long id);
}
