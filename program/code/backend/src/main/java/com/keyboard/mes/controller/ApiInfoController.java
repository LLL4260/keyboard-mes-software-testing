package com.keyboard.mes.controller;

import com.keyboard.mes.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API 元信息接口。
 *
 * <p>供前端联调时快速确认后端服务状态、统一响应结构和可用资源接口。</p>
 */
@RestController
@RequestMapping("/api")
public class ApiInfoController {

    /**
     * 后端健康检查接口。
     *
     * @return 服务状态
     */
    @GetMapping("/health")
    public Result health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("service", "keyboard-mes");
        data.put("time", LocalDateTime.now());
        return Result.success(data);
    }

    /**
     * 查询前端可调用的 API 入口。
     *
     * @return API 元信息
     */
    @GetMapping("/endpoints")
    public Result endpoints() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("baseUrl", "/api");
        data.put("auth", authContract());
        data.put("response", responseContract());
        data.put("businessActions", businessActions());
        data.put("resources", resources());
        return Result.success(data);
    }

    private Map<String, Object> authContract() {
        Map<String, Object> auth = new LinkedHashMap<>();
        auth.put("login", "POST /api/auth/login");
        auth.put("logout", "POST /api/auth/logout");
        auth.put("current", "GET /api/auth/current");
        auth.put("session", "登录会话保存在 Redis 中，前端请求需要携带 Cookie 或 Authorization sessionId");
        return auth;
    }

    private Map<String, Object> responseContract() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", "200 表示成功，400 表示参数或业务错误，401 表示未登录，403 表示无权限，500 表示系统异常");
        response.put("msg", "接口提示信息");
        response.put("data", "接口返回数据");
        return response;
    }

    private List<Map<String, String>> resources() {
        List<Map<String, String>> resources = new ArrayList<>();
        resources.add(resource("系统用户", "/api/sysUser"));
        resources.add(resource("产品型号", "/api/productModel"));
        resources.add(resource("物料", "/api/material"));
        resources.add(resource("产品BOM", "/api/productBom"));
        resources.add(resource("工艺路线", "/api/processRoute"));
        resources.add(actionResource("生产工单", "/api/productionOrder", "POST /api/productionOrder/{id}/generateTasks"));
        resources.add(actionResource("生产任务", "/api/productionTask", "POST /api/productionTask/{id}/start|pause|finish"));
        resources.add(actionResource("报工记录", "/api/workReport", "POST /api/workReport/submit"));
        resources.add(actionResource("检验记录", "/api/inspectionRecord", "POST /api/inspectionRecord/submit"));
        resources.add(actionResource("返修工单", "/api/reworkOrder", "POST /api/reworkOrder/{id}/repair 或 POST /api/reworkOrder/{id}/recheck"));
        resources.add(readonlyResource("产品追溯", "/api/trace/{productSn}"));
        resources.add(readonlyResource("轻量报表", "/api/report", "GET /api/report/overview | GET /api/report/quality | GET /api/report/order/{id}"));
        return resources;
    }

    private List<Map<String, String>> businessActions() {
        List<Map<String, String>> actions = new ArrayList<>();
        actions.add(action("工单生成任务", "POST", "/api/productionOrder/{id}/generateTasks", "按工单绑定的工艺路线生成生产任务"));
        actions.add(action("任务开工", "POST", "/api/productionTask/{id}/start", "任务状态进入生产中"));
        actions.add(action("任务暂停", "POST", "/api/productionTask/{id}/pause", "任务状态进入暂停"));
        actions.add(action("任务完工", "POST", "/api/productionTask/{id}/finish", "任务状态进入已完工"));
        actions.add(action("报工提交", "POST", "/api/workReport/submit", "保存报工并联动任务和工单状态"));
        actions.add(action("质检提交", "POST", "/api/inspectionRecord/submit", "保存质检；不合格时自动生成返修单"));
        actions.add(action("返修处理", "POST", "/api/reworkOrder/{id}/repair", "记录维修措施、换料、工时和维修结果"));
        actions.add(action("返修复检", "POST", "/api/reworkOrder/{id}/recheck", "保存复检记录；通过后恢复任务和工单状态"));
        actions.add(action("工单跟踪报表", "GET", "/api/report/order/{id}", "按生产工单聚合成品完成数、合格数、不良数、工序进度和待办提醒"));
        return actions;
    }

    private Map<String, String> resource(String name, String path) {
        Map<String, String> resource = new LinkedHashMap<>();
        resource.put("name", name);
        resource.put("path", path);
        resource.put("list", "GET " + path + "/list");
        resource.put("add", "POST " + path + "/add");
        resource.put("detail", "GET " + path + "/{id}");
        resource.put("update", "PUT " + path + "/{id} 或 POST " + path + "/update");
        resource.put("delete", "DELETE " + path + "/{id}");
        return resource;
    }

    private Map<String, String> actionResource(String name, String path, String action) {
        Map<String, String> resource = resource(name, path);
        resource.put("action", action);
        return resource;
    }

    private Map<String, String> action(String name, String method, String path, String description) {
        Map<String, String> action = new LinkedHashMap<>();
        action.put("name", name);
        action.put("method", method);
        action.put("path", path);
        action.put("description", description);
        return action;
    }

    private Map<String, String> readonlyResource(String name, String path) {
        Map<String, String> resource = new LinkedHashMap<>();
        resource.put("name", name);
        resource.put("path", path);
        resource.put("read", "GET " + path);
        return resource;
    }

    private Map<String, String> readonlyResource(String name, String path, String read) {
        Map<String, String> resource = readonlyResource(name, path);
        resource.put("read", read);
        return resource;
    }
}
