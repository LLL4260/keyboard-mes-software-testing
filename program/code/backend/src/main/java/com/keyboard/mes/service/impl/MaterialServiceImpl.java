package com.keyboard.mes.service.impl;

import com.keyboard.mes.entity.Material;
import com.keyboard.mes.repository.MaterialMapper;
import com.keyboard.mes.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 物料表业务实现。
 *
 * @author Keyboard MES项目组
 */
@Service
public class MaterialServiceImpl implements MaterialService {

    @Autowired
    private MaterialMapper materialMapper;

    @Override
    public ArrayList<Material> materialList() {
        return materialMapper.materialList();
    }

    @Override
    public boolean save(Material material) {
        int num = materialMapper.insert(material);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Material getMaterialById(Long id) {
        return materialMapper.getById(id);
    }

    @Override
    public boolean delete(Long id) {
        int num = materialMapper.delete(id);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean update(Material material) {
        int num = materialMapper.update(material);
        if (num > 0) {
            return true;
        }
        return false;
    }
}
