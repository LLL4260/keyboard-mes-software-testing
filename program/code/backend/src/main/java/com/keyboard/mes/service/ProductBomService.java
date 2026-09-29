package com.keyboard.mes.service;

import com.keyboard.mes.entity.ProductBom;

import java.util.ArrayList;

/**
 * 产品BOM表业务接口。
 */
public interface ProductBomService {

    ArrayList<ProductBom> productBomList();

    boolean save(ProductBom productBom);

    ProductBom getProductBomById(Long id);

    boolean delete(Long id);

    boolean update(ProductBom productBom);
}
