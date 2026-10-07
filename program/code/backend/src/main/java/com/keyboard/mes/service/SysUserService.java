package com.keyboard.mes.service;

import com.keyboard.mes.entity.SysUser;

import java.util.ArrayList;

/**
 * 系统用户表业务接口。
 *
 * @author Keyboard MES项目组
 */
public interface SysUserService {

    /**
     * 查询系统用户列表。
     *
     * @return 系统用户列表
     */
    ArrayList<SysUser> sysUserList();

    /**
     * 保存系统用户。
     *
     * @param sysUser 系统用户数据
     * @return 操作是否成功
     */
    boolean save(SysUser sysUser);

    /**
     * 按主键查询系统用户。
     *
     * @param id 记录主键
     * @return 查询或处理后的系统用户
     */
    SysUser getSysUserById(Long id);

    /**
     * 按主键删除系统用户。
     *
     * @param id 记录主键
     * @return 操作是否成功
     */
    boolean delete(Long id);

    /**
     * 更新系统用户。
     *
     * @param sysUser 系统用户数据
     * @return 操作是否成功
     */
    boolean update(SysUser sysUser);
}
