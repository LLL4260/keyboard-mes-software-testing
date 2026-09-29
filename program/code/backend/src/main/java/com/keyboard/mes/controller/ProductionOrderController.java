package com.keyboard.mes.controller;

import com.keyboard.mes.dto.Result;
import com.keyboard.mes.entity.ProductionOrder;
import com.keyboard.mes.service.ProductionOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * 生产工单表接口。
 *
 * <p>参考 code-files 项目的 Controller 写法，提供列表、新增、查询、删除和更新接口。</p>
 */
@RestController
@RequestMapping({"/productionOrder", "/api/productionOrder"})
public class ProductionOrderController {

    @Autowired
    private ProductionOrderService productionOrderService;

    /** 查询生产工单表列表。 */
    @GetMapping("/list")
    public Result productionOrderList() {
        ArrayList<ProductionOrder> records = productionOrderService.productionOrderList();
        return Result.success(records);
    }

    /** 新增生产工单表。 */
    @PostMapping("/add")
    public Result save(@Valid @RequestBody ProductionOrder productionOrder) {
        boolean flag = productionOrderService.save(productionOrder);
        if (flag) {
            return Result.success("添加成功");
        }
        return Result.fail("添加失败");
    }

    /** 根据主键查询生产工单表。 */
    @GetMapping("/{id}")
    public Result getProductionOrder(@PathVariable("id") Long id) {
        ProductionOrder record = productionOrderService.getProductionOrderById(id);
        if (record != null) {
            return Result.success(record);
        }
        return Result.fail("数据不存在");
    }

    /** 根据主键删除生产工单表。 */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        boolean flag = productionOrderService.delete(id);
        if (flag) {
            return Result.success("删除成功");
        }
        return Result.fail("删除失败");
    }

    /** 根据主键更新生产工单表。 */
    @PutMapping("/{id}")
    public Result update(@PathVariable("id") Long id, @Valid @RequestBody ProductionOrder productionOrder) {
        productionOrder.setId(id);
        boolean flag = productionOrderService.update(productionOrder);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }

    /** 兼容参考项目中的 /update 写法。 */
    @PostMapping("/update")
    public Result update(@Valid @RequestBody ProductionOrder productionOrder) {
        boolean flag = productionOrderService.update(productionOrder);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }

    /** 根据工艺路线生成生产任务。 */
    @PostMapping("/{id}/generateTasks")
    public Result generateTasks(@PathVariable("id") Long id) {
        return Result.success("生成任务成功", productionOrderService.generateTasks(id));
    }
}
