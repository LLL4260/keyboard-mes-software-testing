package com.keyboard.mes.repository;

import com.keyboard.mes.entity.ReworkOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 返修工单表数据访问接口。
 */
@Repository
@Mapper
public interface ReworkOrderMapper {

    ArrayList<ReworkOrder> reworkOrderList();

    ArrayList<ReworkOrder> findByProductSn(@Param("productSn") String productSn);

    int insert(ReworkOrder reworkOrder);

    ReworkOrder getById(@Param("id") Long id);

    int update(ReworkOrder reworkOrder);

    int delete(@Param("id") Long id);
}
