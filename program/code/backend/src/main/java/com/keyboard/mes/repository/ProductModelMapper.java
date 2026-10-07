package com.keyboard.mes.repository;

import com.keyboard.mes.entity.ProductModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 产品型号表数据访问接口。
 *
 * @author Keyboard MES项目组
 */
@Repository
@Mapper
public interface ProductModelMapper {

    /**
     * 查询产品型号列表。
     *
     * @return 产品型号列表
     */
    ArrayList<ProductModel> productModelList();

    /**
     * 新增产品型号。
     *
     * @param productModel 产品型号数据
     * @return 受影响的记录数
     */
    int insert(ProductModel productModel);

    /**
     * 按主键查询产品型号。
     *
     * @param id 记录主键
     * @return 查询或处理后的产品型号
     */
    ProductModel getById(@Param("id") Long id);

    /**
     * 更新产品型号。
     *
     * @param productModel 产品型号数据
     * @return 受影响的记录数
     */
    int update(ProductModel productModel);

    /**
     * 按主键删除产品型号。
     *
     * @param id 记录主键
     * @return 受影响的记录数
     */
    int delete(@Param("id") Long id);
}
