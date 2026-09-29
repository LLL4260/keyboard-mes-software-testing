package com.keyboard.mes.controller;

import com.keyboard.mes.dto.Result;
import com.keyboard.mes.entity.InspectionRecord;
import com.keyboard.mes.service.InspectionRecordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 检验记录表接口。
 *
 * <p>参考 code-files 项目的 Controller 写法，提供列表、新增、查询、删除和更新接口。</p>
 */
@RestController
@RequestMapping({"/inspectionRecord", "/api/inspectionRecord"})
public class InspectionRecordController {

    @Autowired
    private InspectionRecordService inspectionRecordService;

    /** 查询检验记录表列表。 */
    @GetMapping("/list")
    public Result inspectionRecordList() {
        ArrayList<InspectionRecord> records = inspectionRecordService.inspectionRecordList();
        return Result.success(records);
    }

    /** 查询移动质检可选择的已完工/待检任务编号及关联工单编号。 */
    @GetMapping("/options")
    public Result inspectionOptions() {
        List<Map<String, Object>> options = inspectionRecordService.inspectionOptions();
        return Result.success(options);
    }

    /** 新增检验记录表。 */
    @PostMapping("/add")
    public Result save(@Valid @RequestBody InspectionRecord inspectionRecord) {
        boolean flag = inspectionRecordService.save(inspectionRecord);
        if (flag) {
            return Result.success("添加成功");
        }
        return Result.fail("添加失败");
    }

    /** 根据主键查询检验记录表。 */
    @GetMapping("/{id}")
    public Result getInspectionRecord(@PathVariable("id") Long id) {
        InspectionRecord record = inspectionRecordService.getInspectionRecordById(id);
        if (record != null) {
            return Result.success(record);
        }
        return Result.fail("数据不存在");
    }

    /** 根据主键删除检验记录表。 */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        boolean flag = inspectionRecordService.delete(id);
        if (flag) {
            return Result.success("删除成功");
        }
        return Result.fail("删除失败");
    }

    /** 根据主键更新检验记录表。 */
    @PutMapping("/{id}")
    public Result update(@PathVariable("id") Long id, @Valid @RequestBody InspectionRecord inspectionRecord) {
        inspectionRecord.setId(id);
        boolean flag = inspectionRecordService.update(inspectionRecord);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }

    /** 兼容参考项目中的 /update 写法。 */
    @PostMapping("/update")
    public Result update(@Valid @RequestBody InspectionRecord inspectionRecord) {
        boolean flag = inspectionRecordService.update(inspectionRecord);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }

    /** 提交质检并联动返修和任务状态。 */
    @PostMapping("/submit")
    public Result submit(@Valid @RequestBody InspectionRecord inspectionRecord) {
        return Result.success("质检提交成功", inspectionRecordService.submit(inspectionRecord));
    }
}
