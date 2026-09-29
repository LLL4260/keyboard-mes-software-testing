# F4 / Q026 防御性验收：SQL 注入 / XSS / 路径穿越测试
# 需求 SE-06：保护信息和数据的能力，使经授权的人员和系统在需要时能进行正常的访问
$ErrorActionPreference = 'Continue'
$ProgressPreference = 'SilentlyContinue'

# 登录
$loginBody = '{"employeeNo":"U001","password":"123456"}'
$loginResp = Invoke-RestMethod -Uri 'http://localhost:8088/api/auth/login' -Method Post -Body $loginBody -ContentType 'application/json' -TimeoutSec 10
$sid = $loginResp.data.sessionId
$headers = @{ Authorization = $sid }

# 攻击载荷清单
$payloads = @(
    @{ name='SQLi_login_quote'; url='/api/auth/login'; method='POST'; body='{"employeeNo":"'' OR ''1''=''1","password":"x"}' },
    @{ name='SQLi_login_union'; url='/api/auth/login'; method='POST'; body='{"employeeNo":"x'' UNION SELECT 1--","password":"x"}' },
    @{ name='SQLi_login_sleep'; url='/api/auth/login'; method='POST'; body='{"employeeNo":"x''; SLEEP(5)--","password":"x"}' },
    @{ name='SQLi_login_drop'; url='/api/auth/login'; method='POST'; body='{"employeeNo":"x''; DROP TABLE sys_user--","password":"x"}' },
    @{ name='XSS_login_script'; url='/api/auth/login'; method='POST'; body='{"employeeNo":"<script>alert(1)</script>","password":"x"}' },
    @{ name='XSS_login_img'; url='/api/auth/login'; method='POST'; body='{"employeeNo":"<img src=x onerror=alert(1)>","password":"x"}' },
    @{ name='SQLi_user_param'; url='/api/sysUser/1%20OR%201=1'; method='GET'; body=$null },
    @{ name='SQLi_user_union'; url='/api/sysUser/0%20UNION%20SELECT%20password%20FROM%20sys_user'; method='GET'; body=$null },
    @{ name='XSS_user_param'; url='/api/sysUser/%3Cscript%3Ealert(1)%3C%2Fscript%3E'; method='GET'; body=$null },
    @{ name='PathTraversal'; url='/api/sysUser/..%2F..%2F..%2Fetc%2Fpasswd'; method='GET'; body=$null },
    @{ name='CmdInjection'; url='/api/sysUser/1;cat%20/etc/passwd'; method='GET'; body=$null },
    @{ name='LDAP_injection'; url='/api/auth/login'; method='POST'; body='{"employeeNo":"*)(uid=*))(|(uid=*","password":"x"}' },
    @{ name='XSS_report_param'; url='/api/report/overview?start=%3Cscript%3Ealert(1)%3C%2Fscript%3E'; method='GET'; body=$null },
    @{ name='SQLi_report_param'; url='/api/report/overview?start=2026-01-01%27%20OR%201=1--'; method='GET'; body=$null }
)

$results = @()
$attacksBlocked = 0
$leakedPayload = 0
$serverError = 0
$sqlLeakTotal = 0

foreach ($p in $payloads) {
    $url = "http://localhost:8088" + $p.url
    $status = -1
    $body = ''
    try {
        if ($p.method -eq 'POST') {
            $resp = Invoke-WebRequest -Uri $url -Method Post -Body $p.body -ContentType 'application/json' -Headers $headers -TimeoutSec 10 -UseBasicParsing
            $status = [int]$resp.StatusCode
            $body = $resp.Content
        } else {
            $resp = Invoke-WebRequest -Uri $url -Method Get -Headers $headers -TimeoutSec 10 -UseBasicParsing
            $status = [int]$resp.StatusCode
            $body = $resp.Content
        }
    } catch {
        $excp = $_.Exception
        if ($excp.Response) {
            $status = [int]$excp.Response.StatusCode
            try { $body = (New-Object System.IO.StreamReader($excp.Response.GetResponseStream())).ReadToEnd() } catch {}
        } else {
            $status = -1
            $body = "EXC:" + $excp.Message
        }
    }

    # 判断是否泄露攻击载荷原文
    $leaked = $false
    if ($p.body -and $body -and $body.Contains($p.body)) { $leaked = $true; $leakedPayload++ }
    # 判断是否泄露 SQL/JDBC 错误细节
    $sqlLeak = $false
    if ($body -match "(?i)SQL syntax|MySQL|JDBC|StatementCallback|SQLException|java\.sql|com\.mysql|Hibernate|MyBatis|Mapper\.java|Service\.java|Controller\.java|nested exception|root cause") { $sqlLeak = $true; $sqlLeakTotal++ }

    $verdict = 'UNKNOWN'
    if ($status -ge 400 -and $status -lt 500) {
        $verdict = 'BLOCKED_OK'; $attacksBlocked++
    } elseif ($status -eq 200) {
        try { $j = $body | ConvertFrom-Json; if ($j.code -ne 200) { $verdict = 'BUSINESS_REJECT'; $attacksBlocked++ } else { $verdict = 'TOLERATED'; if (-not $leaked -and -not $sqlLeak) { $attacksBlocked++ } } } catch { $verdict = 'NON_JSON_200'; $serverError++ }
    } elseif ($status -ge 500) {
        $verdict = 'SERVER_ERROR'; $serverError++
    } elseif ($status -eq -1) {
        $verdict = 'NETWORK_EXC'; $serverError++
    }
    $results += [PSCustomObject]@{ Name=$p.name; Status=$status; Verdict=$verdict; Leaked=$leaked; SQLLeak=$sqlLeak }
}

Write-Output "===== Q026 RESULT ====="
Write-Output "TotalPayloads: $($payloads.Count)"
Write-Output "AttacksBlocked: $attacksBlocked"
Write-Output "PayloadLeaked: $leakedPayload"
Write-Output "SQLLeakTotal: $sqlLeakTotal"
Write-Output "ServerErrors: $serverError"
Write-Output "Details:"
$results | ForEach-Object { Write-Output ("  {0,-25} HTTP={1,-4} {2,-15} leaked={3} sqleak={4}" -f $_.Name, $_.Status, $_.Verdict, $_.Leaked, $_.SQLLeak) }

if ($leakedPayload -gt 0 -or $serverError -gt 0) {
    Write-Output "VERDICT: FAIL - 存在载荷泄露或服务异常"
    exit 2
} elseif ($attacksBlocked -eq $payloads.Count) {
    Write-Output "VERDICT: PASS - 全部攻击载荷被有效防御或拒绝"
    exit 0
} else {
    Write-Output "VERDICT: PARTIAL - 部分载荷未明确防御"
    exit 1
}
