package com.keyboard.mes.controller;

import com.keyboard.mes.dto.Result;
import com.keyboard.mes.entity.ProductModel;
import com.keyboard.mes.service.ProductModelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * 产品型号表接口。
 *
 * <p>参考 code-files 项目的 Controller 写法，提供列表、新增、查询、删除和更新接口。</p>
 *
 * @author Keyboard MES项目组
 */
@RestController
@RequestMapping({"/productModel", "/api/productModel"})
public class ProductModelController {

    @Autowired
    private ProductModelService productModelService;

    /** 查询产品型号表列表。 */
    @GetMapping("/list")
    public Result productModelList() {
        ArrayList<ProductModel> records = productModelService.productModelList();
        return Result.success(records);
    }

    /** 新增产品型号表。 */
    @PostMapping("/add")
    public Result save(@Valid @RequestBody ProductModel productModel) {
        boolean flag = productModelService.save(productModel);
        if (flag) {
            return Result.success("添加成功");
        }
        return Result.fail("添加失败");
    }

    /** 根据主键查询产品型号表。 */
    @GetMapping("/{id}")
    public Result getProductModel(@PathVariable("id") Long id) {
        ProductModel record = productModelService.getProductModelById(id);
        if (record != null) {
            return Result.success(record);
        }
        return Result.fail("数据不存在");
    }

    /** 根据主键删除产品型号表。 */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        boolean flag = productModelService.delete(id);
        if (flag) {
            return Result.success("删除成功");
        }
        return Result.fail("删除失败");
    }

    /** 根据主键更新产品型号表。 */
    @PutMapping("/{id}")
    public Result update(@PathVariable("id") Long id, @Valid @RequestBody ProductModel productModel) {
        productModel.setId(id);
        boolean flag = productModelService.update(productModel);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }

    /** 兼容参考项目中的 /update 写法。 */
    @PostMapping("/update")
    public Result update(@Valid @RequestBody ProductModel productModel) {
        boolean flag = productModelService.update(productModel);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }
}
