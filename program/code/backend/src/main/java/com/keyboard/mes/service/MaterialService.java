package com.keyboard.mes.service;

import com.keyboard.mes.entity.Material;

import java.util.ArrayList;

/**
 * 物料表业务接口。
 *
 * @author Keyboard MES项目组
 */
public interface MaterialService {

    /**
     * 查询物料列表。
     *
     * @return 物料列表
     */
    ArrayList<Material> materialList();

    /**
     * 保存物料。
     *
     * @param material 物料数据
     * @return 操作是否成功
     */
    boolean save(Material material);

    /**
     * 按主键查询物料。
     *
     * @param id 记录主键
     * @return 查询或处理后的物料
     */
    Material getMaterialById(Long id);

    /**
     * 按主键删除物料。
     *
     * @param id 记录主键
     * @return 操作是否成功
     */
    boolean delete(Long id);

    /**
     * 更新物料。
     *
     * @param material 物料数据
     * @return 操作是否成功
     */
    boolean update(Material material);
}
