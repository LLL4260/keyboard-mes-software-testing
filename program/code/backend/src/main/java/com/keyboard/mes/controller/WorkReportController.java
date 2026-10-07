package com.keyboard.mes.controller;

import com.keyboard.mes.dto.Result;
import com.keyboard.mes.entity.WorkReport;
import com.keyboard.mes.service.WorkReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 报工记录表接口。
 *
 * <p>参考 code-files 项目的 Controller 写法，提供列表、新增、查询、删除和更新接口。</p>
 *
 * @author Keyboard MES项目组
 */
@RestController
@RequestMapping({"/workReport", "/api/workReport"})
public class WorkReportController {

    @Autowired
    private WorkReportService workReportService;

    /** 查询报工记录表列表。 */
    @GetMapping("/list")
    public Result workReportList() {
        ArrayList<WorkReport> records = workReportService.workReportList();
        return Result.success(records);
    }

    /** 新增报工记录表。 */
    @PostMapping("/add")
    public Result save(@Valid @RequestBody WorkReport workReport) {
        boolean flag = workReportService.save(workReport);
        if (flag) {
            return Result.success("添加成功");
        }
        return Result.fail("添加失败");
    }

    /** 根据主键查询报工记录表。 */
    @GetMapping("/{id}")
    public Result getWorkReport(@PathVariable("id") Long id) {
        WorkReport record = workReportService.getWorkReportById(id);
        if (record != null) {
            return Result.success(record);
        }
        return Result.fail("数据不存在");
    }

    /** 根据主键删除报工记录表。 */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        boolean flag = workReportService.delete(id);
        if (flag) {
            return Result.success("删除成功");
        }
        return Result.fail("删除失败");
    }

    /** 根据主键更新报工记录表。 */
    @PutMapping("/{id}")
    public Result update(@PathVariable("id") Long id, @Valid @RequestBody WorkReport workReport) {
        workReport.setId(id);
        boolean flag = workReportService.update(workReport);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }

    /** 兼容参考项目中的 /update 写法。 */
    @PostMapping("/update")
    public Result update(@Valid @RequestBody WorkReport workReport) {
        boolean flag = workReportService.update(workReport);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }

    /** 查询移动报工可选择的任务编号及关联工单编号。 */
    @GetMapping("/options")
    public Result reportOptions() {
        List<Map<String, Object>> options = workReportService.reportOptions();
        return Result.success(options);
    }

    /** 提交报工并联动更新任务和工单进度。 */
    @PostMapping("/submit")
    public Result submit(@Valid @RequestBody WorkReport workReport) {
        return Result.success("报工提交成功", workReportService.submit(workReport));
    }
}
