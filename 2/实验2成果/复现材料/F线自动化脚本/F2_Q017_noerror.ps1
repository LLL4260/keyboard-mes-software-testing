# F2 / Q017 无错性验收：循环调用 API 1 万次统计错误率（顺序版）
# 需求 RE-01：在规定条件下和规定时间内，软件在指定条件下运行不发生失效
$ErrorActionPreference = 'Continue'
$ProgressPreference = 'SilentlyContinue'

# 登录
$loginBody = '{"employeeNo":"U001","password":"123456"}'
try {
    $loginResp = Invoke-RestMethod -Uri 'http://localhost:8088/api/auth/login' -Method Post -Body $loginBody -ContentType 'application/json' -TimeoutSec 10
    if ($loginResp.code -ne 200) { Write-Output "LOGIN_FAILED: code=$($loginResp.code)"; exit 1 }
    $sid = $loginResp.data.sessionId
    Write-Output "Login OK: sid=$($sid.Substring(0,8))..."
} catch {
    Write-Output "LOGIN_EXC: $($_.Exception.Message)"
    exit 1
}
$headers = @{ Authorization = $sid }

$TOTAL = 10000
$success = 0
$fail = 0
$firstErrors = @()
$failReasons = @{}
$start = Get-Date

for ($i = 0; $i -lt $TOTAL; $i++) {
    try {
        $resp = Invoke-RestMethod -Uri 'http://localhost:8088/api/auth/current' -Method Get -Headers $headers -TimeoutSec 5
        if ($resp.code -eq 200 -and $resp.data.employeeNo -eq 'U001') {
            $success++
        } else {
            $fail++
            if ($firstErrors.Count -lt 10) { $firstErrors += "i=$i code=$($resp.code)" }
            $key = "code=$($resp.code)"
            if (-not $failReasons.ContainsKey($key)) { $failReasons[$key] = 0 }
            $failReasons[$key]++
        }
    } catch {
        $fail++
        $msg = $_.Exception.Message
        if ($firstErrors.Count -lt 10) { $firstErrors += "i=$i exc=$msg" }
        $key = "exc:$($msg.Substring(0, [math]::Min(40, $msg.Length)))"
        if (-not $failReasons.ContainsKey($key)) { $failReasons[$key] = 0 }
        $failReasons[$key]++
    }
    if (($i + 1) % 1000 -eq 0) { Write-Output "Progress: $($i+1)/$TOTAL  success=$success fail=$fail" }
}

$end = Get-Date
$elapsed = ($end - $start).TotalSeconds
$errorRate = [math]::Round($fail / $TOTAL * 100, 4)
$avgMs = [math]::Round($elapsed * 1000 / $TOTAL, 3)
$rps = [math]::Round($TOTAL / $elapsed, 2)

Write-Output "===== Q017 RESULT ====="
Write-Output "Total: $TOTAL"
Write-Output "Success: $success"
Write-Output "Fail: $fail"
Write-Output "ErrorRate%: $errorRate"
Write-Output "ElapsedSec: $([math]::Round($elapsed,2))"
Write-Output "AvgMs: $avgMs"
Write-Output "Rps: $rps"
Write-Output "FirstErrors:"
if ($firstErrors.Count -eq 0) { Write-Output " (none)" } else { $firstErrors | ForEach-Object { Write-Output " $_" } }
Write-Output "FailReasons:"
if ($failReasons.Count -eq 0) { Write-Output " (none)" } else { $failReasons.GetEnumerator() | ForEach-Object { Write-Output ("  {0} = {1}" -f $_.Key, $_.Value) } }

if ($errorRate -eq 0) {
    Write-Output "VERDICT: PASS"
    exit 0
} elseif ($errorRate -lt 1) {
    Write-Output "VERDICT: PASS_NOTE"
    exit 0
} else {
    Write-Output "VERDICT: FAIL"
    exit 2
}
