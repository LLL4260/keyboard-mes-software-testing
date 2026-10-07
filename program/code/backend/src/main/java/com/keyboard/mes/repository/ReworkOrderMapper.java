package com.keyboard.mes.repository;

import com.keyboard.mes.entity.ReworkOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 返修工单表数据访问接口。
 *
 * @author Keyboard MES项目组
 */
@Repository
@Mapper
public interface ReworkOrderMapper {

    /**
     * 查询返修工单列表。
     *
     * @return 返修工单列表
     */
    ArrayList<ReworkOrder> reworkOrderList();

    /**
     * 按产品SN查询返修工单。
     *
     * @param productSn 产品SN
     * @return 返修工单列表
     */
    ArrayList<ReworkOrder> findByProductSn(@Param("productSn") String productSn);

    /**
     * 新增返修工单。
     *
     * @param reworkOrder 返修工单数据
     * @return 受影响的记录数
     */
    int insert(ReworkOrder reworkOrder);

    /**
     * 按主键查询返修工单。
     *
     * @param id 记录主键
     * @return 查询或处理后的返修工单
     */
    ReworkOrder getById(@Param("id") Long id);

    /**
     * 更新返修工单。
     *
     * @param reworkOrder 返修工单数据
     * @return 受影响的记录数
     */
    int update(ReworkOrder reworkOrder);

    /**
     * 按主键删除返修工单。
     *
     * @param id 记录主键
     * @return 受影响的记录数
     */
    int delete(@Param("id") Long id);
}
