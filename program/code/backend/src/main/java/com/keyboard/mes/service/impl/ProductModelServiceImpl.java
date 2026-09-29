package com.keyboard.mes.service.impl;

import com.keyboard.mes.entity.ProductModel;
import com.keyboard.mes.repository.ProductModelMapper;
import com.keyboard.mes.service.ProductModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 产品型号表业务实现。
 */
@Service
public class ProductModelServiceImpl implements ProductModelService {

    @Autowired
    private ProductModelMapper productModelMapper;

    @Override
    public ArrayList<ProductModel> productModelList() {
        return productModelMapper.productModelList();
    }

    @Override
    public boolean save(ProductModel productModel) {
        int num = productModelMapper.insert(productModel);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public ProductModel getProductModelById(Long id) {
        return productModelMapper.getById(id);
    }

    @Override
    public boolean delete(Long id) {
        int num = productModelMapper.delete(id);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean update(ProductModel productModel) {
        int num = productModelMapper.update(productModel);
        if (num > 0) {
            return true;
        }
        return false;
    }
}
