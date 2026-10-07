package com.keyboard.mes.repository;

import com.keyboard.mes.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 系统用户表数据访问接口。
 *
 * @author Keyboard MES项目组
 */
@Repository
@Mapper
public interface SysUserMapper {

    /**
     * 查询系统用户列表。
     *
     * @return 系统用户列表
     */
    ArrayList<SysUser> sysUserList();

    /**
     * 新增系统用户。
     *
     * @param sysUser 系统用户数据
     * @return 受影响的记录数
     */
    int insert(SysUser sysUser);

    /**
     * 按主键查询系统用户。
     *
     * @param id 记录主键
     * @return 查询或处理后的系统用户
     */
    SysUser getById(@Param("id") Long id);

    /**
     * 按工号查询系统用户。
     *
     * @param employeeNo 员工工号
     * @return 查询或处理后的系统用户
     */
    SysUser getByEmployeeNo(@Param("employeeNo") String employeeNo);

    /**
     * 更新系统用户。
     *
     * @param sysUser 系统用户数据
     * @return 受影响的记录数
     */
    int update(SysUser sysUser);

    /**
     * 按主键删除系统用户。
     *
     * @param id 记录主键
     * @return 受影响的记录数
     */
    int delete(@Param("id") Long id);
}
