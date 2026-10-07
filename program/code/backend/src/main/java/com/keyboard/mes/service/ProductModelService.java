package com.keyboard.mes.service;

import com.keyboard.mes.entity.ProductModel;

import java.util.ArrayList;

/**
 * 产品型号表业务接口。
 *
 * @author Keyboard MES项目组
 */
public interface ProductModelService {

    /**
     * 查询产品型号列表。
     *
     * @return 产品型号列表
     */
    ArrayList<ProductModel> productModelList();

    /**
     * 保存产品型号。
     *
     * @param productModel 产品型号数据
     * @return 操作是否成功
     */
    boolean save(ProductModel productModel);

    /**
     * 按主键查询产品型号。
     *
     * @param id 记录主键
     * @return 查询或处理后的产品型号
     */
    ProductModel getProductModelById(Long id);

    /**
     * 按主键删除产品型号。
     *
     * @param id 记录主键
     * @return 操作是否成功
     */
    boolean delete(Long id);

    /**
     * 更新产品型号。
     *
     * @param productModel 产品型号数据
     * @return 操作是否成功
     */
    boolean update(ProductModel productModel);
}
