package com.keyboard.mes.repository;

import com.keyboard.mes.entity.ProductBom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 产品BOM表数据访问接口。
 */
@Repository
@Mapper
public interface ProductBomMapper {

    ArrayList<ProductBom> productBomList();

    int insert(ProductBom productBom);

    ProductBom getById(@Param("id") Long id);

    int update(ProductBom productBom);

    int delete(@Param("id") Long id);
}
