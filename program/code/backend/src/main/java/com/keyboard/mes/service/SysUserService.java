package com.keyboard.mes.service;

import com.keyboard.mes.entity.SysUser;

import java.util.ArrayList;

/**
 * 系统用户表业务接口。
 */
public interface SysUserService {

    ArrayList<SysUser> sysUserList();

    boolean save(SysUser sysUser);

    SysUser getSysUserById(Long id);

    boolean delete(Long id);

    boolean update(SysUser sysUser);
}
