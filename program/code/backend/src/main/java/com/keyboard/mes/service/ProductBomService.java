package com.keyboard.mes.service;

import com.keyboard.mes.entity.ProductBom;

import java.util.ArrayList;

/**
 * 产品BOM表业务接口。
 *
 * @author Keyboard MES项目组
 */
public interface ProductBomService {

    /**
     * 查询产品BOM列表。
     *
     * @return 产品BOM列表
     */
    ArrayList<ProductBom> productBomList();

    /**
     * 保存产品BOM。
     *
     * @param productBom 产品BOM数据
     * @return 操作是否成功
     */
    boolean save(ProductBom productBom);

    /**
     * 按主键查询产品BOM。
     *
     * @param id 记录主键
     * @return 查询或处理后的产品BOM
     */
    ProductBom getProductBomById(Long id);

    /**
     * 按主键删除产品BOM。
     *
     * @param id 记录主键
     * @return 操作是否成功
     */
    boolean delete(Long id);

    /**
     * 更新产品BOM。
     *
     * @param productBom 产品BOM数据
     * @return 操作是否成功
     */
    boolean update(ProductBom productBom);
}
