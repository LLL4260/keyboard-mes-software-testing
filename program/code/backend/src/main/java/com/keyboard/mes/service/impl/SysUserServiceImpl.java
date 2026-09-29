package com.keyboard.mes.service.impl;

import com.keyboard.mes.entity.SysUser;
import com.keyboard.mes.repository.SysUserMapper;
import com.keyboard.mes.service.SysUserService;
import com.keyboard.mes.util.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 系统用户表业务实现。
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public ArrayList<SysUser> sysUserList() {
        return sysUserMapper.sysUserList();
    }

    @Override
    public boolean save(SysUser sysUser) {
        sysUser.setPasswordHash(PasswordUtils.encodeIfPlainText(sysUser.getPasswordHash()));
        int num = sysUserMapper.insert(sysUser);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public SysUser getSysUserById(Long id) {
        return sysUserMapper.getById(id);
    }

    @Override
    public boolean delete(Long id) {
        int num = sysUserMapper.delete(id);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean update(SysUser sysUser) {
        sysUser.setPasswordHash(PasswordUtils.encodeIfPlainText(sysUser.getPasswordHash()));
        int num = sysUserMapper.update(sysUser);
        if (num > 0) {
            return true;
        }
        return false;
    }
}
