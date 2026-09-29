package com.keyboard.mes.controller;

import com.keyboard.mes.dto.Result;
import com.keyboard.mes.entity.SysUser;
import com.keyboard.mes.service.SysUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * 系统用户表接口。
 *
 * <p>参考 code-files 项目的 Controller 写法，提供列表、新增、查询、删除和更新接口。</p>
 */
@RestController
@RequestMapping({"/sysUser", "/api/sysUser"})
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /** 查询系统用户表列表。 */
    @GetMapping("/list")
    public Result sysUserList() {
        ArrayList<SysUser> records = sysUserService.sysUserList();
        return Result.success(records);
    }

    /** 新增系统用户表。 */
    @PostMapping("/add")
    public Result save(@Valid @RequestBody SysUser sysUser) {
        boolean flag = sysUserService.save(sysUser);
        if (flag) {
            return Result.success("添加成功");
        }
        return Result.fail("添加失败");
    }

    /** 根据主键查询系统用户表。 */
    @GetMapping("/{id}")
    public Result getSysUser(@PathVariable("id") Long id) {
        SysUser record = sysUserService.getSysUserById(id);
        if (record != null) {
            return Result.success(record);
        }
        return Result.fail("数据不存在");
    }

    /** 根据主键删除系统用户表。 */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        boolean flag = sysUserService.delete(id);
        if (flag) {
            return Result.success("删除成功");
        }
        return Result.fail("删除失败");
    }

    /** 根据主键更新系统用户表。 */
    @PutMapping("/{id}")
    public Result update(@PathVariable("id") Long id, @Valid @RequestBody SysUser sysUser) {
        sysUser.setId(id);
        boolean flag = sysUserService.update(sysUser);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }

    /** 兼容参考项目中的 /update 写法。 */
    @PostMapping("/update")
    public Result update(@Valid @RequestBody SysUser sysUser) {
        boolean flag = sysUserService.update(sysUser);
        if (flag) {
            return Result.success("更新成功");
        }
        return Result.fail("更新失败");
    }
}
