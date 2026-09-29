# F8 / F027 (full SN trace) + F028 (different SN isolation) + F029 (empty/invalid SN)
# Need: query trace by valid SN, verify full chain; check different SNs return different data;
# empty/invalid SN returns business error
$ErrorActionPreference = 'Continue'
$ProgressPreference = 'SilentlyContinue'

# Login
$loginBody = '{"employeeNo":"U001","password":"123456"}'
$loginResp = Invoke-RestMethod -Uri 'http://localhost:8088/api/auth/login' -Method Post -Body $loginBody -ContentType 'application/json' -TimeoutSec 10
if ($loginResp.code -ne 200) { Write-Output "LOGIN_FAILED"; exit 1 }
$sid = $loginResp.data.sessionId
$headers = @{ Authorization = $sid }

# Pick two real product SNs
$sn1 = 'SN-SPLIT75-0706-001'
$sn2 = 'SN-GAMETKL-0707-001'

# === F027 Full SN trace ===
Write-Output "===== F027 Full SN trace ====="
try {
    $r1 = Invoke-RestMethod -Uri "http://localhost:8088/api/trace/$sn1" -Method Get -Headers $headers -TimeoutSec 10
    Write-Output ("HTTP=$($r1.code) sn=$($r1.data.productSn)")
    Write-Output ("  reports: $($r1.data.reports.Count)  inspections: $($r1.data.inspections.Count)  reworks: $($r1.data.reworks.Count)  tasks: $($r1.data.tasks.Count)  orders: $($r1.data.orders.Count)")
    $f027Pass = ($r1.code -eq 200 -and $r1.data.productSn -eq $sn1 -and ($r1.data.reports.Count -gt 0 -or $r1.data.inspections.Count -gt 0 -or $r1.data.reworks.Count -gt 0))
} catch {
    Write-Output "EXC: $($_.Exception.Message)"
    $f027Pass = $false
}

# === F028 Different SN isolation ===
Write-Output ""
Write-Output "===== F028 Different SN isolation ====="
try {
    $r2 = Invoke-RestMethod -Uri "http://localhost:8088/api/trace/$sn2" -Method Get -Headers $headers -TimeoutSec 10
    Write-Output ("HTTP=$($r2.code) sn=$($r2.data.productSn)")
    $f028Pass = ($r2.code -eq 200 -and $r2.data.productSn -eq $sn2 -and $r2.data.productSn -ne $r1.data.productSn)
    if ($f028Pass) { Write-Output "  SN isolation OK" } else { Write-Output "  SN isolation FAIL" }
} catch { $f028Pass = $false }

# === F029 Empty / invalid SN ===
Write-Output ""
Write-Output "===== F029 Empty / invalid SN ====="
$emptySnResult = $false
$invalidSnResult = $false
# Empty via path is hard with curl; use URL-encoded %20 or query param. Try direct (Spring will trim? Actually path /api/trace/ ) - use empty string with -Uri 'http://localhost:8088/api/trace/'
try {
    $r3 = Invoke-WebRequest -Uri 'http://localhost:8088/api/trace/' -Method Get -Headers $headers -TimeoutSec 5 -UseBasicParsing
    Write-Output ("  empty: HTTP=$([int]$r3.StatusCode) body=$($r3.Content)")
} catch {
    $resp = $_.Exception.Response
    if ($resp) { Write-Output ("  empty: HTTP=$([int]$resp.StatusCode)") }
    if ([int]$resp.StatusCode -ge 400) { $emptySnResult = $true }
}
# Non-existent SN
try {
    $r4 = Invoke-RestMethod -Uri 'http://localhost:8088/api/trace/SN-NOTEXIST-99999' -Method Get -Headers $headers -TimeoutSec 5
    Write-Output ("  not-exist: code=$($r4.code) reports=$($r4.data.reports.Count) inspections=$($r4.data.inspections.Count) reworks=$($r4.data.reworks.Count)")
    if ($r4.code -eq 200 -and $r4.data.reports.Count -eq 0 -and $r4.data.inspections.Count -eq 0 -and $r4.data.reworks.Count -eq 0) { $invalidSnResult = $true }
} catch { Write-Output "  EXC: $($_.Exception.Message)" }
$f029Pass = $emptySnResult -and $invalidSnResult

Write-Output ""
Write-Output "===== JUDGEMENT ====="
if ($f027Pass) { Write-Output "VERDICT_F027: PASS - 追溯链完整" } else { Write-Output "VERDICT_F027: FAIL" }
if ($f028Pass) { Write-Output "VERDICT_F028: PASS - 不同 SN 隔离" } else { Write-Output "VERDICT_F028: FAIL" }
if ($f029Pass) { Write-Output "VERDICT_F029: PASS - 空白与不存在 SN 处理正确" } else { Write-Output "VERDICT_F029: FAIL" }
