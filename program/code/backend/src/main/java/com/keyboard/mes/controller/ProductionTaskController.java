package com.keyboard.mes.controller;

import com.keyboard.mes.dto.Result;
import com.keyboard.mes.entity.ProductionTask;
import com.keyboard.mes.service.ProductionTaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * 生产任务表接口。
 *
 * <p>参考 code-files 项目的 Controller 写法，提供列表、新增、查询、删除和更新接口。</p>
 *
 * @author Keyboard MES项目组
 */
@RestController
@RequestMapping({"/productionTask", "/api/productionTask"})
public class ProductionTaskController {

    @Autowired
    private ProductionTaskService productionTaskService;

    /** 查询生产任务表列表。 */
    @GetMapping("/list")
    public Result productionTaskList() {
        ArrayList<ProductionTask> records = productionTaskService.productionTaskList();
        return Result.success(records);
    }

    /** 新增生产任务表。 */
    @PostMapping("/add")
    public Result save(@Valid @RequestBody ProductionTask productionTask) {
        boolean flag = productionTaskService.save(productionTask);
        if (flag) {
            return Result.success("添加成功");
        }
        return Result.fail("添加失败");
    }

    /** 根据主键查询生产任务表。 */
    @GetMapping("/{id}")
    public Result getProductionTask(@PathVariable("id") Long id) {
        ProductionTask record = productionTaskService.getProductionTaskById(id);
        if (record != null) {
            return Result.success(record);
        }
        return Result.fail("数据不存在");
    }

    /** 根据主键删除生产任务表。 */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        boolean flag = productionTaskService.delete(id);
        if (flag) {
            return Result.success("删除成功");
        }
        return Result.fail("删除失败");
    }

    /** 根据主键更新生产任务表。 */
    @PutMapping("/{id}")
    public Result update(@PathVariable("id") Long id, @Valid @RequestBody ProductionTask productionTask) {
        productionTask.setId(id);
        boolean flag = productionTaskService.update(productionTask);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }

    /** 兼容参考项目中的 /update 写法。 */
    @PostMapping("/update")
    public Result update(@Valid @RequestBody ProductionTask productionTask) {
        boolean flag = productionTaskService.update(productionTask);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }

    /** 生产任务开工。 */
    @PostMapping("/{id}/start")
    public Result start(@PathVariable("id") Long id) {
        return Result.success("开工成功", productionTaskService.start(id));
    }

    /** 生产任务暂停。 */
    @PostMapping("/{id}/pause")
    public Result pause(@PathVariable("id") Long id) {
        return Result.success("暂停成功", productionTaskService.pause(id));
    }

    /** 生产任务完工。 */
    @PostMapping("/{id}/finish")
    public Result finish(@PathVariable("id") Long id) {
        return Result.success("完工成功", productionTaskService.finish(id));
    }
}
