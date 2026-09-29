package com.keyboard.mes.controller;

import com.keyboard.mes.dto.Result;
import com.keyboard.mes.entity.InspectionRecord;
import com.keyboard.mes.entity.ReworkOrder;
import com.keyboard.mes.service.ReworkOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * 返修工单表接口。
 *
 * <p>参考 code-files 项目的 Controller 写法，提供列表、新增、查询、删除和更新接口。</p>
 */
@RestController
@RequestMapping({"/reworkOrder", "/api/reworkOrder"})
public class ReworkOrderController {

    @Autowired
    private ReworkOrderService reworkOrderService;

    /** 查询返修工单表列表。 */
    @GetMapping("/list")
    public Result reworkOrderList() {
        ArrayList<ReworkOrder> records = reworkOrderService.reworkOrderList();
        return Result.success(records);
    }

    /** 新增返修工单表。 */
    @PostMapping("/add")
    public Result save(@Valid @RequestBody ReworkOrder reworkOrder) {
        boolean flag = reworkOrderService.save(reworkOrder);
        if (flag) {
            return Result.success("添加成功");
        }
        return Result.fail("添加失败");
    }

    /** 根据主键查询返修工单表。 */
    @GetMapping("/{id}")
    public Result getReworkOrder(@PathVariable("id") Long id) {
        ReworkOrder record = reworkOrderService.getReworkOrderById(id);
        if (record != null) {
            return Result.success(record);
        }
        return Result.fail("数据不存在");
    }

    /** 根据主键删除返修工单表。 */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        boolean flag = reworkOrderService.delete(id);
        if (flag) {
            return Result.success("删除成功");
        }
        return Result.fail("删除失败");
    }

    /** 根据主键更新返修工单表。 */
    @PutMapping("/{id}")
    public Result update(@PathVariable("id") Long id, @Valid @RequestBody ReworkOrder reworkOrder) {
        reworkOrder.setId(id);
        boolean flag = reworkOrderService.update(reworkOrder);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }

    /** 兼容参考项目中的 /update 写法。 */
    @PostMapping("/update")
    public Result update(@Valid @RequestBody ReworkOrder reworkOrder) {
        boolean flag = reworkOrderService.update(reworkOrder);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }

    /** 记录返修维修结果。 */
    @PostMapping("/{id}/repair")
    public Result repair(@PathVariable("id") Long id, @RequestBody ReworkOrder reworkOrder) {
        return Result.success("返修处理成功", reworkOrderService.repair(id, reworkOrder));
    }

    /** 提交返修复检结果。 */
    @PostMapping("/{id}/recheck")
    public Result recheck(@PathVariable("id") Long id, @Valid @RequestBody InspectionRecord inspectionRecord) {
        return Result.success("返修复检成功", reworkOrderService.recheck(id, inspectionRecord));
    }
}
