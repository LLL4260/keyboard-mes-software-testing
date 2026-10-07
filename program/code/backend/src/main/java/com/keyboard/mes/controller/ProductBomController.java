package com.keyboard.mes.controller;

import com.keyboard.mes.dto.Result;
import com.keyboard.mes.entity.ProductBom;
import com.keyboard.mes.service.ProductBomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * 产品BOM表接口。
 *
 * <p>参考 code-files 项目的 Controller 写法，提供列表、新增、查询、删除和更新接口。</p>
 *
 * @author Keyboard MES项目组
 */
@RestController
@RequestMapping({"/productBom", "/api/productBom"})
public class ProductBomController {

    @Autowired
    private ProductBomService productBomService;

    /** 查询产品BOM表列表。 */
    @GetMapping("/list")
    public Result productBomList() {
        ArrayList<ProductBom> records = productBomService.productBomList();
        return Result.success(records);
    }

    /** 新增产品BOM表。 */
    @PostMapping("/add")
    public Result save(@Valid @RequestBody ProductBom productBom) {
        boolean flag = productBomService.save(productBom);
        if (flag) {
            return Result.success("添加成功");
        }
        return Result.fail("添加失败");
    }

    /** 根据主键查询产品BOM表。 */
    @GetMapping("/{id}")
    public Result getProductBom(@PathVariable("id") Long id) {
        ProductBom record = productBomService.getProductBomById(id);
        if (record != null) {
            return Result.success(record);
        }
        return Result.fail("数据不存在");
    }

    /** 根据主键删除产品BOM表。 */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        boolean flag = productBomService.delete(id);
        if (flag) {
            return Result.success("删除成功");
        }
        return Result.fail("删除失败");
    }

    /** 根据主键更新产品BOM表。 */
    @PutMapping("/{id}")
    public Result update(@PathVariable("id") Long id, @Valid @RequestBody ProductBom productBom) {
        productBom.setId(id);
        boolean flag = productBomService.update(productBom);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }

    /** 兼容参考项目中的 /update 写法。 */
    @PostMapping("/update")
    public Result update(@Valid @RequestBody ProductBom productBom) {
        boolean flag = productBomService.update(productBom);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }
}
