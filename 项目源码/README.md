# Keyboard MES 课程实验源码

| 目录 | 写报告时可核对的内容 |
| --- | --- |
| [backend](backend/) | 控制器、服务、Mapper、接口契约和实验 3 的单元测试。按分工查找工单、任务、质检或鉴权对象。 |
| [frontend](frontend/) | Vue Web 端的登录、资源管理和任务操作。 |
| [miniprogram](miniprogram/) | 微信小程序的登录、看板、现场操作、数据分析和追溯页面。 |

## 单元测试

使用 JDK 17 和 Maven 3.9，从 `backend` 目录运行 `mvn test`。首次构建需要联网下载依赖。测试用例位于 `src/test/java`，执行明细和覆盖率报告分别生成在 `target/surefire-reports` 和 `target/site/jacoco/index.html`。写个人报告时，记录自己的构建、测试方法、断言、实际结果和执行日期。

## 启动本机实验环境

1. 准备 MySQL 8、Redis 和一个新建的独立实验数据库。给实验数据库用户授权访问该库。选中该库，运行[建表脚本](backend/src/main/resources/sql/schema.sql)，随后在同一数据库用一个连接完整执行[首次实验初始化](backend/src/main/resources/sql/first-lab-user.sql)。脚本仅在用户表为空时生成随机实验管理员，并添加一个示例键盘型号。最后的查询只在本次创建时返回登录信息，请在本地私下保存。
2. 在启动后端的终端设置 `DB_NAME`、`DB_USERNAME`、`DB_PASSWORD` 和 `REDIS_DATABASE`。前三项使用上一步的数据库及数据库访问凭据，Redis 选择实验专用的逻辑库编号。数据库默认连接本机 3306，Redis 默认本机 6379。其他主机、端口和 Redis 密码可分别通过 `DB_HOST`、`DB_PORT`、`REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD` 设置。在 `backend` 运行 `mvn spring-boot:run`，后端监听 8088。
3. 使用 Node.js 22（符合本项目锁文件中的 Vite 版本要求），在 `frontend` 运行 `npm ci` 和 `npm run dev`，使用终端显示的本地地址。Web 登录使用第一步生成的实验管理员。按分工准备各自的角色和业务数据，记录工单号、任务号、前置状态和实际响应，便于对照截图。
4. 涉及小程序的步骤可在微信开发者工具中导入 `miniprogram`，按本地开发环境设置测试 AppID。模拟器默认访问本机 8088 的 API；真机需要配置设备可达的测试后端地址并满足开发工具的网络要求。

初始示例只含管理员和产品型号。已有团队执行统计来自对应实验的留档。复测时保留自己的数据规模、前置配置和执行结果。
