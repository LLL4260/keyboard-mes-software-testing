package com.keyboard.mes.controller;

import com.keyboard.mes.dto.Result;
import com.keyboard.mes.entity.ProcessRoute;
import com.keyboard.mes.service.ProcessRouteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * 工艺路线表接口。
 *
 * <p>参考 code-files 项目的 Controller 写法，提供列表、新增、查询、删除和更新接口。</p>
 *
 * @author Keyboard MES项目组
 */
@RestController
@RequestMapping({"/processRoute", "/api/processRoute"})
public class ProcessRouteController {

    @Autowired
    private ProcessRouteService processRouteService;

    /** 查询工艺路线表列表。 */
    @GetMapping("/list")
    public Result processRouteList() {
        ArrayList<ProcessRoute> records = processRouteService.processRouteList();
        return Result.success(records);
    }

    /** 新增工艺路线表。 */
    @PostMapping("/add")
    public Result save(@Valid @RequestBody ProcessRoute processRoute) {
        boolean flag = processRouteService.save(processRoute);
        if (flag) {
            return Result.success("添加成功");
        }
        return Result.fail("添加失败");
    }

    /** 根据主键查询工艺路线表。 */
    @GetMapping("/{id}")
    public Result getProcessRoute(@PathVariable("id") Long id) {
        ProcessRoute record = processRouteService.getProcessRouteById(id);
        if (record != null) {
            return Result.success(record);
        }
        return Result.fail("数据不存在");
    }

    /** 根据主键删除工艺路线表。 */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        boolean flag = processRouteService.delete(id);
        if (flag) {
            return Result.success("删除成功");
        }
        return Result.fail("删除失败");
    }

    /** 根据主键更新工艺路线表。 */
    @PutMapping("/{id}")
    public Result update(@PathVariable("id") Long id, @Valid @RequestBody ProcessRoute processRoute) {
        processRoute.setId(id);
        boolean flag = processRouteService.update(processRoute);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }

    /** 兼容参考项目中的 /update 写法。 */
    @PostMapping("/update")
    public Result update(@Valid @RequestBody ProcessRoute processRoute) {
        boolean flag = processRouteService.update(processRoute);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }
}
