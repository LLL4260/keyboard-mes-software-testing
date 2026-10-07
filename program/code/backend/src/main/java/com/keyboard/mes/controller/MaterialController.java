package com.keyboard.mes.controller;

import com.keyboard.mes.dto.Result;
import com.keyboard.mes.entity.Material;
import com.keyboard.mes.service.MaterialService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * 物料表接口。
 *
 * <p>参考 code-files 项目的 Controller 写法，提供列表、新增、查询、删除和更新接口。</p>
 *
 * @author Keyboard MES项目组
 */
@RestController
@RequestMapping({"/material", "/api/material"})
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    /** 查询物料表列表。 */
    @GetMapping("/list")
    public Result materialList() {
        ArrayList<Material> records = materialService.materialList();
        return Result.success(records);
    }

    /** 新增物料表。 */
    @PostMapping("/add")
    public Result save(@Valid @RequestBody Material material) {
        boolean flag = materialService.save(material);
        if (flag) {
            return Result.success("添加成功");
        }
        return Result.fail("添加失败");
    }

    /** 根据主键查询物料表。 */
    @GetMapping("/{id}")
    public Result getMaterial(@PathVariable("id") Long id) {
        Material record = materialService.getMaterialById(id);
        if (record != null) {
            return Result.success(record);
        }
        return Result.fail("数据不存在");
    }

    /** 根据主键删除物料表。 */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        boolean flag = materialService.delete(id);
        if (flag) {
            return Result.success("删除成功");
        }
        return Result.fail("删除失败");
    }

    /** 根据主键更新物料表。 */
    @PutMapping("/{id}")
    public Result update(@PathVariable("id") Long id, @Valid @RequestBody Material material) {
        material.setId(id);
        boolean flag = materialService.update(material);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }

    /** 兼容参考项目中的 /update 写法。 */
    @PostMapping("/update")
    public Result update(@Valid @RequestBody Material material) {
        boolean flag = materialService.update(material);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }
}
