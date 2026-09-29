package com.keyboard.mes.shiro;

import com.keyboard.mes.entity.SysUser;
import com.keyboard.mes.repository.SysUserMapper;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * MES 用户认证和授权 Realm。
 *
 * <p>认证数据来自 sys_user 表，角色直接使用 role_code 字段。</p>
 */
public class MesShiroRealm extends AuthorizingRealm {

    private final SysUserMapper sysUserMapper;

    public MesShiroRealm(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
        setAuthenticationTokenClass(UsernamePasswordToken.class);
        // 登录认证必须读取数据库最新密码，避免 Redis 中旧认证缓存导致账号密码误判。
        setAuthenticationCachingEnabled(false);
        setAuthorizationCachingEnabled(true);
    }

    /**
     * 查询用户角色和权限。
     *
     * @param principals 登录主体
     * @return 授权信息
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SysUser user = (SysUser) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        if (user == null || user.getRoleCode() == null) {
            return info;
        }
        info.addRole(user.getRoleCode());
        if ("admin".equals(user.getRoleCode())) {
            info.addStringPermission("*");
        } else {
            info.addStringPermission("mes:" + user.getRoleCode());
        }
        return info;
    }

    /**
     * 登录认证：根据工号查用户，把数据库密码摘要交给 Shiro 凭证匹配器校验。
     *
     * @param token 登录令牌
     * @return 认证信息
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;
        String employeeNo = usernamePasswordToken.getUsername();
        SysUser user = sysUserMapper.getByEmployeeNo(employeeNo);
        if (user == null) {
            throw new UnknownAccountException("账号不存在");
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new LockedAccountException("账号已停用");
        }
        return new SimpleAuthenticationInfo(user, user.getPasswordHash(), getName());
    }
}
