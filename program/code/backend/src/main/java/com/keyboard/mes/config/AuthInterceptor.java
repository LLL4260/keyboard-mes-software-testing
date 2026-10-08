package com.keyboard.mes.config;

import com.keyboard.mes.service.AuthSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Locale;
import java.util.Set;

/**
 * 登录态拦截器。
 *
 * <p>替代 Shiro Web Filter，适配 Spring Boot 3 的 Jakarta Servlet。</p>
 *
 * @author Keyboard MES项目组
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String ROLE_ADMIN = "admin";
    private static final String TRACE_PATH_PREFIX = "/api/trace";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_DELETE = "DELETE";
    private static final String UPDATE_PATH_SUFFIX = "/update";
    private static final String METHOD_PUT = "PUT";
    private static final String TASK_ACTION_PATH_PATTERN = ".*/productionTask/\\d+/(start|pause|finish)$";
    private static final String REWORK_ACTION_PATH_PATTERN = ".*/reworkOrder/\\d+/(repair|recheck)$";
    private static final String METHOD_POST = "POST";


    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private final AuthSessionService authSessionService;
    private final Map<String, Map<String, Set<String>>> roleRules = buildRoleRules();

    public AuthInterceptor(AuthSessionService authSessionService) {
        this.authSessionService = authSessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (METHOD_OPTIONS.equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        Map<String, Object> currentUser = authSessionService.getCurrentUser(request);
        if (currentUser != null) {
            request.setAttribute("currentUser", currentUser);
            if (isAllowed(request, currentUser)) {
                return true;
            }
            log.warn("权限拦截：user={}, method={}, path={}",
                    currentUser.get("employeeNo"), request.getMethod(), request.getRequestURI());
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, "无权限访问");
            return false;
        }
        log.warn("未登录访问：method={}, path={}", request.getMethod(), request.getRequestURI());
        writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
        return false;
    }

    private boolean isAllowed(HttpServletRequest request, Map<String, Object> currentUser) {
        String roleCode = String.valueOf(currentUser.getOrDefault("roleCode", "")).trim().toLowerCase(Locale.ROOT);
        if (ROLE_ADMIN.equals(roleCode)) {
            return true;
        }
        String resource = resolveResource(request);
        Map<String, Set<String>> actionRules = roleRules.get(resource);
        if (actionRules == null) {
            return false;
        }
        Set<String> allowedRoles = actionRules.getOrDefault(resolveAction(request), Collections.emptySet());
        return allowedRoles.contains(roleCode);
    }

    private String resolveResource(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        if (path.startsWith(TRACE_PATH_PREFIX)) {
            return "trace";
        }
        String normalized = path.startsWith("/api/") ? path.substring(5) : path.substring(1);
        int slashIndex = normalized.indexOf('/');
        return slashIndex >= 0 ? normalized.substring(0, slashIndex) : normalized;
    }

    private String resolveAction(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if (METHOD_GET.equalsIgnoreCase(method)) {
            return "read";
        }
        if (METHOD_DELETE.equalsIgnoreCase(method)) {
            return "delete";
        }
        if (METHOD_PUT.equalsIgnoreCase(method) || path.endsWith(UPDATE_PATH_SUFFIX)) {
            return "update";
        }
        if (path.matches(TASK_ACTION_PATH_PATTERN)) {
            return "taskAction";
        }
        if (path.matches(REWORK_ACTION_PATH_PATTERN)) {
            return "update";
        }
        if (METHOD_POST.equalsIgnoreCase(method)) {
            return "create";
        }
        return "read";
    }

    private void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + status + ",\"msg\":\"" + message + "\",\"data\":null}");
    }

    private Map<String, Map<String, Set<String>>> buildRoleRules() {
        Map<String, Map<String, Set<String>>> rules = new HashMap<>(32);
        allow(rules, "trace", allActions(), roles("planner", "operator", "inspector", "repair"));
        allow(rules, "report", actions("read"), roles("planner", "operator", "inspector", "repair"));
        allow(rules, "sysUser", actions("read", "create", "update", "delete"), roles());
        allow(rules, "productModel", allActions(), roles("planner"));
        allow(rules, "material", allActions(), roles("planner"));
        allow(rules, "productBom", allActions(), roles("planner"));
        allow(rules, "processRoute", allActions(), roles("planner"));
        allow(rules, "productionOrder", allActions(), roles("planner"));
        allow(rules, "productionTask", actions("read", "update", "taskAction"), roles("planner", "operator"));
        allow(rules, "productionTask", actions("create", "delete"), roles("planner"));
        allow(rules, "workReport", actions("read", "create", "update"), roles("operator"));
        allow(rules, "inspectionRecord", actions("read", "create", "update"), roles("inspector"));
        allow(rules, "inspectionRecord", actions("read"), roles("repair"));
        allow(rules, "reworkOrder", actions("read", "create", "update"), roles("inspector"));
        allow(rules, "reworkOrder", actions("read", "update"), roles("repair"));
        return rules;
    }

    private void allow(Map<String, Map<String, Set<String>>> rules, String resource, Set<String> actions, Set<String> roles) {
        Map<String, Set<String>> actionRules = rules.computeIfAbsent(resource, key -> new HashMap<>(8));
        for (String action : actions) {
            actionRules.computeIfAbsent(action, key -> new HashSet<>()).addAll(roles);
        }
    }

    private Set<String> allActions() {
        return actions("read", "create", "update", "delete", "taskAction");
    }

    private Set<String> actions(String... actions) {
        return new HashSet<>(Arrays.asList(actions));
    }

    private Set<String> roles(String... roles) {
        return new HashSet<>(Arrays.asList(roles));
    }
}
