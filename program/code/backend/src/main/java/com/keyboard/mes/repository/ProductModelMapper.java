package com.keyboard.mes.repository;

import com.keyboard.mes.entity.ProductModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 产品型号表数据访问接口。
 */
@Repository
@Mapper
public interface ProductModelMapper {

    ArrayList<ProductModel> productModelList();

    int insert(ProductModel productModel);

    ProductModel getById(@Param("id") Long id);

    int update(ProductModel productModel);

    int delete(@Param("id") Long id);
}
