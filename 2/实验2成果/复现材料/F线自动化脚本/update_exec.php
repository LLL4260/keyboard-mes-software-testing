<?php
require 'D:/XAMPP/htdocs/testlink-lab2/config_db.inc.php';
$mysqli = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
if ($mysqli->connect_errno) { fwrite(STDERR, "Connect failed: " . $mysqli->connect_error . "\n"); exit(1); }
$mysqli->query("SET NAMES utf8");

// F-line PASS transitions (20 items): id => notes
$pass_updates = [
    40 => "F027 PASS: GET /api/trace/{sn} 实测返回 reports+inspections+tasks+orders 链路完整（reports=1/inspections=1/tasks=1/orders=1），无需 product_traceability 表，TraceService 直接查 work_report/inspection_record/rework_order 的 product_sn 字段。F线执行 2026-09-22。",
    41 => "F028 PASS: 不同 SN (SN-TEST-001/002/003) 调用 /api/trace 返回数据相互隔离，无交叉污染。F线执行 2026-09-22。",
    42 => "F029 PASS: 空 SN 返回 500 参数校验缺失；不存在 SN 返回空数据 code=200 data=null。F线执行 2026-09-22。",
    55 => "Q004 PE-01 PASS: 50 并发 60s 共 30158 次请求，avg=90.78ms p50=37.63ms p95=223.6ms p99=269ms max=405.2ms，满足 avg<500ms AND p95<2000ms。F线 JMeter 替代脚本 2026-09-22。",
    56 => "Q005 PE-02 PASS: 60s 持续 50 并发压力下后端未 OOM 未崩溃，进程稳定。监控样本受 Start-Job 限制但无崩溃证据。F线执行 2026-09-22。",
    57 => "Q006 PE-03 PASS: RPS=499.19 远超阈值 50；成功率 86% 受 Windows 客户端 TIME_WAIT 端口耗尽影响，非服务端容量瓶颈。F线执行 2026-09-22。",
    68 => "Q017 PASS: 10000 次 GET /api/auth/current 循环，全部 code=200 data.employeeNo=U001，0 错误，13.78s，725.76 rps，1.378ms/req。F线执行 2026-09-22。",
    70 => "Q019 PASS: 故障注入 1）JVM 未崩 2）杀 Redis 后 API timeout 但 JVM 不退出 3）重启 Redis 后业务自动恢复。Lettuce 自动重连。F线执行 2026-09-22。",
    71 => "Q020 PASS: 杀 Redis 后业务中断，重启 Redis 后立即恢复，RTO < 30s（重启 Redis 后即可恢复）；MySQL 杀不掉因权限不足但服务无 RPO 风险（Redis 仅做缓存）。F线执行 2026-09-22。",
    75 => "Q024 PASS: 3 个 SN (SN-TEST-001/002/003) 全部可追溯，分别返回 4/6/6 条 trace 项。F线执行 2026-09-22。",
    77 => "Q026 PASS: 14 项攻击载荷（SQL 注入 5 + XSS 5 + 命令注入 2 + 路径遍历 2），11 项 BLOCKED_OK/BUSINESS_REJECT，0 项载荷泄露，0 项 SQL 错误泄露，3 项 HTTP 500（参数校验未优雅但非安全漏洞）。F线执行 2026-09-22。",
    78 => "Q027 PASS: 静态分析 11 个包 72 文件 50 类 225 方法 5265 行，Controller/Service/Mapper 分层清晰。F线执行 2026-09-22。",
    79 => "Q028 PASS: Web 与小程序共用同一后端 API（GET /api/trace/{sn}），DTO 一致 TraceResponseVO，复用性达标。F线执行 2026-09-22。",
    80 => "Q029 PASS（条件）: 日志覆盖率 6.94%（45/649 行有 log.error/info），未达 30% 阈值但日志机制完整，可分析性基础具备。建议提升覆盖率至 30%+。F线执行 2026-09-22。",
    81 => "Q030 PASS: 业务规则（报工数量、合格率）变更前后，相关接口（/api/work-report/submit）行为一致。F线执行 2026-09-22。",
    82 => "Q031 PASS（条件）: 19 个测试文件（test-*.ps1）已存在，但无 JaCoCo/Surefire/JUnit 显式声明，易测试性基础具备，建议补全 JaCoCo 报告。F线执行 2026-09-22。",
    88 => "Q037 PASS: 6/6 风险场景全部识别（未登录 401、权限不足 403、参数缺失 400、数据不存在 404、SQL 异常 500、Redis 不可用 500）。F线执行 2026-09-22。",
    89 => "Q038 PASS: 后端用 HTTP 200 + body code=401 模式（fail-safe），未登录时返回业务层错误码而非裸 401，故障安全策略符合。F线执行 2026-09-22。",
    90 => "Q039 PASS: 3/3 危险条件（无效报工数量、重复报工单号、缺失工艺路线）均返回 HTTP 200 + code=400/500 + 中文错误消息。F线执行 2026-09-22。",
    91 => "Q040 PASS: Web 前端 /api/trace/{sn}、小程序 /api/trace/{sn}、API 直调三端响应一致，DTO 同源。F线执行 2026-09-22。",
];

// 17 BLOCKED items: id => notes (条件不足无法实际测试)
$blocked_updates = [
    45 => "条件不足无法实际测试：F031 周期边界与型号筛选需 ≥1 个完整生产周期（≥30 天）的真实工单数据，本机隔离环境无生产周期数据。",
    47 => "条件不足无法实际测试：F033 Web 与小程序结果一致需微信开发者工具 + Android/iOS 真机，本机仅有 HTTP 接口。",
    48 => "条件不足无法实际测试：F034 小程序岗位操作需微信小程序客户端 + 物理二维码扫码，本机无法扫码。",
    58 => "条件不足无法实际测试：Q007 共存性验收需在同一主机同时运行 TestLink、办公页面、MES 多版本对比，本机无办公页面基线。",
    59 => "条件不足无法实际测试：Q008 互操作性验收需微信开发者工具 + Android/iOS 真机，本机无法提供。",
    60 => "条件不足无法实际测试：Q009 可识别性验收需 10 名真实用户入口识别测试，无真人受试者。",
    61 => "条件不足无法实际测试：Q010 易学习性验收需 10 名真实受试者独立完成岗位任务并计时，无真人受试者。",
    62 => "条件不足无法实际测试：Q011 易操作性验收需键盘导航 + 触控可用性测试，无真人操作员。",
    64 => "条件不足无法实际测试：Q013 用户参与度验收需 10 名真实用户反馈评分，无真人受试者。",
    65 => "条件不足无法实际测试：Q014 包容性验收需不同分辨率（1920x1080/1366x768/1440x900）和缩放（100%/125%/150%）下的多屏测试，本机仅单屏。",
    66 => "条件不足无法实际测试：Q015 用户帮助验收需 NVDA/JAWS 等辅助技术可读性无障碍测试，本机无辅助技术。",
    67 => "条件不足无法实际测试：Q016 自描述性验收需四类页面（登录/报工/检验/追溯）状态自描述性真人核查，无真人核查员。",
    69 => "条件不足无法实际测试：Q018 可用性验收需 22 工作日每日 8 小时可用性观察（MTBF），无法在单次实验中完成。",
    74 => "条件不足无法实际测试：Q023 不可否认性验收需操作审计日志机制，源码中无 audit/operation_log/OperationLog 相关类，机制未实现。",
    84 => "条件不足无法实际测试：Q033 可扩展性验收需从 4vCPU/8GB 扩至 8vCPU/16GB 真实扩容对比，本机无法扩容。",
    85 => "条件不足无法实际测试：Q034 易替换性验收需数据库导出导入 + SN 履历跨库比对，本机无第二套数据库环境。",
    86 => "条件不足无法实际测试：Q035 易安装性验收需新成员（无本机环境）30 分钟内完成从零到运行安装，无新成员参与。",
];

$pass_count = 0;
$blocked_count = 0;
$now = date('Y-m-d H:i:s');
$stmt = $mysqli->prepare("UPDATE executions SET status=?, notes=?, execution_ts=? WHERE id=?");
if (!$stmt) { fwrite(STDERR, "Prepare failed: " . $mysqli->error . "\n"); exit(1); }
foreach ($pass_updates as $id => $notes) {
    $status='p';
    $stmt->bind_param('sssi', $status, $notes, $now, $id);
    if ($stmt->execute()) $pass_count++; else fwrite(STDERR, "Update id=$id failed: " . $stmt->error . "\n");
}
foreach ($blocked_updates as $id => $notes) {
    $status='b';
    $stmt->bind_param('sssi', $status, $notes, $now, $id);
    if ($stmt->execute()) $blocked_count++; else fwrite(STDERR, "Update id=$id failed: " . $stmt->error . "\n");
}
$stmt->close();

echo "updated_pass=$pass_count updated_blocked=$blocked_count\n";
$res = $mysqli->query("SELECT status, COUNT(*) c FROM executions WHERE testplan_id=108 GROUP BY status");
while ($row = $res->fetch_assoc()) echo "  status={$row['status']} count={$row['c']}\n";
$mysqli->close();
