# 第三章 · SpotBugs 与 Alibaba Java Coding Guidelines

先在同一源码版本上用 JDK 17 编译后端，再分别检查源码与字节码。阿里规约检查采用 P3C PMD 2.1.1 和 PMD 6.55.0；SpotBugs 4.10.4 分析编译后的类，并加载 Maven 项目的依赖类路径。保存工具版本、扫描范围、规则、缺失类或分析错误数。比较两次运行时保持输入范围和配置一致。

| 成员 | 阿里规约检查对象 | SpotBugs 核查线索 |
| --- | --- | --- |
| A | WorkReportMapper 的接口和方法注释 | SB-009：检查 ReportServiceImpl 如何保存并使用 workReportMapper，结合构造器及查询调用解释判断。 |
| B | 工单相关接口声明和注释 | SB-011：检查 TraceServiceImpl 的 productionOrderMapper 是谁创建的、如何注入、是否对外返回。 |
| C | 任务相关接口声明和注释 | SB-007：检查 ReportServiceImpl 如何通过 productionTaskMapper 查询任务。 |
| D | 检验相关接口声明和注释 | SB-004：检查 ReportServiceImpl 中 inspectionRecordMapper 的生命周期和可见性。 |
| E | 认证和会话相关接口 | SB-002、SB-003：核对 AuthSessionService 持有的 ObjectMapper、Redis 模板是否共享，查找初始化后修改配置的调用。分别写两个字段的结论。 |

### 如何读告警

2026-10-11 团队两次用 P3C 规则扫描 72 个 Java 源文件，均记录 0 项告警，该结果限于所启用的规则。SpotBugs 分析 73 个类，15 项告警均为 `EI_EXPOSE_REP2`，分析错误和缺失类均为 0。该规则提示对象保存了来自外部的可变引用，需结合框架管理方式判断影响。

人工核查将 13 项判为框架场景误报，其中 A、B、C、D 对应的字段持有容器注入的 Mapper 代理。E 的两项共享组件具有可变配置，团队保留为设计风险。同范围复查仍报告 15 项，团队未修改源码或抑制规则。旧 FindBugs 和早期回归记录属于其他批次。

**报告取证**　用“编号—类与字段—代码事实—判断—复查状态”记录自己负责的项。优先展示该告警的定位或调用依据。两种工具的结果各给一行，并保留源码文件数和字节码类数各自的单位。
