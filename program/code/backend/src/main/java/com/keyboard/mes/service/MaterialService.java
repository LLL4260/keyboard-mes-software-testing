package com.keyboard.mes.service;

import com.keyboard.mes.entity.Material;

import java.util.ArrayList;

/**
 * 物料表业务接口。
 */
public interface MaterialService {

    ArrayList<Material> materialList();

    boolean save(Material material);

    Material getMaterialById(Long id);

    boolean delete(Long id);

    boolean update(Material material);
}
