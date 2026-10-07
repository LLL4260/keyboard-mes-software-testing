package com.keyboard.mes.service.impl;

import com.keyboard.mes.entity.ProductBom;
import com.keyboard.mes.repository.ProductBomMapper;
import com.keyboard.mes.service.ProductBomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 产品BOM表业务实现。
 *
 * @author Keyboard MES项目组
 */
@Service
public class ProductBomServiceImpl implements ProductBomService {

    @Autowired
    private ProductBomMapper productBomMapper;

    @Override
    public ArrayList<ProductBom> productBomList() {
        return productBomMapper.productBomList();
    }

    @Override
    public boolean save(ProductBom productBom) {
        int num = productBomMapper.insert(productBom);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public ProductBom getProductBomById(Long id) {
        return productBomMapper.getById(id);
    }

    @Override
    public boolean delete(Long id) {
        int num = productBomMapper.delete(id);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean update(ProductBom productBom) {
        int num = productBomMapper.update(productBom);
        if (num > 0) {
            return true;
        }
        return false;
    }
}
