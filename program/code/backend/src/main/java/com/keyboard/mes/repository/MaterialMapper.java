package com.keyboard.mes.repository;

import com.keyboard.mes.entity.Material;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 物料表数据访问接口。
 *
 * @author Keyboard MES项目组
 */
@Repository
@Mapper
public interface MaterialMapper {

    /**
     * 查询物料列表。
     *
     * @return 物料列表
     */
    ArrayList<Material> materialList();

    /**
     * 新增物料。
     *
     * @param material 物料数据
     * @return 受影响的记录数
     */
    int insert(Material material);

    /**
     * 按主键查询物料。
     *
     * @param id 记录主键
     * @return 查询或处理后的物料
     */
    Material getById(@Param("id") Long id);

    /**
     * 更新物料。
     *
     * @param material 物料数据
     * @return 受影响的记录数
     */
    int update(Material material);

    /**
     * 按主键删除物料。
     *
     * @param id 记录主键
     * @return 受影响的记录数
     */
    int delete(@Param("id") Long id);
}
