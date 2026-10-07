package com.keyboard.mes.controller;

import com.keyboard.mes.dto.LoginRequest;
import com.keyboard.mes.dto.Result;
import com.keyboard.mes.entity.SysUser;
import com.keyboard.mes.service.AuthSessionService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 登录认证接口。
 *
 * <p>Shiro 负责认证，登录态通过 Redis 保存。</p>
 *
 * @author Keyboard MES项目组
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SecurityManager securityManager;
    private final AuthSessionService authSessionService;

    public AuthController(SecurityManager securityManager, AuthSessionService authSessionService) {
        this.securityManager = securityManager;
        this.authSessionService = authSessionService;
    }

    /**
     * 用户登录。
     *
     * @param request 登录参数
     * @return 当前登录用户和 sessionId
     */
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        if (request == null || !StringUtils.hasText(request.getEmployeeNo()) || !StringUtils.hasText(request.getPassword())) {
            return Result.fail(400, "账号和密码不能为空");
        }
        Subject subject = new Subject.Builder(securityManager).buildSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(request.getEmployeeNo(), request.getPassword());
        try {
            subject.login(token);
            SysUser user = (SysUser) subject.getPrincipal();
            Map<String, Object> data = authSessionService.createSession(user, response);
            return Result.success("登录成功", data);
        } catch (AuthenticationException exception) {
            return Result.fail(401, "账号或密码错误");
        }
    }

    /**
     * 查询当前登录用户。
     *
     * @return 当前登录用户
     */
    @GetMapping("/current")
    public Result current(HttpServletRequest request) {
        Map<String, Object> user = authSessionService.getCurrentUser(request);
        if (user == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(user);
    }

    /**
     * 用户退出登录。
     *
     * @return 退出结果
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request, HttpServletResponse response) {
        authSessionService.destroySession(request, response);
        return Result.success("退出成功");
    }

    /**
     * Shiro 未登录兜底接口。
     *
     * @return 未登录提示
     */
    @GetMapping("/unauthenticated")
    public Result unauthenticated() {
        return Result.fail(401, "请先登录");
    }

}
