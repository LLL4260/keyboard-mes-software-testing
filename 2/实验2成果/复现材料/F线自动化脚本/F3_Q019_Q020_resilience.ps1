# F3 / Q019 (RE-03 Fault tolerance) + Q020 (RE-04 Recoverability)
# Strategy:
# 1) Kill Redis, call API, expect graceful degradation (no JVM crash)
# 2) Restart Redis, call API, expect recovery
# 3) Kill MySQL, call API, expect graceful degradation
# 4) Restart MySQL, call API, expect recovery
$ErrorActionPreference = 'Continue'
$ProgressPreference = 'SilentlyContinue'

function Get-Session {
    $loginBody = '{"employeeNo":"U001","password":"123456"}'
    try {
        $r = Invoke-RestMethod -Uri 'http://localhost:8088/api/auth/login' -Method Post -Body $loginBody -ContentType 'application/json' -TimeoutSec 10
        if ($r.code -eq 200) { return $r.data.sessionId }
    } catch {}
    return $null
}

function Test-Api($name, $sid) {
    try {
        $h = @{ Authorization = $sid }
        $r = Invoke-RestMethod -Uri 'http://localhost:8088/api/auth/current' -Method Get -Headers $h -TimeoutSec 5
        return @{ Name=$name; Status='OK'; Code=$r.code; User=$r.data.employeeNo }
    } catch {
        $resp = $_.Exception.Response
        if ($resp) {
            try { $body = (New-Object System.IO.StreamReader($resp.GetResponseStream())).ReadToEnd() } catch { $body = '' }
            return @{ Name=$name; Status='HTTP_ERR'; HttpCode=[int]$resp.StatusCode; BodyLen=$body.Length }
        }
        return @{ Name=$name; Status='EXC'; Msg=$_.Exception.Message }
    }
}

# === Q019 Fault tolerance ===
Write-Output "===== Q019 Fault tolerance ====="
$sid = Get-Session
if (-not $sid) { Write-Output "PRE_LOGIN_FAILED"; exit 1 }
$baseline = Test-Api 'baseline' $sid
Write-Output ("  baseline: {0}" -f ($baseline | ConvertTo-Json -Compress))

# Step 1: Kill Redis
$redisProc = Get-Process -Name 'redis-server' -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $redisProc) {
    Write-Output "  Redis not running, skip Q019/Q020"
    exit 1
}
$redisPid = $redisProc.Id
$redisExe = $redisProc.Path
Write-Output ("  Redis PID={0} exe={1}, killing..." -f $redisPid, $redisExe)
Stop-Process -Id $redisPid -Force
Start-Sleep -Seconds 2
$r1 = Test-Api 'after_redis_kill' $sid
Write-Output ("  after_redis_kill: {0}" -f ($r1 | ConvertTo-Json -Compress))
Start-Sleep -Seconds 3
$r2 = Test-Api 'after_redis_kill_3s' $sid
Write-Output ("  after_redis_kill_3s: {0}" -f ($r2 | ConvertTo-Json -Compress))

# === Q020 Recoverability ===
Write-Output ""
Write-Output "===== Q020 Recoverability ====="
Write-Output ("  restart Redis: {0}" -f $redisExe)
if ($redisExe -and (Test-Path $redisExe)) {
    Start-Process -FilePath $redisExe -ArgumentList '--port','6379' -WindowStyle Hidden
    Start-Sleep -Seconds 3
    $r3 = Test-Api 'after_redis_restart' $sid
    Write-Output ("  after_redis_restart: {0}" -f ($r3 | ConvertTo-Json -Compress))
} else {
    Write-Output "  redisExe not found, skip restart"
}

# Step 3: Kill MySQL
Write-Output ""
Write-Output "===== Q019 part 2 (MySQL) ====="
$mysqlProc = Get-Process -Name 'mysqld' -ErrorAction SilentlyContinue | Select-Object -First 1
$mysqlExe = $null
if ($mysqlProc) {
    $mysqlPid = $mysqlProc.Id
    $mysqlExe = $mysqlProc.Path
    Write-Output ("  MySQL PID={0} exe={1}, killing..." -f $mysqlPid, $mysqlExe)
    Stop-Process -Id $mysqlPid -Force
    Start-Sleep -Seconds 3
    $r4 = Test-Api 'after_mysql_kill' $sid
    Write-Output ("  after_mysql_kill: {0}" -f ($r4 | ConvertTo-Json -Compress))
} else {
    Write-Output "  MySQL not running, skip"
}

# Step 4: Restart MySQL
Write-Output ""
Write-Output "===== Q020 part 2 (MySQL restart) ====="
if ($mysqlExe -and (Test-Path $mysqlExe)) {
    Write-Output ("  restart MySQL: {0}" -f $mysqlExe)
    Start-Process -FilePath $mysqlExe -ArgumentList '--console' -WindowStyle Hidden
    $waited = 0
    while ($waited -lt 60) {
        Start-Sleep -Seconds 5
        $waited += 5
        $check = Test-NetConnection -ComputerName 127.0.0.1 -Port 3306 -WarningAction SilentlyContinue
        if ($check.TcpTestSucceeded) {
            Write-Output ("  MySQL back after {0}s" -f $waited)
            break
        }
    }
    Start-Sleep -Seconds 3
    $r5 = Test-Api 'after_mysql_restart' $sid
    Write-Output ("  after_mysql_restart: {0}" -f ($r5 | ConvertTo-Json -Compress))
} else {
    Write-Output "  mysqld.exe not found, manual restart needed"
}

# === Judgement ===
Write-Output ""
Write-Output "===== JUDGEMENT ====="
# Q019: MES backend process (java) should still be alive after killing deps
$mesAlive = Get-Process -Name 'java' -ErrorAction SilentlyContinue | Select-Object -First 1
if ($mesAlive) {
    Write-Output "MES backend alive: PASS Q019 (no JVM crash on dep loss)"
    Write-Output "VERDICT_Q019: PASS"
} else {
    Write-Output "VERDICT_Q019: FAIL"
}

# Q020: After restart, login + access should work
$sid2 = Get-Session
if ($sid2) {
    $rFinal = Test-Api 'final' $sid2
    if ($rFinal.Code -eq 200 -and $rFinal.User -eq 'U001') {
        Write-Output "VERDICT_Q020: PASS - business recovered"
    } else {
        Write-Output ("VERDICT_Q020: FAIL - access anomaly: {0}" -f ($rFinal | ConvertTo-Json -Compress))
    }
} else {
    Write-Output "VERDICT_Q020: FAIL - cannot login after recovery"
}
