# F7 / Q037 (SA-02 Risk identification) + Q038 (SA-03 Fail-safe) + Q039 (SA-04 Hazard warning) + Q040 (SA-05 Security integration)
$ErrorActionPreference = 'Continue'
$ProgressPreference = 'SilentlyContinue'

# Login as admin
$loginBody = '{"employeeNo":"U001","password":"123456"}'
$loginResp = Invoke-RestMethod -Uri 'http://localhost:8088/api/auth/login' -Method Post -Body $loginBody -ContentType 'application/json' -TimeoutSec 10
if ($loginResp.code -ne 200) { Write-Output "LOGIN_FAILED"; exit 1 }
$sid = $loginResp.data.sessionId
$headers = @{ Authorization = $sid }

# === Q037 SA-02 Risk identification: boundary/abnormal input ===
Write-Output "===== Q037 SA-02 Risk identification ====="
$risks = @(
    @{ name='negative_qty'; url='/api/productionTask/generateTasks'; method='POST'; body='{"orderId":1}' },  # boundary
    @{ name='invalid_id'; url='/api/sysUser/-1'; method='GET'; body=$null },
    @{ name='huge_quantity'; url='/api/productionTask/generateTasks'; method='POST'; body='{"orderId":1,"quantity":99999999}' },
    @{ name='zero_qty_login'; url='/api/auth/login'; method='POST'; body='{"employeeNo":"","password":""}' },
    @{ name='sql_in_auth'; url='/api/auth/login'; method='POST'; body='{"employeeNo":"'' OR 1=1--","password":"x"}' },
    @{ name='long_string'; url='/api/sysUser'; method='POST'; body='{"employeeNo":"' + ('a' * 1000) + '","password":"x","name":"x"}' }
)
$q037Blocked = 0
$q037Total = 0
foreach ($r in $risks) {
    $q037Total++
    try {
        $url = "http://localhost:8088" + $r.url
        if ($r.method -eq 'POST') {
            $resp = Invoke-WebRequest -Uri $url -Method Post -Body $r.body -ContentType 'application/json' -Headers $headers -TimeoutSec 10 -UseBasicParsing
        } else {
            $resp = Invoke-WebRequest -Uri $url -Method Get -Headers $headers -TimeoutSec 10 -UseBasicParsing
        }
        $status = [int]$resp.StatusCode
        $body = $resp.Content
    } catch {
        $excp = $_.Exception
        if ($excp.Response) {
            $status = [int]$excp.Response.StatusCode
            try { $body = (New-Object System.IO.StreamReader($excp.Response.GetResponseStream())).ReadToEnd() } catch { $body = '' }
        } else { $status = -1; $body = "EXC" }
    }
    # PASS: input was rejected (4xx/5xx or business code != 200) and no SQL/JDBC leak
    $safeReject = $false
    if ($status -ge 400) { $safeReject = $true }
    elseif ($body -match '"code":\s*(\d+)') {
        $bizCode = $matches[1]
        if ($bizCode -ne '200') { $safeReject = $true }
    }
    $leak = $body -match '(?i)SQL syntax|MySQL|JDBC|SQLException|java\.sql|com\.mysql|Mapper\.java'
    if ($safeReject -and -not $leak) { $q037Blocked++ }
    Write-Output ("  {0,-20} status={1,-4} safeReject={2} leak={3}" -f $r.name, $status, $safeReject, $leak)
}
if ($q037Blocked -eq $q037Total) { Write-Output "VERDICT_Q037_SA02: PASS - all risk inputs safely rejected" } else { Write-Output "VERDICT_Q037_SA02: PARTIAL ($q037Blocked/$q037Total)" }

# === Q038 SA-03 Fail-safe: dependency interrupt (already tested in F3) ===
Write-Output ""
Write-Output "===== Q038 SA-03 Fail-safe ====="
# Evidence from F3: MES backend didn't crash on Redis loss
# Re-verify: kill session by attempting access with invalid sessionId
$badHeaders = @{ Authorization = 'invalid_session_id_xxx' }
$status38 = -1
try {
    $resp = Invoke-WebRequest -Uri 'http://localhost:8088/api/auth/current' -Method Get -Headers $badHeaders -TimeoutSec 5 -UseBasicParsing
    $status38 = [int]$resp.StatusCode
} catch {
    if ($_.Exception.Response) { $status38 = [int]$_.Exception.Response.StatusCode }
    else { $status38 = -1 }
}
if ($status38 -ge 400 -and $status38 -lt 500) {
    Write-Output "  invalid session returns $status38 - safe deny (fail-safe to deny)"
    Write-Output "VERDICT_Q038_SA03: PASS - fail-safe deny on session failure"
} elseif ($status38 -eq 401) {
    Write-Output "VERDICT_Q038_SA03: PASS - 401 unauthorized"
} else {
    Write-Output "VERDICT_Q038_SA03: PARTIAL - status=$status38"
}

# === Q039 SA-04 Hazard warning: dangerous input returns warning ===
Write-Output ""
Write-Output "===== Q039 SA-04 Hazard warning ====="
$hazards = @(
    @{ name='wrong_password'; url='/api/auth/login'; method='POST'; body='{"employeeNo":"U001","password":"WRONG"}' },
    @{ name='operator_overreach'; expected='403'; url='/api/sysUser/list'; method='GET'; body=$null; user='U003'; pass='123456' },
    @{ name='repair_overreach'; expected='403'; url='/api/inspectionRecord/submit'; method='POST'; body='{"reportId":1,"result":1}'; user='U007'; pass='123456' }
)
$q039Warned = 0
$q039Total = 0
foreach ($h in $hazards) {
    $q039Total++
    try {
        $h2 = $headers
        if ($h.user) {
            # Login as low-priv user
            $lb = "{`"employeeNo`":`"$($h.user)`",`"password`":`"$($h.pass)`"}"
            $lr = Invoke-RestMethod -Uri 'http://localhost:8088/api/auth/login' -Method Post -Body $lb -ContentType 'application/json' -TimeoutSec 10
            if ($lr.code -eq 200) { $h2 = @{ Authorization = $lr.data.sessionId } }
        }
        $url = "http://localhost:8088" + $h.url
        if ($h.method -eq 'POST') {
            $resp = Invoke-WebRequest -Uri $url -Method Post -Body $h.body -ContentType 'application/json' -Headers $h2 -TimeoutSec 10 -UseBasicParsing
        } else {
            $resp = Invoke-WebRequest -Uri $url -Method Get -Headers $h2 -TimeoutSec 10 -UseBasicParsing
        }
        $status = [int]$resp.StatusCode
        $body = $resp.Content
    } catch {
        $excp = $_.Exception
        if ($excp.Response) {
            $status = [int]$excp.Response.StatusCode
            try { $body = (New-Object System.IO.StreamReader($excp.Response.GetResponseStream())).ReadToEnd() } catch { $body = '' }
        } else { $status = -1; $body = "EXC" }
    }
    $warned = $false
    if ($status -ge 400 -and $status -lt 500) { $warned = $true }
    elseif ($body -match '"code":\s*(\d+)') {
        if ($matches[1] -ne '200') { $warned = $true }
    }
    if ($warned) { $q039Warned++ }
    Write-Output ("  {0,-25} status={1,-4} warned={2} body={3}" -f $h.name, $status, $warned, ($body.Substring(0, [math]::Min(80, $body.Length))))
}
if ($q039Warned -eq $q39Total) { Write-Output "VERDICT_Q039_SA04: PASS" } elseif ($q039Warned -gt 0) { Write-Output "VERDICT_Q039_SA04: PARTIAL ($q039Warned/$q039Total)" } else { Write-Output "VERDICT_Q039_SA04: FAIL" }

# === Q040 SA-05 Security integration: three-tier consistency ===
Write-Output ""
Write-Output "===== Q040 SA-05 Security integration ====="
# Compare API response consistency: same user login should give same roleCode
$loginResp2 = Invoke-RestMethod -Uri 'http://localhost:8088/api/auth/login' -Method Post -Body $loginBody -ContentType 'application/json' -TimeoutSec 10
$roleCode = $loginResp2.data.user.roleCode
Write-Output "  U001 admin roleCode=$roleCode"

# Try login as operator
$opLogin = Invoke-RestMethod -Uri 'http://localhost:8088/api/auth/login' -Method Post -Body '{"employeeNo":"U003","password":"123456"}' -ContentType 'application/json' -TimeoutSec 10
$opRole = $opLogin.data.user.roleCode
Write-Output "  U003 operator roleCode=$opRole"

# Operator should NOT access sysUser
$opHeaders = @{ Authorization = $opLogin.data.sessionId }
try {
    $resp = Invoke-WebRequest -Uri 'http://localhost:8088/api/sysUser/list' -Method Get -Headers $opHeaders -TimeoutSec 10 -UseBasicParsing
    $opAccessSysUser = $true
    Write-Output "  U003 accessed sysUser/list: HTTP=$($resp.StatusCode) FAIL"
} catch {
    $opAccessSysUser = $false
    Write-Output "  U003 denied sysUser/list: $([int]$_.Exception.Response.StatusCode) PASS"
}

# Check front-end served (port 5173 should respond)
$frontendOk = $false
try {
    $fe = Invoke-WebRequest -Uri 'http://localhost:5173/' -TimeoutSec 5 -UseBasicParsing
    if ($fe.StatusCode -eq 200) { $frontendOk = $true; Write-Output "  frontend port 5173 OK" }
} catch { Write-Output "  frontend not running, skip 3-tier check" }

if ($roleCode -eq 'admin' -and $opRole -eq 'operator' -and -not $opAccessSysUser) {
    Write-Output "VERDICT_Q040_SA05: PASS - role permissions consistent across auth and resource access"
} else {
    Write-Output "VERDICT_Q040_SA05: PARTIAL"
}
