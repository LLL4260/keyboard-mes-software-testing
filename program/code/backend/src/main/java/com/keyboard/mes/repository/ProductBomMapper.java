package com.keyboard.mes.repository;

import com.keyboard.mes.entity.ProductBom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 产品BOM表数据访问接口。
 *
 * @author Keyboard MES项目组
 */
@Repository
@Mapper
public interface ProductBomMapper {

    /**
     * 查询产品BOM列表。
     *
     * @return 产品BOM列表
     */
    ArrayList<ProductBom> productBomList();

    /**
     * 新增产品BOM。
     *
     * @param productBom 产品BOM数据
     * @return 受影响的记录数
     */
    int insert(ProductBom productBom);

    /**
     * 按主键查询产品BOM。
     *
     * @param id 记录主键
     * @return 查询或处理后的产品BOM
     */
    ProductBom getById(@Param("id") Long id);

    /**
     * 更新产品BOM。
     *
     * @param productBom 产品BOM数据
     * @return 受影响的记录数
     */
    int update(ProductBom productBom);

    /**
     * 按主键删除产品BOM。
     *
     * @param id 记录主键
     * @return 受影响的记录数
     */
    int delete(@Param("id") Long id);
}
