package com.keyboard.mes.repository;

import com.keyboard.mes.entity.Material;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 物料表数据访问接口。
 */
@Repository
@Mapper
public interface MaterialMapper {

    ArrayList<Material> materialList();

    int insert(Material material);

    Material getById(@Param("id") Long id);

    int update(Material material);

    int delete(@Param("id") Long id);
}
