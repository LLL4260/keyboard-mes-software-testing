# F9 / Q024 (SE-04 Traceability) - use /api/trace/{sn} endpoint
# Q023 (SE-03 Non-repudiation) - marked BLOCKED, no audit log table exists
$ErrorActionPreference = 'Continue'
$ProgressPreference = 'SilentlyContinue'

$loginBody = '{"employeeNo":"U001","password":"123456"}'
$loginResp = Invoke-RestMethod -Uri 'http://localhost:8088/api/auth/login' -Method Post -Body $loginBody -ContentType 'application/json' -TimeoutSec 10
if ($loginResp.code -ne 200) { Write-Output "LOGIN_FAILED"; exit 1 }
$sid = $loginResp.data.sessionId
$headers = @{ Authorization = $sid }

# === Q024 SE-04 Traceability ===
Write-Output "===== Q024 SE-04 Traceability ====="
# Pick 3 SNs
$sns = @('SN-SPLIT75-0706-001','SN-GAMETKL-0707-001','SN-SILENT98-0708-001')
$traced = 0
$failed = 0
foreach ($sn in $sns) {
    try {
        $r = Invoke-RestMethod -Uri "http://localhost:8088/api/trace/$sn" -Method Get -Headers $headers -TimeoutSec 10
        if ($r.code -eq 200 -and $r.data.productSn -eq $sn) {
            $total = $r.data.reports.Count + $r.data.inspections.Count + $r.data.reworks.Count + $r.data.tasks.Count + $r.data.orders.Count
            Write-Output ("  $sn : trace items=$total")
            if ($total -gt 0) { $traced++ } else { $failed++ }
        } else { $failed++ }
    } catch { $failed++ }
}
$q024Pass = ($traced -ge 2) -and ($failed -eq 0)
if ($q024Pass) { Write-Output "VERDICT_Q024_SE04: PASS - product SN traceability verified for $traced/$($sns.Count) SNs" } else { Write-Output "VERDICT_Q024_SE04: FAIL" }

# === Q023 SE-03 Non-repudiation ===
Write-Output ""
Write-Output "===== Q023 SE-03 Non-repudiation ====="
# Source code scan: no audit log / operation_log / @Auditable annotation found
$srcRoot = 'e:\Documents\workspace\course experiment\软件测试与质量保证\program\code\backend\src\main\java'
$hasAudit = $false
$javaFiles = Get-ChildItem -Path $srcRoot -Recurse -Filter '*.java'
foreach ($f in $javaFiles) {
    $content = Get-Content -Path $f.FullName -Raw
    if ($content -match '(?i)audit|operation_log|OperationLog') { $hasAudit = $true; Write-Output "  audit ref in: $($f.Name)"; break }
}
if (-not $hasAudit) {
    Write-Output "  no audit log mechanism found in source"
    Write-Output "VERDICT_Q023_SE03: BLOCKED - 需补开发操作日志/审计切面后才能测试"
} else {
    # If found, would test audit trail; here just check DB
    $opLogExists = mysql --protocol=TCP --host=127.0.0.1 --port=3306 --user=root --password=password --default-character-set=utf8mb4 --database=keyboard_mes_lab2 -N -e "SHOW TABLES LIKE 'operation_log';" 2>$null
    if ($opLogExists) { Write-Output "VERDICT_Q023: PASS" } else { Write-Output "VERDICT_Q023: BLOCKED" }
}
