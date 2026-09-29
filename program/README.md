# Keyboard MES 项目

键盘装配制造执行系统，用于软件测试与质量保证课程。

| 目录 | 内容 |
| --- | --- |
| [code](code/) | Spring Boot 后端、Vue 前端、微信小程序、测试及数据库脚本 |
| [docs](docs/README.md) | 需求、设计、接口、开发参考与整理记录 |

## 常用入口

- [需求规约](docs/requirements/需求规约.docx)
- [项目概要设计书](docs/design/项目概要设计书.docx)
- [项目运行说明](docs/development/项目说明.md)
- [API 接口文档](docs/development/API接口文档.md)
- [数据库契约](docs/development/数据库契约.md)

## 开发位置

使用 IDEA 打开 `code`，加载 `code/backend/pom.xml`。Web 前端位于 `code/frontend`，微信开发者工具导入 `code/miniprogram`。

课程项目使用根目录 Git 仓库，`code` 和 `docs` 均由该仓库管理。执行 Git 命令时请从课程根目录进入，并在暂存前检查文件清单。

业务配置、SQL、测试以及构建依赖保留在代码工程中。需求、设计和长篇使用文档统一保存在 `docs`。
