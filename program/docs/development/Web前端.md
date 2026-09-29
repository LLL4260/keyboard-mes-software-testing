# Keyboard MES Web 前端

Vue 3 + Vue Router + Vite 构建的 MES 管理台。

前端目录为 `program/code/frontend`，以下命令在该目录执行。

## 启动与构建

```powershell
npm ci
npm run dev
```

访问 `http://localhost:5173`。当前接口客户端直接请求 `http://localhost:8088/api`。

```powershell
npm run build
```

构建结果生成在 `dist/`，该目录不纳入源码交付包。

## 主要结构

```text
src/App.vue                 应用壳与路由出口
src/app.js                  Vue 入口
src/router.js               登录、工作台、报表、追溯路由
src/access.js               角色可见范围与字段权限
src/api.js                  Result 处理、Authorization Session、401 处理
src/domain.js               资源、字段、字典与外键关系
src/workflow.js             业务工作台入口
src/views/LoginView.vue     登录页
src/views/WorkspaceView.vue 管理、报表和追溯主界面
assets/styles.css           全局视觉样式
```

Web 登录成功后将 `MES_SESSION_ID` 保存到当前标签页的 `sessionStorage`，后续请求使用 `Authorization: Bearer <sessionId>`。小程序则使用后端返回的 `MES_SESSION_ID` Cookie。后端角色拦截仍是权限安全边界。
