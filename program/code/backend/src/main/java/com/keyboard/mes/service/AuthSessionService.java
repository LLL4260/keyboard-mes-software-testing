package com.keyboard.mes.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keyboard.mes.entity.SysUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 登录 Session 服务。
 *
 * <p>使用 Redis 保存前后端分离登录态，Shiro 负责认证，Redis 负责状态持久化。</p>
 *
 * @author Keyboard MES项目组
 */
@Service
public class AuthSessionService {

    public static final String COOKIE_NAME = "MES_SESSION_ID";
    public static final String HEADER_NAME = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String KEY_PREFIX = "keyboard_mes:login:";

    private static final class LoginUserTypeReference extends TypeReference<Map<String, Object>> {
    }

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.shiro.session-timeout-seconds:1800}")
    private int sessionTimeoutSeconds;

    public AuthSessionService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 创建登录态并写入浏览器 Cookie。
     *
     * @param user 当前用户
     * @param response HTTP 响应
     * @return 登录响应数据
     */
    public Map<String, Object> createSession(SysUser user, HttpServletResponse response) {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> loginUser = toLoginUser(user);
        try {
            redisTemplate.opsForValue().set(redisKey(sessionId), objectMapper.writeValueAsString(loginUser),
                    Duration.ofSeconds(sessionTimeoutSeconds));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("serialize login user failed", exception);
        }
        response.addCookie(sessionCookie(sessionId, sessionTimeoutSeconds));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sessionId", sessionId);
        data.put("user", loginUser);
        return data;
    }

    /**
     * 查询当前请求对应的登录用户。
     *
     * @param request HTTP 请求
     * @return 登录用户，不存在时返回 null
     */
    public Map<String, Object> getCurrentUser(HttpServletRequest request) {
        String sessionId = resolveSessionId(request);
        if (!StringUtils.hasText(sessionId)) {
            return null;
        }
        String key = redisKey(sessionId);
        String value = redisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        redisTemplate.expire(key, Duration.ofSeconds(sessionTimeoutSeconds));
        try {
            return objectMapper.readValue(value, new LoginUserTypeReference());
        } catch (JsonProcessingException exception) {
            return null;
        }
    }

    /**
     * 销毁登录态。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     */
    public void destroySession(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = resolveSessionId(request);
        if (StringUtils.hasText(sessionId)) {
            redisTemplate.delete(redisKey(sessionId));
        }
        response.addCookie(sessionCookie("", 0));
    }

    private String resolveSessionId(HttpServletRequest request) {
        String authorization = request.getHeader(HEADER_NAME);
        if (StringUtils.hasText(authorization)) {
            if (authorization.startsWith(BEARER_PREFIX)) {
                return authorization.substring(BEARER_PREFIX.length());
            }
            return authorization;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String redisKey(String sessionId) {
        return KEY_PREFIX + sessionId;
    }

    private Cookie sessionCookie(String value, int maxAge) {
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    private Map<String, Object> toLoginUser(SysUser user) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("employeeNo", user.getEmployeeNo());
        data.put("name", user.getName());
        data.put("roleCode", user.getRoleCode());
        data.put("roleName", user.getRoleName());
        data.put("department", user.getDepartment());
        data.put("position", user.getPosition());
        data.put("status", user.getStatus());
        return data;
    }
}
