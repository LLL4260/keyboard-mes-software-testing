package com.keyboard.mes.repository;

import com.keyboard.mes.entity.ProductionOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 生产工单表数据访问接口。
 *
 * @author Keyboard MES项目组
 */
@Repository
@Mapper
public interface ProductionOrderMapper {

    /**
     * 查询生产工单列表。
     *
     * @return 生产工单列表
     */
    ArrayList<ProductionOrder> productionOrderList();

    /**
     * 新增生产工单。
     *
     * @param productionOrder 生产工单数据
     * @return 受影响的记录数
     */
    int insert(ProductionOrder productionOrder);

    /**
     * 按主键查询生产工单。
     *
     * @param id 记录主键
     * @return 查询或处理后的生产工单
     */
    ProductionOrder getById(@Param("id") Long id);

    /**
     * 更新生产工单。
     *
     * @param productionOrder 生产工单数据
     * @return 受影响的记录数
     */
    int update(ProductionOrder productionOrder);

    /**
     * 按主键删除生产工单。
     *
     * @param id 记录主键
     * @return 受影响的记录数
     */
    int delete(@Param("id") Long id);
}
