package com.keyboard.mes.service;

import com.keyboard.mes.entity.ProductModel;

import java.util.ArrayList;

/**
 * 产品型号表业务接口。
 */
public interface ProductModelService {

    ArrayList<ProductModel> productModelList();

    boolean save(ProductModel productModel);

    ProductModel getProductModelById(Long id);

    boolean delete(Long id);

    boolean update(ProductModel productModel);
}
