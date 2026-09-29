package com.keyboard.mes.repository;

import com.keyboard.mes.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 系统用户表数据访问接口。
 */
@Repository
@Mapper
public interface SysUserMapper {

    ArrayList<SysUser> sysUserList();

    int insert(SysUser sysUser);

    SysUser getById(@Param("id") Long id);

    SysUser getByEmployeeNo(@Param("employeeNo") String employeeNo);

    int update(SysUser sysUser);

    int delete(@Param("id") Long id);
}
